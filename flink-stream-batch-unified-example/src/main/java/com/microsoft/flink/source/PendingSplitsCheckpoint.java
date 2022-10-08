package com.microsoft.flink.source;

import java.util.Collection;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class PendingSplitsCheckpoint {
    private final Collection<AzureTableSourceSplit> splits;
    private final Collection<Long> alreadyProcessedPartitionKeys;
    @Nullable byte[] serializedFormCache;

    protected PendingSplitsCheckpoint(
            Collection<AzureTableSourceSplit> splits, Collection<Long> alreadyProcessedPartitionKeys) {
        this.splits = Collections.unmodifiableCollection(splits);
        this.alreadyProcessedPartitionKeys = Collections.unmodifiableCollection(alreadyProcessedPartitionKeys);
    }

    public Collection<AzureTableSourceSplit> getSplits() {
        return splits;
    }

    public Collection<Long> getAlreadyProcessedPartitionKeys() {
        return alreadyProcessedPartitionKeys;
    }

    @Override
    public String toString() {
        return "PendingSplitsCheckpoint{"
                + "splits="
                + splits
                + ", alreadyProcessedPartitionKeys="
                + alreadyProcessedPartitionKeys
                + '}';
    }

    public static PendingSplitsCheckpoint fromCollectionSnapshot(
            final Collection<AzureTableSourceSplit> splits) {
        checkNotNull(splits);

        // create a copy of the collection to make sure this checkpoint is immutable
        final Collection<AzureTableSourceSplit> copy = new ArrayList<>(splits);
        return new PendingSplitsCheckpoint(copy, Collections.emptySet());
    }

    public static PendingSplitsCheckpoint fromCollectionSnapshot(
            final Collection<AzureTableSourceSplit> splits,
            final Collection<Long> alreadyProcessedPartitionKeys) {
        checkNotNull(splits);

        // create a copy of the collection to make sure this checkpoint is immutable
        final Collection<AzureTableSourceSplit> splitsCopy = new ArrayList<>(splits);
        final Collection<Long> pathsCopy = new ArrayList<>(alreadyProcessedPartitionKeys);

        return new PendingSplitsCheckpoint(splitsCopy, pathsCopy);
    }

    static PendingSplitsCheckpoint reusingCollection(
            final Collection<AzureTableSourceSplit> splits,
            final Collection<Long> alreadyProcessedPartitionKeys) {
        return new PendingSplitsCheckpoint(splits, alreadyProcessedPartitionKeys);
    }
}

