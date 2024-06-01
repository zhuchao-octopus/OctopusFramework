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

package com.zhuchao.android.car.aidl;

import android.os.Parcel;
import android.os.Parcelable;

import com.zhuchao.android.video.Movie;

/*
 * Movie class represents video entity with videoName, description, image thumbs and video url.
 */
public class PMovie extends Movie implements Parcelable {

    public PMovie(String url) {
        super(url);
    }

    public PMovie(int movie_id, int source_id, String title, String name, String type, String artist, String album, String category, String actor, String studio, String language, String sharpness, String description, String bgImageUrl, String cardImageUrl, long duration, long size, String year, String date, String srcUrl, int status) {
        super(movie_id, source_id, title, name, type, artist, album, category, actor, studio, language, sharpness, description, bgImageUrl, cardImageUrl, year, date, srcUrl, duration, size, status);
    }

    public PMovie(Movie movie) {
        super(movie.getMovieId(), movie.getSourceId(), movie.getTitle(), movie.getName(), movie.getType(), movie.getArtist(), movie.getAlbum(), movie.getCategory(), movie.getActor(), movie.getStudio(), movie.getLanguage(), movie.getSharpness(), movie.getDescription(), movie.getBgImageUrl(), movie.getCardImageUrl(), movie.getYear(), movie.getDate(), movie.getSrcUrl(), movie.getDuration(), movie.getSize(), movie.getStatus());
    }

    protected PMovie(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PMovie> CREATOR = new Creator<PMovie>() {
        @Override
        public PMovie createFromParcel(Parcel in) {
            return new PMovie(in);
        }

        @Override
        public PMovie[] newArray(int size) {
            return new PMovie[size];
        }
    };
}
