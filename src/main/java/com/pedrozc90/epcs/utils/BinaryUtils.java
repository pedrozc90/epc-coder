package com.pedrozc90.epcs.utils;

import java.math.BigInteger;

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

        final int length = binary.length();
        if (length % 4 != 0) {
            throw new IllegalArgumentException("Binary string length must be a multiple of 4, but got length: " + length);
        }

        final StringBuilder hex = new StringBuilder(length / 4);

        for (int i = 0; i < length; i += 4) {
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

    /**
     * Converts a binary string to a decimal integer string with preserved leading zeros.
     *
     * @param binary    - binary string to be converted
     * @param minLength - minimum length of output (for leading zeros)
     * @return decimal representation as string with leading zeros, ex: "0614141"
     */
    public static String decodeInteger(final String binary, final int minLength) {
        if (binary == null || binary.isBlank()) {
            throw new IllegalArgumentException("Binary string cannot be null or empty");
        }

        // Validate binary string contains only 0s and 1s
        for (int i = 0; i < binary.length(); i++) {
            char c = binary.charAt(i);
            if (c != '0' && c != '1') {
                throw new IllegalArgumentException("Invalid binary character: '%c' at position %d".formatted(c, i));
            }
        }

        // Convert binary to decimal (automatically removes leading zeros)
        final String decimal = new BigInteger(binary, 2).toString();

        // Pad with leading zeros to reach minimum length
        return StringUtils.leftPad(decimal, minLength, '0');
    }

    /**
     * Decodes a binary string to a decimal integer string (GS1 Integer Decoding Method).
     *
     * <p>The decoding of this segment is a decimal numeral whose value is the value
     * of the input considered as an unsigned binary integer. The output shall not
     * begin with a zero character if it is two or more digits in length.</p>
     *
     * @param binary - binary string to be decoded
     * @return decimal integer string without leading zeros (unless value is "0")
     * @throws IllegalArgumentException if binary string is null, empty, or contains invalid characters
     */
    public static String decodeInteger(final String binary) {
        return decodeInteger(binary, 0);
    }

    /**
     * Encodes a decimal integer string to a binary string (GS1 Integer Encoding Method).
     *
     * <p>The encoding of this segment is a b-bit integer (padded to the left with
     * zero bits as necessary), where b is the value specified in the "Coding Segment
     * Bit Count" row of the encoding table.</p>
     *
     * @param value - decimal integer string (must not have leading zeros unless value is "0")
     * @param bits  - bit count for the binary output (padding length)
     * @return binary string padded to specified bit length
     * @throws IllegalArgumentException if value is null, empty, not numeric, or exceeds bit capacity
     */
    public static String encodeInteger(final String value, final int bits) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }

        // // Validate: leading zeros are not permitted (except for "0")
        // if (value.length() > 1 && value.startsWith("0")) {
        //     throw new IllegalArgumentException("Leading zeros are not permitted in integer encoding: '%s'".formatted(value));
        // }

        // Validate: must be numeric
        if (!value.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Value must be numeric: '%s'".formatted(value));
        }

        final BigInteger integer = new BigInteger(value);

        // Validate: value must fit in specified bit count
        final BigInteger maxValue = BigInteger.TWO.pow(bits).subtract(BigInteger.ONE);
        if (integer.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException("Value %s exceeds maximum for %d bits (max: %s)".formatted(value, bits, maxValue));
        }

        // Convert to binary and pad left with zeros
        final String binary = integer.toString(2);
        String t = "0".repeat(Math.max(0, bits - binary.length())) + binary;
        String s = StringUtils.leftPad(binary, bits, '0');
        return s;
    }

    /**
     * Encodes a long integer to a binary string (GS1 Integer Encoding Method).
     *
     * @param value - long integer value
     * @param bits  - bit count for the binary output (padding length)
     * @return binary string padded to specified bit length
     */
    public static String encodeInteger(final long value, final int bits) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative: " + value);
        }
        return encodeInteger(String.valueOf(value), bits);
    }

    /**
     * Encodes a long integer to a binary string (GS1 Integer Encoding Method).
     *
     * @param value - long integer value
     * @param bits  - bit count for the binary output (padding length)
     * @return binary string padded to specified bit length
     */
    public static String encodeInteger(final int value, final int bits) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative: " + value);
        }
        return encodeInteger(Integer.toString(value), bits);
    }

    /**
     * Decode a binary string to a alphanumeric string using GS1 String Decoding Method.
     *
     * @param binary - binary string to be decoded.
     * @param bits   - encoding bits, 6 bits or 7 bits
     * @return decoded alphanumeric string (may contain %XX escape sequence)
     * @throws IllegalArgumentException if validation fails
     */
    public static String decodeString(final String binary, final int bits) {
        return switch (bits) {
            case 6 -> Encoding6Bit.decode(binary);
            case 7 -> Encoding7Bit.decode(binary);
            default -> throw new IllegalArgumentException("Unsupported '%d' bit encoding".formatted(bits));
        };
    }

//    /**
//     * Decodes a binary string to an alphanumeric string using GS1 String Decoding Method.
//     *
//     * <p>The input bit string length is always a multiple of seven. Each 7-bit segment
//     * is translated into a single character or 3-character escape triplet by looking up
//     * the 7-bit segment in Table A-1.</p>
//     *
//     * <p>Decoding stops at the first all-zero segment (if any). All 7-bit segments
//     * following an all-zero segment must also be all zeros.</p>
//     *
//     * @param binary - binary string (length must be multiple of 7)
//     * @return decoded alphanumeric string (may contain %XX escape sequences)
//     * @throws IllegalArgumentException if validation fails
//     */
//    public static String decodeString(final String binary) {
//        if (binary == null || binary.isEmpty()) {
//            throw new IllegalArgumentException("Binary string cannot be null or empty");
//        }
//
//        // Validate: length must be multiple of 7
//        if (binary.length() % 7 != 0) {
//            throw new IllegalArgumentException(
//                "Binary string length must be a multiple of 7, but got length: " + binary.length()
//            );
//        }
//
//        final StringBuilder result = new StringBuilder();
//        boolean foundAllZero = false;
//        boolean hasNonZeroSegment = false;
//
//        for (int i = 0; i < binary.length(); i += 7) {
//            String segment = binary.substring(i, i + 7);
//
//            // Check for all-zero segment (terminator)
//            if (segment.equals("0000000")) {
//                foundAllZero = true;
//                // All remaining segments must also be all zeros - validate this
//                for (int j = i + 7; j < binary.length(); j += 7) {
//                    String remaining = binary.substring(j, j + 7);
//                    if (!remaining.equals("0000000")) {
//                        throw new IllegalArgumentException(
//                            "All 7-bit segments following an all-zero segment must also be all zeros"
//                        );
//                    }
//                }
//                break; // Stop decoding
//            }
//
//            // Validate: first segment must not be all zeros (string must contain at least one character)
//            if (i == 0 && segment.equals("0000000")) {
//                throw new IllegalArgumentException(
//                    "The first 7-bit segment must not be all zeros (string must contain at least one character)"
//                );
//            }
//
//            hasNonZeroSegment = true;
//
//            // Convert 7-bit segment to decimal
//            int value = Integer.parseInt(segment, 2);
//
//            // Determine if we need an escape sequence
//            // According to Table A-1, printable ASCII (32-127) can be used directly
//            // Non-printable characters (0-31) and DEL (127) should use escape sequences
//            if (value >= 32 && value <= 126) {
//                // Printable ASCII - use directly
//                result.append((char) value);
//            } else {
//                // Use escape sequence %XX
//                result.append(String.format("%%%02X", value));
//            }
//        }
//
//        return result.toString();
//    }

    /**
     * Encodes an alphanumeric string to a binary string using GS1 String Decoding Method.
     *
     * @param value    - binary string to be decoded.
     * @param bits     - total bit count for the binary output
     * @param encoding - encoding bits, 6 bits or 7 bits
     * @return decoded alphanumeric string (may contain %XX escape sequence)
     * @throws IllegalArgumentException if validation fails
     */
    public static String encodeString(final String value, final int bits, final int encoding) {
        return switch (encoding) {
            case 6 -> Encoding6Bit.encode(value, bits);
            case 7 -> Encoding7Bit.encode(value, bits);
            default -> throw new IllegalArgumentException("Unsupported '%d' bit encoding".formatted(encoding));
        };
    }

//    /**
//     * Encodes an alphanumeric string to binary using GS1 String Encoding Method.
//     *
//     * <p>The String encoding method is used for a segment that appears as an alphanumeric
//     * string in the URI, and as an ISO/IEC 646 [ISO646] (ASCII) encoded bit string in the
//     * binary encoding.</p>
//     *
//     * <p>Each character is encoded as a 7-bit string according to Table A-1. For escape
//     * sequences (%XX), the 7-bit value is the hexadecimal value. The output is padded to
//     * the right with zeros to total b bits, where b is the value specified in the
//     * "Coding Segment Bit Count" (padding bits = b - 7N).</p>
//     *
//     * @param value - alphanumeric string to encode (may contain %XX escape sequences)
//     * @param bits - total bit count for the binary output
//     * @return binary string padded to specified bit length
//     * @throws IllegalArgumentException if validation fails
//     */
//    public static String encodeString(final String value, final int bits) {
//        if (value == null) {
//            throw new IllegalArgumentException("Value cannot be null");
//        }
//
//        final StringBuilder binary = new StringBuilder();
//
//        int i = 0;
//        while (i < value.length()) {
//            char c = value.charAt(i);
//
//            // Handle escape sequences (%XX)
//            if (c == '%' && i + 2 < value.length()) {
//                String hexPart = value.substring(i + 1, i + 3);
//                try {
//                    int hexValue = Integer.parseInt(hexPart, 16);
//
//                    // Validate: must map to one of the 82 allowed characters (Table A-1)
//                    if (hexValue > 127) {
//                        throw new IllegalArgumentException("Invalid escape sequence %%%s: value must be 0-127".formatted(hexPart));
//                    }
//
//                    // Convert hex value to 7-bit binary
//                    String bits7 = Integer.toBinaryString(hexValue);
//                    binary.append("0".repeat(7 - bits7.length())).append(bits7);
//                    i += 3; // Skip %XX
//                } catch (NumberFormatException e) {
//                    throw new IllegalArgumentException("Invalid escape sequence %%%s: not valid hexadecimal".formatted(hexPart)                    );
//                }
//            } else {
//                // Single character - encode as 7-bit ASCII
//                int asciiValue = (int) c;
//
//                // Validate: must be valid ASCII (0-127)
//                if (asciiValue > 127) {
//                    throw new IllegalArgumentException(
//                        "Invalid character '%c': only ASCII characters (0-127) are allowed".formatted(c)
//                    );
//                }
//
//                // Convert to 7-bit binary
//                String bits7 = Integer.toBinaryString(asciiValue);
//                binary.append("0".repeat(7 - bits7.length())).append(bits7);
//                i++;
//            }
//        }
//
//        // Validate: number of characters must be <= b/7
//        int numCharacters = binary.length() / 7;
//        int maxCharacters = bits / 7;
//        if (numCharacters > maxCharacters) {
//            throw new IllegalArgumentException("String has %d characters but maximum for %d bits is %d".formatted(numCharacters, bits, maxCharacters));
//        }
//
//        // Pad to the right with zeros to reach total bit count
//        int paddingBits = bits - binary.length();
//        binary.append("0".repeat(paddingBits));
//
//        return binary.toString();
//    }

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
