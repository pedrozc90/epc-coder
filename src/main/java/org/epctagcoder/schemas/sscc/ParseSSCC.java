package org.epctagcoder.schemas.sscc;

import org.epctagcoder.schemas.PrefixLength;
import org.epctagcoder.schemas.TableItem;
import org.epctagcoder.schemas.sscc.enums.SSCCExtensionDigit;
import org.epctagcoder.schemas.sscc.enums.SSCCFilterValue;
import org.epctagcoder.schemas.sscc.enums.SSCCHeader;
import org.epctagcoder.schemas.sscc.enums.SSCCTagSize;
import org.epctagcoder.schemas.sscc.partitionTable.SSCCPartitionTable;
import org.epctagcoder.utils.Converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseSSCC {

    private static final Integer RESERVED = 0; // 24 zero bits

    private SSCC sscc = new SSCC();
    private SSCCExtensionDigit extensionDigit;
    private String companyPrefix;
    private PrefixLength prefixLength;
    private SSCCTagSize tagSize;
    private SSCCFilterValue filterValue;
    private String serial;
    private String rfidTag;
    private String epcTagURI;
    private String epcPureIdentityURI;
    private TableItem tableItem;

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private ParseSSCC(Steps steps) {
        this.extensionDigit = steps.extensionDigit;
        this.companyPrefix = steps.companyPrefix;
        this.tagSize = steps.tagSize;
        this.filterValue = steps.filterValue;
        this.serial = steps.serial;
        this.rfidTag = steps.rfidTag;
        this.epcTagURI = steps.epcTagURI;
        this.epcPureIdentityURI = steps.epcPureIdentityURI;
        parse();
    }


    private void parse() {
        Optional<SSCCExtensionDigit> optionalCompanyPrefix = Optional.ofNullable(extensionDigit);
        Optional<String> optionalRfidTag = Optional.ofNullable(rfidTag);
        Optional<String> optionalEpcTagURI = Optional.ofNullable(epcTagURI);
        Optional<String> optionalEpcPureIdentityURI = Optional.ofNullable(epcPureIdentityURI);

        if (optionalRfidTag.isPresent()) {
            final String inputBin = Converter.hexToBin(rfidTag);
            final String headerBin = inputBin.substring(0, 8);
            final String filterBin = inputBin.substring(8, 11);
            final String partitionBin = inputBin.substring(11, 14);

            final SSCCPartitionTable partitionTable = new SSCCPartitionTable();
            tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

            String companyPrefixBin = inputBin.substring(14, 14 + tableItem.getM());
            String serialWithExtensionBin = inputBin.substring(14 + tableItem.getM(), 14 + tableItem.getM() + tableItem.getN());
            String filterDec = Long.toString(Long.parseLong(filterBin, 2));
            String companyPrefixDec = Converter.binToDec(companyPrefixBin);
            String serialWithExtension = Converter.strZero(Converter.binToDec(serialWithExtensionBin), tableItem.getDigits());
            String extensionDec = serialWithExtension.substring(0, 1);

            serial = serialWithExtension.substring(1);
            companyPrefix = Converter.strZero(companyPrefixDec, tableItem.getL());
            extensionDigit = SSCCExtensionDigit.forCode(Integer.parseInt(extensionDec));
            filterValue = SSCCFilterValue.forCode(Integer.parseInt(filterDec));
            tagSize = SSCCTagSize.forCode(SSCCHeader.forCode(headerBin).getTagSize());
            prefixLength = PrefixLength.forCode(tableItem.getL());
        } else {
            if (optionalCompanyPrefix.isPresent()) {
                final SSCCPartitionTable partitionTable = new SSCCPartitionTable();
                prefixLength = PrefixLength.forCode(companyPrefix.length());
                validateCompanyPrefix();
                tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
                validateExtensionDigitAndSerial();
            } else {
                if (optionalEpcTagURI.isPresent()) {
                    Pattern pattern = Pattern.compile("(urn:epc:tag:sscc-)(96)\\:([0-7])\\.(\\d+)\\.([0-9])(\\d+)");
                    Matcher matcher = pattern.matcher(epcTagURI);

                    if (matcher.matches()) {
                        tagSize = SSCCTagSize.forCode(Integer.parseInt(matcher.group(2)));
                        filterValue = SSCCFilterValue.forCode(Integer.parseInt(matcher.group(3)));
                        companyPrefix = matcher.group(4);
                        prefixLength = PrefixLength.forCode(matcher.group(4).length());
                        extensionDigit = SSCCExtensionDigit.forCode(Integer.parseInt(matcher.group(5)));
                        serial = matcher.group(6);
                    } else {
                        throw new IllegalArgumentException("EPC Tag URI is invalid");
                    }
                } else if (optionalEpcPureIdentityURI.isPresent()) {
                    Pattern pattern = Pattern.compile("(urn:epc:id:sscc)\\:(\\d+)\\.([0-9])(\\d+)");
                    Matcher matcher = pattern.matcher(epcPureIdentityURI);

                    if (matcher.matches()) {
                        companyPrefix = matcher.group(2);
                        prefixLength = PrefixLength.forCode(matcher.group(2).length());
                        extensionDigit = SSCCExtensionDigit.forCode(Integer.parseInt(matcher.group(3)));
                        serial = matcher.group(4);
                    } else {
                        throw new IllegalArgumentException("EPC Pure Identity is invalid");
                    }
                }

                if (prefixLength == null) {
                    throw new IllegalArgumentException("Invalid Prefix Length");
                } else {
                    final SSCCPartitionTable partitionTable = new SSCCPartitionTable();
                    tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
                }
            }
        }

        final String outputBin = getBinary();
        final String outputHex = Converter.binToHex(outputBin);

        sscc.setEpcScheme("sscc");
        sscc.setApplicationIdentifier("AI 00");
        sscc.setTagSize(Integer.toString(tagSize.getValue()));
        sscc.setFilterValue(Integer.toString(filterValue.getValue()));
        sscc.setPartitionValue(Integer.toString(tableItem.getPartitionValue()));
        sscc.setPrefixLength(Integer.toString(prefixLength.getValue()));
        sscc.setCompanyPrefix(companyPrefix);
        sscc.setExtensionDigit(Integer.toString(extensionDigit.getValue()));
        sscc.setSerial(serial);
        sscc.setCheckDigit(Integer.toString(getCheckDigit()));
        sscc.setEpcPureIdentityURI(String.format("urn:epc:id:sscc:%s.%s%s", companyPrefix, extensionDigit.getValue(), serial));
        sscc.setEpcTagURI(String.format("urn:epc:tag:sscc-%s:%s.%s.%s%s", tagSize.getValue(), filterValue.getValue(), companyPrefix, extensionDigit.getValue(), serial));
        sscc.setEpcRawURI(String.format("urn:epc:raw:%s.x%s", tagSize.getValue(), outputHex));
        sscc.setBinary(outputBin);
        sscc.setRfidTag(outputHex);
    }

    private Integer getCheckDigit() {
        String value = new StringBuilder()
            .append(extensionDigit.getValue())
            .append(companyPrefix)
            .append(serial)
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
        bin.append(Converter.decToBin(tableItem.getPartitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(companyPrefix), tableItem.getM()));
        bin.append(Converter.decToBin(extensionDigit.getValue() + serial, tableItem.getN()));
        bin.append(Converter.decToBin(RESERVED, 24));

        return bin.toString();
    }

    public SSCC getSSCC() {
        return sscc;
    }

    public String getRfidTag() {
        return Converter.binToHex(getBinary());
    }

    private void validateExtensionDigitAndSerial() {
        StringBuilder value = new StringBuilder()
            .append(extensionDigit.getValue())
            .append(serial);

        if (value.length() != tableItem.getDigits()) {
            throw new IllegalArgumentException(String.format("Concatenation between Extension Digit \"%d\" and Serial \"%s\" has %d length and should have %d length",
                extensionDigit.getValue(), serial, value.length(), tableItem.getDigits()));
        }
    }

    private void validateCompanyPrefix() {
        final Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    public interface ChoiceStep {
        ExtensionDigiStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEPCTagURI(final String epcTagURI);

        TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface ExtensionDigiStep {
        SerialStep withExtensionDigit(final SSCCExtensionDigit extensionDigit);
    }

    public interface SerialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final SSCCTagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final SSCCFilterValue filterValue);
    }

    public interface BuildStep {
        ParseSSCC build();
    }

    private static class Steps implements ChoiceStep, ExtensionDigiStep, SerialStep, TagSizeStep, FilterValueStep, BuildStep {

        private SSCCExtensionDigit extensionDigit;
        private String companyPrefix;
        private SSCCTagSize tagSize;
        private SSCCFilterValue filterValue;
        private String serial;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public ParseSSCC build() {
            return new ParseSSCC(this);
        }

        @Override
        public SerialStep withExtensionDigit(final SSCCExtensionDigit extensionDigit) {
            this.extensionDigit = extensionDigit;
            return this;
        }

        @Override
        public ExtensionDigiStep withCompanyPrefix(final String companyPrefix) {
            this.companyPrefix = companyPrefix;
            return this;
        }

        @Override
        public TagSizeStep withSerial(final String serial) {
            this.serial = serial;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final SSCCTagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public BuildStep withFilterValue(final SSCCFilterValue filterValue) {
            this.filterValue = filterValue;
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
