package org.example.generator;

import org.example.solver.TopoGraph;
import org.example.utils.exception.MainException;

import java.util.*;

public class LikePatterGenerator {
    TopoGraph topoGraph;

    public LikePatterGenerator(TopoGraph topoGraph) {
        this.topoGraph = topoGraph;
    }

    public String[] getLikeParas() {
        String[] likeParas = new String[topoGraph.getV()];
        HashSet<String> distinctLikeParas = new HashSet<>();
        Random random = new Random();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        //统计入度
        int[] inNum = getInNum();
        for (int i = 0; i < inNum.length; i++) {
            if (inNum[i] == 0) {
                String likePara = String.valueOf(chars[random.nextInt(62)]);
                while(distinctLikeParas.contains(likePara)){
                    likePara = String.valueOf(chars[random.nextInt(62)]);
                }
                distinctLikeParas.add(likePara);
                likeParas[i] = likePara;
                DFS(topoGraph, likeParas, i, chars);
            }
        }
        return likeParas;
    }

    public void DFS(TopoGraph topoGraph, String[] likeParas, int i, char[] chars) {
        Queue<Integer> allNextV = topoGraph.adj(i);
        Random random = new Random();
        String head = likeParas[i];
        for (Integer integer : allNextV) {
            likeParas[integer] = "";
            likeParas[integer] += head;
            likeParas[integer] += String.valueOf(chars[random.nextInt(62)]);
            DFS(topoGraph, likeParas, integer, chars);
        }
    }

    public int[] getInNum() {
        int[] inNum = new int[topoGraph.getV()];
        for (int i = 0; i < topoGraph.getV(); i++) {
            Queue<Integer> allNextV = topoGraph.adj(i);
            for (Integer integer : allNextV) {
                inNum[integer]++;
            }
        }
        return inNum;
    }
}
