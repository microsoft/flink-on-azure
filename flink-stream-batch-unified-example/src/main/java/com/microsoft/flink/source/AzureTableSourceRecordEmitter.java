package com.microsoft.flink.source;

import org.apache.flink.api.connector.source.SourceOutput;
import org.apache.flink.connector.base.source.reader.RecordEmitter;
import org.apache.flink.table.data.RowData;

public class AzureTableSourceRecordEmitter implements RecordEmitter<AzureTableRecordAndPosition, RowData, AzureTableSourceSplitState> {
    @Override
    public void emitRecord(
            AzureTableRecordAndPosition element,
            SourceOutput<RowData> output,
            AzureTableSourceSplitState splitState)
            throws Exception {
        output.collect(element.getRecord());
        splitState.setOffset(element.getPartitionKey());
    }
}

