package com.pedrozc90.epcs.schemes.sgtin;

import com.pedrozc90.epcs.exception.EpcParseException;
import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINExtensionDigit;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINFilterValue;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINHeader;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINTagSize;
import com.pedrozc90.epcs.schemes.sgtin.objects.SGTIN;
import com.pedrozc90.epcs.schemes.sgtin.partitionTable.SGTINPartitionTable;
import com.pedrozc90.epcs.utils.Converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SGTINParser {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("(urn:epc:tag:sgtin-)(96|198):([0-7])\\.(\\d+)\\.([0-8])(\\d+)\\.(.+)");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("(urn:epc:id:sgtin):(\\d+)\\.([0-8])(\\d+)\\.(.+)");

    private static final SGTINPartitionTable partitionTable = new SGTINPartitionTable();

    private final SGTIN sgtin;

    public static ChoiceStep Builder() {
        return new Steps();
    }

    private SGTINParser(final Steps steps) throws EpcParseException {
        final ParsedData data = parse(steps);
        sgtin = toSGTIN(data);
    }

    private ParsedData parse(final Steps steps) throws EpcParseException {
        if (steps.rfidTag != null) {
            return decodeRFIDTag(steps.rfidTag);
        } else if (steps.epcTagURI != null) {
            return decodeEpcTagURI(steps.epcTagURI);
        } else if (steps.epcPureIdentityURI != null) {
            return decodeEpcPureIdentityURI(steps.epcPureIdentityURI, steps.tagSize, steps.filterValue);
        }
        return encode(steps);
    }

    /* --- Rfid Tag --- */
    private ParsedData decodeRFIDTag(final String rfidTag) throws EpcParseException {
        final String inputBin = Converter.hexToBin(rfidTag);

        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        final SGTINHeader header = SGTINHeader.of(headerBin);
        final SGTINTagSize tagSize = SGTINTagSize.of(header.getTagSize());

        final TableItem tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

        final String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        final SGTINFilterValue filterValue = SGTINFilterValue.of(Integer.parseInt(filterDec));

        final String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        final String itemReferenceWithExtensionBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());

        final String serialBin = inputBin
            .substring(14 + tableItem.m() + tableItem.n())
            .substring(0, tagSize.getSerialBitCount());

        final String companyPrefixDec = Converter.binToDec(companyPrefixBin);
        final String companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l());
        final PrefixLength prefixLength = PrefixLength.of(tableItem.l());

        final String itemReferenceWithExtensionDec = Converter.strZero(Converter.binToDec(itemReferenceWithExtensionBin), tableItem.digits());

        final String extensionDec = itemReferenceWithExtensionDec.substring(0, 1);
        final SGTINExtensionDigit extensionDigit = SGTINExtensionDigit.of(Integer.parseInt(extensionDec));

        final String itemReference = itemReferenceWithExtensionDec.substring(1);

        final String serial = parseSerial(tagSize, serialBin);

        return new ParsedData(tableItem, tagSize, filterValue, extensionDigit, prefixLength, companyPrefix, itemReference, serial);
    }

    private String parseSerial(final SGTINTagSize tagSize, final String serialBin) throws EpcParseException {
        if (tagSize.getSerialBitCount() == 140) {
            final String tmp = Converter.convertBinToBit(serialBin, 7, 8);
            return Converter.binToString(tmp);
        } else if (tagSize.getSerialBitCount() == 38) {
            return Converter.binToDec(serialBin);
        }
        throw new EpcParseException("Unsupported tag size");
    }

    /* --- EPc Tag URI --- */
    private ParsedData decodeEpcTagURI(final String epcTagURI) throws EpcParseException {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new EpcParseException("Epc Tag URI is invalid");
        }

        final SGTINTagSize tagSize = SGTINTagSize.of(Integer.parseInt(matcher.group(2)));
        final SGTINFilterValue filterValue = SGTINFilterValue.of(Integer.parseInt(matcher.group(3)));
        final String companyPrefix = matcher.group(4);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final SGTINExtensionDigit extensionDigit = SGTINExtensionDigit.of(Integer.parseInt(matcher.group(5)));
        final String itemReference = matcher.group(6);
        final String serial = matcher.group(7);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        return new ParsedData(tableItem, tagSize, filterValue, extensionDigit, prefixLength, companyPrefix, itemReference, serial);
    }

    private ParsedData decodeEpcPureIdentityURI(final String epcPureIdentityURI,
                                                final SGTINTagSize tagSize,
                                                final SGTINFilterValue filterValue) throws EpcParseException {
        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new EpcParseException("Epc Pure Identity is invalid");
        }

        final String companyPrefix = matcher.group(2);
        final PrefixLength prefixLength = PrefixLength.of(matcher.group(2).length());
        final SGTINExtensionDigit extensionDigit = SGTINExtensionDigit.of(Integer.parseInt(matcher.group(3)));
        final String itemReference = matcher.group(4);
        final String serial = matcher.group(5);
        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        return new ParsedData(tableItem, tagSize, filterValue, extensionDigit, prefixLength, companyPrefix, itemReference, serial);
    }

    /* --- Company Prefix --- */
    private ParsedData encode(final Steps steps) throws EpcParseException {
        final PrefixLength prefixLength = PrefixLength.of(steps.companyPrefix.length());
        validateCompanyPrefix(prefixLength);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateExtensionDigitAndItemReference(steps.extensionDigit, steps.itemReference, tableItem);

        validateSerial(steps.tagSize, steps.serial);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, steps.extensionDigit, prefixLength, steps.companyPrefix, steps.itemReference, steps.serial);
    }

    private BinaryResult toBinary(final ParsedData data) {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        final int remainder = Converter.remainder(data.tagSize.getValue());

        bin.append(Converter.decToBin(data.tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(data.filterValue.getValue(), 3));
        bin.append(Converter.decToBin(data.tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(data.companyPrefix, data.tableItem.m()));
        bin.append(Converter.decToBin(Integer.parseInt(data.extensionDigit.getValue() + data.itemReference), data.tableItem.n()));

        if (data.tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(data.serial, data.tagSize.getSerialBitCount() + remainder));
        } else if (data.tagSize.getValue() == 198) {
            final String serialBin = Converter.StringToBinary(data.serial, 7);
            bin.append(Converter.fill(serialBin, data.tagSize.getSerialBitCount() + remainder));
        }

        return new BinaryResult(bin.toString(), remainder);
    }

    private SGTIN toSGTIN(final ParsedData data) {
        final Integer checkDigit = calculateCheckDigit(data.extensionDigit, data.companyPrefix, data.itemReference);

        final BinaryResult result = toBinary(data);
        final String hex = Converter.binToHex(result.binary);

        final SGTIN sgtin = new SGTIN();
        // sgtin.setEpcScheme("sgtin");
        sgtin.setApplicationIdentifier("AI 414 + AI 254");
        sgtin.setTagSize(Integer.toString(data.tagSize.getValue()));
        sgtin.setFilterValue(Integer.toString(data.filterValue.getValue()));
        sgtin.setPartitionValue(Integer.toString(data.tableItem.partitionValue()));
        sgtin.setPrefixLength(Integer.toString(data.prefixLength.getValue()));
        sgtin.setCompanyPrefix(data.companyPrefix);
        sgtin.setItemReference(data.itemReference);
        sgtin.setExtensionDigit(Integer.toString(data.extensionDigit.getValue()));
        sgtin.setSerial(data.serial);
        sgtin.setCheckDigit(Integer.toString(checkDigit));
        sgtin.setEpcPureIdentityURI("urn:epc:id:sgtin:%s.%s%s.%s".formatted(data.companyPrefix, data.extensionDigit.getValue(), data.itemReference, data.serial));
        sgtin.setEpcTagURI("urn:epc:tag:sgtin-%s:%s.%s.%s%s.%s".formatted(data.tagSize.getValue(), data.filterValue.getValue(), data.companyPrefix, data.extensionDigit.getValue(), data.itemReference, data.serial));
        sgtin.setEpcRawURI("urn:epc:raw:%s.x%s".formatted(data.tagSize.getValue() + result.remainder, hex));
        sgtin.setRfidTag(hex);
        sgtin.setBinary(result.binary);

        return sgtin;
    }

    /* --- Validations --- */
    private Integer calculateCheckDigit(final SGTINExtensionDigit extensionDigit, final String companyPrefix, final String itemReference) {
        final String value = extensionDigit.getValue() + companyPrefix + itemReference;

        return (10 - ((3
            * (Character.getNumericValue(value.charAt(0)) + Character.getNumericValue(value.charAt(2))
            + Character.getNumericValue(value.charAt(4))
            + Character.getNumericValue(value.charAt(6)) + Character.getNumericValue(value.charAt(8))
            + Character.getNumericValue(value.charAt(10)) + Character.getNumericValue(value.charAt(12)))
            + (Character.getNumericValue(value.charAt(1)) + Character.getNumericValue(value.charAt(3))
            + Character.getNumericValue(value.charAt(5)) + Character.getNumericValue(value.charAt(7))
            + Character.getNumericValue(value.charAt(9)) + Character.getNumericValue(value.charAt(11))))
            % 10)) % 10;
    }

    private void validateExtensionDigitAndItemReference(final SGTINExtensionDigit extensionDigit,
                                                        final String itemReference,
                                                        final TableItem tableItem) throws EpcParseException {
        final StringBuilder value = new StringBuilder()
            .append(extensionDigit.getValue())
            .append(itemReference);

        if (value.length() != tableItem.digits()) {
            throw new EpcParseException(
                "Concatenation between Extension Digit \"%d\" and Item Reference \"%s\" has %d length and should have %d length",
                extensionDigit.getValue(),
                itemReference,
                value.length(),
                tableItem.digits()
            );
        }
    }

    private void validateCompanyPrefix(final PrefixLength prefixLength) throws EpcParseException {
        if (prefixLength == null) {
            throw new EpcParseException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    private void validateSerial(final SGTINTagSize tagSize, final String serial) throws EpcParseException {
        switch (tagSize) {
            case BITS_96 -> {
                if (!serial.matches("\\d+")) {
                    throw new IllegalArgumentException("SGTIN-96 Serial must be numeric, got: '%s'".formatted(serial));
                }
                if (serial.startsWith("0")) {
                    throw new EpcParseException("SGTIN-96 Serial with leading zeros is not allowed");
                }
                if (Long.parseLong(serial) > tagSize.getSerialMaxValue()) {
                    throw new EpcParseException("SGTIN-96 Serial value is out of range. Should be less than or equal 274,877,906,943");
                }
            }
            case BITS_198 -> {
                if (serial.length() > tagSize.getSerialMaxLength()) {
                    throw new EpcParseException("SGTIN-198 Serial value is out of range. Should be up to 20 alphanumeric characters");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(tagSize));
        }
    }

    /* --- Object --- */
    private record ParsedData(
        TableItem tableItem,
        SGTINTagSize tagSize,
        SGTINFilterValue filterValue,
        SGTINExtensionDigit extensionDigit,
        PrefixLength prefixLength,
        String companyPrefix,
        String itemReference,
        String serial
    ) {
        // ignore
    }

    private record BinaryResult(String binary, int remainder) {
        // ignore
    }

    /* --- Builder --- */
    public interface ChoiceStep {
        ExtensionDigitStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEpcTagURI(final String epcTagURI);

        TagSizeStep withEpcPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface ExtensionDigitStep {
        ItemReferenceStep withExtensionDigit(final SGTINExtensionDigit extensionDigit);
    }

    public interface ItemReferenceStep {
        SerialStep withItemReference(final String itemReference);
    }

    public interface SerialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final SGTINTagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final SGTINFilterValue filterValue);
    }

    public interface BuildStep {
        SGTIN build() throws EpcParseException;
    }

    private static class Steps implements ChoiceStep, ExtensionDigitStep, ItemReferenceStep, SerialStep, TagSizeStep, FilterValueStep, BuildStep {

        private SGTINTagSize tagSize;
        private SGTINFilterValue filterValue;
        private SGTINExtensionDigit extensionDigit;
        private String companyPrefix;
        private String itemReference;
        private String serial;

        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public ExtensionDigitStep withCompanyPrefix(final String companyPrefix) {
            this.companyPrefix = companyPrefix;
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
        public ItemReferenceStep withExtensionDigit(final SGTINExtensionDigit extensionDigit) {
            this.extensionDigit = extensionDigit;
            return this;
        }

        @Override
        public BuildStep withFilterValue(final SGTINFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public SerialStep withItemReference(final String itemReference) {
            this.itemReference = itemReference;
            return this;
        }

        @Override
        public TagSizeStep withSerial(final String serial) {
            this.serial = serial;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final SGTINTagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public SGTIN build() throws EpcParseException {
            final SGTINParser parser = new SGTINParser(this);
            return parser.sgtin;
        }

    }

}
