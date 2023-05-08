package org.example.generator;

import org.example.solver.InType;
import org.example.solver.LikeType;
import org.example.solver.TopoGraph;
import org.example.utils.exception.MainException;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "generate", description = "generate newsql and data", mixinStandardHelpOptions = true, usageHelpAutoWidth = true)
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
    private double denominator;

    public Integer call() throws IOException, MainException {
        init();
        changeParaNumAndRows();
        //为n个参数的每一个生成一个随机的不重复的值
        int inSum = 0;
        for (InType inType : InTypes) {
            inSum += inType.getParaNum();
        }

        LikePatterGenerator likePatterGenerator = new LikePatterGenerator(topoGraph);
        //todo 需要考虑in和like的参数有重复的情况
        String[] likeParas = likePatterGenerator.getLikeParas();

        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(8, inSum + 1);
        //todo 这里考虑随机生成的in类型参数不能与like冲突，但是之后应该考虑前向中向后向like的情况
        String[] inParas = randomStringGenerator.getRandomStringArray(likeParas);

        int[] entry = likePatterGenerator.getInNum();
        String[] allParaValue = giveValue2AllParas(inParas, likeParas, entry, InTypes.size());
        SqlWriter sqlWriter = new SqlWriter(allParaValue, likeParas, paraPresent, tableName, colName, InTypes.size(), LikeTypes.size());
        sqlWriter.writeNewSql();
        DataWriter dataWriter = new DataWriter(allParaValue, paraRows, nullRows, dataPath, tableSize, inParas[inParas.length - 1]);
        dataWriter.generateData();
        return 0;
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
                boolean isFront = false, isMiddle = false, isBehind = false;
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
            } else if (infos.length == 2 && infos[0].equals("denominator:")) {
                denominator = Double.parseDouble(infos[1]);
            } else {
                long[] vector = new long[infos.length];
                for (int i = 0; i < infos.length; i++) {
                    vector[i] = Integer.parseInt(infos[i]);
                }
                allVector.add(vector);
            }
        }
        long[] cutParaRows = allVector.get(0);
        for (int i = 0; i < cutParaRows.length; i++) {
            cutParaRows[i] = (long) (cutParaRows[i] * denominator);
        }
        paraRows = cutParaRows;
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

    public String[] giveValue2AllParas(String[] inParas, String[] likeParas, int[] entry, int inSum) {
        String[] allParaValues = new String[paraRows.length];
        for (int i = 0; i < entry.length; i++) {
            if (entry[i] == 0) {
                giveValue2LikeParas(allParaValues, likeParas, topoGraph, i, inSum);
            }
        }
        for (int i = 0; i < allParaValues.length; i++) {
            if (allParaValues[i] != null) {
                System.out.println(allParaValues[i] + " " + paraRows[i]);
            }
        }
        int start = 0;
        for (int i = 0; i < allParaValues.length; i++) {
            if (allParaValues[i] == null) {
                allParaValues[i] = inParas[start++];
            }
        }
        return allParaValues;
    }

    public void giveValue2LikeParas(String[] allParaValues, String[] likeParas, TopoGraph topoGraph, int i, int inTypeSize) {
        Queue<Integer> allChild = topoGraph.adj(i);
        String currentStr = likeParas[i];
        long[] isPresent = paraPresent.get(i + inTypeSize);
        if (allChild.size() == 0) {
            List<Integer> allPosition = new ArrayList<>();
            for (int j = 0; j < isPresent.length; j++) {
                if (isPresent[j] == 1) {
                    allPosition.add(j);
                }
            }
            List<Integer> allPositionCanBeFill = getAllPositionCanBeFill(allPosition, Long.parseLong(LikeTypes.get(i).getRows()), paraRows);
            List<String> strs = getDifferentStringFromChild(currentStr, null, allPositionCanBeFill.size());
            int start = 0;
            for (Integer integer : allPositionCanBeFill) {
                allParaValues[integer] = strs.get(start++);
            }
        } else {
            List<String> childStr = new ArrayList<>();
            List<long[]> childIsPresent = new ArrayList<>();
            for (Integer child : allChild) {
                childStr.add(likeParas[child]);
                childIsPresent.add(paraPresent.get(child + inTypeSize));
            }
            List<Integer> allPosition = new ArrayList<>();
            for (int k = 0; k < isPresent.length; k++) {
                if (isPresent[k] == 1) {
                    boolean canBeFill = true;
                    for (long[] eachChileIsPresent : childIsPresent) {
                        if (eachChileIsPresent[k] == 1) {
                            canBeFill = false;
                        }
                    }
                    if (canBeFill) {
                        //allParaValues[k] = str;
                        allPosition.add(k);
                    }
                }
            }
            long otherRows = Long.parseLong(LikeTypes.get(i).getRows());
            for (Integer integer : allChild) {
                otherRows = otherRows - Long.parseLong(LikeTypes.get(integer).getRows());
            }
            List<Integer> allPositionCanBeFill = getAllPositionCanBeFill(allPosition, otherRows, paraRows);
            List<String> strs = getDifferentStringFromChild(currentStr, childStr, allPositionCanBeFill.size());
            int start = 0;
            for (Integer integer : allPositionCanBeFill) {
                allParaValues[integer] = strs.get(start++);
            }
            for (Integer child : allChild) {
                giveValue2LikeParas(allParaValues, likeParas, topoGraph, child, inTypeSize);
            }
        }
    }

    public List<String> getDifferentStringFromChild(String currentStr, List<String> childStr, int num) {
        List<String> result = new ArrayList<>();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random random = new Random();
        HashSet<String> distinctStr;
        if (childStr != null) {
            distinctStr = new HashSet<>(childStr);
        } else {
            distinctStr = new HashSet<>();
        }
        for (int i = 0; i < num; i++) {
            String str = currentStr + chars[random.nextInt(62)];
            while (distinctStr.contains(str)) {
                str = currentStr + chars[random.nextInt(62)];
            }
            distinctStr.add(str);
            result.add(str);
        }
        return result;
    }

    public List<Integer> getAllPositionCanBeFill(List<Integer> allPostition, long otherRows, long[] paraRows) {
        List<Integer> allPositionCanBeFill = new ArrayList<>();
        long[] tmp = new long[allPostition.size()];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = paraRows[allPostition.get(i)];
        }
        List<long[]> allCom = new ArrayList<>();
        printCombinations(tmp, -1, otherRows, new long[]{}, allCom);
        List<Integer> hasFill = new ArrayList<>();
        for (int i = 0; i < allCom.get(0).length; i++) {
            for (Integer integer : allPostition) {
                if ((paraRows[integer] == allCom.get(0)[i]) && (!hasFill.contains(integer))) {
                    allPositionCanBeFill.add(integer);
                    hasFill.add(integer);
                    break;
                }
            }
        }
        return allPositionCanBeFill;
    }

    public void printCombinations(long[] array, int pos, long sum, long[] acc, List<long[]> allCom) {
        if (Arrays.stream(acc).sum() == sum) {
            allCom.add(acc);
        }
        for (int i = pos + 1; i < array.length; i++) {
            long[] newAcc = new long[acc.length + 1];
            System.arraycopy(acc, 0, newAcc, 0, acc.length);
            newAcc[acc.length] = array[i];
            printCombinations(array, i, sum, newAcc, allCom);
        }
    }

    public void changeParaNumAndRows() {
//        for (int i = 0; i < InTypes.size(); i++) {
//            System.out.println(InTypes.get(i).getRows());
//        }
//        for (int i = 0; i < LikeTypes.size(); i++) {
//            System.out.println(LikeTypes.get(i).getRows());
//        }
//        System.out.println("shnkshs");
        //
        for (int i = 0; i < InTypes.size(); i++) {
            long[] isPresent = paraPresent.get(i);
            long sum = Arrays.stream(isPresent).sum();
            InTypes.get(i).setParaNum((int) (sum));
        }
        for (int i = 0; i < LikeTypes.size(); i++) {
            long[] isPresent = paraPresent.get(i + InTypes.size());
            long sum = 0;
            for (int j = 0; j < isPresent.length; j++) {
                if (isPresent[j] == 1) {
                    sum += paraRows[j];
                }
            }
            LikeTypes.get(i).setRows(String.valueOf(sum));
        }
        //
//        for (int i = 0; i < InTypes.size() + LikeTypes.size(); i++) {
//            long[] isPresent = paraPresent.get(i);
//            long sum = 0;
//            for (int j = 0; j < isPresent.length; j++) {
//                if (isPresent[j] == 1) {
//                    sum += paraRows[j];
//                }
//            }
//            System.out.println(sum);
//        }
    }
}
