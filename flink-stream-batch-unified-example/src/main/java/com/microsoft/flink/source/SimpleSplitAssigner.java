package com.microsoft.flink.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class SimpleSplitAssigner implements AzureTableSplitAssigner {
    private final ArrayList<AzureTableSourceSplit> splits;

    public SimpleSplitAssigner(Collection<AzureTableSourceSplit> splits) {
        this.splits = new ArrayList<>(splits);
    }

    @Override
    public Optional<AzureTableSourceSplit> getNext() {
        final int size = splits.size();
        return size == 0 ? Optional.empty() : Optional.of(splits.remove(size - 1));
    }

    @Override
    public void addSplits(Collection<AzureTableSourceSplit> newSplits) {
        splits.addAll(newSplits);
    }

    @Override
    public Collection<AzureTableSourceSplit> remainingSplits() {
        return splits;
    }

    @Override
    public String toString() {
        return "SimpleSplitAssigner{" + "splits=" + splits + '}';
    }
}
