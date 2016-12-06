/*
*  
* Support zoom/moving/rotation of the objects.
*/
/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.android.GameDrawing;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import java.nio.FloatBuffer;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
//TODO:Render Diagonal Edge
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";

    private NodeToDraw mNodeToDraw;

    private EdgeToDraw mEdgeC1ToDraw;
    private EdgeToDraw mEdgeC2ToDraw;
    private EdgeToDraw mEdgeC3ToDraw;

    //Side length of node
    private final float NodeSideLength=0.1f;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private float mAngle;

    private float scaleFactor=1.0f;

    private float mRotationX=0f;
    private float mRotationY=0f;
    private float mRotationZ=1.0f;
    private final int GL_CULL_FACE=0x00000b44;
    private final int GL_BACK=0x00000405;

    private final int GL_DEPTH_TEST=0x00000b71;

    float[] scratch = new float[16];

    private int touchNodeIndex=-2;
    // Position of eye in front of the origin
    private float eyeX;
    private float eyeY;
    private float eyeZ;

    private float centerX;
    private float centerY;
    private float centerZ;

    // This is where our head would be pointing were we holding the camera.
    private float upX;
    private float upY;
    private float upZ;
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        setCameraDefault();

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //Enable culling face feature to improve performance
        GLES20.glEnable(GL_CULL_FACE);
        GLES20.glCullFace(GL_BACK);

        //without depth test, objects far away from camera may block objects close to camera
        GLES20.glEnable(GL_DEPTH_TEST);

        //NodeToDraw, represented by cube
        mNodeToDraw = new NodeToDraw(OpenGLES20Activity.mGame.getNodeNum(), NodeSideLength, NodeSideLength, NodeSideLength);
        //EdgeToDraw, with different cost
        //TODO:Not urgent, currently different color requires different shader, find a way to reduce shader
        mEdgeC1ToDraw = new EdgeToDraw(OpenGLES20Activity.mGame.getUsedEdge(),1,NodeSideLength, NodeSideLength, NodeSideLength);
        mEdgeC2ToDraw = new EdgeToDraw(OpenGLES20Activity.mGame.getUsedEdge(),2,NodeSideLength, NodeSideLength, NodeSideLength);
        mEdgeC3ToDraw = new EdgeToDraw(OpenGLES20Activity.mGame.getUsedEdge(),3,NodeSideLength, NodeSideLength, NodeSideLength);

    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation for the triangle

        // Use the following code to generate constant rotation.
        // Leave this code out when using TouchEvents.
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);

        Matrix.setRotateM(mRotationMatrix, 0, mAngle, mRotationX, mRotationY, mRotationZ);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        //scale matrix "scratch" in x,y,z by "scaleFactor", offset=0
        Matrix.scaleM(scratch, 0, scaleFactor, scaleFactor, scaleFactor);

        mNodeToDraw.draw(scratch);

        //long startTime = System.currentTimeMillis();
        mEdgeC1ToDraw.draw(scratch);
        mEdgeC2ToDraw.draw(scratch);
        mEdgeC3ToDraw.draw(scratch);
       /* long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Log.d("time","time "+elapsedTime);*/
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 18);

    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }

    public float getScaleFactor(){
        return scaleFactor;
    }

    public void setScaleFactor(float factor){
        scaleFactor=factor;
    }

    public void setRotationAxis(float x, float y, float z){
        mRotationX=x;
        mRotationY=y;
        mRotationZ=z;
    }

    public void setSelectedNode(int xCord, int yCord){
        int h=OpenGLES20Activity.height-yCord;
        FloatBuffer bb = FloatBuffer.allocate(4);
        GLES20.glFlush();
        GLES20.glReadPixels(xCord,h,1, 1, GLES20.GL_RGBA,GLES20.GL_FLOAT, bb);
        float clickRed = bb.get(0);
        float clickGreen = bb.get(1);
        float clickBlue = bb.get(2) ;
        float clickAlpha=bb.get(3) ;
        if( clickAlpha==0.8f)
            touchNodeIndex= (int)((clickRed)*255f);
        else
            touchNodeIndex=-2;
    }

    public void setSelectedNode(int num) {
        touchNodeIndex = num;
    }
    public int getSelectedNodeIndex(){
        return touchNodeIndex;
    }
   /* public void redrawNodes(int indexOld, int indexNew){
        mNodeToDraw[indexOld].setColor((float)indexOld/255f, 128f/255f, 128f/255f, 0.8f);
        mNodeToDraw[indexOld].draw(scratch);
        mNodeToDraw[indexNew].setColor(1f,1f, 1f, 1.0f);
        mNodeToDraw[indexNew].draw(scratch);
    }*/
//TODO:button to set default, reference see giuhub code in bookmark->opengl
    public void setCameraDefault(){
        eyeX=0.5f;
        eyeY=1.5f;
        eyeZ=-2.0f;
        centerX=eyeX;
        centerY=0f;
        centerZ=0f;
        upX=0f;
        upY=1.0f;
        upZ=0f;
    }
    //TODO: User defined sentivity
    public void setCamera(float dx, float dy){
        eyeX+=dx/150.0f;
        centerX=eyeX;
        eyeY+=dy/200.0f;
        centerY+=dy/200.0f;
    }

}