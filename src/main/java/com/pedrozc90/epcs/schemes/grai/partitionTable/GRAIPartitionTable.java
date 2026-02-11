package com.pedrozc90.epcs.schemes.grai.partitionTable;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PartitionTable;

import java.util.List;

public class GRAIPartitionTable extends PartitionTable {

    private static List<TableItem> createTable() {
        return List.of(
            new TableItem(0, 40, 12, 4, 0),
            new TableItem(1, 37, 11, 7, 1),
            new TableItem(2, 34, 10, 10, 2),
            new TableItem(3, 30, 9, 14, 3),
            new TableItem(4, 27, 8, 17, 4),
            new TableItem(5, 24, 7, 20, 5),
            new TableItem(6, 20, 6, 24, 6)
        );
    }

    public GRAIPartitionTable() {
        super(createTable());
    }

}
