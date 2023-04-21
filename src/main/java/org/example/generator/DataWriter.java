package org.example.generator;

import org.example.solver.TopoGraph;
import org.example.utils.exception.MainException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class DataWriter {
    private String[] allParaValues;
    private long[] paraRows;
    private long nullRows;
    private String dataPath;
    private long tableSize;
    private String otherStr;

    public void generateData() throws IOException, MainException {
        if (paraRows.length != allParaValues.length) {
            throw new MainException("参数数量不一致");
        }
        FileWriter fw = new FileWriter(new File(dataPath));
        BufferedWriter bw = new BufferedWriter(fw);
        long currentRow = nullRows;
        for (long i = 0; i < nullRows; i++) {
            bw.write("\\N" + System.lineSeparator());
        }
        for (int i = 0; i < paraRows.length; i++) {
            String str = allParaValues[i];
            for (long j = 0; j < paraRows[i]; j++) {
                bw.write(str + System.lineSeparator());
            }
            currentRow += paraRows[i];
        }
        if (currentRow < tableSize) {
            for (long i = 0; i < tableSize - currentRow; i++) {
                bw.write(otherStr + System.lineSeparator());
            }
        }
        bw.close();
        fw.close();
    }

    public DataWriter(String[] allParaValues, long[] paraRows, long nullRows, String dataPath, long tableSize, String otherStr) {
        this.allParaValues = allParaValues;
        this.paraRows = paraRows;
        this.nullRows = nullRows;
        this.dataPath = dataPath;
        this.tableSize = tableSize;
        this.otherStr = otherStr;
    }

    public String[] getAllParaValues() {
        return allParaValues;
    }

    public void setAllParaValues(String[] allParaValues) {
        this.allParaValues = allParaValues;
    }

    public long[] getParaRows() {
        return paraRows;
    }

    public void setParaRows(long[] paraRows) {
        this.paraRows = paraRows;
    }

    public long getNullRows() {
        return nullRows;
    }

    public void setNullRows(long nullRows) {
        this.nullRows = nullRows;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public long getTableSize() {
        return tableSize;
    }

    public void setTableSize(long tableSize) {
        this.tableSize = tableSize;
    }

    public String getOtherStr() {
        return otherStr;
    }

    public void setOtherStr(String otherStr) {
        this.otherStr = otherStr;
    }
}
