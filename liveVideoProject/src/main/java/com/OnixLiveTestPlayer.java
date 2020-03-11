package com;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Surface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class OnixLiveTestPlayer {

    private Configurator configurator = new Configurator();
    private IjkMediaPlayer player;
    private Context context;

    public OnixLiveTestPlayer(Context context) {
        player = createPlayer(context);
        this.context = context;
    }

    public void startStream(SurfaceTexture texture) {
        try {
            Uri uri = Uri.parse(configurator.getString("url", ""));
            Surface surface = new Surface(texture);
            player.setOnPreparedListener(mp -> player.start(context));
            player.setOnErrorListener((mp, what, extra) -> false);
            player.setDataSource(context, uri);
            player.setSurface(surface);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setScreenOnWhilePlaying(true);
            player.prepareAsync();
            player.start(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopStream() {
        player.stop();
    }

    private IjkMediaPlayer createPlayer(Context context) {


        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_WARN);


        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-all-videos", configurator.getInt("mediacodec-all-videos", 1));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-mpeg2", configurator.getInt("mediacodec-mpeg2", 1));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-mpeg4", configurator.getInt("mediacodec-mpeg4", 1));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", configurator.getInt("mediacodec-hevc", 1));

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", configurator.getInt("mediacodec", 1));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", configurator.getInt("mediacodec-auto-rotate", 0));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", configurator.getInt("mediacodec-handle-resolution-change", 0));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", configurator.getInt("opensles", 1));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", configurator.getInt("framedrop", 1));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync", configurator.getString("sync", "audio"));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", configurator.getInt("start-on-prepared", 0));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", configurator.getInt("http-detect-range-support", 0));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", configurator.getInt("skip_loop_filter", 48));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", configurator.getInt("analyzeduration", 2000000));
        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "f", configurator.getString("f", "mpegts"));


        String flags = configurator.getString("fflags", "nobuffer");
        if (!TextUtils.isEmpty(flags)) {
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", configurator.getString("fflags", flags));
        }

        int probeSize = configurator.getInt("probsize", 8192);
        if (probeSize > 0)
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probsize", probeSize);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", configurator.getInt("packet-buffering", 0));

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", configurator.getInt("reconnect", 1));
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", configurator.getInt("dns_cache_clear", 0));

        ijkMediaPlayer._setMed(342, context);
        //ijkMediaPlayer.setVolume(0, 0);
        return ijkMediaPlayer;
    }

    private class Configurator {

        private static final String CONFIGURATION_PATH = "givevision/givevision.properties";
        private Properties properties = new Properties();

        public Configurator() {
            try {
                properties.load(new FileInputStream(new File(Environment.getExternalStorageDirectory(), CONFIGURATION_PATH)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int getInt(String key, int def) {
            String value = properties.getProperty(key, "");
            if (value.isEmpty())
                return def;
            else
                return Integer.valueOf(value);
        }

        public String getString(String key, String def) {
            return properties.getProperty(key, def);
        }
    }

}
