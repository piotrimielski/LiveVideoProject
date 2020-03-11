package com.givevision.lifevideo;

public class LogManagement {
	// logging facilities to enable easy overriding. thanks, Dan!
		//
	public static Boolean D = true;
	
	public static void Log_v(String tag, String message) {
		Log_v(tag, message, null);
	}

	protected static void Log_v(String tag, String message, Throwable e) {
		log("v", tag, message, e);
	}

	public static void Log_d(String tag, String message) {
		Log_d(tag, message, null);
	}

	protected static void Log_d(String tag, String message, Throwable e) {
		log("d", tag, message, e);
	}

	public static void Log_i(String tag, String message) {
		Log_i(tag, message, null);
	}

	protected static void Log_i(String tag, String message, Throwable e) {
		log("i", tag, message, e);
	}

	public static void Log_w(String tag, String message) {
		Log_w(tag, message, null);
	}

	protected static void Log_w(String tag, String message, Throwable e) {
		log("w", tag, message, e);
	}

	public static void Log_e(String tag, String message) {
		Log_e(tag, message, null);
	}

	protected static void Log_e(String tag, String message, Throwable e) {
		log("e", tag, message, e);
	}

	protected static void log(String level, String tag, String message, Throwable e) {
			if (message == null || D==false) {
				return;
			}
			if (level.equalsIgnoreCase("v")) {
				if (e == null)
					android.util.Log.v(tag, message);
				else
					android.util.Log.v(tag, message, e);
			} else if (level.equalsIgnoreCase("d")) {
				if (e == null)
					android.util.Log.d(tag, message);
				else
					android.util.Log.d(tag, message, e);
			} else if (level.equalsIgnoreCase("i")) {
				if (e == null)
					android.util.Log.i(tag, message);
				else
					android.util.Log.i(tag, message, e);
			} else if (level.equalsIgnoreCase("w")) {
				if (e == null)
					android.util.Log.w(tag, message);
				else
					android.util.Log.w(tag, message, e);
			} else {
				if (e == null)
					android.util.Log.e(tag, message);
				else
					android.util.Log.e(tag, message, e);
			}
	}
}
