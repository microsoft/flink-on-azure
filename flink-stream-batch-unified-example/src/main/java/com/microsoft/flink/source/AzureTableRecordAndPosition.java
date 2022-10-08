package com.microsoft.flink.source;

import org.apache.flink.table.data.RowData;

public class AzureTableRecordAndPosition {
    private final RowData record;
    private final Long partitionKey;

    public AzureTableRecordAndPosition(RowData record, Long partitionKey) {
        this.record = record;
        this.partitionKey = partitionKey;
    }

    public RowData getRecord() {
        return record;
    }

    public Long getPartitionKey() {
        return partitionKey;
    }

    @Override
    public String toString() {
        return "AzureTableRecordAndPosition{" + "record=" + record + ", partitionKey=" + partitionKey + '}';
    }
}
