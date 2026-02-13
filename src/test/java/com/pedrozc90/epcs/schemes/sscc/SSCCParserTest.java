package com.pedrozc90.epcs.schemes.sscc;

import com.pedrozc90.epcs.schemes.sscc.enums.SSCCExtensionDigit;
import com.pedrozc90.epcs.schemes.sscc.enums.SSCCFilterValue;
import com.pedrozc90.epcs.schemes.sscc.enums.SSCCTagSize;
import com.pedrozc90.epcs.schemes.sscc.objects.SSCC;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SSCCParserTest {

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(
                "311BA1B300CE0A6A83000000",
                "urn:epc:tag:sscc-96:0.952012.03456789123",
                "urn:epc:id:sscc:952012.03456789123",
                "sscc",
                "96",
                "0",
                "6",
                "952012",
                "6",
                "5",
                "0",
                "3456789123",
                96
            ),
            Arguments.arguments(
                "31AC16465751CCD0C2000000",
                "urn:epc:tag:sscc-96:5.023356789.30200002",
                "urn:epc:id:sscc:023356789.30200002",
                "sscc",
                "96",
                "5",
                "9",
                "023356789",
                "3",
                "2",
                "3",
                "0200002",
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
        final String expectedPartitionValue,
        final String expectedCheckDigit,
        final String expectedExtensionDigit,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final SSCCTagSize tagSize = SSCCTagSize.of(Integer.parseInt(expectedTagSize));
        final SSCCFilterValue filterValue = SSCCFilterValue.of(Integer.parseInt(expectedFilterValue));
        final SSCCExtensionDigit extensionDigit = SSCCExtensionDigit.of(Integer.parseInt(expectedExtensionDigit));

        final SSCC result = SSCCParser.Builder()
            .withCompanyPrefix(expectedCompanyPrefix)
            .withExtensionDigit(extensionDigit)
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
        assertEquals(expectedPartitionValue, result.getPartitionValue());
        assertEquals(expectedCheckDigit, result.getCheckDigit());
        assertEquals(expectedExtensionDigit, result.getExtensionDigit());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @DisplayName("Decode RFIDTag")
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
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final SSCC result = SSCCParser.Builder()
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
        assertEquals(expectedPartitionValue, result.getPartitionValue());
        assertEquals(expectedCheckDigit, result.getCheckDigit());
        assertEquals(expectedExtensionDigit, result.getExtensionDigit());
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
        final String expectedPartitionValue,
        final String expectedCheckDigit,
        final String expectedExtensionDigit,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final SSCC result = SSCCParser.Builder()
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
        assertEquals(expectedPartitionValue, result.getPartitionValue());
        assertEquals(expectedCheckDigit, result.getCheckDigit());
        assertEquals(expectedExtensionDigit, result.getExtensionDigit());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

    @DisplayName("Decode Epc Pure Identity URI")
    @ParameterizedTest(name = "[{index}] Epc Pure Identity URI: {1}")
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
        final String expectedPartitionValue,
        final String expectedCheckDigit,
        final String expectedExtensionDigit,
        final String expectedSerial,
        final Integer expectedBitCount
    ) throws Exception {
        final SSCCTagSize tagSize = SSCCTagSize.of(Integer.parseInt(expectedTagSize));
        final SSCCFilterValue filterValue = SSCCFilterValue.of(Integer.parseInt(expectedFilterValue));

        final SSCC result = SSCCParser.Builder()
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
        assertEquals(expectedPartitionValue, result.getPartitionValue());
        assertEquals(expectedCheckDigit, result.getCheckDigit());
        assertEquals(expectedExtensionDigit, result.getExtensionDigit());
        assertEquals(expectedSerial, result.getSerial());
        assertEquals(expectedBitCount, result.getBinary().length());
    }

}
