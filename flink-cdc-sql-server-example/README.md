# Flink CDC SQL Server Connector Example

## Prerequisites

* [Java Development Kit (JDK) 1.8](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html)
* [Apache Maven](http://maven.apache.org/download.cgi) and [install](http://maven.apache.org/install.html) a Maven binary archive

## Build Flink Job

```
mvn clean package
```

## SQL Server CDC Example

create a **flink** database and create a **Employee** table

```
CREATE DATABASE flink

CREATE TABLE Employee(  
    EmpId INT PRIMARY KEY,  
    LastName VARCHAR(255),  
    FirstName VARCHAR(255),  
    Address VARCHAR(255),  
    City VARCHAR(255)   
); 

INSERT INTO Employee (EmpId, LastName, FirstName, ADDRESS, City) VALUES (1, 'XYZ', 'ABC', 'India', 'Mumbai'); 
INSERT INTO Employee (EmpId, LastName, FirstName, ADDRESS, City) VALUES (2, 'X', 'A', 'India', 'Pune');
```

enable SQL Server CDC

```
EXEC sys.sp_cdc_enable_db 

EXEC sys.sp_cdc_enable_table @source_schema = 'dbo', @source_name = 'Employee', @role_name = NULL, @supports_net_changes = 1, @capture_instance = 'dbo_Employee_v1';
```

## Flink CDC Example

Fill your 

* hostname
* username
* password 

on FlinkCDCSQLServerExample

### Debezium SQL Server Table Lock

debezium.snapshot.isolation.mode = read_committed
debezium.snapshot.lock.timeout.ms = -1

in the near future, debezium will work with sql server to push out lock-free solution like MySQL

