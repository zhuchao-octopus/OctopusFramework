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

import java.util.ArrayList;
import java.util.List;

public class VideoList {

    private  List<OMedia> list;

    public VideoList() {
        this.list = new ArrayList<>();
    }

    public void addVideo(OMedia oMedia)
    {
        //if(!list.contains(oMedia))
        //if(findMoviebyId(oMedia.getmMovie().getMovieId()) == null)
        if(oMedia != null)
        {
            OMedia lvideo = findMoviebyIndex(list.size()-1);
            if(lvideo != null) lvideo.setNextOMedia(oMedia);
            oMedia.setPreOMedia(lvideo);
            list.add(oMedia);
            OMedia fvideo = findMoviebyIndex(0);

            fvideo.setPreOMedia(oMedia);
            oMedia.setNextOMedia(fvideo);
        }
    }
    void removeVideo(OMedia oMedia)
    {
        if(list.contains(oMedia))
            list.remove(oMedia);
    }
    void clear()
    {
         list.clear();
    }
    public OMedia findMoviebyId(int movieId)
    {
        for(int i =0;i< list.size();i++)
        {
            OMedia movie = (OMedia)list.get(i);
            if(movie.getMovie().getMovieId() == movieId)
                return movie;
        }
        return null;
    }

    public OMedia findMoviebyIndex(int index)
    {
        OMedia movie =null;
        if(index >=0 && index < list.size())
            movie = (OMedia)list.get(index);
        return movie;
    }

    public  List<OMedia> getVideos() {
        return list;
    }

    public  int getMovieCount()
    {
       return list.size();
    }

    public List<OMedia> findMoviebySourceId(int sourceId)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getMovie();
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
            Movie movie = (Movie)list.get(i).getMovie();
            if(movie.getMovieName().contains(typeName))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<OMedia> findMoviebyCategory(String CategoryName)
    {
        List<OMedia> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getMovie();
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
            Movie movie = (Movie)list.get(i).getMovie();
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
            Movie movie = (Movie)list.get(i).getMovie();
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
            Movie movie = (Movie)list.get(i).getMovie();
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
            Movie movie = (Movie)list.get(i).getMovie();
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
            Movie movie = (Movie)list.get(i).getMovie();
            if(movie.getVipLevel() <= vipId && movie.getVipLevel() >= -1 )
                movies.add(list.get(i));
        }
        return movies;
    }
}