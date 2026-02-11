package com.pedrozc90.epcs.schemes.sgln.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SGLNHeader {

    HEADER_00110010("00110010") {
        public Integer getTagSize() {
            return 96;
        }
    },
    HEADER_00111001("00111001") {
        public Integer getTagSize() {
            return 195;
        }
    };

    private static final Map<String, SGLNHeader> _map = new HashMap<>();

    static {
        for (SGLNHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static SGLNHeader of(final String value) {
        final SGLNHeader header = _map.get(value);
        if (header == null) {
            throw new IllegalArgumentException("SGLN header [%s] is invalid. Allowed only 00110010 or 00111001".formatted(value));
        }
        return header;
    }

}
