package com.pedrozc90.epcs.schemes.cpi.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum CPIHeader {

    HEADER_00111100("00111100") {
        public Integer getTagSize() {
            return 96;
        }
    },
    HEADER_00111101("00111101") {
        public Integer getTagSize() {
            return 0; //null;  // variable
        }
    };

    private static final Map<String, CPIHeader> _map = new HashMap<>();

    static {
        for (CPIHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static CPIHeader of(final String value) {
        final CPIHeader header = _map.get(value);
        if (header == null) {
            throw new IllegalArgumentException("CPI header [%s] is invalid. Allowed only 00111100 or 00111101".formatted(value));
        }
        return header;
    }

}
