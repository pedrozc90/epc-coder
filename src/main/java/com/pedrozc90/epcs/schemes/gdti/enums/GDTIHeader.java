package com.pedrozc90.epcs.schemes.gdti.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GDTIHeader {

    HEADER_00101100("00101100") {
        public Integer getTagSize() {
            return 96;
        }
    },
    HEADER_00111110("00111110") {
        public Integer getTagSize() {
            return 174;
        }
    };

    private static final Map<String, GDTIHeader> _map = new HashMap<>();

    static {
        for (GDTIHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static GDTIHeader of(final String value) {
        final GDTIHeader header = _map.get(value);
        if (header == null) {
            throw new IllegalArgumentException("GDTI header [%s] is invalid. Allowed only 00101100 or 00111110".formatted(value));
        }
        return header;
    }

}
