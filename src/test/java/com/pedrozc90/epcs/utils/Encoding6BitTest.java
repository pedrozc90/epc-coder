package com.pedrozc90.epcs.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Encoding6BitTest {

    @DisplayName(value = "Escape special characters from GS1 6-bit alphabet")
    @ParameterizedTest(name = "[{index}] value = {0}")
    @CsvSource(value = {
        "A, A",
        "AF, AF",
        "AF9, AF9",
        "AF9#, AF9%23",
        "AF9#/, AF9%23%2F"
    })
    public void escape(final String value, final String expected) {
        final String result = Encoding6Bit.escape(value);
        assertEquals(expected, result);
    }

    @DisplayName(value = "Normalize special characters from GS1 6-bit alphabet")
    @ParameterizedTest(name = "[{index}] value = {0}")
    @CsvSource(value = {
        "A, A",
        "AF, AF",
        "AF9, AF9",
        "AF9%23, AF9#",
        "AF9%23%2F, AF9#/"
    })
    public void normalize(final String value, final String expected) {
        final String result = Encoding6Bit.normalize(value);
        assertEquals(expected, result);
    }

}
