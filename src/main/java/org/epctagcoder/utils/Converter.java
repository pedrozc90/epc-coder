package org.epctagcoder.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Converter {

    static final Map<Character, String> _binary = new HashMap<>();
    static final Map<String, String> _hexadecimal = new HashMap<>();

    static {
        _binary.put('0', "0000");
        _binary.put('1', "0001");
        _binary.put('2', "0010");
        _binary.put('3', "0011");
        _binary.put('4', "0100");
        _binary.put('5', "0101");
        _binary.put('6', "0110");
        _binary.put('7', "0111");
        _binary.put('8', "1000");
        _binary.put('9', "1001");
        _binary.put('a', "1010");
        _binary.put('A', "1010");
        _binary.put('b', "1011");
        _binary.put('B', "1011");
        _binary.put('c', "1100");
        _binary.put('C', "1100");
        _binary.put('d', "1101");
        _binary.put('D', "1101");
        _binary.put('e', "1110");
        _binary.put('E', "1110");
        _binary.put('f', "1111");
        _binary.put('F', "1111");

        _hexadecimal.put("0000", "0");
        _hexadecimal.put("0001", "1");
        _hexadecimal.put("0010", "2");
        _hexadecimal.put("0011", "3");
        _hexadecimal.put("0100", "4");
        _hexadecimal.put("0101", "5");
        _hexadecimal.put("0110", "6");
        _hexadecimal.put("0111", "7");
        _hexadecimal.put("1000", "8");
        _hexadecimal.put("1001", "9");
        _hexadecimal.put("1010", "A");
        _hexadecimal.put("1011", "B");
        _hexadecimal.put("1100", "C");
        _hexadecimal.put("1101", "D");
        _hexadecimal.put("1110", "E");
        _hexadecimal.put("1111", "F");
    }

    /**
     * Convert a hexadecimal string to binary string.
     *
     * @param hex - hexadecimal string
     * @return binary string
     */
    public static String hexToBin(final String hex) {
        final StringBuilder out = new StringBuilder();
        for (Character c : hex.toUpperCase().toCharArray()) {
            final String b = _binary.get(c);
            if (b == null) {
                throw new IllegalArgumentException("%c is not a valid hex digit".formatted(c));
            }
            out.append(b);
        }

        return out.toString();
    }

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

    public static String binToString(String s) {
        StringBuilder bin = new StringBuilder();

        for (int i = 0; i <= s.length() - 8; i += 8) {
            int k = Integer.parseInt(s.substring(i, i + 8), 2);
            bin.append((char) k);
        }

        return bin.toString().trim();
    }

    // funciona, substituir?
    public static String decToBin(String dec, int bits) {
        return strZero(new BigInteger(dec).toString(2), bits);
    }

    public static String decToBin(Integer dec, int bits) {
        return strZero(BigInteger.valueOf(dec.longValue()).toString(2), bits);
    }

    // funciona, substituir?
    public static String binToDec(String bin) {
        return new BigInteger(bin, 2).toString();
    }

    // montei esse, d� d� descartar
    public static String binToDec2(String bin) {
        int len = bin.length();
        int rev = len - 1;
        BigDecimal d = new BigDecimal("0");

        StringBuilder dec = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String pos = bin.substring(i, i + 1);
            d = d.add(new BigDecimal(pos).multiply(new BigDecimal("2").pow(rev)));
            rev--;
        }
        dec.append(d);
        return dec.toString();
    }

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

    public static String fill(String text, int size) {
        StringBuilder builder = new StringBuilder(text);
        while (builder.length() < size) {
            builder.append('0');
        }
        return builder.toString();
    }

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

    public static String strZero(String str, int len) {

        StringBuilder sb = new StringBuilder();

        for (int toPrepend = len - str.length(); toPrepend > 0; toPrepend--) {
            sb.append('0');
        }

        sb.append(str);
        return sb.toString();
    }

    public static boolean isNumeric(String str) {
        return str.chars().allMatch(Character::isDigit);
    }

    public static List<String> splitEqually(String text, int size) {
        List<String> ret = new ArrayList<>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

}
