package org.epctagcoder.schemas.giai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GIAIHeader {

    HEADER_00110100("00110100") {
        public Integer getTagSize() {
            return 96;
        }
    },
    HEADER_00111000("00111000") {
        public Integer getTagSize() {
            return 202;
        }
    };

    private static final Map<String, GIAIHeader> _map = new LinkedHashMap<>();

    static {
        for (GIAIHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static GIAIHeader forCode(final String code) {
        return _map.get(code);
    }

}
