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

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.zhuchao.android.fbase.FileUtils;

/*
 * Movie class represents video entity with videoName, description, image thumbs and video url.
 */
public class Movie implements Parcelable {
    //static final long serialVersionUID = 727566175075960653L;
    private int movieId;
    private int sourceId;
    private String title;
    private String name;
    private String type;
    private String artist;
    private String album;
    private String category;
    private String actor;
    private String studio;
    private String language;
    private String sharpness;
    private String description;
    private String bgImageUrl;
    private String cardImageUrl;
    private String year;
    private String date;
    private String srcUrl;
    private long duration;
    private long size;
    private int status;

    public Movie(final String url) {
        this.srcUrl = url;
        if (EmptyString(srcUrl)) return;
        this.name = this.srcUrl.substring(this.srcUrl.lastIndexOf("/") + 1);
    }

    public Movie(int movieId, int sourceId, String title, String name, String type, String artist, String album, String category, String actor, String studio, String language, String sharpness, String description, String bgImageUrl, String cardImageUrl, String year, String date, String srcUrl, long duration, long size, int status) {
        this.movieId = movieId;
        this.sourceId = sourceId;
        this.title = title;
        this.name = name;
        this.type = type;
        this.artist = artist;
        this.album = album;
        this.category = category;
        this.actor = actor;
        this.studio = studio;
        this.language = language;
        this.sharpness = sharpness;
        this.description = description;
        this.bgImageUrl = bgImageUrl;
        this.cardImageUrl = cardImageUrl;

        this.year = year;
        this.date = date;
        this.srcUrl = srcUrl;

        this.duration = duration;
        this.size = size;
        this.status = status;
    }

    protected Movie(Parcel in) {
        movieId = in.readInt();
        sourceId = in.readInt();
        title = in.readString();
        name = in.readString();
        type = in.readString();
        artist = in.readString();
        album = in.readString();
        category = in.readString();
        actor = in.readString();
        studio = in.readString();
        language = in.readString();
        sharpness = in.readString();
        description = in.readString();
        bgImageUrl = in.readString();
        cardImageUrl = in.readString();
        year = in.readString();
        date = in.readString();
        srcUrl = in.readString();

        duration = in.readLong();
        size = in.readLong();
        status = in.readInt();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getArtist() {
        if (FileUtils.EmptyString( this.artist)) this.artist = "Unknow";
        return artist;
    }

    public void setArtist(String artist) {
        if (FileUtils.EmptyString(artist)) this.artist = "Unknow";
        else this.artist = artist;
    }

    public String getAlbum() {
        if (FileUtils.EmptyString(this.album)) this.album = "Unknow";
        return album;
    }

    public void setAlbum(String album) {
        if (FileUtils.EmptyString(album)) this.album = "Unknow";
        else this.album = album;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSharpness() {
        return sharpness;
    }

    public void setSharpness(String sharpness) {
        this.sharpness = sharpness;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBgImageUrl() {
        return bgImageUrl;
    }

    public void setBgImageUrl(String bgImageUrl) {
        this.bgImageUrl = bgImageUrl;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(movieId);
        dest.writeInt(sourceId);
        dest.writeString(title);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(category);
        dest.writeString(actor);
        dest.writeString(studio);
        dest.writeString(language);
        dest.writeString(sharpness);
        dest.writeString(description);
        dest.writeString(bgImageUrl);
        dest.writeString(cardImageUrl);

        dest.writeString(year);
        dest.writeString(date);
        dest.writeString(srcUrl);

        dest.writeLong(duration);
        dest.writeLong(size);
        dest.writeInt(status);
    }

    @Override
    public String toString() {
        return "Movie{" + "movieId=" + movieId + ", sourceId=" + sourceId + ", title='" + title + '\'' + ", name='" + name + '\'' + ", type='" + type + '\'' + ", artist='" + artist + '\'' + ", album='" + album + '\'' + ", category='" + category + '\'' + ", actor='" + actor + '\'' + ", studio='" + studio + '\'' + ", language='" + language + '\'' + ", sharpness='" + sharpness + '\'' + ", description='" + description + '\'' + ", bgImageUrl='" + bgImageUrl + '\'' + ", cardImageUrl='" + cardImageUrl + '\'' + ", year='" + year + '\'' + ", date='" + date + '\'' + ", srcUrl='" + srcUrl + '\'' + ", duration=" + duration + ", size=" + size + ", status=" + status + '}';
    }
}
