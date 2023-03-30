package org.example.generator;

import org.example.solver.InType;
import org.example.solver.LikeType;
import org.example.solver.TopoGraph;
import picocli.CommandLine;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "generate", description = "generate newsql and data",
        mixinStandardHelpOptions = true, usageHelpAutoWidth = true)
public class DataGenerator implements Callable<Integer> {

    private List<InType> InTypes;
    private List<LikeType> LikeTypes;
    private long[] paraRows;
    private List<long[]> paraPresent;
    @CommandLine.Option(names = {"-c", "--config_path"}, required = true, description = "the config path for generate")
    private String outputPath;
    @CommandLine.Option(names = {"-d", "--data_path"}, required = true, description = "the config path for solve equation")
    private String dataPath;
    private String colName;
    private String tableName;
    private long nullRows;
    private long tableSize;
    private TopoGraph topoGraph;

    public Integer call() throws IOException {
        init();
        //为n个参数的每一个生成一个随机的不重复的值
        int inSum = 0;
        for (InType inType : InTypes) {
            inSum += inType.getParaNum();
        }
        int likeSum = LikeTypes.size();
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(8, inSum);
        String[] inParas = randomStringGenerator.getRandomStringArray();
        LikePatterGenerator likePatterGenerator = new LikePatterGenerator(topoGraph);
        String[] likeParas = likePatterGenerator.getLikeParas();
        SqlWriter sqlWriter = new SqlWriter(inParas, likeParas, getParaNums(InTypes), paraPresent, tableName, colName);
        sqlWriter.writeNewSql();
        DataWriter dataWriter = new DataWriter(inParas, paraRows, paraPresent, tableName, colName, nullRows, dataPath, tableSize);
        dataWriter.generateData();
        return 0;
    }

    public long[] getParaNums(List<InType> inTypes) {
        long[] paraNums = new long[inTypes.size()];
        for (int i = 0; i < inTypes.size(); i++) {
            paraNums[i] = inTypes.get(i).getParaNum();
        }
        return paraNums;
    }

    public void init() throws IOException {
        List<String> allInfo = readOutPutFile();
        initInTypeAndLikeType(allInfo);
    }

    private List<String> readOutPutFile() throws IOException {
        List<String> output = new ArrayList<>();
        FileInputStream inputStream = new FileInputStream(outputPath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String str = null;
        while ((str = bufferedReader.readLine()) != null) {
            output.add(str);
        }
        inputStream.close();
        bufferedReader.close();
        return output;
    }

    private void initInTypeAndLikeType(List<String> allInfo) {
        List<long[]> allVector = new ArrayList<>();
        InTypes = new ArrayList<>();
        LikeTypes = new ArrayList<>();
        for (String eachLineInfo : allInfo) {
            String[] infos = eachLineInfo.split(" ");
            if (infos.length == 3 && infos[0].equals("intype:")) {
                InType intype = new InType(Integer.parseInt(infos[1]), infos[2]);
                InTypes.add(intype);
            } else if (infos.length == 3 && infos[0].equals("liketype:")) {
                boolean isFront = false,
                        isMiddle = false,
                        isBehind = false;
                if (infos[1].equals("front")) {
                    isFront = true;
                }
                if (infos[1].equals("middle")) {
                    isMiddle = true;
                }
                if (infos[1].equals("behind")) {
                    isBehind = true;
                }
                LikeType likeType = new LikeType(isFront, isMiddle, isBehind, infos[2]);
                LikeTypes.add(likeType);
            } else if (infos.length == 1) {
                tableName = infos[0].split("\\.")[0];
                colName = infos[0].split("\\.")[1];
            } else if (infos.length == 2 && infos[0].equals("nullRows:")) {
                nullRows = Integer.parseInt(infos[1]);
            } else if (infos.length == 2 && infos[0].equals("tableSize:")) {
                tableSize = Integer.parseInt(infos[1]);
            } else {
                long[] vector = new long[infos.length];
                for (int i = 0; i < infos.length; i++) {
                    vector[i] = Integer.parseInt(infos[i]);
                }
                allVector.add(vector);
            }
        }
        paraRows = allVector.get(0);
        paraPresent = allVector.subList(1, allVector.size());
        initTopograph(InTypes.size(), LikeTypes.size());
    }

    public boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isBigDecimal(String str) {
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        int i = (chars[0] == '-') ? 1 : 0;
        if (i == sz) return false;

        if (chars[i] == '.') return false;//除了负号，第一位不能为'小数点'

        boolean radixPoint = false;
        for (; i < sz; i++) {
            if (chars[i] == '.') {
                if (radixPoint) return false;
                radixPoint = true;
            } else if (!(chars[i] >= '0' && chars[i] <= '9')) {
                return false;
            }
        }
        return true;
    }

    public void initTopograph(int inTypeSize, int likeTypeSize) {
        //todo 考虑like有重复的情况
        topoGraph = new TopoGraph(likeTypeSize);
        for (int i = 1; i < likeTypeSize; i++) {
            long[] vecter1 = paraPresent.get(i + inTypeSize);
            for (int j = 0; j < i; j++) {
                long[] vecter2 = paraPresent.get(j + inTypeSize);
                if (isContainVecter(vecter1, vecter2)) {
                    topoGraph.addEdge(i, j);
                }
                if (isContainVecter(vecter2, vecter1)) {
                    topoGraph.addEdge(j, i);
                }
            }
        }
    }

    public boolean isContainVecter(long[] vecter1, long[] vecter2) {
        if (vecter1.length == vecter2.length) {
            long sum1 = 0;
            for (long l : vecter1) {
                sum1 += l;
            }
            long sum2 = 0;
            for (long j : vecter2) {
                sum2 += j;
            }
            long mul = 0;
            for (int i = 0; i < vecter1.length; i++) {
                mul += vecter1[i] * vecter2[i];
            }
            if (sum1 > sum2 && mul == sum2) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
