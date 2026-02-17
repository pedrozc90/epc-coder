package com.pedrozc90.epcs.schemes.gdti;

import com.pedrozc90.epcs.exception.EpcParseException;
import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.gdti.enums.GDTIFilterValue;
import com.pedrozc90.epcs.schemes.gdti.enums.GDTIHeader;
import com.pedrozc90.epcs.schemes.gdti.enums.GDTITagSize;
import com.pedrozc90.epcs.schemes.gdti.objects.GDTI;
import com.pedrozc90.epcs.schemes.gdti.partitionTable.GDTIPartitionTable;
import com.pedrozc90.epcs.utils.BinaryUtils;
import com.pedrozc90.epcs.utils.Converter;
import com.pedrozc90.epcs.utils.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GDTIParser {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("(urn:epc:tag:gdti-)(96|174):([0-7])\\.(\\d+)\\.(\\d+)\\.(\\w+)");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("(urn:epc:id:gdti):(\\d+)\\.(\\d+)\\.(\\w+)");

    private static final GDTIPartitionTable partitionTable = new GDTIPartitionTable();

    private final GDTI gdti;

    public static ChoiceStep Builder() {
        return new Steps();
    }

    private GDTIParser(final Steps steps) throws EpcParseException {
        final ParsedData data = parse(steps);
        gdti = toGDTI(data);
    }

    private ParsedData parse(final Steps steps) throws EpcParseException {
        if (steps.rfidTag != null) {
            return parseRFIDTag(steps.rfidTag);
        } else if (steps.epcTagURI != null) {
            return parseEpcTagURI(steps.epcTagURI);
        } else if (steps.epcPureIdentityURI != null) {
            return parseEpcPureIdentityURI(steps.epcPureIdentityURI, steps.tagSize, steps.filterValue);
        }
        return encode(steps);
    }

    private static ParsedData parseRFIDTag(final String rfidTag) {
        final String inputBin = BinaryUtils.toBinary(rfidTag);

        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        final GDTIHeader header = GDTIHeader.of(headerBin);
        final GDTITagSize tagSize = GDTITagSize.of(header.getTagSize());

        final TableItem tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));
        final PrefixLength prefixLength = PrefixLength.of(tableItem.l());

        final String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        final GDTIFilterValue filterValue = GDTIFilterValue.of(Integer.parseInt(filterDec));

        final String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        final String companyPrefixDec = Converter.binToDec(companyPrefixBin);
        final String companyPrefix = StringUtils.leftPad(companyPrefixDec, tableItem.l(), '0');

        final String docTypeBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
        final String docTypeDec = Converter.binToDec(docTypeBin);
        final String docType = StringUtils.leftPad(docTypeDec, tableItem.digits(), '0');

        String serialBin = inputBin.substring(14 + tableItem.m() + tableItem.n());

        String serial = null;
        if (tagSize.getSerialBitCount() == 119) {
            serialBin = Converter.convertBinToBit(serialBin, 7, 8);
            serial = Converter.binToString(serialBin);
        } else if (tagSize.getSerialBitCount() == 41) {
            serial = Converter.binToDec(serialBin);
        }

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, docType, serial);
    }

    private static ParsedData parseEpcTagURI(final String epcTagURI) {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Tag URI is invalid");
        }

        final GDTITagSize tagSize = GDTITagSize.of(Integer.parseInt(matcher.group(2)));
        final GDTIFilterValue filterValue = GDTIFilterValue.of(Integer.parseInt(matcher.group(3)));
        final String companyPrefix = matcher.group(4);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String docType = matcher.group(5);
        final String serial = matcher.group(6);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, docType, serial);
    }

    private ParsedData parseEpcPureIdentityURI(final String epcPureIdentityURI, final GDTITagSize tagSize, final GDTIFilterValue filterValue) {
        if (tagSize == null) throw new IllegalArgumentException("tag size must not be null");
        if (filterValue == null) throw new IllegalArgumentException("filter value must not be null");

        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Pure Identity is invalid");
        }

        final String companyPrefix = matcher.group(2);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String docType = matcher.group(3);
        final String serial = matcher.group(4);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, docType, serial);
    }

    private ParsedData encode(final Steps steps) {
        final PrefixLength prefixLength = PrefixLength.of(steps.companyPrefix.length());
        validateCompanyPrefix(prefixLength);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateDocType(tableItem, steps.docType);

        validateSerial(steps.tagSize, steps.serial);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, prefixLength, steps.companyPrefix, steps.docType, steps.serial);
    }

    private GDTI toGDTI(final ParsedData data) {
        final BinaryResult result = toBinary(data);

        final String outputBin = result.binary;
        final String outputHex = BinaryUtils.toHex(outputBin);

        final int remainder = result.remainder;

        final Integer checkDigit = getCheckDigit(data.companyPrefix, data.docType);

        return new GDTI(
            // "gdti",
            // "AI 253",
            Integer.toString(data.tagSize.getValue()),
            Integer.toString(data.filterValue.getValue()),
            Integer.toString(data.tableItem.partitionValue()),
            Integer.toString(data.prefixLength.getValue()),
            data.companyPrefix,
            data.docType,
            data.serial,
            Integer.toString(checkDigit),
            "urn:epc:id:gdti:%s.%s.%s".formatted(data.companyPrefix, data.docType, data.serial),
            "urn:epc:tag:gdti-%s:%s.%s.%s.%s".formatted(data.tagSize.getValue(), data.filterValue.getValue(), data.companyPrefix, data.docType, data.serial),
            "urn:epc:raw:%s.x%s".formatted(data.tagSize.getValue() + remainder, outputHex),
            outputBin,
            outputHex
        );
    }

    private BinaryResult toBinary(final ParsedData data) {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        final int remainder = Converter.remainder(data.tagSize.getValue());

        bin.append(Converter.decToBin(data.tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(data.filterValue.getValue(), 3));
        bin.append(Converter.decToBin(data.tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(data.companyPrefix), data.tableItem.m()));
        bin.append(Converter.decToBin(Integer.parseInt(data.docType), data.tableItem.n()));

        if (data.tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(data.serial, data.tagSize.getSerialBitCount() + remainder));
        } else if (data.tagSize.getValue() == 174) {
            bin.append(Converter.fill(Converter.StringToBinary(data.serial, 7), data.tagSize.getSerialBitCount() + remainder));
        }

        return new BinaryResult(bin.toString(), remainder);
    }

    /* --- Validations --- */
    private Integer getCheckDigit(final String companyPrefix, final String docType) {
        final String value = companyPrefix + docType;

        final Integer d13 = (10 - ((3
            * (Character.getNumericValue(value.charAt(1)) + Character.getNumericValue(value.charAt(3))
            + Character.getNumericValue(value.charAt(5))
            + Character.getNumericValue(value.charAt(7)) + Character.getNumericValue(value.charAt(9))
            + Character.getNumericValue(value.charAt(11)))
            + (Character.getNumericValue(value.charAt(0)) + Character.getNumericValue(value.charAt(2))
            + Character.getNumericValue(value.charAt(4)) + Character.getNumericValue(value.charAt(6))
            + Character.getNumericValue(value.charAt(8)) + Character.getNumericValue(value.charAt(10))))
            % 10)) % 10;

        return d13;
    }

    private void validateCompanyPrefix(final PrefixLength prefixLength) {
        final Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    private void validateDocType(final TableItem tableItem, final String docType) {
        if (docType.length() != tableItem.digits()) {
            throw new IllegalArgumentException("Asset Type \"%s\" has %d length and should have %d length".formatted(
                docType,
                docType.length(),
                tableItem.digits()
            ));
        }
    }

    private void validateSerial(final GDTITagSize tagSize, final String serial) {
        switch (tagSize) {
            case BITS_96 -> {
                if (!serial.matches("\\d+")) {
                    throw new IllegalArgumentException("GDTI-96 Serial must be numeric, got: '%s'".formatted(serial));
                }
                if (serial.startsWith("0")) {
                    throw new IllegalArgumentException("GDTI-96 Serial with leading zeros is not allowed");
                }
                if (Long.parseLong(serial) > tagSize.getSerialMaxValue()) {
                    throw new IllegalArgumentException("GDTI-96 Serial value is out of range. Should be less than or equal %d".formatted(tagSize.getSerialMaxValue()));
                }
            }
            case BITS_174 -> {
                if (serial.length() > tagSize.getSerialMaxLength()) {
                    throw new IllegalArgumentException("GDTI-174 Serial value is out of range. Should be up to %d alphanumeric characters".formatted(tagSize.getSerialMaxLength()));
                }
            }
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(tagSize));
        }
    }

    /* --- Object --- */
    private record ParsedData(
        TableItem tableItem,
        GDTITagSize tagSize,
        GDTIFilterValue filterValue,
        PrefixLength prefixLength,
        String companyPrefix,
        String docType,
        String serial
    ) {
        // empty
    }

    private record BinaryResult(
        String binary,
        int remainder
    ) {
        // empty
    }

    /* --- Builder --- */
    public interface ChoiceStep {
        DocTypeStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEpcTagURI(final String epcTagURI);

        TagSizeStep withEpcPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface DocTypeStep {
        SerialStep withDocType(final String docType);
    }

    public interface SerialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final GDTITagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final GDTIFilterValue filterValue);
    }

    public interface BuildStep {
        GDTI build() throws EpcParseException;
    }

    private static class Steps implements ChoiceStep, DocTypeStep, SerialStep, TagSizeStep, FilterValueStep, BuildStep {

        private GDTITagSize tagSize;
        private GDTIFilterValue filterValue;
        private String companyPrefix;
        private String docType;
        private String serial;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public BuildStep withFilterValue(final GDTIFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final GDTITagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public TagSizeStep withSerial(final String serial) {
            this.serial = serial;
            return this;
        }

        @Override
        public SerialStep withDocType(final String docType) {
            this.docType = docType;
            return this;
        }

        @Override
        public DocTypeStep withCompanyPrefix(final String companyPrefix) {
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
        public GDTI build() throws EpcParseException {
            final GDTIParser parser = new GDTIParser(this);
            return parser.gdti;
        }

    }

}
