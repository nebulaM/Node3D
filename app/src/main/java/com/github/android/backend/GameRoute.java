/*  
*  Generate a given number of nodes and random number of edges between nodes in a 3D coordinate
*  Take into account of existing edges so there will not be diagonal edges make a cross.
*  i.e., at most 1 diagonal edge exists in a square-shaped coordinate formulated by 4 nodes;
*  at most 1 diagonal edge exists in a cube-shaped coordinate formulated by 8 nodes.
* */
package com.github.android.backend;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Random;
/**
 * Create a new route
 *
 */
public class GameRoute {
    private final int routeSize;
    private final int nodeNum;
    private int nodeProbability;
    private int edgeProbability;
    private boolean[][] adjacentArray;
    private Map<boolean[][],Integer> edgeCost;
    private List<Node> nodeList;
    private List<Edge> edgeList;

    private final char edgeLevel;

    private static final int edgeCostMax=3;//max edgeCost is 3, we will add 1 to exclusive 0 and inclusive 3 in rand method

    private static final boolean debug=false;//this is better:"true".equals(System.getProperty("debug"));

    /**
     *
     * @param routeSize number of node in a single dimension
     *
     *                  min routeSize is 2, it looks like this:
     *                  N--------N
     *                 /|       /|
     *                / |      / |
     *               N--N-----N--N
     *               | /      | /
     *               |/       |/
     *               N--------N
     * @param edgeProbability
     *
     * @param edgeLevel options are S for Simple, M for Medium or H for Hard
     */

    public GameRoute(int routeSize, int nodeNum, int edgeProbability, char edgeLevel){
        if(routeSize>1) {
            this.routeSize = routeSize;
        }
        else
            throw new IllegalArgumentException("routeSize must greater than 1");
        this.nodeProbability=nodeProbability;
        this.edgeProbability=edgeProbability;
        this.edgeCost=Collections.synchronizedMap(new TreeMap());
        this.nodeList=Collections.synchronizedList(new ArrayList()) ;
        if(nodeNum<2 ) {
            this.nodeNum = 2;
        }
        else if( nodeNum>routeSize*routeSize*routeSize){
            this.nodeNum=routeSize*routeSize*routeSize;
        }
        else {
            this.nodeNum = nodeNum;
        }
        this.adjacentArray=new boolean[this.nodeNum][this.nodeNum];

        //scale for openGL coordinate, without this, nodes are too close to each other when total node number is small
        float xScale,yScale,zScale;
        if(this.nodeNum<this.routeSize){
            xScale=(float)(this.nodeNum);
        }
        else
            xScale=(float)(this.routeSize-1);

        if(this.nodeNum<this.routeSize*this.routeSize){
            if((this.nodeNum/this.routeSize)>1)
                yScale=(float)(this.nodeNum/this.routeSize);
            else
                yScale=1;
        }
        else
            yScale=(float)(this.routeSize-1);

        if(this.nodeNum<this.routeSize*this.routeSize*this.routeSize){
            if((this.nodeNum/(this.routeSize*this.routeSize))>1)
                zScale=(float)(this.nodeNum/(this.routeSize*this.routeSize));
            else
                zScale=1;
        }
        else
            zScale=(float)(this.routeSize-1);
        //put all nodes in the nodeL ist
        for (int i=0;i<this.nodeNum;++i){
            nodeList.add(new Node(i,this.routeSize,xScale,yScale,zScale));
        }

        nodeList.get(0).setPlayerIsHere(true);

        this.edgeList=new ArrayList<>();
        if(edgeLevel=='S' || edgeLevel=='M' || edgeLevel=='H') {
            this.edgeLevel = edgeLevel;
        }
        else
            throw new IllegalArgumentException("choose edgeLevel from one of the following letters: S, M, H");
    }

    /**
     * edge between node
     * in total there are 3*routeSize different small adjacent matrices
     * the small adjacent matrices in "createPath" have a size of m[routeSize][routeSize], they will be mapped to a this.adjacentAarry in the end
     */
    public synchronized void createPath(){

        randomConnectNodes(nodeList,edgeProbability, adjacentArray);
        if(debug)
            printAdjacentMatrix("randomConnectNodes");
       
    }
    
    
    /**
     *DO NOT USE
     * complex geometry in drawing when a node is not connected to any other node(s)
     */
    /*private void randomNodeUsage(){
        //initialize
        //usefulNode is required as a parameter when draw nodes in OpenGL
        //first 2 nodes always set to use so we have at least one edge
        //usefulNode=2;
        //nodeList.get(0).setIsUsed(true);
        //nodeList.get(1).setIsUsed(true);
        if(nodeProbability<0 || nodeProbability>100){
            throw new IllegalArgumentException("choose a probability from integer 0-100");
        }
        Random rand = new Random();
        int currentNum;
        for (int index=2;index<nodeNum;++index){
            currentNum=rand.nextInt(100);
            if(currentNum>nodeProbability) {
                nodeList.get(index).setIsUsed(false);
            }
            else {
                nodeList.get(index).setIsUsed(true);
                //usefulNode++;
            }
        }
    }*/

    /**
     * @param nodeList
     * @param probability
     * @param m
     */

    private synchronized void randomConnectNodes(List<Node> nodeList, int probability, boolean[][] m){
        int upperBound=nodeNum;
        int yPositionScale=routeSize;
        int zPositionScale=routeSize*routeSize;
        Random randEdge = new Random();
        Random randCost = new Random();
        Node startNode, endNode;
        int dx,dy,dz;

        //initialize
        edgeList.clear();
        for(int startIndex=0;startIndex<upperBound;++startIndex) {
            for(int endIndex=0;endIndex < upperBound; ++endIndex){
                m[startIndex][endIndex]=false;
            }
        }
        //start to put connection information in adjacentMatrix
        for(int startIndex=0;startIndex<upperBound;++startIndex) {
            startNode = nodeList.get(startIndex);
                for (int endIndex = 0; endIndex < upperBound; ++endIndex) {
                    // a node does not connect to itself
                    if (startIndex != endIndex) {
                        endNode = nodeList.get(endIndex);
                            dx = endNode.getXCord() - startNode.getXCord();
                            dy = endNode.getYCord() - startNode.getYCord();
                            dz = endNode.getZCord() - startNode.getZCord();
                            //only if the distance between two nodes <= 1 in all of x, y, z direction, will we consider connect the nodes.
                            if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && Math.abs(dz) <= 1) {
                                //Graph is not bi-direct, so m[i][j] = m[j][i]
                                if (!m[endIndex][startIndex]) {
                                    if((dx!=0 && dy==0 && dz==0) || (dx==0 && dy!=0 && dz==0) || (dx==0 && dy==0 && dz!=0)) {
                                        //Case 1: endNode and startNode are in a line that is parallel to one of the Cartesian axis, nothing special
                                        if(!nodeHasEdge(startIndex,m)){
                                            m[startIndex][endIndex]=true;
                                        }
                                        else
                                            m[startIndex][endIndex]=tryConnect(randEdge.nextInt(100), probability);
                                    }
                                    else if(edgeLevel!='S'){
                                        if(dx!=0 && dy!=0 && dz==0){//Case 2: endNode and startNode forms an diagonal of a square on the x-y plane
                                            //Only if the other diagonal of the square is not connected, will we try to connect THIS diagonal
                                            if(!m[startIndex+dx][startIndex+(dy*yPositionScale)]){
                                                m[startIndex][endIndex]=tryConnect(randEdge.nextInt(100), probability);
                                            }
                                        }
                                        else if(dy!=0 && dz!=0 && dx==0){
                                            //Only if the other diagonal of the square is not connected, will we try to connect THIS diagonal
                                            if(!m[startIndex+(dy*yPositionScale)][startIndex+(dz*zPositionScale)]){
                                                m[startIndex][endIndex]=tryConnect(randEdge.nextInt(100), probability);
                                            }
                                        }
                                        else if(dx!=0 && dz!=0 && dy==0) {
                                            //Only if the other diagonal of the square is not connected, will we try to connect THIS diagonal
                                            if (!m[startIndex + dx][startIndex + (dz * zPositionScale)]) {
                                                m[startIndex][endIndex] = tryConnect(randEdge.nextInt(100), probability);
                                            }
                                        }
                                    }
                                    else if(edgeLevel=='H'){
                                        //Case 3: endNode and startNode forms an diagonal of a cube in this coordinate
                                        //Only if the other three diagonals of the cube is not connected, will we try to connect THIS diagonal
                                        if(!m[startIndex+dx][endIndex-dx]){
                                            if(!m[startIndex+dy*yPositionScale][endIndex-dy*yPositionScale]){
                                                if(!m[startIndex+dz*zPositionScale][endIndex-dz*zPositionScale]){
                                                    m[startIndex][endIndex]=tryConnect(randEdge.nextInt(100), probability);
                                                }
                                            }
                                        }
                                    }
                                    m[endIndex][startIndex]=m[startIndex][endIndex];
                                }
                                else {//m[endIndex][startIndex]=true, so does m[startIndex][endIndex]
                                    m[startIndex][endIndex] = true;
                                }
                            }
                    }
                }
        }




        //Not bi-direction, so endIndex is always bigger than startIndex
        //startIndex always < endIndex
        for(int startIndex=0;startIndex<upperBound;++startIndex) {
            for (int endIndex=startIndex; endIndex<upperBound;++endIndex)
                if(m[startIndex][endIndex]){
                    edgeList.add( new Edge( startIndex, endIndex,(1+randCost.nextInt(edgeCostMax)) ) );
                }
        }


    }

    private boolean nodeHasEdge(int startIndex, boolean[][] m){
        for(int endIndex=0; endIndex<nodeNum; ++endIndex){
            if(m[startIndex][endIndex]){
                return true;
            }
        }
        return false;
    }
    /**
     * @param a
     * @param probability   of connecting two nodes
     */
    private boolean tryConnect(int a, int probability){
        if (a <= probability) {//m[startIndex][endIndex]=true, so does m[endIndex][startIndex]
            return true;
        }
        else
            return false;
    }

    /**
     * print out the usage and coordinate of node
     */
    public synchronized void printNodeList(String info){
        System.out.println("Tracing node list @"+info+":");
        System.out.println("Node ID\tx\ty\tz");
        for(int i=0;i<nodeNum;++i) {
            System.out.println(nodeList.get(i).getNodeID()+"\t\t"+nodeList.get(i).getXCord()+"\t"+nodeList.get(i).getYCord()+"\t"+nodeList.get(i).getZCord());
        }
    }
    /**
     * print out adjacent matrix for all nodes
     */
    public void printAdjacentMatrix(String info){
        System.out.println("Tracing AdjacentMatrix @"+info+":");
        System.out.println();
        System.out.print("\t");
        for(int i=0;i<nodeNum;++i){
            System.out.print((i+1)+"\t");
        }
        for(int i=0;i<nodeNum;++i){
            System.out.print("\n"+(i+1)+"\t");
            for(int j=0;j<nodeNum;++j){
                if(adjacentArray[i][j])
                    System.out.print("1\t");
                else
                    System.out.print("0\t");
            }
        }
    }



    public synchronized int getNodeNum(){
        return nodeNum;
    }

    public synchronized int getUsedEdge(){
        if(edgeList==null)
            throw new NullPointerException();
        else
            return edgeList.size();
    }


    public synchronized int getNodeIndex(int i){
        return nodeList.get(i).getNodeID();
    }

    public synchronized float[] getNodeCordOpenGL(int i){
        float[] array=nodeList.get(i).getCordOpenGL();
        return array;
    }

    public synchronized float[] getEdgeStartNodeCordOpenGL(int edgeIndex){
        float[] array=nodeList.get(edgeList.get(edgeIndex).getStartNodeIndex()).getCordOpenGL();
        return array;
    }
    public synchronized float[] getEdgeEndNodeCordOpenGL(int edgeIndex){
        float[] array=nodeList.get(edgeList.get(edgeIndex).getEndNodeIndex()).getCordOpenGL();
        return array;
    }

    public synchronized int getEdgeCost(int index){
        return edgeList.get(index).getEdgeCost();
    }

    public synchronized boolean getEdge(int startIndex, int endIndex){
        return adjacentArray[startIndex][endIndex];
    }

    public synchronized boolean getPlayerIsHere(int nodeIndex){
        return nodeList.get(nodeIndex).getPlayerIsHere();
    }

    public synchronized void setPlayIsHere(int nodeIndex){
        nodeList.get(nodeIndex).setPlayerIsHere(true);
    }
    public synchronized void setPlayIsNOTHere(int nodeIndex){
        nodeList.get(nodeIndex).setPlayerIsHere(false);
    }
}
