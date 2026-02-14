package com.pedrozc90.epcs.utils;

public class BinaryUtils {

    /**
     * Converts a hexadecimal string into a binary string.
     *
     * @param hex - Hexadecimal string to convert
     * @return Binary string with preserved leading zeros
     */
    public static String toBinary(final String hex) {
        if (hex == null || hex.isEmpty()) {
            return "";
        }

        final StringBuilder binary = new StringBuilder(hex.length() * 4);

        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            int value = hexCharToValue(c);

            // Convert to 4-bit binary
            binary.append((value & 8) != 0 ? '1' : '0');
            binary.append((value & 4) != 0 ? '1' : '0');
            binary.append((value & 2) != 0 ? '1' : '0');
            binary.append((value & 1) != 0 ? '1' : '0');
        }

        return binary.toString();
    }

    /**
     * Convert a binary string to a hexadecimal string
     *
     * @param binary - binary string to convert
     * @return hexadecimal string
     */
    public static String toHex(final String binary) {
        if (binary == null || binary.isEmpty()) {
            return "";
        }

        if (binary.length() % 4 != 0) {
            throw new IllegalArgumentException("Binary string length must be a multiple of 4, but got length: " + binary.length());
        }

        final StringBuilder hex = new StringBuilder(binary.length() / 4);

        for (int i = 0; i < binary.length(); i += 4) {
            int value = 0;

            for (int j = 0; j < 4; j++) {
                char c = binary.charAt(i + j);
                if (c == '1') {
                    value |= (1 << (3 - j));
                } else if (c != '0') {
                    throw new IllegalArgumentException("Invalid binary character: '%c'".formatted(c));
                }
            }

            hex.append(valueToHexChar(value));
        }

        return hex.toString();
    }

    /* --- Helpers --- */
    private static int hexCharToValue(final char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        } else {
            throw new IllegalArgumentException("Invalid hexadecimal character: '%c'".formatted(c));
        }
    }

    private static char valueToHexChar(final int value) {
        if (value < 10) {
            return (char) ('0' + value);
        } else {
            return (char) ('A' + value - 10);
        }
    }

}
