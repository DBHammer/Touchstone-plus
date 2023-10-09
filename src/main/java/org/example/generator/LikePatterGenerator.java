package org.example.generator;

import org.example.solver.LikeType;
import org.example.solver.TopoGraph;

import java.util.*;

public class LikePatterGenerator {
    TopoGraph topoGraph;
    List<LikeType> LikeTyps;
    char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    char[] level = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public LikePatterGenerator(TopoGraph topoGraph, List<LikeType> likeTyps) {
        this.topoGraph = topoGraph;
        LikeTyps = likeTyps;
    }

    public String[] getLikeParas() {
        String[] likeParas = new String[topoGraph.getV()];
        HashSet<String> distinctLikeParasInBehindMatch = new HashSet<>();
        HashSet<String> distinctLikeParasInFrontMatch = new HashSet<>();
        Random random = new Random();

        //统计入度
        int[] inNum = getInNum();
        //得到每个节点的深度
        int[] allNodeDepth = getDepthOfAllNode();
        for (int i = 0; i < inNum.length; i++) {
            String likePara = String.valueOf(chars[random.nextInt(62)]);
            if (inNum[i] == 0) {
                if (!LikeTyps.get(i).isOnlyFrontMatch()) {
                    while (distinctLikeParasInBehindMatch.contains(likePara)) {
                        likePara = String.valueOf(chars[random.nextInt(62)]);
                    }
                    distinctLikeParasInBehindMatch.add(likePara);
                    likeParas[i] = level[allNodeDepth[i]] + likePara;
                    DFS(topoGraph, likeParas, i, chars, allNodeDepth);
                }
                if (LikeTyps.get(i).isOnlyFrontMatch()) {
                    while (distinctLikeParasInFrontMatch.contains(likePara)) {
                        likePara = String.valueOf(chars[random.nextInt(62)]);
                    }
                    distinctLikeParasInFrontMatch.add(likePara);
                    likeParas[i] = level[allNodeDepth[i]] + likePara;
                    DFS(topoGraph, likeParas, i, chars, allNodeDepth);
                }
            }
        }

        return likeParas;
    }

    public void DFS(TopoGraph topoGraph, String[] likeParas, int i, char[] chars, int[] allNodeDepth) {
        Queue<Integer> allNextV = topoGraph.adj(i);
        Random random = new Random();
        String head = likeParas[i];
        HashSet<String> distinctHeads = new HashSet<>();
        for (Integer integer : allNextV) {
            likeParas[integer] = "";
            char body = chars[random.nextInt(62)];
            String child = String.valueOf(body);
            while (distinctHeads.contains(child)) {
                child = String.valueOf(chars[random.nextInt(62)]);
            }
            distinctHeads.add(child);
            likeParas[integer] = head + level[allNodeDepth[integer]] + child;
            DFS(topoGraph, likeParas, integer, chars, allNodeDepth);
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

    public int[] getDepthOfAllNode() {
        int[] allNodeDepth = new int[topoGraph.getV()];
        Arrays.fill(allNodeDepth, -1);
        int[] inNum = getInNum();
        for (int i = 0; i < inNum.length; i++) {
            if (inNum[i] == 0) {
                allNodeDepth[i] = 0;
                findDepthInTree(i, allNodeDepth);
            }
        }
        return allNodeDepth;
    }

    public void findDepthInTree(int root, int[] allNodeDepth) {
        int curDepth = allNodeDepth[root];
        Queue<Integer> children = topoGraph.adj(root);
        for (Integer child : children) {
            allNodeDepth[child] = curDepth + 1;
            findDepthInTree(child, allNodeDepth);
        }
    }
}
