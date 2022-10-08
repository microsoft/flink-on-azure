package com.microsoft.flink.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class NonSplittingRecursiveEnumerator implements AzureTableEnumerator {
    @Override
    public Collection<AzureTableSourceSplit> enumerateSplits(Long startPartitionKey, Long endPartitionKey, Long steps) throws IOException {
        final ArrayList<AzureTableSourceSplit> splits = new ArrayList<>();

        steps = (endPartitionKey - startPartitionKey) / 10;

        for (Long partitionKey = startPartitionKey; partitionKey <= endPartitionKey; partitionKey = partitionKey + steps) {
            Long left = partitionKey;
            Long right =  Long.min(partitionKey + steps - 1, endPartitionKey);
            splits.add(new AzureTableSourceSplit(left, right, 0L));
        }

        return splits;
    }
}

