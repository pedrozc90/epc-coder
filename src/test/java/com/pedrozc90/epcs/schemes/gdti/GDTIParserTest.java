package com.pedrozc90.epcs.schemes.gdti;

import com.pedrozc90.epcs.schemes.gdti.enums.GDTIFilterValue;
import com.pedrozc90.epcs.schemes.gdti.enums.GDTITagSize;
import com.pedrozc90.epcs.schemes.gdti.objects.GDTI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GDTIParserTest {

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "2C74257BF460720000000190",
                "urn:epc:tag:gdti-96:3.0614141.12345.400",
                "urn:epc:id:gdti:0614141.12345.400",
                "gdti",
                "96",
                "3",
                "7",
                "0614141",
                "12345",
                "400",
                96
            ),
            Arguments.arguments(
                "2C76451FD46072000000162E",
                "urn:epc:tag:gdti-96:3.9521141.12345.5678",
                "urn:epc:id:gdti:9521141.12345.5678",
                "gdti",
                "96",
                "3",
                "7",
                "9521141",
                "12345",
                "5678",
                96
            ),
            Arguments.arguments(
                "3E76451FD7039B061438997367D0C18B266D1AB66EE0",
                "urn:epc:tag:gdti-174:3.9521141.98765.ABCDefgh012345678",
                "urn:epc:id:gdti:9521141.98765.ABCDefgh012345678",
                "gdti",
                "174",
                "3",
                "7",
                "9521141",
                "98765",
                "ABCDefgh012345678",
                176
            )
        );
    }

    @DisplayName("Encode")
    @ParameterizedTest(name = "[{index}] encode: {0}")
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
        final String expectedDocType,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final GDTITagSize tagSize = GDTITagSize.of(Integer.parseInt(expectedTagSize));
        final GDTIFilterValue filterValue = GDTIFilterValue.of(Integer.parseInt(expectedFilterValue));

        final GDTI result = GDTIParser.Builder()
            .withCompanyPrefix(expectedCompanyPrefix)
            .withDocType(expectedDocType)
            .withSerial(expectedSerial)
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
        assertEquals(expectedDocType, result.getDocType());
        assertEquals(expectedSerial, result.getSerial());
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
        final String expectedDocType,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final GDTI result = GDTIParser.Builder()
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
        assertEquals(expectedDocType, result.getDocType());
        assertEquals(expectedSerial, result.getSerial());
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
        final String expectedDocType,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final GDTI result = GDTIParser.Builder()
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
        assertEquals(expectedDocType, result.getDocType());
        assertEquals(expectedSerial, result.getSerial());
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
        final String expectedDocType,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final GDTITagSize tagSize = GDTITagSize.of(Integer.parseInt(expectedTagSize));
        final GDTIFilterValue filterValue = GDTIFilterValue.of(Integer.parseInt(expectedFilterValue));

        final GDTI result = GDTIParser.Builder()
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
        assertEquals(expectedDocType, result.getDocType());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

}
