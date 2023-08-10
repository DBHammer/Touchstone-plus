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

        LikePatterGenerator likePatterGenerator = new LikePatterGenerator(topoGraph, LikeTypes);
        //todo 需要考虑in和like的参数有重复的情况
        String[] likeParas = likePatterGenerator.getLikeParas();

        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(8, inSum + LikeTypes.size(), LikeTypes);
        //todo 这里考虑随机生成的in类型参数不能与like冲突，但是之后应该考虑前向中向后向like的情况
        String[] inParas = randomStringGenerator.getRandomStringArray(likeParas);

        int[] entry = likePatterGenerator.getInNum();
        String[] allParaValue = giveValue2AllParas(inParas, likeParas, entry, InTypes.size());
        SqlWriter sqlWriter = new SqlWriter(allParaValue, likeParas, paraPresent, tableName, colName, InTypes.size(), LikeTypes);
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
                if (infos[1].contains("front")) {
                    isFront = true;
                }
                if (infos[1].contains("middle")) {
                    isMiddle = true;
                }
                if (infos[1].contains("behind")) {
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
        if (str == null || str.trim().isEmpty()) {
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
        //首先为所有的01向量进行排序，按照1的个数从大到小排序
        int[] sortedNode = new int[likeTypeSize];
        for (int i = 0; i < likeTypeSize; i++) {
            sortedNode[i] = i + inTypeSize;
        }
        sortNode(sortedNode);
//        for (int i = 0; i < sortedNode.length; i++) {
//            int num = sortedNode[i];
//            System.out.println(Arrays.stream(paraPresent.get(num)).sum());
//        }
        //为前缀匹配和后缀匹配分别简历拓扑图
        topoGraph = new TopoGraph(likeTypeSize);
        for (int i = 0; i < likeTypeSize; i++) {
            long[] vecter1 = paraPresent.get(sortedNode[i]);
            for (int j = i + 1; j < likeTypeSize; j++) {
                if (canBeTopo(sortedNode[i] - inTypeSize, sortedNode[j] - inTypeSize)) {
                    long[] vecter2 = paraPresent.get(sortedNode[j]);
                    if (isContainVecter(vecter1, vecter2)) {
                        topoGraph.addEdge(sortedNode[i], sortedNode[j]);
                        break;
                    }
                    if (isContainVecter(vecter2, vecter1)) {
                        topoGraph.addEdge(sortedNode[j], sortedNode[i]);
                        break;
                    }
                }
            }
        }
    }

    //对约束进行排序
    public void sortNode(int[] sortedNode) {
        int len = sortedNode.length;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j + 1 < len - i - 1; j++) {
                long sumj = Arrays.stream(paraPresent.get(sortedNode[j])).sum();
                long sumj1 = Arrays.stream(paraPresent.get(sortedNode[j + 1])).sum();
                if (sumj > sumj1) {
                    int tmp = sortedNode[j];
                    sortedNode[j] = sortedNode[j + 1];
                    sortedNode[j + 1] = tmp;
                }
            }
        }
    }

    /**
     * 判断是否能纳入拓扑图，目前只允许前缀式匹配和后缀式匹配，类似%a%暂时不纳入topo图
     */
    public boolean canBeTopo(int i, int j) {
        if (LikeTypes.get(i).isOnlyFrontMatch() && LikeTypes.get(j).isOnlyFrontMatch()) {
            return true;
        } else if (LikeTypes.get(i).isOnlyBehindMatch() && LikeTypes.get(j).isOnlyBehindMatch()) {
            return true;
        } else {
            return false;
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

    public String[] giveValue2AllParas(String[] inParas, String[] likeParas, int[] entry, int inSum) throws MainException {
        String[] allParaValues = new String[paraRows.length];
        String[] allFrontParaValues = new String[paraRows.length];
        String[] allBehindParaValues = new String[paraRows.length];
        for (int i = 0; i < entry.length; i++) {
            if (entry[i] == 0 && LikeTypes.get(i).isOnlyBehindMatch()) {
                giveValue2LikeParas(allFrontParaValues, likeParas, topoGraph, i, inSum);
            }
            if (entry[i] == 0 && LikeTypes.get(i).isOnlyFrontMatch()) {
                giveValue2LikeParas(allBehindParaValues, likeParas, topoGraph, i, inSum);
            }
        }
        combinateParaValues(likeParas, allParaValues, allFrontParaValues, allBehindParaValues, entry);
        for (int i = 0; i < allParaValues.length; i++) {
            if (allParaValues[i].isEmpty()) {
                allParaValues[i] = inParas[inSum++];
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

    public void combinateParaValues(String[] likeParas, String[] allParaValue, String[] allFrontParaValues, String[] allBehindParaValues, int[] entry) throws MainException {
        //记录所有需要被前缀和后缀LIKE包含到的位置
        boolean[] isLike = new boolean[allParaValue.length];
        for (int i = 0; i < allParaValue.length; i++) {
            if (allFrontParaValues[i] != null || allBehindParaValues[i] != null) {
                isLike[i] = true;
            } else {
                isLike[i] = false;
            }
        }
        //首先对前缀和后缀式子做一个补全,防止在做类似%a的插入的时候，影响到a%的概率。
        for (int i = 0; i < allFrontParaValues.length; i++) {
            if (allFrontParaValues[i] == null && isLike[i]) {
                //随机生成一个与之前前缀不同的
                String head = getDifferentString(likeParas, entry, 1);
                allFrontParaValues[i] = head;
            }
        }
        for (int i = 0; i < allBehindParaValues.length; i++) {
            if (allBehindParaValues[i] == null && isLike[i]) {
                //随机生成一个与之前前缀不同的
                String tail = getDifferentString(likeParas, entry, 0);
                allBehindParaValues[i] = tail;
            }
        }
        for (int i = 0; i < allParaValue.length; i++) {
            String str = "";
            String head = allFrontParaValues[i];
            String tail = allBehindParaValues[i];
            if (head != null) {
                for (int j = 0; j < head.length(); j++) {
                    str += head.charAt(j);
                }
            }
            if (tail != null) {
                for (int j = tail.length() - 1; j >= 0; j--) {
                    str += tail.charAt(j);
                }
            }
            allParaValue[i] = str;
        }
    }

    public String getDifferentString(String[] likeParas, int[] entry, int isFrontOrBehind) throws MainException {
        String result = "";
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random random = new Random();
        if (isFrontOrBehind == 1) {
            //为后缀式%a对应的前缀式补全与之前前缀式不同的前缀，防止冲突
            List<String> allHeadExist = new ArrayList<>();
            for (int i = 0; i < likeParas.length; i++) {
                if (LikeTypes.get(i).isOnlyBehindMatch() && entry[i] == 0) {
                    allHeadExist.add(likeParas[i]);
                }
            }
            return getString(result, chars, random, allHeadExist);
        } else if (isFrontOrBehind == 0) {
            List<String> allTailExist = new ArrayList<>();
            for (int i = 0; i < likeParas.length; i++) {
                if (LikeTypes.get(i).isOnlyFrontMatch() && entry[i] == 0) {
                    allTailExist.add(likeParas[i]);
                }
            }
            return getString(result, chars, random, allTailExist);
        } else {
            throw new MainException("补全的类型必须是前缀式或者后缀式");
        }
    }

    private String getString(String result, char[] chars, Random random, List<String> allStringExist) {
        for (int i = 0; i < allStringExist.get(0).length(); i++) {
            result += chars[random.nextInt(62)];
        }
        while (allStringExist.contains(result) && !result.isEmpty()) {
            result = "";
            for (int i = 0; i < allStringExist.get(0).length(); i++) {
                result += chars[random.nextInt(62)];
            }
        }
        return result;
    }

    public void giveValue2LikeParas(String[] allParaValues, String[] likeParas, TopoGraph topoGraph, int i, int inTypeSize) {
        Queue<Integer> allChild = topoGraph.adj(i);
        String currentStr = likeParas[i];
        long[] isPresent = paraPresent.get(i + inTypeSize);
        if (allChild.isEmpty()) {
            List<Integer> allPosition = new ArrayList<>();
            for (int j = 0; j < isPresent.length; j++) {
                if (isPresent[j] == 1) {
                    allPosition.add(j);
                }
            }
            List<Integer> allPositionCanBeFill = getAllPositionCanBeFill(allPosition, Long.parseLong(LikeTypes.get(i).getRows()), paraRows);
            List<String> strs = getDifferentStringFromChild(currentStr, null, allPositionCanBeFill.size(), likeParas);
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
                    for (long[] eachChildIsPresent : childIsPresent) {
                        if (eachChildIsPresent[k] == 1) {
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
            List<String> strs = getDifferentStringFromChild(currentStr, childStr, allPositionCanBeFill.size(), likeParas);
            int start = 0;
            for (Integer integer : allPositionCanBeFill) {
                allParaValues[integer] = strs.get(start++);
            }
            for (Integer child : allChild) {
                giveValue2LikeParas(allParaValues, likeParas, topoGraph, child, inTypeSize);
            }
        }
    }

    public List<String> getDifferentStringFromChild(String currentStr, List<String> childStr, int num, String[] likeParas) {
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
    }
}
