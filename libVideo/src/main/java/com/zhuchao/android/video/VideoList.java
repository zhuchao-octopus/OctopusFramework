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

import static com.zhuchao.android.libfileutils.FilesManager.getFileName;

import android.text.TextUtils;

import com.zhuchao.android.libfileutils.FilesManager;
import com.zhuchao.android.libfileutils.MediaFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class VideoList {
    //private List<OMedia> list;
    public HashMap<String, Object> hashMap;

    public VideoList() {
        this.hashMap = new HashMap();
    }

    public void add(OMedia oMedia) {
        if (oMedia == null) return;
        if (findByPath(oMedia.getMovie().getsUrl()) != null) return;

        OMedia fvideo = findByIndex(0);
        OMedia lvideo = findByIndex(hashMap.size() - 1);

        if (lvideo != null) {
            lvideo.setNext(oMedia);
            oMedia.setPre(lvideo);
        } else {
            oMedia.setPre(oMedia);
            oMedia.setNext(oMedia);
        }

        if (fvideo != null) {
            fvideo.setPre(oMedia);
            oMedia.setNext(fvideo);
        }
        //if(TextUtils.isEmpty(oMedia.getName()))
        hashMap.put(oMedia.md5(), oMedia);
        //else
        //  hashMap.put(oMedia.getName(), oMedia);
    }

    public void add(String key, Object value) {
        hashMap.put(key, value);
    }

    public int getCount() {
        return hashMap.size();
    }

    public void delete(OMedia oMedia) {
        if (oMedia == null) return;
        OMedia oPre = oMedia.getPre();
        OMedia oNext = oMedia.getNext();
        if (oPre != null)
            oPre.setNext(oNext);
        if (oNext != null)
            oNext.setPre(oPre);
        hashMap.remove(oMedia);
    }

    public void delete(String fileName) {
        OMedia ob = findByPath(fileName);
        if (ob != null)
            delete(ob);
    }

    public void clear() {
        hashMap.clear();
    }

    public OMedia findByIndex(int index) {
        int i = 0;
        if (index < 0 || index >= hashMap.size()) return null;
        Object[] array = hashMap.values().toArray();
        if (array == null) return null;
        return (OMedia) array[index];
    }

    public OMedia findAny() {
        Random generator = new Random();
        Object[] values = hashMap.values().toArray();
        Object randomValue = values[generator.nextInt(values.length)];
        return (OMedia) randomValue;
    }

    public List<OMedia> findsByName(String fileName) {
        List<OMedia> movies = new ArrayList<>();
        for (HashMap.Entry<String, Object> m : hashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            if (fileName.equals(oMedia.getMovie().getName()))
                movies.add(oMedia);
        }
        return movies;
    }

    public OMedia findByName(String fileName) {
        for (HashMap.Entry<String, Object> m : hashMap.entrySet()) {
            OMedia oMedia = (OMedia) m.getValue();
            if (fileName.equals(oMedia.getMovie().getName()))
                return oMedia;
        }
        return null;
    }

    public OMedia findByPath(String fileName) {
        String md5 = FilesManager.md5(fileName);
        return (OMedia) hashMap.get(md5);
    }

    //com.zhuchao.android.MEDIAFILEA_SCAN_ACTION
    public void loadFromDir(String FilePath, int FileType)
    {
        List<String> FileList = MediaFile.getMediaFiles(FilePath, FileType);
        for (int i = 0; i < FileList.size(); i++)
        {
            Movie movie = new Movie(FileList.get(i));
            String filename = getFileName(movie.getsUrl());
            if (!TextUtils.isEmpty(filename))
                movie.setName(filename);
            add(new OMedia(movie));
        }
    }
}