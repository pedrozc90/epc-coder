package com.pedrozc90.epcs.schemes.gsrn;

import com.pedrozc90.epcs.exception.EpcParseException;
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

public class GSRNParser {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("^(urn:epc:tag:gsrn-)(96):([0-7])\\.(\\d+)\\.(\\d+)$");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("^(urn:epc:id:gsrn):(\\d+)\\.(\\d+)$");

    private static final GSRNPartitionTable partitionTable = new GSRNPartitionTable();
    private static final Integer RESERVED = 0; // 24 zero bits

    private final GSRN gsrn;

    public static ChoiceStep Builder() throws Exception {
        return new Steps();
    }

    private GSRNParser(final Steps steps) throws EpcParseException {
        final ParsedData data = parse(steps);
        gsrn = toGSRN(data);
    }

    private ParsedData parse(final Steps steps) throws EpcParseException {
        if (steps.rfidTag != null) {
            return decodeRFIDTag(steps.rfidTag);
        } else if (steps.epcTagURI != null) {
            return decodeEpcTagURI(steps.epcTagURI);
        } else if (steps.epcPureIdentityURI != null) {
            return decodeEpcPureIdentityURI(steps.epcPureIdentityURI, steps.tagSize, steps.filterValue);
        }
        return encode(steps);
    }

    private ParsedData decodeRFIDTag(final String rfidTag) {
        final String inputBin = Converter.hexToBin(rfidTag);

        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        final GSRNHeader header = GSRNHeader.of(headerBin);
        final GSRNTagSize tagSize = GSRNTagSize.of(header.getTagSize());

        final TableItem tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));
        final PrefixLength prefixLength = PrefixLength.of(tableItem.l());

        final String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        final GSRNFilterValue filterValue = GSRNFilterValue.of(Integer.parseInt(filterDec));

        final String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        final String companyPrefixDec = Converter.binToDec(companyPrefixBin);
        final String companyPrefix = Converter.strZero(companyPrefixDec, tableItem.l());

        final String serialWithExtensionBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());

        final String serviceReference = Converter.strZero(Converter.binToDec(serialWithExtensionBin), tableItem.digits());

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, serviceReference);
    }

    private ParsedData decodeEpcTagURI(final String epcTagURI) {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Tag URI is invalid");
        }

        final GSRNTagSize tagSize = GSRNTagSize.of(Integer.parseInt(matcher.group(2)));
        final GSRNFilterValue filterValue = GSRNFilterValue.of(Integer.parseInt(matcher.group(3)));
        final String companyPrefix = matcher.group(4);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String serviceReference = matcher.group(5);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateServiceReference(tableItem, serviceReference);

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, serviceReference);
    }

    private ParsedData decodeEpcPureIdentityURI(final String epcPureIdentityURI, final GSRNTagSize tagSize, final GSRNFilterValue filterValue) {
        if (tagSize == null) throw new IllegalArgumentException("tag size must not be null");
        if (filterValue == null) throw new IllegalArgumentException("filter value must not be null");

        final Matcher matcher = PURE_IDENTITY_URI_PATTERN.matcher(epcPureIdentityURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Pure Identity is invalid");
        }

        final String companyPrefix = matcher.group(2);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String serviceReference = matcher.group(3);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateServiceReference(tableItem, serviceReference);

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, serviceReference);
    }

    private ParsedData encode(final Steps steps) {
        final PrefixLength prefixLength = PrefixLength.of(steps.companyPrefix.length());
        validateCompanyPrefix(prefixLength);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateServiceReference(tableItem, steps.serviceReference);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, prefixLength, steps.companyPrefix, steps.serviceReference);
    }

    private GSRN toGSRN(final ParsedData data) {
        final String outputBin = toBinary(data);
        final String outputHex = Converter.binToHex(outputBin);

        final Integer checkDigit = getCheckDigit(data.companyPrefix, data.serviceReference);

        final GSRN gsrn = new GSRN();
        // gsrn.setEpcScheme("gsrn");
        gsrn.setApplicationIdentifier("AI 8018");
        gsrn.setTagSize(Integer.toString(data.tagSize.getValue()));
        gsrn.setFilterValue(Integer.toString(data.filterValue.getValue()));
        gsrn.setPartitionValue(Integer.toString(data.tableItem.partitionValue()));
        gsrn.setPrefixLength(Integer.toString(data.prefixLength.getValue()));
        gsrn.setCompanyPrefix(data.companyPrefix);
        gsrn.setServiceReference(data.serviceReference);
        gsrn.setCheckDigit(Integer.toString(checkDigit));
        gsrn.setEpcPureIdentityURI("urn:epc:id:gsrn:%s.%s".formatted(data.companyPrefix, data.serviceReference));
        gsrn.setEpcTagURI("urn:epc:tag:gsrn-%s:%s.%s.%s".formatted(data.tagSize.getValue(), data.filterValue.getValue(), data.companyPrefix, data.serviceReference));
        gsrn.setEpcRawURI("urn:epc:raw:%s.x%s".formatted(data.tagSize.getValue(), outputHex));
        gsrn.setBinary(outputBin);
        gsrn.setRfidTag(outputHex);
        return gsrn;
    }

    private Integer getCheckDigit(final String companyPrefix, final String serviceReference) {
        final String value = companyPrefix + serviceReference;

        final Integer d18 = (10 - ((3
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

    private String toBinary(final ParsedData data) {
        final StringBuilder bin = new StringBuilder();

        bin.append(Converter.decToBin(data.tagSize.getHeader(), 8));
        bin.append(Converter.decToBin(data.filterValue.getValue(), 3));
        bin.append(Converter.decToBin(data.tableItem.partitionValue(), 3));
        bin.append(Converter.decToBin(Integer.parseInt(data.companyPrefix), data.tableItem.m()));
        bin.append(Converter.decToBin(Integer.parseInt(data.serviceReference), data.tableItem.n()));
        bin.append(Converter.decToBin(RESERVED, 24));

        return bin.toString();
    }

    /* --- Validations --- */
    private void validateCompanyPrefix(final PrefixLength prefixLength) {
        final Optional<PrefixLength> optPrefixLength = Optional.ofNullable(prefixLength);
        if (optPrefixLength.isEmpty()) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
    }

    private void validateServiceReference(final TableItem tableItem, final String serviceReference) {
        final int length = serviceReference.length();
        if (length != tableItem.digits()) {
            throw new IllegalArgumentException("Service Reference \"%s\" has %d length and should have %d length".formatted(serviceReference, length, tableItem.digits()));
        }
    }

    /* --- Objects --- */
    public record ParsedData(
        TableItem tableItem,
        GSRNTagSize tagSize,
        GSRNFilterValue filterValue,
        PrefixLength prefixLength,
        String companyPrefix,
        String serviceReference
    ) {
        // ignore
    }

    /* --- Builder --- */
    public interface ChoiceStep {
        ServiceReferenceStep withCompanyPrefix(final String companyPrefix);

        BuildStep withRFIDTag(final String rfidTag);

        BuildStep withEpcTagURI(final String epcTagURI);

        TagSizeStep withEpcPureIdentityURI(final String epcPureIdentityURI);
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
        GSRN build() throws EpcParseException;
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
        public GSRN build() throws EpcParseException {
            final GSRNParser parser = new GSRNParser(this);
            return parser.gsrn;
        }

    }

}
