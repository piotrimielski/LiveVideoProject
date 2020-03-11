package com.givevision.sightplus.objects;

import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glFlush;

import com.givevision.sightplus.util.Constants;
import com.givevision.sightplus.data.VertexArray;
import com.givevision.sightplus.programs.TextureShaderProgram;


public class RightScreen {
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
	private static final int STRIDE = (POSITION_COMPONENT_COUNT	+ TEXTURE_COORDINATES_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;
		
	private static final float[] VERTEX_DATA = { 
		0.0f, -0.65f, 
		-1.0f, -0.65f, 
		0.0f, 0.65f, 
		-1.0f, 0.65f 
	};

	private static final float[] TEXTURE_DATA = { 
		1.0f, 1.0f, 
		0.0f, 1.0f, 
		1.0f, 0.0f, 
		0.0f, 0.0f 
	};
	
	private final VertexArray vertexArray;
	private VertexArray textureArray;
	
	public RightScreen() {
		vertexArray = new VertexArray(VERTEX_DATA);
		textureArray = new VertexArray(TEXTURE_DATA);
	}
	
	public void bindData(TextureShaderProgram textureProgram,float mZoom,float x,float y) {
		vertexArray.setVertexAttribPointer(0,textureProgram.getPositionAttributeLocation(),POSITION_COMPONENT_COUNT,0);
		textureArray = new VertexArray( setTextureArray(mZoom,x,y));
		textureArray.setVertexAttribPointer(0,textureProgram.getTextureCoordinatesAttributeLocation(),TEXTURE_COORDINATES_COMPONENT_COUNT,0);
	}
	
	public void draw() {
//		glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		glFlush();
	}
	private float[] setTextureArray(float mZoom,float x,float y){
//		float a=(float)((0.15*mZoom)/Constants.MAX_GL_ZOOM);
		float a=mZoom*5f;
		//Log.e("RightScreen", "screen:: mZoom "+mZoom + " a "+a+" x "+ x+ " y "+y);
	
		float[] mScaleMatrix = {
				(1.0f-a)+x, (1.0f-a)+y, 
				a+x, (1.0f-a)+y, 
				(1.0f-a)+x, a+y, 
				a+x, a+y
	    };
		return mScaleMatrix;
	}
	
	
}

