package com.rockchip.car.recorder.service;

/**
 * Created by Administrator on 2016/7/15.
 */
public interface IService {

    public static final String CAMERA_ERROR = "CAMERA_ERROR";
    public static final String CAMERA_IDLE = "CAMERA_IDLE";
    public static final String CAMERA_OPENED = "CAMERA_OPENED";
    public static final String CAMERA_PREVIEWING = "CAMERA_PREVIEWING";
    public static final String CAMERA_RECORDING = "CAMERA_RECORDING";

    public static final int I_CAMERA_ERROR = -1;
    public static final int I_CAMERA_IDLE = 0;
    public static final int I_CAMERA_OPENED = 1;
    public static final int I_CAMERA_PREVIEWING = 2;
    public static final int I_CAMERA_RECORDING = 3;

    public int getNumberOfCameras();

    /**
     * this will open all camera_surfaceview
     *
     * @return
     */
    public boolean[] open();

    /**
     * Open the specified id camera_surfaceview
     *
     * @param id
     * @return
     */
    public boolean open(int id);

    /**
     * this will start preview for all camera_surfaceview
     *
     * @return
     */
    public boolean[] startPreview();

    /**
     * startpreview for the specified id camera_surfaceview
     *
     * @param id
     * @return
     */
    public boolean startPreview(int id);

    public <T> boolean startPreviewDirect(int id, T surface);

    public <T> boolean startRecordingDirect(int id, T texture);

    /**
     * this will stop preview for all camera_surfaceview
     *
     * @return
     */
    public boolean[] stopPreview();

    /**
     * stop preview for the specified id camera_surfaceview
     *
     * @param id
     * @return
     */
    public boolean stopPreview(int id);

    /**
     * this will release for all camera_surfaceview
     *
     * @return
     */
    public boolean[] release();

    /**
     * release camera_surfaceview for the specified id camera_surfaceview
     *
     * @param id
     * @return
     */
    public boolean release(int id);

    /**
     * take Picture for all camera_surfaceview
     */
    public void takePicture();

    /**
     * take Picture for the specified id camera_surfaceview
     *
     * @param id
     */
    public void takePicture(int id);

    public boolean[] initRecorder();

    public boolean initRecorder(int id);

    /**
     * start record for all Camera
     *
     * @return
     */
    public void startRecord();

    /**
     * start record for the specified id camera_surfaceview
     *
     * @param id
     * @return
     */
    public void startRecord(int id);

    public void stopRecord();

    public void stopRecord(int id);

    public boolean isOpended(int id);

    public boolean isPreviewing(int id);

    public boolean isRecording(int id);
}
