package com.pedrozc90.epcs.schemes.grai;

import com.pedrozc90.epcs.schemes.grai.enums.GRAIFilterValue;
import com.pedrozc90.epcs.schemes.grai.enums.GRAITagSize;
import com.pedrozc90.epcs.schemes.grai.objects.GRAI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParseGRAITest {

    @Test
    public void encode() throws Exception {
        final GRAI result = ParseGRAI.Builder()
            .withCompanyPrefix("0614141")
            .withAssetType("00008")
            .withSerial("1234")
            .withTagSize(GRAITagSize.BITS_96)
            .withFilterValue(GRAIFilterValue.RESERVED_3)
            .build()
            .getGRAI();

        assertNotNull(result);
        assertEquals("3374257BF4000200000004D2", result.getRfidTag());
        assertEquals("urn:epc:tag:grai-96:3.0614141.00008.1234", result.getEpcTagURI());
        assertEquals("urn:epc:id:grai:0614141.00008.1234", result.getEpcPureIdentityURI());
        assertEquals("grai", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("3", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("00008", result.getAssetType());
        assertEquals("1234", result.getSerial());
    }

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "3374257BF4000200000004D2",
                "urn:epc:tag:grai-96:3.0614141.00008.1234",
                "urn:epc:id:grai:0614141.00008.1234",
                "grai",
                "96",
                "3",
                "7",
                "0614141",
                "00008",
                "1234",
                96
            ),
            Arguments.arguments(
                "3376451FD40C0E400000162E",
                "urn:epc:tag:grai-96:3.9521141.12345.5678",
                "urn:epc:id:grai:9521141.12345.5678",
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
                "3776451FD40C0E59B2C2BF1000000000000000000000",
                "urn:epc:tag:grai-170:3.9521141.12345.32a/b",
                "urn:epc:id:grai:9521141.12345.32a/b",
                "sgln",
                "170",
                "3",
                "7",
                "9521141",
                "12345",
                "32a/b",
                170
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
        final String expectedAssetType,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final GRAI result = ParseGRAI.Builder()
            .withRFIDTag(expectedRfidTag)
            .build()
            .getGRAI();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedAssetType, result.getAssetType());
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
        final String expectedAssetType,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final GRAI result = ParseGRAI.Builder()
            .withEPCTagURI(expectedEpcTagURI)
            .build()
            .getGRAI();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedAssetType, result.getAssetType());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @Test
    public void decode_EpcPureIdentityURI() throws Exception {
        final GRAI result = ParseGRAI.Builder()
            .withEPCPureIdentityURI("urn:epc:id:grai:0614141.00008.1234")
            .withTagSize(GRAITagSize.BITS_96)
            .withFilterValue(GRAIFilterValue.RESERVED_3)
            .build()
            .getGRAI();

        assertEquals("3374257BF4000200000004D2", result.getRfidTag());
        assertEquals("urn:epc:tag:grai-96:3.0614141.00008.1234", result.getEpcTagURI());
        assertEquals("grai", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("3", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("00008", result.getAssetType());
        assertEquals("1234", result.getSerial());
    }

}
