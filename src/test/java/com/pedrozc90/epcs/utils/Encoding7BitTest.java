package com.pedrozc90.epcs.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Encoding7BitTest {

    @DisplayName(value = "Escape special characters from GS1 7-bit alphabet")
    @ParameterizedTest(name = "[{index}] value = {0}")
    @CsvSource(value = {
        "A, A",
        "AF, AF",
        "AF0, AF0",
        "AF0%, AF0%25",
        "AF0?, AF0%3F",
        "AF0?x&, AF0%3Fx%26",
        "AF0?x&%, AF0%3Fx%26%25",
    })
    public void escape(final String value, final String expected) {
        final String result = Encoding7Bit.escape(value);
        assertEquals(expected, result);
    }

    @DisplayName(value = "Normalize special characters from GS1 6-bit alphabet")
    @ParameterizedTest(name = "[{index}] value = {0}")
    @CsvSource(value = {
        "A, A",
        "AF, AF",
        "AF0, AF0",
        "AF0%25, AF0%",
        "AF0%3F, AF0?",
        "AF0%3Fx%26, AF0?x&",
        "AF0%3Fx%26%25, AF0?x&%"
    })
    public void normalize(final String value, final String expected) {
        final String result = Encoding7Bit.normalize(value);
        assertEquals(expected, result);
    }

}
