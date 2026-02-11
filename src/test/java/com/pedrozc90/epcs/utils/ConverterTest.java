package com.pedrozc90.epcs.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConverterTest {

    @DisplayName("Convert hexadecimal string into binary string")
    @ParameterizedTest(name = "[{index}] Hex to Bin: {0} = {1}")
    @CsvSource(
        value = {
            "30, 00110000",
            "3F0B, 0011111100001011",
            "3066C4409047E14000001A85, 001100000110011011000100010000001001000001000111111000010100000000000000000000000001101010000101",
        },
        delimiter = ','
    )
    public void hexToBin(final String value, final String expected) {
        final String result = Converter.hexToBin(value);
        assertEquals(expected, result);
    }

    @DisplayName("Convert binary string into hexadecimal string")
    @ParameterizedTest(name = "[{index}] Bin to Hex: {0} = {1}")
    @CsvSource(
        value = {
            "00110000, 30",
            "0011111100001011, 3F0B",
            "001100000110011011000100010000001001000001000111111000010100000000000000000000000001101010000101, 3066C4409047E14000001A85",
        },
        delimiter = ','
    )
    public void binToHex(final String value, final String expected) {
        final String result = Converter.binToHex(value);
        assertEquals(expected, result);
    }

}
