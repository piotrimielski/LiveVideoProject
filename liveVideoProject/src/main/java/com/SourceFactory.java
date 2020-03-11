package com;

import android.os.Bundle;
import android.view.SurfaceHolder;

import com.givevision.lifevideo.MainActivity;
import com.givevision.lifevideo.MainRenderer;
import com.givevision.lifevideo.MainView;

public class SourceFactory {
    public static BaseRenderrer createRenderrer(MainView view, MainActivity activity, SurfaceHolder ourHolder, Source source, Bundle args) {
        if (source == Source.NETWORK_STREAM) {
            return new AvLibDemoRenderer(view, activity, ourHolder, args);
        }

        return new MainRenderer(view, activity, ourHolder);
    }
}
