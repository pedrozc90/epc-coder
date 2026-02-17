package com.pedrozc90.epcs.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilsTest {

    @ParameterizedTest
    @CsvSource(value = {
        "0, 0",
        "00, 0",
        "000, 0",
        "064141, 64141",
        "95060001343, 95060001343"
    })
    public void removeTrailingZeros(final String input, final String expected) {
        final String result = StringUtils.removeLeadingZeros(input);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideData() {
        return Stream.of(
            Arguments.arguments(0x2C, "0010 1100"),  // GDTI-96
            Arguments.arguments(0x2D, "0010 1101"),  // GSRN-96
            Arguments.arguments(0x2E, "0010 1110"),  // GSRNP-96
            Arguments.arguments(0x2F, "0010 1111"),  // USDoD-96
            Arguments.arguments(0x30, "0011 0000"),  // SGTIN-96
            Arguments.arguments(0x31, "0011 0001"),  // SSCC-96
            Arguments.arguments(0x32, "0011 0010"),  // SGLN-96
            Arguments.arguments(0x33, "0011 0011"),  // GRAI-96
            Arguments.arguments(0x34, "0011 0100"),  // GIAI-96
            Arguments.arguments(0x35, "0011 0101"),  // GID-96
            Arguments.arguments(0x36, "0011 0110"),  // SGTIN-198
            Arguments.arguments(0x37, "0011 0111"),  // GRAI-170
            Arguments.arguments(0x38, "0011 1000"),  // GIAI-202
            Arguments.arguments(0x39, "0011 1001")   // SGLN-195
        );
    }

    @ParameterizedTest(name = "[{index}] hex: {0} -> bin: {1}")
    @MethodSource("provideData")
    public void encodeHeader(final Integer header, final String expectedBin) {
        final String bin = BinaryUtils.encodeInteger(header, 8);
        assertEquals(expectedBin.replaceAll("\\s", ""), bin);
    }

}
