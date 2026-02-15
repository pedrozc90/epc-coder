package com.pedrozc90.epcs.schemes.gsrnp;

import com.pedrozc90.epcs.exception.EpcParseException;
import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PrefixLength;
import com.pedrozc90.epcs.schemes.gsrnp.enums.GSRNPFilterValue;
import com.pedrozc90.epcs.schemes.gsrnp.enums.GSRNPHeader;
import com.pedrozc90.epcs.schemes.gsrnp.enums.GSRNPTagSize;
import com.pedrozc90.epcs.schemes.gsrnp.objects.GSRNP;
import com.pedrozc90.epcs.schemes.gsrnp.partitionTable.GSRNPPartitionTable;
import com.pedrozc90.epcs.utils.BinaryUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GSRNPParser {

    private static final Pattern TAG_URI_PATTERN = Pattern.compile("^(urn:epc:tag:gsrnp-)(96):([0-7])\\.(\\d+)\\.(\\d+)$");
    private static final Pattern PURE_IDENTITY_URI_PATTERN = Pattern.compile("^(urn:epc:id:gsrnp):(\\d+)\\.(\\d+)$");

    private static final GSRNPPartitionTable partitionTable = new GSRNPPartitionTable();
    private static final Integer RESERVED = 0; // 24 zero bits

    private final GSRNP gsrnp;

    public static ChoiceStep Builder() {
        return new Steps();
    }

    private GSRNPParser(final Steps steps) throws EpcParseException {
        final ParsedData data = parse(steps);
        gsrnp = toGSRNP(data);
    }

    private ParsedData parse(final Steps steps) throws EpcParseException {
        if (steps.rfidTag != null) {
            return decodeRFIDTag(steps.rfidTag);
        } else if (steps.epcTagURI != null) {
            return decodeEpcTagURI(steps.epcTagURI);
        } else if (steps.epcPureIdentityURI != null) {
            return decodeEpcPureIdentityURI(steps.epcPureIdentityURI, steps.tagSize, steps.filterValue);
        }
        return parseCompanyPrefix(steps);
    }

    private ParsedData decodeRFIDTag(final String rfidTag) {
        final String inputBin = BinaryUtils.toBinary(rfidTag);

        final String headerBin = inputBin.substring(0, 8);
        final String filterBin = inputBin.substring(8, 11);
        final String partitionBin = inputBin.substring(11, 14);

        final GSRNPHeader header = GSRNPHeader.of(headerBin);
        final GSRNPTagSize tagSize = GSRNPTagSize.of(header.getTagSize());

        final TableItem tableItem = partitionTable.getPartitionByValue(Integer.parseInt(partitionBin, 2));
        final PrefixLength prefixLength = PrefixLength.of(tableItem.l());

        final String filterDec = Long.toString(Long.parseLong(filterBin, 2));
        final GSRNPFilterValue filterValue = GSRNPFilterValue.of(Integer.parseInt(filterDec));

        final String companyPrefixBin = inputBin.substring(14, 14 + tableItem.m());
        final String companyPrefix = BinaryUtils.decodeInteger(companyPrefixBin, tableItem.l());

        final String serialWithExtensionBin = inputBin.substring(14 + tableItem.m(), 14 + tableItem.m() + tableItem.n());
        final String serviceReference = BinaryUtils.decodeInteger(serialWithExtensionBin, tableItem.digits());

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, serviceReference);
    }

    private ParsedData decodeEpcTagURI(final String epcTagURI) {
        final Matcher matcher = TAG_URI_PATTERN.matcher(epcTagURI);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Epc Tag URI is invalid");
        }

        final GSRNPTagSize tagSize = GSRNPTagSize.of(Integer.parseInt(matcher.group(2)));
        final GSRNPFilterValue filterValue = GSRNPFilterValue.of(Integer.parseInt(matcher.group(3)));
        final String companyPrefix = matcher.group(4);
        final PrefixLength prefixLength = PrefixLength.of(companyPrefix.length());
        final String serviceReference = matcher.group(5);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateServiceReference(tableItem, serviceReference);

        return new ParsedData(tableItem, tagSize, filterValue, prefixLength, companyPrefix, serviceReference);
    }

    private ParsedData decodeEpcPureIdentityURI(final String epcPureIdentityURI, final GSRNPTagSize tagSize, final GSRNPFilterValue filterValue) {
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

    private ParsedData parseCompanyPrefix(final Steps steps) {
        final PrefixLength prefixLength = PrefixLength.of(steps.companyPrefix.length());

        validateCompanyPrefix(prefixLength);

        final TableItem tableItem = partitionTable.getPartitionByL(prefixLength.getValue());

        validateServiceReference(tableItem, steps.serviceReference);

        return new ParsedData(tableItem, steps.tagSize, steps.filterValue, prefixLength, steps.companyPrefix, steps.serviceReference);
    }

    private GSRNP toGSRNP(final ParsedData data) {
        final String outputBin = toBinary(data);
        final String outputHex = BinaryUtils.toHex(outputBin);

        final Integer checkDigit = getCheckDigit(data.companyPrefix, data.serviceReference);

        final GSRNP gsrnp = new GSRNP();
        // gsrnp.setEpcScheme("gsrnp");
        gsrnp.setApplicationIdentifier("AI 8017");
        gsrnp.setTagSize(Integer.toString(data.tagSize.getValue()));
        gsrnp.setFilterValue(Integer.toString(data.filterValue.getValue()));
        gsrnp.setPartitionValue(Integer.toString(data.tableItem.partitionValue()));
        gsrnp.setPrefixLength(Integer.toString(data.prefixLength.getValue()));
        gsrnp.setCompanyPrefix(data.companyPrefix);
        gsrnp.setServiceReference(data.serviceReference);
        gsrnp.setCheckDigit(Integer.toString(checkDigit));
        gsrnp.setEpcPureIdentityURI("urn:epc:id:gsrnp:%s.%s".formatted(data.companyPrefix, data.serviceReference));
        gsrnp.setEpcTagURI("urn:epc:tag:gsrnp-%s:%s.%s.%s".formatted(data.tagSize.getValue(), data.filterValue.getValue(), data.companyPrefix, data.serviceReference));
        gsrnp.setEpcRawURI("urn:epc:raw:%s.x%s".formatted(data.tagSize.getValue(), outputHex));
        gsrnp.setBinary(outputBin);
        gsrnp.setRfidTag(outputHex);
        return gsrnp;
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

        bin.append(BinaryUtils.encodeInteger(data.tagSize.getHeader(), 8));
        bin.append(BinaryUtils.encodeInteger(data.filterValue.getValue(), 3));
        bin.append(BinaryUtils.encodeInteger(data.tableItem.partitionValue(), 3));
        bin.append(BinaryUtils.encodeInteger(data.companyPrefix, data.tableItem.m()));
        bin.append(BinaryUtils.encodeInteger(data.serviceReference, data.tableItem.n()));
        bin.append(BinaryUtils.encodeInteger(RESERVED, 24));

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
    private record ParsedData(
        TableItem tableItem,
        GSRNPTagSize tagSize,
        GSRNPFilterValue filterValue,
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
        FilterValueStep withTagSize(final GSRNPTagSize tagSize);
    }

    public interface FilterValueStep {
        BuildStep withFilterValue(final GSRNPFilterValue filterValue);
    }

    public interface BuildStep {
        GSRNP build() throws EpcParseException;
    }

    private static class Steps implements ChoiceStep, ServiceReferenceStep, TagSizeStep, FilterValueStep, BuildStep {

        private String companyPrefix;
        private GSRNPTagSize tagSize;
        private GSRNPFilterValue filterValue;
        private String serviceReference;
        private String rfidTag;
        private String epcTagURI;
        private String epcPureIdentityURI;

        @Override
        public BuildStep withFilterValue(final GSRNPFilterValue filterValue) {
            this.filterValue = filterValue;
            return this;
        }

        @Override
        public FilterValueStep withTagSize(final GSRNPTagSize tagSize) {
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
        public GSRNP build() throws EpcParseException {
            final GSRNPParser parser = new GSRNPParser(this);
            return parser.gsrnp;
        }

    }

}
