#extension GL_OES_EGL_image_external : require
#define KERNEL_SIZE 9
precision mediump float;
varying vec2 v_TexCoordinate;
uniform samplerExternalOES sTexture;
uniform float uColorAdjust;
uniform float uKernel[KERNEL_SIZE];
uniform vec2 uTexOffset[KERNEL_SIZE];
uniform float bluePercent;
void main() {
     int i = 0;
     vec4 sum = vec4(0.0);
    if (v_TexCoordinate.x < 0.0 || v_TexCoordinate.x > 1.0 || v_TexCoordinate.y < 0.0 || v_TexCoordinate.y > 1.0 )
    discard;
     vec4 texture = texture2D(sTexture, v_TexCoordinate);
     vec4 finalColor = vec4(0.0);
     for (i = 0; i < KERNEL_SIZE; i++) {
        vec4 texc = texture2D(sTexture, v_TexCoordinate + uTexOffset[i]);
        sum += texc * uKernel[i];
     }
     sum += uColorAdjust;
     sum.r=0.85;
     sum.g=0.85;
     finalColor = texture2D(sTexture, v_TexCoordinate);
     if(sum.b>0.2){ //sum.b>0.7
      finalColor.r=0.0;
      finalColor.g=0.0;
      finalColor.b=0.0;
      finalColor.b=finalColor.b*bluePercent;
    }else{
      finalColor=sum;
    }
    gl_FragColor = finalColor;
}