package org.epctagcoder.schemas;

import lombok.Data;

@Data
public class TableItem {

    private final int partitionValue;
    private final int l;
    private final int m;
    private final int n;
    private final int digits;

    public TableItem(final int partitionValue, final int m, final int l, final int n, final int digits) {
        this.partitionValue = partitionValue;
        this.m = m;
        this.l = l;
        this.n = n;
        this.digits = digits;
    }

}
