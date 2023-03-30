package org.example;

import org.example.dbconnector.DatabaseConnectorConfig;
import org.example.dbconnector.DbConnector;
import org.example.dbconnector.adapter.PgConnector;
import org.example.utils.exception.MainException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class testResult {

    public static String inputPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\newSql.txt";
    public static String originPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\inputTest.txt";
    public static String sqlHead = "select count(*) from item where ";

    public static void main(String[] args) throws MainException, SQLException, IOException {
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("biui.me", "5432", "postgres", "Biui1227..", "tpcdsonecolumn");
        DbConnector dbConnector1 = new PgConnector(config1);
        DatabaseConnectorConfig config2 = new DatabaseConnectorConfig("biui.me", "5432", "postgres", "Biui1227..", "tpcds");
        DbConnector dbConnector2 = new PgConnector(config2);
        List<String> sqls = getEachLine(inputPath);
        List<String> sqls2 = getEachLine(originPath);
        for (int i = 0; i < sqls.size(); i++) {
            String sql = sqlHead + sqls.get(i);
            String sql2 = sqlHead + sqls2.get(i).split("=")[0].trim();
            int result = dbConnector1.getSqlResult(sql);
            int result2 = dbConnector2.getSqlResult(sql2);
            int tableSize = 18000;
            if (result == result2) {
                System.out.println("yes " + result + " " + result2);
            } else {
                System.out.println("no");
            }
            /*BigDecimal sizes = new BigDecimal(result);
            BigDecimal allsizes = new BigDecimal(tableSize);
            System.out.println(sizes.divide(allsizes, 6, RoundingMode.HALF_UP));*/
        }
    }

    public static List<String> getEachLine(String intputPath) {
        File file = new File(intputPath);
        List<String> eachLine = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            eachLine = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eachLine;
    }
}
