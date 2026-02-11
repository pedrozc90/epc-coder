package com.pedrozc90.epcs.schemes.sgln;

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

public class ParseSGLN {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("(urn:epc:tag:sgln-)(96|195):([0-7])\\.(\\d+)\\.(\\d+)\\.((\\w|/)+)");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("(urn:epc:id:sgln):(\\d+)\\.(\\d+)\\.(\\w+)");

    private static final SGLNPartitionTable partitionTable = new SGLNPartitionTable();

    private SGLN sgln = new SGLN();
    private String companyPrefix;
    private PrefixLength prefixLength;
    private SGLNTagSize tagSize;
    private SGLNFilterValue filterValue;
    private String locationReference;
    private String extension;
    private String rfidTag;
    private String epcTagURI;
    private String epcPureIdentityURI;
    private TableItem tableItem;
    private int remainder;

    private ParseSGLN(final Steps steps) {
        companyPrefix = steps.getCompanyPrefix();
        tagSize = steps.getTagSize();
        filterValue = steps.getFilterValue();
        locationReference = steps.getLocationReference();
        extension = steps.getExtension();
        rfidTag = steps.getRfidTag();
        epcTagURI = steps.getEpcTagURI();
        epcPureIdentityURI = steps.getEpcPureIdentityURI();
        parse();
    }

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private void parse() {
        Optional<String> optionalCompanyPrefix = Optional.ofNullable(companyPrefix);
        Optional<String> optionalRfidTag = Optional.ofNullable(rfidTag);
        Optional<String> optionalEpcTagURI = Optional.ofNullable(epcTagURI);
        Optional<String> optionalEpcPureIdentityURI = Optional.ofNullable(epcPureIdentityURI);

        if (optionalRfidTag.isPresent()) {
            parseRfidTag();
        } else {
            if (optionalCompanyPrefix.isPresent()) {
                prefixLength = PrefixLength.of(companyPrefix.length());
                validateCompanyPrefix();
                tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
                validateLocationReference();
                validateExtension();
            } else {
                if (optionalEpcTagURI.isPresent()) {
                    parseEpcTagURI();
                } else if (optionalEpcPureIdentityURI.isPresent()) {
                    parseEpcPureIdentityURI();
                }
            }

            final SGLNPartitionTable partitionTable = new SGLNPartitionTable();
            tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
        }

        final String outputBin = getBinary();
        final String outputHex = Converter.binToHex(outputBin);

        // sgln.setEpcScheme("sgln");
        sgln.setApplicationIdentifier("AI 254");
        sgln.setTagSize(Integer.toString(tagSize.getValue()));
        sgln.setFilterValue(Integer.toString(filterValue.getValue()));
        sgln.setPartitionValue(Integer.toString(tableItem.partitionValue()));
        sgln.setPrefixLength(Integer.toString(prefixLength.getValue()));
        sgln.setCompanyPrefix(companyPrefix);
        sgln.setLocationReference(locationReference);
        sgln.setExtension(extension);
        sgln.setCheckDigit(Integer.toString(getCheckDigit()));
        sgln.setEpcPureIdentityURI(String.format("urn:epc:id:sgln:%s.%s.%s", companyPrefix, locationReference, extension));
        sgln.setEpcTagURI(String.format("urn:epc:tag:sgln-%s:%s.%s.%s.%s", tagSize.getValue(), filterValue.getValue(), companyPrefix, locationReference, extension));
        sgln.setEpcRawURI(String.format("urn:epc:raw:%s.x%s", tagSize.getValue() + remainder, outputHex));
        sgln.setBinary(outputBin);
        sgln.setRfidTag(outputHex);
    }

    private void parseRfidTag() {
        final String inputBin = Converter.hexToBin(rfidTag);
        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        tagSize = SGLNTagSize.of(SGLNHeader.of(headerBin).getTagSize());
        tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));
        String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        String locationReferenceBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
        String extensionBin = inputBin.substring(14 + tableItem.m() + tableItem.n());

        String companyPrefixDec = Converter.binToDec(companyPrefixBin);
        String locationReferenceDec = Converter.binToDec(locationReferenceBin);

        locationReference = Converter.strZero(locationReferenceDec, tableItem.digits());

        if (tagSize.getSerialBitCount() == 140) {
            extensionBin = Converter.convertBinToBit(extensionBin, 7, 8);
            extension = Converter.binToString(extensionBin);
        } else if (tagSize.getSerialBitCount() == 41) {
            extension = Converter.binToDec(extensionBin);
        }

        companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l());
        filterValue = SGLNFilterValue.of(Integer.parseInt(filterDec));
        prefixLength = PrefixLength.of(tableItem.l());
    }

    private void parseEpcTagURI() {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("EPC Tag URI is invalid");
        }

        tagSize = SGLNTagSize.of(Integer.parseInt(matcher.group(2)));
        filterValue = SGLNFilterValue.of(Integer.parseInt(matcher.group(3)));
        companyPrefix = matcher.group(4);
        prefixLength = PrefixLength.of(matcher.group(4).length());
        locationReference = matcher.group(5);
        extension = matcher.group(6);
    }

    private void parseEpcPureIdentityURI() {
        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("EPC Pure Identity is invalid");
        }

        companyPrefix = matcher.group(2);
        prefixLength = PrefixLength.of(matcher.group(2).length());
        locationReference = matcher.group(3);
        extension = matcher.group(4);
    }

    private String getBinary() {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        remainder = Converter.remainder(tagSize.getValue());

        bin.append(Converter.decToBin(tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(filterValue.getValue(), 3));
        bin.append(Converter.decToBin(tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(companyPrefix), tableItem.m()));
        bin.append(Converter.decToBin(Integer.parseInt(locationReference), tableItem.n()));

        if (tagSize.getValue() == 195) {
            bin.append(Converter.fill(Converter.StringToBinary(extension, 7), tagSize.getSerialBitCount() + remainder));
        } else if (tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(extension, tagSize.getSerialBitCount() + remainder));
        }

        return bin.toString();
    }

    private Integer getCheckDigit() {
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

    public SGLN getSGLN() {
        return sgln;
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

    private void validateLocationReference() {
        if (locationReference.length() != tableItem.digits()) {
            throw new IllegalArgumentException(String.format("Location Reference \"%s\" has %d length and should have %d length", locationReference, locationReference.length(), tableItem.digits()));
        }
    }

    private void validateExtension() {
        if (tagSize.getValue() == 195) {
            if (extension.length() > tagSize.getSerialMaxLength()) {
                throw new IllegalArgumentException(String.format("Extension value is out of range. Should be up to %d alphanumeric characters", tagSize.getSerialMaxLength()));
            }
        } else if (tagSize.getValue() == 96) {
            if (Long.parseLong(extension) > tagSize.getSerialMaxValue()) {
                throw new IllegalArgumentException(String.format("Extension value is out of range. Should be less than or equal %d", tagSize.getSerialMaxValue()));
            }
        }
    }

    public interface ChoiceStep {
        LocationReferenceStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEPCTagURI(final String epcTagURI);

        TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface BuildStep {
        ParseSGLN build();
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
        public ParseSGLN build() {
            return new ParseSGLN(this);
        }

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

    }

}
