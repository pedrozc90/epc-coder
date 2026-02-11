package com.pedrozc90.epcs.schemes.sgtin.partitionTable;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PartitionTable;

import java.util.List;

public class SGTINPartitionTable extends PartitionTable {

    private static List<TableItem> createTable() {
        return List.of(
            new TableItem(0, 40, 12, 4, 1),
            new TableItem(1, 37, 11, 7, 2),
            new TableItem(2, 34, 10, 10, 3),
            new TableItem(3, 30, 9, 14, 4),
            new TableItem(4, 27, 8, 17, 5),
            new TableItem(5, 24, 7, 20, 6),
            new TableItem(6, 20, 6, 24, 7)
        );
    }

    public SGTINPartitionTable() {
        super(createTable());
    }

    @Override
    public TableItem getPartitionByValue(final Integer index) {
        if (index < 0 || index >= _list.size()) {
            throw new IllegalArgumentException("Partition value %d is not within expected range (0 - %d)".formatted(index, _list.size() - 1));
        }
        return _list.get(index);
    }

}
