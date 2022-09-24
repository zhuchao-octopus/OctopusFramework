package com.zhuchao.android.player.dlna;

import android.content.Context;
import android.content.Intent;

import org.cybergarage.upnp.Device;

import java.util.List;


public class DLNAUtil {
    private static final String MEDIARENDER = "urn:schemas-upnp-org:device:MediaRenderer:1";
    public static final IController DLNAController = new MultiPointController();

    /**
     * Check if the device is a media render device
     *
     * @param device
     * @return
     */
    public static boolean isMediaRenderDevice(Device device) {
        if (device != null
                && MEDIARENDER.equalsIgnoreCase(device.getDeviceType())) {
            return true;
        }

        return false;
    }

    public static void startDLNAService(Context context) {
        Intent intent = new Intent(context, DLNAService.class);
        context.startService(intent);
    }

    public static void stopDLNAService(Context context) {
        Intent intent = new Intent(context, DLNAService.class);
        context.stopService(intent);
    }

    public static void setDLNADeviceListener(DLNAContainer.DeviceChangeListener deviceChangeListener) {
        DLNAContainer.getInstance().setDeviceChangeListener(deviceChangeListener);
    }

    public static List<Device> getDLNADevices() {
        return DLNAContainer.getInstance().getDevices();
    }

    public static void setDLNASelectedDevice(Device device) {
        DLNAContainer.getInstance().setSelectedDevice(device);
    }

    public static Device getDLNASelectedDevice() {
        return DLNAContainer.getInstance().getSelectedDevice();
    }

    public static void shareTo(String fromUrl, Device toDevice) {
        new Thread() {
            public void run() {
                DLNAController.play(toDevice, fromUrl);
            }
        }.start();
    }
}
