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

package JNIAPI;

import android.util.Log;

import com.zhuchao.android.libfilemanager.FilesManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JniSerialPort {

    private static final String TAG = "JniSerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd=null;
    private FileInputStream mFileInputStream=null;
    private FileOutputStream mFileOutputStream=null;

    public JniSerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

        /* Check access permission */
        if (!device.canRead() || !device.canWrite())
        {
            if(!FilesManager.isExists("/system/bin/su"))
                return ;
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"  + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                //throw new SecurityException();
            }
        }

        try {
            mFd = open(device.getAbsolutePath(), baudrate, flags);
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e(TAG, "native open fail:"+e.toString());
        }

        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            //throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    public boolean isDeviceReady()
    {
        if(mFd!=null && mFileInputStream !=null && mFileOutputStream!=null)
            return true;
        else
            return false;
    }


    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    static {
        try {
            System.loadLibrary("jhzserialport");
        }catch (UnsatisfiedLinkError e) {

        }
    }
}
