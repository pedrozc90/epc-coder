package com.pedrozc90.epcs.schemes.gsrn.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GSRNFilterValue {

    ALL_OTHERS_0(0),
    RESERVED_1(1),
    RESERVED_2(2),
    RESERVED_3(3),
    RESERVED_4(4),
    RESERVED_5(5),
    RESERVED_6(6),
    RESERVED_7(7);

    private static final Map<Integer, GSRNFilterValue> _map = new HashMap<>();

    static {
        for (GSRNFilterValue row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public static GSRNFilterValue of(final int value) {
        return _map.get(value);
    }

}
