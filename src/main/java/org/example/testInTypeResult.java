package org.example;

import org.example.dbconnector.DatabaseConnectorConfig;
import org.example.dbconnector.DbConnector;
import org.example.dbconnector.adapter.PgConnector;
import org.example.utils.exception.MainException;

import java.sql.SQLException;
import java.util.List;

public class testInTypeResult {

    public static void main(String[] args) throws MainException, SQLException {
        GenerateData();
    }

    public static void GenerateData() throws MainException, SQLException {
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("biui.me", "5432", "postgres", "Biui1227..", "tpcds");
        DbConnector dbConnector1 = new PgConnector(config1);
        List<String> allDistinctPara = dbConnector1.getAllDistinctString("i_brand", "item");
        int tableSize = dbConnector1.getTableSize("item");
        long nullFow = dbConnector1.getNullRow("item", "i_brand");
        System.out.println(nullFow);
    }
}
