package org.example;

import org.example.dbconnector.DatabaseConnectorConfig;
import org.example.dbconnector.DbConnector;
import org.example.dbconnector.adapter.PgConnector;
import org.example.utils.exception.MainException;

import java.io.*;
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
        //constructSF();
        //generateForExperiment();
        generateInTypeAndLikeType();
        //generateInTypeCol();
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
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("wqs97.click", "5432", "postgres", "Biui1227..", "tpch1");
        DbConnector dbConnector1 = new PgConnector(config1);
        DatabaseConnectorConfig config2 = new DatabaseConnectorConfig("wqs97.click", "5432", "postgres", "Biui1227..", "tpch2");
        DbConnector dbConnector2 = new PgConnector(config2);
        DatabaseConnectorConfig config3 = new DatabaseConnectorConfig("wqs97.click", "5432", "postgres", "Biui1227..", "tpch3");
        DbConnector dbConnector3 = new PgConnector(config3);
        List<String> allDistinctPara = dbConnector1.getAllDistinctString("p_type", "part");
        int tableSize1 = dbConnector1.getTableSize("part");
        Random r = new Random();
        FileWriter fw1 = new FileWriter(new File(inputPath));
        BufferedWriter bw1 = new BufferedWriter(fw1);
        FileWriter fw2 = new FileWriter(new File(inputPath.split("\\.")[0] + "2.txt"));
        BufferedWriter bw2 = new BufferedWriter(fw2);
        FileWriter fw3 = new FileWriter(new File(inputPath.split("\\.")[0] + "3.txt"));
        BufferedWriter bw3 = new BufferedWriter(fw3);
        //生成in语句
        for (int i = 0; i < 0; i++) {
            Collections.shuffle(allDistinctPara);
            int length = r.nextInt(5) + 1;
            List<String> eachInList = allDistinctPara.subList(0, length);
            StringBuilder sql = new StringBuilder("select count(*) from part where p_type in (");
            for (String eachIn : eachInList) {
                sql.append("'").append(eachIn.trim()).append("', ");
            }
            sql = new StringBuilder(sql.substring(0, sql.length() - 2) + ");");
            int outputCount1 = dbConnector1.getSqlResult(sql.toString());
            //同时计算SF1-3的行数
            int outputCount2 = dbConnector2.getSqlResult(sql.toString());
            int outputCount3 = dbConnector3.getSqlResult(sql.toString());
            StringBuilder eachline1 = new StringBuilder("part.p_type in (");
            StringBuilder eachline2 = new StringBuilder("part.p_type in (");
            StringBuilder eachline3 = new StringBuilder("part.p_type in (");
            for (String eachIn : eachInList) {
                eachline1.append("'").append(eachIn.trim()).append("'").append(", ");
                eachline2.append("'").append(eachIn.trim()).append("'").append(", ");
                eachline3.append("'").append(eachIn.trim()).append("'").append(", ");
            }
            //同时写SF1-3的负载
            eachline1 = new StringBuilder(eachline1.substring(0, eachline1.length() - 2) + ") = " + outputCount1);
            eachline2 = new StringBuilder(eachline2.substring(0, eachline2.length() - 2) + ") = " + outputCount2);
            eachline3 = new StringBuilder(eachline3.substring(0, eachline3.length() - 2) + ") = " + outputCount3);
            bw1.write(eachline1 + System.lineSeparator());
            bw2.write(eachline2 + System.lineSeparator());
            bw3.write(eachline3 + System.lineSeparator());
        }

        //得到p_type列所有的开头，中间和尾部
        HashSet<String> allDistinctHead = new HashSet<>();
        HashSet<String> allDistinctMid = new HashSet<>();
        HashSet<String> allDistinctTail = new HashSet<>();
        for (String s : allDistinctPara) {
            String[] headSplit = s.split(" ");
            //取随机长度的头
            int headLength = r.nextInt(2) + 1;
            String head = "";
            for (int i = 0; i < headLength; i++) {
                head += headSplit[i] + " ";
            }
            head = head.trim();
            //取随机长度的尾
            int tailLength = r.nextInt(2) + 1;
            String tail = "";
            for (int i = 3 - tailLength; i < 3; i++) {
                tail += headSplit[i] + " ";
            }
            tail = tail.trim();

            allDistinctHead.add(head);
            allDistinctMid.add(headSplit[1]);
            allDistinctTail.add(tail);
        }
        HashSet<Integer> chosenStart = new HashSet<>();
        HashSet<Integer> chosenMid = new HashSet<>();
        HashSet<Integer> chosenTail = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            //随机选择一个开头
            int likeStart = r.nextInt(0, 50);
            while (chosenStart.contains(likeStart)) {
                likeStart = r.nextInt(0, 50);
            }
            chosenStart.add(likeStart);

            String likeSqlPara = "part.p_type like ";
            StringBuilder likeSql = new StringBuilder("select count(*) from part where p_type like");
            String likePara = allDistinctPara.get(likeStart);
            likeSql.append(" '").append(likePara).append("%'");
            int outputCount = dbConnector1.getSqlResult(likeSql.toString());
            likeSql.append(" = ").append(outputCount);
            likeSqlPara = likeSqlPara + "'" + likePara + "%' = " + outputCount;
            bw1.write(likeSqlPara + System.lineSeparator());
        }
        for (int i = 0; i < 0; i++) {
            //随机选择一个中间
            int likeMid = r.nextInt(50, 100);
            while (chosenMid.contains(likeMid)) {
                likeMid = r.nextInt(50, 100);
            }
            chosenMid.add(likeMid);


            String likeSqlPara = "part.p_type like ";
            StringBuilder likeSql = new StringBuilder("select count(*) from part where p_type like");
            String likePara = allDistinctPara.get(likeMid);
            likeSql.append(" '").append("%").append(likePara).append("%'");
            int outputCount = dbConnector1.getSqlResult(likeSql.toString());
            likeSql.append(" = ").append(outputCount);
            likeSqlPara = likeSqlPara + "'" + "%" + likePara + "%' = " + outputCount;
            bw1.write(likeSqlPara + System.lineSeparator());
        }
        for (int i = 0; i < 0; i++) {
            //随机选择一个结尾
            int likeTail = r.nextInt(100, 150);
            while (chosenTail.contains(likeTail)) {
                likeTail = r.nextInt(100, 150);
            }
            chosenTail.add(likeTail);


            String likeSqlPara = "part.p_type like ";
            StringBuilder likeSql = new StringBuilder("select count(*) from part where p_type like");
            String likePara = allDistinctPara.get(likeTail);
            likeSql.append(" '").append("%").append(likePara).append("'");
            int outputCount = dbConnector1.getSqlResult(likeSql.toString());
            likeSql.append(" = ").append(outputCount);
            likeSqlPara = likeSqlPara + "'%" + likePara + "' = " + outputCount;
            bw1.write(likeSqlPara + System.lineSeparator());
        }
        bw1.close();
        fw1.close();

        bw2.close();
        fw2.close();
        bw3.close();
        fw3.close();
    }


    public static void generateForExperiment() throws MainException, SQLException, IOException {
        //对tpch里part表的p_type列生成随机的in约束
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("wqs97.click", "5432", "postgres", "Biui1227..", "tpcds");
        DbConnector dbConnector1 = new PgConnector(config1);
        List<String> allDistinctPara = dbConnector1.getAllDistinctString("ca_county", "customer_address");
        //allDistinctPara = allDistinctPara.subList(0,150);
        int tableSize = dbConnector1.getTableSize("customer_address");
        Random r = new Random();
        FileWriter fw = new FileWriter(new File(inputPath));
        BufferedWriter bw = new BufferedWriter(fw);
        //生成in语句
        for (int i = 0; i < 50; i++) {
            Collections.shuffle(allDistinctPara);
            int length = r.nextInt(5) + 1;
            List<String> eachInList = allDistinctPara.subList(0, length);
            StringBuilder sql = new StringBuilder("select count(*) from customer_address where ca_county in (");
            for (String eachIn : eachInList) {
                sql.append("'").append(eachIn.trim()).append("', ");
            }
            sql = new StringBuilder(sql.substring(0, sql.length() - 2) + ");");
            int outputCount = dbConnector1.getSqlResult(sql.toString());
            StringBuilder eachline = new StringBuilder("customer_address.ca_county in (");
            for (String eachIn : eachInList) {
                eachline.append("'").append(eachIn.trim()).append("'").append(", ");
            }
            eachline = new StringBuilder(eachline.substring(0, eachline.length() - 2) + ") = " + outputCount);
            bw.write(eachline + System.lineSeparator());
        }

        //得到p_type列所有的开头，中间和尾部
        HashSet<String> allDistinctHead = new HashSet<>();
        HashSet<String> allDistinctMid = new HashSet<>();
        HashSet<String> allDistinctTail = new HashSet<>();
        for (String s : allDistinctPara) {
            int len = s.split(" ").length;
            if (len == 3) {
                String[] headSplit = s.split(" ");
                //取随机长度的头
                int headLength = r.nextInt(2) + 1;
                String head = "";
                for (int i = 0; i < headLength; i++) {
                    head += headSplit[i] + " ";
                }
                head = head.trim();
                //取随机长度的尾
                int tailLength = r.nextInt(2) + 1;
                String tail = "";
                for (int i = 3 - tailLength; i < 3; i++) {
                    tail += headSplit[i] + " ";
                }
                tail = tail.trim();

                allDistinctHead.add(head);
                allDistinctMid.add(headSplit[1]);
                allDistinctTail.add(tail);
            }
            if (len == 2) {
                String head = s.split(" ")[0];
                String tail = s.split(" ")[1];
                allDistinctHead.add(head);
                //allDistinctMid.add(headSplit[1]);
                allDistinctTail.add(tail);
            }
            if (len == 1) {
                allDistinctHead.add(s);
                //allDistinctMid.add(headSplit[1]);
                allDistinctTail.add(s);
            }
        }
        HashSet<Integer> chosenStart = new HashSet<>();
        HashSet<Integer> chosenMid = new HashSet<>();
        HashSet<Integer> chosenTail = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            //随机选择一个开头
            int likeStart = r.nextInt(150);
            while (chosenStart.contains(likeStart)) {
                likeStart = r.nextInt(150);
            }
            chosenStart.add(likeStart);

            String likeSqlPara = "customer_address.ca_county like ";
            StringBuilder likeSql = new StringBuilder("select count(*) from customer_address where ca_county like");
            String likePara = allDistinctHead.stream().toList().get(likeStart);
            likeSql.append(" '").append(likePara).append("%'");
            int outputCount = dbConnector1.getSqlResult(likeSql.toString());
            likeSql.append(" = ").append(outputCount);
            likeSqlPara = likeSqlPara + "'" + likePara + "%' = " + outputCount;
            bw.write(likeSqlPara + System.lineSeparator());
        }
        for (int i = 0; i < 10; i++) {
            //随机选择一个中间
            int likeMid = r.nextInt(22);
            while (chosenMid.contains(likeMid)) {
                likeMid = r.nextInt(22);
            }
            chosenMid.add(likeMid);


            String likeSqlPara = "customer_address.ca_county like ";
            StringBuilder likeSql = new StringBuilder("select count(*) from customer_address where ca_county like");
            String likePara = allDistinctMid.stream().toList().get(likeMid);
            likeSql.append(" '").append("%").append(likePara).append("%'");
            int outputCount = dbConnector1.getSqlResult(likeSql.toString());
            likeSql.append(" = ").append(outputCount);
            likeSqlPara = likeSqlPara + "'" + "%" + likePara + "%' = " + outputCount;
            bw.write(likeSqlPara + System.lineSeparator());
        }
        for (int i = 0; i < 20; i++) {
            //随机选择一个结尾
            int likeTail = r.nextInt(72);
            while (chosenTail.contains(likeTail)) {
                likeTail = r.nextInt(72);
            }
            chosenTail.add(likeTail);


            String likeSqlPara = "customer_address.ca_county like ";
            StringBuilder likeSql = new StringBuilder("select count(*) from customer_address where ca_county like");
            String likePara = allDistinctTail.stream().toList().get(likeTail);
            likeSql.append(" '").append("%").append(likePara).append("'");
            int outputCount = dbConnector1.getSqlResult(likeSql.toString());
            likeSql.append(" = ").append(outputCount);
            likeSqlPara = likeSqlPara + "'%" + likePara + "' = " + outputCount;
            bw.write(likeSqlPara + System.lineSeparator());
        }
        bw.close();
        fw.close();
    }

    public static void constructSF() throws IOException, MainException, SQLException {
        List<String> allCC = readOutPutFile();
        FileWriter fw1 = new FileWriter(new File("D:\\eclipse-workspace\\multiStirngMatching\\conf\\inputTest100.txt"));
        BufferedWriter bw1 = new BufferedWriter(fw1);
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("wqs97.click", "5432", "postgres", "Biui1227..", "tpcds");
        DbConnector dbConnector1 = new PgConnector(config1);
        for (String s : allCC) {
            StringBuilder sql = new StringBuilder("select count(*) from customer_address where ");
            sql.append(s.split("=")[0]);
            int count = dbConnector1.getSqlResult(sql.toString());
            bw1.write(s.split("=")[0] + "= " + count*100 + System.lineSeparator());
        }
        bw1.close();
        fw1.close();
        allCC.size();
    }

    public static List<String> readOutPutFile() throws IOException {
        List<String> output = new ArrayList<>();
        FileInputStream inputStream = new FileInputStream("D:\\eclipse-workspace\\multiStirngMatching\\conf\\inputTest.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String str = null;
        while ((str = bufferedReader.readLine()) != null) {
            output.add(str);
        }
        inputStream.close();
        bufferedReader.close();
        return output;
    }
}
