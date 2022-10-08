package com.microsoft.flink.source;

import org.apache.flink.api.connector.source.Boundedness;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.connector.source.SourceReader;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.table.data.RowData;

import java.util.Collections;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class AzureTableSource implements Source<RowData, AzureTableSourceSplit, PendingSplitsCheckpoint> {
    private final String connectionString;
    private final String tableName;
    private final Long startPartitionKey;
    private final Long endPartitionKey;

    public AzureTableSource(String connectionString, String tableName, Long startPartitionKey, Long endPartitionKey) {
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
        return new AzureTableSourceEnumerator(
                context, this.startPartitionKey, this.endPartitionKey, Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public SplitEnumerator<AzureTableSourceSplit, PendingSplitsCheckpoint> restoreEnumerator(
            SplitEnumeratorContext<AzureTableSourceSplit> context, PendingSplitsCheckpoint checkpoint)
            throws Exception {
        return new AzureTableSourceEnumerator(
                context, this.startPartitionKey, this.endPartitionKey, checkpoint.getSplits(), checkpoint.getAlreadyProcessedPartitionKeys());
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

