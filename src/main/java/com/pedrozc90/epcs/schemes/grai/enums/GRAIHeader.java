package com.pedrozc90.epcs.schemes.grai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GRAIHeader {

    HEADER_00110011("00110011") {
        public Integer getTagSize() {
            return 96;
        }
    },
    HEADER_00110111("00110111") {
        public Integer getTagSize() {
            return 170;
        }
    };

    private static final Map<String, GRAIHeader> _map = new HashMap<>();

    static {
        for (GRAIHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static GRAIHeader of(final String value) {
        final GRAIHeader header = _map.get(value);
        if (header == null) {
            throw new IllegalArgumentException("GRAI header [%s] is invalid. Allowed only 00110011 or 00110111".formatted(value));
        }
        return header;
    }

}
