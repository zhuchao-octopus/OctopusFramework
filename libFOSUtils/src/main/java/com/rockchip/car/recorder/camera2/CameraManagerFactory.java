/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockchip.car.recorder.camera2;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory class for {@link CameraManager}.
 */
public class CameraManagerFactory {

    private static Map<Integer, AndroidCameraManagerImpl> sAndroidCameraManager = new HashMap<>();

    /**
     * Returns the android camera_surfaceview implementation of {@link CameraManager}.
     *
     * @return The {@link CameraManager} to control the camera_surfaceview device.
     */
    public static synchronized CameraManager getAndroidCameraManager(int id) {
        if (sAndroidCameraManager.get(id) == null) {
            sAndroidCameraManager.put(id, new AndroidCameraManagerImpl(id));
        }
        return sAndroidCameraManager.get(id);
    }
}
