package com.pedrozc90.epcs.schemes.cpi;

import com.pedrozc90.epcs.schemes.cpi.enums.CPIFilterValue;
import com.pedrozc90.epcs.schemes.cpi.enums.CPITagSize;
import com.pedrozc90.epcs.schemes.cpi.objects.CPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CPIParserTest {

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "3C34257BF400181C80000190",
                "urn:epc:tag:cpi-96:1.0614141.12345.400",
                "urn:epc:id:cpi:0614141.12345.400",
                "cpi",
                "96",
                "1",
                "7",
                "0614141",
                "12345",
                "400",
                96
            ),
            Arguments.arguments(
                "3C76451FD400C0E680003039",
                "urn:epc:tag:cpi-96:3.9521141.98765.12345",
                "urn:epc:id:cpi:9521141.98765.12345",
                "cpi",
                "96",
                "3",
                "7",
                "9521141",
                "98765",
                "12345",
                96
            ),
            Arguments.arguments(
                "3D76451FD75411DEF6B4CC00000003039000",
                "urn:epc:tag:cpi-var:3.9521141.5PQ7/Z43.12345",
                "urn:epc:id:cpi:9521141.5PQ7/Z43.12345",
                "cpi",
                "var",
                "3",
                "7",
                "9521141",
                "5PQ7/Z43",
                "12345",
                144
            ),
            Arguments.arguments(
                "3DF4257BF71CB304260000075BCD1500",
                "urn:epc:tag:cpi-var:7.0614141.123ABX.123456789",
                "urn:epc:id:cpi:0614141.123ABX.123456789",
                "cpi",
                "var",
                "3",
                "7",
                "0614141",
                "123ABX",
                "123456789",
                128
            ),
            Arguments.arguments(
                "3DF4257BF71CB30420C000075BCD1500",
                "urn:epc:tag:cpi-var:7.0614141.123ABC.12345678",
                "urn:epc:id:cpi:0614141.123ABC.123456789",
                "cpi",
                "var",
                "3",
                "7",
                "0614141",
                "123ABC",
                "12345678",
                128
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
        final String expectedComponentPartReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final CPITagSize tagSize = CPITagSize.of(expectedBitCount);
        final CPIFilterValue filterValue = CPIFilterValue.of(Integer.parseInt(expectedFilterValue));
        final CPI result = CPIParser.Builder()
            .withCompanyPrefix(expectedCompanyPrefix)
            .withComponentPartReference(expectedComponentPartReference)
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
        assertEquals(expectedComponentPartReference, result.getComponentPartReference());
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
        final String expectedComponentPartReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final CPI result = CPIParser.Builder()
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
        assertEquals(expectedComponentPartReference, result.getComponentPartReference());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @DisplayName("Decode Epc Tag URI")
    @ParameterizedTest(name = "[{index}] EpcTagURI: {1}")
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
        final String expectedComponentPartReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final CPI result = CPIParser.Builder()
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
        assertEquals(expectedComponentPartReference, result.getComponentPartReference());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @DisplayName("Decode EpcPureIdentityURI")
    @ParameterizedTest(name = "[{index}] EpcPureIdentityURI: {2}")
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
        final String expectedComponentPartReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final CPITagSize tagSize = CPITagSize.of(expectedBitCount);
        final CPIFilterValue filterValue = CPIFilterValue.of(Integer.parseInt(expectedFilterValue));

        final CPI result = CPIParser.Builder()
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
        assertEquals(expectedComponentPartReference, result.getComponentPartReference());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

}
