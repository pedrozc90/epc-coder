package com.pedrozc90.epcs.utils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class Converter {

    private static final Map<Character, String> _binary;
    private static final Map<String, String> _hexadecimal;
    private static final Map<Character, String> _gs1_6bits;

    static {
        final Map<Character, String> binary = new HashMap<>();
        binary.put('0', "0000");
        binary.put('1', "0001");
        binary.put('2', "0010");
        binary.put('3', "0011");
        binary.put('4', "0100");
        binary.put('5', "0101");
        binary.put('6', "0110");
        binary.put('7', "0111");
        binary.put('8', "1000");
        binary.put('9', "1001");
        binary.put('a', "1010");
        binary.put('A', "1010");
        binary.put('b', "1011");
        binary.put('B', "1011");
        binary.put('c', "1100");
        binary.put('C', "1100");
        binary.put('d', "1101");
        binary.put('D', "1101");
        binary.put('e', "1110");
        binary.put('E', "1110");
        binary.put('f', "1111");
        binary.put('F', "1111");

        final Map<String, String> hexadecimal = new HashMap<>();
        hexadecimal.put("0000", "0");
        hexadecimal.put("0001", "1");
        hexadecimal.put("0010", "2");
        hexadecimal.put("0011", "3");
        hexadecimal.put("0100", "4");
        hexadecimal.put("0101", "5");
        hexadecimal.put("0110", "6");
        hexadecimal.put("0111", "7");
        hexadecimal.put("1000", "8");
        hexadecimal.put("1001", "9");
        hexadecimal.put("1010", "A");
        hexadecimal.put("1011", "B");
        hexadecimal.put("1100", "C");
        hexadecimal.put("1101", "D");
        hexadecimal.put("1110", "E");
        hexadecimal.put("1111", "F");

        final Map<Character, String> gs1_6bits = new HashMap<>();
        gs1_6bits.put('#', "100011"); // Pound / Number Sign
        gs1_6bits.put('-', "101101"); // Hyphen / Minus Sign
        gs1_6bits.put('/', "101111"); // Forward Slash

        gs1_6bits.put('0', "110000"); // Zero Digit
        gs1_6bits.put('1', "110001"); // One Digit
        gs1_6bits.put('2', "110010"); // Two Digit
        gs1_6bits.put('3', "110011"); // Three Digit
        gs1_6bits.put('4', "110100"); // Four Digit
        gs1_6bits.put('5', "110101"); // Five Digit
        gs1_6bits.put('6', "110110"); // Six Digit
        gs1_6bits.put('7', "110111"); // Seven Digit
        gs1_6bits.put('8', "111000"); // Eight Digit
        gs1_6bits.put('9', "111001"); // Nine Digit

        gs1_6bits.put('A', "000001"); // Capital A
        gs1_6bits.put('B', "000010"); // Capital B
        gs1_6bits.put('C', "000011"); // Capital C
        gs1_6bits.put('D', "000100"); // Capital D
        gs1_6bits.put('E', "000101"); // Capital E
        gs1_6bits.put('F', "000110"); // Capital F
        gs1_6bits.put('G', "000111"); // Capital G
        gs1_6bits.put('H', "001000"); // Capital H
        gs1_6bits.put('I', "001001"); // Capital I
        gs1_6bits.put('J', "001010"); // Capital J
        gs1_6bits.put('K', "001011"); // Capital K
        gs1_6bits.put('L', "001100"); // Capital L
        gs1_6bits.put('M', "001101"); // Capital M
        gs1_6bits.put('N', "001110"); // Capital N
        gs1_6bits.put('O', "001111"); // Capital O
        gs1_6bits.put('P', "010000"); // Capital P
        gs1_6bits.put('Q', "010001"); // Capital Q
        gs1_6bits.put('R', "010010"); // Capital R
        gs1_6bits.put('S', "010011"); // Capital S
        gs1_6bits.put('T', "010100"); // Capital T
        gs1_6bits.put('U', "010101"); // Capital U
        gs1_6bits.put('V', "010110"); // Capital V
        gs1_6bits.put('W', "010111"); // Capital W
        gs1_6bits.put('X', "011000"); // Capital X
        gs1_6bits.put('Y', "011001"); // Capital Y
        gs1_6bits.put('Z', "011010"); // Capital Z

        _binary = Collections.unmodifiableMap(binary);
        _hexadecimal = Collections.unmodifiableMap(hexadecimal);
        _gs1_6bits = Collections.unmodifiableMap(gs1_6bits);
    }

    public static int remainder(int length) {
        return (int) (Math.ceil((length / 16.0)) * 16) - length;
    }

    /**
     * Convert a hexadecimal string to a binary string.
     *
     * @param hex - hexadecimal string
     * @return binary string
     */
    public static String hexToBin(final String hex) {
        final StringBuilder out = new StringBuilder();
        for (Character c : hex.toUpperCase().toCharArray()) {
            if (Character.isWhitespace(c)) continue;

            final String b = _binary.get(c);
            if (b == null) {
                throw new IllegalArgumentException("%c is not a valid hex digit".formatted(c));
            }
            out.append(b);
        }

        return out.toString();
    }

    public static String to6BitsBinary(final String value) {
        final StringBuilder out = new StringBuilder();
        for (Character c : value.toCharArray()) {
            final String b = _gs1_6bits.get(c);
            if (b == null) {
                throw new IllegalArgumentException("'%c' is not a valid 6 bits digit".formatted(c));
            }
            out.append(b);
        }
        return out.toString();
    }

    /**
     * Convert a binary string to a hexadecimal string.
     *
     * @param bin - binary string, e.g.: "10101010" = "AA"
     * @return hexadecimal string, e.g.: "0F"
     */
    public static String binToHex(final String bin) {
        if (bin.length() % 4 != 0) {
            throw new IllegalArgumentException("Binary string '%s' does not represent a valid Hex number".formatted(bin));
        }

        final StringBuilder out = new StringBuilder();

        int pos = 0;
        while (pos < bin.length()) {
            final String sub = bin.substring(pos, pos + 4);
            final String hex = _hexadecimal.get(sub);
            if (hex == null) {
                throw new IllegalArgumentException("Binary string '%s' does not represent a valid Hex number".formatted(sub));
            }
            out.append(hex);
            pos += 4;
        }

        return out.toString();
    }

    public static String decToBin(final BigInteger value, final int bits) {
        if (value == null) return null;
        return StringUtils.leftPad(value.toString(2), bits, '0');
    }

    public static String decToBin(final String value, final int bits) {
        if (value == null || value.isEmpty()) return null;
        return decToBin(new BigInteger(value), bits);
    }

    public static String decToBin(final Long value, int bits) {
        if (value == null) return null;
        return decToBin(BigInteger.valueOf(value), bits);
    }

    public static String decToBin(final Integer value, int bits) {
        if (value == null) return null;
        return decToBin(value.longValue(), bits);
    }

    /**
     * Convert a binary string to a decimal string.
     *
     * @param value - binary string, e.g.: "0101" = "5"
     * @return decimal string, e.g.: "5"
     */
    public static String binToDec(final String value) {
        if (value == null) return null;
        return new BigInteger(value, 2).toString();
    }

    /* --- string helpers ---*/
    public static String fill(final String value, final int size) {
        return StringUtils.rightPad(value, size, '0');
    }

    public static List<String> chunk(final String value, final int size) {
        return StringUtils.chunk(value, size);
    }

    public static boolean isNumeric(final String value) {
        return (value != null) && value.chars().allMatch(Character::isDigit);
    }

    public static boolean isNotNumeric(final String value) {
        return !isNumeric(value);
    }

    /* --- REVIEW ---*/
    public static String binToString(String s) {
        StringBuilder bin = new StringBuilder();

        for (int i = 0; i <= s.length() - 8; i += 8) {
            int k = Integer.parseInt(s.substring(i, i + 8), 2);
            bin.append((char) k);
        }

        return bin.toString().trim();
    }

    @Deprecated
    //	http://stackoverflow.com/questions/4211705/binary-to-text-in-java?noredirect=1&lq=1
    public static String convertBinToBit(String s, int fromBit, int toBit) {
        StringBuilder bin = new StringBuilder();
        // https://stackoverflow.com/a/3760193/1696733
        for (int start = 0; start < s.length(); start += fromBit) {
            String a = s.substring(start, Math.min(s.length(), start + fromBit));
            bin.append(lPadZero(Integer.parseInt(a), toBit));
        }

        return bin.toString();
    }

    @Deprecated
    //http://stackoverflow.com/questions/917163/convert-a-string-like-testing123-to-binary-in-java
    public static String StringToBinary(String str, int bits) {
        StringBuilder result = new StringBuilder();
        String tmpStr;
        int tmpInt;
        char[] messChar = str.toCharArray();

        for (char c : messChar) {
            tmpStr = Integer.toBinaryString(c);
            tmpInt = tmpStr.length();
            if (tmpInt != bits) {
                tmpInt = bits - tmpInt;
                if (tmpInt == bits) {
                    result.append(tmpStr);
                } else if (tmpInt > 0) {
                    for (int j = 0; j < tmpInt; j++) {
                        result.append("0");
                    }
                    result.append(tmpStr);
                } else {
                    System.err.println("argument 'bits' is too small");
                }
            } else {
                result.append(tmpStr);
            }
        }

        return result.toString();
    }

    @Deprecated
    public static String lPadZero(int in, int fill) {

        boolean negative = false;
        int value, len = 0;

        if (in >= 0) {
            value = in;
        } else {
            negative = true;
            value = -in;
            in = -in;
            len++;
        }

        if (value == 0) {
            len = 1;
        } else {
            for (; value != 0; len++) {
                value /= 10;
            }
        }

        StringBuilder sb = new StringBuilder();

        if (negative) {
            sb.append('-');
        }

        for (int i = fill; i > len; i--) {
            sb.append('0');
        }

        sb.append(in);

        return sb.toString();
    }

}
