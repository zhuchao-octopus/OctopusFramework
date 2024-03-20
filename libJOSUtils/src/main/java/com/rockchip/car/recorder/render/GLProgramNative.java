package com.rockchip.car.recorder.render;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.opengl.GLES20;
import android.os.SystemClock;
import android.util.Log;

import com.rockchip.car.recorder.utils.SLog;

/**
 * step to use:<br/>
 * 1. new GLProgram()<br/>
 * 2. buildProgram()<br/>
 * 3. buildTextures()<br/>
 * 4. drawFrame()<br/>
 */
public class GLProgramNative implements GLProgram{
	private static final String TAG = "CAM_GLFrogramNative";
    static {
        System.loadLibrary("glrender");
    }

    // flow control
    private boolean isProgBuilt = false;
    private int mId;
    
    /**
     * position can only be 0~4:<br/>
     * fullscreen => 0<br/>
     * left-top => 1<br/>
     * right-top => 2<br/>
     * left-bottom => 3<br/>
     * right-bottom => 4
     */
    public GLProgramNative(int id) {
        if (id < 0 || id > 1) {
            throw new RuntimeException("Index can only be 0 to 1");
        }
        mId = id;
    }
    
    /**
     * prepared for later use
     */
    public void setup(int position) {
        //this.SetCoordinates(0, 0, 1, 1, 0);
    }

    public void SetViewport(int width, int height) {
    	this.nativeSetViewport(mId, width, height);
    }
    
	@Override
	public void createBuffers(float[] vert) {
		this.SetCoordinates(mId, 0, vert[0], vert[5], vert[2], vert[1]);
	}
	
    public boolean isProgramBuilt() {
        return isProgBuilt;
    }
    
    public void buildProgram() {
    	int ret = nativeGLSetup(mId);
    	if (ret == 0) {
    		isProgBuilt = true;
    	}
    }

	@Override
	public boolean buildTextures(byte[] yuv, byte[] u, byte[] v, byte[] uv,
			int width, int height, int format) {
		if (!isProgBuilt) {
			SLog.d(TAG, "EGL not initialized.");
		}
		
		/*if (yuv == null || width == 0 || height == 0) {
			Utils.LOGD("texture data is null.");
			return false;
		}
		if (yuv.length != (width * height * mRatio)) {
		    Utils.LOGD("data object = " + yuv + ",data length = " + yuv.length
		            + ", w = " + width + ", h = " + height
		            + ", mRatio = " + mRatio
		            + ",(width * height * mRatio) = " + (width * height * mRatio));
		    Utils.LOGD("wrong preview data size!");
		    return false;
		}*/
		if (!SetupTextures(mId, width, height, format)) {
			SLog.d(TAG, "setup textures failed.");
			return false;
		}
		
		if (!UpdateTextures(mId, yuv, width, height)) {
			SLog.d(TAG, "setup textures failed.");
			return false;
		}
		return true;
	}

    public void drawFrame() {
    	if (isProgBuilt) {
			Render(mId);
    	}
    }
    
	@Override
	public void destroyGL() {
		if (isProgBuilt) {
			this.nativeDestroyGL(mId);
			isProgBuilt = false;
		}
	}
    
    public native int nativeGLSetup(int id);
    public native void nativeSetViewport(int id, int width, int height);
    public native int SetCoordinates(int id, int zOrder, float left, float top, float right, float bottom);
    public native int loadShader(int shaderType, String pSource);
    public native int createProgram(String pVertexSource, String pFragmentSource);
    public native void printGLString(String name, int s);
    public native void checkGlError(String op);
	public native int Render(int id);
	public native boolean SetupTextures(int id, int width, int height, int format);
	public native boolean UpdateTextures(int id, byte[] data, int width, int height);
	public native void nativeDestroyGL(int id);
	public static native void nativeRGAyuv2rgba(byte[] input, int[] output, int srcWidth, int srcHeight, int dstWidth, int dstHeight);
}