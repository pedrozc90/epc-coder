package org.epctagcoder.schemas.sgtin.partitionTable;

import org.epctagcoder.exception.EPCParseException;
import org.epctagcoder.schemas.TableItem;

import java.util.ArrayList;
import java.util.List;


public class SGTINPartitionTable {

    static final private List<TableItem> _list = new ArrayList<>();

    static {
        _list.add(new TableItem(0, 40, 12, 4, 1));
        _list.add(new TableItem(1, 37, 11, 7, 2));
        _list.add(new TableItem(2, 34, 10, 10, 3));
        _list.add(new TableItem(3, 30, 9, 14, 4));
        _list.add(new TableItem(4, 27, 8, 17, 5));
        _list.add(new TableItem(5, 24, 7, 20, 6));
        _list.add(new TableItem(6, 20, 6, 24, 7));
    }

    public TableItem getPartitionByL(final Integer index) {
        for (TableItem item : _list) {
            if (item.getL() == index) {
                return item;
            }
        }
        return null;
    }

    public TableItem getPartitionByValue(final int index) throws EPCParseException {
        if (index < 0 || index >= _list.size()) {
            throw new EPCParseException("Partition value %d is not within expected range (0 - %d)".formatted(index, _list.size() - 1));
        }
        return _list.get(index);
    }

}
