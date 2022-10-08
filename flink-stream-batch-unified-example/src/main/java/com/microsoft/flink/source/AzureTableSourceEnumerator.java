package com.microsoft.flink.source;

import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class AzureTableSourceEnumerator implements SplitEnumerator<AzureTableSourceSplit, PendingSplitsCheckpoint> {
    private final AzureTableEnumerator enumerator;
    private final AzureTableSplitAssigner splitAssigner;
    private final SplitEnumeratorContext<AzureTableSourceSplit> context;
    private final Long startPartitionKey;
    private final Long endPartitionKey;
    private final HashSet<Long> alreadyDiscoveredPartitionKeys;
    private final LinkedHashMap<Integer, String> readersAwaitingSplit;

    public AzureTableSourceEnumerator(
            SplitEnumeratorContext<AzureTableSourceSplit> context,
            Long startPartitionKey,
            Long endPartitionKey,
            Collection<AzureTableSourceSplit> splits,
            Collection<Long> alreadyDiscoveredPartitionKeys) {
        this.enumerator = new NonSplittingRecursiveEnumerator();
        this.splitAssigner = new SimpleSplitAssigner(checkNotNull(splits));
        this.context = checkNotNull(context);
        this.startPartitionKey = checkNotNull(startPartitionKey);
        this.endPartitionKey = checkNotNull(endPartitionKey);
        this.alreadyDiscoveredPartitionKeys = new HashSet<>(checkNotNull(alreadyDiscoveredPartitionKeys));
        this.readersAwaitingSplit = new LinkedHashMap<>();
    }

    @Override
    public void start() {
        context.callAsync(
                () -> enumerator.enumerateSplits(startPartitionKey, endPartitionKey, 10L),
                this::processDiscoveredSplits,
                2000,
                1000);
    }

    private void processDiscoveredSplits(Collection<AzureTableSourceSplit> splits, Throwable error) {
        if (error != null) {
            return;
        }

        final Collection<AzureTableSourceSplit> newSplits =
                splits.stream()
                        .filter((split) -> alreadyDiscoveredPartitionKeys.add(split.getStartPartitionKey()))
                        .collect(Collectors.toList());
        splitAssigner.addSplits(newSplits);

        assignSplits();
    }

    @Override
    public void handleSplitRequest(int subtaskId, @Nullable String requesterHostname) {
        readersAwaitingSplit.put(subtaskId, requesterHostname);
        assignSplits();
    }

    @Override
    public void addSplitsBack(List<AzureTableSourceSplit> splits, int subtaskId) {
        splitAssigner.addSplits(splits);
    }

    @Override
    public void addReader(int subtaskId) {
        // this source is purely lazy-pull-based, nothing to do upon registration
    }

    @Override
    public PendingSplitsCheckpoint snapshotState(long checkpointId) throws Exception {
        final PendingSplitsCheckpoint checkpoint =
                PendingSplitsCheckpoint.fromCollectionSnapshot(
                        splitAssigner.remainingSplits(), this.alreadyDiscoveredPartitionKeys);

        return checkpoint;
    }

    @Override
    public void close() throws IOException {
        // no resources to close
    }

    private void assignSplits() {
        final Iterator<Map.Entry<Integer, String>> awaitingReader =
                readersAwaitingSplit.entrySet().iterator();

        while (awaitingReader.hasNext()) {
            final Map.Entry<Integer, String> nextAwaiting = awaitingReader.next();

            // if the reader that requested another split has failed in the meantime, remove
            // it from the list of waiting readers
            if (!context.registeredReaders().containsKey(nextAwaiting.getKey())) {
                awaitingReader.remove();
                continue;
            }

            final int awaitingSubtask = nextAwaiting.getKey();
            final Optional<AzureTableSourceSplit> nextSplit = splitAssigner.getNext();
            if (nextSplit.isPresent()) {
                context.assignSplit(nextSplit.get(), awaitingSubtask);
                awaitingReader.remove();
            } else {
//                context.signalNoMoreSplits(awaitingSubtask);
                break;
            }
        }
    }
}

