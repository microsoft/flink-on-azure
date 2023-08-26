# Flink Streaming Example

## Prerequisites

* [Java Development Kit (JDK) 1.8](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html)
* [Apache Maven](http://maven.apache.org/download.cgi) and [install](http://maven.apache.org/install.html) a Maven binary archive

## Build Flink Job



```
mvn clean package
```

## Custom Flink Streaming Source & Sink

Flink has 3 types of custom streaming source:

* Implements SourceFunction with single parallelism.
* Implements ParallelSourceFunction with multi parallelism.
* Implements RichParallelSourceFunction with multi parallelism, with open() to initialize resource, close() to dispose resource.

Flink has 2 types of custom streaming sink:

* Implements SinkFunction with single parallelism.
* Implements RichSinkFunction with single parallelism, with open() to initialize resource, close() to dispose resource.

## Flink Batch Stream Alignment

Flink has raised new kind of Source & Sink API for Batch Stream Alignment

* [FLIP-27: Refactor Source Interface](https://cwiki.apache.org/confluence/display/FLINK/FLIP-27%3A+Refactor+Source+Interface)
* [FLIP-143: Unified Sink API](https://cwiki.apache.org/confluence/display/FLINK/FLIP-143%3A+Unified+Sink+API)
