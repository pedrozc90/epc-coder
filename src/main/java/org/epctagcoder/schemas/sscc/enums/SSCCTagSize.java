package org.epctagcoder.schemas.sscc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SSCCTagSize {

    BITS_96(96) {
        public Integer getHeader() {
            return 49;
        }
    };

    private static final Map<Integer, SSCCTagSize> _map = new LinkedHashMap<>();

    static {
        for (SSCCTagSize row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public abstract Integer getHeader();

    public static SSCCTagSize forCode(final int code) {
        return _map.get(code);
    }

}
