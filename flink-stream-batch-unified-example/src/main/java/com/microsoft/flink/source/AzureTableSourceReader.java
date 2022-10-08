package com.microsoft.flink.source;

import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.connector.base.source.reader.SingleThreadMultiplexSourceReaderBase;
import org.apache.flink.table.data.RowData;

import java.util.Map;

public class AzureTableSourceReader extends SingleThreadMultiplexSourceReaderBase<AzureTableRecordAndPosition, RowData, AzureTableSourceSplit, AzureTableSourceSplitState> {
    public AzureTableSourceReader(SourceReaderContext readerContext, String connectionString, String tableName) {
        super(
                () -> new AzureTableSourceSplitReader(connectionString, tableName),
                new AzureTableSourceRecordEmitter(),
                readerContext.getConfiguration(),
                readerContext);
    }

    @Override
    public void start() {
        // we request a split only if we did not get splits during the checkpoint restore
        if (getNumberOfCurrentlyAssignedSplits() == 0) {
            context.sendSplitRequest();
        }
    }

    @Override
    protected void onSplitFinished(Map<String, AzureTableSourceSplitState> finishedSplitIds) {
        context.sendSplitRequest();
    }

    @Override
    protected AzureTableSourceSplitState initializedState(AzureTableSourceSplit split) {
        return new AzureTableSourceSplitState(split);
    }

    @Override
    protected AzureTableSourceSplit toSplitType(String splitId, AzureTableSourceSplitState splitState) {
        return splitState.toAzureTableSourceSplit();
    }
}
