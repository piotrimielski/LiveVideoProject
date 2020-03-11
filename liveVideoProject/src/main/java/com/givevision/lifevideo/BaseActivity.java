package com.givevision.lifevideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.givevision.sightplus.util.Constants;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class BaseActivity extends Activity{

	 private static final String TAG = "BaseActivity";

	 public static boolean isAppWentToBg = false;

	 protected static boolean mainActivityIsOpen;
	 protected float zoom=1.0f;
	 protected int prog=0;
     protected Context ctx;
	 protected SimpleDateFormat sdf;
	 protected long fps;
	 public static SharedPreferences sharedpreferences;

	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		 LogManagement.Log_d(TAG, "BaseActivity:: onCreate started ");
          ctx=this;
		 sdf= new SimpleDateFormat(Constants.formats[4], Locale.UK);
		  if(savedInstanceState != null){
			  mainActivityIsOpen=savedInstanceState.getBoolean("mainActivityIsOpen",false);
			  prog=savedInstanceState.getInt("prog",0);
			  zoom=savedInstanceState.getFloat("zoom",1.0f);
			  LogManagement.Log_d(TAG, "onCreate saved mainActivityIsOpen = "+mainActivityIsOpen+" zoom = "+zoom+" prog = "+prog);
		  }

		 sharedpreferences  = getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);

	     Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler(){ 
		      @Override 
		      public void uncaughtException (Thread thread, Throwable e){ 
		        handleUncaughtException (thread, e);
		      } 
	     });
	  }

	  public void handleUncaughtException (Thread thread, Throwable e){
	    e.printStackTrace(); // not all Android versions will print the stack trace automatically
	    System.exit(1); // kill off the crashed app
	  }


	 @Override
	 protected void onStart() {
		 super.onStart();
		 LogManagement.Log_d(TAG, "BaseActivity:: onStart started ");
		 // Get a sensor manager to listen for shakes
	 }
	private static final int REQUEST_ENABLE_BT = 1;
	 @Override
	 protected void onResume() {
		 super.onResume();
		 LogManagement.Log_d(TAG, "BaseActivity:: onResume started ");

	 }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	 @Override
	 public void onSaveInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can save the view hierarchy state
	     super.onSaveInstanceState(savedInstanceState);
	     // Save the user's current state
	     savedInstanceState.putBoolean("mainActivityIsOpen", mainActivityIsOpen);
	     savedInstanceState.putFloat("zoom", zoom);
	     savedInstanceState.putInt("prog", prog);
	 }
	 
	 @Override
	 protected void onPause(){
		 super.onPause();
		 LogManagement.Log_i(TAG, "BaseActivity:: onPause started");
	 }
	 
	 @Override
	 protected void onStop() {
		 super.onStop();
		 LogManagement.Log_d(TAG, "BaseActivity:: onStop started ");

	 }
	 
	 @Override
	 protected void onDestroy() {
	     super.onDestroy();
	     Log.i(TAG, "BaseActivity:: onDestroy started ");
	 }
	 
	 
	 @Override
	 public void onBackPressed() {
		 super.onBackPressed();
		
	 }

	 @Override
	 public void onWindowFocusChanged(boolean hasFocus) {
		 super.onWindowFocusChanged(hasFocus);
		 
	 }

	protected void startTTSService(String str, String queue) {

		LogManagement.Log_d(TAG, "startTTSService isAppWentToBg= "+isAppWentToBg);
		if(Constants.TTS){
			Intent serviceIntent = new Intent(getBaseContext(), TTSService.class);
			LogManagement.Log_d(TAG, "startService string= "+str);
			serviceIntent.putExtra("str", str);
			if(queue!=null) {
                serviceIntent.putExtra("queue", queue);
            }
			startService(serviceIntent);
		}


	}
	 boolean isSpeaking=false;
	 protected void speak(String myText){
		 LogManagement.Log_d(TAG, "speak : " + myText);
		 if(!isSpeaking){
			 startTTSService(myText,"QUEUE_ADD");
		 }else{
			 startTTSService(myText,null);
		 }
		 
	 }

	protected void upDatePref(String key, String value){
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	protected void upDatePref(String key, int value){
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	protected void upDatePref(String key, float value){
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putFloat(key, value);
		editor.commit();
	}
	protected void upDatePref(String key, boolean value){
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

}