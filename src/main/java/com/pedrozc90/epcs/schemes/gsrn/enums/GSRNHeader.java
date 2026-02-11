package com.pedrozc90.epcs.schemes.gsrn.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GSRNHeader {

    HEADER_00101101("00101101") {
        public Integer getTagSize() {
            return 96;
        }
    };

    private static final Map<String, GSRNHeader> _map = new HashMap<>();

    static {
        for (GSRNHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static GSRNHeader of(final String value) {
        final GSRNHeader header = _map.get(value);
        if (header == null) {
            throw new IllegalArgumentException("GSRN header [%s] is invalid. Allowed only 00101101".formatted(value));
        }
        return header;
    }

}
