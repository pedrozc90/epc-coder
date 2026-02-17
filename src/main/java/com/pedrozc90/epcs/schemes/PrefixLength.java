package com.pedrozc90.epcs.schemes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum PrefixLength {

    DIGIT_6(6),
    DIGIT_7(7),
    DIGIT_8(8),
    DIGIT_9(9),
    DIGIT_10(10),
    DIGIT_11(11),
    DIGIT_12(12);

    private static final Map<Integer, PrefixLength> _map = new HashMap<>();

    static {
        for (PrefixLength rae : values()) {
            _map.put(rae.value, rae);
        }
    }

    private final int value;

    public static PrefixLength of(final int length) {
        final PrefixLength value = _map.get(length);
        if (value == null) {
            throw new IllegalArgumentException("Company Prefix is invalid. Length not found in the partition table");
        }
        return value;
    }

}
