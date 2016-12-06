package com.github.android.backend;

public class Edge {
    private final int startNodeIndex;
    private final int endNodeIndex;
    private final int edgeCost;

    public Edge(int startNodeIndex, int endNodeIndex, int edgeCost){
        this.startNodeIndex=startNodeIndex;
        this.endNodeIndex=endNodeIndex;
        this.edgeCost=edgeCost;

    }

    public synchronized int getStartNodeIndex(){
        return startNodeIndex;
    }
    public synchronized int getEndNodeIndex(){
        return endNodeIndex;
    }
    public synchronized int getEdgeCost(){
        return edgeCost;
    }
}
