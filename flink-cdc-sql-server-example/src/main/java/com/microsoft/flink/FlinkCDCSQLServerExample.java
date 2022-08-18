package com.microsoft.flink;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class FlinkCDCSQLServerExample {

    public static void main(String... args) throws Exception {
        ParameterTool parameterTool = ParameterTool.fromPropertiesFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("flink.properties"))
                .mergeWith(ParameterTool.fromArgs(args))
                .mergeWith(ParameterTool.fromSystemProperties());

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        String cdcEmployeeDDL = "CREATE TABLE Employee\n" +
                "(\n" +
                "  EmpId INT,\n" +
                "  LastName STRING,\n" +
                "  FirstName STRING,\n" +
                "  Address STRING,\n" +
                "  City STRING\n" +
                ") WITH (\n" +
                " 'connector' = 'sqlserver-cdc',\n" +
                " 'hostname' = '',\n" +
                " 'port' = '1433',\n" +
                " 'username' = '',\n" +
                " 'password' = '',\n" +
                " 'database-name' = 'flink',\n" +
                " 'schema-name' = 'dbo',\n" +
                " 'table-name' = 'Employee',\n" +
                " 'debezium.snapshot.isolation.mode' = 'read_committed',\n" +
                " 'debezium.snapshot.lock.timeout.ms' = '-1'\n" +
                ")";
        tableEnv.executeSql(cdcEmployeeDDL);
        Table employee = tableEnv.sqlQuery("select * from Employee");
        DataStream<Row> dataStream = tableEnv.toChangelogStream(employee);
        dataStream.print();

        env.execute();
    }

}