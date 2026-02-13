package com.pedrozc90.epcs.schemes.cpi;

import com.pedrozc90.epcs.exception.EpcParseException;
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

public class CPIParser {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("^(urn:epc:tag:cpi-)(96|var):([0-7])\\.([0-9]+)\\.(.+)\\.([0-9]+)$");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("^(urn:epc:id:cpi):([0-9]+)\\.(.+)\\.([0-9]+)$");

    private final CPI cpi;

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private CPIParser(final Steps steps) {
        final ParsedData data = parse(steps);
        cpi = toCPI(data);
    }

    private ParsedData parse(final Steps steps) {
        if (steps.rfidTag != null) {
            return decodeRFIDTag(steps.rfidTag);
        } else if (steps.epcTagURI != null) {
            return decodeEpcTagURI(steps.epcTagURI);
        } else if (steps.epcPureIdentityURI != null) {
            return decodeEpcPureIdentifyURI(steps.epcPureIdentityURI, steps.tagSize, steps.filterValue);
        }
        return encode(steps);
    }

    private ParsedData decodeRFIDTag(String rfidTag) {
        final String inputBin = Converter.hexToBin(rfidTag);

        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        final CPIHeader header = CPIHeader.of(headerBin);
        final CPITagSize tagSize = CPITagSize.of(header.getTagSize());
        final CPIPartitionTable partitionTable = CPIPartitionTable.getInstance(tagSize);
        final TableItem tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));

        final PrefixLength prefixLength = PrefixLength.of(tableItem.l());

        final String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        final CPIFilterValue filterValue = CPIFilterValue.of(Integer.parseInt(filterDec));

        String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        String componentPartReferenceBin = null;
        String componentPartReference = null;
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

        final String companyPrefixDec = Converter.binToDec(companyPrefixBin);
        final String serial = Converter.binToDec(serialBin);
        final String companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l()); // strzero aqui

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, componentPartReference, serial);
    }

    private ParsedData decodeEpcTagURI(final String epcTagURI) {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Tag URI is invalid");
        }

        CPITagSize tagSize;
        final String size = matcher.group(2);
        if (size.equals("var")) {
            tagSize = CPITagSize.of(0);
        } else {
            tagSize = CPITagSize.of(Integer.parseInt(size));
        }

        final CPIFilterValue filterValue = CPIFilterValue.of(Integer.parseInt(matcher.group(3)));
        final String companyPrefix = matcher.group(4);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String componentPartReference = matcher.group(5);
        final String serial = matcher.group(6);

        final CPIPartitionTable partitionTable = CPIPartitionTable.getInstance(tagSize);
        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateComponentPartReference(tableItem, tagSize, componentPartReference);

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, componentPartReference, serial);
    }

    private ParsedData decodeEpcPureIdentifyURI(final String epcPureIdentityURI, final CPITagSize tagSize, final CPIFilterValue filterValue) {
        if (tagSize == null) throw new IllegalArgumentException("tag size must not be null");
        if (filterValue == null) throw new IllegalArgumentException("filter value must not be null");

        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Pure Identity is invalid");
        }

        final String companyPrefix = matcher.group(2);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String componentPartReference = matcher.group(3);
        final String serial = matcher.group(4);

        final CPIPartitionTable partitionTable = CPIPartitionTable.getInstance(tagSize);
        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateComponentPartReference(tableItem, tagSize, componentPartReference);

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, componentPartReference, serial);
    }

    private ParsedData encode(final Steps steps) {
        final CPIPartitionTable partitionTable = CPIPartitionTable.getInstance(steps.tagSize);
        final PrefixLength prefixLength = PrefixLength.of(steps.companyPrefix.length());

        validateCompanyPrefix(prefixLength);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateComponentPartReference(tableItem, steps.tagSize, steps.componentPartReference);

        validateSerial(steps.tagSize, steps.serial);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, prefixLength, steps.companyPrefix, steps.componentPartReference, steps.serial);
    }

    private BinaryResult toBinary(final ParsedData data) {
        final StringBuilder bin = new StringBuilder();

        bin.append(Converter.decToBin(data.tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(data.filterValue.getValue(), 3));
        bin.append(Converter.decToBin(data.tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(data.companyPrefix), data.tableItem.m()));

        // variable
        if (data.tagSize.getValue() == 0) {
            // bin.append(Converter.StringToBinary(componentPartReference, 6));
            bin.append(Converter.to6BitsBinary(data.componentPartReference));
            bin.append("000000");
        } else if (data.tagSize.getValue() == 96) {
            bin.append(Converter.decToBin(Integer.parseInt(data.componentPartReference), data.tableItem.n()));
        }

        bin.append(Converter.decToBin(data.serial, data.tagSize.getSerialBitCount()));

        // remainder = (int) (Math.ceil((bin.length() / 16.0)) * 16) - bin.length();
        final int remainder = Converter.remainder(bin.length());
        if (remainder > 0) {
            bin.append(Converter.fill("0", remainder));
        }

        return new BinaryResult(bin.toString(), remainder);
    }

    private CPI toCPI(final ParsedData data) {
        final BinaryResult result = toBinary(data);

        final String outputBin = result.binary;
        final String outputHex = Converter.binToHex(outputBin);

        final int remainder = result.remainder;

        final String tagSize = (data.tagSize.getValue() == 0) ? "var" : Integer.toString(data.tagSize.getValue());

        final CPI cpi = new CPI();
        // cpi.setEpcScheme("cpi");
        cpi.setApplicationIdentifier("AI 8010 + AI 8011");
        cpi.setTagSize(tagSize);
        cpi.setFilterValue(Integer.toString(data.filterValue.getValue()));
        cpi.setPartitionValue(Integer.toString(data.tableItem.partitionValue()));
        cpi.setPrefixLength(Integer.toString(data.prefixLength.getValue()));
        cpi.setCompanyPrefix(data.companyPrefix);
        cpi.setComponentPartReference(data.componentPartReference);
        cpi.setSerial(data.serial);
        cpi.setEpcPureIdentityURI("urn:epc:id:cpi:%s.%s.%s".formatted(data.companyPrefix, data.componentPartReference, data.serial));
        cpi.setEpcTagURI("urn:epc:tag:cpi-%s:%s.%s.%s.%s".formatted(tagSize, data.filterValue.getValue(), data.companyPrefix, data.componentPartReference, data.serial));
        cpi.setEpcRawURI("urn:epc:raw:%s.x%s".formatted(outputBin.length(), outputHex));
        cpi.setBinary(outputBin);
        cpi.setRfidTag(outputHex);
        return cpi;
    }

    /* --- Validation --- */
    private void validateCompanyPrefix(final PrefixLength prefixLength) {
        final Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    private void validateComponentPartReference(final TableItem tableItem, final CPITagSize tagSize, final String componentPartReference) {
        if (componentPartReference.length() > tableItem.digits()) {
            throw new IllegalArgumentException("Component/Part Reference is out of range");
        }

        switch (tagSize) {
            case BITS_96 -> {
                // if (Converter.isNotNumeric(componentPartReference)) {
                //     throw new IllegalArgumentException("Component/Part Reference is allowed with numerical only");
                // }

                if (!componentPartReference.matches("\\d+")) {
                    throw new IllegalArgumentException("CPI-96 Component/Part Reference must be numeric, got: '%s'".formatted(componentPartReference));
                }
                if (componentPartReference.startsWith("0")) {
                    throw new IllegalArgumentException("CPI-96 Component/Part Reference with leading zeros is not allowed");
                }
            }
            case BITS_VARIABLE -> {
                // no validation
            }
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(tagSize));
        }
    }

    private void validateSerial(final CPITagSize tagSize, final String serial) {
        switch (tagSize) {
            case BITS_96 -> {
                if (!serial.matches("\\d+")) {
                    throw new IllegalArgumentException("CPI-96 Serial must be numeric, got: '%s'".formatted(serial));
                }
                if (serial.startsWith("0")) {
                    throw new IllegalArgumentException("CPI-96 Serial with leading zeros is not allowed");
                }
                if (Long.parseLong(serial) > tagSize.getSerialMaxValue()) {
                    throw new IllegalArgumentException("CPI-96 Serial value is out of range. Should be less than or equal %d".formatted(tagSize.getSerialMaxValue()));
                }
            }
            case BITS_VARIABLE -> {
                if (serial.startsWith("0")) {
                    throw new IllegalArgumentException("CPI-var Serial with leading zeros is not allowed");
                }
                if (serial.length() > tagSize.getSerialMaxLength()) {
                    throw new IllegalArgumentException("CPI-var Serial value is out of range. Should be up to %d alphanumeric characters".formatted(tagSize.getSerialMaxLength()));
                }
            }
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(tagSize));
        }
    }

    /* --- Objects --- */
    private record ParsedData(
        TableItem tableItem,
        CPITagSize tagSize,
        CPIFilterValue filterValue,
        PrefixLength prefixLength,
        String companyPrefix,
        String componentPartReference,
        String serial
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
        ComponentPartReferenceStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEpcTagURI(final String epcTagURI);

        TagSizeStep withEpcPureIdentityURI(final String epcPureIdentityURI);
    }

    public interface ComponentPartReferenceStep {
        SerialStep withComponentPartReference(final String componentPartReference);
    }

    public interface SerialStep {
        TagSizeStep withSerial(final String serial);
    }

    public interface TagSizeStep {
        FilterValueStep withTagSize(final CPITagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final CPIFilterValue filterValue);
    }

    public interface BuildStep {
        CPI build() throws EpcParseException;
    }

    private static class Steps implements ChoiceStep, ComponentPartReferenceStep, SerialStep, TagSizeStep, FilterValueStep, BuildStep {

        private String companyPrefix;
        private CPITagSize tagSize;
        private CPIFilterValue filterValue;
        private String componentPartReference;
        private String serial;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

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
        public SerialStep withComponentPartReference(final String componentPartReference) {
            this.componentPartReference = componentPartReference;
            return this;
        }

        @Override
        public ComponentPartReferenceStep withCompanyPrefix(final String companyPrefix) {
            this.companyPrefix = companyPrefix;
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
        public CPI build() throws EpcParseException {
            final CPIParser parser = new CPIParser(this);
            return parser.cpi;
        }

    }

}
