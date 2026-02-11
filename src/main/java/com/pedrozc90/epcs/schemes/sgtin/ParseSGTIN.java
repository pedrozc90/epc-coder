package com.pedrozc90.epcs.schemes.sgtin;

import com.pedrozc90.epcs.exception.EPCParseException;
import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINExtensionDigit;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINFilterValue;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINHeader;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINTagSize;
import com.pedrozc90.epcs.schemes.sgtin.objects.SGTIN;
import com.pedrozc90.epcs.schemes.sgtin.partitionTable.SGTINPartitionTable;
import com.pedrozc90.epcs.utils.Converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseSGTIN  {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("(urn:epc:tag:sgtin-)(96|198):([0-7])\\.(\\d+)\\.([0-8])(\\d+)\\.(.+)");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("(urn:epc:id:sgtin):(\\d+)\\.([0-8])(\\d+)\\.(\\w+)");

    private static final SGTINPartitionTable partitionTable = new SGTINPartitionTable();

    private final SGTIN sgtin = new SGTIN();
    private final String rfidTag;
    private final String epcTagURI;
    private final String epcPureIdentityURI;

    private SGTINExtensionDigit extensionDigit;
    private String companyPrefix;
    private PrefixLength prefixLength;
    private SGTINTagSize tagSize;
    private SGTINFilterValue filterValue;
    private String itemReference;
    private String serial;
    private TableItem tableItem;
    private int remainder;

    public static ChoiceStep Builder() {
        return new Steps();
    }

    private ParseSGTIN(final Steps steps) throws EPCParseException {
        this.extensionDigit = steps.extensionDigit;
        this.companyPrefix = steps.companyPrefix;
        this.tagSize = steps.tagSize;
        this.filterValue = steps.filterValue;
        this.itemReference = steps.itemReference;
        this.serial = steps.serial;
        this.rfidTag = steps.rfidTag;
        this.epcTagURI = steps.epcTagURI;
        this.epcPureIdentityURI = steps.epcPureIdentityURI;
        parse();
    }

    private void parseRfidTag() throws EPCParseException {
        final String inputBin = Converter.hexToBin(rfidTag);
        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        tagSize = SGTINTagSize.of(SGTINHeader.of(headerBin).getTagSize());
        tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

        String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        String itemReferenceWithExtensionBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());

        String serialBin = inputBin.substring(14 + tableItem.m() + tableItem.n())
            .substring(0, tagSize.getSerialBitCount());

        String companyPrefixDec = Converter.binToDec(companyPrefixBin);
        String itemReferenceWithExtensionDec = Converter.strZero(Converter.binToDec(itemReferenceWithExtensionBin), tableItem.digits());
        String extensionDec = itemReferenceWithExtensionDec.substring(0, 1);

        itemReference = itemReferenceWithExtensionDec.substring(1);

        if (tagSize.getSerialBitCount() == 140) {
            serialBin = Converter.convertBinToBit(serialBin, 7, 8);
            serial = Converter.binToString(serialBin);
        } else if (tagSize.getSerialBitCount() == 38) {
            serial = Converter.binToDec(serialBin);
        }

        companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l());
        extensionDigit = SGTINExtensionDigit.of(Integer.parseInt(extensionDec));
        filterValue = SGTINFilterValue.of(Integer.parseInt(filterDec));
        prefixLength = PrefixLength.of(tableItem.l());
    }

    private void parseCompanyPrefix() throws EPCParseException {
        prefixLength = PrefixLength.of(companyPrefix.length());

        validateCompanyPrefix();

        tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateExtensionDigitAndItemReference();
        validateSerial();
    }

    private void parseEPCTagURI() throws EPCParseException {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new EPCParseException("EPC Tag URI is invalid");
        }

        tagSize = SGTINTagSize.of(Integer.parseInt(matcher.group(2)));
        filterValue = SGTINFilterValue.of(Integer.parseInt(matcher.group(3)));
        companyPrefix = matcher.group(4);
        prefixLength = PrefixLength.of(matcher.group(4).length());
        extensionDigit = SGTINExtensionDigit.of(Integer.parseInt(matcher.group(5)));
        itemReference = matcher.group(6);
        serial = matcher.group(7);
    }

    private void parseEPCPureIdentityURI() throws EPCParseException {
        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new EPCParseException("EPC Pure Identity is invalid");
        }

        companyPrefix = matcher.group(2);
        prefixLength = PrefixLength.of(matcher.group(2).length());
        extensionDigit = SGTINExtensionDigit.of(Integer.parseInt(matcher.group(3)));
        itemReference = matcher.group(4);
        serial = matcher.group(5);
    }

    private void parse() throws EPCParseException {
        if (rfidTag != null) {
            parseRfidTag();
        } else if (extensionDigit != null) {
            parseCompanyPrefix();
        } else {
            if (epcTagURI != null) {
                parseEPCTagURI();
            } else if (epcPureIdentityURI != null) {
                parseEPCPureIdentityURI();
            }

            tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
        }

        final String outputBin = getBinary();
        final String outputHex = Converter.binToHex(outputBin);

        // sgtin.setEpcScheme("sgtin");
        sgtin.setApplicationIdentifier("AI 414 + AI 254");
        sgtin.setTagSize(Integer.toString(tagSize.getValue()));
        sgtin.setFilterValue(Integer.toString(filterValue.getValue()));
        sgtin.setPartitionValue(Integer.toString(tableItem.partitionValue()));
        sgtin.setPrefixLength(Integer.toString(prefixLength.getValue()));
        sgtin.setCompanyPrefix(companyPrefix);
        sgtin.setItemReference(itemReference);
        sgtin.setExtensionDigit(Integer.toString(extensionDigit.getValue()));
        sgtin.setSerial(serial);
        sgtin.setCheckDigit(Integer.toString(getCheckDigit()));
        sgtin.setEpcPureIdentityURI("urn:epc:id:sgtin:%s.%s%s.%s".formatted(companyPrefix, extensionDigit.getValue(), itemReference, serial));
        sgtin.setEpcTagURI("urn:epc:tag:sgtin-%s:%s.%s.%s%s.%s".formatted(tagSize.getValue(), filterValue.getValue(), companyPrefix, extensionDigit.getValue(), itemReference, serial));
        sgtin.setEpcRawURI("urn:epc:raw:%s.x%s".formatted(tagSize.getValue() + remainder, outputHex));
        sgtin.setBinary(outputBin);
        sgtin.setRfidTag(outputHex);
    }

    private String getBinary() {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        remainder = Converter.remainder(tagSize.getValue());

        bin.append(Converter.decToBin(tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(filterValue.getValue(), 3));
        bin.append(Converter.decToBin(tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(companyPrefix, tableItem.m()));
        bin.append(Converter.decToBin(Integer.parseInt(extensionDigit.getValue() + itemReference), tableItem.n()));

        if (tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(serial, tagSize.getSerialBitCount() + remainder));
        } else if (tagSize.getValue() == 198) {
            final String serialBin = Converter.StringToBinary(serial, 7);
            bin.append(Converter.fill(serialBin, tagSize.getSerialBitCount() + remainder));
        }

        return bin.toString();
    }

    private Integer getCheckDigit() {
        final String value = extensionDigit.getValue() + companyPrefix + itemReference;

        return (10 - ((3
            * (Character.getNumericValue(value.charAt(0)) + Character.getNumericValue(value.charAt(2))
            + Character.getNumericValue(value.charAt(4))
            + Character.getNumericValue(value.charAt(6)) + Character.getNumericValue(value.charAt(8))
            + Character.getNumericValue(value.charAt(10)) + Character.getNumericValue(value.charAt(12)))
            + (Character.getNumericValue(value.charAt(1)) + Character.getNumericValue(value.charAt(3))
            + Character.getNumericValue(value.charAt(5)) + Character.getNumericValue(value.charAt(7))
            + Character.getNumericValue(value.charAt(9)) + Character.getNumericValue(value.charAt(11))))
            % 10)) % 10;
    }

    public SGTIN getSGTIN() {
        return sgtin;
    }

    public String getRfidTag() {
        return Converter.binToHex(getBinary());
    }

    private void validateExtensionDigitAndItemReference() throws EPCParseException {
        final StringBuilder value = new StringBuilder()
            .append(extensionDigit.getValue())
            .append(itemReference);

        if (value.length() != tableItem.digits()) {
            final String message = "Concatenation between Extension Digit \"%d\" and Item Reference \"%s\" has %d length and should have %d length";
            throw new EPCParseException(String.format(message, extensionDigit.getValue(), itemReference, value.length(), tableItem.digits()));
        }
    }

    private void validateCompanyPrefix() throws EPCParseException {
        if (prefixLength == null) {
            throw new EPCParseException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    private void validateSerial() throws EPCParseException {
        if (tagSize.getValue() == 198) {
            if (serial.length() > tagSize.getSerialMaxLength()) {
                throw new EPCParseException("Serial value is out of range. Should be up to 20 alphanumeric characters");
            }
        } else if (tagSize.getValue() == 96) {
            if (Long.parseLong(serial) > tagSize.getSerialMaxValue()) {
                throw new EPCParseException("Serial value is out of range. Should be less than or equal 274,877,906,943");
            }
            if (serial.startsWith("0")) {
                throw new EPCParseException("Serial with leading zeros is not allowed");
            }
        }
    }

    public interface ChoiceStep {
        ExtensionDigitStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEPCTagURI(final String epcTagURI);

        TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface ExtensionDigitStep {
        ItemReferenceStep withExtensionDigit(final SGTINExtensionDigit extensionDigit);
    }

    public interface ItemReferenceStep {
        SerialStep withItemReference(final String itemReference);
    }

    public interface SerialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final SGTINTagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final SGTINFilterValue filterValue);
    }

    public interface BuildStep {
        ParseSGTIN build() throws EPCParseException;
    }

    private static class Steps implements ChoiceStep, ExtensionDigitStep, ItemReferenceStep, SerialStep, TagSizeStep, FilterValueStep, BuildStep {

        private SGTINExtensionDigit extensionDigit;
        private String companyPrefix;
        private SGTINTagSize tagSize;
        private SGTINFilterValue filterValue;
        private String itemReference;
        private String serial;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public ParseSGTIN build() throws EPCParseException {
            return new ParseSGTIN(this);
        }

        @Override
        public ItemReferenceStep withExtensionDigit(final SGTINExtensionDigit extensionDigit) {
            this.extensionDigit = extensionDigit;
            return this;
        }

        @Override
        public SerialStep withItemReference(final String itemReference) {
            this.itemReference = itemReference;
            return this;
        }

        @Override
        public TagSizeStep withSerial(final String serial) {
            this.serial = serial;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final SGTINTagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public BuildStep withFilterValue(final SGTINFilterValue filterValue) {
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

        @Override
        public ExtensionDigitStep withCompanyPrefix(final String companyPrefix) {
            this.companyPrefix = companyPrefix;
            return this;
        }

    }

}
