package com.pedrozc90.epcs.schemes.sgln;

import com.pedrozc90.epcs.schemes.sgln.enums.SGLNFilterValue;
import com.pedrozc90.epcs.schemes.sgln.enums.SGLNTagSize;
import com.pedrozc90.epcs.schemes.sgln.objects.SGLN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SGLNParserTest {

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
        final String expectedLocationReference,
        final String expectedExtension,
        final Integer expectedBitCount
    ) throws Exception {
        final SGLNTagSize tagSize = SGLNTagSize.of(Integer.parseInt(expectedTagSize));
        final SGLNFilterValue filterValue = SGLNFilterValue.of(Integer.parseInt(expectedFilterValue));

        final SGLN result = SGLNParser.Builder()
            .withCompanyPrefix(expectedCompanyPrefix)
            .withLocationReference(expectedLocationReference)
            .withExtension(expectedExtension)
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
        assertEquals(expectedLocationReference, result.getLocationReference());
        assertEquals(expectedExtension, result.getExtension());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @DisplayName("Decode RFID Tag")
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
        final SGLN result = SGLNParser.Builder()
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
        assertEquals(expectedLocationReference, result.getLocationReference());
        assertEquals(expectedExtension, result.getExtension());
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
        final String expectedLocationReference,
        final String expectedExtension,
        final Integer expectedBitCount
    ) throws Exception {
        final SGLN result = SGLNParser.Builder()
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
        assertEquals(expectedLocationReference, result.getLocationReference());
        assertEquals(expectedExtension, result.getExtension());
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
        final String expectedLocationReference,
        final String expectedExtension,
        final Integer expectedBitCount
    ) throws Exception {
        final SGLNTagSize tagSize = SGLNTagSize.of(Integer.parseInt(expectedTagSize));
        final SGLNFilterValue filterValue = SGLNFilterValue.of(Integer.parseInt(expectedFilterValue));

        final SGLN result = SGLNParser.Builder()
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
        assertEquals(expectedLocationReference, result.getLocationReference());
        assertEquals(expectedExtension, result.getExtension());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

}
