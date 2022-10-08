package com.microsoft.flink.source;

import org.apache.flink.util.FlinkRuntimeException;

import javax.annotation.Nullable;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class AzureTableSourceSplitState {
    private final AzureTableSourceSplit split;
    private @Nullable Long offset;

    public AzureTableSourceSplitState(AzureTableSourceSplit split) {
        this.split = checkNotNull(split);
        this.offset = split.getOffset();
    }

    public void setOffset(@Nullable Long partitionKey) {
        this.offset = partitionKey - this.split.getStartPartitionKey();
    }

    public AzureTableSourceSplit toAzureTableSourceSplit() {
        final AzureTableSourceSplit updatedSplit = split.updateWithCheckpointedPosition(offset);

        // some sanity checks to avoid surprises and not accidentally lose split information
        if (updatedSplit == null) {
            throw new FlinkRuntimeException(
                    "Split returned 'null' in updateWithCheckpointedPosition(): " + split);
        }
        if (updatedSplit.getClass() != split.getClass()) {
            throw new FlinkRuntimeException(
                    String.format(
                            "Split returned different type in updateWithCheckpointedPosition(). "
                                    + "Split type is %s, returned type is %s",
                            split.getClass().getName(), updatedSplit.getClass().getName()));
        }

        return updatedSplit;
    }
}

