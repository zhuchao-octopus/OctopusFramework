package com.rockchip.car.recorder.service;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.CameraProfile;

import com.rockchip.car.recorder.camera2.CameraHolder;
import com.rockchip.car.recorder.utils.SLog;
import com.rockchip.car.recorder.utils.SystemProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/23.
 */
public class ParametersSet {

    private static final String TAG = "CAM_ParametersSet";

    private static Map<Integer, Integer> mQualitiesToIndex = new HashMap<Integer, Integer>();
    private static Map<Integer, Integer> mIndexToQualities = new HashMap<Integer, Integer>();
    private static int mMin;
    private static int mMax;
    static {
        mMin = 0;
        int tmp = mMin;
        mIndexToQualities.put(tmp, CamcorderProfile.QUALITY_LOW);
        mQualitiesToIndex.put(CamcorderProfile.QUALITY_LOW, tmp++);
        mIndexToQualities.put(tmp, CamcorderProfile.QUALITY_HIGH);
        mQualitiesToIndex.put(CamcorderProfile.QUALITY_HIGH, tmp++);
        mIndexToQualities.put(tmp, CamcorderProfile.QUALITY_QCIF);
        mQualitiesToIndex.put(CamcorderProfile.QUALITY_QCIF, tmp++);
        mIndexToQualities.put(tmp, CamcorderProfile.QUALITY_QVGA);
        mQualitiesToIndex.put(CamcorderProfile.QUALITY_QVGA, tmp++);
        mIndexToQualities.put(tmp, CamcorderProfile.QUALITY_480P);
        mQualitiesToIndex.put(CamcorderProfile.QUALITY_480P, tmp++);
        mIndexToQualities.put(tmp, CamcorderProfile.QUALITY_720P);
        mQualitiesToIndex.put(CamcorderProfile.QUALITY_720P, tmp++);
        mIndexToQualities.put(tmp, CamcorderProfile.QUALITY_1080P);
        mQualitiesToIndex.put(CamcorderProfile.QUALITY_1080P, tmp++);
        mIndexToQualities.put(tmp, CamcorderProfile.QUALITY_2160P);
        mQualitiesToIndex.put(CamcorderProfile.QUALITY_2160P, tmp);
        mMax = tmp;
    }

    public static CamcorderProfile getCamcorderProfile(int id, int quality) {
        return getCamcorderProfile(CameraHolder.instance().getParameters(id), id, quality);
    }

    public static CamcorderProfile getCamcorderProfile(Camera.Parameters paramaters, int id, int quality) {
        return getCamcorderProfile(paramaters, id, quality, true, true);
    }

    public static CamcorderProfile getCamcorderProfile(Camera.Parameters parameters, int id, int quality, boolean adjustable, boolean down) {
        if (parameters == null)
            return null;
        if (isSupportedCamcorderProfile(parameters, id, quality)) {
            return CamcorderProfile.get(id, quality);
        } else if (adjustable) {
            if (down) {
                Integer index = mQualitiesToIndex.get(quality);
                if (index != null && index > mMin) {
                    Integer q = mIndexToQualities.get(--index);
                    return getCamcorderProfile(parameters, id, q, adjustable, down);
                }
            } else {
                Integer index = mQualitiesToIndex.get(quality);
                if (index != null && index < mMax) {
                    Integer q = mIndexToQualities.get(++index);
                    return getCamcorderProfile(parameters, id, q, adjustable, down);
                }
            }
        }
        return null;
    }

    public static CamcorderProfile getBestCamcorderProfile(int id, int quality) {
        return getBestCamcorderProfile(CameraHolder.instance().getParameters(id), id, quality);
    }

    public static CamcorderProfile getBestCamcorderProfile(Camera.Parameters parameters, int id, int quality) {
        if (isSupportedCamcorderProfile(parameters, id, quality)) {
            return getCamcorderProfile(id, quality);
        }
        return getSupportedHightestCamcordProfile(id);
    }

    public static boolean isSupportedCamcorderProfile(int id, int quality) {
        return isSupportedCamcorderProfile(CameraHolder.instance().getParameters(id), id, quality);
    }

    private static boolean isSupportedCamcorderProfile(Camera.Parameters parameters, int id, int quality) {
        boolean hasProfile = CamcorderProfile.hasProfile(id, quality);
        if (hasProfile) {
            CamcorderProfile profile = CamcorderProfile.get(id, quality);
            if (profile == null) {
                return false;
            }
            List<Camera.Size> sizes = getSupportedVideoSize(parameters);

            if (sizes == null) {
                return false;
            }
            for (Camera.Size size : sizes) {
                if (profile.videoFrameWidth == size.width && profile.videoFrameHeight == size.height) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Camera.Size> getSupportedVideoSize(Camera.Parameters parameters) {
        return parameters != null ? parameters.getSupportedVideoSizes() : new ArrayList<Camera.Size>();
    }

    public static List<CamcorderProfile> getSupportedRecordSize(int id) {
        List<CamcorderProfile> cam = new ArrayList<CamcorderProfile>();
        Camera.Parameters parameters = CameraHolder.instance().getParameters(id);
        if (parameters != null) {
            for (int i = mMax; i >= mMin; i--) {
                if (isSupportedCamcorderProfile(parameters, id, mIndexToQualities.get(i))) {
                    CamcorderProfile c = CamcorderProfile.get(id, mIndexToQualities.get(i));
                    cam.add(c);
                    SLog.d(TAG, "ParametersSet.getSupportedRecordSize(" + id + "). quality:" + c.quality + "; width:" + c.videoFrameWidth + "; height:" + c.videoFrameHeight);
                }
            }
        }
        return cam;
    }

    public static List<CamcorderProfile> getSupportedRecordSize() {
        return getSupportedRecordSize(getMainCameraId());
    }

    public static boolean initParameters(int id) {
        SLog.d(TAG, "ParametersSet::initParameters.");
        if (CameraHolder.instance().getCamera(id) == null) {
            SLog.w(TAG, "ParametersSet::initParameters. getCamera is null");
            return false;
        }
        try {
            Camera.Parameters parameters = CameraHolder.instance().getParameters(id);
            if (parameters == null)
                return false;
            //================ Set Preview Parameters ================
//            String strPreviewSize = SystemProperties.get("sys.camera.recorder.preview_" + id, "");
//            int iSize[] = strToSize(strPreviewSize);
//            if (strPreviewSize.equals("") || iSize == null) {
                Camera.Size size = getSupportedHightestPreviewSize(id);
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                }
//            } else {
//                parameters.setPreviewSize(iSize[0], iSize[1]);
//            }
            if (id == CameraHolder.instance().getBackCameraId() ) {
               
            }
            int defaultPreviewFrameRate = parameters.getPreviewFrameRate();
            SLog.d(TAG, "ParameterSet::initParameters. id:" + id + "; defaultPreviewFrameRate:" + defaultPreviewFrameRate);
            if (!isPreviewFpsSupported(id, defaultPreviewFrameRate)) {
                int supportedMax = getSupportedMaxFps(id);
                SLog.d(TAG, "ParameterSet::initParameters. id:" + id + "; preview frame rate " + defaultPreviewFrameRate + " unsupported, set supported MAX " + supportedMax + " now");
                parameters.setPreviewFrameRate(supportedMax);
            }

            //================ Set Picture Parameters ================
            int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(id, CameraProfile.QUALITY_HIGH);
            parameters.setJpegQuality(jpegQuality);
//            String strPictureSize = SharedPreference.getString(getPrefKey(Config.KEY_PICTURE_SIZE, id), id == 0 ? "1920x1280" : "640*480");
 //           String strPictureSize = "";//CameraSettings.getPictureSize(id);
 //           iSize = strToSize(strPictureSize);
 //           parameters.setPictureSize(iSize[0], iSize[1]);

            //================ Set Record Parameters ================


            //================ Camera Effect ================
//            if (id == getMainCameraId()) {
//                // Set WhiteBalance
//                String balance = CameraSettings.getWhiteBlance();
//                List<String> balences = parameters.getSupportedWhiteBalance();
//                if (balences != null && balance != null && balences.indexOf(balance) > 0) {
//                    parameters.setWhiteBalance(balance);
//                }
//
//                // Set Exposure
//                int exposure = Integer.parseInt(CameraSettings.getExposure());
//                int maxExposure = parameters.getMaxExposureCompensation();
//                int minExposure = parameters.getMinExposureCompensation();
//                if (exposure >= minExposure && exposure <= maxExposure) {
//                    parameters.setExposureCompensation(exposure);
//                }
//
//                // Set ColorEffect
//                String effect = CameraSettings.getColorEffect();
//                List<String> effects = parameters.getSupportedColorEffects();
//                if (effects != null && effect != null && effects.indexOf(effect) > 0) {
//                    parameters.setColorEffect(effect);
//                }
//            }
//            parameters.set("watermark-en", "" + CameraSettings.isWaterMark());


			parameters.set("soc_camera_channel", mChannel); //px5 we dont use this . but it must>0
			if(mMirror!=0){
				parameters.set("mirror-preview", "true");
			}
            CameraHolder.instance().setParameters(id, parameters);
        } catch (RuntimeException e) {
            e.printStackTrace();
            SLog.w(TAG, "ParametersSet::initParameters.Occur RuntimeException!");
            return false;
        }
        return true;
    }
    
    public static int mMirror = 0;
    public static int mChannel = 1;
    

    private static String getSuffix(int id) {
        switch (id) {
            case 0:
                return "_mipi";
            case 1:
                return "_usb";
            case 2:
                return "_cvbs";
        }
        return "";
    }

    public static String getPrefKey(String str, int id) {
        return str + getSuffix(id);
    }

    public static int[] strToSize(String str) {
        int index = 0;
        if (str.contains("x")) {
            index = str.indexOf('x');
        } else if (str.contains("X")) {
            index = str.indexOf('X');
        } else if (str.contains("*")) {
            index = str.indexOf('*');
        }
        if (index > 0) {
            try {
                int width = Integer.parseInt(str.substring(0, index).replaceAll(" ", ""));
                int height = Integer.parseInt(str.substring(index + 1, str.length()).replaceAll(" ", ""));
                return new int[]{width, height};
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<String> getSupportedWhiteBalance() {
        return getMainCameraParameters() != null ? getMainCameraParameters().getSupportedWhiteBalance() : null;
    }

    public static List<String> getSupportedColorEffects() {
        return getMainCameraParameters() != null ? getMainCameraParameters().getSupportedColorEffects() : null;
    }

    public static List<Camera.Size> getSupportedPictureSize() {
        return getSupportedPictureSize(getMainCameraId());
    }

    public static List<Camera.Size> getSupportedPictureSize(int id) {
        Camera.Parameters parameters = CameraHolder.instance().getParameters(id);
        if (parameters == null || parameters.getSupportedPictureSizes() == null) {
            return new ArrayList<Camera.Size>();
        }
        return parameters.getSupportedPictureSizes();
    }

    public static List<Integer> getSupportedPreviewFps() {
        return getSupportedPreviewFps(getMainCameraId());
    }

    public static List<int[]> getSupportedPreviewFpsRange(int id) {
        if (CameraHolder.instance().getParameters(id) == null)  return null;
        return CameraHolder.instance().getParameters(id).getSupportedPreviewFpsRange();
    }

    public static List<Integer> getSupportedPreviewFps(int id) {
        if (CameraHolder.instance().getParameters(id) == null)  return null;
        return CameraHolder.instance().getParameters(id).getSupportedPreviewFrameRates();
    }

    public static boolean isPreviewFpsSupported(int id, Integer fps) {
        List<Integer> supporteds = getSupportedPreviewFps(id);
        return supporteds == null ? false : supporteds.indexOf(fps) >= 0;
    }

    public static Integer getSupportedMaxFps(int id) {
        List<Integer> fps = getSupportedPreviewFps(id);
        if (fps != null && fps.size() > 0) {
            int max = fps.get(0);
            for (Integer in : fps) {
                if (max < in) {
                    max = in;
                }
            }
            return max;
        }
        return 0;
    }

    public static CamcorderProfile getSupportedHightestCamcordProfile(int id) {
        List<CamcorderProfile> profiles = getSupportedRecordSize(id);
        if (profiles == null || profiles.size() < 1) return null;
        CamcorderProfile result = profiles.get(0);
        for (CamcorderProfile profile : profiles) {
            if (profile.videoFrameWidth*profile.videoFrameHeight > result.videoFrameWidth*result.videoFrameHeight) {
                result = profile;
            }
        }
        return result;
    }

    public static Camera.Size getSupportedHightestPictureSize(List<Camera.Size> supported) {
        if (supported == null || supported.size() <= 0)   return null;
        Camera.Size mMax = supported.get(0);
        for (int i = 0; i < supported.size(); i++) {
            Camera.Size size = supported.get(i);
            if (size.width * size.height > mMax.width*mMax.height) {
                mMax = supported.get(i);
            }
        }
        return mMax;
    }

    public static Camera.Size getSupportedHightestPictureSize(int id) {
        List<Camera.Size> supported = getSupportedPictureSize(id);
        return supported == null ? null : getSupportedHightestPictureSize(supported);
    }

    public static Camera.Size getSupportedHightestPreviewSize(int id) {
        if (CameraHolder.instance().getParameters(id) == null)  return null;
        List<Camera.Size> sizes = CameraHolder.instance().getParameters(id).getSupportedPreviewSizes();
        if (sizes == null || sizes.size() <= 0) return null;
        Camera.Size mMax = sizes.get(0);
        int length = sizes.size();
        for (int i = 0; i < length; i++) {
            Camera.Size size = sizes.get(i);
            SLog.d(TAG, "The Preview size length is " + sizes.size() + ", is support:" + size.width + "x" + size.height);
            if (size.width * size.height > mMax.width*mMax.height) {
                mMax = size;
            }
        }
        SLog.d(TAG, "The Max Preview size is support:" + mMax.width + "x" + mMax.height);
        return mMax;
    }

    public static boolean isPreviewSizeSupported(int id, Camera.Size size) {
        if (CameraHolder.instance().getParameters(id) == null)  return false;
        List<Camera.Size> sizes = CameraHolder.instance().getParameters(id).getSupportedPreviewSizes();
        if (sizes == null || sizes.size() <= 0) return false;
        for (int i = 0; i < sizes.size(); i++) {
            if (sizes.get(i).width == size.width && sizes.get(i).height == size.height) {
                return true;
            }
        }
        return false;
    }

    public static Integer getMinExposureCompensation() {
        return getMainCameraParameters() != null ? getMainCameraParameters().getMinExposureCompensation() : null;
    }

    public static Integer getMaxExposureCompensation() {
        return getMainCameraParameters() != null ? getMainCameraParameters().getMaxExposureCompensation() : null;
    }

    public static int getMainCameraId() {
        return 1;//BaseApplication.getInstance().getResources().getInteger(R.integer.surface0_to_camera);
    }

    private static Camera.Parameters getMainCameraParameters() {
        return CameraHolder.instance().getParameters(getMainCameraId());
    }

    public static void setPictureSize(int id) {
//        Camera.Parameters parameters = CameraHolder.instance().getParameters(id);
//        if (parameters != null) {
//            String strPictureSize = CameraSettings.getPictureSize(id);
//            int[] iSize = strToSize(strPictureSize);
//            parameters.setPictureSize(iSize[0], iSize[1]);
//            Camera camera = CameraHolder.instance().getCamera(id);
//            if (camera != null) {
//                camera.setParameters(parameters);
//            }
//        }
    }

    public static void setWhiteBlance(int id) {
//        Camera.Parameters parameters = CameraHolder.instance().getParameters(id);
//        if (parameters != null) {
//            String balance = CameraSettings.getWhiteBlance();
//            List<String> balences = parameters.getSupportedWhiteBalance();
//            if (balences != null && balance != null && balences.size() > 0 && balences.indexOf(balance) >= 0) {
//                parameters.setWhiteBalance(balance);
//                Camera camera = CameraHolder.instance().getCamera(id);
//                if (camera != null) {
//                    camera.setParameters(parameters);
//                }
//            }
//        }
    }

    public static void setExposure(int id) {
//        Camera.Parameters parameters = CameraHolder.instance().getParameters(id);
//        if (parameters != null) {
//            int exposure = Integer.parseInt(CameraSettings.getExposure());
//            int maxExposure = parameters.getMaxExposureCompensation();
//            int minExposure = parameters.getMinExposureCompensation();
//            if (exposure >= minExposure && exposure <= maxExposure) {
//                parameters.setExposureCompensation(exposure);
//                Camera camera = CameraHolder.instance().getCamera(id);
//                if (camera != null) {
//                    camera.setParameters(parameters);
//                }
//            }
//        }
    }

    public static void setColorEffect(int id) {
//        Camera.Parameters parameters = CameraHolder.instance().getParameters(id);
//        if (parameters != null) {
//            String effect = CameraSettings.getColorEffect();
//            List<String> effects = parameters.getSupportedColorEffects();
//            if (effects != null && effect != null && effects.size() > 0 && effects.indexOf(effect) >= 0) {
//                parameters.setColorEffect(effect);
//                Camera camera = CameraHolder.instance().getCamera(id);
//                if (camera != null) {
//                    camera.setParameters(parameters);
//                }
//            }
//        }
    }

    public static void setWaterMark(int id, String info) {
//        SLog.d(TAG, "ParametersSet::setWaterMark. id:" + id + "; WaterMark:" + CameraSettings.isWaterMark());
//        Camera.Parameters parameters = CameraHolder.instance().getParameters(id);
//        if (parameters != null) {
//            parameters.set("watermark-en", "" + CameraSettings.isWaterMark());
//            if (info != null) {
//                parameters.set("watermark-info", info);
//            }
//            Camera camera = CameraHolder.instance().getCamera(id);
//            if (camera != null) {
//                camera.setParameters(parameters);
//            }
//        }
    }
}
