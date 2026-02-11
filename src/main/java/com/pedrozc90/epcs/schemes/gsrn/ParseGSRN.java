package com.pedrozc90.epcs.schemes.gsrn;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.gsrn.enums.GSRNFilterValue;
import com.pedrozc90.epcs.schemes.gsrn.enums.GSRNHeader;
import com.pedrozc90.epcs.schemes.gsrn.enums.GSRNTagSize;
import com.pedrozc90.epcs.schemes.gsrn.objects.GSRN;
import com.pedrozc90.epcs.schemes.gsrn.partitionTable.GSRNPartitionTable;
import com.pedrozc90.epcs.utils.Converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseGSRN {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("(urn:epc:tag:gsrn-)(96):([0-7])\\.(\\d+)\\.(\\d+)");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("(urn:epc:id:gsrn):(\\d+)\\.(\\d+)");

    private static final GSRNPartitionTable partitionTable = new GSRNPartitionTable();
    private static final Integer RESERVED = 0; // 24 zero bits

    private GSRN gsrn = new GSRN();
    private String companyPrefix;
    private PrefixLength prefixLength;
    private GSRNTagSize tagSize;
    private GSRNFilterValue filterValue;
    private String serviceReference;
    private String rfidTag;
    private String epcTagURI;
    private String epcPureIdentityURI;
    private TableItem tableItem;

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private ParseGSRN(final Steps steps) {
        this.companyPrefix = steps.companyPrefix;
        this.tagSize = steps.tagSize;
        this.filterValue = steps.filterValue;
        this.serviceReference = steps.serviceReference;
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

            tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

            String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
            String serialWithExtensionBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
            String filterDec = Long.toString(Long.parseLong(filterBin, 2));
            String companyPrefixDec = Converter.binToDec(companyPrefixBin);

            serviceReference = Converter.strZero(Converter.binToDec(serialWithExtensionBin), tableItem.digits());
            companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l());
            filterValue = GSRNFilterValue.of(Integer.parseInt(filterDec));
            tagSize = GSRNTagSize.of(GSRNHeader.of(headerBin).getTagSize());
            prefixLength = PrefixLength.of(tableItem.l());

        } else {
            if (optionalCompanyPrefix.isPresent()) {
                prefixLength = PrefixLength.of(companyPrefix.length());

                validateCompanyPrefix();

                tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

                validateServiceReference();
            } else {
                if (optionalEpcTagURI.isPresent()) {
                    final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Tag URI is invalid");
                    }

                    tagSize = GSRNTagSize.of(Integer.parseInt(matcher.group(2)));
                    filterValue = GSRNFilterValue.of(Integer.parseInt(matcher.group(3)));
                    companyPrefix = matcher.group(4);
                    prefixLength = PrefixLength.of(matcher.group(4).length());
                    serviceReference = matcher.group(5);
                } else if (optionalEpcPureIdentityURI.isPresent()) {

                    final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Pure Identity is invalid");
                    }

                    companyPrefix = matcher.group(2);
                    prefixLength = PrefixLength.of(matcher.group(2).length());
                    serviceReference = matcher.group(3);
                }

                tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
            }
        }

        final String outputBin = getBinary();
        final String outputHex = Converter.binToHex(outputBin);

        // gsrn.setEpcScheme("gsrn");
        gsrn.setApplicationIdentifier("AI 8018");
        gsrn.setTagSize(Integer.toString(tagSize.getValue()));
        gsrn.setFilterValue(Integer.toString(filterValue.getValue()));
        gsrn.setPartitionValue(Integer.toString(tableItem.partitionValue()));
        gsrn.setPrefixLength(Integer.toString(prefixLength.getValue()));
        gsrn.setCompanyPrefix(companyPrefix);
        gsrn.setServiceReference(serviceReference);
        gsrn.setCheckDigit(Integer.toString(getCheckDigit()));
        gsrn.setEpcPureIdentityURI(String.format("urn:epc:id:gsrn:%s.%s", companyPrefix, serviceReference));
        gsrn.setEpcTagURI(String.format("urn:epc:tag:gsrn-%s:%s.%s.%s", tagSize.getValue(), filterValue.getValue(), companyPrefix, serviceReference));
        gsrn.setEpcRawURI(String.format("urn:epc:raw:%s.x%s", tagSize.getValue(), outputHex));
        gsrn.setBinary(outputBin);
        gsrn.setRfidTag(outputHex);
    }

    private Integer getCheckDigit() {
        String value = new StringBuilder()
            .append(companyPrefix)
            .append(serviceReference)
            .toString();

        Integer d18 = (10 - ((3
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

    private String getBinary() {
        StringBuilder bin = new StringBuilder();

        bin.append(Converter.decToBin(tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(filterValue.getValue(), 3));
        bin.append(Converter.decToBin(tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(companyPrefix), tableItem.m()));
        bin.append(Converter.decToBin(Integer.parseInt(serviceReference), tableItem.n()));
        bin.append(Converter.decToBin(RESERVED, 24));

        return bin.toString();
    }

    public GSRN getGSRN() {
        return gsrn;
    }

    public String getRfidTag() {
        return Converter.binToHex(getBinary());
    }

    private void validateServiceReference() {
        StringBuilder value = new StringBuilder()
            .append(serviceReference);

        if (value.length() != tableItem.digits()) {
            throw new IllegalArgumentException(String.format("Service Reference \"%s\" has %d length and should have %d length",
                serviceReference, value.length(), tableItem.digits()));
        }
    }

    private void validateCompanyPrefix() {
        final Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    public interface ChoiceStep {
        ServiceReferenceStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEPCTagURI(final String epcTagURI);

        TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface ServiceReferenceStep {
        TagSizeStep withServiceReference(final String serviceReference);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final GSRNTagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final GSRNFilterValue filterValue);
    }

    public interface BuildStep {
        ParseGSRN build();
    }

    private static class Steps implements ChoiceStep, ServiceReferenceStep, TagSizeStep, FilterValueStep, BuildStep {

        private String companyPrefix;
        private GSRNTagSize tagSize;
        private GSRNFilterValue filterValue;
        private String serviceReference;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public ParseGSRN build() {
            return new ParseGSRN(this);
        }

        @Override
        public BuildStep withFilterValue(final GSRNFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final GSRNTagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public TagSizeStep withServiceReference(final String serviceReference) {
            this.serviceReference = serviceReference;
            return this;
        }

        @Override
        public ServiceReferenceStep withCompanyPrefix(final String companyPrefix) {
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

    }

}
