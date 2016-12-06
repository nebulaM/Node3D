/* Copyright (c) 2016 nebulaM*/
package com.github.android.backend;
/**
 * Stores information on whether a node is used or not, and node coordinates
 */
public class Node {
    private int nodeID;
    private int xCord;
    private int yCord;
    private int zCord;

    //map to OpenGL's coordinate
    private float xCordOpenGL;
    private float yCordOpenGL;
    private float zCordOpenGL;
    //scale the maximum coordinate
    private final float scaleCordOpenGL=0.9f;

    private boolean playerIsHere=false;
    /**
     *
     * @param nodeID
     * @param mapSize
     *
     * mapSize=3 example:
     * Z=0                Z=1               Z=2
     * Z--------> X       Z--------->X      Z--------->X
     * | 0  1  2          | 9  10 11        | 18 19 20
     * | 3  4  5          | 12 13 14        | 21 22 23
     * | 6  7  8          | 15 16 17        | 24 25 26
     * Y                  Y                 Y
     */
    public Node(int nodeID,int mapSize, float xScale, float yScale, float zScale){
        if(mapSize<2) {
            throw new IllegalArgumentException("minimum mapSize is 2");
        }
        if(nodeID>=(mapSize*mapSize*mapSize)){
            throw new IllegalArgumentException("maximum nodeID is "+mapSize*mapSize*mapSize);
        }

        this.nodeID=nodeID;
        this.xCord = nodeID % mapSize;
        int temp=nodeID, sqMapSize=mapSize*mapSize;

        this.yCord = temp / mapSize;
        while (temp>=sqMapSize){
            this.yCord-=mapSize;
            temp-=sqMapSize;
        }
        this.zCord=nodeID /(sqMapSize);

        this.xCordOpenGL=(float)xCord*scaleCordOpenGL / (xScale);

        this.yCordOpenGL = (float) yCord * scaleCordOpenGL / (yScale);

        this.zCordOpenGL=(float)zCord*scaleCordOpenGL / (zScale);
    }


    public int getXCord(){
        return xCord;
    }

    public int getYCord(){
        return yCord;
    }

    public int getZCord(){
        return zCord;
    }

    public int getNodeID(){
        return nodeID;
    }

    public synchronized float[] getCordOpenGL(){
        float[] array={xCordOpenGL,yCordOpenGL,zCordOpenGL};
        return array;
    }

    public synchronized void setPlayerIsHere(boolean x){
        playerIsHere=x;
    }
    public synchronized boolean getPlayerIsHere(){
        return playerIsHere;
    }
}
