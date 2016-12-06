/*
*  
* Modified from Android's Tutorial on drawing a 2D square using OpenGl:
* https://developer.android.com/training/graphics/opengl/draw.html
* License: Apache 2.0
* http://www.apache.org/licenses/LICENSE-2.0
* Batch drawing many cuboids with different color, xyz parameter and color information are inputs to the constructor of this class.
* Only draw parallel edges for now.
* */

package com.github.android.GameDrawing;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import android.opengl.GLES20;

//TODO:Better load buffer method?(buffer.put not efficient)
public class EdgeToDraw {
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 ,//top
                                        4, 6, 5, 4, 7, 6 ,//bottom
                                        8, 9, 10, 8, 10,11,//right
                                        12, 14, 13, 12, 15, 14//left
    }; // order to draw vertices for one path

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[];

    private final String TAG = "EdgeToDraw";

    private final float[] YellowGreen={154f/255f, 205f/255f, 50f/255f, 0.0f};
    private final float[] Khaki={240f/255f, 230f/255f, 140f/255f, 0.0f };
    private final float[] LightSalmon={1.0f, 160f/255f, 122f/255f,0.0f };

    private final float edgeLengthFactor=2.0f;
    private final float edgeWidthFactor=6.0f;
    private final float edgeHeightFactor=16.0f;

    private final float xEdgeWidth;
    private final float yEdgeHeight;
    private final float zEdgeLength;

    private final float yEdgeLength;
    private final float zEdgeHeight;

    private final float xEdgeLength;
    private final float zEdgeWidth;

    private int cost=0;
    private final int totalEdge;

    public EdgeToDraw( int totalEdge, int whichEdgeCost,float nodeXLength, float nodeYLength, float nodeZLength) {
        this.totalEdge=totalEdge;
        for(int i=0; i<this.totalEdge; ++i) {
            if (OpenGLES20Activity.mGame.getEdgeCost(i) == whichEdgeCost)
                cost++;
        }
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
               cost*48 * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                cost*drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();

        xEdgeWidth=nodeXLength/edgeWidthFactor;
        yEdgeHeight=nodeYLength/edgeHeightFactor;
        zEdgeLength=nodeZLength/edgeLengthFactor;

        yEdgeLength=nodeYLength/edgeLengthFactor;
        zEdgeHeight=nodeZLength/edgeHeightFactor;

        xEdgeLength=nodeXLength/edgeLengthFactor;
        zEdgeWidth=nodeZLength/edgeWidthFactor;
        int offset=0;
        for(int i=0; i<this.totalEdge; ++i){
            int edgeCost=OpenGLES20Activity.mGame.getEdgeCost(i);
            if(edgeCost==whichEdgeCost){
                float[] edgeStartNodeCord=OpenGLES20Activity.mGame.getEdgeStartNodeCordOpenGL(i);
                float[] edgeEndNodeCord=OpenGLES20Activity.mGame.getEdgeEndNodeCordOpenGL(i);
                //no difference in "x cord and y cord" of "start and end" nodes
                    vertexBuffer.put(setCord(edgeStartNodeCord, edgeEndNodeCord));
                    drawListBuffer.put(setDrawList(drawOrder,offset));
                    offset++;
                }
            }
       if(whichEdgeCost==1){
           color=YellowGreen;
       }
        else if(whichEdgeCost==2){
           color=Khaki;
       }
        else if(whichEdgeCost==3){
           color=LightSalmon;
       }
        else{
           color=new float[] {0f,0f,0f,0f};
       }
        vertexBuffer.position(0);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        if(cost>0) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing
         GLES20.glUniform4fv(mColorHandle, 1, color, 0);
         // Draw the square
         GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length * cost, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     * define
     * axis
     * ^
     * | topLeft--------topRight
     * |    |               |
     * |    |               |
     * | btmLeft--------btmRight
     * -------------------------> axis
     */
    private float[] setCord(float[] startNodeCord, float[] endNodeCord){
        float[] rectangleCoords;
        //startNode's Index always < endNode's Index

        if(startNodeCord[0]==endNodeCord[0] &&startNodeCord[1]==endNodeCord[1]) {

            rectangleCoords = new float[]{

                    //Top
                    // _______________________X
                    // | btmLeft-------topLeft
                    // | |              |
                    // | |              |
                    // |btmRight-------topRight
                    // Z
                    startNodeCord[0] + xEdgeWidth, startNodeCord[1] + yEdgeHeight, startNodeCord[2] + zEdgeLength,   // top left
                    startNodeCord[0] - xEdgeWidth, startNodeCord[1] + yEdgeHeight, startNodeCord[2] + zEdgeLength,   //bottom left
                    endNodeCord[0] - xEdgeWidth, endNodeCord[1] + yEdgeHeight, endNodeCord[2] - zEdgeLength,     //bottom right
                    endNodeCord[0] + xEdgeWidth, endNodeCord[1] + yEdgeHeight, endNodeCord[2] - zEdgeLength,   //top right
                    //Bottom
                    startNodeCord[0] + xEdgeWidth, startNodeCord[1] - yEdgeHeight, startNodeCord[2] + zEdgeLength,   // top left
                    startNodeCord[0] - xEdgeWidth, startNodeCord[1] - yEdgeHeight, startNodeCord[2] + zEdgeLength,   //bottom left
                    endNodeCord[0] - xEdgeWidth, endNodeCord[1] - yEdgeHeight, endNodeCord[2] - zEdgeLength,     //bottom right
                    endNodeCord[0] + xEdgeWidth, endNodeCord[1] - yEdgeHeight, endNodeCord[2] - zEdgeLength,   //top right
                    //Right
                    endNodeCord[0] + xEdgeWidth, endNodeCord[1] - yEdgeHeight, endNodeCord[2] - zEdgeLength,   // top left
                    startNodeCord[0] + xEdgeWidth, startNodeCord[1] - yEdgeHeight, startNodeCord[2] + zEdgeLength,   //bottom left
                    startNodeCord[0] + xEdgeWidth, startNodeCord[1] + yEdgeHeight, startNodeCord[2] + zEdgeLength,     //bottom right
                    endNodeCord[0] + xEdgeWidth, endNodeCord[1] + yEdgeHeight, endNodeCord[2] - zEdgeLength,   //top right
                    //Left
                    endNodeCord[0] - xEdgeWidth, endNodeCord[1] - yEdgeHeight, endNodeCord[2] - zEdgeLength,   // top left
                    startNodeCord[0] - xEdgeWidth, startNodeCord[1] - yEdgeHeight, startNodeCord[2] + zEdgeLength,   //bottom left
                    startNodeCord[0] - xEdgeWidth, startNodeCord[1] + yEdgeHeight, startNodeCord[2] + zEdgeLength,     //bottom right
                    endNodeCord[0] - xEdgeWidth, endNodeCord[1] + yEdgeHeight, endNodeCord[2] - zEdgeLength   //top right

            };
        }
        else if(startNodeCord[0]==endNodeCord[0] &&startNodeCord[2]==endNodeCord[2]){
            rectangleCoords = new float[]{
                    //Top
                    endNodeCord[0] - xEdgeWidth, endNodeCord[1] - yEdgeLength, endNodeCord[2] + zEdgeHeight,   // top left
                    startNodeCord[0] - xEdgeWidth, startNodeCord[1] + yEdgeLength, startNodeCord[2] + zEdgeHeight,   //bottom left
                    startNodeCord[0] + xEdgeWidth, startNodeCord[1] + yEdgeLength, startNodeCord[2] + zEdgeHeight,     //bottom right
                    endNodeCord[0] + xEdgeWidth, endNodeCord[1] - yEdgeLength, endNodeCord[2] + zEdgeHeight,   //top right
                    //Bottom
                    endNodeCord[0] - xEdgeWidth, endNodeCord[1] - yEdgeLength, endNodeCord[2] - zEdgeHeight,   // top left
                    startNodeCord[0] - xEdgeWidth, startNodeCord[1] + yEdgeLength, startNodeCord[2] - zEdgeHeight,   //bottom left
                    startNodeCord[0] + xEdgeWidth, startNodeCord[1] + yEdgeLength, startNodeCord[2] - zEdgeHeight,     //bottom right
                    endNodeCord[0] + xEdgeWidth, endNodeCord[1] - yEdgeLength, endNodeCord[2] - zEdgeHeight,   //top right
                    //Right
                    startNodeCord[0] + xEdgeWidth, startNodeCord[1] + yEdgeLength, startNodeCord[2] + zEdgeHeight,   // top left
                    startNodeCord[0] + xEdgeWidth, startNodeCord[1] + yEdgeLength, startNodeCord[2] - zEdgeHeight,   //bottom left
                    endNodeCord[0] + xEdgeWidth, endNodeCord[1] - yEdgeLength, endNodeCord[2] - zEdgeHeight,     //bottom right
                    endNodeCord[0] + xEdgeWidth, endNodeCord[1] - yEdgeLength, endNodeCord[2] + zEdgeHeight,   //top right
                    //Left
                    startNodeCord[0] - xEdgeWidth, startNodeCord[1] + yEdgeLength, startNodeCord[2] + zEdgeHeight,   // top left
                    startNodeCord[0] - xEdgeWidth, startNodeCord[1] + yEdgeLength, startNodeCord[2] - zEdgeHeight,   //bottom left
                    endNodeCord[0] - xEdgeWidth, endNodeCord[1] - yEdgeLength, endNodeCord[2] - zEdgeHeight,     //bottom right
                    endNodeCord[0] - xEdgeWidth, endNodeCord[1] - yEdgeLength, endNodeCord[2] + zEdgeHeight,   //top right


            };
        }

        else if(startNodeCord[1]==endNodeCord[1] &&startNodeCord[2]==endNodeCord[2]){
            rectangleCoords = new float[]{
                    //Top
                    endNodeCord[0] - xEdgeLength, endNodeCord[1] + yEdgeHeight, endNodeCord[2] - zEdgeWidth,   // top left
                    startNodeCord[0] + xEdgeLength, startNodeCord[1] + yEdgeHeight, startNodeCord[2] - zEdgeWidth,   //bottom left
                    startNodeCord[0] + xEdgeLength, startNodeCord[1] + yEdgeHeight, startNodeCord[2] + zEdgeWidth,     //bottom right
                    endNodeCord[0] - xEdgeLength, endNodeCord[1] + yEdgeHeight, endNodeCord[2] + zEdgeWidth,   //top right
                    //Bottom
                    endNodeCord[0] - xEdgeLength, endNodeCord[1] - yEdgeHeight, endNodeCord[2] - zEdgeWidth,   // top left
                    startNodeCord[0] + xEdgeLength, startNodeCord[1] - yEdgeHeight, startNodeCord[2] - zEdgeWidth,   //bottom left
                    startNodeCord[0] + xEdgeLength, startNodeCord[1] - yEdgeHeight, startNodeCord[2] + zEdgeWidth,     //bottom right
                    endNodeCord[0] - xEdgeLength, endNodeCord[1] - yEdgeHeight, endNodeCord[2] + zEdgeWidth,   //top right
                    //Right
                    startNodeCord[0] + xEdgeLength, startNodeCord[1] + yEdgeHeight, startNodeCord[2] + zEdgeWidth,   // top left
                    startNodeCord[0] + xEdgeLength, startNodeCord[1] - yEdgeHeight, startNodeCord[2] + zEdgeWidth,   //bottom left
                    endNodeCord[0] - xEdgeLength, endNodeCord[1] - yEdgeHeight, endNodeCord[2] + zEdgeWidth,     //bottom right
                    endNodeCord[0] - xEdgeLength, endNodeCord[1] + yEdgeHeight, endNodeCord[2] + zEdgeWidth,   //top right
                    //Left
                    startNodeCord[0] + xEdgeLength, startNodeCord[1] + yEdgeHeight, startNodeCord[2] - zEdgeWidth,   // top left
                    startNodeCord[0] + xEdgeLength, startNodeCord[1] - yEdgeHeight, startNodeCord[2] - zEdgeWidth,   //bottom left
                    endNodeCord[0] - xEdgeLength, endNodeCord[1] - yEdgeHeight, endNodeCord[2] - zEdgeWidth,     //bottom right
                    endNodeCord[0] - xEdgeLength, endNodeCord[1] + yEdgeHeight, endNodeCord[2] - zEdgeWidth,   //top right
            };
        }
        else
            throw new NullPointerException();
        return rectangleCoords;
    }

    private short[] setDrawList(short[] drawOrder, int index){
        int offset=(short)(index*16);
        short[] thisOrder=new short[drawOrder.length];
        for(int i=0;i<drawOrder.length;++i){
            thisOrder[i]=(short)(drawOrder[i]+offset);
        }
        return thisOrder;
    }
}