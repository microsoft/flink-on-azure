# Flink Stream/Batch Unified Connector API Example

## Prerequisites

* [Java Development Kit (JDK) 1.8](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html)
* [Apache Maven](http://maven.apache.org/download.cgi) and [install](http://maven.apache.org/install.html) a Maven binary archive

## Build Flink Job

```
mvn clean package
```

## Custom Flink Stream/Batch Unified Source & Sink API

### source

* [FLIP-27: Refactor Source Interface](https://cwiki.apache.org/confluence/display/FLINK/FLIP-27%3A+Refactor+Source+Interface)

### sink

* [FLIP-143: Unified Sink API](https://cwiki.apache.org/confluence/display/FLINK/FLIP-143%3A+Unified+Sink+API)
* Implements SinkFunction with single parallelism.
* Implements RichSinkFunction with single parallelism, with open() to initialize resource, close() to dispose resource.
