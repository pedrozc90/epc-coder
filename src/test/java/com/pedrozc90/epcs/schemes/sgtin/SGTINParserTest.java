package com.pedrozc90.epcs.schemes.sgtin;

import com.pedrozc90.epcs.exception.EpcParseException;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINExtensionDigit;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINFilterValue;
import com.pedrozc90.epcs.schemes.sgtin.enums.SGTINTagSize;
import com.pedrozc90.epcs.schemes.sgtin.objects.SGTIN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.pedrozc90.epcs.schemes.sgtin.SGTINParser.Builder;
import static org.junit.jupiter.api.Assertions.*;

public class SGTINParserTest {

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
                "0",
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
                "0",
                "5",
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
        final String expectedPartitionValue,
        final String expectedCheckDigit,
        final String expectedExtensionDigit,
        final String expectedItemReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws EpcParseException {
        final SGTINTagSize tagSize = SGTINTagSize.of(Integer.parseInt(expectedTagSize));
        final SGTINFilterValue filterValue = SGTINFilterValue.of(Integer.parseInt(expectedFilterValue));
        final SGTINExtensionDigit extensionDigit = SGTINExtensionDigit.of(Integer.parseInt(expectedExtensionDigit));

        final SGTIN result = Builder()
            .withCompanyPrefix(expectedCompanyPrefix)
            .withExtensionDigit(extensionDigit)
            .withItemReference(expectedItemReference)
            .withSerial(expectedSerial)
            .withTagSize(tagSize)
            .withFilterValue(filterValue)
            .build();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.rfidTag());
        assertEquals(expectedEpcTagURI, result.epcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.epcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.epcScheme());
        assertEquals(expectedTagSize, result.tagSize());
        assertEquals(expectedFilterValue, result.filterValue());
        assertEquals(expectedPrefixLength, result.prefixLength());
        assertEquals(expectedCompanyPrefix, result.companyPrefix());
        assertEquals(expectedPartitionValue, result.partitionValue());
        assertEquals(expectedCheckDigit, result.checkDigit());
        assertEquals(expectedExtensionDigit, result.extensionDigit());
        assertEquals(expectedItemReference, result.itemReference());
        assertEquals(expectedSerial, result.serial());
        assertEquals(expectedBitCount, result.binary().length());
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
        final String expectedPartitionValue,
        final String expectedCheckDigit,
        final String expectedExtensionDigit,
        final String expectedItemReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws EpcParseException {
        final SGTIN result = Builder()
            .withRFIDTag(expectedRfidTag)
            .build();

        assertNotNull(result);
        assertEquals(expectedRfidTag, result.rfidTag());
        assertEquals(expectedEpcTagURI, result.epcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.epcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.epcScheme());
        assertEquals(expectedTagSize, result.tagSize());
        assertEquals(expectedFilterValue, result.filterValue());
        assertEquals(expectedPrefixLength, result.prefixLength());
        assertEquals(expectedCompanyPrefix, result.companyPrefix());
        assertEquals(expectedPartitionValue, result.partitionValue());
        assertEquals(expectedCheckDigit, result.checkDigit());
        assertEquals(expectedExtensionDigit, result.extensionDigit());
        assertEquals(expectedItemReference, result.itemReference());
        assertEquals(expectedSerial, result.serial());
        assertEquals(expectedBitCount, result.binary().length());
    }

    @DisplayName("Decode Epc Tag URI")
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
        final String expectedExtensionDigit,
        final String expectedItemReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws EpcParseException {
        final SGTIN result = Builder()
            .withEpcTagURI(expectedEpcTagURI)
            .build();

        assertNotNull(result);
        assertEquals(expextedRfidTag, result.rfidTag());
        assertEquals(expectedEpcTagURI, result.epcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.epcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.epcScheme());
        assertEquals(expectedTagSize, result.tagSize());
        assertEquals(expectedFilterValue, result.filterValue());
        assertEquals(expectedPrefixLength, result.prefixLength());
        assertEquals(expectedCompanyPrefix, result.companyPrefix());
        assertEquals(expectedPartitionValue, result.partitionValue());
        assertEquals(expectedCheckDigit, result.checkDigit());
        assertEquals(expectedExtensionDigit, result.extensionDigit());
        assertEquals(expectedItemReference, result.itemReference());
        assertEquals(expectedSerial, result.serial());
        assertEquals(expectedBitCount, result.binary().length());
    }

    @DisplayName("Decode Epc Pure Identity URI")
    @ParameterizedTest(name = "[{index}] Epc Pure Identity URI: {2}")
    @MethodSource("provideData")
    public void decode_EpcPureIdentityURI(
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
        final String expectedExtensionDigit,
        final String expectedItemReference,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws EpcParseException {
        final SGTINTagSize tagSize = SGTINTagSize.of(Integer.parseInt(expectedTagSize));
        final SGTINFilterValue filterValue = SGTINFilterValue.of(Integer.parseInt(expectedFilterValue));

        final SGTIN result = Builder()
            .withEpcPureIdentityURI(expectedEpcPureIdentityURI)
            .withTagSize(tagSize)
            .withFilterValue(filterValue)
            .build();

        assertNotNull(result);
        assertEquals(expextedRfidTag, result.rfidTag());
        assertEquals(expectedEpcTagURI, result.epcTagURI());
        assertEquals(expectedEpcPureIdentityURI, result.epcPureIdentityURI());
        assertEquals(expectedEpcScheme, result.epcScheme());
        assertEquals(expectedTagSize, result.tagSize());
        assertEquals(expectedFilterValue, result.filterValue());
        assertEquals(expectedPrefixLength, result.prefixLength());
        assertEquals(expectedCompanyPrefix, result.companyPrefix());
        assertEquals(expectedPartitionValue, result.partitionValue());
        assertEquals(expectedCheckDigit, result.checkDigit());
        assertEquals(expectedExtensionDigit, result.extensionDigit());
        assertEquals(expectedItemReference, result.itemReference());
        assertEquals(expectedSerial, result.serial());
        assertEquals(expectedBitCount, result.binary().length());
    }

    // Specific field extraction tests
    @ParameterizedTest(name = "[{index}] RFID: {0} -> Serial: {1}")
    @CsvSource({
        "3024698E2CB1005678901234, 96511988276",
        "3074257bf7194e4000001a85, 6789",
        "303500C1C20044C80009FD8D8, 34360393101"
    })
    public void parseEpcSerial(final String rfidTag, final String expectedSerial) throws EpcParseException {
        final SGTIN sgtin = Builder().withRFIDTag(rfidTag).build();
        assertEquals(expectedSerial, sgtin.serial());
    }

    @ParameterizedTest(name = "[{index}] RFID: {0} -> EAN: {1}")
    @CsvSource({
        "3024698E2CB1005678901234, 141674018641",
        // Add more test cases
    })
    public void parseEpcEan(final String rfidTag, final String expectedEan) throws EpcParseException {
        final SGTIN sgtin = Builder().withRFIDTag(rfidTag).build();
        assertEquals(expectedEan, sgtin.companyPrefix() + sgtin.itemReference());
    }

    @ParameterizedTest(name = "[{index}] RFID: {0} -> Filter: {1}")
    @CsvSource({
        "3024698E2CB1005678901234, 1",
        "3074257bf7194e4000001a85, 3",
        "303500C1C20044C80009FD8D8, 1"
    })
    public void parseEpcFilter(final String rfidTag, final String expectedFilter) throws EpcParseException {
        final SGTIN sgtin = Builder().withRFIDTag(rfidTag).build();
        assertEquals(expectedFilter, sgtin.filterValue());
    }

    @ParameterizedTest(name = "[{index}] RFID: {0} -> Partition: {1}")
    @CsvSource({
        "30285471BD5A0A5678901234, 2",
        "3024698E2CB1005678901234, 1"
    })
    public void parseEpcPartition(final String rfidTag, final String expectedPartition) throws EpcParseException {
        final SGTIN sgtin = Builder().withRFIDTag(rfidTag).build();
        assertEquals(expectedPartition, sgtin.partitionValue());
    }

    // Exception tests
    @ParameterizedTest(name = "[{index}] Invalid RFID: {0}")
    @ValueSource(strings = {
        "F45349FB11DF49FA935AB6FF",  // Not GS1
        "303C83F1B7DD441678901234",  // Wrong partition
        "30285471BD5A0A56789G1234"   // Non-hex character
    })
    public void whenInvalidRfidTag_thenExceptionIsRaised(final String invalidRfidTag) {
        assertThrows(
            IllegalArgumentException.class,
            () -> Builder().withRFIDTag(invalidRfidTag).build()
        );
    }

}
