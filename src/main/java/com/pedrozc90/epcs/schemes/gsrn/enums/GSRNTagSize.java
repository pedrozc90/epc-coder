package com.pedrozc90.epcs.schemes.gsrn.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GSRNTagSize {

    BITS_96(96) {
        public Integer getHeader() {
            return 45;
        }
    };

    private static final Map<Integer, GSRNTagSize> _map = new HashMap<>();

    static {
        for (GSRNTagSize row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public abstract Integer getHeader();

    public static GSRNTagSize of(final int value) {
        return _map.get(value);
    }

}
