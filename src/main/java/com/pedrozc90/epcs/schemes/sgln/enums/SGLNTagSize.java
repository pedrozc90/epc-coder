package com.pedrozc90.epcs.schemes.sgln.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SGLNTagSize {

    BITS_96(96) {
        @Override
        public Integer getHeader() {
            // return 51;
            return 0x32; // 50
        }

        @Override
        public Integer getSerialBitCount() {
            return 41;
        }

        @Override
        public Integer getSerialMaxLength() {
            return 13;
        }

        @Override
        public Long getSerialMaxValue() {
            return 2_199_023_255_551L;
        }
    },
    BITS_195(195) {
        @Override
        public Integer getHeader() {
            return 55;
        }

        @Override
        public Integer getSerialBitCount() {
            return 140;
        }

        @Override
        public Integer getSerialMaxLength() {
            return 20;
        }

        @Override
        public Long getSerialMaxValue() {
            return null;  // not used
        }
    };

    private static final Map<Integer, SGLNTagSize> _map = new HashMap<>();

    static {
        for (SGLNTagSize row : values()) {
            _map.put(row.value, row);
        }
    }

    private final int value;

    public abstract Integer getHeader();

    public abstract Integer getSerialBitCount();

    public abstract Integer getSerialMaxLength();

    public abstract Long getSerialMaxValue();

    public static SGLNTagSize of(final int value) {
        return _map.get(value);
    }

}
