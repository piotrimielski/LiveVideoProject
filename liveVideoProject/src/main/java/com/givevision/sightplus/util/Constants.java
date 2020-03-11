package com.givevision.sightplus.util;

public final class Constants {
	public static final String MyPREFERENCES = "GIVEVISION_Pref";
	public static final String PREF_L_KEY = "locked";

	public static final int KEY_UP = 19;
	public static final int KEY_DOWN = 20;
	public static final int KEY_LEFT = 21;
	public static final int KEY_RIGHT = 22;
	public static final int KEY_TRIGGER = 96;
	public static final int KEY_BACK = 97;
	public static final int KEY_POWER1 = 100;
	public static final int KEY_POWER2 = 62;

	public static final int BYTES_PER_FLOAT = 4;
	public static final int KERNEL_SIZE = 9;
	public static float[] MATRIX_SHARPEN = 
    	{  0, -1,  0, 
    	-1,  5, -1,  
    	 0, -1,  0, };
	public static float[] MATRIX__EDGE = 
    	  { -1, -1, -1,
    	-1, 8, -1,
    	-1, -1, -1};

	// Defines a custom Intent action
	public static final String BROADCAST_TTS_ACTION = "TTS service";
	public static final String TTS_STARTED = "started talk";
    
    public static  float MAX_CAM_ZOOM = 4; //6 or 4 for asus
    public static final float MAX_GL_ZOOM = 0.08f;
    public static final boolean TTS = true;
    
	public static final String[] formats=new String[] {
		   "yyyyMMddHHmmss",
		   "yyyy-MM-dd",
		   "yyyy-MM-dd HH:mm",
		   "yyyy-MM-dd HH:mm:ss",
		   "yyyy-MM-dd HH:mm:ss.SSSZ",
		   "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		 };
	
}
