package com.rockchip.car.recorder.render;

import android.graphics.ImageFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.rockchip.car.recorder.activity.AVideoUI;
import com.rockchip.car.recorder.utils.SLog;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLFrameRenderer implements Renderer {

    private static final String TAG = "CAM_GLFrameRenderer";
    private ISimplePlayer mParentAct;
    private IRawDataCallback mRawDataCallback;
    private GLSurfaceView mTargetSurface;
    private GLProgram prog;
    private int mScreenWidth, mScreenHeight;
    private int mVideoWidth, mVideoHeight;
    private boolean renderState = true;
    public boolean mRenderCreated = false;
    private boolean mRequestRenderDestroy = false;
    private final int mId;
    private int mFormat = GLProgram.FORMAT_NV12;
    private float mRatio = 3 / 2.0f;
    private boolean isRequestRender = true;
    private boolean mReadyToRender = true;
    private boolean mSurfaceReady = true;
    private Object RENDER_LOCK = new Object();
    private AVideoUI.ICallbackRender mCallbackRender;
    private int mTimes = 0;

    public GLFrameRenderer(int id, ISimplePlayer playerCallback, IRawDataCallback dataCallback, GLSurfaceView surface, AVideoUI.ICallbackRender callbackRender) {
        if (Constant.USE_NATIVE_GL) {
            prog = new GLProgramNative(id);
        } else {
            prog = new GLProgramJava(0);
        }
        mParentAct = playerCallback;
        mRawDataCallback = dataCallback;
        mTargetSurface = surface;
        this.mId = id;
        this.mCallbackRender = callbackRender;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        SLog.d(TAG, "GLFrameRenderer " + mId + ":: onSurfaceCreated");
        if (!prog.isProgramBuilt()) {
            prog.buildProgram();
            SLog.d(TAG, "GLFrameRenderer :: buildProgram done");
        }
        mScreenWidth = mTargetSurface.getMeasuredWidth();
        mScreenHeight = mTargetSurface.getMeasuredHeight();
        mRenderCreated = true;
        SLog.d(TAG, "surface size = " + mScreenWidth + "," + mScreenHeight);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        SLog.d(TAG, "GLFrameRenderer " + mId + ":: onSurfaceChanged size = " + width + "," + height);
        mScreenWidth = width;
        mScreenHeight = height;
        if (!prog.isProgramBuilt()) {
            prog.buildProgram();
            SLog.d(TAG, "GLFrameRenderer :: buildProgram done");
            mRenderCreated = true;
        }
        prog.SetViewport(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
    	Log.w("dddd", " onDrawFrame!");
        synchronized (RENDER_LOCK) {
            if (mRequestRenderDestroy) {
                Log.w("CAM_GLFrameRender", mId + " request destroy!");
                mRenderCreated = false;
                mRequestRenderDestroy = false;
                prog.destroyGL();
                return;
            }
            if(!mRenderCreated) {
                Log.w("CAM_GLFrameRender", mId + " wanted onDrawFrame, but render was already destroied");
                if (!prog.isProgramBuilt()) {
                    prog.buildProgram();
                    SLog.d(TAG, "GLFrameRenderer :: buildProgram done");
                    mRenderCreated = true;
                    //prog.SetViewport(mScreenWidth, mScreenHeight);
                }
                //return;
            }
//            if (!mReadyToRender || !mSurfaceReady) {
//                gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//                gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
//                gl.glFlush();
//                return;
//            }
        	if (Constant.SPLIT_RAW_DATA) {
        		byte[] data = mRawDataCallback.fetchYData();
        		renderState = prog.buildTextures(data,
            			mRawDataCallback.fetchUData(),
            			mRawDataCallback.fetchVData(),
            			mRawDataCallback.fetchUVData(),
            			mVideoWidth, mVideoHeight, mFormat);
        		mRawDataCallback.notifyTextureUpdated(data);
        		if (!renderState) {
        			return;
        		}
                prog.drawFrame();
        	} else {
                byte[] data = mRawDataCallback.fetchRawData();
                if (data == null || mVideoWidth == 0 || mVideoHeight == 0) {
                    SLog.d(TAG, "Camera " +  mId + " texture data is null. data:" + (data == null ? null : data.length) + "; mVideoWidth:" + mVideoWidth + "; mVideoHeight:" + mVideoHeight);
                    return;
                }
                if (data.length != (mVideoWidth * mVideoHeight * mRatio)) {
                    SLog.d(TAG, "Camera " + mId + " data object = " + data + ",data length = " + data.length
                            + ", w = " + mVideoWidth + ", h = " + mVideoHeight
                            + ", mRatio = " + mRatio);
                    SLog.d(TAG, "render " + mId + " with wrong preview data size!");
                    data = null;
                    if (mCallbackRender != null) {
                        mCallbackRender.addCallBackBuffer(mId, new byte[(int) (mVideoWidth * mVideoHeight * mRatio)]);
                        SLog.d(TAG, "render " + mId + " with wrong preview data size! addCallBackBuffer");
                    }
                    return;
                }
        		renderState = prog.buildTextures(data,
            			mRawDataCallback.fetchUData(),
            			mRawDataCallback.fetchVData(),
            			mRawDataCallback.fetchUVData(),
            			mVideoWidth, mVideoHeight, mFormat);
        		mRawDataCallback.notifyTextureUpdated(data);
        		if (!renderState) {
        			return;
        		}
                prog.drawFrame();
        	}
        }
        frameRate0();
    }

    public void setFormat(int format) {
        SLog.d(TAG, "setFormat = " + format);
        synchronized (this) {
            if (format == ImageFormat.NV21) {
                mFormat = GLProgram.FORMAT_NV12;
            } else if (format == ImageFormat.YV12) {
                mFormat = GLProgram.FORMAT_YV12;
            } else if (format == ImageFormat.RGB_565) {
                mFormat = GLProgram.FORMAT_RGB;
            }
            mRatio = 3 / 2.0f;
            if (mFormat == GLProgram.FORMAT_RGB)
                mRatio = 3.0f;
        }
    }

    public int getFormat() {
        synchronized (this) {
            if (mFormat == GLProgram.FORMAT_NV12) {
                return ImageFormat.NV21;
            } else if (mFormat == GLProgram.FORMAT_YV12) {
                return ImageFormat.YV12;
            } else if (mFormat == GLProgram.FORMAT_RGB) {
                return ImageFormat.RGB_565;
            }
        }
        return ImageFormat.NV21;
    }

    public void updateSurfaceSize(int w, int h) {
        mScreenWidth = w;
        mScreenHeight = h;
        prog.SetViewport(mScreenWidth, mScreenHeight);
        SLog.d(TAG, "updateSurfaceSize = " + mScreenWidth + "," + mScreenHeight);
    }

    /**
     * this method will be called from native code, it happens when the video is about to play or
     * the video size changes.
     */
    public void update(int w, int h) {
        synchronized (RENDER_LOCK) {
            mSurfaceReady = false;
            SLog.d(TAG, "INIT E");
            SLog.d(TAG, "id, w, h = " + mId + ", " + w + "," + h);
            if (w > 0 && h > 0) {
                if (mScreenWidth > 0 && mScreenHeight > 0) {
                    float f1 = 1f * mScreenHeight / mScreenWidth;
                    float f2 = 1f * h / w;
                    if (f1 == f2) {
                        prog.createBuffers(GLProgram.squareVertices);
                    } else if (f1 < f2) {
                        float widScale = 1.0f;//f1 / f2;
                        prog.createBuffers(new float[] { -widScale, -1.0f, widScale, -1.0f, -widScale, 1.0f, widScale,
                                1.0f, });
                    } else {
                        float heightScale = 1.0f;//f2 / f1;
                        prog.createBuffers(new float[] { -1.0f, -heightScale, 1.0f, -heightScale, -1.0f, heightScale, 1.0f,
                                heightScale, });
                    }
                }
                if (w != mVideoWidth || h != mVideoHeight) {
                    this.mVideoWidth = w;
                    this.mVideoHeight = h;
                }
            }

            if (mParentAct != null)
                mParentAct.onPlayStart();
            SLog.d(TAG, "INIT X");
            mSurfaceReady = true;
        }
    }

    public void requestRender() {
        // request to render
        if (isRequestRender) {
            mTargetSurface.requestRender();
        } else {
            SLog.w(TAG, "SurfaceView " + mId + " is not visibility, ignore requestRender");
        }
    }

    public void isRequestRender(boolean requestRender) {
        synchronized (RENDER_LOCK) {
            this.isRequestRender = requestRender;
        }
    }

    /**
     * this method will be called from native code, it's used for passing yuv data to me.
     */
    public void update(byte[] ydata, byte[] udata, byte[] vdata, byte[] uvdata) {
        mTargetSurface.requestRender();
    }

    /**
     * this method will be called from native code, it's used for passing play state to activity.
     */
    public void updateState(int state) {
        SLog.d(TAG, "updateState E = " + state);
        if (mParentAct != null) {
            mParentAct.onReceiveState(state);
        }
        SLog.d(TAG, "updateState X");
    }

    public void destroy() {
        synchronized (RENDER_LOCK) {
            mRenderCreated = false;
            prog.destroyGL();
        }
    }
    public void requestDestroy() {
        synchronized (RENDER_LOCK) {
            mRequestRenderDestroy = true;
        }
    }
    public long oldTime0;
    public int frameCount0;
    public void frameRate0() {
        double rate;
        if (oldTime0 == 0) {
            oldTime0 = System.currentTimeMillis();
        } else {
            if ((System.currentTimeMillis() - oldTime0) >= 3000) {
                rate = frameCount0 / ((System.currentTimeMillis() - oldTime0)*1.0/1000);
                oldTime0 = System.currentTimeMillis();
                frameCount0 = 0;
//                Utils.LOGD(this + " frame rate=" + rate);
            } else {
                frameCount0++;
            }
        }
    }

    public void isRenderer(boolean render) {
        synchronized (RENDER_LOCK) {
            this.mReadyToRender = render;
        }
    }
}
