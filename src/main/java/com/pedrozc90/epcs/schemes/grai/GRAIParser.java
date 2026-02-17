package com.pedrozc90.epcs.schemes.grai;

import com.pedrozc90.epcs.exception.EpcParseException;
import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.grai.enums.GRAIFilterValue;
import com.pedrozc90.epcs.schemes.grai.enums.GRAIHeader;
import com.pedrozc90.epcs.schemes.grai.enums.GRAITagSize;
import com.pedrozc90.epcs.schemes.grai.objects.GRAI;
import com.pedrozc90.epcs.schemes.grai.partitionTable.GRAIPartitionTable;
import com.pedrozc90.epcs.utils.BinaryUtils;
import com.pedrozc90.epcs.utils.Converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GRAIParser {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("^(urn:epc:tag:grai-)(96|170):([0-7])\\.(\\d+)\\.(\\d+)\\.(.+)$");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("^(urn:epc:id:grai):(\\d+)\\.(\\d+)\\.(.+)$");

    private static final GRAIPartitionTable partitionTable = new GRAIPartitionTable();

    private final GRAI grai;

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private GRAIParser(final Steps steps) throws EpcParseException {
        final ParsedData data = parse(steps);
        grai = toGRAI(data);
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

    private ParsedData decodeRFIDTag(final String rfidTag) {
        final String inputBin = BinaryUtils.toBinary(rfidTag);

        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        final GRAIHeader header = GRAIHeader.of(headerBin);
        final GRAITagSize tagSize = GRAITagSize.of(header.getTagSize());

        final TableItem tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));
        final PrefixLength prefixLength = PrefixLength.of(tableItem.l());

        final String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        final GRAIFilterValue filterValue = GRAIFilterValue.of(Integer.parseInt(filterDec));

        final String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        final String companyPrefix = BinaryUtils.decodeInteger(companyPrefixBin, tableItem.l());

        final String assetTypeBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
        final String assetType = BinaryUtils.decodeInteger(assetTypeBin, tableItem.digits());

        final String serialBin = inputBin.substring(14 + tableItem.m() + tableItem.n());

        final String serial = switch (tagSize.getSerialBitCount()) {
            // grai-96
            case 38 -> BinaryUtils.decodeInteger(serialBin);
            // grai-198
            case 112 -> BinaryUtils.decodeString(serialBin, 7);
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(tagSize));
        };

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, assetType, serial);
    }

    private ParsedData decodeEpcTagURI(final String epcTagURI) {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Tag URI '%s' is invalid".formatted(epcTagURI));
        }

        final GRAITagSize tagSize = GRAITagSize.of(Integer.parseInt(matcher.group(2)));
        final GRAIFilterValue filterValue = GRAIFilterValue.of(Integer.parseInt(matcher.group(3)));
        final String companyPrefix = matcher.group(4);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String assetType = matcher.group(5);
        final String serial = matcher.group(6);

        validateSerial(tagSize, serial);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, assetType, serial);
    }

    private ParsedData decodeEpcPureIdentityURI(final String epcPureIdentityURI, final GRAITagSize tagSize, final GRAIFilterValue filterValue) {
        if (tagSize == null) throw new IllegalArgumentException("tag size must not be null");
        if (filterValue == null) throw new IllegalArgumentException("filter value must not be null");

        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Pure Identity is invalid");
        }

        final String companyPrefix = matcher.group(2);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String assetType = matcher.group(3);
        final String serial = matcher.group(4);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, assetType, serial);
    }

    private ParsedData encode(final Steps steps) {
        final PrefixLength prefixLength = PrefixLength.of(steps.companyPrefix.length());
        validateCompanyPrefix(prefixLength);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateAssetType(tableItem, steps.assetType);
        validateSerial(steps.tagSize, steps.serial);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, prefixLength, steps.companyPrefix, steps.assetType, steps.serial);
    }

    private GRAI toGRAI(final ParsedData data) {
        final BinaryResult result = toBinary(data);

        final String outputBin = result.binary;
        final String outputHex = BinaryUtils.toHex(outputBin);

        final int remainder = result.remainder;

        final Integer checkDigit = getCheckDigit(data.companyPrefix, data.assetType);

        return new GRAI(
            // "grai",
            // "AI 8003",
            Integer.toString(data.tagSize.getValue()),
            Integer.toString(data.filterValue.getValue()),
            Integer.toString(data.tableItem.partitionValue()),
            Integer.toString(data.prefixLength.getValue()),
            data.companyPrefix,
            data.assetType,
            data.serial,
            Integer.toString(checkDigit),
            "urn:epc:id:grai:%s.%s.%s".formatted(data.companyPrefix, data.assetType, data.serial),
            "urn:epc:tag:grai-%s:%s.%s.%s.%s".formatted(data.tagSize.getValue(), data.filterValue.getValue(), data.companyPrefix, data.assetType, data.serial),
            "urn:epc:raw:%s.x%s".formatted(data.tagSize.getValue() + remainder, outputHex),
            outputBin,
            outputHex
        );
    }

    private BinaryResult toBinary(final ParsedData data) {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        final int remainder = Converter.remainder(data.tagSize.getValue());

        bin.append(BinaryUtils.encodeInteger(data.tagSize.getHeader(), 8));
        bin.append(BinaryUtils.encodeInteger(data.filterValue.getValue(), 3));
        bin.append(BinaryUtils.encodeInteger(data.tableItem.partitionValue(), 3));
        bin.append(BinaryUtils.encodeInteger(data.companyPrefix, data.tableItem.m()));
        bin.append(BinaryUtils.encodeInteger(data.assetType, data.tableItem.n()));

        // grai-96
        if (data.tagSize.getValue() == 96) {
            bin.append(BinaryUtils.encodeInteger(data.serial, data.tagSize.getSerialBitCount() + remainder));
        }
        // grai-170
        else if (data.tagSize.getValue() == 170) {
            bin.append(BinaryUtils.encodeString(data.serial, data.tagSize.getSerialBitCount() + remainder, 7));
        }

        return new BinaryResult(bin.toString(), remainder);
    }

    private Integer getCheckDigit(final String companyPrefix, final String assetType) {
        final String value = companyPrefix + assetType;

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

    private void validateAssetType(final TableItem tableItem, final String assetType) {
        if (assetType.length() != tableItem.digits()) {
            throw new IllegalArgumentException("Asset Type \"%s\" has %d length and should have %d length".formatted(assetType, assetType.length(), tableItem.digits()));
        }
    }

    private void validateSerial(final GRAITagSize tagSize, final String serial) {
        switch (tagSize) {
            case BITS_96 -> {
                if (!serial.matches("\\d+")) {
                    throw new IllegalArgumentException("GRAI-96 Serial must be numeric, got: '%s'".formatted(serial));
                }
                if (serial.startsWith("0")) {
                    throw new IllegalArgumentException("GRAI-96 Serial with leading zeros is not allowed");
                }
                if (Long.parseLong(serial) > tagSize.getSerialMaxValue()) {
                    throw new IllegalArgumentException("GRAI-96 Serial value is out of range. Should be less than or equal %d".formatted(tagSize.getSerialMaxValue()));
                }
            }
            case BITS_170 -> {
                // GS1 AI 82 character set: alphanumeric + !"#%&'()*+,-./:;<=>?_
                if (!serial.matches("[\\w!\"#%&'()*+,\\-./:;<=>?]+")) {
                    throw new IllegalArgumentException("GRAI-170 serial contains invalid characters: '%s'".formatted(serial));
                }
                if (serial.length() > tagSize.getSerialMaxLength()) {
                    throw new IllegalArgumentException("GRAI-170 serial value is out of range. Should be up to %d alphanumeric characters".formatted(tagSize.getSerialMaxLength()));
                }
            }
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(tagSize));
        }
    }

    /* --- Objects ---*/
    private record ParsedData(
        TableItem tableItem,
        GRAITagSize tagSize,
        GRAIFilterValue filterValue,
        PrefixLength prefixLength,
        String companyPrefix,
        String assetType,
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

    /* --- Builder ---*/
    public interface ChoiceStep {
        AssetTypeStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEpcTagURI(final String epcTagURI);

        TagSizeStep withEpcPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface AssetTypeStep {
        SerialStep withAssetType(final String assetType);
    }

    public interface SerialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final GRAITagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final GRAIFilterValue filterValue);
    }

    public interface BuildStep {
        GRAI build() throws EpcParseException;
    }

    private static class Steps implements ChoiceStep, AssetTypeStep, SerialStep, TagSizeStep, FilterValueStep, BuildStep {

        private String companyPrefix;
        private GRAITagSize tagSize;
        private GRAIFilterValue filterValue;
        private String assetType;
        private String serial;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public BuildStep withFilterValue(final GRAIFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final GRAITagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public TagSizeStep withSerial(final String serial) {
            this.serial = serial;
            return this;
        }

        @Override
        public SerialStep withAssetType(final String assetType) {
            this.assetType = assetType;
            return this;
        }

        @Override
        public AssetTypeStep withCompanyPrefix(final String companyPrefix) {
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
        public GRAI build() throws EpcParseException {
            final GRAIParser parser = new GRAIParser(this);
            return parser.grai;
        }

    }

}
