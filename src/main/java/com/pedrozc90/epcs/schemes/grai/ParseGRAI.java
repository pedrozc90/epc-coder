package com.pedrozc90.epcs.schemes.grai;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.grai.enums.GRAIFilterValue;
import com.pedrozc90.epcs.schemes.grai.enums.GRAIHeader;
import com.pedrozc90.epcs.schemes.grai.enums.GRAITagSize;
import com.pedrozc90.epcs.schemes.grai.objects.GRAI;
import com.pedrozc90.epcs.schemes.grai.partitionTable.GRAIPartitionTable;
import com.pedrozc90.epcs.utils.Converter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseGRAI {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("(urn:epc:tag:grai-)(96|170):([0-7])\\.(\\d+)\\.(\\d+)\\.(\\w+)");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("(urn:epc:id:grai):(\\d+)\\.(\\d+)\\.(\\w+)");

    private static final GRAIPartitionTable partitionTable = new GRAIPartitionTable();

    private GRAI grai = new GRAI();
    private String companyPrefix;
    private PrefixLength prefixLength;
    private GRAITagSize tagSize;
    private GRAIFilterValue filterValue;
    private String assetType;
    private String serial;
    private String rfidTag;
    private String epcTagURI;
    private String epcPureIdentityURI;
    private TableItem tableItem;
    private int remainder;

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private ParseGRAI(final Steps steps) {
        this.companyPrefix = steps.companyPrefix;
        this.tagSize = steps.tagSize;
        this.filterValue = steps.filterValue;
        this.assetType = steps.assetType;
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

            tagSize = GRAITagSize.of(GRAIHeader.of(headerBin).getTagSize());
            tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

            String filterDec = Long.toString(Long.parseLong(filterBin, 2));
            String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
            String assetTypeBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
            String serialBin = inputBin.substring(14 + tableItem.m() + tableItem.n());

            String companyPrefixDec = Converter.binToDec(companyPrefixBin);
            String assetTypeDec = Converter.binToDec(assetTypeBin);

            assetType = Converter.strZero(assetTypeDec, tableItem.digits());

            if (tagSize.getSerialBitCount() == 112) {
                serialBin = Converter.convertBinToBit(serialBin, 7, 8);
                serial = Converter.binToString(serialBin);
            } else if (tagSize.getSerialBitCount() == 38) {
                serial = Converter.binToDec(serialBin);
            }

            companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l()); // strzero aqui
            filterValue = GRAIFilterValue.of(Integer.parseInt(filterDec));
            prefixLength = PrefixLength.of(tableItem.l());
        } else {
            if (optionalCompanyPrefix.isPresent()) {
                prefixLength = PrefixLength.of(companyPrefix.length());

                validateCompanyPrefix();

                tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

                validateAssetType();
                validateSerial();
            } else {
                if (optionalEpcTagURI.isPresent()) {
                    final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Tag URI is invalid");
                    }

                    tagSize = GRAITagSize.of(Integer.parseInt(matcher.group(2)));
                    filterValue = GRAIFilterValue.of(Integer.parseInt(matcher.group(3)));
                    companyPrefix = matcher.group(4);
                    prefixLength = PrefixLength.of(matcher.group(4).length());
                    assetType = matcher.group(5);
                    serial = matcher.group(6);
                } else if (optionalEpcPureIdentityURI.isPresent()) {
                    final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("EPC Pure Identity is invalid");
                    }

                    companyPrefix = matcher.group(2);
                    prefixLength = PrefixLength.of(matcher.group(2).length());
                    assetType = matcher.group(3);
                    serial = matcher.group(4);
                }
            }

            tableItem = partitionTable.getPartitionByL(prefixLength.getValue());
        }

        final String outputBin = getBinary();
        final String outputHex = Converter.binToHex(outputBin);

        // grai.setEpcScheme("grai");
        grai.setApplicationIdentifier("AI 8003");
        grai.setTagSize(Integer.toString(tagSize.getValue()));
        grai.setFilterValue(Integer.toString(filterValue.getValue()));
        grai.setPartitionValue(Integer.toString(tableItem.partitionValue()));
        grai.setPrefixLength(Integer.toString(prefixLength.getValue()));
        grai.setCompanyPrefix(companyPrefix);
        grai.setAssetType(assetType);
        grai.setSerial(serial);
        grai.setCheckDigit(Integer.toString(getCheckDigit()));
        grai.setEpcPureIdentityURI(String.format("urn:epc:id:grai:%s.%s.%s", companyPrefix, assetType, serial));
        grai.setEpcTagURI(String.format("urn:epc:tag:grai-%s:%s.%s.%s.%s", tagSize.getValue(), filterValue.getValue(), companyPrefix, assetType, serial));
        grai.setEpcRawURI(String.format("urn:epc:raw:%s.x%s", tagSize.getValue() + remainder, outputHex));
        grai.setBinary(outputBin);
        grai.setRfidTag(outputHex);
    }

    private String getBinary() {
        final StringBuilder bin = new StringBuilder();

        // remainder = (int) (Math.ceil((tagSize.getValue() / 16.0)) * 16) - tagSize.getValue();
        remainder = Converter.remainder(tagSize.getValue());

        bin.append(Converter.decToBin(tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(filterValue.getValue(), 3));
        bin.append(Converter.decToBin(tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(companyPrefix), tableItem.m()));
        bin.append(Converter.decToBin(Integer.parseInt(assetType), tableItem.n()));

        if (tagSize.getValue() == 170) {
            bin.append(Converter.fill(Converter.StringToBinary(serial, 7), tagSize.getSerialBitCount() + remainder));
        } else if (tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(serial, tagSize.getSerialBitCount() + remainder));
        }

        return bin.toString();
    }

    private Integer getCheckDigit() {
        final String value = new StringBuilder()
            .append(companyPrefix)
            .append(assetType)
            .toString();

        Integer d13 = (10 - ((3
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

    public GRAI getGRAI() {
        return grai;
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

    private void validateAssetType() {
        if (assetType.length() != tableItem.digits()) {
            throw new IllegalArgumentException("Asset Type \"%s\" has %d length and should have %d length".formatted(assetType, assetType.length(), tableItem.digits()));
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
        assetTypeStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEPCTagURI(final String epcTagURI);

        TagSizeStep withEPCPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface assetTypeStep {
        serialStep withAssetType(final String assetType);
    }

    public interface serialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final GRAITagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final GRAIFilterValue filterValue);
    }

    public interface BuildStep {
        ParseGRAI build();
    }

    private static class Steps implements ChoiceStep, assetTypeStep, serialStep, TagSizeStep, FilterValueStep, BuildStep {

        private String companyPrefix;
        private GRAITagSize tagSize;
        private GRAIFilterValue filterValue;
        private String assetType;
        private String serial;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public ParseGRAI build() {
            return new ParseGRAI(this);
        }

        @Override
        public BuildStep withFilterValue(final GRAIFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final GRAITagSize tagSize) {
            this.tagSize = tagSize;
            return this;
        }

        @Override
        public TagSizeStep withSerial(final String serial) {
            this.serial = serial;
            return this;
        }

        @Override
        public serialStep withAssetType(final String assetType) {
            this.assetType = assetType;
            return this;
        }

        @Override
        public assetTypeStep withCompanyPrefix(final String companyPrefix) {
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
