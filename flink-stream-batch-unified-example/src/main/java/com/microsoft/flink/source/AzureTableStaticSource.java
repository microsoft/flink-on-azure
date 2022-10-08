package com.microsoft.flink.source;

import org.apache.flink.api.connector.source.*;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.table.data.RowData;

import java.util.Collection;

public class AzureTableStaticSource implements Source<RowData, AzureTableSourceSplit, PendingSplitsCheckpoint> {
    private final String connectionString;
    private final String tableName;
    private final Long startPartitionKey;
    private final Long endPartitionKey;

    public AzureTableStaticSource(String connectionString, String tableName, Long startPartitionKey, Long endPartitionKey) {
        this.connectionString = connectionString;
        this.tableName = tableName;
        this.startPartitionKey = startPartitionKey;
        this.endPartitionKey = endPartitionKey;
    }

    @Override
    public Boundedness getBoundedness() {
        return Boundedness.BOUNDED;
    }

    @Override
    public SourceReader<RowData, AzureTableSourceSplit> createReader(SourceReaderContext context)
            throws Exception {
        return new AzureTableSourceReader(context, this.connectionString, this.tableName);
    }

    @Override
    public SplitEnumerator<AzureTableSourceSplit, PendingSplitsCheckpoint> createEnumerator(
            SplitEnumeratorContext<AzureTableSourceSplit> context) throws Exception {
        AzureTableEnumerator enumerator = new NonSplittingRecursiveEnumerator();
        Collection<AzureTableSourceSplit> splits = enumerator.enumerateSplits(startPartitionKey, endPartitionKey, 10L);
        return new AzureTableStaticSourceEnumerator(
                context, splits);
    }

    @Override
    public SplitEnumerator<AzureTableSourceSplit, PendingSplitsCheckpoint> restoreEnumerator(
            SplitEnumeratorContext<AzureTableSourceSplit> context, PendingSplitsCheckpoint checkpoint)
            throws Exception {
        return new AzureTableStaticSourceEnumerator(
                context, checkpoint.getSplits());
    }

    @Override
    public SimpleVersionedSerializer<AzureTableSourceSplit> getSplitSerializer() {
        return AzureTableSourceSplitSerializer.INSTANCE;
    }

    @Override
    public SimpleVersionedSerializer<PendingSplitsCheckpoint> getEnumeratorCheckpointSerializer() {
        return new PendingSplitsCheckpointSerializer(getSplitSerializer());
    }
}

