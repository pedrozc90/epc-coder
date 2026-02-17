package com.pedrozc90.epcs.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 7-bit encoding/decoding for GS1 EPC Tag Data as per GS1 specification.
 * Uses ISO/IEC 646 (ASCII) character set for alphanumeric serial numbers.
 */
public class Encoding7Bit {

    // All zeros terminator
    private static final String TERMINATOR = "0000000";

    // Character to 7-bit binary string mapping (based on GS1 Table A-1)
    private static final Map<Character, String> CHAR_TO_BINARY = new HashMap<>();

    // 7-bit binary string to character mapping
    private static final Map<String, Character> BINARY_TO_CHAR = new HashMap<>();

    static {
        // Initialize the character mapping table based on GS1 specification
        // Special characters
        addMapping('!', "0100001");  // 0x21 - Exclamation Mark
        addMapping('"', "0100010");  // 0x22 - Quotation Mark
        addMapping('%', "0100101");  // 0x25 - Percent Sign
        addMapping('&', "0100110");  // 0x26 - Ampersand
        addMapping('\'', "0100111"); // 0x27 - Apostrophe
        addMapping('(', "0101000");  // 0x28 - Left Parenthesis
        addMapping(')', "0101001");  // 0x29 - Right Parenthesis
        addMapping('*', "0101010");  // 0x2A - Asterisk
        addMapping('+', "0101011");  // 0x2B - Plus sign
        addMapping(',', "0101100");  // 0x2C - Comma
        addMapping('-', "0101101");  // 0x2D - Hyphen/Minus
        addMapping('.', "0101110");  // 0x2E - Full Stop
        addMapping('/', "0101111");  // 0x2F - Solidus

        // Digits 0-9
        addMapping('0', "0110000");  // 0x30
        addMapping('1', "0110001");  // 0x31
        addMapping('2', "0110010");  // 0x32
        addMapping('3', "0110011");  // 0x33
        addMapping('4', "0110100");  // 0x34
        addMapping('5', "0110101");  // 0x35
        addMapping('6', "0110110");  // 0x36
        addMapping('7', "0110111");  // 0x37
        addMapping('8', "0111000");  // 0x38
        addMapping('9', "0111001");  // 0x39

        // More special characters
        addMapping(':', "0111010");  // 0x3A - Colon
        addMapping(';', "0111011");  // 0x3B - Semicolon
        addMapping('<', "0111100");  // 0x3C - Less-than Sign
        addMapping('=', "0111101");  // 0x3D - Equals Sign
        addMapping('>', "0111110");  // 0x3E - Greater-than Sign
        addMapping('?', "0111111");  // 0x3F - Question Mark

        // Capital Letters A-Z
        addMapping('A', "1000001");  // 0x41
        addMapping('B', "1000010");  // 0x42
        addMapping('C', "1000011");  // 0x43
        addMapping('D', "1000100");  // 0x44
        addMapping('E', "1000101");  // 0x45
        addMapping('F', "1000110");  // 0x46
        addMapping('G', "1000111");  // 0x47
        addMapping('H', "1001000");  // 0x48
        addMapping('I', "1001001");  // 0x49
        addMapping('J', "1001010");  // 0x4A
        addMapping('K', "1001011");  // 0x4B
        addMapping('L', "1001100");  // 0x4C
        addMapping('M', "1001101");  // 0x4D
        addMapping('N', "1001110");  // 0x4E
        addMapping('O', "1001111");  // 0x4F
        addMapping('P', "1010000");  // 0x50
        addMapping('Q', "1010001");  // 0x51
        addMapping('R', "1010010");  // 0x52
        addMapping('S', "1010011");  // 0x53
        addMapping('T', "1010100");  // 0x54
        addMapping('U', "1010101");  // 0x55
        addMapping('V', "1010110");  // 0x56
        addMapping('W', "1010111");  // 0x57
        addMapping('X', "1011000");  // 0x58
        addMapping('Y', "1011001");  // 0x59
        addMapping('Z', "1011010");  // 0x5A

        // Underscore
        addMapping('_', "1011111");  // 0x5F

        // Small Letters a-z
        addMapping('a', "1100001");  // 0x61
        addMapping('b', "1100010");  // 0x62
        addMapping('c', "1100011");  // 0x63
        addMapping('d', "1100100");  // 0x64
        addMapping('e', "1100101");  // 0x65
        addMapping('f', "1100110");  // 0x66
        addMapping('g', "1100111");  // 0x67
        addMapping('h', "1101000");  // 0x68
        addMapping('i', "1101001");  // 0x69
        addMapping('j', "1101010");  // 0x6A
        addMapping('k', "1101011");  // 0x6B
        addMapping('l', "1101100");  // 0x6C
        addMapping('m', "1101101");  // 0x6D
        addMapping('n', "1101110");  // 0x6E
        addMapping('o', "1101111");  // 0x6F
        addMapping('p', "1110000");  // 0x70
        addMapping('q', "1110001");  // 0x71
        addMapping('r', "1110010");  // 0x72
        addMapping('s', "1110011");  // 0x73
        addMapping('t', "1110100");  // 0x74
        addMapping('u', "1110101");  // 0x75
        addMapping('v', "1110110");  // 0x76
        addMapping('w', "1110111");  // 0x77
        addMapping('x', "1111000");  // 0x78
        addMapping('y', "1111001");  // 0x79
        addMapping('z', "1111010");  // 0x7A
    }

    private static void addMapping(final char character, final String binary) {
        if (binary.length() != 7) {
            throw new IllegalArgumentException("Binary value must be exactly 7 bits");
        }
        CHAR_TO_BINARY.put(character, binary);
        BINARY_TO_CHAR.put(binary, character);
    }

    /**
     * Encodes a string using 7-bit ASCII encoding (GS1 String Encoding Method).
     * Each character is encoded as a 7-bit value. Output is padded to the right with zeros.
     *
     * @param value - alphanumeric string (ASCII characters)
     * @param bits  - total bit count for output
     * @return binary string padded to specified bit length
     */
    public static String encode(final String value, final int bits) {
        if (value == null || value.isEmpty()) {
            // throw new IllegalArgumentException("Input string cannot be null or empty");
            // all zeros for empty string
            return "0".repeat(bits);
        }

        final StringBuilder out = new StringBuilder(value.length() * 7);

        final int length = value.length();
        for (int i = 0; i < length; i++) {
            final char c = value.charAt(i);
            final String bin = CHAR_TO_BINARY.get(c);
            if (bin == null) {
                throw new IllegalArgumentException("Invalid character: '%c' (not in GS1 character set)".formatted(c));
            }
            out.append(bin);
        }

        // Validate: total bits must fit
        if (value.length() > bits) {
            throw new IllegalArgumentException("String '%s' requires %d bits but only %d bits available".formatted(value, length, bits));
        }

        return StringUtils.rightPad(out.toString(), bits, '0');
    }

    /**
     * Decodes a 7-bit binary string back to the original text.
     * Handles partial segments by treating trailing bits as zeros.
     * <p>
     * Decodes a 7-bit ASCII encoded binary string.
     * Stops at first all-zero segment (0000000).
     *
     * @param value - The binary string to decode
     * @return Decoded string
     * @throws IllegalArgumentException if binary string is invalid
     */
    public static String decode(final String value) {
        if (value == null || value.isEmpty()) {
            // throw new IllegalArgumentException("Binary string cannot be null or empty");
            return "";
        }

        // Remove trailing zeros (padding)
        final String trimmed = StringUtils.removeTrailingZeros(value);
        if (trimmed.isEmpty()) {
            return "";
        }

        // Pad to make multiple of 7
        final int remainder = trimmed.length() % 7;

        final String padded = (remainder != 0)
            ? trimmed + "0".repeat(7 - remainder)
            : trimmed;


        final StringBuilder out = new StringBuilder();

        final int length = padded.length();
        for (int i = 0; i < length; i += 7) {
            final String segment = value.substring(i, i + 7);
            if (segment.equals(TERMINATOR)) {
                break;
            }

            // Look up character
            final Character character = BINARY_TO_CHAR.get(segment);
            if (character == null) {
                throw new IllegalArgumentException("Invalid 7-bit value: '%s' at position '%d'".formatted(segment, i));
            }

            out.append(character);
        }

        return out.toString();
    }

}
