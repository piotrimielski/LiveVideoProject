  #extension GL_OES_EGL_image_external : require
  precision mediump float;
  varying vec2 v_TexCoordinate;
  uniform samplerExternalOES sTexture;
  uniform float bluePercent;
  void main() {
      if (v_TexCoordinate.x < 0.0 || v_TexCoordinate.x > 1.0 || v_TexCoordinate.y < 0.0 || v_TexCoordinate.y > 1.0 )
        discard;
      vec4 color = texture2D(sTexture, v_TexCoordinate);
      color.b=color.b*0.60;
  	  color.b=color.b*bluePercent;
  	  color.g=color.g*0.81;
  	  color.r=color.r*0.79;
      gl_FragColor = color;
  }