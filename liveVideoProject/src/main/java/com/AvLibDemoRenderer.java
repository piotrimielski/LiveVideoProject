package com;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.givevision.lifevideo.LogManagement;
import com.givevision.lifevideo.MainActivity;
import com.givevision.lifevideo.MainView;
import com.givevision.lifevideo.Orientation;
import com.givevision.sightplus.objects.LeftScreen;
import com.givevision.sightplus.objects.RightScreen;
import com.givevision.sightplus.programs.TextureShaderProgram;
import com.givevision.sightplus.util.Constants;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import static android.content.Context.SENSOR_SERVICE;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;
import static java.lang.Math.abs;

@SuppressLint("NewApi")
public class AvLibDemoRenderer extends BaseRenderrer implements SurfaceTexture.OnFrameAvailableListener, Orientation.Listener {
    private static final String TAG = "MainRenderer";

    private int[] hTex;

    private SurfaceTexture mSTexture;
    private boolean mGLInit = false;
    private boolean mUpdateST = false;
    private MainView mView;

    private Size mPreviewSize = new Size(1920, 1080);

    private boolean progChanging = true;

    private final Context context;
    private final float[] projectionMatrix = new float[16];
    private LeftScreen leftScreen;
    private RightScreen rightScreen;
    private TextureShaderProgram textureProgram;
    private float mZoom = 0f;

    public float bluePercent = 1.0f;
    public float blueTxtPercent = 0.0f;
    public final SurfaceHolder ourHolder;

    private Orientation mOrientation;
    private SensorManager mSensorManager;
    private WindowManager mWindowManager;

    private WeakReference<MainActivity> mWeakActivity;
    public MainActivity activity;

    private OnixLiveTestPlayer player;
    private String streamUrl;

    public static Bundle createArguments(String streamUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("url", streamUrl);
        return bundle;
    }

    public AvLibDemoRenderer(MainView view, MainActivity act, SurfaceHolder ourHolder, Bundle args) {
        streamUrl = args.getString("url");
        this.context = view.getContext();
        this.mView = view;
        this.mWeakActivity = new WeakReference<MainActivity>(act);
        this.activity = mWeakActivity.get();
        this.ourHolder = ourHolder;
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        offsetX = 0f;
        offsetY = 0f;
        this.mOrientation = new Orientation(mSensorManager, mWindowManager);
        player = new OnixLiveTestPlayer(act.getApplicationContext());

    }

    private float offsetX, offsetY;
    private boolean isProcessing = false;

    @Override
    public void onOrientationChanged(int posY, float roll, int posX) {
        if (!isProcessing) {
            isProcessing = true;
            if (abs(offsetX - posX / 120f) > 0.01) {
                offsetX = posX / 120f;
            }
            if (abs(offsetY - posY / 120f) > 0.01) {
                offsetY = posY / 120f;
            }
            isProcessing = false;
        }
    }

    public void onResume() {
        LogManagement.Log_i(TAG, "MainRenderer:: onResume: started");
    }

    public void onPause() {
        LogManagement.Log_i(TAG, "MainRenderer::onPause: started");
        if (mGLInit) {
            mGLInit = false;
            mUpdateST = false;
            player.stopStream();
        }

    }

    public synchronized void onSurfaceCreated(GL10 unused, javax.microedition.khronos.egl.EGLConfig config) {
        if (mGLInit)
            return;
        LogManagement.Log_i(TAG, "MainRenderer:: onSurfaceCreated: started");

        initTex();
        mSTexture = new SurfaceTexture(hTex[0]);
        mSTexture.setOnFrameAvailableListener(this);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        leftScreen = new LeftScreen();
        rightScreen = new RightScreen();
        textureProgram = new TextureShaderProgram(context, 0);
        Point ss = new Point();
        mView.getDisplay().getRealSize(ss);
        mSTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        player.startStream(mSTexture);
        mGLInit = true;
    }


    boolean isPrecessing = false;

    public synchronized void onSurfaceChanged(GL10 unused, int width, int height) {
        LogManagement.Log_i(TAG, "MainRenderer:: onSurfaceChanged: started");
        glViewport(0, 0, width, height);
        orthoM(projectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
    }

    public static int loadShader(int type, String shaderCode) {
        //Create a Vertex Shader Type Or a Fragment Shader Type (GLES20.GL_VERTEX_SHADER OR GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        //Add The Source Code and Compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private void drawTexture() {
        if (isProcessing)
            return;

        glClear(GL_COLOR_BUFFER_BIT);

        // Draw the table.
        if (progChanging) {
            progChanging = false;
            if (textureProgram != null) {
                textureProgram.deleteProgram();
            }
            textureProgram = new TextureShaderProgram(context, progPos);
        }
        textureProgram.useProgram();
        if (progPos == 3)
            textureProgram.setUniforms(projectionMatrix, hTex[0], blueTxtPercent);
        else
            textureProgram.setUniforms(projectionMatrix, hTex[0], bluePercent);

        leftScreen.bindData(textureProgram, mZoom, -offsetX, -offsetY);
        leftScreen.draw();
        rightScreen.bindData(textureProgram, mZoom, -offsetX, -offsetY);
        rightScreen.draw();
    }


    private void initTex() {
        hTex = new int[1];
        glGenTextures(1, hTex, 0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    public synchronized void onFrameAvailable(SurfaceTexture st) {
        mView.requestRender();
        if (!mUpdateST) {
            mUpdateST = true;
        }
        frameDisponibleCamera();
    }


    private boolean isStop = false;

    public boolean isFrameStoped() {
        return isStop;
    }

    public synchronized void onDrawFrame(GL10 gl) {
        frameDisponibleGL();
        if (!mGLInit)
            return;
        if (mUpdateST && !isStop) {
            startFrameTime = System.currentTimeMillis();
            mSTexture.updateTexImage();
            drawTexture();
            frameProcessing();
            mUpdateST = false;//
        } else {
            drawTexture();
        }
    }

    public static Rect cropRegionForZoom(CameraCharacteristics characteristics, float zoom) {
        Rect sensor = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        int xCenter = sensor.width() / 2;
        int yCenter = sensor.height() / 2;
        int xDelta = (int) (xCenter / zoom);
        int yDelta = (int) (yCenter / zoom);
        LogManagement.Log_i("mr", "MainRenderer:: cropRegionForZoom zoom=" + zoom + " sensor.width()=" + sensor.width() + " sensor.height()=" + sensor.height());
        LogManagement.Log_i("mr", "MainRenderer:: cropRegionForZoom Rect  xCenter=" + xCenter + " xDelta=" + xDelta + " yCenter=" + yCenter + " yDelta=" + yDelta);
        return new Rect(xCenter - xDelta, yCenter - yDelta, xCenter + xDelta, yCenter + yDelta);
    }

    public void setBluePercent(float bluePercent) {
        this.bluePercent = bluePercent;
    }

    public void setBlueTxtPercent(float blueTxtPercent) {
        this.blueTxtPercent = blueTxtPercent;
    }

    public boolean setZoomValue(float zoom) {
        mZoom = zoom - Constants.MAX_CAM_ZOOM;
        return true;
    }

    public int progPos = 0;

    public void setProgram(int p) {
        if (!mGLInit) return;
        progPos = p;
        progChanging = true;
    }


    // Returns the largest picture size which matches the given aspect ratio.
    private static Size getOptimalVideoSnapshotPictureSize(List<Size> sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null) return null;
        Size optimalSize = null;
        // Try to find a size matches aspect ratio and has the largest width
        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (optimalSize == null || size.getWidth() > optimalSize.getWidth()) {
                optimalSize = size;
            }
        }
        // Cannot find one that matches the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSize == null) {
            Log.w(TAG, "No picture size match the aspect ratio");
            for (Size size : sizes) {
                if (optimalSize == null || size.getWidth() > optimalSize.getWidth()) {
                    optimalSize = size;
                }
            }
        }
        return optimalSize;
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    private long startFrameTime = System.currentTimeMillis();
    private long fpsP = 0;

    private void frameProcessing() {
        //long startFrameTime = System.currentTimeMillis();
        // Calculate the fps this frame
        // We can then use the result to
        // time animations and more.

        new Thread(new Runnable() {
            public void run() {
                long timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fpsP = 1000 / timeThisFrame;
                }
//				Log.w(TAG, "Processing frame in GL fps: "+fpsP);
            }
        }).start();

    }

    protected long startFrameDisponibleTime = System.currentTimeMillis();
    long fpsC = 0;

    private void frameDisponibleCamera() {
        //long startFrameTime = System.currentTimeMillis();
        // Calculate the fps this frame
        // We can then use the result to
        // time animations and more.
        new Thread(new Runnable() {
            public void run() {
                fpsC = 0;
                long timeThisFrame = System.currentTimeMillis() - startFrameDisponibleTime;
                if (timeThisFrame >= 1) {
                    fpsC = 1000 / timeThisFrame;
                }
//				Log.w(TAG, "Disponible frame in camera fps: "+fpsC);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.setFPS(fpsC, fpsP, fpsS);
                    }
                });

                startFrameDisponibleTime = System.currentTimeMillis();
            }
        }).start();

    }

    private long startFrameGPUTime = System.currentTimeMillis();
    private long fpsS = 0;

    private void frameDisponibleGL() {
        //long startFrameTime = System.currentTimeMillis();
        // Calculate the fps this frame
        // We can then use the result to
        // time animations and more.
        new Thread(new Runnable() {
            public void run() {
                fpsS = 0;
                long timeThisFrame = System.currentTimeMillis() - startFrameGPUTime;
                if (timeThisFrame >= 1) {
                    fpsS = 1000 / timeThisFrame;
                }
//				Log.w(TAG, "Disponible frame in GPU fps: "+fpsS);//
                startFrameGPUTime = System.currentTimeMillis();
            }
        }).start();
    }

    public void setPause(boolean p) {
        isStop = p;
    }


}
