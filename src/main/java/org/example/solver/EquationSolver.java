package org.example.solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.Solver;
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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.example.utils.CommonUtils.MAPPER;

@CommandLine.Command(name = "solve", description = "construct equation and solve it",
        mixinStandardHelpOptions = true, usageHelpAutoWidth = true)
public class EquationSolver implements Callable<Integer> {
    @CommandLine.Option(names = {"-c", "--config_path"}, required = true, description = "the config path for solve equation")
    private String configPath;
    @CommandLine.Option(names = {"-t", "--thread_num"}, required = true, description = "the number of thread")
    private int nbModels;
    @CommandLine.Option(names = {"-g", "--group_size"}, description = "group size of the vector")
    int groupSize;
    @CommandLine.Option(names = {"-e", "--para_error"}, description = "error of cutting the domain")
    double error;
    private String dataPath;
    private static String colName;
    private static String tableName;
    private static final long MaxRow = 9999999;

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
        HashSet<String> allDistinctParas = getDistinctParas(eachLine);
        List<String> allParasValueUpperBound = getParaValueUpperBound(eachLine, InTypes, allDistinctParas, getTableSize(dbConnector));
        double denominator = cutDomainInOneError(allParasValueUpperBound, error);
        int allDistinctParaInCol = dbConnector.getAllDistinctString(colName, tableName).size();
        TopoGraph topoGraphs = getTopoGraphs(eachLine);
        solve(InTypes, LikeTypes, config.getOutputDirectory(), dbConnector, topoGraphs, allParasValueUpperBound, allDistinctParaInCol, denominator);
        return 0;
    }

    public void solve(List<InType> InTypes, List<LikeType> LikeTypes, String outputPath, DbConnector dbConnector,
                      TopoGraph topoGraph, List<String> allParasValueUpperBound, int allDistinctParaInCol, double denominator) throws IOException, SQLException, MainException {
        if (InTypes.size() != 0 || LikeTypes.size() != 0) {
            //求方程个数
            int n = allParasValueUpperBound.size();
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
            if (n > allDistinctParaInCol) {
                n = allDistinctParaInCol;
            }
            if (n != allParasValueUpperBound.size()) {
                throw new MainException("n和upperbound的数量不一致");
            }


            //通过groupsize削减x向量维度
            int originN = n;
            if (n % groupSize == 0) {
                n = n / groupSize;
            } else {
                n = n / groupSize + 1;
            }
            int left = 0;
            List<String> newAllParaValueUpperBound = new ArrayList<>();
            while (left + groupSize < originN) {
                long sum = 0;
                for (int i = left; i < left + groupSize; i++) {
                    sum += Long.parseLong(allParasValueUpperBound.get(i));
                }
                double avg = (double) (sum / groupSize);
                newAllParaValueUpperBound.add(String.valueOf((int) (avg + 1)));
                left = left + groupSize;
            }
            long sum = 0;
            for (int i = left; i < allParasValueUpperBound.size(); i++) {
                sum += Long.parseLong(allParasValueUpperBound.get(i));
            }
            int lastGroupSize = originN % groupSize;
            if (lastGroupSize == 0) {
                lastGroupSize = groupSize;
            }
            double avg = (double) (sum / lastGroupSize);
            newAllParaValueUpperBound.add(String.valueOf((int) (avg + 1)));
            allParasValueUpperBound.clear();
            allParasValueUpperBound.addAll(newAllParaValueUpperBound);


            //得到null的行数
            long nullRows = getNullRows(dbConnector);
            long tableSzie = getTableSize(dbConnector);
            ParallelPortfolio portfolio = new ParallelPortfolio();
            for (int s = 0; s < nbModels; s++) {
                portfolio.addModel(makeModel(InTypes, LikeTypes, n, originN, nullRows, tableSzie,
                        topoGraph, allParasValueUpperBound, denominator));
            }
            long a = System.currentTimeMillis();
            if (portfolio.solve()) {
                System.out.println("time:" + (System.currentTimeMillis() - a));
                Variable[] vars = portfolio.getBestModel().getVars();
                int start = 0;
                FileWriter fw = new FileWriter(new File(outputPath));
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(tableName + "." + colName + System.lineSeparator());
                bw.write("tableSize: " + dbConnector.getTableSize(tableName) + System.lineSeparator());
                bw.write("nullRows: " + nullRows + System.lineSeparator());
                bw.write("denominator: " + denominator + System.lineSeparator());
                bw.write("groupSize: " + groupSize + System.lineSeparator());
                bw.write("lastGroupSize: " + lastGroupSize + System.lineSeparator());
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
            } else {
                System.out.println("The solver has proved the problem has no solution");
            }
        }
    }

    public Model makeModel(List<InType> InTypes, List<LikeType> LikeTypes, int n, int originN,
                           long nullRows, long tableSize, TopoGraph topoGraph, List<String> allParasValueUpperBound,
                           double denominator) {
        //构造in类型和like类型的方程
        Model model = new Model();
        IntVar[] allParaRows = new IntVar[n];
        for (int i = 0; i < n; i++) {
            IntVar eachPara = model.intVar("para" + i, 0, Integer.parseInt(allParasValueUpperBound.get(i)));
            allParaRows[i] = eachPara;
        }
        List<IntVar[]> isPercentVector = new ArrayList<>();
        int lastGroupSize = originN % groupSize;
        if (lastGroupSize == 0) {
            lastGroupSize = groupSize;
        }
        for (int i = 0; i < InTypes.size() + LikeTypes.size(); i++) {
            //IntVar[] x = model.intVarArray("x" + i, n, 0, 1);
            IntVar[] x = new IntVar[n];
            for (int i1 = 0; i1 < n - 1; i1++) {
                IntVar each = model.intVar("x" + i + "_" + i1, 0, groupSize);
                x[i1] = each;
            }
            IntVar lastOne = model.intVar("x" + i + "_" + (n - 1), 0, lastGroupSize);
            x[n - 1] = lastOne;
            isPercentVector.add(x);
        }
        String op = "="; // among ">=", ">", "<=", "<", "="
        List<IntVar[]> vectorMuls = new ArrayList<>();
        for (int i = 0; i < InTypes.size() + LikeTypes.size(); i++) {
            IntVar[] vectorMul = new IntVar[n];
            vectorMuls.add(vectorMul);
        }
        for (int i = 0; i < InTypes.size() + LikeTypes.size(); i++) {
            for (int j = 0; j < n; j++) {
                vectorMuls.get(i)[j] = allParaRows[j].mul(isPercentVector.get(i)[j]).intVar();
            }
        }
        //对null值的处理
        long notNullRows = tableSize - nullRows;
        int[] groupRows = new int[n];
        for (int i = 0; i < n - 1; i++) {
            groupRows[i] = groupSize;
        }
        groupRows[n - 1] = lastGroupSize;
        model.scalar(allParaRows, groupRows, "<=", (int) notNullRows).post();
        //model.sum(allParaRows, "<=", (int) notNullRows).post();
        for (int i = 0; i < InTypes.size(); i++) {
            int paraNum = InTypes.get(i).getParaNum();
            //model.sum(isPercentVector.get(i), "=", paraNum).post();
            int up, down;
            if (paraNum == 1) {
                up = 3;
                down = 1;
            } else {
                up = paraNum + 1;
                down = paraNum - 1;
            }
            model.sum(isPercentVector.get(i), "<=", up).post();
            model.sum(isPercentVector.get(i), ">=", down).post();
        }
        for (int i = 0; i < InTypes.size() + LikeTypes.size(); i++) {
            String strRows = InTypes.get(i).getRows();
            int intRows = Integer.parseInt(strRows);
            intRows = (int) (intRows / denominator);
            int up = (int) (1.06 * intRows);
            int down = (int) (0.94 * intRows);
            model.sum(vectorMuls.get(i), "<=", (int) (Math.round(1.04 * intRows))).post();
            model.sum(vectorMuls.get(i), ">=", (int) (Math.round(0.96 * intRows))).post();
        }
        //建模like中的包含和互斥关系
        for (int i = 1; i < LikeTypes.size(); i++) {
            for (int j = 0; j < i; j++) {
                Queue<Integer> edgI = topoGraph.adj(i);
                Queue<Integer> edgJ = topoGraph.adj(j);
                IntVar[] iMulJ = new IntVar[n];
                for (int i1 = 0; i1 < iMulJ.length; i1++) {
                    iMulJ[i1] = isPercentVector.get(i + InTypes.size())[i1].mul(isPercentVector.get(j + InTypes.size())[i1]).intVar();
                }
                if (edgI.contains(j)) {
                    for (int i2 = 0; i2 < iMulJ.length; i2++) {
                        model.arithm(iMulJ[i2], "=", isPercentVector.get(j + InTypes.size())[i2]).post();
                    }
                } else if (edgJ.contains(i)) {
                    for (int i2 = 0; i2 < iMulJ.length; i2++) {
                        model.arithm(iMulJ[i2], "=", isPercentVector.get(i + InTypes.size())[i2]).post();
                    }
                } else {
                    model.sum(iMulJ, "=", 0).post();
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
            if (line.contains(" in ") || line.contains(" IN ")) {
                List<String> allParas = getAllParas(line);
                InType currentInType = new InType(allParas.size(), outputRows);
                InTypes.add(currentInType);
            } else if (line.contains(" like ") || line.contains(" LIKE ")) {
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
            allParas.add(s.trim().substring(1, s.trim().length() - 1));
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

    public List<String> getParaValueUpperBound(List<String> eachLine, List<InType> inTypes, HashSet<String> allDistinctParas, long tableSize) {
        List<String> paraValueUpperBound = new ArrayList<>();
        List<List<String>> allParas = new ArrayList<>();
        for (String s : eachLine) {
            List<String> paras = getAllParas(s);
            allParas.add(paras);
        }
        HashMap<String, Long> distinctPara2UpperBound = new HashMap<>();
        for (String allDistinctPara : allDistinctParas) {
            distinctPara2UpperBound.put(allDistinctPara, tableSize);
        }
        for (int i = 0; i < inTypes.size(); i++) {
            long rows = Long.parseLong(inTypes.get(i).getRows());
            List<String> eachLineParas = allParas.get(i);
            for (String eachLinePara : eachLineParas) {
                long originRows = distinctPara2UpperBound.get(eachLinePara);
                if (rows < originRows) {
                    distinctPara2UpperBound.put(eachLinePara, rows);
                }
            }
        }
        List<Long> distinctPara2UpperBoundValues = new ArrayList<>(distinctPara2UpperBound.values().stream().toList());
        Collections.sort(distinctPara2UpperBoundValues);
        for (Long value : distinctPara2UpperBoundValues) {
            paraValueUpperBound.add(value.toString());
        }
        return paraValueUpperBound;
    }

    public HashSet<String> getDistinctParas(List<String> eachLine) {
        HashSet<String> alldistinct = new HashSet<>();
        for (int i = 0; i < eachLine.size(); i++) {
            List<String> allParas = getAllParas(eachLine.get(i));
            alldistinct.addAll(allParas);
        }
        return alldistinct;
    }

    public double cutDomainInOneError(List<String> allParasValueUpperBound, double error) {
        long min = MaxRow;
        if (error != 0) {
            for (String s : allParasValueUpperBound) {
                long currentRow = Long.parseLong(s);
                if (currentRow < min) {
                    min = currentRow;
                }
            }
            double denominator = min * error;
            List<String> newParaBound = new ArrayList<>();
            for (int i = 0; i < allParasValueUpperBound.size(); i++) {
                long currentRow = Long.parseLong(allParasValueUpperBound.get(i));
                long newRow = (long) (currentRow / denominator);
                newParaBound.add(String.valueOf(newRow));
            }
            allParasValueUpperBound.clear();
            allParasValueUpperBound.addAll(newParaBound);
            return denominator;
        } else {
            return 1.0;
        }
    }


}
