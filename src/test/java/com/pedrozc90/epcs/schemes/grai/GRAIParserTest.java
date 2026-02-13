package com.pedrozc90.epcs.schemes.grai;

import com.pedrozc90.epcs.schemes.grai.enums.GRAIFilterValue;
import com.pedrozc90.epcs.schemes.grai.enums.GRAITagSize;
import com.pedrozc90.epcs.schemes.grai.objects.GRAI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GRAIParserTest {

    private static Stream<ExpectedData> provideData() {
        return Stream.of(
            new ExpectedData(
                "3374257BF4000200000004D2",
                "urn:epc:tag:grai-96:3.0614141.00008.1234",
                "urn:epc:id:grai:0614141.00008.1234",
                "grai",
                "96",
                "3",
                "7",
                "0614141",
                "00008",
                "1234",
                96
            ),
            new ExpectedData(
                "3376451FD40C0E400000162E",
                "urn:epc:tag:grai-96:3.9521141.12345.5678",
                "urn:epc:id:grai:9521141.12345.5678",
                "grai",
                "96",
                "3",
                "7",
                "9521141",
                "12345",
                "5678",
                96
            ),
            new ExpectedData(
                "3776451FD40C0E59B2C2BF1000000000000000000000",
                "urn:epc:tag:grai-170:3.9521141.12345.32a/b",
                "urn:epc:id:grai:9521141.12345.32a/b",
                "grai",
                "170",
                "3",
                "7",
                "9521141",
                "12345",
                "32a/b",
                176
            )
        );
    }

    @DisplayName("Encode")
    @ParameterizedTest(name = "[{index}] RFID Tag: {0}")
    @MethodSource("provideData")
    public void encode(final ExpectedData data) throws Exception {
        final GRAITagSize tagSize = GRAITagSize.of(Integer.parseInt(data.tagSize));
        final GRAIFilterValue filterValue = GRAIFilterValue.of(Integer.parseInt(data.filterValue));

        final GRAI result = GRAIParser.Builder()
            .withCompanyPrefix(data.companyPrefix)
            .withAssetType(data.assetType)
            .withSerial(data.serial)
            .withTagSize(tagSize)
            .withFilterValue(filterValue)
            .build();

        validate(data, result);
    }

    @DisplayName("Decode RFID Tag")
    @ParameterizedTest(name = "[{index}] RFID Tag: {0}")
    @MethodSource("provideData")
    public void decode_RFIDTag(final ExpectedData data) throws Exception {
        final GRAI result = GRAIParser.Builder()
            .withRFIDTag(data.rfidTag)
            .build();

        validate(data, result);
    }

    @DisplayName("Decode Epc Tag URI")
    @ParameterizedTest(name = "[{index}] Epc Tag URI: {0}")
    @MethodSource("provideData")
    public void decode_EpcTagURI(final ExpectedData data) throws Exception {
        final GRAI result = GRAIParser.Builder()
            .withEpcTagURI(data.epcTagURI)
            .build();

        validate(data, result);
    }

    @DisplayName("Decode Epc Tag URI")
    @ParameterizedTest(name = "[{index}] Epc Tag URI: {0}")
    @MethodSource("provideData")
    public void decode_EpcPureIdentityURI(final ExpectedData data) throws Exception {
        final GRAITagSize tagSize = GRAITagSize.of(Integer.parseInt(data.tagSize));
        final GRAIFilterValue filterValue = GRAIFilterValue.of(Integer.parseInt(data.filterValue));

        final GRAI result = GRAIParser.Builder()
            .withEpcPureIdentityURI(data.epcPureIdentityURI)
            .withTagSize(tagSize)
            .withFilterValue(filterValue)
            .build();

        validate(data, result);
    }

    private static void validate(ExpectedData data, GRAI result) {
        assertNotNull(result);
        assertEquals(data.rfidTag, result.getRfidTag());
        assertEquals(data.epcTagURI, result.getEpcTagURI());
        assertEquals(data.epcPureIdentityURI, result.getEpcPureIdentityURI());
        assertEquals(data.epcScheme, result.getEpcScheme());
        assertEquals(data.tagSize, result.getTagSize());
        assertEquals(data.filterValue, result.getFilterValue());
        assertEquals(data.prefixLength, result.getPrefixLength());
        assertEquals(data.companyPrefix, result.getCompanyPrefix());
        assertEquals(data.assetType, result.getAssetType());
        assertEquals(data.serial, result.getSerial());
        assertEquals(data.bitCount, result.getBinary().length());
    }

    protected record ExpectedData(
        String rfidTag,
        String epcTagURI,
        String epcPureIdentityURI,
        String epcScheme,
        String tagSize,
        String filterValue,
        String prefixLength,
        String companyPrefix,
        String assetType,
        String serial,
        Integer bitCount
    ) {

        @Override
        public String toString() {
            return rfidTag;
        }

    }

}
