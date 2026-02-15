package com.pedrozc90.epcs.schemes.giai;

import com.pedrozc90.epcs.exception.EpcParseException;
import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.giai.enums.GIAIFilterValue;
import com.pedrozc90.epcs.schemes.giai.enums.GIAIHeader;
import com.pedrozc90.epcs.schemes.giai.enums.GIAITagSize;
import com.pedrozc90.epcs.schemes.giai.objects.GIAI;
import com.pedrozc90.epcs.schemes.giai.partitionTable.GIAIPartitionTable;
import com.pedrozc90.epcs.utils.BinaryUtils;
import com.pedrozc90.epcs.utils.Converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GIAIParser {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("^(urn:epc:tag:giai-)(96|202):([0-7])\\.(\\d+)\\.(.+)$");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("^(urn:epc:id:giai):(\\d+)\\.(.+)$");

    private final GIAI giai;

    public static ChoiceStep Builder() {
        return new Steps();
    }

    private GIAIParser(final Steps steps) throws EpcParseException {
        final ParsedData data = parse(steps);
        giai = toGIAI(data);
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

        final GIAIHeader header = GIAIHeader.of(headerBin);
        final GIAITagSize tagSize = GIAITagSize.of(header.getTagSize());

        final GIAIPartitionTable partitionTable = GIAIPartitionTable.getInstance(tagSize);
        final TableItem tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));
        final PrefixLength prefixLength = PrefixLength.of(tableItem.l());

        final String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        final GIAIFilterValue filterValue = GIAIFilterValue.of(Integer.parseInt(filterDec));

        final String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        final String companyPrefix = BinaryUtils.decodeInteger(companyPrefixBin, tableItem.l());

        final String individualAssetReferenceBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());

        final String individualAssetReference = switch (tagSize.getSerialBitCount()) {
            // giai-96
            case 38 -> BinaryUtils.decodeInteger(individualAssetReferenceBin);
            // giai-202
            case 112 -> BinaryUtils.decodeString(individualAssetReferenceBin, 7);
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(tagSize));
        };

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, individualAssetReference);
    }

    private ParsedData decodeEpcTagURI(final String epcTagURI) {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Tag URI is invalid");
        }

        final GIAITagSize tagSize = GIAITagSize.of(Integer.parseInt(matcher.group(2)));
        final GIAIFilterValue filterValue = GIAIFilterValue.of(Integer.parseInt(matcher.group(3)));
        final String companyPrefix = matcher.group(4);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String individualAssetReference = matcher.group(5);

        final GIAIPartitionTable partitionTable = GIAIPartitionTable.getInstance(tagSize);
        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateIndividualAssetReference(tableItem, tagSize, individualAssetReference);

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, individualAssetReference);
    }

    private ParsedData decodeEpcPureIdentityURI(final String epcPureIdentityURI, final GIAITagSize tagSize, final GIAIFilterValue filterValue) {
        if (tagSize == null) throw new IllegalArgumentException("tag size must not be null");
        if (filterValue == null) throw new IllegalArgumentException("filter value must not be null");

        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Pure Identity is invalid");
        }

        final String companyPrefix = matcher.group(2);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String individualAssetReference = matcher.group(3);

        final GIAIPartitionTable partitionTable = GIAIPartitionTable.getInstance(tagSize);
        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, individualAssetReference);
    }

    private ParsedData encode(final Steps steps) {
        final GIAIPartitionTable partitionTable = GIAIPartitionTable.getInstance(steps.tagSize);
        final PrefixLength prefixLength = PrefixLength.of(steps.companyPrefix.length());
        validateCompanyPrefix(prefixLength);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateIndividualAssetReference(tableItem, steps.tagSize, steps.individualAssetReference);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, prefixLength, steps.companyPrefix, steps.individualAssetReference);
    }

    private GIAI toGIAI(final ParsedData data) {
        final BinaryResult result = toBinary(data);

        final String outputBin = result.binary;
        final String outputHex = BinaryUtils.toHex(outputBin);

        final int remainder = result.remainder;

        final GIAI giai = new GIAI();
        // giai.setEpcScheme("giai");
        giai.setApplicationIdentifier("AI 8004");
        giai.setTagSize(Integer.toString(data.tagSize.getValue()));
        giai.setFilterValue(Integer.toString(data.filterValue.getValue()));
        giai.setPartitionValue(Integer.toString(data.tableItem.partitionValue()));
        giai.setPrefixLength(Integer.toString(data.prefixLength.getValue()));
        giai.setCompanyPrefix(data.companyPrefix);
        giai.setIndividualAssetReference(data.individualAssetReference);
        giai.setEpcPureIdentityURI("urn:epc:id:giai:%s.%s".formatted(data.companyPrefix, data.individualAssetReference));
        giai.setEpcTagURI("urn:epc:tag:giai-%s:%s.%s.%s".formatted(data.tagSize.getValue(), data.filterValue.getValue(), data.companyPrefix, data.individualAssetReference));
        giai.setEpcRawURI("urn:epc:raw:%s.x%s".formatted(data.tagSize.getValue() + remainder, outputHex));
        giai.setBinary(outputBin);
        giai.setRfidTag(outputHex);
        return giai;
    }

    private BinaryResult toBinary(final ParsedData data) {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        final int remainder = Converter.remainder(data.tagSize.getValue());

        bin.append(BinaryUtils.encodeInteger(data.tagSize.getHeader(), 8));
        bin.append(BinaryUtils.encodeInteger(data.filterValue.getValue(), 3));
        bin.append(BinaryUtils.encodeInteger(data.tableItem.partitionValue(), 3));
        bin.append(BinaryUtils.encodeInteger(data.companyPrefix, data.tableItem.m()));

        if (data.tagSize.getValue() == 96) {
            bin.append(BinaryUtils.encodeInteger(data.individualAssetReference, data.tableItem.n() + remainder));
        } else if (data.tagSize.getValue() == 202) {
            bin.append(BinaryUtils.encodeString(data.individualAssetReference, data.tableItem.n() + remainder, 7));
        }

        return new BinaryResult(bin.toString(), remainder);
    }

    /* --- Validations --- */
    private void validateCompanyPrefix(final PrefixLength prefixLength) {
        final Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    private void validateIndividualAssetReference(final TableItem tableItem, final GIAITagSize tagSize, final String individualAssetReference) {
        if (individualAssetReference.length() > tableItem.digits()) {
            throw new IllegalArgumentException("Individual Asset Reference value is out of range. The length should be %d".formatted(tableItem.digits()));
        }

        switch (tagSize) {
            case BITS_96 -> {
                if (!individualAssetReference.matches("\\d+")) {
                    throw new IllegalArgumentException("GIAI-96 Individual Asset Reference must be numeric, got: '%s'".formatted(individualAssetReference));
                }
                if (individualAssetReference.startsWith("0")) {
                    throw new IllegalArgumentException("GIAI-96 Individual Asset Reference with leading zeros is not allowed");
                }
            }
            case BITS_202 -> {
                // no validation
            }
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(tagSize));
        }
    }

    /* --- Objects --- */
    private record ParsedData(
        TableItem tableItem,
        GIAITagSize tagSize,
        GIAIFilterValue filterValue,
        PrefixLength prefixLength,
        String companyPrefix,
        String individualAssetReference
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
        IndividualAssetReferenceStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEpcTagURI(final String epcTagURI);

        TagSizeStep withEpcPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface IndividualAssetReferenceStep {
        TagSizeStep withIndividualAssetReference(String individualAssetReference);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final GIAITagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(GIAIFilterValue filterValue);
    }

    public interface BuildStep {
        GIAI build() throws EpcParseException;
    }

    private static class Steps implements ChoiceStep, IndividualAssetReferenceStep, TagSizeStep, FilterValueStep, BuildStep {

        private String companyPrefix;
        private GIAITagSize tagSize;
        private GIAIFilterValue filterValue;
        private String individualAssetReference;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public GIAI build() throws EpcParseException {
            final GIAIParser parser = new GIAIParser(this);
            return parser.giai;
        }

        @Override
        public BuildStep withFilterValue(final GIAIFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final GIAITagSize tagSize) {
            this.tagSize = tagSize;
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
        public IndividualAssetReferenceStep withCompanyPrefix(final String companyPrefix) {
            this.companyPrefix = companyPrefix;
            return this;
        }

        @Override
        public TagSizeStep withIndividualAssetReference(final String individualAssetReference) {
            this.individualAssetReference = individualAssetReference;
            return this;
        }

    }

}
