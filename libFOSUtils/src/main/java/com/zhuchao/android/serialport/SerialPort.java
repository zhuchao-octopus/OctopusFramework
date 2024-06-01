/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhuchao.android.serialport;

import com.zhuchao.android.fbase.MMLog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class SerialPort {
    private static final String TAG = "SerialPort";
    public static final String DEFAULT_SU_PATH = "/system/bin/su";
    private final File device;
    private final int baudrate;
    private final int dataBits;
    private final int parity;
    private final int stopBits;
    private final int flags;

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd = null;
    private FileInputStream mFileInputStream = null;
    private FileOutputStream mFileOutputStream = null;

    public SerialPort(File device, int baudRate, int dataBits, int parity, int stopBits, int flags) throws SecurityException, IOException {
        this.device = device;
        this.baudrate = baudRate;
        this.dataBits = dataBits;
        this.parity = parity;
        this.stopBits = stopBits;
        this.flags = flags;
        if (!device.canRead() || !device.canWrite()) {   /* Check access permission */
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec(DEFAULT_SU_PATH);
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    //throw new SecurityException();
                    MMLog.e(TAG, "native open fail,not allow to read and write ");
                    return;
                }
            } catch (Exception e) {
                //e.printStackTrace();
                //throw new SecurityException();
                MMLog.e(TAG, "native open returns null " + e.toString());
                return;
            }
        }

        try {
            mFd = open(device.getAbsolutePath(), baudrate, dataBits, parity, stopBits, flags);
            if (mFd == null) {
                MMLog.e(TAG, "native open failed return null " + device.getAbsolutePath());
                return;
            }
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
        } catch (Exception e) {
            //e.printStackTrace();
            MMLog.e(TAG, e.toString());
        }
    }

    public SerialPort(File device, int baudRate) throws SecurityException, IOException {
        this(device, baudRate, 8, 0, 1, 0);
    }

    public SerialPort(File device, int baudRate, int dataBits, int parity, int stopBits) throws SecurityException, IOException {
        this(device, baudRate, dataBits, parity, stopBits, 0);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    public File getDevice() {
        return device;
    }

    public int getBaudRate() {
        return baudrate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getParity() {
        return parity;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getFlags() {
        return flags;
    }

    public boolean isDeviceReady() {
        return mFd != null && mFileInputStream != null && mFileOutputStream != null;
    }

    // JNI
    private native FileDescriptor open(String absolutePath, int baudRate, int dataBits, int parity, int stopBits, int flags);

    public native void close();

    public void tryClose() {
        try {
            mFileInputStream.close();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            mFileInputStream = null;
        }

        try {
            mFileOutputStream.close();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            mFileOutputStream = null;
        }

        try {
            close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static Builder newBuilder(File device, int baudRate) {
        return new Builder(device, baudRate);
    }

    public static Builder newBuilder(String devicePath, int baudRate) {
        return new Builder(devicePath, baudRate);
    }

    public final static class Builder {
        private final File device;
        private final int baudRate;
        private int dataBits = 8;
        private int parity = 0;
        private int stopBits = 1;
        private int flags = 0;

        private Builder(File device, int baudRate) {
            this.device = device;
            this.baudRate = baudRate;
        }

        private Builder(String devicePath, int baudRate) {
            this(new File(devicePath), baudRate);
        }

        public Builder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        public Builder parity(int parity) {
            this.parity = parity;
            return this;
        }

        public Builder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        public SerialPort build() throws SecurityException, IOException {
            return new SerialPort(device, baudRate, dataBits, parity, stopBits, flags);
        }
    }

    static {
        System.loadLibrary("uart");
    }
}
