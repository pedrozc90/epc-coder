package com.pedrozc90.epcs.schemes.gdti;

import com.pedrozc90.epcs.schemes.gdti.enums.GDTIFilterValue;
import com.pedrozc90.epcs.schemes.gdti.enums.GDTITagSize;
import com.pedrozc90.epcs.schemes.gdti.objects.GDTI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParseGDTITest {

    @Test
    public void encode() throws Exception {
        final GDTI result = ParseGDTI.Builder()
            .withCompanyPrefix("0614141")
            .withDocType("12345")
            .withSerial("400")
            .withTagSize(GDTITagSize.BITS_96)
            .withFilterValue(GDTIFilterValue.RESERVED_3)
            .build()
            .getGDTI();

        assertNotNull(result);
        assertEquals("2C74257BF460720000000190", result.getRfidTag());
        assertEquals("urn:epc:tag:gdti-96:3.0614141.12345.400", result.getEpcTagURI());
        assertEquals("urn:epc:id:gdti:0614141.12345.400", result.getEpcPureIdentityURI());
        assertEquals("gdti", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("3", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("2", result.getCheckDigit());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("12345", result.getDocType());
        assertEquals("400", result.getSerial());
    }

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
                174
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
        final String expectedDocType,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final GDTI result = ParseGDTI.Builder()
            .withRFIDTag(expectedRfidTag)
            .build()
            .getGDTI();

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
        final String expectedDocType,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final GDTI result = ParseGDTI.Builder()
            .withEPCTagURI(expectedEpcTagURI)
            .build()
            .getGDTI();

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

    @Test
    public void decode_EPCPureIdentityURI() throws Exception {
        final GDTI result = ParseGDTI.Builder()
            .withEPCPureIdentityURI("urn:epc:id:gdti:0614141.12345.400")
            .withTagSize(GDTITagSize.BITS_96)
            .withFilterValue(GDTIFilterValue.RESERVED_3)
            .build()
            .getGDTI();

        assertNotNull(result);
        assertEquals("2C74257BF460720000000190", result.getRfidTag());
        assertEquals("urn:epc:tag:gdti-96:3.0614141.12345.400", result.getEpcTagURI());
        assertEquals("gdti", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("3", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("2", result.getCheckDigit());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("12345", result.getDocType());
        assertEquals("400", result.getSerial());
    }

}
