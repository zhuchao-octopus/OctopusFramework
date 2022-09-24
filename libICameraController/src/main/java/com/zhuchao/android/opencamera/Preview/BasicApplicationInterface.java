package com.zhuchao.android.opencamera.Preview;

import android.location.Location;

import com.zhuchao.android.opencamera.control.CameraController;

/**
 * A partial implementation of ApplicationInterface that provides "default" implementations. So
 * sub-classing this is easier than implementing ApplicationInterface directly - you only have to
 * provide the unimplemented methods to get started, and can later override
 * BasicApplicationInterface's methods as required.
 * Note there is no need for your subclass of BasicApplicationInterface to call "super" methods -
 * these are just default implementations that should be overridden as required.
 */
public abstract class BasicApplicationInterface implements ApplicationInterface {
    public Location getLocation() {
        return null;
    }

    public int getCameraIdPref() {
        return 0;
    }

    public String getFlashPref() {
        return "flash_off";
    }

    public String getFocusPref(boolean is_video) {
        return "focus_mode_continuous_picture";
    }

    public boolean isVideoPref() {
        return false;
    }

    public String getSceneModePref() {
        return CameraController.SCENE_MODE_DEFAULT;
    }

    public String getColorEffectPref() {
        return CameraController.COLOR_EFFECT_DEFAULT;
    }

    public String getWhiteBalancePref() {
        return CameraController.WHITE_BALANCE_DEFAULT;
    }

    public int getWhiteBalanceTemperaturePref() {
        return 0;
    }

    public String getAntiBandingPref() {
        return CameraController.ANTIBANDING_DEFAULT;
    }

    public String getEdgeModePref() {
        return CameraController.EDGE_MODE_DEFAULT;
    }

    public String getCameraNoiseReductionModePref() {
        return CameraController.NOISE_REDUCTION_MODE_DEFAULT;
    }

    public String getRecordVideoOutputFormatPref() {
        return "preference_video_output_format_default";
    }

    public CameraController.TonemapProfile getVideoTonemapProfile() {
        return CameraController.TonemapProfile.TONEMAPPROFILE_OFF;
    }

    public ApplicationInterface.VideoMaxFileSize getVideoMaxFileSizePref() throws ApplicationInterface.NoFreeStorageException {
        ApplicationInterface.VideoMaxFileSize video_max_filesize = new ApplicationInterface.VideoMaxFileSize();
        video_max_filesize.max_filesize = 0;
        video_max_filesize.auto_restart = true;
        return video_max_filesize;
    }


}
