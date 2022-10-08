package com.microsoft.flink.source;

import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class AzureTableStaticSourceEnumerator implements SplitEnumerator<AzureTableSourceSplit, PendingSplitsCheckpoint> {
    private final AzureTableSplitAssigner splitAssigner;
    private final SplitEnumeratorContext<AzureTableSourceSplit> context;

    public AzureTableStaticSourceEnumerator(
            SplitEnumeratorContext<AzureTableSourceSplit> context,
            Collection<AzureTableSourceSplit> splits) {
        this.splitAssigner = new SimpleSplitAssigner(checkNotNull(splits));
        this.context = checkNotNull(context);
    }

    @Override
    public void start() {
        // no resources to start
    }

    @Override
    public void close() throws IOException {
        // no resources to close
    }

    @Override
    public void addReader(int subtaskId) {
        // this source is purely lazy-pull-based, nothing to do upon registration
    }

    @Override
    public void handleSplitRequest(int subtaskId, @Nullable String requesterHostname) {
        if (!context.registeredReaders().containsKey(subtaskId)) {
            // reader failed between sending the request and now. skip this request.
            return;
        }

        final Optional<AzureTableSourceSplit> nextSplit = splitAssigner.getNext();
        if (nextSplit.isPresent()) {
            final AzureTableSourceSplit split = nextSplit.get();
            context.assignSplit(split, subtaskId);
        } else {
            context.signalNoMoreSplits(subtaskId);
        }
    }

    @Override
    public void addSplitsBack(List<AzureTableSourceSplit> splits, int subtaskId) {
        splitAssigner.addSplits(splits);
    }


    @Override
    public PendingSplitsCheckpoint snapshotState(long checkpointId) throws Exception {
        return PendingSplitsCheckpoint.fromCollectionSnapshot(splitAssigner.remainingSplits());
    }

}


