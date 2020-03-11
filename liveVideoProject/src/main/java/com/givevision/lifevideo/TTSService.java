package com.givevision.lifevideo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.givevision.sightplus.util.Constants;

import java.util.Locale;
import java.util.Timer;

public class TTSService extends Service implements TextToSpeech.OnInitListener{

	private String str;
	private Timer timer = new Timer();
	private int delay =500; // delay for 0.5 sec.
	private int period = 500; // repeat every 0.5 sec.
	private TextToSpeech mTts;
	private static final String TAG="TTSService";
	private boolean isInit;
 	private Runnable runnable;

	private void sendBroadcastToActivity(String action, String code, boolean result) {
		Intent new_intent = new Intent();
		new_intent.setAction(action);
		new_intent.putExtra(code,result);
		sendBroadcast(new_intent);
		LogManagement.Log_v(TAG, "myTTSService:: Broadcast sent to Activity");
	}
	private void sendBroadcastToActivity(String action, String code, int result) {
		Intent new_intent = new Intent();
		new_intent.setAction(action);
		new_intent.putExtra(code,result);
		sendBroadcast(new_intent);
		LogManagement.Log_v(TAG, "myTTSService:: Broadcast sent to Activity");
	}

	@Override
	
	public IBinder onBind(Intent arg0) {
	
	    return null;
	}
	
	
	@Override
	public void onCreate() {
	    super.onCreate();
	    mTts = new TextToSpeech(getApplicationContext(),this);
	    mTts.setSpeechRate(1.0f);
	    Log.v(TAG, "myTTSService oncreate_service");
	    str ="";
		runnable = new Runnable() {
			@Override
			public void run() {
				while(mTts.isSpeaking()){
					SystemClock.sleep(500);
				}
				sendBroadcastToActivity(Constants.BROADCAST_TTS_ACTION, Constants.TTS_STARTED, false);
			}
		};
		sendBroadcastToActivity(Constants.BROADCAST_TTS_ACTION, Constants.TTS_STARTED, false);
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	    // TODO Auto-generated method stub
		Log.v(TAG, "myTTSService onDestroy");
		timer.cancel();
	     if (mTts != null) {
	            mTts.stop();
	            mTts.shutdown();
	     }

	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "myTTSService onstart_service");
		if(intent!=null && intent.hasExtra("str")&&isInit){
			String str = intent.getStringExtra("str");
			sendBroadcastToActivity(Constants.BROADCAST_TTS_ACTION, Constants.TTS_STARTED, true);
			if(intent.hasExtra("queue")) {
				sayHello(str, intent.getStringExtra("queue"));
			}else{
				sayHello(str);
			}
			new Thread(runnable).start();
		}else{
			
		}
		return TTSService.START_NOT_STICKY;
	}
	
	@Override
	public void onInit(int status) {
	    Log.v(TAG, "myTTSService oninit");
	     if (status == TextToSpeech.SUCCESS) {
	            int result = mTts.setLanguage(Locale.UK);
	            if (result == TextToSpeech.LANG_MISSING_DATA ||
	                result == TextToSpeech.LANG_NOT_SUPPORTED) {
	                Log.v(TAG, "myTTSService Language is not available.");
	            } else {
	            	isInit = true;
//	                sayHello(str);
	            }
	        } else {
	            Log.e(TAG, "myTTSService Could not initialize TextToSpeech.");
	        }
	}
	public void sayHello(String str) {
		if (mTts != null) {
			Log.v(TAG, "myTTSService  QUEUE_FLUSH sayHello: "+str);
			String utteranceId=this.hashCode() + "";
			mTts.speak(str, TextToSpeech.QUEUE_FLUSH, null,utteranceId);
		}	      
	}
	public void sayHello(String str, String queue) {
		if (mTts != null) {
			String utteranceId=this.hashCode() + "";
			if(queue.contains("QUEUE_ADD")){
                Log.v(TAG, "myTTSService QUEUE_ADD sayHello: "+str);
				mTts.speak(str, TextToSpeech.QUEUE_ADD, null,utteranceId);
			}else{
                Log.v(TAG, "myTTSService QUEUE_FLUSH sayHello: "+str);
				mTts.speak(str, TextToSpeech.QUEUE_FLUSH, null,utteranceId);
			}

		}
	}

}