package com.pedrozc90.epcs.schemes.giai.partitionTable;

import com.pedrozc90.epcs.objects.TableItem;
import com.pedrozc90.epcs.schemes.PartitionTable;
import com.pedrozc90.epcs.schemes.giai.enums.GIAITagSize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GIAIPartitionTable extends PartitionTable {

    private static final Map<Integer, GIAIPartitionTable> instances = new HashMap<>();

    static {
        instances.put(96, new GIAIPartitionTable(96));
        instances.put(202, new GIAIPartitionTable(202));
    }

    public GIAIPartitionTable(final int tagSize) {
        super(createTable(tagSize));
    }

    public static GIAIPartitionTable getInstance(final int tagSize) {
        final GIAIPartitionTable table = instances.get(tagSize);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported tag size: " + tagSize);
        }
        return table;
    }

    public static GIAIPartitionTable getInstance(final GIAITagSize tagSize) {
        return getInstance(tagSize.getValue());
    }

    private static List<TableItem> createTable(final int tagSize) {
        final List<TableItem> _list = new ArrayList<TableItem>();
        if (tagSize == 96) {
            return List.of(
                new TableItem(0, 40, 12, 42, 13),
                new TableItem(1, 37, 11, 45, 14),
                new TableItem(2, 34, 10, 48, 15),
                new TableItem(3, 30, 9, 52, 16),
                new TableItem(4, 27, 8, 55, 17),
                new TableItem(5, 24, 7, 58, 18),
                new TableItem(6, 20, 6, 62, 19)
            );
        } else if (tagSize == 202) {
            return List.of(
                new TableItem(0, 40, 12, 148, 18),
                new TableItem(1, 37, 11, 151, 19),
                new TableItem(2, 34, 10, 154, 20),
                new TableItem(3, 30, 9, 158, 21),
                new TableItem(4, 27, 8, 161, 22),
                new TableItem(5, 24, 7, 164, 23),
                new TableItem(6, 20, 6, 168, 24)
            );
        }
        throw new IllegalArgumentException("Unsupported tag size: " + tagSize);
    }

}
