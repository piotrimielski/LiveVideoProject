package com.givevision.lifevideo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;

import com.BaseRenderrer;
import com.Source;
import com.SourceFactory;

public class MainView extends GLSurfaceView {
    BaseRenderrer mRenderer;

    MainView(Context context, MainActivity act, Source source, Bundle args) {
        super(context);
        SurfaceHolder ourHolder = getHolder();

        mRenderer = SourceFactory.createRenderrer(this, act, ourHolder, source, args);

        setEGLContextClientVersion(2);
        setRenderer(mRenderer);

//        setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
    }

    @Override
    public void onPause() {
        mRenderer.onPause();
        super.onPause();
    }

    public void setBluePercent(float bluePercent) {
        mRenderer.setBluePercent(bluePercent);
    }

    public void setBlueTxtPercent(float blueTxtPercent) {
        mRenderer.setBlueTxtPercent(blueTxtPercent);
    }

    public void setZoom(float zoom) {
        mRenderer.setZoomValue(zoom);
    }

    public void setProgram(int p) {
        mRenderer.setProgram(p);
    }

    public boolean isFrameStoped() {
        return mRenderer.isFrameStoped();
    }

    public void setPause(boolean p) {
        mRenderer.setPause(p);
    }
}
