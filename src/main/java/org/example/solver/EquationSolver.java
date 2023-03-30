package org.example.solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.example.dbconnector.DbConnector;
import org.example.dbconnector.adapter.PgConnector;
import org.example.utils.CommonUtils;
import org.example.utils.TaskConfigurator;
import org.example.utils.exception.MainException;
import picocli.CommandLine;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.example.utils.CommonUtils.MAPPER;

@CommandLine.Command(name = "solve", description = "construct equation and solve it",
        mixinStandardHelpOptions = true, usageHelpAutoWidth = true)
public class EquationSolver implements Callable<Integer> {
    @CommandLine.Option(names = {"-c", "--config_path"}, required = true, description = "the config path for solve equation")
    private String configPath;
    private String dataPath;
    private static String colName;
    private static String tableName;

    public Integer call() throws IOException, MainException, SQLException {
        TaskConfigurator config;
        config = MAPPER.readValue(CommonUtils.readFile(configPath), TaskConfigurator.class);
        DbConnector dbConnector = new PgConnector(config.getDatabaseConnectorConfig());
        List<String> eachLine = getEachLine(config.getInputDirectory());
        colName = getColName(eachLine);
        tableName = getTableName(eachLine);
        List<InType> InTypes = new ArrayList<>();
        List<LikeType> LikeTypes = new ArrayList<>();
        getInTypeAndLikeType(InTypes, LikeTypes, eachLine);
        TopoGraph topoGraphs = getTopoGraphs(eachLine);
        solve(InTypes, LikeTypes, config.getOutputDirectory(), dbConnector, topoGraphs);
        return 0;
    }

    public void solve(List<InType> InTypes, List<LikeType> LikeTypes, String outputPath, DbConnector dbConnector, TopoGraph topoGraph) throws IOException, SQLException {
        if (InTypes.size() != 0 || LikeTypes.size() != 0) {
            //求方程个数
            int n = 0;
            if (InTypes.size() != 0) {
                for (InType currentInType : InTypes) {
                    n += currentInType.paraNum;
                }
            }
            if (LikeTypes.size() != 0) {
                int[] typeNum = {0, 0, 0};
                for (LikeType likeType : LikeTypes) {
                    if (likeType.isFrontMatch()) {
                        typeNum[0]++;
                    }
                    if (likeType.isMiddleMatch()) {
                        typeNum[1]++;
                    }
                    if (likeType.isBehindMatch()) {
                        typeNum[2]++;
                    }
                }
                n += getMax(typeNum);
            }
            //得到null的行数
            long nullRows = getNullRows(dbConnector);
            long tableSzie = getTableSize(dbConnector);
            ParallelPortfolio portfolio = new ParallelPortfolio();
            int nbModels = 1;
            for (int s = 0; s < nbModels; s++) {
                portfolio.addModel(makeModel(InTypes, LikeTypes, n, nullRows, tableSzie, topoGraph));
            }
            long a = System.currentTimeMillis();
            portfolio.solve();
            System.out.println("time:" + (System.currentTimeMillis() - a));
            if (portfolio.solve()) {
                Variable[] vars = portfolio.getBestModel().getVars();
                int start = 0;
                FileWriter fw = new FileWriter(new File(outputPath));
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(tableName + "." + colName + System.lineSeparator());
                bw.write("tableSize: " + dbConnector.getTableSize(tableName) + System.lineSeparator());
                bw.write("nullRows: " + nullRows + System.lineSeparator());
                //todo 对like的输出
                for (InType inType : InTypes) {
                    String eachInType = "intype: " + inType.getParaNum() + " " + inType.getRows() + System.lineSeparator();
                    bw.write(eachInType);
                }
                for (LikeType likeType : LikeTypes) {
                    String likePosition = "";
                    if (likeType.isFrontMatch()) {
                        likePosition += "front ";
                    }
                    if (likeType.isMiddleMatch()) {
                        likePosition += "middle ";
                    }
                    if (likeType.isBehindMatch()) {
                        likePosition = "behind ";
                    }
                    String eachLikeType = "liketype: " + likePosition + likeType.getRows() + System.lineSeparator();
                    bw.write(eachLikeType);
                }
                for (int i = 0; i < InTypes.size() + LikeTypes.size() + 1; i++) {
                    String eachline = "";
                    for (int j = 0; j < n; j++) {
                        String eachPara = vars[start++].toString().split("=")[1].trim();
                        eachline = eachline + eachPara + " ";
                    }
                    eachline += System.lineSeparator();
                    bw.write(eachline);
                }
                bw.close();
                fw.close();
            }
        }
    }

    public Model makeModel(List<InType> InTypes, List<LikeType> LikeTypes, int n, long nullRows, long tableSize, TopoGraph topoGraph) {
        //构造in类型和like类型的方程
        Model model = new Model();
        int inNum = 0;
        for (InType inType : InTypes) {
            inNum += inType.getParaNum();
        }
        int likeNum = n - inNum;
        IntVar[] inParaRowVector;
        IntVar[] likeParaRowVector;
        List<IntVar[]> isPercentVector = new ArrayList<>();
        inParaRowVector = model.intVarArray("in", inNum, 0, (int) tableSize);
        likeParaRowVector = model.intVarArray("like", likeNum, 0, (int) tableSize);
        for (int i = 0; i < InTypes.size() + LikeTypes.size(); i++) {
            IntVar[] x = model.intVarArray("x" + i, n, 0, 1);
            isPercentVector.add(x);
        }
        String op = "="; // among ">=", ">", "<=", "<", "="
        List<IntVar[]> vectorMuls = new ArrayList<>();
        for (int i = 0; i < InTypes.size() + LikeTypes.size(); i++) {
            IntVar[] vectorMul = new IntVar[n];
            vectorMuls.add(vectorMul);
        }
        for (int i = 0; i < InTypes.size() + LikeTypes.size(); i++) {
            for (int j = 0; j < inNum; j++) {
                vectorMuls.get(i)[j] = inParaRowVector[j].mul(isPercentVector.get(i)[j]).intVar();
            }
            for (int k = 0; k < likeNum; k++) {
                vectorMuls.get(i)[k + inNum] = likeParaRowVector[k].mul(isPercentVector.get(i)[k + inNum]).intVar();
            }
        }
        //对null值的处理
        long notNullRows = tableSize - nullRows;
        IntVar[] allParaRowVector = model.intVarArray("total", n, 0, (int) tableSize);
        for (int i = 0; i < n; i++) {
            if (i < inNum) {
                allParaRowVector[i] = inParaRowVector[i];
            } else {
                allParaRowVector[i] = likeParaRowVector[i - inNum];
            }
        }
        model.sum(allParaRowVector, "<=", (int) notNullRows).post();
        for (int i = 0; i < InTypes.size(); i++) {
            String strRows = InTypes.get(i).getRows();
            int intRows = Integer.parseInt(strRows);
            model.sum(vectorMuls.get(i), "=", intRows).post();
            model.sum(isPercentVector.get(i), "=", InTypes.get(i).getParaNum()).post();
        }
        for (int j = 0; j < LikeTypes.size(); j++) {
            String strRows = LikeTypes.get(j).getRows();
            int intRows = Integer.parseInt(strRows);
            model.sum(vectorMuls.get(j + inNum), "=", intRows).post();
        }
        //建模like中的包含和互斥关系
        for (int i = 1; i < LikeTypes.size(); i++) {
            for (int j = 0; j < i; j++) {
                Queue<Integer> edgI = topoGraph.adj(i);
                Queue<Integer> edgJ = topoGraph.adj(j);
                IntVar[] iMulJ = new IntVar[likeNum];
                for (int i1 = 0; i1 < iMulJ.length; i1++) {
                    iMulJ[i1] = isPercentVector.get(i + inNum)[i1].mul(isPercentVector.get(j + inNum)[i1]).intVar();
                }
                //IntVar sumI = 0; sumJ;
                if (edgI.contains(j)) {
                    for (int i2 = 0; i2 < iMulJ.length; i2++) {
                        model.arithm(iMulJ[i2], "=", isPercentVector.get(j + inNum)[i2]).post();
                    }
                } else if (edgJ.contains(i)) {
                    for (int i2 = 0; i2 < iMulJ.length; i2++) {
                        model.arithm(iMulJ[i2], "=", isPercentVector.get(i + inNum)[i2]).post();
                    }
                } else {
                    model.sum(iMulJ, "=", 0);
                }
            }
        }
        return model;
    }

    public long getNullRows(DbConnector dbConnector) throws SQLException {
        return dbConnector.getNullRow(tableName, colName);
    }

    public long getTableSize(DbConnector dbConnector) throws SQLException {
        return dbConnector.getTableSize(tableName);
    }

    public List<String> getEachLine(String intputPath) {
        File file = new File(intputPath);
        List<String> eachLine = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            eachLine = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eachLine;
    }

    public void getInTypeAndLikeType(List<InType> InTypes, List<LikeType> LikeTypes, List<String> eachLine) {
        for (String line : eachLine) {
            String outputRows = getOutput(line);
            if (line.contains("in") || line.contains("IN")) {
                List<String> allParas = getAllParas(line);
                InType currentInType = new InType(allParas.size(), outputRows);
                InTypes.add(currentInType);
            } else if (line.contains("like") || line.contains("LIKE")) {
                boolean[] booleans = getMatchPosition(line);
                LikeType currentLikeType = new LikeType(booleans[0], booleans[1], booleans[2], outputRows);
                LikeTypes.add(currentLikeType);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    public boolean[] getMatchPosition(String line) {
        boolean[] matchPosition = {false, false, false};
        int likePosition = line.indexOf("like");
        if (likePosition == -1) {
            likePosition = line.indexOf("LIKE");
        }
        int equalPosition = line.lastIndexOf("=");
        String likeMatch = line.substring(likePosition + 4, equalPosition).trim();
        //消除单引号
        likeMatch = likeMatch.substring(1, likeMatch.length() - 1);
        int currentPosition = likeMatch.indexOf('%');
        while (currentPosition != -1) {
            if (currentPosition == 0) {
                matchPosition[0] = true;
            } else if (currentPosition < likeMatch.length() - 1) {
                matchPosition[1] = true;
            } else if (currentPosition == likeMatch.length() - 1) {
                matchPosition[2] = true;
                break;
            } else {
                throw new UnsupportedOperationException();
            }
            currentPosition = likeMatch.indexOf("%", currentPosition + 1);
        }
        return matchPosition;
    }

    public List<String> getAllParas(String line) {
        int left = line.indexOf("(");
        int currentRight = line.indexOf(")");
        int right = currentRight;
        while (currentRight != -1) {
            right = currentRight;
            currentRight = line.indexOf(")", currentRight + 1);
        }
        if (right == -1 || left == -1) {
            throw new UnsupportedOperationException();
        }
        String[] allParaString = line.substring(left + 1, right).split(",");
        List<String> allParas = new ArrayList<>();
        for (String s : allParaString) {
            allParas.add(s.trim().substring(1, s.length() - 1));
        }
        return allParas;
    }

    public String getOutput(String line) {
        int currentIndex = line.indexOf("=");
        int index = currentIndex;
        while (currentIndex != -1) {
            index = currentIndex;
            currentIndex = line.indexOf("=", currentIndex + 1);
        }
        return line.substring(index + 1).trim();
    }

    public int getMax(int[] arr) {
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (max < arr[i]) {
                max = arr[i];
            }
        }
        return max;
    }

    public String getColName(List<String> eachLine) {
        String firstCol = eachLine.get(0);
        if (firstCol.contains("in") || firstCol.contains("IN")) {
            String[] strs = firstCol.split(" in ");
            if (strs.length == 0) {
                strs = firstCol.split(" IN ");
            }
            return strs[0].trim().split("\\.")[1];
        } else if (firstCol.contains("like") || firstCol.contains("LIKE")) {
            String[] strs = firstCol.split(" like ");
            if (strs.length == 0) {
                strs = firstCol.split(" LIKE ");
            }
            return strs[0].trim().split("\\.")[1];
        } else {
            return null;
        }
    }

    public String getTableName(List<String> eachLine) {
        String firstCol = eachLine.get(0);
        String[] strs = firstCol.split(" in ");
        if (strs.length == 0) {
            strs = firstCol.split(" IN ");
        }
        return strs[0].trim().split("\\.")[0];
    }

    public TopoGraph getTopoGraphs(List<String> eachline) {
        //todo like可能有重复
        List<String> likeStr = new ArrayList<>();
        for (String line : eachline) {
            if (line.contains("like") || line.contains("LIKE")) {
                String[] subStrs = line.split("like");
                if (subStrs.length == 1) {
                    subStrs = line.split("LIKE");
                }
                String str = subStrs[1].split("=")[0].trim();
                likeStr.add(str.substring(0, str.length() - 2));
            }
        }
        TopoGraph topoGraph = new TopoGraph(likeStr.size());
        for (int i = 1; i < likeStr.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (likeStr.get(i).contains(likeStr.get(j))) {
                    topoGraph.addEdge(j, i);
                }
                if (likeStr.get(j).contains(likeStr.get(i))) {
                    topoGraph.addEdge(i, j);
                }
            }
        }
        return topoGraph;
    }

}
