package org.example;

import org.chocosolver.solver.constraints.graph.symmbreaking.Pair;
import org.example.dbconnector.DatabaseConnectorConfig;
import org.example.dbconnector.DbConnector;
import org.example.dbconnector.adapter.PgConnector;
import org.example.utils.exception.MainException;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class testResult {

    public static String inputPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\newSql.txt";
    public static String originPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\inputTest.txt";
    public static String originLikePath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\inputLikeTest.txt";
    public static String sqlHead = "select count(*) from part where ";
    public static String sqlHeadLike = "select count(*) from part where ";
    public static String sqlHead2 = "select count(*) from item where ";
    public static String sqlHeadIn = "select count(*) from item where ";

    public static void main(String[] args) throws MainException, SQLException, IOException, ExecutionException, InterruptedException {
        //testLike();
        testAll();
    }

    public static void testLike() throws MainException, SQLException {
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("wqs97.click", "5432", "postgres", "Biui1227..", "likecolumngen");
        DbConnector dbConnector1 = new PgConnector(config1);
        DatabaseConnectorConfig config2 = new DatabaseConnectorConfig("wqs97.click", "5432", "postgres", "Biui1227..", "likecolumn");
        DbConnector dbConnector2 = new PgConnector(config2);
        List<String> sqls = getEachLine(inputPath);
        List<String> sqls2 = getEachLine(originLikePath);
        for (int i = 0; i < sqls.size(); i++) {
            String sql = sqlHeadLike + sqls.get(i);
            String sql2 = sqlHeadLike + sqls2.get(i).split("=")[0].trim();
            int result = dbConnector1.getSqlResult(sql);
            int result2 = dbConnector2.getSqlResult(sql2);
            int tableSize = 1000;
            if (result == result2) {
                System.out.println("yes " + result + " " + result2);
            } else {
                System.out.println("no " + result + " " + result2);
            }
        }
    }

    public static void testAll() throws MainException, SQLException, IOException, ExecutionException, InterruptedException {
        DatabaseConnectorConfig config1 = new DatabaseConnectorConfig("wqs97.click", "5432", "postgres", "Biui1227..", "tpchonecolumn");
        DbConnector dbConnector1 = new PgConnector(config1);
        DatabaseConnectorConfig config2 = new DatabaseConnectorConfig("wqs97.click", "5432", "postgres", "Biui1227..", "tpch1");
        DbConnector dbConnector2 = new PgConnector(config2);
        List<String> sqls = getEachLine(inputPath);
        List<String> sqls2 = getEachLine(originPath);
        List<Pair<String, String>> new2Olds = new ArrayList<>();
        for (int i = 0; i < sqls.size(); i++) {
            Pair<String, String> s = new Pair<>(sqls.get(i), sqls2.get(i));
            new2Olds.add(s);
        }
        AtomicInteger sum = new AtomicInteger(0);
        new ForkJoinPool(8).submit(() -> new2Olds.parallelStream().forEach(eachNew2Old -> {
            String sql = sqlHead + eachNew2Old.getA();
            String sql2 = sqlHead + eachNew2Old.getB().split("=")[0].trim();
            double result = 0;
            try {
                result = dbConnector1.getSqlResult(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            double result2 = 0;
            try {
                result2 = dbConnector2.getSqlResult(sql2);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (result == result2) {
                System.out.println("yes " + result + " " + result2);
            } else {
                double error = Math.abs(result - result2) / result2;
                sum.addAndGet((int) (error*100000));
                if (error <= 0.06) {
                    System.out.println("no but" + error);
                    //System.out.println("no but" + result + " " + result2);
                } else {
                    System.out.println("shit" + error);
                    //System.out.println("shit " + result + " " + result2);
                }
            }

        })).get();
        System.out.println((double) sum.get() / 100000/25);
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
