package com.pedrozc90.epcs.schemes.sgtin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SGTINTagSize {

    BITS_96(96) {
        public Integer getHeader() {
            return 48;
        }

        public Integer getSerialBitCount() {
            return 38;
        }

        public Integer getSerialMaxLength() {
            return 11;
        }

        public Long getSerialMaxValue() {
            return 274_877_906_943L;
        }
    },
    BITS_198(198) {
        public Integer getHeader() {
            return 54;
        }

        public Integer getSerialBitCount() {
            return 140;
        }

        public Integer getSerialMaxLength() {
            return 20;
        }

        public Long getSerialMaxValue() {
            return null;  // not used
        }
    };

    private static final Map<Integer, SGTINTagSize> _map = new HashMap<>();

    static {
        for (SGTINTagSize row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public abstract Integer getHeader();

    public abstract Integer getSerialBitCount();

    public abstract Integer getSerialMaxLength();

    public abstract Long getSerialMaxValue();

    public static SGTINTagSize of(final int value) {
        final SGTINTagSize bits = _map.get(value);
        if (bits == null) {
            throw new IllegalArgumentException("SGTIN tag size %d is invalid. Only 96 bits or 198 bits supported.".formatted(value));
        }
        return bits;
    }

}
