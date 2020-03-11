package com.givevision.lifevideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.givevision.sightplus.objects.LeftScreen;
import com.givevision.sightplus.objects.RightScreen;
import com.givevision.sightplus.programs.TextureShaderProgram;
import com.givevision.sightplus.util.Constants;
import com.BaseRenderrer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
public class MainRenderer extends BaseRenderrer implements  SurfaceTexture.OnFrameAvailableListener, Orientation.Listener {
    private static final String TAG = "MainRenderer";

    private int[] hTex;

    private SurfaceTexture mSTexture;
    private boolean mGLInit = false;
    private boolean mUpdateST = false;
    private MainView mView;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private String mCameraID;
    private Size mPreviewSize = new Size(1920, 1080);

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private boolean progChanging = true;

    private final Context context;
    private final float[] projectionMatrix = new float[16];
    private LeftScreen leftScreen;
    private RightScreen rightScreen;
    private TextureShaderProgram textureProgram;
    private float mZoom = 0f;

    //private Triangle triangle;
    public CameraManager manager;
    public float bluePercent = 1.0f;
    public float blueTxtPercent = 0.0f;
    public final SurfaceHolder ourHolder;

    private Orientation mOrientation;
    private SensorManager mSensorManager;
    private WindowManager mWindowManager;

    private WeakReference<MainActivity> mWeakActivity;
    public MainActivity activity;

    public MainRenderer(MainView view, MainActivity act, SurfaceHolder ourHolder) {
        this.context = view.getContext();
        this.mView = view;
        this.manager = (CameraManager) mView.getContext().getSystemService(Context.CAMERA_SERVICE);
        this.mWeakActivity = new WeakReference<MainActivity>(act);
        this.activity = mWeakActivity.get();
        this.ourHolder = ourHolder;
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        offsetX = 0f;
        offsetY = 0f;
        this.mOrientation = new Orientation(mSensorManager, mWindowManager);

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

//		LogManagement.Log_v(TAG, "onOrientationChanged:: posY= "+posY+ " posX= "+posX);
    }


    public void runtimeException(RuntimeException ex) {
        LogManagement.Log_i(TAG, "runtimeException:: RuntimeException : " + ex);
    }

    public void onResume() {
        LogManagement.Log_i(TAG, "MainRenderer:: onResume: started");
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				loadPic();
//			}
//		}).start();
    }

    public void onPause() {
        LogManagement.Log_i(TAG, "MainRenderer::onPause: started");
        if (mGLInit) {
            mGLInit = false;
            mUpdateST = false;
            closeCamera();
            stopBackgroundThread();
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

        LogManagement.Log_i(TAG, "MainRenderer:: onSurfaceCreated: do to cacPreviewSize");
        cacPreviewSize(ss.x, ss.y);
        LogManagement.Log_i(TAG, "MainRenderer:: onSurfaceCreated: do to openCamera");
        startBackgroundThread();
        openCamera();
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
//		LogManagement.Log_d(TAG, "MainRenderer:: onFrameAvailable from camera");
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
//		LogManagement.Log_d(TAG, "MainRenderer:: onDrawFrame to screen");
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

    void cacPreviewSize(final int width, final int height) {
        try {
            for (String cameraID : manager.getCameraIdList()) {
                LogManagement.Log_d(TAG, "MainRenderer:: cacPreviewSize:: cam mCameraID=" + cameraID);
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;

                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    mCameraID = cameraID;
                    Log.d("Img", "MainRenderer:: cacPreviewSize:: INFO_SUPPORTED_HARDWARE_LEVEL camera " + cameraID +
                            " level :" + characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL));
                    StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] sizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
                    List<Size> listSizes = new ArrayList<Size>();
                    for (Size size : sizes) {
                        listSizes.add(size);
                        LogManagement.Log_d(TAG, "MainRenderer:: cacPreviewSize:: camera=" + cameraID + " size=" + size);
                    }

                    int[] VideoStabilizationModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
                    for (int stabilizationMode : VideoStabilizationModes) {
                        LogManagement.Log_d(TAG, "MainRenderer:: cacPreviewSize:: camera=" + cameraID + " stabilizationMode=" + stabilizationMode);
                    }

                    Constants.MAX_CAM_ZOOM = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
                    LogManagement.Log_d(TAG, "MainRenderer:: cacPreviewSize::  max zoom=" + Constants.MAX_CAM_ZOOM);

                    LogManagement.Log_d(TAG, "MainRenderer:: cacPreviewSize::  mCameraID=" + cameraID);

                    mPreviewSize = new Size(getOptimalVideoSnapshotPictureSize(listSizes, (width / 2) / height).getWidth(), (int) (getOptimalVideoSnapshotPictureSize(listSizes, width / height).getHeight()));
                    ;

                    LogManagement.Log_v(TAG, "MainRenderer:: cacPreviewSize:: mPreviewSize=  " + mPreviewSize.getWidth() + " - " + mPreviewSize.getHeight());
                    break;
                }
            }
        } catch (CameraAccessException e) {
            LogManagement.Log_e("mr", "RenderThread:: cacPreviewSize:: cacPreviewSize - Camera Access Exception");
            runtimeException(new RuntimeException("Interrupted cacPreviewSize CameraAccessException " + e));
        } catch (IllegalArgumentException e) {
            LogManagement.Log_e("mr", "RenderThread:: cacPreviewSize:: cacPreviewSize - Illegal Argument Exception");
            runtimeException(new RuntimeException("Interrupted cacPreviewSize IllegalArgumentException " + e));
        } catch (SecurityException e) {
            LogManagement.Log_e("mr", "RenderThread:: cacPreviewSize:: cacPreviewSize - Security Exception");
            runtimeException(new RuntimeException("Interrupted cacPreviewSize SecurityException " + e));
        }
    }

    private CameraCharacteristics characteristics;
    private Size[] jpegSizes;

    @SuppressLint("MissingPermission")
    void openCamera() {
        LogManagement.Log_i(TAG, "MainRenderer:: Camera Build.VERSION.SDK_INT " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                if (mCameraID != null) {
                    characteristics = manager.getCameraCharacteristics(mCameraID);
                    try {
                        if (!mCameraOpenCloseLock.tryAcquire(5000, TimeUnit.MILLISECONDS)) {
                            throw new RuntimeException("Time out waiting to lock camera opening.");
                        } else {
                            try {
                                manager.openCamera(mCameraID, mStateCallback, mBackgroundHandler);
                                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                                int width = 640;
                                int height = 480;
                                if (jpegSizes != null && 0 < jpegSizes.length) {
                                    width = jpegSizes[0].getWidth();
                                    height = jpegSizes[0].getHeight();
                                }
                                Log.d(TAG, "MainRenderer:: openCamera width= " + width + " height= " + height);
                            } catch (CameraAccessException e) {
                                LogManagement.Log_e("mr", "MainRenderer:: OpenCamera - Camera Access Exception" + e);
                                runtimeException(new RuntimeException("Interrupted openCamera Camera Access Exception e " + e));
                            }
                        }
                    } catch (InterruptedException e1) {
                        LogManagement.Log_e("mr", "MainRenderer:: OpenCamera - InterruptedException" + e1);
                        runtimeException(new RuntimeException("Interrupted openCamera InterruptedException e1 " + e1));
                    }
                } else {
                    runtimeException(new RuntimeException("Interrupted mCameraID null"));
                }
            } catch (CameraAccessException e1) {
                LogManagement.Log_e("mr", "MainRenderer:: OpenCamera - Camera Access Exception" + e1);
                runtimeException(new RuntimeException("Interrupted openCamera Camera Access Exception " + e1));
            }
        }
    }


    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            LogManagement.Log_v("mr", "MainRenderer:: Interrupted while trying to closeCamera " + e);
            runtimeException(new RuntimeException("Interrupted while trying to closeCamera ", e));
        } finally {
            mCameraOpenCloseLock.release();
            LogManagement.Log_v("mr", "MainRenderer:: mCameraOpenCloseLock released");
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            LogManagement.Log_v("mr", "MainRenderer:: camera onOpened cameraDevice id=" + cameraDevice.getId());
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            LogManagement.Log_v("mr", "MainRenderer:: camera onDisconnected cameraDevice id=" + cameraDevice.getId());
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            LogManagement.Log_v("mr", "MainRenderer:: camera onError cameraDevice id=" + cameraDevice.getId() + " error=" + error);
            try {
                manager.openCamera(mCameraID, mStateCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                mCameraOpenCloseLock.release();
                cameraDevice.close();
                mCameraDevice = null;

                runtimeException(new RuntimeException("Interrupted onError cameraDevice id=" + cameraDevice.getId() + " error=" + error + " CameraAccessException=" + e));
            }
        }
    };

    private void createCameraPreviewSession() {
        try {
            LogManagement.Log_v("mr", "MainRenderer:: createCameraPreviewSession mPreviewSize:  " + mPreviewSize.getWidth() + " - " + mPreviewSize.getHeight());

            mSTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Surface surface = new Surface(mSTexture);

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mPreviewRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) {
                                runtimeException(new RuntimeException("Interrupted cameraCaptureSession mCameraDevice null"));
                                return;
                            }

                            LogManagement.Log_i("mr", "MainRenderer:: createCaptureSession");

                            mCaptureSession = cameraCaptureSession;
                            try {
                                final int[] availableOpticalStabilization = characteristics.get(
                                        CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
                                if (availableOpticalStabilization != null) {
                                    for (int mode : availableOpticalStabilization) {
                                        if (mode == CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON) {
                                            mPreviewRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                                                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
                                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                                                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
                                            LogManagement.Log_d(TAG, "MainRenderer:: Using optical stabilization.");
//											return;
                                        }
                                    }
                                }
                                mPreviewRequestBuilder.set(
                                        CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_FAST);

//                            	mPreviewRequestBuilder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_FAST);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
//                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
//                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_TWILIGHT);
//                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);

                                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                LogManagement.Log_e("mr", "MainRenderer:: createCaptureSession e " + e);
                                runtimeException(new RuntimeException("Interrupted createCaptureSession CameraAccessException: " + e));
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            runtimeException(new RuntimeException("Interrupted cameraCaptureSession onConfigureFailed"));
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            LogManagement.Log_e("mr", "MainRenderer:: createCameraPreviewSession");
            runtimeException(new RuntimeException("Interrupted CameraAccessException e " + e));
        }
    }

    private void startBackgroundThread() {
        LogManagement.Log_i(TAG, "MainRenderer:: startBackgroundThread: started");
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        LogManagement.Log_i(TAG, "MainRenderer:: startBackgroundThread: stoped");
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;

            } catch (InterruptedException e) {
                LogManagement.Log_e("mr", "MainRenderer:: stopBackgroundThread");
                runtimeException(new RuntimeException("Interrupted stopBackgroundThread InterruptedException e " + e));
            }
        }
        mBackgroundHandler = null;
    }

    /**
     * Calculates sensor crop region for a zoom level (zoom >= 1.0).
     *
     * @return Crop region.
     */
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

    private void test() {
        LogManagement.Log_i(TAG, "MainRenderer:: Zoom CONTROL_MAX_REGIONS_AF: " + characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF));
        LogManagement.Log_i(TAG, "MainRenderer:: Zoom SENSOR_INFO_ACTIVE_ARRAY_SIZE: " + characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE));
        MeteringRectangle[] focusAreas = new MeteringRectangle[1];

        Rect sensor_rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Rect focusArea = new Rect(
                (int) (sensor_rect.width() * 0.4),
                (int) (sensor_rect.height() * 0.4),
                (int) (sensor_rect.width() * 0.75),
                (int) (sensor_rect.height() * 0.75));
        focusAreas[0] = new MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX);

        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);

        if (characteristics != null && characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) > 0) {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraCharacteristics.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, focusAreas);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraCharacteristics.CONTROL_AF_TRIGGER_START);
        }
        try {
            mCaptureSession.stopRepeating();
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);

        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            LogManagement.Log_e("mr", "MainRenderer:: createCaptureSession e " + e);
            runtimeException(new RuntimeException("Interrupted setZoomValue CameraAccessException e " + e));
        }
        LogManagement.Log_i(TAG, "MainRenderer:: Zoom applied: ");
    }

    public void setBluePercent(float bluePercent) {
        this.bluePercent = bluePercent;
    }

    public void setBlueTxtPercent(float blueTxtPercent) {
        this.blueTxtPercent = blueTxtPercent;
    }

    private boolean isRectagleSet = false;
    private boolean isListening = false;

    public boolean setZoomValue(float zoom) {
        if (!mGLInit || mCaptureSession == null) return false;
        LogManagement.Log_w(TAG, "MainRenderer:: Zoom : " + zoom + " mZoom : " + mZoom);
        if (mZoom > 0.04 && isStop) {
            if (!isListening) {
                this.mOrientation.startListening(this);
                isListening = true;
            }
        } else {
            if (isListening) {
                this.mOrientation.stopListening();
                offsetX = 0f;
                offsetY = 0f;
                isListening = false;
            }
        }
        if (zoom <= Constants.MAX_CAM_ZOOM) {
            isRectagleSet = false;
            mZoom = 0;
            Rect zoomValue = cropRegionForZoom(characteristics, zoom);
            mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomValue);
            try {
//        		mCaptureSession.stopRepeating();
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
            } catch (CameraAccessException e) {
                // TODO Auto-generated catch block
                LogManagement.Log_e("mr", "MainRenderer:: createCaptureSession e " + e);
                runtimeException(new RuntimeException("Interrupted setZoomValue CameraAccessException e " + e));
            }
            LogManagement.Log_i(TAG, "MainRenderer:: Zoom applied: " + zoom);
        } else {
            if (mZoom <= Constants.MAX_CAM_ZOOM + Constants.MAX_GL_ZOOM) {
                if (!isRectagleSet) {
                    isRectagleSet = true;
                    Rect zoomValue = cropRegionForZoom(characteristics, Constants.MAX_CAM_ZOOM);
                    mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomValue);
                    try {
//                		mCaptureSession.stopRepeating();
                        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        // TODO Auto-generated catch block
                        LogManagement.Log_e("mr", "MainRenderer:: createCaptureSession e " + e);
                        runtimeException(new RuntimeException("Interrupted setZoomValue CameraAccessException e " + e));
                    }

                    LogManagement.Log_i(TAG, "MainRenderer:: Zoom applied: " + zoom);
                }
                mZoom = zoom - Constants.MAX_CAM_ZOOM;
            } else {
                mZoom = Constants.MAX_CAM_ZOOM;
            }
        }

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
