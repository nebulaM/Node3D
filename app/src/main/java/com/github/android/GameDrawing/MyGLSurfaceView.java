/*
* Modified by nebulaM
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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    private ScaleGestureDetector mScaleDetector;


    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        //set mScaleDetector for zoom in/out
         mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 360;
    private float mPreviousX;
    private float mPreviousY;

    private final float TanPos20=0.268f;
    private final float TanNeg20=-0.268f;

    private final float TanPos70=2.75f;
    private final float TanNeg70=-2.75f;

    //Max and min scale factor
    private final float ZOOM_SCALE_FACTOR_MIN=0.5f;
    private final float ZOOM_SCALE_FACTOR_MAX=15.0f;

    private float mScaleFactor;

    private float rx;
    private float ry;
    private int cameraMask;

    /**
     * This method monitors touch event, sets rotation angle in MyGLRenderer
     * Also enable the ScaleGestureDetector to monitor events
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Let the ScaleGestureDetector inspect all events
        mScaleDetector.onTouchEvent(e);

        //only interested in single touch
        if(e.getPointerCount() == 1) {
            float x = e.getX();
            float y = e.getY();
            rx = e.getRawX();
            ry = e.getRawY();
            cameraMask=0;
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;
                    float dxyTangent = dy / dx;

                    //max allowance moving in y(height) direction when rotating along y axis
                    //if the angle between x,y direction not in this range, then do not rotate
                    if (y > OpenGLES20Activity.height / 1.5f) {
                        if (Math.abs(dx) > 10) {
                        if ((dxyTangent > TanNeg20) && (dxyTangent < TanPos20)) {
                            if (dx > 30)
                                dx = 30;
                            if (dx < -30)
                                dx = -30;

                                mRenderer.setAngle(mRenderer.getAngle() + ((dx) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                                mRenderer.setRotationAxis(0f, 1.0f, 0f);
                                requestRender();
                            }
                        }
                    }
                    else if(Math.abs(dx) > 10 || Math.abs(dy)>10){
                        if(Math.abs(dx)<=10){
                            dx=0;
                        }
                        if(Math.abs(dy)<=10) {
                            dy=0;
                        }
                        cameraMask=1;
                        if(dx>30)
                            dx=30;
                        if(dx<-30)
                            dx=-30;
                        if(dy>30)
                            dy=30;
                        if(dy<-30)
                            dy=-30;
                        if(cameraMask==1) {
                            cameraMask=0;
                            mRenderer.setCamera(dx, dy);
                            requestRender();
                        }
                    }
                    mPreviousX = x;
                    mPreviousY = y;
                    break;

   /*             case MotionEvent.ACTION_DOWN:
                    //TODO:Tocuh on opengl render object NOT WORKING(shader ID)?
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                           mRenderer.setSelectedNode((int) rx, (int) ry);
                        }
                    });
                if (mRenderer.getSelectedNodeIndex() > 0) {
                    int playerPosition = OpenGLES20Activity.mPlayer.getCurrentPosition();
                    if (OpenGLES20Activity.mGame.getEdge(playerPosition, mRenderer.getSelectedNodeIndex())) {
                        OpenGLES20Activity.mGame.setPlayIsNOTHere(playerPosition);
                        OpenGLES20Activity.mGame.setPlayIsHere(mRenderer.getSelectedNodeIndex());
                        OpenGLES20Activity.mPlayer.setCurrentPosition(mRenderer.getSelectedNodeIndex());
                    }
                    mRenderer.setSelectedNode(-2);
                }
                    break;*/
            }
        }
        return true;
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        /**
         * set "scaleFactor" in "MyGLRenderer".
         * originally "scaleFactor" is set to 1.0f
         * @param detector
         * @return
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor = mRenderer.getScaleFactor()*detector.getScaleFactor();
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(ZOOM_SCALE_FACTOR_MIN, Math.min(mScaleFactor, ZOOM_SCALE_FACTOR_MAX));
            mRenderer.setScaleFactor(mScaleFactor);
            //redraw
            cameraMask=0;
            requestRender();
            return true;
        }
    }

}

