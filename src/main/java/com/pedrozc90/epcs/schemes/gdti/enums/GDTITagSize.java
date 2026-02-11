package com.pedrozc90.epcs.schemes.gdti.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GDTITagSize {

    BITS_96(96) {
        public Integer getHeader() {
            return 44;
        }

        public Integer getSerialBitCount() {
            return 41;
        }

        public Integer getSerialMaxLength() {
            return 13;
        }

        public Long getSerialMaxValue() { // confirmar isso
            return 2_199_023_255_551L;

        }
    },
    BITS_174(174) {
        public Integer getHeader() {
            return 62;
        }

        public Integer getSerialBitCount() {
            return 119;
        }

        public Integer getSerialMaxLength() {
            return 17;
        }

        public Long getSerialMaxValue() {
            return null;  // not used
        }
    };

    private static final Map<Integer, GDTITagSize> _map = new HashMap<>();

    static {
        for (GDTITagSize row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public abstract Integer getHeader();

    public abstract Integer getSerialBitCount();

    public abstract Integer getSerialMaxLength();

    public abstract Long getSerialMaxValue();

    public static GDTITagSize of(final int value) {
        return _map.get(value);
    }

}
