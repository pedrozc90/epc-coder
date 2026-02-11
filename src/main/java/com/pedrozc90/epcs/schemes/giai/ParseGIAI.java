package com.pedrozc90.epcs.schemes.giai;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.giai.enums.GIAIFilterValue;
import com.pedrozc90.epcs.schemes.giai.enums.GIAIHeader;
import com.pedrozc90.epcs.schemes.giai.enums.GIAITagSize;
import com.pedrozc90.epcs.schemes.giai.objects.GIAI;
import com.pedrozc90.epcs.schemes.giai.partitionTable.GIAIPartitionTable;
import com.pedrozc90.epcs.utils.Converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseGIAI {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("(urn:epc:tag:giai-)(96|202):([0-7])\\.(\\d+)\\.(\\w+)");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("(urn:epc:id:giai):(\\d+)\\.(\\w+)");

    private GIAI giai = new GIAI();
    private String companyPrefix;
    private PrefixLength prefixLength;
    private GIAITagSize tagSize;
    private GIAIFilterValue filterValue;
    private String individualAssetReference;
    private String rfidTag;
    private String epcTagURI;
    private String epcPureIdentityURI;
    private TableItem tableItem;
    private int remainder;

    public static ChoiceStep Builder() {
        return new Steps();
    }

    private ParseGIAI(final Steps steps) {
        this.companyPrefix = steps.companyPrefix;
        this.tagSize = steps.tagSize;
        this.filterValue = steps.filterValue;
        this.individualAssetReference = steps.individualAssetReference;
        this.rfidTag = steps.rfidTag;
        this.epcTagURI = steps.epcTagURI;
        this.epcPureIdentityURI = steps.epcPureIdentityURI;
        parse();
    }

    private void parse() {
        Optional<String> optionalCompanyPrefix = Optional.ofNullable(companyPrefix);
        Optional<String> optionalRfidTag = Optional.ofNullable(rfidTag);
        Optional<String> optionalEpcTagURI = Optional.ofNullable(epcTagURI);
        Optional<String> optionalEpcPureIdentityURI = Optional.ofNullable(epcPureIdentityURI);

        if (optionalRfidTag.isPresent()) {
            final String inputBin = Converter.hexToBin(rfidTag);
            final String headerBin = inputBin.substring(0, 8);
            final String filterBin = inputBin.substring(8, 11);
            final String partitionBin = inputBin.substring(11, 14);

            tagSize = GIAITagSize.of(GIAIHeader.of(headerBin).getTagSize());
            final GIAIPartitionTable partitionTable = GIAIPartitionTable.getInstance(tagSize);
            tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

            String filterDec = Long.toString(Long.parseLong(filterBin, 2));
            String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
            String individualAssetReferenceBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
            String companyPrefixDec = Converter.binToDec(companyPrefixBin);

            if (tagSize.getSerialBitCount() == 112) {
                individualAssetReferenceBin = Converter.convertBinToBit(individualAssetReferenceBin, 7, 8);
                individualAssetReference = Converter.binToString(individualAssetReferenceBin);
            } else if (tagSize.getSerialBitCount() == 38) {
                individualAssetReference = Converter.binToDec(individualAssetReferenceBin);
            }

            companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l());
            filterValue = GIAIFilterValue.of(Integer.parseInt(filterDec));
            prefixLength = PrefixLength.of(tableItem.l());
        } else {
            if (optionalCompanyPrefix.isPresent()) {
                final GIAIPartitionTable partitionTable = GIAIPartitionTable.getInstance(tagSize);
                prefixLength = PrefixLength.of(companyPrefix.length());

                validateCompanyPrefix();

                tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

                validateIndividualAssetReference();
            } else {
                if (optionalEpcTagURI.isPresent()) {
                    final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Tag URI is invalid");
                    }

                    tagSize = GIAITagSize.of(Integer.parseInt(matcher.group(2)));
                    filterValue = GIAIFilterValue.of(Integer.parseInt(matcher.group(3)));
                    companyPrefix = matcher.group(4);
                    prefixLength = PrefixLength.of(matcher.group(4).length());
                    individualAssetReference = matcher.group(5);
                } else if (optionalEpcPureIdentityURI.isPresent()) {
                    final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Pure Identity is invalid");
                    }

                    companyPrefix = matcher.group(2);
                    prefixLength = PrefixLength.of(matcher.group(2).length());
                    individualAssetReference = matcher.group(3);
                }
            }

            final GIAIPartitionTable partitionTable = GIAIPartitionTable.getInstance(tagSize);
            tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
        }

        final String outputBin = getBinary();
        final String outputHex = Converter.binToHex(outputBin);

        // giai.setEpcScheme("giai");
        giai.setApplicationIdentifier("AI 8004");
        giai.setTagSize(Integer.toString(tagSize.getValue()));
        giai.setFilterValue(Integer.toString(filterValue.getValue()));
        giai.setPartitionValue(Integer.toString(tableItem.partitionValue()));
        giai.setPrefixLength(Integer.toString(prefixLength.getValue()));
        giai.setCompanyPrefix(companyPrefix);
        giai.setIndividualAssetReference(individualAssetReference);
        giai.setEpcPureIdentityURI(String.format("urn:epc:id:giai:%s.%s", companyPrefix, individualAssetReference));
        giai.setEpcTagURI(String.format("urn:epc:tag:giai-%s:%s.%s.%s", tagSize.getValue(), filterValue.getValue(), companyPrefix, individualAssetReference));
        giai.setEpcRawURI(String.format("urn:epc:raw:%s.x%s", tagSize.getValue() + remainder, outputHex));
        giai.setBinary(outputBin);
        giai.setRfidTag(outputHex);
    }

    private String getBinary() {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        remainder = Converter.remainder(tagSize.getValue());

        bin.append(Converter.decToBin(tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(filterValue.getValue(), 3));
        bin.append(Converter.decToBin(tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(companyPrefix), tableItem.m()));

        if (tagSize.getValue() == 202) {
            bin.append(Converter.fill(Converter.StringToBinary(individualAssetReference, 7), tableItem.n() + remainder));
        } else if (tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(individualAssetReference, tableItem.n() + remainder));
        }

        return bin.toString();
    }

    public GIAI getGIAI() {
        return giai;
    }

    public String getRfidTag() {
        return Converter.binToHex(getBinary());
    }

    private void validateCompanyPrefix() {
        final Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    private void validateIndividualAssetReference() {
        if (individualAssetReference.length() > tableItem.digits()) {
            throw new IllegalArgumentException(String.format("Individual Asset Reference value is out of range. The length should be %d",
                tableItem.digits()));
        }

        if (tagSize.getValue() == 96) {
            if (individualAssetReference.startsWith("0")) {
                throw new IllegalArgumentException("Individual Asset Reference with leading zeros is not allowed");
            }
        }
    }

    public interface ChoiceStep {
        IndividualAssetReferenceStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEPCTagURI(final String epcTagURI);

        TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI);
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
        ParseGIAI build();
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
        public ParseGIAI build() {
            return new ParseGIAI(this);
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
        public BuildStep withEPCTagURI(final String epcTagURI) {
            this.epcTagURI = epcTagURI;
            return this;
        }

        @Override
        public TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI) {
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
