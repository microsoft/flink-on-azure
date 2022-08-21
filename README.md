# Flink on Azure

Apache Flink is a framework and distributed processing engine for stateful computations over unbounded and bounded data streams. Flink has been designed to run in all common cluster environments, perform computations at in-memory speed and at any scale.

This repo provides examples of Flink integration with Azure, like Azure Kubernetes, Azure SQL Server, Azure Data Factory, etc.

## Examples

Outline the examples in the repository.

| Example | Description | Pipeline Status |
|-|-|-|
| [Flink Streaming Examples](flink-streaming-example) |  Examples for Flink Streaming, including custom source & sink |  |
| [Flink Batch Examples](flink-batch-example) |  Examples for Flink Batch, including custom sink (source is developing and update soon) |  |
| [Flink History Server](flink-history-server) |  Examples for Flink History Server |  |
| [Flink CDC SQL Server Examples](flink-cdc-sql-server-example) |  Examples for Flink CDC SQL Server Connector |  |
| [Flink on Native Azure Kubernetes](flink-on-native-azure-kubernetes) |  Examples for Flink Job on Native Azure Kubernetes |  |
| [Flink Azure Data Factory Cloud Native Extension](flink-adf-cloud-native-extension) |  Flink Azure Data Factory Cloud Native Extension |  |

## Prerequisites

Basic:

* [Git](https://www.git-scm.com/downloads)
* [Java Development Kit (JDK) 1.8](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html)
* [Apache Maven](http://maven.apache.org/download.cgi) and [install](http://maven.apache.org/install.html) a Maven binary archive
* [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)

Azure:

* [Azure Container Registry](https://azure.microsoft.com/en-us/services/container-registry/)
* [Azure Kubernetes](https://azure.microsoft.com/en-us/services/kubernetes-service/)
* [Azure SQL Server](https://azure.microsoft.com/en-us/services/sql-database/)
* [Azure Data Factory](https://azure.microsoft.com/en-us/services/data-factory/)
* [Azure Data Lake Storage Gen2](https://azure.microsoft.com/en-us/services/storage/data-lake-storage/#overview)

Flink:

* [Flink](https://downloads.apache.org/flink)


## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Trademarks

This project may contain trademarks or logos for projects, products, or services. Authorized use of Microsoft
trademarks or logos is subject to and must follow
[Microsoft's Trademark & Brand Guidelines](https://www.microsoft.com/en-us/legal/intellectualproperty/trademarks/usage/general).
Use of Microsoft trademarks or logos in modified versions of this project must not cause confusion or imply Microsoft sponsorship.
Any use of third-party trademarks or logos are subject to those third-party's policies.
