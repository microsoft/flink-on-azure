package com.microsoft.flink.source;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class StreamingSource implements SourceFunction<String> {
    private Boolean flag = true;

    private Integer index = 0;

    @Override
    public void run(SourceContext<String> ctx) throws Exception {

        final Object checkpointLock = ctx.getCheckpointLock();
        while (flag) {
            index++;
            if (index % 2 == 1) {
                ctx.collect(index.toString());
            } else {
                Thread.sleep(1000);
            }
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }
}
