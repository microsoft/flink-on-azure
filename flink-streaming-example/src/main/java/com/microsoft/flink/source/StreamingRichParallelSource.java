package com.microsoft.flink.source;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

public class StreamingRichParallelSource extends RichParallelSourceFunction<String> {

    private Boolean flag;

    private Integer index;

    public StreamingRichParallelSource() {

    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.flag = true;
        this.index = 0;
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {

        final Object checkpointLock = ctx.getCheckpointLock();
        while (flag) {
            index++;
            if (index % 2 == 1) {
                synchronized (checkpointLock) {
                    ctx.collect(index.toString());
                }
            } else {
                Thread.sleep(1000);
            }
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }

    @Override
    public void close() {
        flag = false;
    }
}
