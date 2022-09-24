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

package com.zhuchao.android.session.PaserBean;

/*
 * Movie class represents video entity with videoName, description, image thumbs and video url.
 */
public class MovieBean {
    private int movieId;
    private String movieName;
    private String movieType;
    private String year;
    private String region;
    private String actor;
    private String language;
    private String sharpness;
    private String description;
    private String studio;
    private String bgImageUrl;
    private String cardImageUrl;
    private int prePisodeId;
    private int nextPisodeId;
    private int totalPisode;
    private int episodeLast;
    private int reputation;
    private int vipLevel;
    private int sourceId;
    private String sourceUrl;
    private String category;
    private String date;
    private int status;

    public MovieBean(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public MovieBean(int movieId, String movieName, String movieType, String year, String region, String actor, String language, String sharpness, String description, String studio, String bgImageUrl, String cardImageUrl, int prePisodeId, int nextPisodeId, int totalPisode, int episodeLast, int reputation, int vipLevel, int sourceId, String sourceUrl, String category, String date, int status) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.movieType = movieType;
        this.year = year;
        this.region = region;
        this.actor = actor;
        this.language = language;
        this.sharpness = sharpness;
        this.description = description;
        this.studio = studio;
        this.bgImageUrl = bgImageUrl;
        this.cardImageUrl = cardImageUrl;
        this.prePisodeId = prePisodeId;
        this.nextPisodeId = nextPisodeId;
        this.totalPisode = totalPisode;
        this.episodeLast = episodeLast;
        this.reputation = reputation;
        this.vipLevel = vipLevel;
        this.sourceId = sourceId;
        this.sourceUrl = sourceUrl;
        this.category = category;
        this.date = date;
        this.status = status;
    }

    public int getMovieId() {
        return movieId;
    }

    void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieType() {
        return movieType;
    }

    public void setMovieType(String movieType) {
        this.movieType = movieType;
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

    public int getPrePisodeId() {
        return prePisodeId;
    }

    public void setPrePisodeId(int prePisodeId) {
        this.prePisodeId = prePisodeId;
    }

    public int getNextPisodeId() {
        return nextPisodeId;
    }

    public void setNextPisodeId(int nextPisodeId) {
        this.nextPisodeId = nextPisodeId;
    }

    public int getTotalPisode() {
        return totalPisode;
    }

    public void setTotalPisode(int totalPisode) {
        this.totalPisode = totalPisode;
    }

    public int getEpisodeLast() {
        return episodeLast;
    }

    public void setEpisodeLast(int episodeLast) {
        this.episodeLast = episodeLast;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
