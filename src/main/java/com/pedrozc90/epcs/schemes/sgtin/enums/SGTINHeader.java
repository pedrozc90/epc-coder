package com.pedrozc90.epcs.schemes.sgtin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SGTINHeader {

    HEADER_00110000("00110000") {
        public Integer getTagSize() {
            return 96;
        }
    },
    HEADER_00110110("00110110") {
        public Integer getTagSize() {
            return 198;
        }
    };

    private static final Map<String, SGTINHeader> _map = new HashMap<>();

    static {
        for (SGTINHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static SGTINHeader of(final String value) throws IllegalArgumentException {
        final SGTINHeader header = _map.get(value);
        if (header == null) {
            throw new IllegalArgumentException("SGTIN header [%s] is invalid. Allowed only 00110000 or 00110110".formatted(value));
        }
        return header;
    }

}
