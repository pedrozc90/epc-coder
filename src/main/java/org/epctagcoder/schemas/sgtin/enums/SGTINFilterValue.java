package org.epctagcoder.schemas.sgtin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SGTINFilterValue {

    ALL_OTHERS_0(0),
    POS_ITEM_1(1),
    CASE_2(2),
    RESERVED_3(3),
    INNER_PACK_4(4),
    RESERVED_5(5),
    UNIT_LOAD_6(6),
    COMPONENT_7(7);

    private static final Map<Integer, SGTINFilterValue> _map = new LinkedHashMap<>();

    static {
        for (SGTINFilterValue row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public static SGTINFilterValue forCode(final int code) {
        return _map.get(code);
    }

}
