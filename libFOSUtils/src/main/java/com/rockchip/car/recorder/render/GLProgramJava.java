package com.rockchip.car.recorder.render;

import android.opengl.GLES20;

import com.rockchip.car.recorder.utils.SLog;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * step to use:<br/>
 * 1. new GLProgram()<br/>
 * 2. buildProgram()<br/>
 * 3. buildTextures()<br/>
 * 4. drawFrame()<br/>
 */
public class GLProgramJava implements GLProgram {
    private String TAG = "CAM_GLProgramJava";
    // program id
    private int _program;
    // window position
    public final int mWinPosition;
    // texture id
    private int _textureI;
    private int _textureII;
    private int _textureIII;
    // texture index in gles
    private int _tIindex;
    private int _tIIindex;
    private int _tIIIindex;
    // vertices on screen
    private float[] _vertices;
    // handles
    private int _positionHandle = -1, _coordHandle = -1;
    private int _yhandle = -1, _uhandle = -1, _vhandle = -1, _uvhandle;
    private int _ytid = -1, _utid = -1, _vtid = -1, _uvtid = -1;
    // vertices buffer
    private ByteBuffer _vertice_buffer;
    private ByteBuffer _coord_buffer;
    // video width and height
    private int _video_width = -1;
    private int _video_height = -1;
    // flow control
    private boolean isProgBuilt = false;

    /**
     * position can only be 0~4:<br/>
     * fullscreen => 0<br/>
     * left-top => 1<br/>
     * right-top => 2<br/>
     * left-bottom => 3<br/>
     * right-bottom => 4
     */
    public GLProgramJava(int position) {
        if (position < 0 || position > 4) {
            throw new RuntimeException("Index can only be 0 to 4");
        }
        mWinPosition = position;
        setup(mWinPosition);
    }

    /**
     * prepared for later use
     */
    public void setup(int position) {
        switch (mWinPosition) {
            case 1:
                _vertices = GLProgram.squareVertices1;
                _textureI = GLES20.GL_TEXTURE0;
                _textureII = GLES20.GL_TEXTURE1;
                _textureIII = GLES20.GL_TEXTURE2;
                _tIindex = 0;
                _tIIindex = 1;
                _tIIIindex = 2;
                break;
            case 2:
                _vertices = squareVertices2;
                _textureI = GLES20.GL_TEXTURE3;
                _textureII = GLES20.GL_TEXTURE4;
                _textureIII = GLES20.GL_TEXTURE5;
                _tIindex = 3;
                _tIIindex = 4;
                _tIIIindex = 5;
                break;
            case 3:
                _vertices = squareVertices3;
                _textureI = GLES20.GL_TEXTURE6;
                _textureII = GLES20.GL_TEXTURE7;
                _textureIII = GLES20.GL_TEXTURE8;
                _tIindex = 6;
                _tIIindex = 7;
                _tIIIindex = 8;
                break;
            case 4:
                _vertices = squareVertices4;
                _textureI = GLES20.GL_TEXTURE9;
                _textureII = GLES20.GL_TEXTURE10;
                _textureIII = GLES20.GL_TEXTURE11;
                _tIindex = 9;
                _tIIindex = 10;
                _tIIIindex = 11;
                break;
            case 0:
            default:
                _vertices = squareVertices;
                _textureI = GLES20.GL_TEXTURE0;
                _textureII = GLES20.GL_TEXTURE1;
                _textureIII = GLES20.GL_TEXTURE2;
                _tIindex = 0;
                _tIIindex = 1;
                _tIIIindex = 2;
                break;
        }
    }

    public boolean isProgramBuilt() {
        return isProgBuilt;
    }

    public void SetViewport(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void buildProgram() {
        // TODO createBuffers(_vertices, coordVertices);
        if (_program <= 0) {
            if (Constant.BIND_UV_DATA) {
                _program = createProgram(VERTEX_SHADER, FRAGMENT_BIND_UV_SHADER);
            } else {
                _program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            }
        }
        SLog.d(TAG, "_program = " + _program);

        /*
         * get handle for "vPosition" and "a_texCoord"
         */
        _positionHandle = GLES20.glGetAttribLocation(_program, "vPosition");
        SLog.d(TAG, "_positionHandle = " + _positionHandle);
        checkGlError("glGetAttribLocation vPosition");
        if (_positionHandle == -1) {
            throw new RuntimeException("Could not get attribute location for vPosition");
        }
        _coordHandle = GLES20.glGetAttribLocation(_program, "a_texCoord");
        SLog.d(TAG, "_coordHandle = " + _coordHandle);
        checkGlError("glGetAttribLocation a_texCoord");
        if (_coordHandle == -1) {
            throw new RuntimeException("Could not get attribute location for a_texCoord");
        }

        /*
         * get uniform location for y/u/v, we pass data through these uniforms
         */
        _yhandle = GLES20.glGetUniformLocation(_program, "tex_y");
        SLog.d(TAG, "_yhandle = " + _yhandle);
        checkGlError("glGetUniformLocation tex_y");
        if (_yhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_y");
        }
        if (Constant.BIND_UV_DATA) {
            _uvhandle = GLES20.glGetUniformLocation(_program, "tex_uv");
            SLog.d(TAG, "_uvhandle = " + _uvhandle);
            checkGlError("glGetUniformLocation tex_uv");
            if (_uvhandle == -1) {
                throw new RuntimeException("Could not get uniform location for tex_uv");
            }
        } else {
            _uhandle = GLES20.glGetUniformLocation(_program, "tex_u");
            SLog.d(TAG, "_uhandle = " + _uhandle);
            checkGlError("glGetUniformLocation tex_u");
            if (_uhandle == -1) {
                throw new RuntimeException("Could not get uniform location for tex_u");
            }
            _vhandle = GLES20.glGetUniformLocation(_program, "tex_v");
            SLog.d("_vhandle = " + _vhandle);
            checkGlError("glGetUniformLocation tex_v");
            if (_vhandle == -1) {
                throw new RuntimeException("Could not get uniform location for tex_v");
            }
        }
        isProgBuilt = true;
    }


    @Override
    public boolean buildTextures(byte[] y, byte[] u, byte[] v, byte[] uv, int width, int height, int format) {
        if (y == null || width == 0 || height == 0) {
            SLog.d("texture data is null.");
            return false;
        }

        boolean videoSizeChanged = (width != _video_width || height != _video_height);
        if (videoSizeChanged) {
            _video_width = width;
            _video_height = height;
            SLog.d("buildTextures videoSizeChanged: w=" + _video_width + " h=" + _video_height);
        }

        // building texture for Y data
        if (_ytid < 0 || videoSizeChanged) {
            if (_ytid >= 0) {
                SLog.d(TAG, "glDeleteTextures Y");
                GLES20.glDeleteTextures(1, new int[]{_ytid}, 0);
                checkGlError("glDeleteTextures");
            }
            // GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            checkGlError("glGenTextures");
            _ytid = textures[0];
            SLog.d(TAG, "glGenTextures Y = " + _ytid);
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _ytid);
        checkGlError("glBindTexture");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, _video_width, _video_height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(y));
        checkGlError("glTexImage2D");
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        if (Constant.BIND_UV_DATA) {
            // building texture for UV data
            if (_uvtid < 0 || videoSizeChanged) {
                if (_uvtid >= 0) {
                    SLog.d(TAG, "glDeleteTextures U");
                    GLES20.glDeleteTextures(1, new int[]{_uvtid}, 0);
                    checkGlError("glDeleteTextures");
                }
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                checkGlError("glGenTextures");
                _uvtid = textures[0];
                SLog.d(TAG, "glGenTextures UV = " + _uvtid);
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _uvtid);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, _video_width / 2, _video_height / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(uv));
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        } else {
            // building texture for U data
            if (_utid < 0 || videoSizeChanged) {
                if (_utid >= 0) {
                    SLog.d(TAG, "glDeleteTextures U");
                    GLES20.glDeleteTextures(1, new int[]{_utid}, 0);
                    checkGlError("glDeleteTextures");
                }
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                checkGlError("glGenTextures");
                _utid = textures[0];
                SLog.d(TAG, "glGenTextures U = " + _utid);
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _utid);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, _video_width / 2, _video_height / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(u));
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            // building texture for V data
            if (_vtid < 0 || videoSizeChanged) {
                if (_vtid >= 0) {
                    SLog.d(TAG, "glDeleteTextures V");
                    GLES20.glDeleteTextures(1, new int[]{_vtid}, 0);
                    checkGlError("glDeleteTextures");
                }
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                checkGlError("glGenTextures");
                _vtid = textures[0];
                SLog.d(TAG, "glGenTextures V = " + _vtid);
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _vtid);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, _video_width / 2, _video_height / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(v));
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        return true;
    }

    /**
     * build a set of textures, one for R, one for G, and one for B.
     */
    public boolean buildTextures(Buffer y, Buffer u, Buffer v, Buffer uv, int width, int height, int format) {
        if (y == null || width == 0 || height == 0) {
            SLog.d(TAG, "buildTextures videoSize cannot be set to zero.");
            return false;
        }
        boolean videoSizeChanged = (width != _video_width || height != _video_height);
        if (videoSizeChanged) {
            _video_width = width;
            _video_height = height;
            SLog.d(TAG, "buildTextures videoSizeChanged: w=" + _video_width + " h=" + _video_height);
        }

        // building texture for Y data
        if (_ytid < 0 || videoSizeChanged) {
            if (_ytid >= 0) {
                SLog.d(TAG, "glDeleteTextures Y");
                GLES20.glDeleteTextures(1, new int[]{_ytid}, 0);
                checkGlError("glDeleteTextures");
            }
            // GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            checkGlError("glGenTextures");
            _ytid = textures[0];
            SLog.d(TAG, "glGenTextures Y = " + _ytid);
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _ytid);
        checkGlError("glBindTexture");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, _video_width, _video_height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y);
        checkGlError("glTexImage2D");
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        if (Constant.BIND_UV_DATA) {
            // building texture for UV data
            if (_uvtid < 0 || videoSizeChanged) {
                if (_uvtid >= 0) {
                    SLog.d(TAG, "glDeleteTextures U");
                    GLES20.glDeleteTextures(1, new int[]{_uvtid}, 0);
                    checkGlError("glDeleteTextures");
                }
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                checkGlError("glGenTextures");
                _uvtid = textures[0];
                SLog.d(TAG, "glGenTextures U = " + _uvtid);
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _uvtid);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, _video_width / 2, _video_height / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, uv);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        } else {
            // building texture for U data
            if (_utid < 0 || videoSizeChanged) {
                if (_utid >= 0) {
                    SLog.d(TAG, "glDeleteTextures U");
                    GLES20.glDeleteTextures(1, new int[]{_utid}, 0);
                    checkGlError("glDeleteTextures");
                }
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                checkGlError("glGenTextures");
                _utid = textures[0];
                SLog.d(TAG, "glGenTextures U = " + _utid);
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _utid);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, _video_width / 2, _video_height / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, u);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            // building texture for V data
            if (_vtid < 0 || videoSizeChanged) {
                if (_vtid >= 0) {
                    SLog.d(TAG, "glDeleteTextures V");
                    GLES20.glDeleteTextures(1, new int[]{_vtid}, 0);
                    checkGlError("glDeleteTextures");
                }
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                checkGlError("glGenTextures");
                _vtid = textures[0];
                SLog.d(TAG, "glGenTextures V = " + _vtid);
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _vtid);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, _video_width / 2, _video_height / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, v);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        return true;
    }

    /**
     * render the frame
     * the YUV data will be converted to RGB by shader.
     */
    public void drawFrame() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(_program);
        checkGlError("glUseProgram");

        GLES20.glVertexAttribPointer(_positionHandle, 2, GLES20.GL_FLOAT, false, 8, _vertice_buffer);
        checkGlError("glVertexAttribPointer mPositionHandle");
        GLES20.glEnableVertexAttribArray(_positionHandle);

        GLES20.glVertexAttribPointer(_coordHandle, 2, GLES20.GL_FLOAT, false, 8, _coord_buffer);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(_coordHandle);

        // bind textures
        GLES20.glActiveTexture(_textureI);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _ytid);
        GLES20.glUniform1i(_yhandle, _tIindex);

        if (Constant.BIND_UV_DATA) {
            GLES20.glActiveTexture(_textureII);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _uvtid);
            GLES20.glUniform1i(_uvhandle, _tIIindex);
        } else {
            GLES20.glActiveTexture(_textureII);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _utid);
            GLES20.glUniform1i(_uhandle, _tIIindex);

            GLES20.glActiveTexture(_textureIII);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _vtid);
            GLES20.glUniform1i(_vhandle, _tIIIindex);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFinish();

        GLES20.glDisableVertexAttribArray(_positionHandle);
        GLES20.glDisableVertexAttribArray(_coordHandle);
    }

    /**
     * create program and load shaders, fragment shader is very important.
     */
    public int createProgram(String vertexSource, String fragmentSource) {
        // create shaders
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        // just check
        SLog.d(TAG, "vertexShader = " + vertexShader);
        SLog.d(TAG, "pixelShader = " + pixelShader);

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                SLog.e(TAG, "Could not link program: ");
                SLog.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    /**
     * create shader with given source.
     */
    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                SLog.e(TAG, "Could not compile shader " + shaderType + ":");
                SLog.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * these two buffers are used for holding vertices, screen vertices and texture vertices.
     */
    public void createBuffers(float[] vert) {
        _vertice_buffer = ByteBuffer.allocateDirect(vert.length * 4);
        _vertice_buffer.order(ByteOrder.nativeOrder());
        _vertice_buffer.asFloatBuffer().put(vert);
        _vertice_buffer.position(0);

        if (_coord_buffer == null) {
            _coord_buffer = ByteBuffer.allocateDirect(coordVertices.length * 4);
            _coord_buffer.order(ByteOrder.nativeOrder());
            _coord_buffer.asFloatBuffer().put(coordVertices);
            _coord_buffer.position(0);
        }
    }

    @Override
    public void destroyGL() {
        if (_ytid >= 0) {
            GLES20.glDeleteTextures(1, new int[]{_uvtid}, 0);
        }
        if (_utid >= 0) {
            GLES20.glDeleteTextures(1, new int[]{_uvtid}, 0);
        }
        if (_vtid >= 0) {
            GLES20.glDeleteTextures(1, new int[]{_uvtid}, 0);
        }
        if (_uvtid >= 0) {
            GLES20.glDeleteTextures(1, new int[]{_uvtid}, 0);
        }
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            SLog.e(TAG, "***** " + op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private static final String VERTEX_SHADER = "attribute vec4 vPosition;\n" + "attribute vec2 a_texCoord;\n" + "varying vec2 tc;\n" + "void main() {\n" + "gl_Position = vPosition;\n" + "tc = a_texCoord;\n" + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n" + "uniform sampler2D tex_y;\n" + "uniform sampler2D tex_u;\n" + "uniform sampler2D tex_v;\n" + "varying vec2 tc;\n" + "void main() {\n" + "vec4 c = vec4((texture2D(tex_y, tc).r - 16./255.) * 1.164);\n" + "vec4 U = vec4(texture2D(tex_u, tc).r - 128./255.);\n" + "vec4 V = vec4(texture2D(tex_v, tc).r - 128./255.);\n" + "c += V * vec4(1.596, -0.813, 0, 0);\n" + "c += U * vec4(0, -0.392, 2.017, 0);\n" + "c.a = 1.0;\n" + "gl_FragColor = c;\n" + "}\n";
    private static final String FRAGMENT_BIND_UV_SHADER = "precision mediump float;\n" + "uniform sampler2D tex_y;\n" + "uniform sampler2D tex_uv;\n" + "varying vec2 tc;\n" + "void main() {\n" +
    			/*
    			"vec4 c = vec4((texture2D(tex_y, tc).r - 16./255.) * 1.164);\n" +
    			"vec4 U = vec4(texture2D(tex_uv, tc).r - 128./255.);\n" +
    			"vec4 V = vec4(texture2D(tex_uv, tc).a - 128./255.);\n" +
    			"c += V * vec4(1.596, -0.813, 0, 0);\n" +
    			"c += U * vec4(0, -0.392, 2.017, 0);\n" + 
    			"c.a = 1.0;\n" +
    			*/
            "vec3 yuv;\n" + "vec3 rgb;\n" + "yuv.x = texture2D(tex_y, tc).r;\n" + "yuv.y = texture2D(tex_uv, tc).r - 0.5;\n" + "yuv.z = texture2D(tex_uv, tc).a - 0.5;\n" + "rgb = mat3( 1,       1,         1,\n" + "        0,     -0.344,  1.772,\n" + "        1.402, -0.714,   0) * yuv;\n" + "gl_FragColor = vec4(rgb.z, rgb.y, rgb.x, 1);\n" + "}\n";

}