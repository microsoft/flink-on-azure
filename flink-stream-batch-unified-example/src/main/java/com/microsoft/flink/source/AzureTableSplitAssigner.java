package com.microsoft.flink.source;

import java.util.Collection;
import java.util.Optional;

public interface AzureTableSplitAssigner {
    Optional<AzureTableSourceSplit> getNext();

    void addSplits(Collection<AzureTableSourceSplit> splits);

    Collection<AzureTableSourceSplit> remainingSplits();
}
