package com.pedrozc90.epcs.schemes.cpi;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.cpi.enums.CPIFilterValue;
import com.pedrozc90.epcs.schemes.cpi.enums.CPIHeader;
import com.pedrozc90.epcs.schemes.cpi.enums.CPITagSize;
import com.pedrozc90.epcs.schemes.cpi.objects.CPI;
import com.pedrozc90.epcs.schemes.cpi.partitionTable.CPIPartitionTable;
import com.pedrozc90.epcs.utils.Converter;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseCPI {

    // urn:epc:tag:cpi-96:F.C.P.S
    // urn:epc:tag:cpi-var:F.C.P.S
    private static final Pattern TAG_URI_PATTERN = Pattern.compile("(urn:epc:tag:cpi-)(96|var):([0-7])\\.([0-9]+)\\.([0-9A-Za-z]+)\\.([0-9]+)");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("(urn:epc:id:cpi):([0-9]+)\\.([0-9A-Za-z]+)\\.([0-9]+)");

    private CPI cpi = new CPI();
    private String companyPrefix;
    private PrefixLength prefixLength;
    private CPITagSize tagSize;
    private CPIFilterValue filterValue;
    private String componentPartReference;
    private String serial;
    private String rfidTag;
    private String epcTagURI;
    private String epcPureIdentityURI;
    private TableItem tableItem;
    private int remainder;

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private ParseCPI(final Steps steps) {
        this.companyPrefix = steps.companyPrefix;
        this.tagSize = steps.tagSize;
        this.filterValue = steps.filterValue;
        this.componentPartReference = steps.componentPartReference;
        this.serial = steps.serial;
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

            tagSize = CPITagSize.of(CPIHeader.of(headerBin).getTagSize());
            final CPIPartitionTable partitionTable = CPIPartitionTable.getInstance(tagSize);
            tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

            String filterDec = Long.toString(Long.parseLong(filterBin, 2));
            String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
            String componentPartReferenceBin = null;
            String serialBin = null;

            if (tagSize.getValue() == 0) {  // variable
                String componentPartReferenceAndSerialBin = inputBin.substring(14 + tableItem.m());

                StringBuilder decodeComponentPartReference = new StringBuilder();
                final List<String> parts = Converter.chunk(componentPartReferenceAndSerialBin, 6);
                for (String part : parts) {
                    if (part.equals("000000")) {
                        break;
                    }
                    decodeComponentPartReference.append(part);
                }

                componentPartReferenceBin = decodeComponentPartReference.toString();
                int posSerial = 14 + tableItem.m() + componentPartReferenceBin.length() + 6;
                componentPartReferenceBin = Converter.convertBinToBit(componentPartReferenceBin, 6, 8);
                componentPartReference = Converter.binToString(componentPartReferenceBin);
                serialBin = inputBin.substring(posSerial, posSerial + tagSize.getSerialBitCount());
            } else if (tagSize.getValue() == 96) {
                componentPartReferenceBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
                componentPartReference = Converter.binToDec(componentPartReferenceBin);
                serialBin = inputBin.substring(14 + tableItem.m() + tableItem.n());
            }

            String companyPrefixDec = Converter.binToDec(companyPrefixBin);
            serial = Converter.binToDec(serialBin);
            companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l()); // strzero aqui
            filterValue = CPIFilterValue.of(Integer.parseInt(filterDec));
            prefixLength = PrefixLength.of(tableItem.l());
        } else {
            if (optionalCompanyPrefix.isPresent()) {
                final CPIPartitionTable partitionTable = CPIPartitionTable.getInstance(tagSize);
                prefixLength = PrefixLength.of(companyPrefix.length());

                validateCompanyPrefix();

                tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

                validateComponentPartReference();
                validateSerial();
            } else {
                if (optionalEpcTagURI.isPresent()) {
                    final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Tag URI is invalid");
                    }

                    if (matcher.group(2).equals("var")) {
                        tagSize = CPITagSize.of(0);
                    } else {
                        tagSize = CPITagSize.of(Integer.parseInt(matcher.group(2)));
                    }

                    filterValue = CPIFilterValue.of(Integer.parseInt(matcher.group(3)));
                    companyPrefix = matcher.group(4);
                    prefixLength = PrefixLength.of(matcher.group(4).length());
                    componentPartReference = matcher.group(5);
                    serial = matcher.group(6);
                } else if (optionalEpcPureIdentityURI.isPresent()) {
                    final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Pure Identity is invalid");
                    }

                    companyPrefix = matcher.group(2);
                    prefixLength = PrefixLength.of(matcher.group(2).length());
                    componentPartReference = matcher.group(3);
                    serial = matcher.group(4);
                }
            }

            final CPIPartitionTable partitionTable = CPIPartitionTable.getInstance(tagSize);
            tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
        }

        final String outputBin = getBinary();
        final String outputHex = Converter.binToHex(outputBin);

        // cpi.setEpcScheme("cpi");
        cpi.setApplicationIdentifier("AI 8010 + AI 8011");
        cpi.setTagSize((tagSize.getValue() == 0) ? "var" : Integer.toString(tagSize.getValue()));
        cpi.setFilterValue(Integer.toString(filterValue.getValue()));
        cpi.setPartitionValue(Integer.toString(tableItem.partitionValue()));
        cpi.setPrefixLength(Integer.toString(prefixLength.getValue()));
        cpi.setCompanyPrefix(companyPrefix);
        cpi.setComponentPartReference(componentPartReference);
        cpi.setSerial(serial);
        cpi.setEpcPureIdentityURI(String.format("urn:epc:id:cpi:%s.%s.%s", companyPrefix, componentPartReference, serial));
        cpi.setEpcTagURI(String.format("urn:epc:tag:cpi-%s:%s.%s.%s.%s", (tagSize.getValue() == 0) ? "var" : tagSize.getValue(), filterValue.getValue(), companyPrefix, componentPartReference, serial));
        cpi.setEpcRawURI(String.format("urn:epc:raw:%s.x%s", outputBin.length(), outputHex));
        cpi.setBinary(outputBin);
        cpi.setRfidTag(outputHex);
    }

    private String getBinary() {
        final StringBuilder bin = new StringBuilder();

        bin.append(Converter.decToBin(tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(filterValue.getValue(), 3));
        bin.append(Converter.decToBin(tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(companyPrefix), tableItem.m()));

        // variable
        if (tagSize.getValue() == 0) {
            // bin.append(Converter.StringToBinary(componentPartReference, 6));
            bin.append(Converter.to6BitsBinary(componentPartReference));
            bin.append("000000");
        } else if (tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(Integer.parseInt(componentPartReference), tableItem.n()));
        }

        bin.append(Converter.decToBin(serial, tagSize.getSerialBitCount()));

        // remainder = (int) (Math.ceil((bin.length() / 16.0)) * 16) - bin.length();
        remainder = Converter.remainder(bin.length());
        if (remainder > 0) {
            bin.append(Converter.fill("0", remainder));
        }

        return bin.toString();
    }

    public CPI getCPI() {
        return cpi;
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

    private void validateComponentPartReference() {
        if (Converter.isNotNumeric(componentPartReference)) {
            throw new IllegalArgumentException("Component/Part Reference is allowed with numerical only");
        }

        if (componentPartReference.length() > tableItem.digits()) {
            throw new IllegalArgumentException("Component/Part Reference is out of range");
        }

        if (tagSize.getValue() == 96) {
            if (componentPartReference.startsWith("0")) {
                throw new IllegalArgumentException("Component/Part Reference with leading zeros is not allowed");
            }
        }
    }

    private void validateSerial() {
        if (serial.startsWith("0")) {
            throw new IllegalArgumentException("Serial with leading zeros is not allowed");
        }

        if (tagSize.getValue() == 0) { // variable
            if (serial.length() > tagSize.getSerialMaxLength()) {
                throw new IllegalArgumentException(String.format("Serial value is out of range. Should be up to %d alphanumeric characters", tagSize.getSerialMaxLength()));
            }
        } else if (tagSize.getValue() == 96) {
            if (Converter.isNotNumeric(serial)) {
                throw new IllegalArgumentException("Serial value is allowed with numerical only");
            }

            if (Long.parseLong(serial) > tagSize.getSerialMaxValue()) {
                throw new IllegalArgumentException(String.format("Serial value is out of range. Should be less than or equal %d", tagSize.getSerialMaxValue()));
            }
        }
    }

    public interface ChoiceStep {
        componentPartReferenceStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEPCTagURI(final String epcTagURI);

        TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface componentPartReferenceStep {
        serialStep withComponentPartReference(final String componentPartReference);
    }

    public interface serialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final CPITagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final CPIFilterValue filterValue);
    }

    public interface BuildStep {
        ParseCPI build();
    }

    private static class Steps implements ChoiceStep, componentPartReferenceStep, serialStep, TagSizeStep, FilterValueStep, BuildStep {

        private String companyPrefix;
        private CPITagSize tagSize;
        private CPIFilterValue filterValue;
        private String componentPartReference;
        private String serial;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public ParseCPI build() {
            return new ParseCPI(this);
        }

        @Override
        public BuildStep withFilterValue(final CPIFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final CPITagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public TagSizeStep withSerial(final String serial) {
            this.serial = serial;
            return this;
        }

        @Override
        public serialStep withComponentPartReference(final String componentPartReference) {
            this.componentPartReference = componentPartReference;
            return this;
        }

        @Override
        public componentPartReferenceStep withCompanyPrefix(final String companyPrefix) {
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
