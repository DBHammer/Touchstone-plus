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
import java.util.*;

public class GenerateInput {

    public static String inputPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\inputTest.txt";
    public static String likeInputPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\inputLikeTest.txt";
    public static String likeColPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\like.txt";
    public static String likeQueryPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\likeQuery.txt";

    public static void main(String[] args) throws MainException, SQLException, IOException {
        //generateInTypeAndLikeType();
        generateInTypeCol();
        //generateLikeTypeCol();
        //writeLikeQuery();
    }

    public static void generateInTypeCol() throws MainException, SQLException, IOException {
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("biui.me", "5432", "postgres", "Biui1227..", "tpcds");
        DbConnector dbConnector1 = new PgConnector(config1);
        List<String> allDistinctPara = dbConnector1.getAllDistinctString("i_brand", "item");
        int tableSize = dbConnector1.getTableSize("item");
        Random r = new Random();
        FileWriter fw = new FileWriter(new File(inputPath));
        BufferedWriter bw = new BufferedWriter(fw);
        HashSet<Map<Integer, Integer>> range = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(allDistinctPara);
            int length = r.nextInt(5) + 1;
            List<String> eachInList = allDistinctPara.subList(0, length);
            StringBuilder sql = new StringBuilder("select count(*) from item where i_brand in (");
            for (String eachIn : eachInList) {
                sql.append("'").append(eachIn.trim()).append("', ");
            }
            sql = new StringBuilder(sql.substring(0, sql.length() - 2) + ");");
            int outputCount = dbConnector1.getSqlResult(sql.toString());
            StringBuilder eachline = new StringBuilder("item.i_brand in (");
            for (String eachIn : eachInList) {
                eachline.append("'").append(eachIn.trim()).append("'").append(", ");
            }
            eachline = new StringBuilder(eachline.substring(0, eachline.length() - 2) + ") = " + outputCount);
            bw.write(eachline + System.lineSeparator());
        }

        bw.close();
        fw.close();
        //int outputCount = dbConnector1.getSqlResult("select count(*) from lineitem where l_shipmode in ('AIR', 'TRUCK')");
    }

    /**
     * 生成like类型泪的函数
     * 假设列一共有1000行，字符串长度为10
     */
    public static void generateLikeTypeCol() throws IOException {
        String[] likeString = getRandomStringArray(10, 1000);
        FileWriter fw = new FileWriter(new File(likeColPath));
        BufferedWriter bw = new BufferedWriter(fw);
        for (int i = 0; i < likeString.length; i++) {
            bw.write(likeString[i] + System.lineSeparator());
        }
        bw.close();
        fw.close();
    }

    public static String[] getRandomStringArray(int maxLength, int size) {
        String[] strs = new String[size];
        char[] chars = "abcde".toCharArray();
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        HashSet<String> set = new HashSet<>();
        int length;
        while (set.size() < size) {//生成随机字符串到set里面
            length = random.nextInt(maxLength - 1) + 1;
            sb.setLength(0);
            for (int i = 0; i < length; i++)
                sb.append(chars[random.nextInt(5)]);
            set.add(sb.toString());
        }
        int i = 0;
        for (String s : set)//将set里面的数据存放到数组
            strs[i++] = s;
        return strs;
    }

    public static void writeLikeQuery() throws MainException, SQLException, IOException {
        DatabaseConnectorConfig config = new DatabaseConnectorConfig("biui.me", "5432", "postgres", "Biui1227..", "likecolumn");
        DbConnector dbConnector = new PgConnector(config);
        String[] head = getRandomStringArray(3, 10);
        String queryHead = "select count(*) from t1 where col like ";
        FileWriter fw = new FileWriter(new File(likeInputPath));
        BufferedWriter bw = new BufferedWriter(fw);
        for (String s : head) {
            String sql = queryHead + "'" + s + "%'";
            int result = dbConnector.getSqlResult(sql);
            int tableSize = 1000;
            BigDecimal sizes = new BigDecimal(result);
            BigDecimal allsizes = new BigDecimal(tableSize);
            BigDecimal percentage = sizes.divide(allsizes, 6, RoundingMode.UP);
            String likeSql = "t1.col like '" + s + "%' = " + result;
            bw.write(likeSql + System.lineSeparator());
        }
        bw.close();
        fw.close();
    }

    public static void generateInTypeAndLikeType() throws MainException, SQLException, IOException {
        //对tpch里part表的p_type列生成随机的in约束
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("biui.me", "5432", "postgres", "Biui1227..", "tpch1");
        DbConnector dbConnector1 = new PgConnector(config1);
        List<String> allDistinctPara = dbConnector1.getAllDistinctString("p_type", "part");
        int tableSize = dbConnector1.getTableSize("part");
        Random r = new Random();
        FileWriter fw = new FileWriter(new File(inputPath));
        BufferedWriter bw = new BufferedWriter(fw);
        //生成in语句
        for (int i = 0; i < 500; i++) {
            Collections.shuffle(allDistinctPara);
            int length = r.nextInt(5) + 1;
            List<String> eachInList = allDistinctPara.subList(0, length);
            StringBuilder sql = new StringBuilder("select count(*) from part where p_type in (");
            for (String eachIn : eachInList) {
                sql.append("'").append(eachIn.trim()).append("', ");
            }
            sql = new StringBuilder(sql.substring(0, sql.length() - 2) + ");");
            int outputCount = dbConnector1.getSqlResult(sql.toString());
            StringBuilder eachline = new StringBuilder("part.p_type in (");
            for (String eachIn : eachInList) {
                eachline.append("'").append(eachIn.trim()).append("'").append(", ");
            }
            eachline = new StringBuilder(eachline.substring(0, eachline.length() - 2) + ") = " + outputCount);
            bw.write(eachline + System.lineSeparator());
        }

        //得到p_type列所有的开头
        HashSet<String> allDistinctHead = new HashSet<>();
        for (String s : allDistinctPara) {
            String[] headSplit = s.split(" ");
            int headLength = r.nextInt(2) + 1;
            String head = "";
            for (int i = 0; i < headLength; i++) {
                head += headSplit[i] + " ";
            }
            head = head.trim();
            allDistinctHead.add(head);
        }
        HashSet<Integer> chosenOne = new HashSet<>();
        /*for (int i = 0; i < 10; i++) {
            int likeStart = r.nextInt(30);
            while (chosenOne.contains(likeStart)) {
                likeStart = r.nextInt(30);
            }
            chosenOne.add(likeStart);
            String likeSqlPara = "p_type like ";
            StringBuilder likeSql = new StringBuilder("select count(*) from part where p_type like");
            String likePara = allDistinctHead.stream().toList().get(likeStart);
            likeSql.append(" '").append(likePara).append("%'");
            int outputCount = dbConnector1.getSqlResult(likeSql.toString());
            likeSql.append(" = ").append(outputCount);
            likeSqlPara = likeSqlPara + "'" + likePara + "%' = " + outputCount;
            bw.write(likeSqlPara + System.lineSeparator());
        }*/
        bw.close();
        fw.close();
    }


}
