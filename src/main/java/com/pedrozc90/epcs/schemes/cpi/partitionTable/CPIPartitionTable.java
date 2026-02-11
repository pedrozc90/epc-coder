package com.pedrozc90.epcs.schemes.cpi.partitionTable;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PartitionTable;
import com.pedrozc90.epcs.schemes.cpi.enums.CPITagSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPIPartitionTable extends PartitionTable {

    private static final Map<Integer, CPIPartitionTable> instances = new HashMap<>();

    static {
        instances.put(96, new CPIPartitionTable(96));
        instances.put(0, new CPIPartitionTable(0));
    }

    public CPIPartitionTable(final int tagSize) {
        super(createTable(tagSize));
    }

    public static CPIPartitionTable getInstance(final int tagSize) {
        final CPIPartitionTable table = instances.get(tagSize);
        if (table == null) {
            return instances.get(0);
        }
        return table;
    }

    public static CPIPartitionTable getInstance(final CPITagSize tagSize) {
        return getInstance(tagSize.getValue());
    }

    private static List<TableItem> createTable(final int tagSize) {
        if (tagSize == 96) {
            return List.of(
                new TableItem(0, 40, 12, 11, 3),
                new TableItem(1, 37, 11, 14, 4),
                new TableItem(2, 34, 10, 17, 5),
                new TableItem(3, 30, 9, 21, 6),
                new TableItem(4, 27, 8, 24, 7),
                new TableItem(5, 24, 7, 27, 8),
                new TableItem(6, 20, 6, 31, 9)
            );
        }
        // variable
        return List.of(
            new TableItem(0, 40, 12, 114, 18),
            new TableItem(1, 37, 11, 120, 19),
            new TableItem(2, 34, 10, 126, 20),
            new TableItem(3, 30, 9, 132, 21),
            new TableItem(4, 27, 8, 138, 22),
            new TableItem(5, 24, 7, 144, 23),
            new TableItem(6, 20, 6, 150, 24)
        );
    }

}
