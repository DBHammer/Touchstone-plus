package org.example.generator;

import org.example.dbconnector.DbConnector;
import org.example.dbconnector.adapter.PgConnector;
import org.example.utils.CommonUtils;
import org.example.utils.TaskConfigurator;
import org.example.utils.exception.MainException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

import static org.chocosolver.util.tools.MathUtils.pow;
import static org.example.utils.CommonUtils.MAPPER;

public class DataWriter {
    private String[] strs;
    private long[] paraRows;
    private List<long[]> isPresent;
    private String tableName;
    private String colName;
    private long nullRows;
    private String dataPath;
    private long tableSize;

    public void generateData() throws IOException {
        FileWriter fw = new FileWriter(new File(dataPath));
        BufferedWriter bw = new BufferedWriter(fw);
        //bw.write(colName + "\n");
        for (int i = 0; i < nullRows; i++) {
            bw.write("\\N" + System.lineSeparator());
        }
        int num = strs.length;
        String otherStr = "";
        long currentRow = nullRows;
        for (int i = 0; i < num; i++) {
            boolean haveOnePresent = false;
            for (long[] ints : isPresent) {
                if (ints[i] == 1) {
                    haveOnePresent = true;
                }
            }
            if (haveOnePresent) {
                long row = paraRows[i];
                System.out.println(row);
                for (int j = 0; j < row; j++) {
                    bw.write(strs[i] + System.lineSeparator());
                }
                currentRow += row;
            } else {
                otherStr = strs[i];
            }
        }
        for (long i = 0; i < tableSize - currentRow; i++) {
            bw.write(otherStr + System.lineSeparator());
        }
        bw.close();
        fw.close();
    }


    public DataWriter(String[] strs, long[] paraProbability, List<long[]> isPresent,
                      String tableName, String colName, long nullRows, String dataPath, long tableSize) {
        this.strs = strs;
        this.paraRows = paraProbability;
        this.isPresent = isPresent;
        this.tableName = tableName;
        this.colName = colName;
        this.nullRows = nullRows;
        this.dataPath = dataPath;
        this.tableSize = tableSize;
    }

    public long getNullRows() {
        return nullRows;
    }

    public void setNullRows(BigDecimal nullPercentage) {
        this.nullRows = nullRows;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String[] getStrs() {
        return strs;
    }

    public void setStrs(String[] strs) {
        this.strs = strs;
    }

    public long[] getParaRows() {
        return paraRows;
    }

    public void setParaRows(long[] paraRows) {
        this.paraRows = paraRows;
    }

    public List<long[]> getIsPresent() {
        return isPresent;
    }

    public void setIsPresent(List<long[]> isPresent) {
        this.isPresent = isPresent;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }
}
