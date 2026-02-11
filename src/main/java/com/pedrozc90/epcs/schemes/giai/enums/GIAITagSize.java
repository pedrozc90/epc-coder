package com.pedrozc90.epcs.schemes.giai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GIAITagSize {

    BITS_96(96) {
        public Integer getHeader() {
            return 52;
        }

        public Integer getSerialBitCount() {
            return 38;
        }

        public Integer getSerialMaxLength() {
            return 13;
        }
    },
    BITS_202(202) {
        public Integer getHeader() {
            return 56;
        }

        public Integer getSerialBitCount() {
            return 112;
        }

        public Integer getSerialMaxLength() {
            return 20;
        }
    };

    private static final Map<Integer, GIAITagSize> _map = new HashMap<>();

    static {
        for (GIAITagSize row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public abstract Integer getHeader();

    public abstract Integer getSerialBitCount();

    public abstract Integer getSerialMaxLength();

    public static GIAITagSize of(final int value) {
        return _map.get(value);
    }

}
