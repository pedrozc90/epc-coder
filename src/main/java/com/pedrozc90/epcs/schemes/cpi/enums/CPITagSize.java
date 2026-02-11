package com.pedrozc90.epcs.schemes.cpi.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum CPITagSize {

    BITS_96(96) {
        public Integer getHeader() {
            return 60;
        }

        public Integer getSerialBitCount() {
            return 31;
        }

        public Integer getSerialMaxLength() {
            return 0;
        }

        public Long getSerialMaxValue() {
            return 2_147_483_647L;
        }
    },
    BITS_VARIABLE(0) {
        public Integer getHeader() {
            return 61;
        }

        public Integer getSerialBitCount() {
            return 40;
        }

        public Integer getSerialMaxLength() {
            return 12;
        }

        public Long getSerialMaxValue() {
            return null;  // not used
        }
    };

    private static final Map<Integer, CPITagSize> _map = new HashMap<>();

    static {
        for (CPITagSize row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public abstract Integer getHeader();

    public abstract Integer getSerialBitCount();

    public abstract Integer getSerialMaxLength();

    public abstract Long getSerialMaxValue();

    public static CPITagSize of(final int value) {
        return _map.getOrDefault(value, BITS_VARIABLE);
    }

}
