package com.microsoft.flink;

import com.microsoft.flink.sink.BatchRichSink;
import com.microsoft.flink.source.AzureTableSource;
import com.microsoft.flink.source.AzureTableStaticSource;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.RowData;

public class FlinkStreamBatchUnifiedExample {

    public static void main(String... args) throws Exception {
        ParameterTool parameterTool = ParameterTool.fromPropertiesFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("flink.properties"))
                .mergeWith(ParameterTool.fromArgs(args))
                .mergeWith(ParameterTool.fromSystemProperties());

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        String connectionString = "";
        String tableName = "";
        Long startPartitionKey = 1L;
        Long endPartitionKey = 3L;

        AzureTableStaticSource boundedSource = new AzureTableStaticSource(
                connectionString,
                tableName,
                startPartitionKey,
                endPartitionKey);
        DataStream<RowData> dataStream1 = env.fromSource(boundedSource, WatermarkStrategy.noWatermarks(), "azure-table-bounded");

        AzureTableSource unboundedSource = new AzureTableSource(
                        connectionString,
                        tableName,
                        startPartitionKey,
                        endPartitionKey);
        DataStream<RowData> dataStream2 = env.fromSource(unboundedSource, WatermarkStrategy.noWatermarks(), "azure-table-unbounded");

        DataStreamSource<String> source = env.fromElements("a", "b");
        source.map(value -> value).addSink(new BatchRichSink());

        env.execute();
    }

}