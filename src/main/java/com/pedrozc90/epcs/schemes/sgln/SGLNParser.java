package com.pedrozc90.epcs.schemes.sgln;

import com.pedrozc90.epcs.exception.EpcParseException;
import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.sgln.enums.SGLNFilterValue;
import com.pedrozc90.epcs.schemes.sgln.enums.SGLNHeader;
import com.pedrozc90.epcs.schemes.sgln.enums.SGLNTagSize;
import com.pedrozc90.epcs.schemes.sgln.objects.SGLN;
import com.pedrozc90.epcs.schemes.sgln.partitionTable.SGLNPartitionTable;
import com.pedrozc90.epcs.utils.Converter;
import lombok.Getter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SGLNParser {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("^(urn:epc:tag:sgln-)(96|195):([0-7])\\.(\\d+)\\.(\\d+)\\.(.+)$");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("^(urn:epc:id:sgln):(\\d+)\\.(\\d+)\\.(.+)$");

    private static final SGLNPartitionTable partitionTable = new SGLNPartitionTable();

    private final SGLN sgln;

    private SGLNParser(final Steps steps) throws EpcParseException {
        final ParsedData data = parse(steps);
        sgln = toSGLN(data);
    }

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
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
        final String inputBin = Converter.hexToBin(rfidTag);

        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        final SGLNTagSize tagSize = SGLNTagSize.of(SGLNHeader.of(headerBin).getTagSize());
        final TableItem tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

        final String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        final SGLNFilterValue filterValue = SGLNFilterValue.of(Integer.parseInt(filterDec));

        final String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        final String companyPrefixDec = Converter.binToDec(companyPrefixBin);
        final String companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l());

        final String locationReferenceBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
        final String locationReferenceDec = Converter.binToDec(locationReferenceBin);
        final String locationReference = Converter.strZero(locationReferenceDec, tableItem.digits());

        String extensionBin = inputBin.substring(14 + tableItem.m() + tableItem.n());

        String extension = null;
        if (tagSize.getSerialBitCount() == 140) {
            extensionBin = Converter.convertBinToBit(extensionBin, 7, 8);
            extension = Converter.binToString(extensionBin);
        } else if (tagSize.getSerialBitCount() == 41) {
            extension = Converter.binToDec(extensionBin);
        }

        final PrefixLength prefixLength = PrefixLength.of(tableItem.l());

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, locationReference, extension);
    }

    private ParsedData decodeEpcTagURI(final String epcTagURI) {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Tag URI is invalid");
        }

        final SGLNTagSize tagSize = SGLNTagSize.of(Integer.parseInt(matcher.group(2)));
        final SGLNFilterValue filterValue = SGLNFilterValue.of(Integer.parseInt(matcher.group(3)));
        final String companyPrefix = matcher.group(4);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String locationReference = matcher.group(5);
        final String extension = matcher.group(6);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateLocationReference(tableItem, locationReference);

        validateExtension(tagSize, extension);

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, locationReference, extension);
    }

    private ParsedData decodeEpcPureIdentityURI(final String epcPureIdentityURI, final SGLNTagSize tagSize, final SGLNFilterValue filterValue) {
        if (tagSize == null) throw new IllegalArgumentException("Tag size must not be null");
        if (filterValue == null) throw new IllegalArgumentException("Filter value must not be null");

        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Pure Identity is invalid");
        }

        final String companyPrefix = matcher.group(2);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String locationReference = matcher.group(3);
        final String extension = matcher.group(4);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateLocationReference(tableItem, locationReference);

        validateExtension(tagSize, extension);

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, locationReference, extension);
    }

    private ParsedData encode(final Steps steps) {
        final PrefixLength prefixLength = PrefixLength.of(steps.companyPrefix.length());
        validateCompanyPrefix(prefixLength);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateLocationReference(tableItem, steps.locationReference);

        validateExtension(steps.tagSize, steps.extension);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, prefixLength, steps.companyPrefix, steps.locationReference, steps.extension);
    }

    private SGLN toSGLN(final ParsedData data) {
        final BinaryResult result = toBinary(data);

        final String outputBin = result.binary;
        final String outputHex = Converter.binToHex(outputBin);

        final int remainder = result.remainder;

        final Integer checkDigit = getCheckDigit(data.companyPrefix, data.locationReference);

        final SGLN sgln = new SGLN();
        // sgln.setEpcScheme("sgln");
        sgln.setApplicationIdentifier("AI 254");
        sgln.setTagSize(Integer.toString(data.tagSize.getValue()));
        sgln.setFilterValue(Integer.toString(data.filterValue.getValue()));
        sgln.setPartitionValue(Integer.toString(data.tableItem.partitionValue()));
        sgln.setPrefixLength(Integer.toString(data.prefixLength.getValue()));
        sgln.setCompanyPrefix(data.companyPrefix);
        sgln.setLocationReference(data.locationReference);
        sgln.setExtension(data.extension);
        sgln.setCheckDigit(Integer.toString(checkDigit));
        sgln.setEpcPureIdentityURI("urn:epc:id:sgln:%s.%s.%s".formatted(data.companyPrefix, data.locationReference, data.extension));
        sgln.setEpcTagURI("urn:epc:tag:sgln-%s:%s.%s.%s.%s".formatted(data.tagSize.getValue(), data.filterValue.getValue(), data.companyPrefix, data.locationReference, data.extension));
        sgln.setEpcRawURI("urn:epc:raw:%s.x%s".formatted(data.tagSize.getValue() + remainder, outputHex));
        sgln.setBinary(outputBin);
        sgln.setRfidTag(outputHex);
        return sgln;
    }

    private BinaryResult toBinary(final ParsedData data) {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        final int remainder = Converter.remainder(data.tagSize.getValue());

        bin.append(Converter.decToBin(data.tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(data.filterValue.getValue(), 3));
        bin.append(Converter.decToBin(data.tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(data.companyPrefix), data.tableItem.m()));
        bin.append(Converter.decToBin(Integer.parseInt(data.locationReference), data.tableItem.n()));

        if (data.tagSize.getValue() == 195) {
            bin.append(Converter.fill(Converter.StringToBinary(data.extension, 7), data.tagSize.getSerialBitCount() + remainder));
        } else if (data.tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(data.extension, data.tagSize.getSerialBitCount() + remainder));
        }

        return new BinaryResult(bin.toString(), remainder);
    }

    /* --- Validations --- */
    private Integer getCheckDigit(final String companyPrefix, final String locationReference) {
        final String value = companyPrefix + locationReference;

        return (10 - ((3
            * (Character.getNumericValue(value.charAt(1)) + Character.getNumericValue(value.charAt(3))
            + Character.getNumericValue(value.charAt(5))
            + Character.getNumericValue(value.charAt(7)) + Character.getNumericValue(value.charAt(9))
            + Character.getNumericValue(value.charAt(11)))
            + (Character.getNumericValue(value.charAt(0)) + Character.getNumericValue(value.charAt(2))
            + Character.getNumericValue(value.charAt(4)) + Character.getNumericValue(value.charAt(6))
            + Character.getNumericValue(value.charAt(8)) + Character.getNumericValue(value.charAt(10))))
            % 10)) % 10;
    }

    private void validateCompanyPrefix(final PrefixLength prefixLength) {
        final Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    private void validateLocationReference(final TableItem tableItem, final String locationReference) {
        if (locationReference.length() != tableItem.digits()) {
            throw new IllegalArgumentException(String.format("Location Reference \"%s\" has %d length and should have %d length", locationReference, locationReference.length(), tableItem.digits()));
        }
    }

    private void validateExtension(final SGLNTagSize tagSize, final String extension) {
        switch (tagSize) {
            case BITS_96 -> {
                if (!extension.matches("\\d+")) {
                    throw new IllegalArgumentException("SGLN-96 extension must be numeric, got: '%s'".formatted(extension));
                }
                if (Long.parseLong(extension) > tagSize.getSerialMaxValue()) {
                    throw new IllegalArgumentException("SGLN-96 Extension value is out of range. Should be less than or equal %d".formatted(tagSize.getSerialMaxValue()));
                }
            }
            case BITS_195 -> {
                if (extension.length() > tagSize.getSerialMaxLength()) {
                    throw new IllegalArgumentException("SGLN-195 Extension value is out of range. Should be up to %d alphanumeric characters".formatted(tagSize.getSerialMaxLength()));
                }
            }
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(tagSize));
        }
    }

    /* --- Objects --- */
    private record ParsedData(
        TableItem tableItem,
        SGLNTagSize tagSize,
        SGLNFilterValue filterValue,
        PrefixLength prefixLength,
        String companyPrefix,
        String locationReference,
        String extension
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
        LocationReferenceStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEpcTagURI(final String epcTagURI);

        TagSizeStep withEpcPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final SGLNTagSize tagSize);
    }

    public interface LocationReferenceStep {
        ExtensionStep withLocationReference(final String locationReference);
    }

    public interface ExtensionStep {
        TagSizeStep withExtension(final String extension);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final SGLNFilterValue filterValue);
    }

    public interface BuildStep {
        SGLN build() throws EpcParseException;
    }

    @Getter
    private static class Steps implements ChoiceStep, LocationReferenceStep, ExtensionStep, TagSizeStep, FilterValueStep, BuildStep {

        private String companyPrefix;
        private SGLNTagSize tagSize;
        private SGLNFilterValue filterValue;
        private String locationReference;
        private String extension;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public LocationReferenceStep withCompanyPrefix(final String companyPrefix) {
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
        public TagSizeStep withExtension(final String extension) {
            this.extension = extension;
            return this;
        }

        @Override
        public BuildStep withFilterValue(final SGLNFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public ExtensionStep withLocationReference(final String locationReference) {
            this.locationReference = locationReference;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final SGLNTagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public SGLN build() throws EpcParseException {
            final SGLNParser parser = new SGLNParser(this);
            return parser.sgln;
        }

    }

}
