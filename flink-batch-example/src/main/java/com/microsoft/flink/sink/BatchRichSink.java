package com.microsoft.flink.sink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class BatchRichSink extends RichSinkFunction<String> {

    @Override
    public void open(Configuration parameters) throws Exception {

    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        System.out.println(value);
    }

    @Override
    public void close() throws Exception {

    }
}
