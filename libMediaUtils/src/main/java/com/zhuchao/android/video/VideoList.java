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

import static com.zhuchao.android.fileutils.FileUtils.EmptyString;

import com.zhuchao.android.callbackevent.NormalCallback;
import com.zhuchao.android.fileutils.DataID;
import com.zhuchao.android.fileutils.FileUtils;
import com.zhuchao.android.fileutils.MMLog;
import com.zhuchao.android.fileutils.MediaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class VideoList {
    private String TAG = "VideoList";
    private NormalCallback RequestCallBack = null;
    private OMedia firstItem = null;
    private OMedia lastItem = null;
    private boolean threadLock = false;
    private HashMap<String, Object> FHashMap = new HashMap();

    /*private TreeMap<String, Object> FHashMap = new TreeMap<String, Object>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }
    });*/

    public VideoList() {
        RequestCallBack = null;
    }

    public VideoList(NormalCallback requestCallBack) {
        RequestCallBack = requestCallBack;
    }

    public HashMap<String, Object> getMap() {
        return FHashMap;
    }

    public void makeSingleLinkAll() {
        OMedia fVideo = findByIndex(0);
        for (int i = 0; i < getCount(); i++) {
            OMedia o = findByIndex(i);
            OMedia oo = findByIndex(i + 1);
            if (oo != null) {
                o.setNext(oo);
                //MLog.MLog.logTAG, "oo= " + oo.toString());
            } else {
                o.setNext(fVideo);
                fVideo.setPre(o);
            }
        }
    }

    public void add(OMedia oMedia) {
        if (oMedia == null) return;
        if (FHashMap.containsKey(oMedia.md5())) return;
        if (FHashMap.size() <= 0) firstItem = oMedia;

        if (lastItem != null) {//依次连接
            lastItem.setNext(oMedia);
            oMedia.setPre(lastItem);
        }
        if (firstItem != null) {//首尾连接
            firstItem.setPre(oMedia);
            oMedia.setNext(firstItem);
        }
        FHashMap.put(oMedia.md5(), oMedia);
        lastItem = oMedia;
        //MLog.log(TAG, "add1");
        if (RequestCallBack != null)
            RequestCallBack.onEventRequest(TAG, getCount());
    }

    public void add(String fileName) {
        if (EmptyString(fileName)) return;
        OMedia oMedia = new OMedia(fileName);
        add(oMedia);
    }

    public void add(VideoList vl) {
        for (HashMap.Entry<String, Object> oo : vl.getMap().entrySet()) {
            add((OMedia) oo);
        }
    }

    public void delete(OMedia oMedia) {
        if (oMedia == null) return;
        OMedia oPre = oMedia.getPre();
        OMedia oNext = oMedia.getNext();
        if (oPre != null)
            oPre.setNext(oNext);
        if (oNext != null)
            oNext.setPre(oPre);

        if (oMedia.equals(firstItem))
            firstItem = oNext;
        else if (oMedia.equals(lastItem))
            lastItem = oPre;

        FHashMap.remove(oMedia.md5());
    }

    public void delete(String fileName) {
        OMedia ob = findByPath(fileName);
        if (ob != null)
            delete(ob);
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
        String md5Key = FileUtils.md5(fileName);
        return (OMedia) FHashMap.get(md5Key);
    }

    public OMedia findAny() {
        Random generator = new Random();
        Object[] values = FHashMap.values().toArray();
        Object randomValue = values[generator.nextInt(values.length)];
        return (OMedia) randomValue;
    }

    //跳过无效的资源对象
    public OMedia getNextAvailable(OMedia oMedia) {
        OMedia ooMedia = null;
        if (getCount() <= 0) return null;
        if (oMedia == null)
            return firstItem;

        ooMedia = oMedia;//找下一个
        for (int i = 0; i < getCount(); i++) {
            if(ooMedia == null) break;
            ooMedia = ooMedia.getNext();
            if ((ooMedia != null) && (ooMedia.isAvailable(null)))
                return ooMedia;
        }
        return null;
    }

    public OMedia getPreAvailable(OMedia oMedia) {
        OMedia ooMedia = null;
        if (getCount() <= 0) return null;
        if (oMedia == null)
            return lastItem;

        ooMedia = oMedia;//找下一个
        for (int i = 0; i < getCount(); i++) {
            if(ooMedia == null) break;
            ooMedia = ooMedia.getPre();
            if ((ooMedia != null) && (ooMedia.isAvailable(null)))
                return ooMedia;
        }
        return null;
    }

    public boolean exist(String fileName) {
        String md5Key = FileUtils.md5(fileName);
        return FHashMap.containsKey(md5Key);
    }

    public boolean exist(OMedia oMedia) {
        return FHashMap.containsValue(oMedia);
    }

    public OMedia getFirstItem() {
        return firstItem;
    }

    public OMedia getLastItem() {
        return lastItem;
    }

    public int getCount() {
        return FHashMap.size();
    }

    public void clear() {
        FHashMap.clear();
    }

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    public void printAll() {
        int i = 0;
        for (HashMap.Entry<String, Object> m : FHashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            MMLog.log(TAG, i + ":" + oMedia.getPathName());
            i++;
        }
    }

    public void printAllByIndex() {
        for (int i = 0; i < getCount(); i++) {
            OMedia oMedia = findByIndex(i);
            if (oMedia != null)
                MMLog.log(TAG, i + ":" + oMedia.getPathName());
            else
                MMLog.log(TAG, "null");
        }
    }

    public void printFollow() {
        if (getCount() <= 0) return;
        OMedia oMedia = firstItem;
        for (int i = 0; i < getCount(); i++) {
            if (oMedia != null) {
                MMLog.log(TAG, i + ":↓" + oMedia.getPathName());
                oMedia = oMedia.getNext();
                if (oMedia == null)
                    MMLog.log(TAG, "null");
                if (oMedia.equals(lastItem))
                    MMLog.log(TAG, "printFollow() done");
            }
        }
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
                    if (MediaFile.isMimeTypeMedia(mm.mimeType) && (fileType == DataID.MEDIA_TYPE_ID_AllMEDIA)) {
                        add(filePathName);//所有的媒体文件
                    } else if (MediaFile.isImageFileType(mm.fileType) && (fileType == DataID.MEDIA_TYPE_ID_PIC)) {
                        add(filePathName);
                    } else if (MediaFile.isAudioFileType(mm.fileType) && (fileType == DataID.MEDIA_TYPE_ID_AUDIO)) {
                        add(filePathName);
                    } else if (MediaFile.isVideoFileType(mm.fileType) && (fileType == DataID.MEDIA_TYPE_ID_VIDEO)) {
                        add(filePathName);
                    } else if (fileType == DataID.MEDIA_TYPE_ID_AllFILE) {
                        add(filePathName);//所有的文件
                    }
                    try {
                        if (RequestCallBack != null)
                            RequestCallBack.onEventRequest(TAG, fileType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}