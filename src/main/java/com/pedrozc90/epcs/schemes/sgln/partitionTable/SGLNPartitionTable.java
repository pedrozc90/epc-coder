package com.pedrozc90.epcs.schemes.sgln.partitionTable;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PartitionTable;

import java.util.List;

public class SGLNPartitionTable extends PartitionTable {

    private static List<TableItem> createTable() {
        return List.of(
            new TableItem(0, 40, 12, 1, 0),
            new TableItem(1, 37, 11, 4, 1),
            new TableItem(2, 34, 10, 7, 2),
            new TableItem(3, 30, 9, 11, 3),
            new TableItem(4, 27, 8, 14, 4),
            new TableItem(5, 24, 7, 17, 5),
            new TableItem(6, 20, 6, 21, 6)
        );
    }

    public SGLNPartitionTable() {
        super(createTable());
    }

}
