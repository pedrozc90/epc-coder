package com.pedrozc90.epcs.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 6-bit character encoding for EPC alphanumeric fields.
 * Based on GS1 EPC Tag Data Standard (Table 1.3.1-1).
 * Supports 40 characters: A-Z, 0-9, #, -, /
 */
public class Encoding6Bit {

    // All zeros terminator
    private static final String TERMINATOR = "000000";

    /**
     * Encoding table: Character → 6-bit binary string
     * Based on GS1 EPC Tag Data Standard
     */
    private static final String[] ENCODE_TABLE = new String[128];

    /**
     * Decoding table: 6-bit value → Character
     * Index 0-63 maps to characters
     */
    private static final char[] DECODE_TABLE = new char[64];

    private static final Map<Character, String> _escapes = new HashMap<>();

    static {
        addMapping('#', 0b100011, "%23"); // 35
        addMapping('-', 0b101101); // 45
        addMapping('/', 0b101111, "%2F"); // 47

        addMapping('0', 0b110000); // 48
        addMapping('1', 0b110001); // 49
        addMapping('2', 0b110010); // 50
        addMapping('3', 0b110011); // 51
        addMapping('4', 0b110100); // 52
        addMapping('5', 0b110101); // 53
        addMapping('6', 0b110110); // 54
        addMapping('7', 0b110111); // 55
        addMapping('8', 0b111000); // 56
        addMapping('9', 0b111001); // 57

        addMapping('A', 0b000001); // 1
        addMapping('B', 0b000010); // 2
        addMapping('C', 0b000011); // 3
        addMapping('D', 0b000100); // 4
        addMapping('E', 0b000101); // 5
        addMapping('F', 0b000110); // 6
        addMapping('G', 0b000111); // 7
        addMapping('H', 0b001000); // 8
        addMapping('I', 0b001001); // 9
        addMapping('J', 0b001010); // 10
        addMapping('K', 0b001011); // 11
        addMapping('L', 0b001100); // 12
        addMapping('M', 0b001101); // 13
        addMapping('N', 0b001110); // 14
        addMapping('O', 0b001111); // 15
        addMapping('P', 0b010000); // 16
        addMapping('Q', 0b010001); // 17
        addMapping('R', 0b010010); // 18
        addMapping('S', 0b010011); // 19
        addMapping('T', 0b010100); // 20
        addMapping('U', 0b010101); // 21
        addMapping('V', 0b010110); // 22
        addMapping('W', 0b010111); // 23
        addMapping('X', 0b011000); // 24
        addMapping('Y', 0b011001); // 25
        addMapping('Z', 0b011010); // 26

        _escapes.put('#', "%23");
        _escapes.put('/', "%2F");
    }

    private static void addMapping(final char character, final int bin, final String escape) {
        // Initialize decode table (6-bit value → character)
        DECODE_TABLE[bin] = character;
        // Initialize encode table (character → 6-bit binary string)
        ENCODE_TABLE[character] = to6BitString(bin);
        if (escape != null) {
            _escapes.put(character, escape);
        }
    }

    private static void addMapping(final char character, final int bin) {
        addMapping(character, bin, null);
    }

    private static String to6BitString(int v) {
        // Always 6 chars, left-padded with 0
        return String.format("%6s", Integer.toBinaryString(v)).replace(' ', '0');
    }

    /**
     * Encodes a string to 6-bit binary representation.
     * Each character becomes 6 bits according to GS1 standard.
     *
     * @param value - String to encode (e.g., "5PQ7/Z43")
     * @param bits  - Total bit count for output (padding length)
     * @return Binary string (each char = 6 bits)
     * @throws IllegalArgumentException if string contains unsupported characters
     */
    public static String encode(final String value, final int bits) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }

        final int length = value.length();
        final StringBuilder out = new StringBuilder(length * 6);

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);

            // Convert lowercase to uppercase
            if (c >= 'a' && c <= 'z') {
                c = (char) (c - 'a' + 'A');
            }

            // Get 6-bit encoding
            if (c >= ENCODE_TABLE.length || ENCODE_TABLE[c] == null) {
                throw new IllegalArgumentException("Character '%c' (ASCII %d) at position %d cannot be encoded in 6-bit format. Allowed characters: A-Z, 0-9, #, -, /".formatted(value.charAt(i), (int) value.charAt(i), i));
            }

            out.append(ENCODE_TABLE[c]);
        }

        return StringUtils.rightPad(out.toString(), bits, '0');
    }

    /**
     * Decodes a 6-bit binary string back to characters.
     *
     * @param binary - Binary string (length must be multiple of 6)
     * @return Decoded string
     * @throws IllegalArgumentException if binary length is invalid
     */
    public static String decode(final String binary) {
        if (binary == null || binary.isEmpty()) {
            return "";
        }

        final int length = binary.length();
        if (length % 6 != 0) {
            throw new IllegalArgumentException("Binary string length must be multiple of 6 for 6-bit decoding. Got: %d bits".formatted(length));
        }

        final StringBuilder out = new StringBuilder(length / 6);

        for (int i = 0; i < length; i += 6) {
            // Extract 6 bits
            int code = 0;
            for (int j = 0; j < 6; j++) {
                char bit = binary.charAt(i + j);
                if (bit != '0' && bit != '1') {
                    throw new IllegalArgumentException("Invalid binary character '%c' at position %d".formatted(bit, i + j));
                }
                code = (code << 1) | (bit - '0');
            }

            // Decode character
            if (code < 0 || code >= DECODE_TABLE.length || DECODE_TABLE[code] == '\0') {
                throw new IllegalArgumentException("Invalid 6-bit code: %s (decimal %d) at position %d".formatted(binary.substring(i, i + 6), code, i));
            }

            out.append(DECODE_TABLE[code]);
        }

        return out.toString();
    }

}
