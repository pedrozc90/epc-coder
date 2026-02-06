package org.epctagcoder.schemas.giai;

import org.epctagcoder.schemas.PrefixLength;
import org.epctagcoder.schemas.TableItem;
import org.epctagcoder.schemas.giai.enums.GIAIFilterValue;
import org.epctagcoder.schemas.giai.enums.GIAIHeader;
import org.epctagcoder.schemas.giai.enums.GIAITagSize;
import org.epctagcoder.schemas.giai.partitionTable.GIAIPartitionTable;
import org.epctagcoder.utils.Converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParseGIAI {

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

            tagSize = GIAITagSize.forCode(GIAIHeader.forCode(headerBin).getTagSize());
            final GIAIPartitionTable partitionTable = new GIAIPartitionTable(tagSize);
            tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

            String filterDec = Long.toString(Long.parseLong(filterBin, 2));
            String companyPrefixBin = inputBin.substring(14, 14 + tableItem.getM());
            String individualAssetReferenceBin = inputBin.substring(14 + tableItem.getM(), 14 + tableItem.getM() + tableItem.getN());
            String companyPrefixDec = Converter.binToDec(companyPrefixBin);

            if (tagSize.getSerialBitCount() == 112) {
                individualAssetReferenceBin = Converter.convertBinToBit(individualAssetReferenceBin, 7, 8);
                individualAssetReference = Converter.binToString(individualAssetReferenceBin);
            } else if (tagSize.getSerialBitCount() == 38) {
                individualAssetReference = Converter.binToDec(individualAssetReferenceBin);
            }

            companyPrefix = Converter.strZero(companyPrefixDec, tableItem.getL());
            filterValue = GIAIFilterValue.forCode(Integer.parseInt(filterDec));
            prefixLength = PrefixLength.forCode(tableItem.getL());
        } else {
            if (optionalCompanyPrefix.isPresent()) {
                final GIAIPartitionTable partitionTable = new GIAIPartitionTable(tagSize);
                prefixLength = PrefixLength.forCode(companyPrefix.length());

                validateCompanyPrefix();

                tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

                validateIndividualAssetReference();
            } else {
                if (optionalEpcTagURI.isPresent()) {
                    Pattern pattern = Pattern.compile("(urn:epc:tag:giai-)(96|202)\\:([0-7])\\.(\\d+)\\.(\\w+)");
                    Matcher matcher = pattern.matcher(epcTagURI);

                    if (matcher.matches()) {
                        tagSize = GIAITagSize.forCode(Integer.parseInt(matcher.group(2)));
                        filterValue = GIAIFilterValue.forCode(Integer.parseInt(matcher.group(3)));
                        companyPrefix = matcher.group(4);
                        prefixLength = PrefixLength.forCode(matcher.group(4).length());
                        individualAssetReference = matcher.group(5);
                    } else {
                        throw new IllegalArgumentException("EPC Tag URI is invalid");
                    }
                } else if (optionalEpcPureIdentityURI.isPresent()) {
                    Pattern pattern = Pattern.compile("(urn:epc:id:giai)\\:(\\d+)\\.(\\w+)");

                    Matcher matcher = pattern.matcher(epcPureIdentityURI);

                    if (matcher.matches()) {
                        companyPrefix = matcher.group(2);
                        prefixLength = PrefixLength.forCode(matcher.group(2).length());
                        individualAssetReference = matcher.group(3);
                        ;
                    } else {
                        throw new IllegalArgumentException("EPC Pure Identity is invalid");
                    }
                }

            }

            final GIAIPartitionTable partitionTable = new GIAIPartitionTable(tagSize);
            tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
        }

        final String outputBin = getBinary();
        final String outputHex = Converter.binToHex(outputBin);

        giai.setEpcScheme("giai");
        giai.setApplicationIdentifier("AI 8004");
        giai.setTagSize(Integer.toString(tagSize.getValue()));
        giai.setFilterValue(Integer.toString(filterValue.getValue()));
        giai.setPartitionValue(Integer.toString(tableItem.getPartitionValue()));
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
        StringBuilder bin = new StringBuilder();

        remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();

        bin.append(Converter.decToBin(tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(filterValue.getValue(), 3));
        bin.append(Converter.decToBin(tableItem.getPartitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(companyPrefix), tableItem.getM()));

        if (tagSize.getValue() == 202) {
            bin.append(Converter.fill(Converter.StringToBinary(individualAssetReference, 7), tableItem.getN() + remainder));
        } else if (tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(individualAssetReference, tableItem.getN() + remainder));
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
        Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    private void validateIndividualAssetReference() {
        if (individualAssetReference.length() > tableItem.getDigits()) {
            throw new IllegalArgumentException(String.format("Individual Asset Reference value is out of range. The length should be %d",
                tableItem.getDigits()));
        }

        if (tagSize.getValue() == 96) {
            if (individualAssetReference.startsWith("0")) {
                throw new IllegalArgumentException("Individual Asset Reference with leading zeros is not allowed");
            }
        }
    }

    public static interface ChoiceStep {
        IndividualAssetReferenceStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEPCTagURI(final String epcTagURI);

        TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI);
    }

    public static interface IndividualAssetReferenceStep {
        TagSizeStep withIndividualAssetReference(String individualAssetReference);
    }


    public static interface TagSizeStep {
        FilterValueStep withTagSize(final GIAITagSize tagSize);
    }

    public static interface FilterValueStep {
        BuildStep withFilterValue(GIAIFilterValue filterValue);
    }

    public static interface BuildStep {
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
