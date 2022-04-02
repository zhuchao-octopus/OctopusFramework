/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.zhuchao.android.video;

import com.zhuchao.android.callbackevent.NormalRequestCallback;
import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.libfileutils.MediaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class VideoList {
    private NormalRequestCallback RequestCallBack = null;
    private int count = 0;
    private boolean threadLock = false;
    private HashMap<String, Object> FHashMap;

    public VideoList() {
        this.FHashMap = new HashMap();
    }

    public VideoList(NormalRequestCallback requestCallBack) {
        RequestCallBack = requestCallBack;
        this.count = 0;
        this.threadLock = false;
        this.FHashMap = new HashMap();
    }

    public HashMap<String, Object> getMap() {
        return FHashMap;
    }

    public void add(OMedia oMedia) {
        if (oMedia == null) return;
        if (findByPath(oMedia.getMovie().getsUrl()) != null) return;

        OMedia fVideo = findByIndex(0);
        OMedia lVideo = findByIndex(FHashMap.size() - 1);

        if (lVideo != null) {
            lVideo.setNext(oMedia);
            oMedia.setPre(lVideo);
        } else {
            oMedia.setPre(oMedia);
            oMedia.setNext(oMedia);
        }
        if (fVideo != null) {
            fVideo.setPre(oMedia);
            oMedia.setNext(fVideo);
        }
        FHashMap.put(oMedia.md5(), oMedia);
    }

    public void add(String FileName) {
        OMedia oMedia = new OMedia(FileName);
        FHashMap.put(oMedia.md5(), oMedia);
    }

    public void add(String key, Object Obj) {
        FHashMap.put(key, Obj);
    }

    public void delete(OMedia oMedia) {
        if (oMedia == null) return;
        OMedia oPre = oMedia.getPre();
        OMedia oNext = oMedia.getNext();
        if (oPre != null)
            oPre.setNext(oNext);
        if (oNext != null)
            oNext.setPre(oPre);
        FHashMap.remove(oMedia);
    }

    public void delete(String fileName) {
        OMedia ob = findByPath(fileName);
        if (ob != null)
            delete(ob);
    }

    public int getCount() {
        return FHashMap.size();
    }

    public void clear() {
        FHashMap.clear();
    }

    public OMedia findByIndex(int index) {
        if (index < 0 || index >= FHashMap.size()) return null;
        Object[] array = FHashMap.values().toArray();
        if (array == null) return null;
        return (OMedia) array[index];
    }

    public List<OMedia> findsByName(String fileName) {
        List<OMedia> movies = new ArrayList<>();
        for (HashMap.Entry<String, Object> m : FHashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            if (fileName.equals(oMedia.getMovie().getName()))
                movies.add(oMedia);
        }
        return movies;
    }

    public OMedia findByName(String fileName) {
        for (HashMap.Entry<String, Object> m : FHashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            if (fileName.equals(oMedia.getMovie().getName()))
                return oMedia;
        }
        return null;
    }

    public OMedia findByPath(String fileName) {
        String md5 = FilesManager.md5(fileName);
        return (OMedia) FHashMap.get(md5);
    }

    public OMedia findAny() {
        Random generator = new Random();
        Object[] values = FHashMap.values().toArray();
        Object randomValue = values[generator.nextInt(values.length)];
        return (OMedia) randomValue;
    }

    public void loadFromDir(String dirPath, int FileType) {
        if (threadLock) return;
        new Thread() {
            public void run() {
                threadLock = true;
                getMediaFiles(dirPath, FileType);
                threadLock = false;
            }
        }.start();
    }

    private void getMediaFiles(String FilePath, int fileType) {
        File path = new File(FilePath);
        File[] files = path.listFiles();
        getMediaFileName(files, fileType);
    }

    private void getMediaFileName(File[] files, int fileType) {
        if (files == null) return;
        String filePathName = null;
        for (File file : files) {
            if (file.isDirectory()) {
                getMediaFileName(file.listFiles(), fileType);
            } else {
                filePathName = file.getPath();// +"  "+ file.getName() ;
                MediaFile.MediaFileType mm = MediaFile.getFileType(filePathName);
                if (mm != null) {
                    count++;
                    if (MediaFile.isMimeTypeMedia(mm.mimeType) && (fileType == 100)) {
                        add(filePathName);//所有的媒体文件
                    } else if (MediaFile.isImageFileType(mm.fileType) && (fileType == 101)) {
                        add(filePathName);
                    } else if (MediaFile.isAudioFileType(mm.fileType) && (fileType == 102)) {
                        add(filePathName);
                    } else if (MediaFile.isVideoFileType(mm.fileType) && (fileType == 103)) {
                        add(filePathName);
                    } else if ((MediaFile.isVideoFileType(mm.fileType) || MediaFile.isAudioFileType(mm.fileType)) && (fileType == 104)) {
                        add(filePathName);
                    } else if (fileType == 99) {
                        add(filePathName);//所有的文件
                    }
                    if (RequestCallBack != null) {
                        RequestCallBack.onRequestComplete(filePathName, count);
                    }
                }
            }
        }
    }
}