package com.pedrozc90.epcs.schemes.sscc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SSCCFilterValue {

    ALL_OTHERS_0(0),
    RESERVED_1(1),
    CASE_2(2),
    RESERVED_3(3),
    RESERVED_4(4),
    RESERVED_5(5),
    UNIT_LOAD_6(6),
    RESERVED_7(7);

    private static final Map<Integer, SSCCFilterValue> _map = new HashMap<>();

    static {
        for (SSCCFilterValue row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public static SSCCFilterValue of(final int value) {
        return _map.get(value);
    }

}
