package com.zhuchao.android.opencamera.Preview;

import android.util.Log;

import com.zhuchao.android.opencamera.MyDebug;
import com.zhuchao.android.opencamera.control.CameraController;

/**
 * Provides communication between the Preview and the rest of the application
 * - so in theory one can drop the Preview/ (and CameraController/) classes
 * into a new application, by providing an appropriate implementation of this
 * ApplicationInterface.
 */
public interface ApplicationInterface {
    class NoFreeStorageException extends Exception {
        private static final long serialVersionUID = -2021932609486148748L;
    }

    class VideoMaxFileSize {
        public long max_filesize; // maximum file size in bytes for video (return 0 for device default - typically this is ~2GB)
        public boolean auto_restart; // whether to automatically restart on hitting max filesize (this setting is still relevant for max_filesize==0, as typically there will still be a device max filesize)
    }

    enum VideoMethod {
        FILE, // video will be saved to a file
        SAF, // video will be saved using Android 5's Storage Access Framework
        MEDIASTORE, // video will be saved to the supplied MediaStore Uri
        URI // video will be written to the supplied Uri
    }

    class CameraResolutionConstraints {
        private static final String TAG = "CameraResConstraints";

        public boolean has_max_mp;
        public int max_mp;

        boolean hasConstraints() {
            return has_max_mp;
        }

        boolean satisfies(CameraController.Size size) {
            if (this.has_max_mp && size.width * size.height > this.max_mp) {
                if (MyDebug.LOG)
                    Log.d(TAG, "size index larger than max_mp: " + this.max_mp);
                return false;
            }
            return true;
        }
    }

    /**
     * The resolution to use for photo mode.
     * If the returned resolution is not supported by the device, or this method returns null, then
     * the preview will choose a size, and then call setCameraResolutionPref() with the chosen
     * size.
     * If the returned resolution is supported by the device, setCameraResolutionPref() will be
     * called with the returned resolution.
     * Note that even if the device supports the resolution in general, the Preview may choose a
     * different resolution in some circumstances:
     * * A burst mode as been requested, but the resolution does not support burst.
     * * A constraint has been set via constraints.
     * In such cases, the resolution actually in use should be found by calling
     * Preview.getCurrentPictureSize() rather than relying on the setCameraResolutionPref(). (The
     * logic behind this is that if a resolution is not supported by the device at all, it's good
     * practice to correct the preference stored in user settings; but this shouldn't be done if
     * the resolution is changed for something more temporary such as enabling burst mode.)
     *
     * @param constraints Optional constraints that may be set. If the returned resolution does not
     *                    satisfy these constraints, then the preview will choose the closest
     *                    resolution that does.
     */
    enum NRModePref {
        NRMODE_NORMAL,
        NRMODE_LOW_LIGHT
    }

    enum RawPref {
        RAWPREF_JPEG_ONLY, // JPEG only
        RAWPREF_JPEG_DNG // JPEG and RAW (DNG)
    }
}
