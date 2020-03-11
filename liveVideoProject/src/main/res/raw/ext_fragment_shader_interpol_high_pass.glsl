#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 v_TexCoordinate;
uniform samplerExternalOES sTexture;
uniform float bluePercent;

vec4 filter9(vec4 c1, vec4 c2, vec4 c3, vec4 c4, vec4 c5, vec4 c6, vec4 c7, vec4 c8, vec4 c9, mat3 kernel, float div) {
    return (c1 * kernel[0][0] +
            c2 * kernel[0][1] +
            c3 * kernel[0][2] +
            c4 * kernel[1][0] +
            c5 * kernel[1][1] +
            c6 * kernel[1][2] +
            c7 * kernel[2][0] +
            c8 * kernel[2][1] +
            c9 * kernel[2][2]) / div;
}

vec4 HighPass(vec4 c1, vec4 c2, vec4 c3, vec4 c4, vec4 c5, vec4 c6, vec4 c7, vec4 c8, vec4 c9) {
    mat3 kernel = mat3(-0.111, -0.111, -0.111,
                       -0.111,  2.0, -0.111,
                       -0.111, -0.111, -0.111);
    return filter9(c1, c2, c3, c4, c5, c6, c7, c8, c9, kernel, 1.11);
}

vec4 color(samplerExternalOES texture, vec2 pos) {
    if(pos.x < 0.0 || pos.y < 0.0 || pos.x > 1.0 || pos.y > 1.0) {
        return vec4(0.0, 0.0, 0.0, 0.0); // black
    } else {

        vec4 Ccolour = texture2D(texture, pos);
            Ccolour.b=Ccolour.b*0.60;
            Ccolour.b=Ccolour.b*bluePercent;
            Ccolour.g=Ccolour.g*0.81;
            Ccolour.r=Ccolour.r*0.79;
        return Ccolour;
    }
}


void main() {
    if (v_TexCoordinate.x < 0.0 || v_TexCoordinate.x > 1.0 || v_TexCoordinate.y < 0.0 || v_TexCoordinate.y > 1.0 )
    discard;
    float px = 1.0/60.0; //Pixel offset for interpolation, tweakable.

    // v0 v1 v2
    // v3 v4 v5
    // v6 v7 v8

    //Interpolation
    vec2 v0 = vec2(v_TexCoordinate.x - px/2.0, v_TexCoordinate.y + px/2.0);
    vec2 v1 = vec2(v_TexCoordinate.x, v_TexCoordinate.y + px/2.0);
    vec2 v2 = vec2(v_TexCoordinate.x + px/2.0, v_TexCoordinate.y + px/2.0);
    vec2 v3 = vec2(v_TexCoordinate.x - px/2.0, v_TexCoordinate.y);
    vec2 v4 = v_TexCoordinate;
    vec2 v5 = vec2(v_TexCoordinate.x + px/2.0, v_TexCoordinate.y);
    vec2 v6 = vec2(v_TexCoordinate.x - px/2.0, v_TexCoordinate.y - px/2.0);
    vec2 v7 = vec2(v_TexCoordinate.x, v_TexCoordinate.y - px/2.0);
    vec2 v8 = vec2(v_TexCoordinate.x + px/2.0, v_TexCoordinate.y - px/2.0);

    vec4 c0 = color(sTexture, v0);
    vec4 c1 = color(sTexture, v1);
    vec4 c2 = color(sTexture, v2);
    vec4 c3 = color(sTexture, v3);
    vec4 c4 = color(sTexture, v4);
    vec4 c5 = color(sTexture, v5);
    vec4 c6 = color(sTexture, v6);
    vec4 c7 = color(sTexture, v7);
    vec4 c8 = color(sTexture, v8);

    vec4 cx = c4;

    cx = HighPass(c0, c1, c2, c3, c4, c5, c6, c7, c8);

    gl_FragColor = cx;
}