package org.epctagcoder.schemas.cpi.partitionTable;

import org.epctagcoder.schemas.TableItem;
import org.epctagcoder.schemas.cpi.enums.CPITagSize;

import java.util.ArrayList;
import java.util.List;


public class CPIPartitionTable {

    private static final List<TableItem> _list = new ArrayList<TableItem>();

    public CPIPartitionTable(final CPITagSize tagSize) {
        if (tagSize.getValue() == 96) {
            _list.clear();
            _list.add(new TableItem(0, 40, 12, 11, 3));
            _list.add(new TableItem(1, 37, 11, 14, 4));
            _list.add(new TableItem(2, 34, 10, 17, 5));
            _list.add(new TableItem(3, 30, 9, 21, 6));
            _list.add(new TableItem(4, 27, 8, 24, 7));
            _list.add(new TableItem(5, 24, 7, 27, 8));
            _list.add(new TableItem(6, 20, 6, 31, 9));
        } else { //if ( tagSize.getValue()==202 ) {  // variable
            _list.clear();
            _list.add(new TableItem(0, 40, 12, 114, 18));
            _list.add(new TableItem(1, 37, 11, 120, 19));
            _list.add(new TableItem(2, 34, 10, 126, 20));
            _list.add(new TableItem(3, 30, 9, 132, 21));
            _list.add(new TableItem(4, 27, 8, 138, 22));
            _list.add(new TableItem(5, 24, 7, 144, 23));
            _list.add(new TableItem(6, 20, 6, 150, 24));
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
