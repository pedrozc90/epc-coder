package com.pedrozc90.epcs.schemes.sgln;

import com.pedrozc90.epcs.schemes.sgln.enums.SGLNFilterValue;
import com.pedrozc90.epcs.schemes.sgln.enums.SGLNTagSize;
import com.pedrozc90.epcs.schemes.sgln.objects.SGLN;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParseSGLNTest {

    @Test
    public void encode() throws Exception {
        final SGLN result = ParseSGLN.Builder()
            .withCompanyPrefix("0614141")
            .withLocationReference("00001")
            .withExtension("1234")
            .withTagSize(SGLNTagSize.BITS_96)
            .withFilterValue(SGLNFilterValue.RESERVED_3)
            .build()
            .getSGLN();

        assertNotNull(result);
        assertEquals("3274257BF4000200000004D2", result.getRfidTag());
        assertEquals("urn:epc:tag:sgln-96:3.0614141.00001.1234", result.getEpcTagURI());
        assertEquals("urn:epc:id:sgln:0614141.00001.1234", result.getEpcPureIdentityURI());
        assertEquals("sgln", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("3", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("00001", result.getLocationReference());
        assertEquals("1234", result.getExtension());
        assertEquals(96, result.getBinary().length());
    }

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "3274257BF4000200000004D2",
                "urn:epc:tag:sgln-96:3.0614141.00001.1234",
                "urn:epc:id:sgln:0614141.00001.1234",
                "sgln",
                "96",
                "3",
                "7",
                "0614141",
                "00001",
                "1234",
                96
            ),
            Arguments.arguments(
                "3276451FD46072000000162E",
                "urn:epc:tag:sgln-96:3.9521141.12345.5678",
                "urn:epc:id:sgln:9521141.12345.5678",
                "sgln",
                "96",
                "3",
                "7",
                "9521141",
                "12345",
                "5678",
                96
            ),
            Arguments.arguments(
                "3976451FD46072CD9615F8800000000000000000000000000000",
                "urn:epc:tag:sgln-195:3.9521141.12345.32a/b",
                "urn:epc:id:sgln:9521141.12345.32a/b",
                "sgln",
                "195",
                "3",
                "7",
                "9521141",
                "12345",
                "32a/b",
                195
            )
        );
    }

    @ParameterizedTest(name = "[{index}] RFID Tag: {0}")
    @MethodSource("provideData")
    public void decode_RFIDTag(
        final String expectedRfidTag,
        final String expectedEpcTagURI,
        final String expectedEpcPureIdentityURI,
        final String expectedEpcScheme,
        final String expectedTagSize,
        final String expectedFilterValue,
        final String expectedPrefixLength,
        final String expectedCompanyPrefix,
        final String expectedLocationReference,
        final String expectedExtension,
        final Integer expectedBitCount
    ) throws Exception {
        final SGLN result = ParseSGLN.Builder()
            .withRFIDTag(expectedRfidTag)
            .build()
            .getSGLN();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedLocationReference, result.getLocationReference());
        assertEquals(expectedExtension, result.getExtension());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @ParameterizedTest(name = "[{index}] EPC Tag URI: {1}")
    @MethodSource("provideData")
    public void decode_EPCTagURI(
        final String expectedRfidTag,
        final String expectedEpcTagURI,
        final String expectedEpcPureIdentityURI,
        final String expectedEpcScheme,
        final String expectedTagSize,
        final String expectedFilterValue,
        final String expectedPrefixLength,
        final String expectedCompanyPrefix,
        final String expectedLocationReference,
        final String expectedExtension,
        final Integer expectedBitCount
    ) throws Exception {
        final SGLN result = ParseSGLN.Builder()
            .withEPCTagURI(expectedEpcTagURI)
            .build()
            .getSGLN();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedLocationReference, result.getLocationReference());
        assertEquals(expectedExtension, result.getExtension());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @Test
    public void decode_EPCPureIdentityURI() throws Exception {
        final SGLN result = ParseSGLN.Builder()
            .withEPCPureIdentityURI("urn:epc:id:sgln:0614141.00001.1234")
            .withTagSize(SGLNTagSize.BITS_96)
            .withFilterValue(SGLNFilterValue.RESERVED_3)
            .build()
            .getSGLN();

        assertNotNull(result);
        assertEquals("3274257BF4000200000004D2", result.getRfidTag());
        assertEquals("urn:epc:tag:sgln-96:3.0614141.00001.1234", result.getEpcTagURI());
        assertEquals("sgln", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("3", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("00001", result.getLocationReference());
        assertEquals("1234", result.getExtension());
        assertEquals(96, result.getBinary().length());
    }

}
