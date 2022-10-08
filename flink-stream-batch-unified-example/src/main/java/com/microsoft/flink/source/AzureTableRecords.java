package com.microsoft.flink.source;

import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class AzureTableRecords implements RecordsWithSplitIds<AzureTableRecordAndPosition> {
    @Nullable private String splitId;

    @Nullable private Iterator<AzureTableRecordAndPosition> recordsForSplitCurrent;

    private final Iterator<AzureTableRecordAndPosition> recordsForSplit;

    private final Set<String> finishedSplits;

    private AzureTableRecords(
            @Nullable String splitId,
            Collection<AzureTableRecordAndPosition> recordsForSplit,
            Set<String> finishedSplits) {
        this.splitId = splitId;
        this.recordsForSplit = checkNotNull(recordsForSplit).iterator();
        this.finishedSplits = checkNotNull(finishedSplits);
    }

    @Nullable
    @Override
    public String nextSplit() {
        // move the split one (from current value to null)
        final String nextSplit = this.splitId;
        this.splitId = null;

        // move the iterator, from null to value (if first move) or to null (if second move)
        this.recordsForSplitCurrent = nextSplit != null ? this.recordsForSplit : null;

        return nextSplit;
    }

    @Nullable
    @Override
    public AzureTableRecordAndPosition nextRecordFromSplit() {
        final Iterator<AzureTableRecordAndPosition> recordsForSplit = this.recordsForSplitCurrent;
        if (recordsForSplit != null) {
            return recordsForSplit.hasNext() ? recordsForSplit.next() : null;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public Set<String> finishedSplits() {
        return finishedSplits;
    }

    public static AzureTableRecords forRecords(
            final String splitId, Collection<AzureTableRecordAndPosition> recordsForSplit) {
        return new AzureTableRecords(splitId, recordsForSplit, Collections.emptySet());
    }

    public static AzureTableRecords finishedSplit(String splitId) {
        return new AzureTableRecords(null, Collections.emptySet(), Collections.singleton(splitId));
    }
}