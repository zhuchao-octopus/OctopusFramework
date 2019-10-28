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

    private  List<Video> list;

    public VideoList() {
        this.list = new ArrayList<>();
    }

    public void addVideo(Video video)
    {
        //if(!list.contains(video))
        //if(findMoviebyId(video.getmMovie().getMovieId()) == null)
        if(video != null)
        {
            Video lvideo = findMoviebyIndex(list.size()-1);
            if(lvideo != null) lvideo.setmNextVideo(video);
            video.setmPreVideo(lvideo);
            list.add(video);
            Video fvideo = findMoviebyIndex(0);

            fvideo.setmPreVideo(video);
            video.setmNextVideo(fvideo);
        }
    }
    void removeVideo(Video video)
    {
        if(list.contains(video))
            list.remove(video);
    }
    void clear()
    {
         list.clear();
    }
    public Video findMoviebyId(int movieId)
    {
        for(int i =0;i< list.size();i++)
        {
            Video movie = (Video)list.get(i);
            if(movie.getmMovie().getMovieId() == movieId)
                return movie;
        }
        return null;
    }

    public Video findMoviebyIndex(int index)
    {
        Video movie =null;
        if(index >=0 && index < list.size())
            movie = (Video)list.get(index);
        return movie;
    }

    public  List<Video> getVideos() {
        return list;
    }

    public  int getMovieCount()
    {
       return list.size();
    }

    public List<Video> findMoviebySourceId(int sourceId)
    {
        List<Video> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getmMovie();
            if(movie.getSourceId() == sourceId)
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<Video> findMoviebyTypeName(String typeName)
    {
        List<Video> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getmMovie();
            if(movie.getMovieName().contains(typeName))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<Video> findMoviebyCategory(String CategoryName)
    {
        List<Video> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getmMovie();
            if(movie.getCategory().contains(CategoryName))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<Video> findMoviebyArea(String area)
    {
        List<Video> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getmMovie();
            if(movie.getRegion() == area)
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<Video> findMoviebyActor(String actor)
    {
        List<Video> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getmMovie();
            if(movie.getActor().contains(actor))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<Video> findMoviebyMovieName(String movieName)
    {
        List<Video> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getmMovie();
            if(movie.getMovieName().contains(movieName))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<Video> findMoviebyYear(String year)
    {
        List<Video> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getmMovie();
            if(movie.getYear().contains(year))
                movies.add(list.get(i));
        }
        return movies;
    }
    public List<Video> findMoviebyVip(int vipId)
    {
        List<Video> movies = new ArrayList<>();
        for(int i =0;i< list.size();i++)
        {
            Movie movie = (Movie)list.get(i).getmMovie();
            if(movie.getVipLevel() <= vipId && movie.getVipLevel() >= -1 )
                movies.add(list.get(i));
        }
        return movies;
    }
}