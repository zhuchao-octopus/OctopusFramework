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

import static com.zhuchao.android.fbase.FileUtils.EmptyString;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MediaFile;
import com.zhuchao.android.fbase.eventinterface.NormalCallback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VideoList {
    private final String TAG = "VideoList";
    private String mListName = "VideoList";
    private NormalCallback mRequestCallBack = null;
    private OMedia mFirstItem = null;
    private OMedia mLastItem = null;
    private boolean mThreadLock = false;
    private final HashMap<String, Object> mFHashMap = new HashMap<>();

    /*private TreeMap<String, Object> FHashMap = new TreeMap<String, Object>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }
    });*/

    public VideoList() {
        mRequestCallBack = null;
    }

    public VideoList(String listName) {
        this.mListName = listName;
        mRequestCallBack = null;
    }

    public VideoList(NormalCallback mRequestCallBack) {
        this.mRequestCallBack = mRequestCallBack;
    }

    public HashMap<String, Object> getMap() {
        return mFHashMap;
    }

    public HashMap<String, Object> getAll() {
        return mFHashMap;
    }

    public void updateSingleLinkOrder() {
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

    public void updateLinkOrder() {
        mFirstItem = null;
        mLastItem = null;
        OMedia oMediaPrev = null;
        for (HashMap.Entry<String, Object> m : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            if (oMedia == null) {
                return;
            }
            if (mFirstItem == null) {
                mFirstItem = oMedia;
                oMedia.setNext(oMedia);
                oMedia.setPre(oMedia);
            } else {
                if (oMediaPrev != null) {
                    oMedia.setPre(oMediaPrev);
                    oMedia.setNext(mFirstItem);
                    oMediaPrev.setNext(oMedia);
                }
            }
            oMediaPrev = oMedia;
            mLastItem = oMedia;
        }
    }

    public void add(OMedia oMedia) {
        if (oMedia == null) return;
        if (mFHashMap.containsKey(oMedia.md5())) return;
        if (mFHashMap.size() == 0) mFirstItem = oMedia;

        if (mLastItem != null) {//依次连接
            mLastItem.setNext(oMedia);///加到最后
            oMedia.setPre(mLastItem);
        }
        if (mFirstItem != null) {//首尾连接
            mFirstItem.setPre(oMedia);
            oMedia.setNext(mFirstItem);
        }
        mFHashMap.put(oMedia.md5(), oMedia);
        mLastItem = oMedia;
        //MLog.log(TAG, "add1");
        if (mRequestCallBack != null) mRequestCallBack.onEventRequest(TAG, getCount());
    }

    public void add(String filePathName) {
        if (EmptyString(filePathName)) return;
        OMedia oMedia = new OMedia(filePathName);
        add(oMedia);
    }

    public void add(VideoList videoList) {
        for (HashMap.Entry<String, Object> oo : videoList.getMap().entrySet()) {
            add((OMedia) oo.getValue());
        }
        //for (HashMap.Entry<String, Object> m : FHashMap.entrySet()) {
        //    OMedia oMedia = (OMedia) m.getValue();
    }

    public void addRow(OMedia oMedia) {
        if (oMedia == null) return;
        if (mFHashMap.containsKey(oMedia.md5())) return;
        if (mFHashMap.size() == 0) mFirstItem = oMedia;
        if (mLastItem == null) mLastItem = oMedia;
        mFHashMap.put(oMedia.md5(), oMedia);
        if (mRequestCallBack != null) mRequestCallBack.onEventRequest(TAG, getCount());
    }

    public void update(OMedia oMedia) {
        if (oMedia == null) return;
        ///if (mFHashMap.containsKey(oMedia.md5())) return;
        if (mFHashMap.size() == 0) mFirstItem = oMedia;
        if (mLastItem == null) mLastItem = oMedia;
        mFHashMap.put(oMedia.md5(), oMedia);
        if (mRequestCallBack != null) mRequestCallBack.onEventRequest(TAG, getCount());
    }

    public void delete(OMedia oMedia) {
        if (oMedia == null) return;
        OMedia oPre = oMedia.getPrev();
        OMedia oNext = oMedia.getNext();
        if (oPre != null) oPre.setNext(oNext);
        if (oNext != null) oNext.setPre(oPre);

        if (oMedia.equals(mFirstItem)) mFirstItem = oNext;
        else if (oMedia.equals(mLastItem)) mLastItem = oPre;

        mFHashMap.remove(oMedia.md5());
    }

    public void delete(String fileName) {
        OMedia ob = findByPath(fileName);
        if (ob != null) delete(ob);
    }

    public void deleteFrom(OMedia oMedia, int count) {
        if (getCount() <= 0 || mFirstItem == null) return;
        for (int i = 0; i < count; i++) {
            delete(oMedia);
        }
    }

    public OMedia getFirstItem() {
        return mFirstItem;
    }

    public OMedia getLastItem() {
        return mLastItem;
    }

    public int getCount() {
        return mFHashMap.size();
    }

    public void clear() {
        mFHashMap.clear();
    }

    public String getListName() {
        return mListName;
    }

    public void setListName(String mListName) {
        this.mListName = mListName;
    }

    public boolean exist(String filePathName) {
        String md5Key = FileUtils.MD5(filePathName);
        return mFHashMap.containsKey(md5Key);
    }

    public boolean exist(OMedia oMedia) {
        return mFHashMap.containsValue(oMedia);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public OMedia findByIndex(int index) {
        if (index < 0 || index >= mFHashMap.size()) return null;
        Object[] array = mFHashMap.values().toArray();
        return (OMedia) array[index];
    }

    public List<OMedia> findsByName(String fileName) {
        List<OMedia> movies = new ArrayList<>();
        for (HashMap.Entry<String, Object> m : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            if (fileName.equals(oMedia.getMovie().getName())) movies.add(oMedia);
        }
        return movies;
    }

    public OMedia findByName(String fileName) {
        for (HashMap.Entry<String, Object> m : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            if (fileName.equals(oMedia.getMovie().getName())) return oMedia;
        }
        return null;
    }

    public OMedia findByPath(String filePathName) {
        String md5Key = FileUtils.MD5(filePathName);
        return (OMedia) mFHashMap.get(md5Key);
    }

    public OMedia findByMovie(Movie movie) {
        String md5Key = FileUtils.MD5(movie.getSrcUrl());
        OMedia oMedia = (OMedia) mFHashMap.get(md5Key);
        if (oMedia == null) return null;
        if (oMedia.getMovie().equals(movie)) return oMedia;
        else return null;
    }

    public OMedia findAny() {
        int random = FileUtils.getRandom(getCount());
        Object[] values = mFHashMap.values().toArray();
        Object randomValue = values[random];
        return (OMedia) randomValue;
    }

    //跳过无效的资源对象
    public OMedia getNextAvailable(OMedia oMedia) {
        OMedia ooMedia = null;
        if (getCount() <= 0) return null;
        if (oMedia == null) return mFirstItem;

        ooMedia = oMedia;//找下一个
        for (int i = 0; i < getCount(); i++) {
            if (ooMedia == null) break;
            ooMedia = ooMedia.getNext();
            if ((ooMedia != null) && (ooMedia.isAvailable(null))) return ooMedia;
        }
        return null;
    }

    public OMedia getPreAvailable(OMedia oMedia) {
        OMedia ooMedia = null;
        if (getCount() <= 0) return null;
        if (oMedia == null) return mLastItem;

        ooMedia = oMedia;//找下一个
        for (int i = 0; i < getCount(); i++) {
            if (ooMedia == null) break;
            ooMedia = ooMedia.getPrev();
            if ((ooMedia != null) && (ooMedia.isAvailable(null))) return ooMedia;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public VideoList getMusic() {
        VideoList audioList1 = new VideoList();
        for (HashMap.Entry<String, Object> oo : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (oMedia.isAudio()) audioList1.addRow(oMedia);
        }
        return audioList1;
    }

    public VideoList getVideo() {
        VideoList audioList1 = new VideoList();
        for (HashMap.Entry<String, Object> oo : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (oMedia.isVideo()) audioList1.addRow(oMedia);
        }
        return audioList1;
    }

    public VideoList getMusicByArtist(@NonNull String artist) {
        VideoList audioList1 = new VideoList();
        for (HashMap.Entry<String, Object> oo : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (artist.equals(oMedia.getMovie().getArtist())) audioList1.addRow(oMedia);
        }
        return audioList1;
    }

    public VideoList getMusicByAlbum(@NonNull String album) {
        VideoList audioList1 = new VideoList();
        for (HashMap.Entry<String, Object> oo : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (album.equals(oMedia.getMovie().getAlbum())) audioList1.addRow(oMedia);
        }
        return audioList1;
    }

    public VideoList getRawMusicByArtist(@NonNull String artist) {
        VideoList audioList1 = new VideoList();
        for (HashMap.Entry<String, Object> oo : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (artist.equals(oMedia.getMovie().getArtist())) audioList1.addRow(oMedia);
        }
        return audioList1;
    }

    public VideoList getRawMusicByAlbum(@NonNull String album) {
        VideoList audioList1 = new VideoList();
        for (HashMap.Entry<String, Object> oo : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (album.equals(oMedia.getMovie().getAlbum())) audioList1.addRow(oMedia);
        }
        return audioList1;
    }

    public void copyAudioMediaFrom(VideoList videoList) {
        if (videoList != null) {
            for (HashMap.Entry<String, Object> oo : videoList.getMap().entrySet()) {
                OMedia oMedia = (OMedia) oo.getValue();
                if (oMedia.isAudio()) add(new OMedia(oMedia.getMovie()));
            }
        }
    }

    public void loadAllRawMediaFrom(VideoList videoList) {
        if (videoList != null) {
            for (HashMap.Entry<String, Object> oo : videoList.getMap().entrySet()) {
                addRow((OMedia) oo.getValue());
            }
        }
    }

    public void loadAllRawVideoFrom(VideoList videoList) {
        for (HashMap.Entry<String, Object> oo : videoList.getMap().entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (oMedia.isVideo()) addRow(oMedia);
        }
    }

    public void loadAllRawAudioFrom(VideoList videoList) {
        for (HashMap.Entry<String, Object> oo : videoList.getMap().entrySet()) {
            OMedia oMedia = (OMedia) oo.getValue();
            if (oMedia.isAudio()) addRow(oMedia);
        }
    }

    public List<Movie> toMovieList() {
        List<Movie> list = new ArrayList<>();
        for (HashMap.Entry<String, Object> m : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            list.add(oMedia.getMovie());
        }
        return list;
    }

    public List<OMedia> toOMediaList() {
        List<OMedia> list = new ArrayList<>();
        for (HashMap.Entry<String, Object> m : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            list.add(oMedia);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> toList() {
        List<T> list = new ArrayList<T>();
        for (Map.Entry<String, Object> entity : mFHashMap.entrySet()) {
            list.add((T) entity.getValue());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> toListLike(String likedName) {
        List<T> list = new ArrayList<T>();
        for (Map.Entry<String, Object> entity : mFHashMap.entrySet()) {
            if (entity.getKey().contains(likedName)) list.add((T) entity.getValue());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> toListByAlbum(String album) {
        List<T> list = new ArrayList<T>();
        for (Map.Entry<String, Object> entity : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) entity.getValue();
            if (album.equals(oMedia.getMovie().getAlbum())) list.add((T) entity.getValue());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> toListByArtist(String artist) {
        List<T> list = new ArrayList<T>();
        for (Map.Entry<String, Object> entity : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) entity.getValue();
            if (artist.equals(oMedia.getMovie().getArtist())) list.add((T) entity.getValue());
        }
        return list;
    }

    public Object[] toArray() {
        return mFHashMap.values().toArray();
    }

    public void printAll() {
        int i = 0;
        MMLog.d(TAG, mListName + " Print all medias count:" + getCount());
        for (HashMap.Entry<String, Object> m : mFHashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            MMLog.log(TAG, i + ":" + oMedia.getPathName());
            i++;
        }
    }

    public void printAllByIndex() {
        for (int i = 0; i < getCount(); i++) {
            OMedia oMedia = findByIndex(i);
            if (oMedia != null) MMLog.log(TAG, mListName + " " + i + ":" + oMedia.getPathName());
            else MMLog.log(TAG, "null");
        }
    }

    public void printFollow() {
        if (getCount() <= 0) return;
        OMedia oMedia = mFirstItem;
        for (int i = 0; i < getCount(); i++) {
            if (oMedia != null) {
                MMLog.log(TAG, i + ":↓" + oMedia.getPathName());
                oMedia = oMedia.getNext();
                if (oMedia == null) {
                    MMLog.log(TAG, "null");
                    break;
                }
            }
        }
        ///if (oMedia.equals(mLastItem)) MMLog.log(TAG, "printFollow() done");
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
                        if (mRequestCallBack != null) mRequestCallBack.onEventRequest(TAG, fileType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @SuppressLint("SdCardPath")
    public void saveToFile(Context context, String fileName) {
        try {
            String line = System.getProperty("line.separator");
            //String fileDir = String.valueOf(context.getExternalFilesDir(null));//"/sdcard/Octopus/";
            String fileDir = String.valueOf(context.getCacheDir()) + "/";//"/sdcard/Octopus/";
            String filePathName = fileDir + fileName; //FileUtils.getDirBaseExternalStorageDirectory(".octopus") + "/" + fileName;
            FileUtils.MakeDirsExists(Objects.requireNonNull(fileDir));
            ///MMLog.d(TAG, mListName + " save to " + filePathName);
            StringBuilder stringBuffer = new StringBuilder();
            FileWriter fw = new FileWriter(filePathName);

            ///Set<Map.Entry<String, Object>> set = FHashMap.entrySet();
            ///for (Map.Entry<String, Object> stringObjectEntry : set) {
            ///    stringBuffer.append(((Map.Entry<?, ?>) stringObjectEntry).getKey()).append(" : ").append(((Map.Entry<?, ?>) stringObjectEntry).getValue()).append(line);
            ///}
            for (HashMap.Entry<String, Object> m : mFHashMap.entrySet()) {
                OMedia oMedia = (OMedia) m.getValue();
                stringBuffer.append(oMedia.getPathName()).append(line);
                ///MMLog.log(TAG, i + ":" + oMedia.getPathName());
            }

            fw.write(stringBuffer.toString());
            fw.close();
        } catch (IOException e) {
            MMLog.e(TAG, e.getMessage()); //e.printStackTrace();
        }
    }

    public void loadFromFile(Context context, String fileName) {
        //String line = System.getProperty("line.separator");
        String fileDir = String.valueOf(context.getCacheDir()) + "/";//"/sdcard/Octopus/";
        String filePathName = fileDir + fileName;
        MMLog.d(TAG, mListName + " load from " + filePathName);
        if (FileUtils.existFile(filePathName)) {
            List<String> newList = FileUtils.ReadTxtFile(filePathName);
            for (String str : newList) {
                this.add(str);
            }
        }
    }

    public void loadFromDir(String dirPath, int FileType) {
        if (mThreadLock) return;
        new Thread() {
            public void run() {
                mThreadLock = true;
                getMediaFiles(dirPath, FileType);
                mThreadLock = false;
            }
        }.start();
    }

    public void loadFromStringList(List<String> filesList) {
        clear();
        for (String str : filesList) {
            add(str);
        }
    }
}