package com.pedrozc90.epcs.schemes.gsrn;

import com.pedrozc90.epcs.schemes.gsrn.enums.GSRNFilterValue;
import com.pedrozc90.epcs.schemes.gsrn.enums.GSRNTagSize;
import com.pedrozc90.epcs.schemes.gsrn.objects.GSRN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GSRNParserTest {

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
        final GSRNTagSize tagSize = GSRNTagSize.of(Integer.parseInt(expectedTagSize));
        final GSRNFilterValue filterValue = GSRNFilterValue.of(Integer.parseInt(expectedFilterValue));

        final GSRN result = GSRNParser.Builder()
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
        final GSRN result = GSRNParser.Builder()
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
        final GSRN result = GSRNParser.Builder()
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
        final GSRNTagSize tagSize = GSRNTagSize.of(Integer.parseInt(expectedTagSize));
        final GSRNFilterValue filterValue = GSRNFilterValue.of(Integer.parseInt(expectedFilterValue));

        final GSRN result = GSRNParser.Builder()
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
