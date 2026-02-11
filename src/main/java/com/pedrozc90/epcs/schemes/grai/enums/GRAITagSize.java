package com.pedrozc90.epcs.schemes.grai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum GRAITagSize {

    BITS_96(96) {
        public Integer getHeader() {
            return 51;
        }

        public Integer getSerialBitCount() {
            return 38;
        }

        public Integer getSerialMaxLength() {
            return 13;
        }

        public Long getSerialMaxValue() {
            return 274_877_906_943L;
        }
    },
    BITS_170(170) {
        public Integer getHeader() {
            return 55;
        }

        public Integer getSerialBitCount() {
            return 112;
        }

        public Integer getSerialMaxLength() {
            return 16;
        }

        public Long getSerialMaxValue() {
            return null;  // not used
        }
    };

    private static final Map<Integer, GRAITagSize> _map = new HashMap<>();

    static {
        for (GRAITagSize row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public abstract Integer getHeader();

    public abstract Integer getSerialBitCount();

    public abstract Integer getSerialMaxLength();

    public abstract Long getSerialMaxValue();

    public static GRAITagSize of(final int value) {
        return _map.get(value);
    }

}
