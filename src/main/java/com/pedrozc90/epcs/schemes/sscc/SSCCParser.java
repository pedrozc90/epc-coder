package com.pedrozc90.epcs.schemes.sscc;

import com.pedrozc90.epcs.exception.EpcParseException;
import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.sscc.enums.SSCCExtensionDigit;
import com.pedrozc90.epcs.schemes.sscc.enums.SSCCFilterValue;
import com.pedrozc90.epcs.schemes.sscc.enums.SSCCHeader;
import com.pedrozc90.epcs.schemes.sscc.enums.SSCCTagSize;
import com.pedrozc90.epcs.schemes.sscc.objects.SSCC;
import com.pedrozc90.epcs.schemes.sscc.partitionTable.SSCCPartitionTable;
import com.pedrozc90.epcs.utils.BinaryUtils;
import com.pedrozc90.epcs.utils.Converter;
import com.pedrozc90.epcs.utils.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSCCParser {

    // urn:epc:tag:sscc-96:0.952012.03456789123
    private static final Pattern TAG_URI_PATTERN = Pattern.compile("^(urn:epc:tag:sscc-)(96):([0-7])\\.(\\d+)\\.([0-9])(\\d+)$");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("^(urn:epc:id:sscc):(\\d+)\\.([0-9])(\\d+)$");

    private static final SSCCPartitionTable partitionTable = new SSCCPartitionTable();
    private static final Integer RESERVED = 0; // 24 zero bits

    private final SSCC sscc;

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private SSCCParser(final Steps steps) throws EpcParseException {
        final ParsedData data = parse(steps);
        sscc = toSSCC(data);
    }

    private ParsedData parse(final Steps steps) throws EpcParseException {
        if (steps.rfidTag != null) {
            return decodeRFIDTag(steps.rfidTag);
        } else if (steps.epcTagURI != null) {
            return decodeEpcTagURI(steps.epcTagURI);
        } else if (steps.epcPureIdentityURI != null) {
            return decodeEpcPureIdentityURI(steps.epcPureIdentityURI, steps.tagSize, steps.filterValue);
        }
        return parseCompanyPrefix(steps);
    }

    private static ParsedData decodeRFIDTag(final String rfidTag) {
        final String inputBin = BinaryUtils.toBinary(rfidTag);

        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        final SSCCHeader header = SSCCHeader.of(headerBin);
        final SSCCTagSize tagSize = SSCCTagSize.of(header.getTagSize());

        final int partitionDec = Integer.parseInt(partitionBin, 2);
        final TableItem tableItem = partitionTable.getPartitionByValue(partitionDec);

        final PrefixLength prefixLength = PrefixLength.of(tableItem.l());

        final String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        final String companyPrefixDec = Converter.binToDec(companyPrefixBin);
        final String companyPrefix = StringUtils.leftPad(companyPrefixDec, tableItem.l(), '0');

        final String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        final SSCCFilterValue filterValue = SSCCFilterValue.of(Integer.parseInt(filterDec));

        final String serialWithExtensionBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
        final String serialWithExtension = StringUtils.leftPad(Converter.binToDec(serialWithExtensionBin), tableItem.digits(), '0');

        final String extensionDec = serialWithExtension.substring(0, 1);
        final SSCCExtensionDigit extensionDigit = SSCCExtensionDigit.of(Integer.parseInt(extensionDec));

        final String serial = serialWithExtension.substring(1);

        return new ParsedData(tableItem, tagSize, filterValue, extensionDigit, prefixLength, companyPrefix, serial);
    }

    private ParsedData decodeEpcTagURI(final String epcTagURI) {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Tag URI is invalid");
        }

        final SSCCTagSize tagSize = SSCCTagSize.of(Integer.parseInt(matcher.group(2)));
        final SSCCFilterValue filterValue = SSCCFilterValue.of(Integer.parseInt(matcher.group(3)));
        final String companyPrefix = matcher.group(4);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final SSCCExtensionDigit extensionDigit = SSCCExtensionDigit.of(Integer.parseInt(matcher.group(5)));
        final String serial = matcher.group(6);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateExtensionDigitAndSerial(tableItem, extensionDigit, serial);

        return new ParsedData(tableItem, tagSize, filterValue, extensionDigit, prefixLength, companyPrefix, serial);
    }

    private ParsedData decodeEpcPureIdentityURI(final String epcPureIdentityURI, final SSCCTagSize tagSize, final SSCCFilterValue filterValue) {
        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Pure Identity is invalid");
        }

        final String companyPrefix = matcher.group(2);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final SSCCExtensionDigit extensionDigit = SSCCExtensionDigit.of(Integer.parseInt(matcher.group(3)));
        final String serial = matcher.group(4);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateExtensionDigitAndSerial(tableItem, extensionDigit, serial);

        return new ParsedData(tableItem, tagSize, filterValue, extensionDigit, prefixLength, companyPrefix, serial);
    }

    private ParsedData parseCompanyPrefix(final Steps steps) {
        final PrefixLength prefixLength = PrefixLength.of(steps.companyPrefix.length());
        validateCompanyPrefix(prefixLength);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
        validateExtensionDigitAndSerial(tableItem, steps.extensionDigit, steps.serial);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, steps.extensionDigit, prefixLength, steps.companyPrefix, steps.serial);
    }

    private SSCC toSSCC(final ParsedData data) {
        final Integer checkDigit = getCheckDigit(data.extensionDigit, data.companyPrefix, data.serial);

        final String outputBin = toBinary(data);
        final String outputHex = BinaryUtils.toHex(outputBin);

        return new SSCC(
            // "sscc",
            // "AI 00",
            Integer.toString(data.tagSize.getValue()),
            Integer.toString(data.filterValue.getValue()),
            Integer.toString(data.tableItem.partitionValue()),
            Integer.toString(data.prefixLength.getValue()),
            data.companyPrefix,
            Integer.toString(data.extensionDigit.getValue()),
            data.serial,
            Integer.toString(checkDigit),
            "urn:epc:id:sscc:%s.%s%s".formatted(data.companyPrefix, data.extensionDigit.getValue(), data.serial),
            "urn:epc:tag:sscc-%s:%s.%s.%s%s".formatted(data.tagSize.getValue(), data.filterValue.getValue(), data.companyPrefix, data.extensionDigit.getValue(), data.serial),
            "urn:epc:raw:%s.x%s".formatted(data.tagSize.getValue(), outputHex),
            outputBin,
            outputHex
        );
    }

    private String toBinary(final ParsedData data) {
        final StringBuilder bin = new StringBuilder();

        bin.append(Converter.decToBin(data.tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(data.filterValue.getValue(), 3));
        bin.append(Converter.decToBin(data.tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(data.companyPrefix), data.tableItem.m()));
        bin.append(Converter.decToBin(data.extensionDigit.getValue() + data.serial, data.tableItem.n()));
        bin.append(Converter.decToBin(RESERVED, 24));

        return bin.toString();
    }

    /* --- Validation --- */
    private Integer getCheckDigit(final SSCCExtensionDigit extensionDigit, final String companyPrefix, final String serial) {
        final String value = extensionDigit.getValue() + companyPrefix + serial;

        final Integer d18 = (10 - ((3
            * (Character.getNumericValue(value.charAt(0)) + Character.getNumericValue(value.charAt(2))
            + Character.getNumericValue(value.charAt(4)) + Character.getNumericValue(value.charAt(6))
            + Character.getNumericValue(value.charAt(8))
            + Character.getNumericValue(value.charAt(10)) + Character.getNumericValue(value.charAt(12))
            + Character.getNumericValue(value.charAt(14)) + Character.getNumericValue(value.charAt(16)))
            + (Character.getNumericValue(value.charAt(1)) + Character.getNumericValue(value.charAt(3))
            + Character.getNumericValue(value.charAt(5)) + Character.getNumericValue(value.charAt(7))
            + Character.getNumericValue(value.charAt(9)) + Character.getNumericValue(value.charAt(11))
            + Character.getNumericValue(value.charAt(13)) + Character.getNumericValue(value.charAt(15))))
            % 10)) % 10;

        return d18;
    }

    private void validateExtensionDigitAndSerial(final TableItem tableItem, final SSCCExtensionDigit extensionDigit, final String serial) {
        final StringBuilder value = new StringBuilder()
            .append(extensionDigit.getValue())
            .append(serial);
        if (value.length() != tableItem.digits()) {
            throw new IllegalArgumentException(String.format("Concatenation between Extension Digit \"%d\" and Serial \"%s\" has %d length and should have %d length", extensionDigit.getValue(), serial, value.length(), tableItem.digits()));
        }
    }

    private void validateCompanyPrefix(final PrefixLength prefixLength) {
        final Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    /* --- Objects --- */
    private record ParsedData(
        TableItem tableItem,
        SSCCTagSize tagSize,
        SSCCFilterValue filterValue,
        SSCCExtensionDigit extensionDigit,
        PrefixLength prefixLength,
        String companyPrefix,
        String serial
    ) {
        // empty
    }

    /* --- Builder --- */
    public interface ChoiceStep {
        ExtensionDigiStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEpcTagURI(final String epcTagURI);

        TagSizeStep withEpcPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface ExtensionDigiStep {
        SerialStep withExtensionDigit(final SSCCExtensionDigit extensionDigit);
    }

    public interface SerialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final SSCCTagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final SSCCFilterValue filterValue);
    }

    public interface BuildStep {
        SSCC build() throws EpcParseException;
    }

    private static class Steps implements ChoiceStep, ExtensionDigiStep, SerialStep, TagSizeStep, FilterValueStep, BuildStep {

        private SSCCExtensionDigit extensionDigit;
        private String companyPrefix;
        private SSCCTagSize tagSize;
        private SSCCFilterValue filterValue;
        private String serial;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public SerialStep withExtensionDigit(final SSCCExtensionDigit extensionDigit) {
            this.extensionDigit = extensionDigit;
            return this;
        }

        @Override
        public ExtensionDigiStep withCompanyPrefix(final String companyPrefix) {
            this.companyPrefix = companyPrefix;
            return this;
        }

        @Override
        public TagSizeStep withSerial(final String serial) {
            this.serial = serial;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final SSCCTagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public BuildStep withFilterValue(final SSCCFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public BuildStep withRFIDTag(final String rfidTag) {
            this.rfidTag = rfidTag;
            return this;
        }

        @Override
        public BuildStep withEpcTagURI(final String epcTagURI) {
            this.epcTagURI = epcTagURI;
            return this;
        }

        @Override
        public TagSizeStep withEpcPureIdentityURI(final String epcPureIdentityURI) {
            this.epcPureIdentityURI = epcPureIdentityURI;
            return this;
        }

        @Override
        public SSCC build() throws EpcParseException {
            final SSCCParser parser = new SSCCParser(this);
            return parser.sscc;
        }

    }

}
