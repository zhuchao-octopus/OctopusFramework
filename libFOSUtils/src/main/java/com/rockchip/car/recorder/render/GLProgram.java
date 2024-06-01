package com.rockchip.car.recorder.render;

public interface GLProgram {
    static final int FORMAT_NV12 = 0;
    static final int FORMAT_RGB = 1;
    static final int FORMAT_YV12 = 2;

    static float[] squareVertices = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f,}; // fullscreen

    static float[] squareVertices1 = {-1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f,}; // left-top

    static float[] squareVertices2 = {0.0f, -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,}; // right-bottom

    static float[] squareVertices3 = {-1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,}; // left-bottom

    static float[] squareVertices4 = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,}; // right-top

    static float[] coordVertices = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,}; // whole-texture

    public void SetViewport(int width, int height);

    public boolean isProgramBuilt();

    public void buildProgram();

    public boolean buildTextures(byte[] yuv, byte[] u, byte[] v, byte[] uv, int width, int height, int format);

    public void drawFrame();

    public void createBuffers(float[] vert);

    public void destroyGL();
}
