/* Copyright (c) 2016 nebulaM*/
package com.github.android.backend;

public class Player {
    private int currentPosition;
    private final int aimPosition;
    private int energy;

    public Player(int startPosition, int endPosition, int maxNode){
        if(startPosition>=0) {
            this.currentPosition = startPosition;
        }
        else
            throw new IllegalArgumentException("startPosition must >0");
        if(endPosition>startPosition) {
            this.aimPosition = endPosition;
        }
        else
            throw new IllegalArgumentException("endPosition must > startPosition");
        this.energy=maxNode*2;
    }

    public boolean costEnergy(int edgeCost){
        energy-=edgeCost;
        if(energy>=0)
            return true;
        else
            return false;
    }

    public synchronized int getEnergy(){
        return energy;
    }

    public synchronized void setCurrentPosition(int nodeIndex){
        if(nodeIndex>=0)
            currentPosition=nodeIndex;
        else
            throw new IllegalArgumentException("nodeIndex must >0");
    }
    public synchronized int getCurrentPosition(){
        return currentPosition;
    }

    public synchronized int getAimPosition(){
        return aimPosition;
    }

}
