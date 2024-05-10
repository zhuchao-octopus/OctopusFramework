package com.zhuchao.android.fbase.bean;


public class LMusic implements Comparable<LMusic> {
    private String pinyin;
    private int id;
    private int albumId;
    private String name;
    private String path;
    private String album;
    private String artist;
    private long size;
    private long duration;

    public LMusic(int id,int albumId,String name, String path, String album, String artist, long size, int duration) {
        this.id = id;
        this.albumId = albumId;
        this.name = name;
        this.path = path;
        this.album = album;
        this.artist = artist;
        this.size = size;
        this.duration = duration;
        //pinyin = PinyinUtils.getPinyin(name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    @Override
    public int compareTo(LMusic music) {
        return this.pinyin.compareTo(music.getPinyin());
    }

    @Override
    public String toString() {
        return "Music{" + "name='" + name + '\'' + ", path='" + path + '\'' + ", album='" + album + '\'' + ", artist='" + artist + '\'' + ", size=" + size + ", duration=" + duration + ", pinyin='" + pinyin + '\'' + '}';
    }
}
