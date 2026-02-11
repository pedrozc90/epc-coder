package com.pedrozc90.epcs.schemes.sscc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SSCCExtensionDigit {

    EXTENSION_0(0),
    EXTENSION_1(1),
    EXTENSION_2(2),
    EXTENSION_3(3),
    EXTENSION_4(4),
    EXTENSION_5(5),
    EXTENSION_6(6),
    EXTENSION_7(7),
    EXTENSION_8(8),
    EXTENSION_9(9);

    private static final Map<Integer, SSCCExtensionDigit> _map = new HashMap<>();

    static {
        for (SSCCExtensionDigit row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public static SSCCExtensionDigit of(final int value) {
        return _map.get(value);
    }

}
