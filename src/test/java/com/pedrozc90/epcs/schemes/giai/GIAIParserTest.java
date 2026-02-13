package com.pedrozc90.epcs.schemes.giai;

import com.pedrozc90.epcs.schemes.giai.enums.GIAIFilterValue;
import com.pedrozc90.epcs.schemes.giai.enums.GIAITagSize;
import com.pedrozc90.epcs.schemes.giai.objects.GIAI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GIAIParserTest {

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
                208
            )
        );
    }

    @DisplayName("Encode")
    @ParameterizedTest(name = "[{index}] RFID Tag: {0}")
    @MethodSource("provideData")
    public void encode(
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
        final GIAITagSize tagSize = GIAITagSize.of(Integer.parseInt(expectedTagSize));
        final GIAIFilterValue filterValue = GIAIFilterValue.of(Integer.parseInt(expectedFilterValue));

        final GIAI result = GIAIParser.Builder()
            .withCompanyPrefix(expectedCompanyPrefix)
            .withIndividualAssetReference(expectedIndividualAssetReference)
            .withTagSize(tagSize)
            .withFilterValue(filterValue)
            .build();

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

    @DisplayName("Decode RFID Tag")
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
        final GIAI result = GIAIParser.Builder()
            .withRFIDTag(expectedRfidTag)
            .build();

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

    @DisplayName("Decode Epc Tag URI")
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
        final GIAI result = GIAIParser.Builder()
            .withEpcTagURI(expectedEpcTagURI)
            .build();

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

    @DisplayName("Decode Epc Pure Identity URI")
    @ParameterizedTest(name = "[{index}] Epc Pure Identity URI: {2}")
    @MethodSource("provideData")
    public void decode_EpcPureIdentityURI(
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
        final GIAITagSize tagSize = GIAITagSize.of(Integer.parseInt(expectedTagSize));
        final GIAIFilterValue filterValue = GIAIFilterValue.of(Integer.parseInt(expectedFilterValue));

        final GIAI result = GIAIParser.Builder()
            .withEpcPureIdentityURI(expectedEpcPureIdentityURI)
            .withTagSize(tagSize)
            .withFilterValue(filterValue)
            .build();

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

}
