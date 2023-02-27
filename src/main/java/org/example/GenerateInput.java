package org.example;

import org.example.dbconnector.DatabaseConnectorConfig;
import org.example.dbconnector.DbConnector;
import org.example.dbconnector.adapter.PgConnector;
import org.example.utils.exception.MainException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class GenerateInput {

    public static String inputPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\inputTest.txt";

    public static void main(String[] args) throws MainException, SQLException, IOException {
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("biui.me", "5432", "postgres", "Biui1227..", "tpcds");
        DbConnector dbConnector1 = new PgConnector(config1);
        List<String> allDistinctPara = dbConnector1.getAllDistinctString("i_brand", "item");
        int tableSize = dbConnector1.getTableSize("item");
        Random r = new Random();
        FileWriter fw = new FileWriter(new File(inputPath));
        BufferedWriter bw = new BufferedWriter(fw);
        for (int i = 0; i < 100; i++) {
            int start = r.nextInt(100) + 1;
            int length = r.nextInt(10) + 2;
            List<String> eachInList = allDistinctPara.subList(start, start + length);
            StringBuilder sql = new StringBuilder("select count(*) from item where i_brand in (");
            for (String eachIn : eachInList) {
                sql.append("'").append(eachIn.trim()).append("', ");
            }
            sql = new StringBuilder(sql.substring(0, sql.length() - 2) + ");");
            int outputCount = dbConnector1.getSqlResult(sql.toString());
            BigDecimal sizes = new BigDecimal(outputCount);
            BigDecimal allsizes = new BigDecimal(tableSize);
            BigDecimal percentage = sizes.divide(allsizes, 6, RoundingMode.HALF_UP);
            String probability = percentage.toString();
            probability = probability.replaceAll("0+$", "");//去掉末尾的0
            StringBuilder eachline = new StringBuilder("i_brand in (");
            for (String eachIn : eachInList) {
                eachline.append(eachIn.trim()).append(", ");

            }
            eachline = new StringBuilder(eachline.substring(0, eachline.length() - 2) + ") = " + probability);
            bw.write(eachline + "\n");
            System.out.println(eachline);
        }
        bw.close();
        fw.close();
        //int outputCount = dbConnector1.getSqlResult("select count(*) from lineitem where l_shipmode in ('AIR', 'TRUCK')");
    }
}
