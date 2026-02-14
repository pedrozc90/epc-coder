package com.pedrozc90.epcs.utils;

/**
 * 6-bit character encoding for EPC alphanumeric fields.
 * Based on GS1 EPC Tag Data Standard (Table 1.3.1-1).
 * Supports 40 characters: A-Z, 0-9, #, -, /
 */
public class Encoding6Bit {

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

    static {
        // Initialize decode table (6-bit value → character)
        DECODE_TABLE[0b100011] = '#';  // 35
        DECODE_TABLE[0b101101] = '-';  // 45
        DECODE_TABLE[0b101111] = '/';  // 47

        DECODE_TABLE[0b110000] = '0';  // 48
        DECODE_TABLE[0b110001] = '1';  // 49
        DECODE_TABLE[0b110010] = '2';  // 50
        DECODE_TABLE[0b110011] = '3';  // 51
        DECODE_TABLE[0b110100] = '4';  // 52
        DECODE_TABLE[0b110101] = '5';  // 53
        DECODE_TABLE[0b110110] = '6';  // 54
        DECODE_TABLE[0b110111] = '7';  // 55
        DECODE_TABLE[0b111000] = '8';  // 56
        DECODE_TABLE[0b111001] = '9';  // 57

        DECODE_TABLE[0b000001] = 'A';  // 1
        DECODE_TABLE[0b000010] = 'B';  // 2
        DECODE_TABLE[0b000011] = 'C';  // 3
        DECODE_TABLE[0b000100] = 'D';  // 4
        DECODE_TABLE[0b000101] = 'E';  // 5
        DECODE_TABLE[0b000110] = 'F';  // 6
        DECODE_TABLE[0b000111] = 'G';  // 7
        DECODE_TABLE[0b001000] = 'H';  // 8
        DECODE_TABLE[0b001001] = 'I';  // 9
        DECODE_TABLE[0b001010] = 'J';  // 10
        DECODE_TABLE[0b001011] = 'K';  // 11
        DECODE_TABLE[0b001100] = 'L';  // 12
        DECODE_TABLE[0b001101] = 'M';  // 13
        DECODE_TABLE[0b001110] = 'N';  // 14
        DECODE_TABLE[0b001111] = 'O';  // 15
        DECODE_TABLE[0b010000] = 'P';  // 16
        DECODE_TABLE[0b010001] = 'Q';  // 17
        DECODE_TABLE[0b010010] = 'R';  // 18
        DECODE_TABLE[0b010011] = 'S';  // 19
        DECODE_TABLE[0b010100] = 'T';  // 20
        DECODE_TABLE[0b010101] = 'U';  // 21
        DECODE_TABLE[0b010110] = 'V';  // 22
        DECODE_TABLE[0b010111] = 'W';  // 23
        DECODE_TABLE[0b011000] = 'X';  // 24
        DECODE_TABLE[0b011001] = 'Y';  // 25
        DECODE_TABLE[0b011010] = 'Z';  // 26

        // Initialize encode table (character → 6-bit binary string)
        ENCODE_TABLE['#'] = "100011";
        ENCODE_TABLE['-'] = "101101";
        ENCODE_TABLE['/'] = "101111";

        ENCODE_TABLE['0'] = "110000";
        ENCODE_TABLE['1'] = "110001";
        ENCODE_TABLE['2'] = "110010";
        ENCODE_TABLE['3'] = "110011";
        ENCODE_TABLE['4'] = "110100";
        ENCODE_TABLE['5'] = "110101";
        ENCODE_TABLE['6'] = "110110";
        ENCODE_TABLE['7'] = "110111";
        ENCODE_TABLE['8'] = "111000";
        ENCODE_TABLE['9'] = "111001";

        ENCODE_TABLE['A'] = "000001";
        ENCODE_TABLE['B'] = "000010";
        ENCODE_TABLE['C'] = "000011";
        ENCODE_TABLE['D'] = "000100";
        ENCODE_TABLE['E'] = "000101";
        ENCODE_TABLE['F'] = "000110";
        ENCODE_TABLE['G'] = "000111";
        ENCODE_TABLE['H'] = "001000";
        ENCODE_TABLE['I'] = "001001";
        ENCODE_TABLE['J'] = "001010";
        ENCODE_TABLE['K'] = "001011";
        ENCODE_TABLE['L'] = "001100";
        ENCODE_TABLE['M'] = "001101";
        ENCODE_TABLE['N'] = "001110";
        ENCODE_TABLE['O'] = "001111";
        ENCODE_TABLE['P'] = "010000";
        ENCODE_TABLE['Q'] = "010001";
        ENCODE_TABLE['R'] = "010010";
        ENCODE_TABLE['S'] = "010011";
        ENCODE_TABLE['T'] = "010100";
        ENCODE_TABLE['U'] = "010101";
        ENCODE_TABLE['V'] = "010110";
        ENCODE_TABLE['W'] = "010111";
        ENCODE_TABLE['X'] = "011000";
        ENCODE_TABLE['Y'] = "011001";
        ENCODE_TABLE['Z'] = "011010";
    }

    /**
     * Encodes a string to 6-bit binary representation.
     * Each character becomes 6 bits according to GS1 standard.
     *
     * @param value - String to encode (e.g., "5PQ7/Z43")
     * @return Binary string (each char = 6 bits)
     * @throws IllegalArgumentException if string contains unsupported characters
     */
    public static String encode(final String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        final StringBuilder binary = new StringBuilder(value.length() * 6);

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            // Convert lowercase to uppercase
            if (c >= 'a' && c <= 'z') {
                c = (char) (c - 'a' + 'A');
            }

            // Get 6-bit encoding
            if (c >= ENCODE_TABLE.length || ENCODE_TABLE[c] == null) {
                throw new IllegalArgumentException(
                    "Character '%c' (ASCII %d) at position %d cannot be encoded in 6-bit format. " +
                        "Allowed characters: A-Z, 0-9, #, -, /"
                            .formatted(value.charAt(i), (int) value.charAt(i), i)
                );
            }

            binary.append(ENCODE_TABLE[c]);
        }

        return binary.toString();
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

        if (binary.length() % 6 != 0) {
            throw new IllegalArgumentException(
                "Binary string length must be multiple of 6 for 6-bit decoding. Got: %d bits"
                    .formatted(binary.length())
            );
        }

        final StringBuilder result = new StringBuilder(binary.length() / 6);

        for (int i = 0; i < binary.length(); i += 6) {
            // Extract 6 bits
            int code = 0;
            for (int j = 0; j < 6; j++) {
                char bit = binary.charAt(i + j);
                if (bit != '0' && bit != '1') {
                    throw new IllegalArgumentException(
                        "Invalid binary character '%c' at position %d"
                            .formatted(bit, i + j)
                    );
                }
                code = (code << 1) | (bit - '0');
            }

            // Decode character
            if (code < 0 || code >= DECODE_TABLE.length || DECODE_TABLE[code] == '\0') {
                throw new IllegalArgumentException(
                    "Invalid 6-bit code: %s (decimal %d) at position %d"
                        .formatted(binary.substring(i, i + 6), code, i)
                );
            }

            result.append(DECODE_TABLE[code]);
        }

        return result.toString();
    }

}
