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

    public PMovie(final String sourceUrl) {
        super(sourceUrl);
    }

    public PMovie(int movieId, int sourceId, String movieName, String movieType, String year, String region, String actor, String language, String sharpness, String description, String studio, String bgImageUrl, String cardImageUrl, String sourceUrl, String category, String date, int status) {
        super(movieId, sourceId, movieName, movieType, year, region, actor, language, sharpness, description, studio, bgImageUrl, cardImageUrl, sourceUrl, category, date, status);
    }

    public PMovie(Movie movie)
    {
        super(movie.getMid(),movie.getSid(),movie.getName(),movie.getType(),movie.getYear(),movie.getRegion(),movie.getActor(),movie.getLanguage(),movie.getSharpness()
        ,movie.getDescription(),movie.getStudio(),movie.getBgImageUrl(),movie.getCardImageUrl(),movie.getSrcUrl(),movie.getCategory(),movie.getDate(),movie.getStatus());
    }
    protected PMovie(Parcel in) {
        super(in);
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
