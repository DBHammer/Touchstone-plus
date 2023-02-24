package org.example;

import org.example.dbconnector.DatabaseConnectorConfig;
import org.example.dbconnector.DbConnector;
import org.example.dbconnector.adapter.PgConnector;
import org.example.utils.exception.MainException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class generateInput {
    public static void main(String[] args) throws MainException, SQLException {
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("biui.me", "5432", "postgres", "Biui1227..", "tpch1");
        DbConnector dbConnector1 = new PgConnector(config1);
        List<String> a = dbConnector1.getAllDistinctString("l_shipmode","lineitem");
        int tableSize = dbConnector1.getTableSize("lineitem");
        int outputCount = dbConnector1.getSqlResult("select count(*) from lineitem where l_shipmode in ('AIR', 'TRUCK')");
    }
}
