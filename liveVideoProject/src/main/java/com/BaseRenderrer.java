package com;

import android.opengl.GLSurfaceView;

public abstract class BaseRenderrer implements GLSurfaceView.Renderer {

    public abstract void onPause();

    public abstract void onResume();

    public abstract void setBluePercent(float bluePercent);

    public abstract void setBlueTxtPercent(float blueTxtPercent);

    public abstract boolean setZoomValue(float zoom);

    public abstract void setProgram(int p);

    public abstract boolean isFrameStoped();

    public abstract void setPause(boolean p);
}
