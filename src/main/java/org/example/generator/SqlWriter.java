package org.example.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqlWriter {
    private String[] inStrs;
    private String[] likeStrs;
    private long[] paraNums;
    private List<long[]> paraPresent;
    private String writeSqlPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\newSql.txt";
    private String tableName;
    private String colName;

    public void writeNewSql() throws IOException {
        List<List<String>> allParas = new ArrayList<>();
        for (int i = 0; i < paraNums.length; i++) {
            List<String> eachSqlParas = new ArrayList<>();
            long[] isPresent = paraPresent.get(i);
            for (int j = 0; j < inStrs.length; j++) {
                if (isPresent[j] == 1) {
                    eachSqlParas.add(inStrs[j]);
                }
            }
            allParas.add(eachSqlParas);
        }
        FileWriter fw = new FileWriter(new File(writeSqlPath));
        BufferedWriter bw = new BufferedWriter(fw);
        for (List<String> allPara : allParas) {
            String eachLine = tableName + "." + colName + " in " + "(";
            for (int i = 0; i < allPara.size(); i++) {
                if (i == allPara.size() - 1) {
                    eachLine += "'" + allPara.get(i) + "'";
                } else {
                    eachLine += "'" + allPara.get(i) + "', ";
                }
            }
            eachLine += ")";
            bw.write(eachLine + System.lineSeparator());
        }
        bw.close();
        fw.close();
    }

    public SqlWriter(String[] strs, String[] likeStrs, long[] paraNums, List<long[]> paraPresent, String tableName, String colName) {
        this.inStrs = strs;
        this.likeStrs = likeStrs;
        this.paraNums = paraNums;
        this.paraPresent = paraPresent;
        this.colName = colName;
        this.tableName = tableName;
    }

    public String[] getStrs() {
        return inStrs;
    }

    public void setStrs(String[] strs) {
        this.inStrs = strs;
    }

    public String[] getLikeStrs() {
        return likeStrs;
    }

    public void setLikeStrs(String[] likeStrs) {
        this.likeStrs = likeStrs;
    }

    public long[] getParaNums() {
        return paraNums;
    }

    public void setParaNums(long[] paraNums) {
        this.paraNums = paraNums;
    }

    public List<long[]> getParaPresent() {
        return paraPresent;
    }

    public void setParaPresent(List<long[]> paraPresent) {
        this.paraPresent = paraPresent;
    }
}
