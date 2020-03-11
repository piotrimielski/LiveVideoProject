package com.givevision.lifevideo;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.WindowManager;

public class Orientation implements SensorEventListener {
    private static final String TAG = "Orientation";

    public interface Listener {
//        void onOrientationChanged(float pitch, float roll, float yaw);
        void onOrientationChanged(int posY, float roll, int posX);
    }

    private static final int SENSOR_DELAY_MICROS = SensorManager.SENSOR_DELAY_FASTEST; //50 * 1000; // 50ms

    private final SensorManager mSensorManager;
    private final Sensor mRotationSensor;
    private final WindowManager mWindowManager;

    private int mLastAccuracy;
    private Listener mListener;

    private Handler handler;

    public Orientation(SensorManager sensorManager, WindowManager windowManager) {
        mSensorManager = sensorManager;
        mWindowManager = windowManager;

        HandlerThread mHandlerThread = new HandlerThread("sensorThread");
        mHandlerThread.start();
        handler = new Handler(mHandlerThread.getLooper());

        // Can be null if the sensor hardware is not available
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void startListening(Listener listener) {
        if (mListener == listener) {
            return;
        }
        isPosMemo=false;
        mListener = listener;
        if (mRotationSensor == null) {
            LogManagement.Log_w(TAG,"Rotation vector sensor not available; will not provide orientation data.");
            return;
        }
        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY_MICROS, handler);
    }

    public void stopListening() {
        mSensorManager.unregisterListener(this);
        mListener = null;
        isPosMemo=false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener == null) {
            return;
        }
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (event.sensor == mRotationSensor) {
            updateOrientation(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }

    private boolean isPrecessed=false;
    private float oldYaw=-1000;;
    private void updateOrientation(float[] rotationVector) {
        // By default, remap the axes as if the front of the
        // device screen was the instrument panel.
        // Adjust the rotation matrix for the device orientation
        int worldAxisForDeviceAxisX = SensorManager.AXIS_X;
        int worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
        int screenRotation = mWindowManager.getDefaultDisplay().getRotation();
        if (screenRotation == Surface.ROTATION_0) {
            LogManagement.Log_w(TAG, "updateOrientation ROTATION_0");
            worldAxisForDeviceAxisX = SensorManager.AXIS_X;
            worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
        } else if (screenRotation == Surface.ROTATION_90) {
            LogManagement.Log_w(TAG, "updateOrientation ROTATION_90");
            worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
            worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;

        } else if (screenRotation == Surface.ROTATION_180) {
            LogManagement.Log_w(TAG, "updateOrientation ROTATION_180");
            worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
            worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
        } else if (screenRotation == Surface.ROTATION_270) {
            LogManagement.Log_w(TAG, "updateOrientation ROTATION_270");
            worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
            worldAxisForDeviceAxisY = SensorManager.AXIS_X;
        }
        if (!isPrecessed) {
            isPrecessed=true;
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

            float[] adjustedRotationMatrix = new float[9];
            SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                    worldAxisForDeviceAxisY, adjustedRotationMatrix);

            // Transform rotation matrix into azimuth/pitch/roll
            float[] orientation = new float[3];
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);

            // Convert radians to degrees
            float yaw = orientation[0] * -57;
            float pitch = orientation[1] * -57;
            float roll = orientation[2] * -57;
            int posY;
            int posX;
            int shiftY = 60;
            int shiftX = 60;
            //sightplus head moving left-right
            //left -> 0 to 180 -180 to 0
            //right -> 0 to -180 180 to 0
            //newYaw ->0 to 360

//            if(oldYaw==-1000){
//                oldYaw=yaw;
//            }else{
//                if(Math.abs(oldYaw-yaw)>10){
//                    yaw= oldYaw;
//                }
//            }
            float newYaw=yaw;
            if (yaw < 0) {
                newYaw = 180 + (180 + yaw);
            } else{
                newYaw = yaw;
            }

            if (!isPosMemo) {
                isPosMemo = true;
                y1 = (int) pitch;
                y2 = y1;
                x1 = (int) newYaw;
                x2 = x1;
            } else {
                y2 = (int) pitch;
                x2 = (int) newYaw;
            }
            int deltaY = y2 - y1;
            int deltaX = x2 - x1;

            // sightplus head moving up-down,
            //horizontal -> 0
            //down -> -90
            //up -> 90

            if (deltaY < 0 && deltaY < -shiftY) {
                posY = -shiftY;
            } else if (deltaY > 0 && deltaY > shiftY) {
                posY = shiftY;
            } else {
                posY = deltaY;
            }

            if (deltaX < 0 && deltaX < -shiftX) {
                posX = -shiftX;
            } else if (deltaX > 0 && deltaX > shiftX) {
                posX = shiftX;
            } else {
                posX = deltaX;
            }
            LogManagement.Log_v(TAG, "onOrientationChanged:: posY= " + posY + " posX= " + posX);
    //        LogManagement.Log_v(TAG, "onOrientationChanged::yaw= " + (int)yaw+" newYaw= " + (int)newYaw+" posX= "+posX+" deltaX= "+deltaX);
    //        LogManagement.Log_v(TAG, "onOrientationChanged::pitch= " + (int)pitch+" posY= "+posY+" deltaY= "+deltaY);
            if(mListener!=null){
                mListener.onOrientationChanged(posY, roll, posX);
            }
            isPrecessed=false;
        }

    }
    private boolean isPosMemo= false;
    private int y1,y2;
    private int x1,x2;
}
