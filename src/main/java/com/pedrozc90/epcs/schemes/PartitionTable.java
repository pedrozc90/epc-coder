package com.pedrozc90.epcs.schemes;

import com.pedrozc90.epcs.objects.TableItem;

import java.util.List;

public abstract class PartitionTable {

    protected final List<TableItem> _list;

    public PartitionTable(final List<TableItem> _list) {
        this._list = _list;
    }

    /**
     * Get partition by L value.
     *
     * @param value - company prefix digits
     * @return table item
     */
    public TableItem getPartitionByL(final Integer value) {
        for (TableItem item : _list) {
            if (item.l() == value) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get partition by value.
     *
     * @param value - partition value
     * @return table item
     */
    public TableItem getPartitionByValue(final Integer value) {
        for (TableItem item : _list) {
            if (item.partitionValue() == value) {
                return item;
            }
        }
        return null;
    }

}
