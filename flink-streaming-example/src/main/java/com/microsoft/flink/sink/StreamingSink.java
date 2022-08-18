package com.microsoft.flink.sink;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class StreamingSink implements SinkFunction<String> {

    @Override
    public void invoke(String value, Context context) throws Exception {
        System.out.println(value);
    }
}
