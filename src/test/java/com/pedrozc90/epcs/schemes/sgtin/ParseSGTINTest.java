package com.pedrozc90.epcs.schemes.sgtin;

import com.pedrozc90.epcs.exception.EPCParseException;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINExtensionDigit;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINFilterValue;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINTagSize;
import com.pedrozc90.epcs.schemes.sgtin.objects.SGTIN;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.pedrozc90.epcs.schemes.sgtin.ParseSGTIN.Builder;
import static org.junit.jupiter.api.Assertions.*;

public class ParseSGTINTest {

    @Test
    public void encode() throws EPCParseException {
        final SGTIN result = Builder()
            .withCompanyPrefix("0614141")
            .withExtensionDigit(SGTINExtensionDigit.EXTENSION_8)
            .withItemReference("12345")
            .withSerial("6789")
            .withTagSize(SGTINTagSize.BITS_96)
            .withFilterValue(SGTINFilterValue.RESERVED_3)
            .build()
            .getSGTIN();

        assertNotNull(result);
        assertEquals("3074257BF7194E4000001A85", result.getRfidTag());
        assertEquals("urn:epc:tag:sgtin-96:3.0614141.812345.6789", result.getEpcTagURI());
        assertEquals("urn:epc:id:sgtin:0614141.812345.6789", result.getEpcPureIdentityURI());
        assertEquals("sgtin", result.getEpcScheme());
        assertEquals("96", result.getTagSize());
        assertEquals("3", result.getFilterValue());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("5", result.getPartitionValue());
        assertEquals("8", result.getCheckDigit());
        assertEquals("12345", result.getItemReference());
        assertEquals("6789", result.getSerial());
        assertEquals(96, result.getBinary().length());
    }

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "3074257BF7194E4000001A85",
                "urn:epc:tag:sgtin-96:3.0614141.812345.6789",
                "urn:epc:id:sgtin:0614141.812345.6789",
                "sgtin",
                "96",
                "3",
                "7",
                "0614141",
                "5",
                "8",
                "12345",
                "6789",
                96
            ),
            Arguments.arguments(
                "3066C4409047E140075BCD15",
                "urn:epc:tag:sgtin-96:3.95060001343.05.123456789",
                "urn:epc:id:sgtin:95060001343.05.123456789",
                "sgtin",
                "96",
                "3",
                "11",
                "95060001343",
                "1",
                "2",
                "5",
                "123456789",
                96
            ),
            Arguments.arguments(
                "3666C4409047E159B2C2BF100000000000000000000000000000",
                "urn:epc:tag:sgtin-198:3.95060001343.05.32a/b",
                "urn:epc:id:sgtin:95060001343.05.32a/b",
                "sgtin",
                "198",
                "3",
                "11",
                "95060001343",
                "1",
                "2",
                "5",
                "32a/b",
                198
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
        final String expectedItemReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws EPCParseException {
        final SGTIN result = Builder()
            .withRFIDTag(expectedRfidTag)
            .build()
            .getSGTIN();

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
        assertEquals(expectedItemReference, result.getItemReference());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @ParameterizedTest(name = "[{index}] Epc Tag URI: {1}")
    @MethodSource("provideData")
    public void decode_EpcTagURI(
        final String expextedRfidTag,
        final String expectedEpcTagURI,
        final String expectedEpcPureIdentityURI,
        final String expectedEpcScheme,
        final String expectedTagSize,
        final String expectedFilterValue,
        final String expectedPrefixLength,
        final String expectedCompanyPrefix,
        final String expectedPartitionValue,
        final String expectedCheckDigit,
        final String expectedItemReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws EPCParseException {
        final SGTIN result = Builder()
            .withEPCTagURI(expectedEpcTagURI)
            .build()
            .getSGTIN();

        assertEquals(expextedRfidTag, result.getRfidTag());
        assertEquals(expectedEpcTagURI, result.getEpcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.getEpcScheme());
        assertEquals(expectedTagSize, result.getTagSize());
        assertEquals(expectedFilterValue, result.getFilterValue());
        assertEquals(expectedPrefixLength, result.getPrefixLength());
        assertEquals(expectedCompanyPrefix, result.getCompanyPrefix());
        assertEquals(expectedPartitionValue, result.getPartitionValue());
        assertEquals(expectedCheckDigit, result.getCheckDigit());
        assertEquals(expectedItemReference, result.getItemReference());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @Test
    public void decode_EpcPureIdentityURI() throws EPCParseException {
        final String value = "urn:epc:id:sgtin:0614141.812345.6789";
        final SGTIN result = Builder()
            .withEPCPureIdentityURI(value)
            .withTagSize(SGTINTagSize.BITS_96)
            .withFilterValue(SGTINFilterValue.RESERVED_3)
            .build()
            .getSGTIN();
        assertEquals("3074257BF7194E4000001A85", result.getRfidTag());
        assertEquals("urn:epc:tag:sgtin-96:3.0614141.812345.6789", result.getEpcTagURI());
        assertEquals("sgtin", result.getEpcScheme());
        assertEquals("3", result.getFilterValue());
        assertEquals("96", result.getTagSize());
        assertEquals("7", result.getPrefixLength());
        assertEquals("0614141", result.getCompanyPrefix());
        assertEquals("5", result.getPartitionValue());
        assertEquals("8", result.getCheckDigit());
        assertEquals("12345", result.getItemReference());
        assertEquals("6789", result.getSerial());
        assertEquals(96, result.getBinary().length());
    }

    // Specific field extraction tests
    @ParameterizedTest(name = "[{index}] RFID: {0} -> Serial: {1}")
    @CsvSource({
        "3024698E2CB1005678901234, 96511988276",
        "3074257bf7194e4000001a85, 6789",
        "303500C1C20044C80009FD8D8, 34360393101"
    })
    public void parseEpcSerial(String rfidTag, String expectedSerial) throws EPCParseException {
        final SGTIN sgtin = Builder().withRFIDTag(rfidTag).build().getSGTIN();
        assertEquals(expectedSerial, sgtin.getSerial());
    }

    @ParameterizedTest(name = "[{index}] RFID: {0} -> EAN: {1}")
    @CsvSource({
        "3024698E2CB1005678901234, 141674018641",
        // Add more test cases
    })
    public void parseEpcEan(String rfidTag, String expectedEan) throws EPCParseException {
        final SGTIN sgtin = Builder().withRFIDTag(rfidTag).build().getSGTIN();
        assertEquals(expectedEan, sgtin.getCompanyPrefix() + sgtin.getItemReference());
    }

    @ParameterizedTest(name = "[{index}] RFID: {0} -> Filter: {1}")
    @CsvSource({
        "3024698E2CB1005678901234, 1",
        "3074257bf7194e4000001a85, 3",
        "303500C1C20044C80009FD8D8, 1"
    })
    public void parseEpcFilter(String rfidTag, String expectedFilter) throws EPCParseException {
        final SGTIN sgtin = Builder().withRFIDTag(rfidTag).build().getSGTIN();
        assertEquals(expectedFilter, sgtin.getFilterValue());
    }

    @ParameterizedTest(name = "[{index}] RFID: {0} -> Partition: {1}")
    @CsvSource({
        "30285471BD5A0A5678901234, 2",
        "3024698E2CB1005678901234, 1"
    })
    public void parseEpcPartition(String rfidTag, String expectedPartition) throws EPCParseException {
        final SGTIN sgtin = Builder().withRFIDTag(rfidTag).build().getSGTIN();
        assertEquals(expectedPartition, sgtin.getPartitionValue());
    }

    // Exception tests
    @ParameterizedTest(name = "[{index}] Invalid RFID: {0}")
    @ValueSource(strings = {
        "F45349FB11DF49FA935AB6FF",  // Not GS1
        "303C83F1B7DD441678901234",  // Wrong partition
        "30285471BD5A0A56789G1234"   // Non-hex character
    })
    public void whenInvalidRfidTag_thenExceptionIsRaised(String invalidRfidTag) {
        assertThrows(
            IllegalArgumentException.class,
            () -> Builder().withRFIDTag(invalidRfidTag).build()
        );
    }

}
