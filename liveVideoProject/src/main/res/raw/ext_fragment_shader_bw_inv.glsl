 #extension GL_OES_EGL_image_external : require
 precision mediump float;
 varying vec2 v_TexCoordinate;
 uniform samplerExternalOES sTexture;
 uniform float bluePercent;
 void main() {
     if (v_TexCoordinate.x < 0.0 || v_TexCoordinate.x > 1.0 || v_TexCoordinate.y < 0.0 || v_TexCoordinate.y > 1.0 )
     discard;
     vec4 tc = texture2D(sTexture, v_TexCoordinate);
     float color = tc.r * 0.2126 + tc.g * 0.7152 + tc.b * 0.0722;

     vec4 outColor  = vec4(color, color, color, 1.0);
     outColor = vec4(1.0, 1.0, 1.0, 1.0) - outColor;
     outColor.b=outColor.b*0.60;
     outColor.b=outColor.b*bluePercent;
     gl_FragColor = outColor;
  }