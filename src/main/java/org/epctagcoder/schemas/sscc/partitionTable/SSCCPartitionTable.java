package org.epctagcoder.schemas.sscc.partitionTable;

import org.epctagcoder.schemas.TableItem;

import java.util.ArrayList;
import java.util.List;


public class SSCCPartitionTable {

    static final private List<TableItem> _list = new ArrayList<TableItem>();

    static {
        _list.add(new TableItem(0, 40, 12, 18, 5));
        _list.add(new TableItem(1, 37, 11, 21, 6));
        _list.add(new TableItem(2, 34, 10, 24, 7));
        _list.add(new TableItem(3, 30, 9, 28, 8));
        _list.add(new TableItem(4, 27, 8, 31, 9));
        _list.add(new TableItem(5, 24, 7, 34, 10));
        _list.add(new TableItem(6, 20, 6, 38, 11));
    }

    public TableItem getPartitionByL(final Integer index) {
        for (TableItem item : _list) {
            if (item.getL() == index) {
                return item;
            }
        }
        return null;
    }

    public TableItem getPartitionByValue(final Integer index) {
        for (TableItem item : _list) {
            if (item.getPartitionValue() == index) {
                return item;
            }
        }
        return null;
    }

}
