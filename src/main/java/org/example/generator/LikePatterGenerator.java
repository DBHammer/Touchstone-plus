package org.example.generator;

import org.example.solver.LikeType;
import org.example.solver.TopoGraph;

import java.util.*;

public class LikePatterGenerator {
    TopoGraph topoGraph;
    List<LikeType> LikeTyps;

    public LikePatterGenerator(TopoGraph topoGraph, List<LikeType> likeTyps) {
        this.topoGraph = topoGraph;
        LikeTyps = likeTyps;
    }

    public String[] getLikeParas() {
        String[] likeParas = new String[topoGraph.getV()];
        HashSet<String> distinctLikeParasInBehindMatch = new HashSet<>();
        HashSet<String> distinctLikeParasInFrontMatch = new HashSet<>();
        Random random = new Random();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        //统计入度
        int[] inNum = getInNum();
        for (int i = 0; i < inNum.length; i++) {
            String likePara = String.valueOf(chars[random.nextInt(62)]);
            if (inNum[i] == 0) {
                if (LikeTyps.get(i).isOnlyBehindMatch()) {
                    while (distinctLikeParasInBehindMatch.contains(likePara)) {
                        likePara = String.valueOf(chars[random.nextInt(62)]);
                    }
                    distinctLikeParasInBehindMatch.add(likePara);
                    likeParas[i] = likePara;
                    DFS(topoGraph, likeParas, i, chars);
                }
                if (LikeTyps.get(i).isOnlyFrontMatch()) {
                    while (distinctLikeParasInFrontMatch.contains(likePara)) {
                        likePara = String.valueOf(chars[random.nextInt(62)]);
                    }
                    distinctLikeParasInFrontMatch.add(likePara);
                    likeParas[i] = likePara;
                    DFS(topoGraph, likeParas, i, chars);
                }
            }
        }

        return likeParas;
    }

    public void DFS(TopoGraph topoGraph, String[] likeParas, int i, char[] chars) {
        Queue<Integer> allNextV = topoGraph.adj(i);
        Random random = new Random();
        String head = likeParas[i];
        HashSet<String> distinctHeads = new HashSet<>();
        for (Integer integer : allNextV) {
            likeParas[integer] = "";
            String child = head + chars[random.nextInt(62)];
            while (distinctHeads.contains(child)) {
                child = head + chars[random.nextInt(62)];
            }
            distinctHeads.add(child);
            likeParas[integer] = child;
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
