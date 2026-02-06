package org.epctagcoder.schemas.sgtin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.epctagcoder.exception.EPCParseException;

import java.util.LinkedHashMap;
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

    private static final Map<String, SGTINHeader> _map = new LinkedHashMap<>();

    static {
        for (SGTINHeader row : values()) {
            _map.put(row.value, row);
        }
    }

    private final String value;

    public abstract Integer getTagSize();

    public static SGTINHeader forCode(final String code) throws EPCParseException {
        final SGTINHeader header = _map.get(code);
        if (header == null) {
            throw new EPCParseException("SGTIN header [%s] is invalid. Allowed only 00110000 or 00110110".formatted(code));
        }
        return header;
    }

}
