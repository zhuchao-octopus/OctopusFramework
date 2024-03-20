package com.rockchip.car.recorder.camera2;

/**
 * Created by Administrator on 2016/7/14.
 */
public class CameraSettings {

    /** */
    public static final int OPEN_CAMERA = 1;
    public static final int RELEASE =     2;
    public static final int RECONNECT =   3;
    public static final int UNLOCK =      4;
    public static final int LOCK =        5;
    // Preview
    public static final int SET_PREVIEW_TEXTURE_ASYNC =        101;
    public static final int START_PREVIEW_ASYNC =              102;
    public static final int STOP_PREVIEW =                     103;
    public static final int SET_PREVIEW_CALLBACK_WITH_BUFFER = 104;
    public static final int ADD_CALLBACK_BUFFER =              105;
    public static final int SET_PREVIEW_DISPLAY_ASYNC =        106;
    public static final int SET_PREVIEW_CALLBACK =             107;
    // Parameters
    public static final int SET_PARAMETERS =     201;
    public static final int GET_PARAMETERS =     202;
    public static final int REFRESH_PARAMETERS = 203;
    // Focus, Zoom
    public static final int AUTO_FOCUS =                   301;
    public static final int CANCEL_AUTO_FOCUS =            302;
    public static final int SET_AUTO_FOCUS_MOVE_CALLBACK = 303;
    public static final int SET_ZOOM_CHANGE_LISTENER =     304;
    // Face detection
    public static final int SET_FACE_DETECTION_LISTENER = 461;
    public static final int START_FACE_DETECTION =        462;
    public static final int STOP_FACE_DETECTION =         463;
    public static final int SET_ERROR_CALLBACK =          464;
    // Presentation
    public static final int ENABLE_SHUTTER_SOUND =    501;
    public static final int SET_DISPLAY_ORIENTATION = 502;


    /**  */
    public static final int MAX_SUPPORT_CAMERAS = 3;
}
