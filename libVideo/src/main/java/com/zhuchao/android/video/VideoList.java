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

import android.content.Context;
import android.text.TextUtils;

import com.zhuchao.android.libfileutils.MediaFile;

import java.util.ArrayList;
import java.util.List;

public class VideoList {

    private  List<OMedia> list;

    public VideoList() {
        this.list = new ArrayList<>();
    }

    public void addVideo(OMedia oMedia)
    {
        if(findMovieByPath(oMedia.getMovie().getSourceUrl()) != null) return;
        if(oMedia != null)
        {
            OMedia fvideo = findMoviebyIndex(0);
            OMedia lvideo = findMoviebyIndex(list.size()-1);

            if(lvideo != null) {
                lvideo.setNext(oMedia);
                oMedia.setPre(lvideo);
            }
            else
            {
                oMedia.setPre(oMedia);
                oMedia.setNext(oMedia);
            }

            if(fvideo != null) {
                fvideo.setPre(oMedia);
                oMedia.setNext(fvideo);
            }

            list.add(oMedia);
        }
    }

    public void addVideo(String url)
    {
        OMedia oMedia = new OMedia(url);
        addVideo(oMedia);
    }

    void removeVideo(OMedia oMedia)
    {
        list.remove(oMedia);
    }
    public void clear()
    {
         list.clear();
    }
    public OMedia findMoviebyId(int movieId)
    {
        for(int i =0;i< list.size();i++)
        {
            OMedia movie = list.get(i);
            if(movie.getMovie().getMovieId() == movieId)
                return movie;
        }
        return null;
    }

    public OMedia findMoviebyIndex(int index)
    {
        OMedia movie =null;
        if(index >=0 && index < list.size())
            movie = list.get(index);
        return movie;
    }

    public  List<OMedia> getVideos() {
        return list;
    }

    public  int getCount()
    {
       return list.size();
    }

    public List<OMedia> findMoviebySourceId(int sourceId)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = list.get(i).getMovie();
            if(movie.getSourceId() == sourceId)
                movies.add(list.get(i));
        }
        return movies;
    }

    public List<OMedia> findMoviebyTypeName(String typeName)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = list.get(i).getMovie();
            if(movie.getMovieName().contains(typeName))
                movies.add(list.get(i));
        }
        return movies;
    }

    public OMedia findMoviebyeName(String Name)
    {
        for(int i =0;i< list.size();i++)
        {
            Movie movie = list.get(i).getMovie();
            if(movie.getMovieName().equals(Name))
               return list.get(i);
        }
        return null;
    }

    public List<OMedia> findMoviebyCategory(String CategoryName)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = list.get(i).getMovie();
            if(movie.getCategory().contains(CategoryName))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<OMedia> findMoviebyArea(String area)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = list.get(i).getMovie();
            if(movie.getRegion() == area)
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<OMedia> findMoviebyActor(String actor)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = list.get(i).getMovie();
            if(movie.getActor().contains(actor))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<OMedia> findMoviebyMovieName(String movieName)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = list.get(i).getMovie();
            if(movie.getMovieName().contains(movieName))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<OMedia> findMoviebyYear(String year)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = list.get(i).getMovie();
            if(movie.getYear().contains(year))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<OMedia> findMoviebyVip(int vipId)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = list.get(i).getMovie();
            if(movie.getVipLevel() <= vipId && movie.getVipLevel() >= -1 )
                movies.add(list.get(i));
        }
        return movies;
    }
    public OMedia findMovieByPath(String path)
    {
        for(int i =0;i< list.size();i++)
        {
            OMedia oMedia = list.get(i);
            if(oMedia.getMovie().getSourceUrl().equals(path) )
                return oMedia;
        }
        return null;
    }

    public void loadFromDir(Context context, String FilePath, Integer fType) {
        List<String> FileList = MediaFile.getMediaFiles(context, FilePath, fType);
        for (int i = 0; i < FileList.size(); i++) {
            Movie movie = new Movie(FileList.get(i));
            String filename = getFileName(movie.getSourceUrl());
            if (!TextUtils.isEmpty(filename))
                movie.setMovieName(filename);
            addVideo(new OMedia(movie));
        }
    }
}