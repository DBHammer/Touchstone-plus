package org.example.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqlWriter {
    private String[] allParaValues;
    private String[] likeParaValues;
    private List<long[]> paraPresent;
    private String writeSqlPath = "conf/newSql.txt";
    private String tableName;
    private String colName;
    private int inTypeSize;
    private int likeTypeSize;

    public void writeNewSql() throws IOException {
        FileWriter fw = new FileWriter(new File(writeSqlPath));
        BufferedWriter bw = new BufferedWriter(fw);
        /*for (List<String> allPara : allInParas) {
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
        for (int i = 0; i < likeStrs.length; i++) {
            String eachLine = tableName + "." + colName + " like " + "'" + likeStrs[i] + "%';";
            bw.write(eachLine + System.lineSeparator());
        }*/
        for (int i = 0; i < inTypeSize; i++) {
            String eachLine = tableName + "." + colName + " in " + "(";
            long[] isPresent = paraPresent.get(i);
            List<String> eachPredicateParas = new ArrayList<>();
            for (int j = 0; j < isPresent.length; j++) {
                if (isPresent[j] == 1) {
                    eachPredicateParas.add(allParaValues[j]);
                }
            }
            for (int k = 0; k < eachPredicateParas.size(); k++) {
                if (k == eachPredicateParas.size() - 1) {
                    eachLine += "'" + eachPredicateParas.get(k) + "'";
                } else {
                    eachLine += "'" + eachPredicateParas.get(k) + "', ";
                }
            }
            eachLine += ")";
            bw.write(eachLine + System.lineSeparator());
        }
        for (int i = 0; i < likeTypeSize; i++) {
            String eachLine = tableName + "." + colName + " like " + "'" + likeParaValues[i] + "%'";
            bw.write(eachLine + System.lineSeparator());
        }
        bw.close();
        fw.close();
    }

    public SqlWriter(String[] allParaValues, String[] likeParaValues, List<long[]> paraPresent, String tableName, String colName, int inTypeSize, int likeTypeSize) {
        this.allParaValues = allParaValues;
        this.likeParaValues = likeParaValues;
        this.paraPresent = paraPresent;
        this.colName = colName;
        this.tableName = tableName;
        this.inTypeSize = inTypeSize;
        this.likeTypeSize = likeTypeSize;
    }

    public String[] getAllParaValues() {
        return allParaValues;
    }

    public void setAllParaValues(String[] allParaValues) {
        this.allParaValues = allParaValues;
    }


    public List<long[]> getParaPresent() {
        return paraPresent;
    }

    public void setParaPresent(List<long[]> paraPresent) {
        this.paraPresent = paraPresent;
    }

    public int getInTypeSize() {
        return inTypeSize;
    }

    public void setInTypeSize(int inTypeSize) {
        this.inTypeSize = inTypeSize;
    }

    public int getLikeTypeSize() {
        return likeTypeSize;
    }

    public void setLikeTypeSize(int likeTypeSize) {
        this.likeTypeSize = likeTypeSize;
    }
}
