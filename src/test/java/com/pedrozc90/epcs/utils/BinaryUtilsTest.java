package com.pedrozc90.epcs.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryUtilsTest {

    @DisplayName("Hex to Binary and Binary to Hex")
    @ParameterizedTest(name = "[{index}] Hex = {0}")
    @ValueSource(strings = {
        "3066C4409047E140075BCD15",
        "3666C4409047E159B2C2BF100000000000000000000000000000",
        "FD3795211411234538566CB0AFC525065F1876F0D996D800",
        "FC342CDE795211411234538566CB0AFC525065F1876F0D996D80",
        "311BA1B300CE0A6A83000000",
        "F90095201234567891235",
        "F9009520123456789123592832F8C3B786CCB6C0",
        "3276451FD46072000000162E",
        "3976451FD46072CD9615F8800000000000000000000000000000",
        "F2395211411234548566CB0AFC4",
        "E9395211411234548566CB0AFC525065F1876F0D996D8000",
        "3376451FD40C0E400000162E",
        "3776451FD40C0E59B2C2BF1000000000000000000000",
        "F13095211411234548566CB0AFC4",
        "EB3095211411234548566CB0AFC525065F1876F0D996D800",
        "3476451FD40000000000162E",
        "3876451FD59B2C2BF10000000000000000000000000000000000",
        "FA3952114132E83C2BF10",
        "EE3952114132E83C2BF1494197C61DBC3665B600",
        "2D76451FD4499602D2000000",
        "F43952114112345678906",
        "E7395211411234567890692832F8C3B786CCB6C0",
        "F53952114112345678906",
        "E8395211411234567890692832F8C3B786CCB6C0",
        "2C76451FD46072000000162E",
        "3E76451FD7039B061438997367D0C18B266D1AB66EE0",
        "F6395211411234540458B8",
        "EA395211411234540458BA4A0CBE30EDE1B32DB0",
        "3C76451FD400C0E680003039",
        "3D76451FD75411DEF6B4CC00000003039000",
        "F0395211415E87A145BAFB4D19A8C0E4",
        "E6395211415E87A145BAFB4D19A8C0E64A0CBE30EDE1B32DB000",
        "3F76451FD612640000019907",
        "F839521141678909509338",
        "EC3952114167890950933C94197C61DBC3665B60",
        "3500E86F8000A9E000000586",
        "3B0E0CF5E76C9047759AD00373DC7602E7200",
        "4076451FD40C0E40820000000F54",
        "4176451FD40C0E4082DBDD8B36600000000000000000000000000000",
        "F3309521141123454010266AE27FDF35",
        "F3309521141123454010266AE27FDF3592832F8C3B786CCB6C00"
    })
    public void conversion(final String hex) {
        final String bin = BinaryUtils.toBinary(hex);
        assertNotNull(bin);
        assertEquals(0, bin.length() % 4);

        final String out = BinaryUtils.toHex(bin);
        assertNotNull(out);

        assertEquals(hex, out);
    }

    @DisplayName("Convert a binary string into a integer string")
    @ParameterizedTest(name = "[{index}] bin: {0} -> value: {1}")
    @CsvSource(value = {
        "000010010101111011111101, 0614141",
        "1011000100010000001001000001000111111, 95060001343"
    })
    public void convertBinaryToInteger(final String bin, final String expected) {
        final String num = BinaryUtils.decodeInteger(bin, expected.length());
        assertEquals(expected, num);

        final String out = BinaryUtils.encodeInteger(num, bin.length());
        assertEquals(bin, out);
    }

    @DisplayName("Convert a number into a n bit binary")
    @ParameterizedTest(name = "[{index}] num: {0}")
    @CsvSource(value = {
        "614141, 24, 000010010101111011111101",
        "95060001343, 37, 1011000100010000001001000001000111111"
    })
    public void decodeIntegerToBinary(final Long number, final int length, final String expected) {
        final String bin = BinaryUtils.encodeInteger(number, length);
        assertTrue(bin.matches("[0-1]+"));
        assertEquals(expected, bin);
        assertEquals(length, bin.length());

        final String s = BinaryUtils.decodeInteger(bin);
        assertEquals(Long.toString(number), s);
    }

    @DisplayName("Convert a binary string into a string")
    @ParameterizedTest(name = "[{index}] bin: {0} -> value: {1}")
    @CsvSource(value = {
        "000010010101111011111101, 0614141",
        "1011000100010000001001000001000111111, 95060001343"
    })
    public void convertBinaryToString(final String bin, final String expected) {
        final String result = BinaryUtils.decodeInteger(bin, expected.length());
        assertEquals(expected, result);
    }

    @DisplayName("Convert a string into a n bit binary")
    @ParameterizedTest(name = "[{index}] num: {0}")
    @CsvSource(value = {
        "A, 28, 1000001000000000000000000000",
        "ABC, 28, 1000001100001010000110000000",
        "A%20B, 35, 10000010100000100001000000000000000",
        "32a/b, 40, 0110011011001011000010101111110001000000",
        "32a%2Fb, 40, 0110011011001011000010101111110001000000"
    })
    public void encodeStringToBinary(final String value, final int length, final String expected) {
        final String result = BinaryUtils.encodeString(value, length, 7);
        assertEquals(expected, result);
        assertEquals(length, result.length());
    }

    @DisplayName("Convert a string into a n bit binary")
    @ParameterizedTest(name = "[{index}] num: {0}")
    @CsvSource(value = {
        "A B, 35, 10000010100000100001000000000000000",
    })
    public void encodeStringWithInvalidCharacterToBinary(final String value, final int length, final String expected) {
        final IllegalArgumentException cause = assertThrows(
            IllegalArgumentException.class,
            () -> BinaryUtils.encodeString(value, length, 7)
        );
        assertEquals("Invalid character: ' ' (not in GS1 character set)", cause.getMessage());
    }

}
