package org.example.solver;

import java.util.*;

public class TopoGraph {

    //顶点数目
    private final int V;
    //边的数目
    private int E;
    //邻接表
    private Queue<Integer>[] adj;

    public TopoGraph(int V) {
        //初始化顶点数量
        this.V = V;
        //初始化边的数量
        this.E = 0;
        //初始化邻接表
        this.adj = new Queue[V];
        for (int i = 0; i < adj.length; i++) {
            adj[i] = new ArrayDeque<>();
        }
    }

    //获取顶点数目
    public int getV() {
        return V;
    }

    //获取边的数目
    public int getE() {
        return E;
    }

    //向有向图中添加一条边 v->w
    public void addEdge(int v, int w) {
        //只需要让顶点w出现在顶点v的邻接表中，因为边是有方向的，最终，顶点v的邻接表中存储的相邻顶点的含义是：  v->其他顶点
        if (!hasPath(v, w)) {
            adj[v].add(w);
            E++;
        }
    }

    public void removeEdge(int v, int w) {
        //只需要让顶点w出现在顶点v的邻接表中，因为边是有方向的，最终，顶点v的邻接表中存储的相邻顶点的含义是：  v->其他顶点
        adj[v].remove(w);
        E--;
    }

    //获取由v指出的边所连接的所有顶点
    public Queue<Integer> adj(int v) {
        return adj[v];
    }

    //该图的反向图
    private TopoGraph reverse() {
        //创建有向图对象
        TopoGraph r = new TopoGraph(V);

        for (int v = 0; v < V; v++) {
            //获取由该顶点v指出的所有边
            for (Integer w : adj[v]) {//原图中表示的是由顶点v->w的边
                r.addEdge(w, v);//w->v

            }

        }
        return r;
    }

    //统计入度为0的点
    public int[] getInZero() {
        int[] inNum = new int[V];
        for (int i = 0; i < V; i++) {
            Queue<Integer> allAdj = adj(i);
            for (Integer integer : allAdj) {
                inNum[integer]++;
            }
        }
        List<Integer> zeroInNum = new ArrayList<>();
        for (int i = 0; i < inNum.length; i++) {
            if (inNum[i] == 0) {
                zeroInNum.add(i);
            }
        }
        return zeroInNum.stream().mapToInt(t -> t).toArray();
    }

    //判断两点是否已经有路径
    public boolean hasPath(int i, int j) {
        Queue<Integer> children = adj(i);
        if (children.contains(j)) {
            return true;
        } else {
            boolean has = false;
            for (Integer child : children) {
                if (hasPath(child, j)) {
                    has = true;
                    break;
                }
            }
            return has;
        }
    }
}
