package com.pedrozc90.epcs.schemes.giai;

import com.pedrozc90.epcs.schemes.giai.enums.GIAIFilterValue;
import com.pedrozc90.epcs.schemes.giai.enums.GIAITagSize;
import com.pedrozc90.epcs.schemes.giai.objects.GIAI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParseGIAITest {

    @Test
    public void encode() throws Exception {
        final GIAI result = ParseGIAI.Builder()
            .withCompanyPrefix("0614141")
            .withIndividualAssetReference("9876")
            .withTagSize(GIAITagSize.BITS_96)
            .withFilterValue(GIAIFilterValue.RESERVED_1)
            .build()
            .getGIAI();

        assertNotNull(result);
        assertEquals("3434257BF400000000002694", result.getRfidTag());
        assertEquals("urn:epc:tag:giai-96:1.0614141.9876", result.getEpcTagURI());
        assertEquals("urn:epc:id:giai:0614141.9876", result.getEpcPureIdentityURI());
        assertEquals("giai", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("1", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("9876", result.getIndividualAssetReference());
    }

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "3434257BF400000000002694",
                "urn:epc:tag:giai-96:1.0614141.9876",
                "urn:epc:id:giai:0614141.9876",
                "giai",
                "96",
                "1",
                "7",
                "0614141",
                "9876",
                96
            ),
            Arguments.arguments(
                "3476451FD40000000000162E",
                "urn:epc:tag:giai-96:3.9521141.5678",
                "urn:epc:id:giai:9521141.5678",
                "giai",
                "96",
                "3",
                "7",
                "9521141",
                "5678",
                96
            ),
            Arguments.arguments(
                "3876451FD59B2C2BF10000000000000000000000000000000000",
                "urn:epc:tag:giai-202:3.9521141.32a/b",
                "urn:epc:id:giai:9521141.32a/b",
                "giai",
                "202",
                "3",
                "7",
                "9521141",
                "32a/b",
                202
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
        final String expectedIndividualAssetReference,
        final Integer expectedBitCount
    ) throws Exception {
        final GIAI result = ParseGIAI.Builder()
            .withRFIDTag(expectedRfidTag)
            .build()
            .getGIAI();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedIndividualAssetReference, result.getIndividualAssetReference());
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
        final String expectedIndividualAssetReference,
        final Integer expectedBitCount
    ) throws Exception {
        final GIAI result = ParseGIAI.Builder()
            .withEPCTagURI(expectedEpcTagURI)
            .build()
            .getGIAI();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedIndividualAssetReference, result.getIndividualAssetReference());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @Test
    public void decode_EpcPureIdentityURI() throws Exception {
        final GIAI result = ParseGIAI.Builder()
            .withEPCPureIdentityURI("urn:epc:id:giai:0614141.9876")
            .withTagSize(GIAITagSize.BITS_96)
            .withFilterValue(GIAIFilterValue.RESERVED_1)
            .build()
            .getGIAI();

        assertNotNull(result);
        assertEquals("3434257BF400000000002694", result.getRfidTag());
        assertEquals("urn:epc:tag:giai-96:1.0614141.9876", result.getEpcTagURI());
        assertEquals("giai", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("1", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("9876", result.getIndividualAssetReference());
    }

}
