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

import android.content.Context;
import android.widget.ImageView;

import java.io.Serializable;

/*
 * Movie class represents video entity with videoName, description, image thumbs and video url.
 */
public class Movie implements Serializable {
    //static final long serialVersionUID = 727566175075960653L;
    private int mid;
    private int sid;
    private int status;
    private String name;
    private String type;
    private String year;
    private String region;
    private String actor;
    private String language;
    private String sharpness;
    private String description;
    private String studio;
    private String bgImageUrl;
    private String cardImageUrl;
    private String category;
    private String date;
    private String srcUrl;

    public Movie(final String sourceUrl) {
        this.srcUrl = sourceUrl;
        if (EmptyString(srcUrl)) return;
        this.name = this.srcUrl.substring(this.srcUrl.lastIndexOf("/") + 1);
    }

    public Movie(int movieId, int sourceId, String movieName, String movieType, String year, String region, String actor, String language, String sharpness, String description, String studio, String bgImageUrl, String cardImageUrl, String sourceUrl, String category, String date, int status) {
        this.mid = movieId;
        this.sid = sourceId;
        this.name = movieName;
        this.type = movieType;
        this.year = year;
        this.region = region;
        this.actor = actor;
        this.language = language;
        this.sharpness = sharpness;
        this.description = description;
        this.studio = studio;
        this.bgImageUrl = bgImageUrl;
        this.cardImageUrl = cardImageUrl;
        this.srcUrl = sourceUrl;
        this.category = category;
        this.date = date;
        this.status = status;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
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

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public void loadResourceInto(Context mContext, String path, ImageView imageView, int preloadImg) {
        //Glide.with(mContext)
        //            .load(path)
        //.placeholder(preloadImg)
        //.diskCacheStrategy(DiskCacheStrategy.NONE)
        //           .into(imageView);
    }

}
