package com.pedrozc90.epcs.schemes.gsrn;

import com.pedrozc90.epcs.schemes.gsrn.enums.GSRNFilterValue;
import com.pedrozc90.epcs.schemes.gsrn.enums.GSRNTagSize;
import com.pedrozc90.epcs.schemes.gsrn.objects.GSRN;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParseGSRNTest {

    @Test
    public void encode() throws Exception {
        final GSRN result = ParseGSRN.Builder()
            .withCompanyPrefix("0614141")
            .withServiceReference("1234567890")
            .withTagSize(GSRNTagSize.BITS_96)
            .withFilterValue(GSRNFilterValue.RESERVED_3)
            .build()
            .getGSRN();

        assertNotNull(result);
        assertEquals("2D74257BF4499602D2000000", result.getRfidTag());
        assertEquals("urn:epc:tag:gsrn-96:3.0614141.1234567890", result.getEpcTagURI());
        assertEquals("urn:epc:id:gsrn:0614141.1234567890", result.getEpcPureIdentityURI());
        assertEquals("gsrn", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("3", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("1234567890", result.getServiceReference());
    }

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "2D74257BF4499602D2000000",
                "urn:epc:tag:gsrn-96:3.0614141.1234567890",
                "urn:epc:id:gsrn:0614141.1234567890",
                "gsrn",
                "96",
                "3",
                "7",
                "0614141",
                "1234567890",
                96
            ),
            Arguments.arguments(
                "2D76451FD4499602D2000000",
                "urn:epc:tag:gsrn-96:3.9521141.1234567890",
                "urn:epc:id:gsrn:9521141.1234567890",
                "gsrn",
                "96",
                "3",
                "7",
                "9521141",
                "1234567890",
                96
            )
        );
    }

    @ParameterizedTest(name = "[{index}] RFID Tag: {0}")
    @MethodSource("provideData")
    public void decode_RfidTag(
        final String expectedRfidTag,
        final String expectedEpcTagURI,
        final String expectedEpcPureIdentityURI,
        final String expectedEpcScheme,
        final String expectedTagSize,
        final String expectedFilterValue,
        final String expectedPrefixLength,
        final String expectedCompanyPrefix,
        final String expectedServiceReference,
        final Integer expectedBitCount
    ) throws Exception {
        final GSRN result = ParseGSRN.Builder()
            .withRFIDTag(expectedRfidTag)
            .build()
            .getGSRN();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedServiceReference, result.getServiceReference());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @ParameterizedTest(name = "[{index}] Epc Tag URI: {1}")
    @MethodSource("provideData")
    public void decode_EpcTagURI(
        final String expectedRfidTag,
        final String expectedEpcTagURI,
        final String expectedEpcPureIdentityURI,
        final String expectedEpcScheme,
        final String expectedTagSize,
        final String expectedFilterValue,
        final String expectedPrefixLength,
        final String expectedCompanyPrefix,
        final String expectedServiceReference,
        final Integer expectedBitCount
    ) throws Exception {
        final GSRN result = ParseGSRN.Builder()
            .withEPCTagURI(expectedEpcTagURI)
            .build()
            .getGSRN();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedServiceReference, result.getServiceReference());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @Test
    public void decode_EPCPureIdentityURI() throws Exception {
        final GSRN result = ParseGSRN.Builder()
            .withEPCPureIdentityURI("urn:epc:id:gsrn:0614141.1234567890")
            .withTagSize(GSRNTagSize.BITS_96)
            .withFilterValue(GSRNFilterValue.RESERVED_3)
            .build()
            .getGSRN();

        assertNotNull(result);
        assertEquals("2D74257BF4499602D2000000", result.getRfidTag());
        assertEquals("urn:epc:tag:gsrn-96:3.0614141.1234567890", result.getEpcTagURI());
        assertEquals("gsrn", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("3", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("1234567890", result.getServiceReference());
    }

}
