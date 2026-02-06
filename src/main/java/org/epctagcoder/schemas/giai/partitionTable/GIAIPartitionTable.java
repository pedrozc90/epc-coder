package org.epctagcoder.schemas.giai.partitionTable;

import org.epctagcoder.schemas.TableItem;
import org.epctagcoder.schemas.giai.enums.GIAITagSize;

import java.util.ArrayList;
import java.util.List;


public class GIAIPartitionTable {

    private static final List<TableItem> _list = new ArrayList<TableItem>();

    public GIAIPartitionTable(final GIAITagSize tagSize) {
        if (tagSize.getValue() == 96) {
            _list.clear();
            _list.add(new TableItem(0, 40, 12, 42, 13));
            _list.add(new TableItem(1, 37, 11, 45, 14));
            _list.add(new TableItem(2, 34, 10, 48, 15));
            _list.add(new TableItem(3, 30, 9, 52, 16));
            _list.add(new TableItem(4, 27, 8, 55, 17));
            _list.add(new TableItem(5, 24, 7, 58, 18));
            _list.add(new TableItem(6, 20, 6, 62, 19));
        } else if (tagSize.getValue() == 202) {
            _list.clear();
            _list.add(new TableItem(0, 40, 12, 148, 18));
            _list.add(new TableItem(1, 37, 11, 151, 19));
            _list.add(new TableItem(2, 34, 10, 154, 20));
            _list.add(new TableItem(3, 30, 9, 158, 21));
            _list.add(new TableItem(4, 27, 8, 161, 22));
            _list.add(new TableItem(5, 24, 7, 164, 23));
            _list.add(new TableItem(6, 20, 6, 168, 24));
        }
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
