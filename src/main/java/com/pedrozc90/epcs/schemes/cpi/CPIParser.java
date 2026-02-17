package com.pedrozc90.epcs.schemes.cpi;

import com.pedrozc90.epcs.exception.EpcParseException;
import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.EpcParser;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.cpi.enums.CPIFilterValue;
import com.pedrozc90.epcs.schemes.cpi.enums.CPIHeader;
import com.pedrozc90.epcs.schemes.cpi.enums.CPITagSize;
import com.pedrozc90.epcs.schemes.cpi.objects.CPI;
import com.pedrozc90.epcs.schemes.cpi.partitionTable.CPIPartitionTable;
import com.pedrozc90.epcs.utils.BinaryUtils;
import com.pedrozc90.epcs.utils.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CPIParser implements EpcParser<CPI> {

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

    private ParsedData decodeRFIDTag(final String rfidTag) {
        final String inputBin = BinaryUtils.toBinary(rfidTag);

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

        final String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        final String companyPrefix = BinaryUtils.decodeInteger(companyPrefixBin, tableItem.l());

        // cpi-96
        final DecodedData decoded = switch (tagSize) {
            case BITS_96 -> {
                final String componentPartReferenceBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
                final String componentPartReference = BinaryUtils.decodeInteger(componentPartReferenceBin);
                final String serialBin = inputBin.substring(14 + tableItem.m() + tableItem.n());
                yield new DecodedData(componentPartReferenceBin, componentPartReference, serialBin);
            }
            // cpi-var
            case BITS_VARIABLE -> {
                final int componentPartReferenceStart = 14 + tableItem.m();
                final String componentPartReferenceAndSerialBin = inputBin.substring(componentPartReferenceStart);

                // find the terminator "000000"
                final StringBuilder tmpBin = new StringBuilder();
                final List<String> parts = StringUtils.chunk(componentPartReferenceAndSerialBin, 6);
                int chunksRead = 0;
                for (String part : parts) {
                    if (part.equals("000000")) {
                        chunksRead++;
                        break;
                    }
                    tmpBin.append(part);
                    chunksRead++;
                }

                // componentPartReferenceBin = Converter.convertBinToBit(componentPartReferenceBin, 6, 8);
                // componentPartReference = Converter.binToString(componentPartReferenceBin);
                final String componentPartReferenceBin = tmpBin.toString();
                final String componentPartReference = BinaryUtils.decodeString(componentPartReferenceBin, 6);

                final int posSerial = componentPartReferenceStart + (chunksRead * 6);
                final String serialBin = inputBin.substring(posSerial, posSerial + tagSize.getSerialBitCount());

                yield new DecodedData(componentPartReferenceBin, componentPartReference, serialBin);
            }
        };

        final String serial = BinaryUtils.decodeInteger(decoded.serialBin);

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, decoded.componentPartReference, serial);
    }

    private ParsedData decodeEpcTagURI(final String epcTagURI) {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Tag URI is invalid");
        }

        final String size = matcher.group(2);
        final CPITagSize tagSize = switch (size) {
            case "96" -> CPITagSize.BITS_96;
            case "var" -> CPITagSize.BITS_VARIABLE;
            default -> throw new IllegalArgumentException("Unsupported tag size '%s'".formatted(size));
        };

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

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateComponentPartReference(tableItem, steps.tagSize, steps.componentPartReference);

        validateSerial(steps.tagSize, steps.serial);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, prefixLength, steps.companyPrefix, steps.componentPartReference, steps.serial);
    }

    private BinaryResult toBinary(final ParsedData data) {
        final StringBuilder bin = new StringBuilder();

        bin.append(BinaryUtils.encodeInteger(data.tagSize.getHeader(), 8));
        bin.append(BinaryUtils.encodeInteger(data.filterValue.getValue(), 3));
        bin.append(BinaryUtils.encodeInteger(data.tableItem.partitionValue(), 3));
        bin.append(BinaryUtils.encodeInteger(data.companyPrefix, data.tableItem.m()));

        // cpi-96
        switch (data.tagSize) {
            case BITS_96 -> {
                bin.append(BinaryUtils.encodeInteger(data.componentPartReference, data.tableItem.n()));
                bin.append(BinaryUtils.encodeInteger(data.serial, data.tagSize.getSerialBitCount()));
            }
            // cpi-var
            case BITS_VARIABLE -> {
                bin.append(BinaryUtils.encodeString(data.componentPartReference, 6 * data.componentPartReference.length(), 6));
                bin.append("000000");
                bin.append(BinaryUtils.encodeInteger(data.serial, data.tagSize.getSerialBitCount()));
            }
        }

        // Calculate remainder AFTER building the complete binary
        // remainder = (int) (Math.ceil((bin.length() / 16.0)) * 16) - bin.length();
        final int remainder = remainder(bin.length());
//        if (remainder > 0) {
//            bin.append(Converter.fill("0", remainder));
//        }
        final String binary = StringUtils.rightPad(bin.toString(), bin.length() + remainder, '0');

        return new BinaryResult(binary, remainder);
    }

    private CPI toCPI(final ParsedData data) {
        final BinaryResult result = toBinary(data);

        final String outputBin = result.binary;
        final String outputHex = BinaryUtils.toHex(outputBin);

        final int remainder = result.remainder;

        final String tagSize = (data.tagSize.getValue() == 0) ? "var" : Integer.toString(data.tagSize.getValue());

        return new CPI(
            // "cpi",
            // "AI 8010 + AI 8011",
            tagSize,
            Integer.toString(data.filterValue.getValue()),
            Integer.toString(data.tableItem.partitionValue()),
            Integer.toString(data.prefixLength.getValue()),
            data.companyPrefix,
            data.componentPartReference,
            data.serial,
            "urn:epc:id:cpi:%s.%s.%s".formatted(data.companyPrefix, data.componentPartReference, data.serial),
            "urn:epc:tag:cpi-%s:%s.%s.%s.%s".formatted(tagSize, data.filterValue.getValue(), data.companyPrefix, data.componentPartReference, data.serial),
            "urn:epc:raw:%s.x%s".formatted(outputBin.length(), outputHex),
            outputBin,
            outputHex
        );
    }

    /* --- Validation --- */
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

    private record DecodedData(
        String componentPartReferenceBin,
        String componentPartReference,
        String serialBin
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
