package org.example.generator;

import org.example.solver.LikeType;
import org.example.utils.exception.MainException;

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
    private List<LikeType> LikeTypes;

    public void writeNewSql() throws IOException, MainException {
        FileWriter fw = new FileWriter(new File(writeSqlPath));
        BufferedWriter bw = new BufferedWriter(fw);
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
        for (int i = 0; i < LikeTypes.size(); i++) {
            if (LikeTypes.get(i).isOnlyBehindMatch()) {
                String eachLine = tableName + "." + colName + " like " + "'" + likeParaValues[i] + "%'";
                bw.write(eachLine + System.lineSeparator());
            } else if (LikeTypes.get(i).isOnlyFrontMatch()) {
                StringBuilder reverseString = new StringBuilder(likeParaValues[i]);
                reverseString = reverseString.reverse();
                String eachLine = tableName + "." + colName + " like " + "'%" + reverseString + "'";
                bw.write(eachLine + System.lineSeparator());
            } else {
                //throw new MainException("暂时不支持的情况");
                String likePattern = likeParaValues[i];
                String eachLine = tableName + "." + colName + " like " + "'%" + likeParaValues[i] + "%'";
                bw.write(eachLine + System.lineSeparator());
            }
        }
        bw.close();
        fw.close();
    }

    public SqlWriter(String[] allParaValues, String[] likeParaValues, List<long[]> paraPresent, String tableName, String colName, int inTypeSize, List<LikeType> LikeTypes) {
        this.allParaValues = allParaValues;
        this.likeParaValues = likeParaValues;
        this.paraPresent = paraPresent;
        this.colName = colName;
        this.tableName = tableName;
        this.inTypeSize = inTypeSize;
        this.LikeTypes = LikeTypes;
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

    public List<LikeType> getLikeTypes() {
        return LikeTypes;
    }

    public void setLikeTypes(List<LikeType> likeTypes) {
        LikeTypes = likeTypes;
    }
}
