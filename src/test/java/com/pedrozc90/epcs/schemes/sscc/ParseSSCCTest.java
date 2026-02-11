package com.pedrozc90.epcs.schemes.sscc;

import com.pedrozc90.epcs.schemes.sscc.enums.SSCCExtensionDigit;
import com.pedrozc90.epcs.schemes.sscc.enums.SSCCFilterValue;
import com.pedrozc90.epcs.schemes.sscc.enums.SSCCTagSize;
import com.pedrozc90.epcs.schemes.sscc.objects.SSCC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParseSSCCTest {

    @Test
    public void encode() throws Exception {
        final SSCC result = ParseSSCC.Builder()
            .withCompanyPrefix("023356789")
            .withExtensionDigit(SSCCExtensionDigit.EXTENSION_3)
            .withSerial("0200002")
            .withTagSize(SSCCTagSize.BITS_96)
            .withFilterValue(SSCCFilterValue.RESERVED_5)
            .build()
            .getSSCC();

        assertNotNull(result);
        assertEquals("31AC16465751CCD0C2000000", result.getRfidTag());
        assertEquals("urn:epc:tag:sscc-96:5.023356789.30200002", result.getEpcTagURI());
        assertEquals("urn:epc:id:sscc:023356789.30200002", result.getEpcPureIdentityURI());
        assertEquals("sscc", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("5", result.getFilterValue());
        assertEquals("9", result.getPrefixLength());
        assertEquals("023356789", result.getCompanyPrefix());
        assertEquals("2", result.getCheckDigit());
        assertEquals("3", result.getExtensionDigit());
        assertEquals("0200002", result.getSerial());
        assertEquals(96, result.getBinary().length());
    }

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "311BA1B300CE0A6A83000000",
                "urn:epc:tag:sscc-96:0.952012.03456789123",
                "urn:epc:id:sscc:952012.03456789123",
                "sscc",
                "96",
                "0",
                "6",
                "952012",
                "6",
                "5",
                "0",
                "3456789123",
                96
            ),
            Arguments.arguments(
                "31AC16465751CCD0C2000000",
                "urn:epc:tag:sscc-96:5.023356789.30200002",
                "urn:epc:id:sscc:023356789.30200002",
                "sscc",
                "96",
                "5",
                "9",
                "023356789",
                "3",
                "6",
                "0",
                "0200002",
                96
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
        final String expectedPartitionValue,
        final String expectedCheckDigit,
        final String expectedExtensionDigit,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final SSCC result = ParseSSCC.Builder()
            .withRFIDTag(expectedRfidTag)
            .build()
            .getSSCC();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedPartitionValue, result.getPartitionValue());
        assertEquals(expectedCheckDigit, result.getCheckDigit());
        assertEquals(expectedExtensionDigit, result.getExtensionDigit());
        assertEquals(expectedSerial, result.getSerial());
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
        final String expectedPartitionValue,
        final String expectedCheckDigit,
        final String expectedExtensionDigit,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final SSCC result = ParseSSCC.Builder()
            .withEPCTagURI(expectedEpcTagURI)
            .build()
            .getSSCC();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedPartitionValue, result.getPartitionValue());
        assertEquals(expectedCheckDigit, result.getCheckDigit());
        assertEquals(expectedExtensionDigit, result.getExtensionDigit());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @Test
    public void decode_EPCPureIdentityURI() throws Exception {
        final SSCC result = ParseSSCC.Builder()
            .withEPCPureIdentityURI("urn:epc:id:sscc:023356789.30200002")
            .withTagSize(SSCCTagSize.BITS_96)
            .withFilterValue(SSCCFilterValue.RESERVED_5)
            .build()
            .getSSCC();

        assertNotNull(result);
        assertEquals("31AC16465751CCD0C2000000", result.getRfidTag());
        assertEquals("urn:epc:tag:sscc-96:5.023356789.30200002", result.getEpcTagURI());
        assertEquals("sscc", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("5", result.getFilterValue());
        assertEquals("9", result.getPrefixLength());
        assertEquals("023356789", result.getCompanyPrefix());
        assertEquals("2", result.getCheckDigit());
        assertEquals("3", result.getExtensionDigit());
        assertEquals("0200002", result.getSerial());
        assertEquals(96, result.getBinary().length());
    }

}
