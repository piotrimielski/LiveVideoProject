package com.givevision.sightplus.programs;

import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glUseProgram;

import com.givevision.lifevideo.R;
import com.givevision.sightplus.util.ShaderHelper;
import com.givevision.sightplus.util.TextResourceReader;

import android.content.Context;

public class ShaderProgram {
	// Uniform constants
	protected static final String U_MATRIX = "u_MVPMatrix";
	protected static final String U_TEXTURE_UNIT = "sTexture";
	protected static final String U_KERNEL =  "uKernel";
	protected static final String U_TEX_OFFSET =  "uTexOffset";
	protected static final String U_COLOR_ADJUST =  "uColorAdjust";
	protected static final String U_BLUE_PERCENT =  "bluePercent";
	// Attribute constants
	protected static final String A_POSITION = "a_Position";
	protected static final String A_SCALE = "a_scale";
	protected static final String A_TEXTURE_COORDINATES = "a_TexCoordinate";
	// Shader program
	protected final int program;
	protected ShaderProgram(Context context, int progPos) {
		// Compile the shaders and link the program.
		int vertexShaderResourceId=R.raw.vertex_shader;
		int fragmentShaderResourceId=R.raw.ext_fragment_shader;
		switch (progPos) {
			case 0:
				vertexShaderResourceId=R.raw.vertex_shader;
				fragmentShaderResourceId=R.raw.ext_fragment_shader;
	            break;
			case 1:
				vertexShaderResourceId=R.raw.vertex_shader;
//				fragmentShaderResourceId=R.raw.ext_fragment_shader_interpol_high_pass;
				fragmentShaderResourceId=R.raw.ext_fragment_shader;
				break;
			case 2:
				vertexShaderResourceId=R.raw.vertex_shader;
				fragmentShaderResourceId=R.raw.ext_fragment_shader_filt;
				break;
			case 3:
				vertexShaderResourceId=R.raw.vertex_shader;
				fragmentShaderResourceId=R.raw.ext_fragment_shader_bw_inv;
	            break;
			case 4:
				vertexShaderResourceId=R.raw.vertex_shader;
				fragmentShaderResourceId=R.raw.ext_fragment_shader_filt_edge_inv;
	            break;  
	//		case 3:
	//			hProgram=loadShader ( vss_default, fss_filtre_edge);
	//            break;  
	//		case 4:
	//			hProgram=loadShader ( vss_default, fss_filtre_edge_inv);
	//            break;  
			default:
	    }
		program = ShaderHelper.buildProgram(
				TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
				TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId)
		);
	}
	public void useProgram() {
		// Set the current OpenGL shader program to this program.
		glUseProgram(program);
	}
	public void deleteProgram() {
		// delete the current OpenGL shader program to this program.
		glDeleteProgram(program);
	}
}
