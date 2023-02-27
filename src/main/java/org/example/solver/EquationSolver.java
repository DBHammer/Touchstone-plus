package org.example.solver;

import org.chocosolver.solver.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import static org.chocosolver.util.tools.MathUtils.pow;


public class EquationSolver {
    private static final String inputPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\input";

    public static void main(String[] args) {
        List<String> eachLine = getEachLine(inputPath);
        List<InType> InTypes = new ArrayList<>();
        List<LikeType> LikeTypes = new ArrayList<>();
        getInTypeAndLikeType(InTypes, LikeTypes, eachLine);
        solve(InTypes, LikeTypes);
    }

    public static void solve(List<InType> InTypes, List<LikeType> LikeTypes) {
        if (InTypes.size() != 0 || LikeTypes.size() != 0) {
            //求方程个数
            int n = 0;
            if (InTypes.size() != 0) {
                for (InType currentInType : InTypes) {
                    n += currentInType.paraNum;
                }
            }
            if (LikeTypes.size() != 0) {
                //todo
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
                //n += getMax(typeNum);
            }
            System.out.println(n);

            //构造in类型的方程
            Model model = new Model();
            int len = getMaxPercentage(InTypes);
            List<IntVar[]> allEquation = new ArrayList<>();
            for (int i = 0; i < InTypes.size() + 1; i++) {
                int max;
                if (i == 0) {
                    max = pow(10, len);
                } else {
                    max = 1;
                }
                IntVar[] x = model.intVarArray("x", n, 0, max);
                allEquation.add(x);
            }
            String op = "="; // among ">=", ">", "<=", "<", "="
            List<IntVar[]> vectorMuls = new ArrayList<>();
            for (int i = 0; i < InTypes.size(); i++) {
                IntVar[] vectorMul = new IntVar[n];
                vectorMuls.add(vectorMul);
            }
            for (int i = 0; i < InTypes.size(); i++) {
                for (int j = 0; j < n; j++) {
                    vectorMuls.get(i)[j] = allEquation.get(0)[j].mul(allEquation.get(i + 1)[j]).intVar();
                }
            }
            model.sum(allEquation.get(0), "<=", pow(10, len)).post();
            for (int i = 0; i < InTypes.size(); i++) {
                String strPercentage = InTypes.get(i).getPercentage();
                int percentage = (int) (Double.parseDouble(strPercentage) * pow(10, len));
                model.sum(vectorMuls.get(i), "=", percentage).post();
                //System.out.println(inTypes.get(i).getParaNum());
                model.sum(allEquation.get(i + 1), "=", InTypes.get(i).getParaNum()).post();
            }
            Solver solver = model.getSolver();
            if (solver.solve()) {
                for (IntVar[] intVars : allEquation) {
                    System.out.println(Arrays.toString(intVars));
                }
            }
        }
    }

    public static int getMaxPercentage(List<InType> InTypes) {
        int len = 0;
        for (InType eachInType : InTypes) {
            int num = eachInType.getPercentage().length();
            if (num > len) {
                len = num;
            }
        }
        if (len <= 4) {
            return 2;
        } else {
            return len - 2;
        }
    }

    public static List<String> getEachLine(String intputPath) {
        File file = new File(intputPath);
        List<String> eachLine = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            eachLine = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eachLine;
    }

    public static void getInTypeAndLikeType(List<InType> InTypes, List<LikeType> LikeTypes, List<String> eachLine) {
        for (String line : eachLine) {
            String probability = getProbability(line);
            if (line.contains("in") || line.contains("IN")) {
                List<String> allParas = getAllParas(line);
                InType currentInType = new InType(allParas.size(), probability);
                InTypes.add(currentInType);
            } else if (line.contains("like") || line.contains("LIKE")) {
                boolean[] booleans = getMatchPosition(line);
                LikeType currentLikeType = new LikeType(booleans[0], booleans[1], booleans[2], probability);
                LikeTypes.add(currentLikeType);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static boolean[] getMatchPosition(String line) {
        boolean[] matchPosition = {false, false, false};
        int likePosition = line.indexOf("like");
        if (likePosition == -1) {
            likePosition = line.indexOf("LIKE");
        }
        int equalPosition = line.lastIndexOf("=");
        String likeMatch = line.substring(likePosition + 4, equalPosition).trim();
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

    public static List<String> getAllParas(String line) {
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
            allParas.add(s.trim());
        }
        return allParas;
    }

    public static String getProbability(String line) {
        int currentIndex = line.indexOf("=");
        int index = currentIndex;
        while (currentIndex != -1) {
            index = currentIndex;
            currentIndex = line.indexOf("=", currentIndex + 1);
        }
        return line.substring(index + 1).trim();
    }

    public static int getMax(int[] arr) {
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (max < arr[i]) {
                max = arr[i];
            }
        }
        return max;
    }
}
