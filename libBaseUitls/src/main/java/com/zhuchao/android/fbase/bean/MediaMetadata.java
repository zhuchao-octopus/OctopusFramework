package com.zhuchao.android.fbase.bean;

import android.graphics.Bitmap;

import com.zhuchao.android.fbase.FileUtils;

public class MediaMetadata {
    private int id;
    private String album;
    private String artist;
    private String author;
    private String description = "";
    private int count = 0;
    private Bitmap bitmap;
    public MediaMetadata() {
    }

    public MediaMetadata(int id, String album, String artist, String author) {
        this.id = id;
        this.album = album;
        this.artist = artist;
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        if (description == null) return "";
        return description;
    }

    public void setDescription(String description) {
        if (description == null) this.description = "";
        else this.description = description;
    }

    public void addDescription(String description) {
        if (FileUtils.NotEmptyString(description)) {
            if(!this.description.contains(description))
              this.description = this.description + " " + description;
        }
    }

    public int getCount() {
        return count;
    }

    public void addCount() {
        this.count++;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "MediaMetadata{" + "id=" + id + ", album='" + album + '\'' + ", artist='" + artist + '\'' + ", author='" + author + '\'' + ", description='" + description + '\'' + ", count=" + count + '}';
    }
}
