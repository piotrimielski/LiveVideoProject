package com.givevision.sightplus.programs;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.givevision.sightplus.util.Constants;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1fv;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2fv;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class TextureShaderProgram  extends ShaderProgram{
	private static final String TAG = "TextureShaderProgram";
	
	// Uniform locations
	private final int uMatrixLocation;
	private final int uTextureUnitLocation;
	// Attribute locations
	private final int aPositionLocation;
	private final int aPositionScale;
	private final int aTextureCoordinatesLocation;
	private int muKernelLoc;
	private static int thickness=500;
	private static float mColorAdjust=0f;
	private float[] mTexOffset;
	private int progPos;
	private int bluePercentLocation;
	
	public TextureShaderProgram(Context context, int progPos) {
		super(context,progPos);
		this.progPos=progPos;
		// Retrieve uniform locations for the shader program.
		uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
		uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
		// Retrieve attribute locations for the shader program.
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		aPositionScale = glGetAttribLocation(program, A_SCALE);
		aTextureCoordinatesLocation =glGetAttribLocation(program, A_TEXTURE_COORDINATES);
		bluePercentLocation = glGetUniformLocation(program, U_BLUE_PERCENT);
    	muKernelLoc = glGetUniformLocation(program, U_KERNEL);
		Log.d(TAG, "TextureShaderProgram:: muKernelLoc: " +muKernelLoc+ " progPos: " + progPos+ " program: " + program+ " bluePercentLocation: "+bluePercentLocation);

	}
	
	public void setUniforms(float[] matrix, int textureId,float bluePercent) {
		// Pass the matrix into the shader program.
		glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
		if (muKernelLoc < 0) {
	          // no kernel in this one
	          muKernelLoc = -1;
	    } else {     
	      	 if(progPos==2){
	      		thickness=200;
	      		 setTexSize(thickness, thickness);
	      		 mColorAdjust=0f;
	          	 glUniform1fv(muKernelLoc, Constants.KERNEL_SIZE, Constants.MATRIX_SHARPEN, 0);
	             glUniform2fv(glGetUniformLocation(program, U_TEX_OFFSET), Constants.KERNEL_SIZE, mTexOffset, 0);
	             glUniform1f(glGetUniformLocation(program,U_COLOR_ADJUST), mColorAdjust);
		    }else if(progPos==4){
		    	thickness=100;
		      	setTexSize(thickness, thickness);
		      	mColorAdjust=1f;
		        glUniform1fv(muKernelLoc, Constants.KERNEL_SIZE, Constants.MATRIX__EDGE, 0);
		        glUniform2fv(glGetUniformLocation(program, U_TEX_OFFSET), Constants.KERNEL_SIZE, mTexOffset, 0);
		        glUniform1f(glGetUniformLocation(program, U_COLOR_ADJUST), mColorAdjust);
		    }
	    }
		if(bluePercentLocation >= 0){
			GLES20.glUniform1f(bluePercentLocation, bluePercent);
		}		
		
		// Set the active texture unit to texture unit 0.
		glActiveTexture(GL_TEXTURE0);
		// Bind the texture to this unit.
		glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
		// Tell the texture uniform sampler to use this texture in the shader by
		// telling it to read from texture unit 0.
		glUniform1i(uTextureUnitLocation, 0);
	}

	public int getPositionAttributeLocation() {
		return aPositionLocation;
	}
	
	public int getPositionAttributeScale() {
		return aPositionScale;
	}
	
	public int getTextureCoordinatesAttributeLocation() {
		return aTextureCoordinatesLocation;
	}
	
	public void setTexSize(int width, int height) {
        float rw = 1.0f / width;
        float rh = 1.0f / height;

        // Don't need to create a new array here, but it's syntactically convenient.
        mTexOffset = new float[] {
            -rw, -rh,   0f, -rh,    rw, -rh,
            -rw, 0f,    0f, 0f,     rw, 0f,
            -rw, rh,    0f, rh,     rw, rh
        };
        	//
//        	Log.d(TAG, "TextureShaderProgram:: TexSize: " + width + "x" + height + ": " + Arrays.toString(mTexOffset));
    }
}
