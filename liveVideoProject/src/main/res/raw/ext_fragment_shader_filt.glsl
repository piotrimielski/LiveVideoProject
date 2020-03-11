#extension GL_OES_EGL_image_external : require
#define KERNEL_SIZE 9
precision mediump float;
uniform float uKernel[KERNEL_SIZE];
uniform vec2 uTexOffset[KERNEL_SIZE];
uniform float uColorAdjust;
uniform samplerExternalOES sTexture;
varying vec2 v_TexCoordinate;
uniform float bluePercent;
void main() {
    int i = 0;
    vec4 sum = vec4(0.0);
    if (v_TexCoordinate.x < 0.0 || v_TexCoordinate.x > 1.0 || v_TexCoordinate.y < 0.0 || v_TexCoordinate.y > 1.0 )
    discard;
    vec4 color = texture2D(sTexture, v_TexCoordinate);
    for (i = 0; i < KERNEL_SIZE; i++) {
        vec4 texc = texture2D(sTexture, v_TexCoordinate + uTexOffset[i]);
        sum += texc * uKernel[i];
    }
    sum += uColorAdjust;
    //float c=sum.r * 0.2126 + sum.g * 0.7152 + sum.b * 0.0722;
    //if (c < 0.5){
    //   c = 0.0;
    //} else {
    //   c = 1.0;
    //}
    //vec4 outColor  = vec4(c, c, c, 1.0)*color;
    //gl_FragColor = outColor;
 	//sum.b=sum.b*bluePercent;
 	sum.b=sum.b*0.60;
    sum.b=sum.b*bluePercent;
    sum.g=sum.g*0.81;
    sum.r=sum.r*0.79;
    gl_FragColor = sum;
}