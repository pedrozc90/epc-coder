package com.pedrozc90.epcs.schemes.sscc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SSCCHeader {

    HEADER_00110001("00110001") {
        public Integer getTagSize() {
            return 96;
        }
    };

    private static final Map<String, SSCCHeader> _map = new HashMap<>();

    static {
        for (SSCCHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static SSCCHeader of(final String value) throws IllegalArgumentException {
        final SSCCHeader header = _map.get(value);
        if (header == null) {
            throw new IllegalArgumentException("SSCC header [%s] is invalid. Allowed only 00110001".formatted(value));
        }
        return header;
    }

}
