package com.pedrozc90.epcs.schemes.gdti;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.gdti.enums.GDTIFilterValue;
import com.pedrozc90.epcs.schemes.gdti.enums.GDTIHeader;
import com.pedrozc90.epcs.schemes.gdti.enums.GDTITagSize;
import com.pedrozc90.epcs.schemes.gdti.objects.GDTI;
import com.pedrozc90.epcs.schemes.gdti.partitionTable.GDTIPartitionTable;
import com.pedrozc90.epcs.utils.Converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseGDTI {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("(urn:epc:tag:gdti-)(96|174):([0-7])\\.(\\d+)\\.(\\d+)\\.(\\w+)");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("(urn:epc:id:gdti):(\\d+)\\.(\\d+)\\.(\\w+)");

    private static final GDTIPartitionTable partitionTable = new GDTIPartitionTable();

    private GDTI gdti = new GDTI();
    private String companyPrefix;
    private PrefixLength prefixLength;
    private GDTITagSize tagSize;
    private GDTIFilterValue filterValue;
    private String docType;
    private String serial;
    private String rfidTag;
    private String epcTagURI;
    private String epcPureIdentityURI;
    private TableItem tableItem;
    private int remainder;

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private ParseGDTI(final Steps steps) {
        this.companyPrefix = steps.companyPrefix;
        this.tagSize = steps.tagSize;
        this.filterValue = steps.filterValue;
        this.docType = steps.docType;
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

            tagSize = GDTITagSize.of(GDTIHeader.of(headerBin).getTagSize());
            tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

            String filterDec = Long.toString(Long.parseLong(filterBin, 2));
            String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
            String docTypeBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
            String serialBin = inputBin.substring(14 + tableItem.m() + tableItem.n());

            String companyPrefixDec = Converter.binToDec(companyPrefixBin);
            String docTypeDec = Converter.binToDec(docTypeBin);

            docType = Converter.strZero(docTypeDec, tableItem.digits());

            if (tagSize.getSerialBitCount() == 119) {
                serialBin = Converter.convertBinToBit(serialBin, 7, 8);
                serial = Converter.binToString(serialBin);
            } else if (tagSize.getSerialBitCount() == 41) {
                serial = Converter.binToDec(serialBin);
            }

            companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l()); // strzero aqui
            filterValue = GDTIFilterValue.of(Integer.parseInt(filterDec));
            prefixLength = PrefixLength.of(tableItem.l());
        } else {
            if (optionalCompanyPrefix.isPresent()) {
                prefixLength = PrefixLength.of(companyPrefix.length());

                validateCompanyPrefix();

                tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

                validateDocType();
                validateSerial();

            } else {
                if (optionalEpcTagURI.isPresent()) {
                    final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Tag URI is invalid");
                    }

                    tagSize = GDTITagSize.of(Integer.parseInt(matcher.group(2)));
                    filterValue = GDTIFilterValue.of(Integer.parseInt(matcher.group(3)));
                    companyPrefix = matcher.group(4);
                    prefixLength = PrefixLength.of(matcher.group(4).length());
                    docType = matcher.group(5);
                    serial = matcher.group(6);
                } else if (optionalEpcPureIdentityURI.isPresent()) {
                    final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Pure Identity is invalid");
                    }

                    companyPrefix = matcher.group(2);
                    prefixLength = PrefixLength.of(matcher.group(2).length());
                    docType = matcher.group(3);
                    serial = matcher.group(4);
                }
            }

            tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
        }

        final String outputBin = getBinary();
        final String outputHex = Converter.binToHex(outputBin);

        // gdti.setEpcScheme("gdti");
        gdti.setApplicationIdentifier("AI 253");
        gdti.setTagSize(Integer.toString(tagSize.getValue()));
        gdti.setFilterValue(Integer.toString(filterValue.getValue()));
        gdti.setPartitionValue(Integer.toString(tableItem.partitionValue()));
        gdti.setPrefixLength(Integer.toString(prefixLength.getValue()));
        gdti.setCompanyPrefix(companyPrefix);
        gdti.setDocType(docType);
        gdti.setSerial(serial);
        gdti.setCheckDigit(Integer.toString(getCheckDigit()));
        gdti.setEpcPureIdentityURI(String.format("urn:epc:id:gdti:%s.%s.%s", companyPrefix, docType, serial));
        gdti.setEpcTagURI(String.format("urn:epc:tag:gdti-%s:%s.%s.%s.%s", tagSize.getValue(), filterValue.getValue(), companyPrefix, docType, serial));
        gdti.setEpcRawURI(String.format("urn:epc:raw:%s.x%s", tagSize.getValue() + remainder, outputHex));
        gdti.setBinary(outputBin);
        gdti.setRfidTag(outputHex);
    }

    private String getBinary() {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        remainder = Converter.remainder(tagSize.getValue());

        bin.append(Converter.decToBin(tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(filterValue.getValue(), 3));
        bin.append(Converter.decToBin(tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(companyPrefix), tableItem.m()));
        bin.append(Converter.decToBin(Integer.parseInt(docType), tableItem.n()));

        if (tagSize.getValue() == 174) {
            bin.append(Converter.fill(Converter.StringToBinary(serial, 7), tagSize.getSerialBitCount() + remainder));
        } else if (tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(serial, tagSize.getSerialBitCount() + remainder));
        }

        return bin.toString();
    }

    private Integer getCheckDigit() {
        final String value = new StringBuilder()
            .append(companyPrefix)
            .append(docType)
            .toString();

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

    public GDTI getGDTI() {
        return gdti;
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

    private void validateDocType() {
        if (docType.length() != tableItem.digits()) {
            throw new IllegalArgumentException(String.format("Asset Type \"%s\" has %d length and should have %d length",
                docType, docType.length(), tableItem.digits()));
        }
    }

    private void validateSerial() {
        if (tagSize.getValue() == 170) {
            if (serial.length() > tagSize.getSerialMaxLength()) {
                throw new IllegalArgumentException(String.format("Serial value is out of range. Should be up to %d alphanumeric characters",
                    tagSize.getSerialMaxLength()));
            }
        } else if (tagSize.getValue() == 96) {
            if (Long.parseLong(serial) > tagSize.getSerialMaxValue()) {
                throw new IllegalArgumentException(String.format("Serial value is out of range. Should be less than or equal %d",
                    tagSize.getSerialMaxValue()));
            }
            if (serial.startsWith("0")) {
                throw new IllegalArgumentException("Serial with leading zeros is not allowed");
            }
        }

    }

    public interface ChoiceStep {
        docTypeStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEPCTagURI(final String epcTagURI);

        TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface docTypeStep {
        serialStep withDocType(final String docType);
    }

    public interface serialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final GDTITagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final GDTIFilterValue filterValue);
    }

    public interface BuildStep {
        ParseGDTI build();
    }

    private static class Steps implements ChoiceStep, docTypeStep, serialStep, TagSizeStep, FilterValueStep, BuildStep {

        private String companyPrefix;
        private GDTITagSize tagSize;
        private GDTIFilterValue filterValue;
        private String docType;
        private String serial;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public ParseGDTI build() {
            return new ParseGDTI(this);
        }

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
        public serialStep withDocType(final String docType) {
            this.docType = docType;
            return this;
        }

        @Override
        public docTypeStep withCompanyPrefix(final String companyPrefix) {
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
