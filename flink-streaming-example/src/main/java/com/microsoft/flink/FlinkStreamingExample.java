package com.microsoft.flink;

import com.microsoft.flink.sink.StreamingRichSink;
import com.microsoft.flink.sink.StreamingSink;
import com.microsoft.flink.source.StreamingParallelSource;
import com.microsoft.flink.source.StreamingRichParallelSource;
import com.microsoft.flink.source.StreamingSource;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkStreamingExample {
    public static void main(String... args) throws Exception {
        ParameterTool parameterTool = ParameterTool.fromPropertiesFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("flink.properties"))
                .mergeWith(ParameterTool.fromArgs(args))
                .mergeWith(ParameterTool.fromSystemProperties());

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(new StreamingRichParallelSource())
                .map(value -> value)
                .addSink(new StreamingSink());

        env.execute();
    }

}