package com.givevision.lifevideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.givevision.sightplus.util.AndroidUtils;
import com.givevision.sightplus.util.Constants;
import com.Source;
import com.AvLibDemoRenderer;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    private static final String ARG_NEED_FAKE_CONTROLS = "ARG_NEED_FAKE_CONTROLS";
    private static final String ARG_NEED_STREAM_URL = "ARG_NEED_STREAM_URL";
    private static final String ARG_SOURCE = "ARG_SOURCE";


    private MainView mView;
    private boolean isLocked = false;
    private static final int MAXPROG = 5;

    private RelativeLayout relativeLayout;
    private Context context;
    private SeekBar seekBar1;
    private SeekBar seekBar2;
    private TextView fpsC;
    private TextView fpsS;
    private TextView fpsP;
    private float bluePercent = 1.0f;
    private float blueTxtPercent = 0.0f;

    private boolean fakeControls;
    private Source source;
    private String streamUrl;


    public static void launch(Context context, boolean needControls, String streamUrl, Source source) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_NEED_FAKE_CONTROLS, needControls);
        intent.putExtra(ARG_NEED_STREAM_URL, streamUrl);
        intent.putExtra(ARG_SOURCE, source);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        LogManagement.Log_d(TAG, "MainActivity:: onCreate");

//        fakeControls = getIntent().getBooleanExtra(ARG_NEED_FAKE_CONTROLS, false);
//        streamUrl = getIntent().getStringExtra(ARG_NEED_STREAM_URL);
//        source = (Source) getIntent().getSerializableExtra(ARG_SOURCE);

        source = Source.NETWORK_STREAM;
        fakeControls = false;
        streamUrl = "";

        screenGL();
        setContentView(R.layout.activity_main);

        intGL();

        speak(".");
    }

    private void intGL() {

        LogManagement.Log_d(TAG, "MainActivity:: intGL");
        relativeLayout = (RelativeLayout) findViewById(R.id.surfaceContainer);
        relativeLayout.setId(AndroidUtils.generateViewId());
        relativeLayout.setKeepScreenOn(true);
        relativeLayout.setBackgroundColor(Color.DKGRAY);
        ; //or whatever your image is


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        mView = new MainView(this, this, source, AvLibDemoRenderer.createArguments(streamUrl));

        mView.setId(AndroidUtils.generateViewId());
//		mView.setKeepScreenOn(true);
        relativeLayout.addView(mView);

        seekBar1 = new SeekBar(this);
        seekBar1.setId(AndroidUtils.generateViewId());
        seekBar1.setMax(150);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(width / 3, 50);
        params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params1.setMargins(180, 100, 0, 0);
        seekBar1.setLayoutParams(params1);
        seekBar1.setProgress(0);

        relativeLayout.addView(seekBar1);

        seekBar2 = new SeekBar(this);
        seekBar2.setId(AndroidUtils.generateViewId());
        seekBar2.setMax(150);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(width / 3, 50);
        params2.addRule(RelativeLayout.RIGHT_OF, seekBar1.getId());
        params2.setMargins(380, 100, 0, 0);
        seekBar2.setLayoutParams(params2);
        seekBar2.setProgress(0);

        relativeLayout.addView(seekBar2);

        fpsC = new TextView(this);
        fpsC.setTextSize(14);
        fpsC.setPadding(20, 0, 0, 0);
        relativeLayout.addView(fpsC);

        fpsP = new TextView(this);
        fpsP.setTextSize(14);
        fpsP.setPadding(350, 0, 0, 0);
        relativeLayout.addView(fpsP);

        fpsS = new TextView(this);
        fpsS.setTextSize(14);
        fpsS.setPadding(700, 0, 0, 0);
        relativeLayout.addView(fpsS);

        setFPS(0, 0, 0);

        initControls();
    }

    private void initControls() {

        if (fakeControls) {
            findViewById(R.id.controls).setVisibility(View.VISIBLE);
            findViewById(R.id.up).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, Constants.KEY_UP)));
            findViewById(R.id.down).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, Constants.KEY_DOWN)));
            findViewById(R.id.start).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, Constants.KEY_TRIGGER)));
            findViewById(R.id.left).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, Constants.KEY_LEFT)));
            findViewById(R.id.right).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, Constants.KEY_RIGHT)));
        } else {
            findViewById(R.id.controls).setVisibility(View.GONE);
        }

    }


    public int barZoomValue() {
        if (zoomSaved == 0) {
//            if (zoom <= Constants.MAX_CAM_ZOOM) {
//                return (int) (60 * (zoom - 1) / Constants.MAX_CAM_ZOOM);
//            } else {
//                return (int) (60 + (90 * (zoom - Constants.MAX_CAM_ZOOM) / Constants.MAX_GL_ZOOM));
//            }
            return (int) ((150 * (zoom - Constants.MAX_CAM_ZOOM) / Constants.MAX_GL_ZOOM));
        } else {
//            if (zoomSaved <= Constants.MAX_CAM_ZOOM) {
//                return (int) (60 * (zoomSaved - 1) / Constants.MAX_CAM_ZOOM);
//            } else {
//                return (int) (60 + (90 * (zoomSaved - Constants.MAX_CAM_ZOOM) / Constants.MAX_GL_ZOOM));
//            }
            return (int) ((150 * (zoomSaved - Constants.MAX_CAM_ZOOM) / Constants.MAX_GL_ZOOM));
        }
    }


    public void setFPS(long fC, long fP, long fS) {
        fpsC.setText("FPS C: " + fC);
        fpsP.setText("FPS P: " + fP);
        fpsS.setText("FPS S: " + fS);
    }

    private void startGL() {
        LogManagement.Log_d(TAG, "MainActivity:: startGL");
        resumeGL();
    }

    private boolean isResumed = false;

    private void resumeGL() {
        LogManagement.Log_d(TAG, "MainActivity:: resumeGL");
        mView.onResume();
        isResumed = true;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void screenGL() {
        LogManagement.Log_d(TAG, "MainActivity:: screenGL");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        int ui = getWindow().getDecorView().getSystemUiVisibility();
        ui = ui | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        final Window win = getWindow();
        win.getDecorView().setSystemUiVisibility(ui);
        win.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        win.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        win.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        );
    }

    private void pauseGL() {
        LogManagement.Log_d(TAG, "MainActivity:: pauseGL");
        mView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogManagement.Log_d(TAG, "MainActivity:: onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogManagement.Log_d(TAG, "MainActivity:: onResume");
        startGL();
        speak("zoom test on");
    }


    @Override
    protected void onPause() {
        super.onPause();
        LogManagement.Log_d(TAG, "MainActivity:: onPause");
        upDatePref(Constants.PREF_L_KEY, false);
        pauseGL();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogManagement.Log_d(TAG, "MainActivity:: onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
        LogManagement.Log_d(TAG, "MainActivity:: onDestroy");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogManagement.Log_v(TAG, "MainActivity:: dispatchKeyEvent event.getKeyCode() " + event.getKeyCode() +
                " event.getAction():" + event.getAction());
        return mainDispatchKeyEvent(event);

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogManagement.Log_v(TAG, "MainActivity:: onKeyDown event.getKeyCode() " + event.getKeyCode());
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            // this is method which detect press even of button
            event.startTracking(); // Needed to track long presses
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        LogManagement.Log_v(TAG, "MainActivity:: onKeyLongPress event.getKeyCode() " + event.getKeyCode());
//        if (keyCode == KeyEvent.KEYCODE_POWER) {
//            // Here we can detect long press of power button
//            return true;
//        }
        return super.onKeyLongPress(keyCode, event);
    }

    private boolean mainDispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == Constants.KEY_TRIGGER) { //START - TRIGGER
            magModePrChoice(event);
            return true;
        } else if (event.getKeyCode() == Constants.KEY_BACK) { //BACK
            return true;
        } else if (event.getKeyCode() == Constants.KEY_UP) { //UP
            magModeSetZoom(event, +1.0f);
            return true;
        } else if (event.getKeyCode() == Constants.KEY_DOWN) {//DOWN
            magModeSetZoom(event, -1.0f);
            return true;
        } else if (event.getKeyCode() == Constants.KEY_LEFT) { //LEFT
            return true;
        } else if (event.getKeyCode() == Constants.KEY_RIGHT) { //RIGHT
            lockScreen();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    private boolean isWaiting = false;

    private void lockScreen() {
//		LogManagement.Log_v(TAG, "lockScreen isLocked= "+isLocked);
        if (!isWaiting) {
            if (!isLocked) {
                isLocked = true;
                str = "screen locked";
                speak(str);
                zoomSaved = Constants.MAX_CAM_ZOOM;
                upDatePref(Constants.PREF_L_KEY, true);
                setZoom(zoomSaved);
                mView.setPause(true);
            } else {
                isLocked = false;
//			clearPosX();
                str = "screen unlocked";
                speak(str);
                upDatePref(Constants.PREF_L_KEY, false);
//                zoomSaved = 0;
                zoom = Constants.MAX_CAM_ZOOM;
                setZoom(zoom);
                mView.setPause(false);
            }
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isWaiting = false;
                }
            }, 1000);
        }
        isWaiting = true;

    }

    private Handler handlerZoom;

    public void setZoom(float z) {
        mView.setZoom(z);
        seekBar1.setProgress(barZoomValue());
        seekBar2.setProgress(barZoomValue());
        if (handlerZoom == null) {
            handlerZoom = new Handler();
            handlerZoom.postDelayed(new Runnable() {
                public void run() {
                    LogManagement.Log_v(TAG, "zoom sent= " + zoom);
                    int zoom = barZoomValue();
                    handlerZoom = null;
                }
            }, 5000);
        }
    }

    private String str;
    private Handler handlerProg;

    public void setProgram(int p) {
        switch (p) {
            case 0:
                str = context.getString(R.string.main_normal);
                speak(str);
                break;
            case 1:
                str = context.getString(R.string.main_enhanced);
                speak(str);
                break;
            case 2:
                str = context.getString(R.string.main_contrast);
                speak(str);
                break;
            case 3:
                str = context.getString(R.string.main_inverted);
                speak(str);
                break;
            case 4:
                str = context.getString(R.string.main_text);
                speak(str);
                break;
            default:
                break;
        }
        mView.setBluePercent(bluePercent);
        mView.setBlueTxtPercent(blueTxtPercent);
        mView.setProgram(p);
        if (handlerProg == null) {
            handlerProg = new Handler();
            handlerProg.postDelayed(new Runnable() {
                public void run() {
                    handlerProg = null;
                }
            }, 1000);
        }
    }

    private void magModePrChoice(KeyEvent event) {
        Log.d(TAG, "MainActivity magModePrChoice KeyCode= " + event.getKeyCode());
        if (event.getAction() == KeyEvent.ACTION_UP) {
            prog++;
            if (prog >= MAXPROG)
                prog = 0;
            setProgram(prog);
        }
    }

    private float zoomSaved = 0f;

    private void magModeSetZoom(KeyEvent event, float direction) {
//        if (mView.isFrameStoped()) {
            if (zoomSaved < Constants.MAX_CAM_ZOOM) {
                zoomSaved = Constants.MAX_CAM_ZOOM;
            }
            if (direction > 0) {
                zoomSaved = zoomSaved + (direction * 0.001f);
                if (zoomSaved > Constants.MAX_CAM_ZOOM + Constants.MAX_GL_ZOOM) {
                    zoomSaved = Constants.MAX_CAM_ZOOM + Constants.MAX_GL_ZOOM;
                }
            } else {
                if (zoomSaved > Constants.MAX_CAM_ZOOM) {
                    zoomSaved = zoomSaved + (direction * 0.001f);
                } else {
                    zoomSaved = Constants.MAX_CAM_ZOOM;
                }
            }

            setZoom(zoomSaved);
//        } else {
//            if (direction > 0) {
//                if (zoom >= Constants.MAX_CAM_ZOOM) {
//                    zoom = zoom + (direction * 0.001f);
//                } else {
//                    zoom = zoom + (direction * 0.05f);
//                    if (zoom > Constants.MAX_CAM_ZOOM) {
//                        zoom = Constants.MAX_CAM_ZOOM + (direction * 0.001f);
//                    }
//                }
//                if (zoom > Constants.MAX_CAM_ZOOM + Constants.MAX_GL_ZOOM) {
//                    zoom = Constants.MAX_CAM_ZOOM + Constants.MAX_GL_ZOOM;
//                }
//            } else {
//                if (zoom > Constants.MAX_CAM_ZOOM) {
//                    zoom = zoom + (direction * 0.001f);
//                } else {
//                    zoom = zoom + (direction * 0.05f);
//                }
//                if (zoom < 1) {
//                    zoom = 1.0f;
//                }
//            }
//            setZoom(zoom);
//            zoomSaved = zoom;
//        }


        LogManagement.Log_v(TAG, " MainActivity:: zoom= " + zoom + " zoomSaved= " + zoomSaved);
    }
}


