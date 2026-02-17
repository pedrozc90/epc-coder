package com.pedrozc90.epcs.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    private static final int PAD_LIMIT = 8192;
    private static final String SPACE = " ";

    /**
     * Split a string into chunks of a given size.
     *
     * @param value - the string to be split
     * @param size  - chunk size
     * @return list of chunks
     */
    public static List<String> chunk(final String value, final int size) {
        if (value == null || value.isEmpty()) return List.of();

        if (size <= 0) throw new IllegalArgumentException("Chunk size must be greater than zero");

        final int len = value.length();
        if (size >= len) return List.of(value);

        final List<String> out = new ArrayList<>((len + size - 1) / size);

        for (int i = 0; i < len; i += size) {
            final String sub = value.substring(i, Math.min(len, i + size));
            out.add(sub);
        }

        return out;
    }

    /**
     * Left pad a String with a specified String.
     *
     * <p>
     * Pad to a size of {@code size}.
     * </p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)      = null
     * StringUtils.leftPad("", 3, "z")      = "zzz"
     * StringUtils.leftPad("bat", 3, "yz")  = "bat"
     * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
     * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
     * StringUtils.leftPad("bat", 1, "yz")  = "bat"
     * StringUtils.leftPad("bat", -1, "yz") = "bat"
     * StringUtils.leftPad("bat", 5, null)  = "  bat"
     * StringUtils.leftPad("bat", 5, "")    = "  bat"
     * </pre>
     *
     * @param value  - the string to pad out, may be null
     * @param size   - the size to pad to
     * @param padStr - the string to pad with, null or empty treated as a single space.
     * @return left padded String or original String if no padding is necessary, {@code null} if null String input.
     */
    public static String leftPad(final String value, final int size, String padStr) {
        if (value == null) return null;

        if (padStr == null || padStr.isEmpty()) {
            padStr = SPACE;
        }

        final int padLen = padStr.length();
        final int strLen = value.length();
        final int pads = size - strLen;

        // returns original string
        if (pads <= 0) {
            return value;
        }

        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(value, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(value);
        }

        if (pads < padLen) {
            return padStr.substring(0, pads).concat(value);
        }

        final char[] padding = new char[pads];
        final char[] padChars = padStr.toCharArray();
        for (int i = 0; i < pads; i++) {
            padding[i] = padChars[i % padLen];
        }

        return new String(padding).concat(value);
    }

    /**
     * Left pad a String with a specified character.
     *
     * <p>
     * Pad to a size of {@code size}.
     * </p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)     = null
     * StringUtils.leftPad("", 3, 'z')     = "zzz"
     * StringUtils.leftPad("bat", 3, 'z')  = "bat"
     * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
     * StringUtils.leftPad("bat", 1, 'z')  = "bat"
     * StringUtils.leftPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param value   the String to pad out, may be null.
     * @param size    the size to pad to.
     * @param padChar the character to pad with.
     * @return left padded String or original String if no padding is necessary, {@code null} if null String input.
     * @since 2.0
     */
    public static String leftPad(final String value, final int size, final char padChar) {
        if (value == null) return null;

        final int pads = size - value.length();

        // returns original string
        if (pads <= 0) {
            return value;
        }

        final String padStr = Character.toString(padChar);
        if (pads > PAD_LIMIT) {
            return leftPad(value, size, padStr);
        }

        return padStr.repeat(pads).concat(value);
    }

    /**
     * Right pad a String with a specified String.
     *
     * <p>
     * The String is padded to the size of {@code size}.
     * </p>
     *
     * <pre>
     * StringUtils.rightPad(null, *, *)      = null
     * StringUtils.rightPad("", 3, "z")      = "zzz"
     * StringUtils.rightPad("bat", 3, "yz")  = "bat"
     * StringUtils.rightPad("bat", 5, "yz")  = "batyz"
     * StringUtils.rightPad("bat", 8, "yz")  = "batyzyzy"
     * StringUtils.rightPad("bat", 1, "yz")  = "bat"
     * StringUtils.rightPad("bat", -1, "yz") = "bat"
     * StringUtils.rightPad("bat", 5, null)  = "bat  "
     * StringUtils.rightPad("bat", 5, "")    = "bat  "
     * </pre>
     *
     * @param str    the String to pad out, may be null.
     * @param size   the size to pad to.
     * @param padStr the String to pad with, null or empty treated as single space.
     * @return right padded String or original String if no padding is necessary, {@code null} if null String input.
     */
    public static String rightPad(final String str, final int size, String padStr) {
        if (str == null) return null;

        if (padStr == null || padStr.isEmpty()) {
            padStr = SPACE;
        }

        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;

        // returns original string
        if (pads <= 0) {
            return str;
        }

        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        }

        if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        }

        final char[] padding = new char[pads];
        final char[] padChars = padStr.toCharArray();
        for (int i = 0; i < pads; i++) {
            padding[i] = padChars[i % padLen];
        }

        return str.concat(new String(padding));
    }

    /**
     * Right pad a String with a specified character.
     *
     * <p>
     * The String is padded to the size of {@code size}.
     * </p>
     *
     * <pre>
     * StringUtils.rightPad(null, *, *)     = null
     * StringUtils.rightPad("", 3, 'z')     = "zzz"
     * StringUtils.rightPad("bat", 3, 'z')  = "bat"
     * StringUtils.rightPad("bat", 5, 'z')  = "batzz"
     * StringUtils.rightPad("bat", 1, 'z')  = "bat"
     * StringUtils.rightPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param value   the String to pad out, may be null.
     * @param size    the size to pad to.
     * @param padChar the character to pad with.
     * @return right padded String or original String if no padding is necessary, {@code null} if null String input.
     * @since 2.0
     */
    public static String rightPad(final String value, final int size, final char padChar) {
        if (value == null) return null;

        final int pads = size - value.length();

        // returns original string
        if (pads <= 0) {
            return value;
        }

        final String padStr = String.valueOf(padChar);
        if (pads > PAD_LIMIT) {
            return rightPad(value, size, padStr);
        }
        return value.concat(padStr.repeat(pads));
    }

    /**
     * Removes leading zeros from a numeric string.
     * Returns "0" if the string is all zeros or empty.
     *
     * @param str - numeric string with potential leading zeros
     * @return string with leading zeros removed, or "0" if all zeros
     */
    public static String removeLeadingZeros(final String str) {
        if (str == null || str.isEmpty()) {
            return "0";
        }

        int start = 0;
        int length = str.length();

        // Find first non-zero character
        while (start < length && str.charAt(start) == '0') {
            start++;
        }

        // If all zeros, return "0"
        return (start == length) ? "0" : str.substring(start);
    }

    /**
     * Removes trailing zeros from a string.
     * Returns "" if the string is all zeros.
     *
     * @param str - string with potential trailing zeros
     * @return string with trailing zeros removed
     */
    public static String removeTrailingZeros(final String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        int end = str.length() - 1;
        while (end >= 0 && str.charAt(end) == '0') {
            end--;
        }

        return (end < 0) ? "" : str.substring(0, end + 1);
    }

}
