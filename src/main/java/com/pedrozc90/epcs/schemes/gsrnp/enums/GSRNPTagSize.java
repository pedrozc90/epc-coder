package com.pedrozc90.epcs.schemes.gsrnp.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GSRNPTagSize {

    BITS_96(96) {
        public Integer getHeader() {
            return 46;
        }
    };

    private static final Map<Integer, GSRNPTagSize> _map = new HashMap<>();

    static {
        for (GSRNPTagSize row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public abstract Integer getHeader();

    public static GSRNPTagSize of(final int value) {
        return _map.get(value);
    }

}
