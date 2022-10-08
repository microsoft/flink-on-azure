package com.microsoft.flink.source;

import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;

import java.io.IOException;

import static org.apache.flink.util.Preconditions.checkArgument;

public class AzureTableSourceSplitSerializer implements SimpleVersionedSerializer<AzureTableSourceSplit> {
    public static final AzureTableSourceSplitSerializer INSTANCE = new AzureTableSourceSplitSerializer();

    private static final ThreadLocal<DataOutputSerializer> SERIALIZER_CACHE =
            ThreadLocal.withInitial(() -> new DataOutputSerializer(64));

    private static final int VERSION = 1;

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public byte[] serialize(AzureTableSourceSplit split) throws IOException {
        checkArgument(
                split.getClass() == AzureTableSourceSplit.class,
                "Cannot serialize subclasses of AzureTableSourceSplit");

        final DataOutputSerializer out = SERIALIZER_CACHE.get();

        out.writeLong(split.getStartPartitionKey());
        out.writeLong(split.getEndPartitionKey());
        out.writeLong(split.getOffset());

        final byte[] result = out.getCopyOfBuffer();
        out.clear();

        return result;
    }

    @Override
    public AzureTableSourceSplit deserialize(int version, byte[] serialized) throws IOException {
        if (version != VERSION) {
            throw new IOException("Unknown version: " + version);
        }
        final DataInputDeserializer in = new DataInputDeserializer(serialized);

        final Long startPartitionKey = in.readLong();
        final Long endPartitionKey = in.readLong();
        final Long offset = in.readLong();

        return new AzureTableSourceSplit(startPartitionKey, endPartitionKey, offset);
    }
}

