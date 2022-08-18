package com.microsoft.flink.source;

import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;

public class StreamingParallelSource implements ParallelSourceFunction<String> {
    private Boolean flag = true;

    private Integer index = 0;

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
}
