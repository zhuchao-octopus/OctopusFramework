package com.zhuchao.android.fbase.bean;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class AudioMetaFile {
    // 基本元数据字段
    private String name;//文件的名字包括扩展名
    private String title; // 标题 歌曲的名字
    private String album; // 专辑
    private String artist; // 艺术家
    private String albumArtist; // 专辑艺术家
    private String author; // 作者
    private String composer; // 作曲家
    private String date; // 日期
    private String genre; // 流派
    private long duration; // 时长
    private String mimeType; // MIME类型
    private String comment; // 评论
    private String cdTrackNumber; // CD曲目编号
    private String discNumber; // 光盘编号
    private String writer; // 作者
    private String compilation; // 合集

    // 附加的 MediaStore 元数据字段
    private long size; // 文件大小
    private int id; // 歌曲的ID
    private int albumId; // 专辑ID
    private int track; // 曲目编号
    private int year; // 发行年份
    private long dateAdded; // 添加日期
    private long dateModified; // 修改日期
    private String filePathName; // 文件路径

    // 构造函数
    public AudioMetaFile() {

    }

    public AudioMetaFile(String name, String title, String album, String artist, String albumArtist, String author, String composer, String date, String genre, long duration, String mimeType, String comment, String cdTrackNumber, String discNumber, String writer, String compilation, long size, int id, int albumId, int track, int year, long dateAdded, long dateModified, String data) {
        this.name = name;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.albumArtist = albumArtist;
        this.author = author;
        this.composer = composer;
        this.date = date;
        this.genre = genre;
        this.duration = duration;
        this.mimeType = mimeType;
        this.comment = comment;
        this.cdTrackNumber = cdTrackNumber;
        this.discNumber = discNumber;
        this.writer = writer;
        this.compilation = compilation;
        this.size = size;
        this.id = id;
        this.albumId = albumId;
        this.track = track;
        this.year = year;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.filePathName = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCdTrackNumber() {
        return cdTrackNumber;
    }

    public void setCdTrackNumber(String cdTrackNumber) {
        this.cdTrackNumber = cdTrackNumber;
    }

    public String getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(String discNumber) {
        this.discNumber = discNumber;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getCompilation() {
        return compilation;
    }

    public void setCompilation(String compilation) {
        this.compilation = compilation;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public String getFilePathName() {
        return filePathName;
    }

    public void setFilePathName(String filePathName) {
        this.filePathName = filePathName;
    }

    @Override
    public String toString() {
        return "AudioFile{" + "name='" + name + '\'' + ", album='" + album + '\'' + ", artist='" + artist + '\'' + ", size=" + size + ", duration=" + duration + ", id=" + id + ", albumId=" + albumId + ", title='" + title + '\'' + ", track=" + track + ", year=" + year + ", mimeType='" + mimeType + '\'' + ", dateAdded=" + dateAdded + ", dateModified=" + dateModified + ", data='" + filePathName + '\'' + '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Bitmap getAlbumArtPicture(String filePathName) {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(filePathName);
            retriever.release();
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            } else {
                return null;
            }
        } catch (Exception ignored) {
        }
      return null;
    }
}
