package com.microsoft.flink.source;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds;
import org.apache.flink.connector.base.source.reader.splitreader.SplitReader;
import org.apache.flink.connector.base.source.reader.splitreader.SplitsAddition;
import org.apache.flink.connector.base.source.reader.splitreader.SplitsChange;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.StringData;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.*;

public class AzureTableSourceSplitReader implements SplitReader<AzureTableRecordAndPosition, AzureTableSourceSplit> {
    private final Queue<AzureTableSourceSplit> splits;
    private TableClient tableClient;
    private Queue<TableEntity> entities;
    @Nullable private String currentSplitId;

    public AzureTableSourceSplitReader(String connectionString, String tableName) {
        this.splits = new ArrayDeque<>();
        this.entities = null;
        this.tableClient = new TableClientBuilder()
                .connectionString(connectionString)
                .tableName(tableName)
                .buildClient();
    }

    private void checkSplitOrStartNext() throws IOException {
        if (entities != null) {
            return;
        }

        this.entities = new ArrayDeque<>();
        final AzureTableSourceSplit nextSplit = splits.poll();
        if (nextSplit == null) {
            throw new IOException("Cannot fetch from another split - no split remaining");
        }

        ListEntitiesOptions options = new ListEntitiesOptions()
                .setFilter(String.format("PartitionKey ge '%s' and PartitionKey le '%s'", nextSplit.getStartPartitionKey() + nextSplit.getOffset(), nextSplit.getEndPartitionKey()));

        tableClient.listEntities(options, null, null).forEach(tableEntity -> {
            entities.add(tableEntity);
        });

        currentSplitId = nextSplit.splitId();
    }

    private AzureTableRecords finishSplit() throws IOException {
        tableClient = null;
        entities = null;

        final AzureTableRecords finishRecords = AzureTableRecords.finishedSplit(currentSplitId);
        currentSplitId = null;
        return finishRecords;
    }

    @Override
    public RecordsWithSplitIds<AzureTableRecordAndPosition> fetch() throws IOException {
        checkSplitOrStartNext();

        final TableEntity entity = entities.poll();
        if (entity == null) {
            return finishSplit();
        }

        final GenericRowData rowData = new GenericRowData(1);
        rowData.setField(0, StringData.fromString(entity.getProperty("AccountJournalId").toString()));
        final AzureTableRecordAndPosition record = new AzureTableRecordAndPosition(rowData, Long.valueOf(entity.getPartitionKey()));

        return AzureTableRecords.forRecords(currentSplitId, Collections.singleton(record));
    }

    @Override
    public void handleSplitsChanges(SplitsChange<AzureTableSourceSplit> splitsChanges) {
        if (!(splitsChanges instanceof SplitsAddition)) {
            throw new UnsupportedOperationException(
                    String.format(
                            "The SplitChange type of %s is not supported.",
                            splitsChanges.getClass()));
        }

        splits.addAll(splitsChanges.splits());
    }

    @Override
    public void wakeUp() {}

    @Override
    public void close() throws Exception {
        this.tableClient = null;
    }
}
