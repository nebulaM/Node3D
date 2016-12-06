/*
*  
* Modified from Android's Tutorial on drawing a 2D square using OpenGl:
* https://developer.android.com/training/graphics/opengl/draw.html
* License: Apache 2.0
* http://www.apache.org/licenses/LICENSE-2.0
* Batch drawing many cubes, xyz parameter and color information are inputs to the constructor of this class.
* */
package com.github.android.GameDrawing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
//TODO: Touchable Nodes
/**
 * A 3-dimensional cube for use as a drawn object in OpenGL ES 2.0.
 */
public class NodeToDraw {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 vColor1;"+
                    "varying lowp vec4 vColor;"+
                    "void main() {" +
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  vColor=vColor1;"+
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying lowp vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final FloatBuffer colorBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    // number of color parameter per vertex in this array
    static final int COLORS_PER_VERTEX = 4;

    private final float cubeCords[];
    private final short drawOrder[];
    private float cubeColors[] ;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes(float) per vertex

    private final int colorStride =  COLORS_PER_VERTEX * 4;// 4 bytes per vertex


    private int totalNodes;

    private final int nodeCordArraySize=72;
    private final int nodeCordDrawOrderArraySize=36;
    private final int nodeCordDrawOrderSize=24;

    private final int colorIndexPerCube=96;//24 vertex per cube, where each vertex has 4 color components
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public NodeToDraw(int totalNodes, float xLength, float yLength, float zLength) {
        this.totalNodes=totalNodes;
        final float halfXLength = xLength / 2.0f;
        final float halfYLength = yLength / 2.0f;
        final float halfZLength = zLength / 2.0f;
        cubeCords = new float[nodeCordArraySize*this.totalNodes];
        drawOrder=new short[nodeCordDrawOrderArraySize*this.totalNodes];
        cubeColors = new float[4*nodeCordDrawOrderSize*this.totalNodes];
        float[] nodeCord;
        float xCord,yCord,zCord;
        for(int i=0; i< this.totalNodes; ++i) {
            nodeCord=OpenGLES20Activity.mGame.getNodeCordOpenGL(i);
            xCord=nodeCord[0];
            yCord=nodeCord[1];
            zCord=nodeCord[2];
            /*boxCords = new float[]{
                //Z POS(Front)
                xCord - halfXLength, yCord + halfYLength, zCord + halfZLength,   // top left, looking from +z
                xCord - halfXLength, yCord - halfYLength, zCord + halfZLength,   // bottom left
                xCord + halfXLength, yCord - halfYLength, zCord + halfZLength,   // bottom right
                xCord + halfXLength, yCord + halfYLength, zCord + halfZLength,   // top right

                //Z NEG(Back)
                xCord - halfXLength, yCord + halfYLength, zCord - halfZLength,   // top left, looking from -z
                xCord - halfXLength, yCord - halfYLength, zCord - halfZLength,   // bottom left
                xCord + halfXLength, yCord - halfYLength, zCord - halfZLength,   // bottom right
                xCord + halfXLength, yCord + halfYLength, zCord - halfZLength,   // top right

                //Y POS(Top)
                xCord - halfXLength, yCord + halfYLength, zCord - halfZLength,   // top left, looking from +y
                xCord - halfXLength, yCord + halfYLength, zCord + halfZLength,   // bottom left
                xCord + halfXLength, yCord + halfYLength, zCord + halfZLength,   // bottom right
                xCord + halfXLength, yCord + halfYLength, zCord - halfZLength,   // top right

                //Y NEG(Bottom)
                xCord - halfXLength, yCord - halfYLength, zCord - halfZLength,   // top left, looking from -y
                xCord - halfXLength, yCord - halfYLength, zCord + halfZLength,   // bottom left
                xCord + halfXLength, yCord - halfYLength, zCord + halfZLength,   // bottom right
                xCord + halfXLength, yCord - halfYLength, zCord - halfZLength,   // top right

                //X POS(Right)
                xCord + halfXLength, yCord + halfYLength, zCord + halfZLength,   // top left, looking from +x
                xCord + halfXLength, yCord - halfYLength, zCord + halfZLength,   // bottom left
                xCord + halfXLength, yCord - halfYLength, zCord - halfZLength,   // bottom right
                xCord + halfXLength, yCord + halfYLength, zCord - halfZLength,   // top right

                //X NEG(Left)
                xCord - halfXLength, yCord + halfYLength, zCord + halfZLength,   // top left, looking from -x
                xCord - halfXLength, yCord - halfYLength, zCord + halfZLength,   // bottom left
                xCord - halfXLength, yCord - halfYLength, zCord - halfZLength,   // bottom right
                xCord - halfXLength, yCord + halfYLength, zCord - halfZLength,   // top right

        };*/
            //Z POS(Front)
            cubeCords[0+i*nodeCordArraySize]= xCord - halfXLength;cubeCords[1+i*nodeCordArraySize]= yCord + halfYLength;cubeCords[2+i*nodeCordArraySize]= zCord + halfZLength;   // top left, looking from +z
            cubeCords[3+i*nodeCordArraySize]= xCord - halfXLength;cubeCords[4+i*nodeCordArraySize]= yCord - halfYLength;cubeCords[5+i*nodeCordArraySize]= zCord + halfZLength;   // bottom left
            cubeCords[6+i*nodeCordArraySize]= xCord + halfXLength;cubeCords[7+i*nodeCordArraySize]= yCord - halfYLength;cubeCords[8+i*nodeCordArraySize]= zCord + halfZLength;   // bottom right
            cubeCords[9+i*nodeCordArraySize]= xCord + halfXLength;cubeCords[10+i*nodeCordArraySize]=yCord + halfYLength;cubeCords[11+i*nodeCordArraySize]= zCord + halfZLength;   // top right

            //Z NEG(Back)
            cubeCords[12+i*nodeCordArraySize]= xCord - halfXLength;cubeCords[13+i*nodeCordArraySize]= yCord + halfYLength;cubeCords[14+i*nodeCordArraySize]= zCord - halfZLength;   // top left, looking from -z
            cubeCords[15+i*nodeCordArraySize]= xCord - halfXLength;cubeCords[16+i*nodeCordArraySize]= yCord - halfYLength;cubeCords[17+i*nodeCordArraySize]= zCord - halfZLength;   // bottom left
            cubeCords[18+i*nodeCordArraySize]= xCord + halfXLength;cubeCords[19+i*nodeCordArraySize]= yCord - halfYLength;cubeCords[20+i*nodeCordArraySize]= zCord - halfZLength;   // bottom right
            cubeCords[21+i*nodeCordArraySize]=xCord + halfXLength;cubeCords[22+i*nodeCordArraySize]= yCord + halfYLength;cubeCords[23+i*nodeCordArraySize]= zCord - halfZLength;   // top right
            //Y POS(Top)
            cubeCords[24+i*nodeCordArraySize]= xCord - halfXLength; cubeCords[25+i*nodeCordArraySize]= yCord + halfYLength; cubeCords[26+i*nodeCordArraySize]= zCord - halfZLength;   // top left; looking from +y
            cubeCords[27+i*nodeCordArraySize]= xCord - halfXLength; cubeCords[28+i*nodeCordArraySize]= yCord + halfYLength; cubeCords[29+i*nodeCordArraySize]= zCord + halfZLength;   // bottom left
            cubeCords[30+i*nodeCordArraySize]= xCord + halfXLength; cubeCords[31+i*nodeCordArraySize]= yCord + halfYLength; cubeCords[32+i*nodeCordArraySize]= zCord + halfZLength;   // bottom right
            cubeCords[33+i*nodeCordArraySize]= xCord + halfXLength; cubeCords[34+i*nodeCordArraySize]= yCord + halfYLength; cubeCords[35+i*nodeCordArraySize]= zCord - halfZLength;   // top right

            //Y NEG(Bottom)
            cubeCords[36+i*nodeCordArraySize]= xCord - halfXLength; cubeCords[37+i*nodeCordArraySize]= yCord - halfYLength; cubeCords[38+i*nodeCordArraySize]= zCord - halfZLength;   // top left; looking from -y
            cubeCords[39+i*nodeCordArraySize]= xCord - halfXLength; cubeCords[40+i*nodeCordArraySize]= yCord - halfYLength; cubeCords[41+i*nodeCordArraySize]= zCord + halfZLength;   // bottom left
            cubeCords[42+i*nodeCordArraySize]= xCord + halfXLength; cubeCords[43+i*nodeCordArraySize]= yCord - halfYLength; cubeCords[44+i*nodeCordArraySize]= zCord + halfZLength;   // bottom right
            cubeCords[45+i*nodeCordArraySize]= xCord + halfXLength; cubeCords[46+i*nodeCordArraySize]= yCord - halfYLength; cubeCords[47+i*nodeCordArraySize]= zCord - halfZLength;   // top right

            //X POS(Right)
            cubeCords[48+i*nodeCordArraySize]= xCord + halfXLength; cubeCords[49+i*nodeCordArraySize]= yCord + halfYLength; cubeCords[50+i*nodeCordArraySize]= zCord + halfZLength;   // top left; looking from +x
            cubeCords[51+i*nodeCordArraySize]= xCord + halfXLength; cubeCords[52+i*nodeCordArraySize]= yCord - halfYLength; cubeCords[53+i*nodeCordArraySize]= zCord + halfZLength;   // bottom left
            cubeCords[54+i*nodeCordArraySize]= xCord + halfXLength; cubeCords[55+i*nodeCordArraySize]= yCord - halfYLength; cubeCords[56+i*nodeCordArraySize]= zCord - halfZLength;   // bottom right
            cubeCords[57+i*nodeCordArraySize]= xCord + halfXLength; cubeCords[58+i*nodeCordArraySize]= yCord + halfYLength; cubeCords[59+i*nodeCordArraySize]= zCord - halfZLength;   // top right

            //X NEG(Left)
            cubeCords[60+i*nodeCordArraySize]= xCord - halfXLength; cubeCords[61+i*nodeCordArraySize]= yCord + halfYLength; cubeCords[62+i*nodeCordArraySize]= zCord + halfZLength;   // top left; looking from -x
            cubeCords[63+i*nodeCordArraySize]= xCord - halfXLength; cubeCords[64+i*nodeCordArraySize]= yCord - halfYLength; cubeCords[65+i*nodeCordArraySize]= zCord + halfZLength;   // bottom left
            cubeCords[66+i*nodeCordArraySize]= xCord - halfXLength; cubeCords[67+i*nodeCordArraySize]= yCord - halfYLength; cubeCords[68+i*nodeCordArraySize]= zCord - halfZLength;   // bottom right
            cubeCords[69+i*nodeCordArraySize]= xCord - halfXLength; cubeCords[70+i*nodeCordArraySize]= yCord + halfYLength; cubeCords[71+i*nodeCordArraySize]= zCord - halfZLength;   // top right
            /*
            { 0,  1,  2,  0,  2,  3,  //front, looks CCW from +z
            4,  6,  5,  4,  7,  6,  //back, looks CW from +z, but CCW from -z
            8,  9,  10, 8,  10, 11, //top
            12, 14, 13, 12, 15, 14, //bottom
            16, 17, 18, 16, 18, 19, //right
            20, 22, 21, 20, 23, 22  //left
            };   // order to draw vertices
            */
            for(int j=0;j<3;++j) {
                drawOrder[0+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+i*nodeCordDrawOrderSize);
                drawOrder[1+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+1+i*nodeCordDrawOrderSize);
                drawOrder[2+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+2+i*nodeCordDrawOrderSize);
                drawOrder[3+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+i*nodeCordDrawOrderSize);
                drawOrder[4+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+2+i*nodeCordDrawOrderSize);
                drawOrder[5+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+3+i*nodeCordDrawOrderSize);
                drawOrder[6+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+4+i*nodeCordDrawOrderSize);
                drawOrder[7+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+6+i*nodeCordDrawOrderSize);
                drawOrder[8+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+5+i*nodeCordDrawOrderSize);
                drawOrder[9+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+4+i*nodeCordDrawOrderSize);
                drawOrder[10+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+7+i*nodeCordDrawOrderSize);
                drawOrder[11+j*12 + i * nodeCordDrawOrderArraySize] = (short)(j*8+6+i*nodeCordDrawOrderSize);
            }

            if(OpenGLES20Activity.mGame.getPlayerIsHere(i)){
                for(int j=0; j<nodeCordDrawOrderSize;j++ ) {//white
                    cubeColors[j*4 + i*4*nodeCordDrawOrderSize] = 1.0f;
                    cubeColors[j*4+1 + i*4*nodeCordDrawOrderSize] = 1f;
                    cubeColors[j*4+2 + i*4*nodeCordDrawOrderSize] = 1f;
                    cubeColors[j*4+3 + i*4*nodeCordDrawOrderSize] = 1.0f;
                }
            }
            else {
                for(int j=0; j<nodeCordDrawOrderSize;j++ ) {
                    cubeColors[j*4 + i*4*nodeCordDrawOrderSize] = (float)((i+1)*20/255f);//((float) (i+1)*10)/ 255f;
                    cubeColors[j*4+1 + i*4*nodeCordDrawOrderSize] = 0.5f;
                    cubeColors[j*4+2 + i*4*nodeCordDrawOrderSize] = 0.5f;
                    cubeColors[j*4+3 + i*4*nodeCordDrawOrderSize] = 0.8f;
                }
            }
        }

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer vb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                cubeCords.length * 4);
        vb.order(ByteOrder.nativeOrder());
        vertexBuffer = vb.asFloatBuffer();
        vertexBuffer.put(cubeCords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                cubeColors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(cubeColors);
        colorBuffer.position(0);

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
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the vertices location
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // get handle to vertex shader's vColor member
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor1");

        // Enable a handle to the vertices color
        GLES20.glEnableVertexAttribArray(mColorHandle);

        //set a pointer points to colorBuffer that contains vertex color
        GLES20.glVertexAttribPointer(mColorHandle, COLORS_PER_VERTEX, GLES20.GL_FLOAT, false, colorStride, colorBuffer);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw all elements
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex position array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        // Disable vertex color array
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}