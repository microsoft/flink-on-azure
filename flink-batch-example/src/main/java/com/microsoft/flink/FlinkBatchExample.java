package com.microsoft.flink;

import com.microsoft.flink.sink.BatchRichSink;
import com.microsoft.flink.sink.BatchSink;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkBatchExample {

    public static void main(String... args) throws Exception {
        ParameterTool parameterTool = ParameterTool.fromPropertiesFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("flink.properties"))
                .mergeWith(ParameterTool.fromArgs(args))
                .mergeWith(ParameterTool.fromSystemProperties());

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        DataStreamSource<String> source = env.fromElements("a", "b");
        source.map(value -> value).addSink(new BatchRichSink());

        env.execute();
    }

}