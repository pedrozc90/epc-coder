package com.pedrozc90.epcs.schemes.gsrnp;

import com.pedrozc90.epcs.schemes.gsrnp.enums.GSRNPFilterValue;
import com.pedrozc90.epcs.schemes.gsrnp.enums.GSRNPTagSize;
import com.pedrozc90.epcs.schemes.gsrnp.objects.GSRNP;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GSRNPParserTest {

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "2E74257BF4499602D2000000",
                "urn:epc:tag:gsrnp-96:3.0614141.1234567890",
                "urn:epc:id:gsrnp:0614141.1234567890",
                "gsrnp",
                "96",
                "3",
                "7",
                "0614141",
                "1234567890",
                96
            ),
            Arguments.arguments(
                "2E76451FD4499602D2000000",
                "urn:epc:tag:gsrnp-96:3.9521141.1234567890",
                "urn:epc:id:gsrnp:9521141.1234567890",
                "gsrnp",
                "96",
                "3",
                "7",
                "9521141",
                "1234567890",
                96
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
        final String expectedServiceReference,
        final Integer expectedBitCount
    ) throws Exception {
        final GSRNPTagSize tagSize = GSRNPTagSize.of(Integer.parseInt(expectedTagSize));
        final GSRNPFilterValue filterValue = GSRNPFilterValue.of(Integer.parseInt(expectedFilterValue));

        final GSRNP result = GSRNPParser.Builder()
            .withCompanyPrefix(expectedCompanyPrefix)
            .withServiceReference(expectedServiceReference)
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
        assertEquals(expectedServiceReference, result.getServiceReference());
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
        final String expectedServiceReference,
        final Integer expectedBitCount
    ) throws Exception {
        final GSRNP result = GSRNPParser.Builder()
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
        assertEquals(expectedServiceReference, result.getServiceReference());
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
        final String expectedServiceReference,
        final Integer expectedBitCount
    ) throws Exception {
        final GSRNP result = GSRNPParser.Builder()
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
        assertEquals(expectedServiceReference, result.getServiceReference());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @DisplayName("Decode Epc Pure Identity URI")
    @ParameterizedTest(name = "[{index}] Epc Tag URI: {1}")
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
        final String expectedServiceReference,
        final Integer expectedBitCount
    ) throws Exception {
        final GSRNPTagSize tagSize = GSRNPTagSize.of(Integer.parseInt(expectedTagSize));
        final GSRNPFilterValue filterValue = GSRNPFilterValue.of(Integer.parseInt(expectedFilterValue));

        final GSRNP result = GSRNPParser.Builder()
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
        assertEquals(expectedServiceReference, result.getServiceReference());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

}
