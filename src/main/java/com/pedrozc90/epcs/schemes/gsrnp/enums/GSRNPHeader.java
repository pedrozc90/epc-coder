package com.pedrozc90.epcs.schemes.gsrnp.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GSRNPHeader {

    HEADER_00101110("00101110") {
        public Integer getTagSize() {
            return 96;
        }
    };

    private static final Map<String, GSRNPHeader> _map = new HashMap<>();

    static {
        for (GSRNPHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static GSRNPHeader of(final String code) {
        if (code == null) return null;
        return _map.get(code);
    }

}
