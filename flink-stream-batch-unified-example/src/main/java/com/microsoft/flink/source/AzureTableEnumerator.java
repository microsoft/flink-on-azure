package com.microsoft.flink.source;

import java.io.IOException;
import java.util.Collection;

public interface AzureTableEnumerator {
    Collection<AzureTableSourceSplit> enumerateSplits(Long startPartitionKey, Long endPartitionKey, Long steps) throws IOException;
}
