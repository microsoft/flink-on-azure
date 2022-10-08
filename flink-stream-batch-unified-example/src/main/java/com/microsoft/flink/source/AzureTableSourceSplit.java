package com.microsoft.flink.source;

import org.apache.flink.api.connector.source.SourceSplit;

import javax.annotation.Nullable;
import java.io.Serializable;

import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.util.Preconditions.checkArgument;


public class AzureTableSourceSplit implements SourceSplit, Serializable {

    private static final long serialVersionUID = 1L;
    private final Long startPartitionKey;
    private final Long endPartitionKey;
    private Long offset;
    public AzureTableSourceSplit(
            Long startPartitionKey,
            Long endPartitionKey,
            Long offset) {
        checkArgument(startPartitionKey >= 0, "startPartitionKey must be >= 0");
        checkArgument(endPartitionKey >= 0, "endPartitionKey must be >= 0");
        this.startPartitionKey = startPartitionKey;
        this.endPartitionKey = endPartitionKey;
        this.offset = offset;
    }

    @Override
    public String splitId() {
        return this.startPartitionKey.toString();
    }

    public Long getStartPartitionKey() {
        return this.startPartitionKey;
    }

    public Long getEndPartitionKey() {
        return this.endPartitionKey;
    }

    public Long getOffset() {
        return this.offset;
    }

    public AzureTableSourceSplit updateWithCheckpointedPosition(@Nullable Long offset) {
        return new AzureTableSourceSplit(this.startPartitionKey, this.endPartitionKey, offset);
    }

    @Override
    public String toString() {
        return "AzureTableSourceSplit{"
                + "id='"
                + startPartitionKey.toString()
                + '\''
                + ", startPartitionKey="
                + startPartitionKey
                + ", endPartitionKey="
                + endPartitionKey
                + ", offset="
                + offset
                + '}';
    }
}
