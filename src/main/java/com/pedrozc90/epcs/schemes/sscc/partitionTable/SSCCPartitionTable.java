package com.pedrozc90.epcs.schemes.sscc.partitionTable;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PartitionTable;

import java.util.List;

public class SSCCPartitionTable extends PartitionTable {

    private static List<TableItem> createTable() {
        return List.of(
            new TableItem(0, 40, 12, 18, 5),
            new TableItem(1, 37, 11, 21, 6),
            new TableItem(2, 34, 10, 24, 7),
            new TableItem(3, 30, 9, 28, 8),
            new TableItem(4, 27, 8, 31, 9),
            new TableItem(5, 24, 7, 34, 10),
            new TableItem(6, 20, 6, 38, 11)
        );
    }

    public SSCCPartitionTable() {
        super(createTable());
    }

}
