package com.zhuchao.android.fbase.bean;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class VideoMetaFile {
    private int id;
    // 文件显示名称
    private String name;
    // 视频文件标题
    private String title;
    // 视频文件路径
    private String filePathName;
    // 视频文件大小（以字节为单位）
    private long size;
    // 视频时长（以毫秒为单位）
    private long duration;
    // 视频文件的 MIME 类型
    private String mimeType;
    // 视频文件添加到媒体库的日期（以自 Unix 纪元以来的秒数表示）
    private long dateAdded;
    // 视频文件的修改日期（以自 Unix 纪元以来的秒数表示）
    private long dateModified;
    // 视频宽度（以像素为单位）
    private int width;
    // 视频高度（以像素为单位）
    private int height;
    // 视频帧率（每秒帧数）
    private float frameRate;
    // 视频比特率（以比特每秒为单位）
    private int bitRate;

    public VideoMetaFile()
    {

    }
    public VideoMetaFile(String name, String title, String filePathName, long size, long duration, String mimeType, long dateAdded, long dateModified, int width, int height, float frameRate, int bitRate) {
        this.name = name;
        this.title = title;
        this.filePathName = filePathName;
        this.size = size;
        this.duration = duration;
        this.mimeType = mimeType;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.bitRate = bitRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getFilePathName() {
        return filePathName;
    }

    public void setFilePathName(String filePathName) {
        this.filePathName = filePathName;
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

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    // 获取视频文件中的一帧
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Bitmap getFrameAtTime(long timeUs,String filePathName) {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(filePathName);
            return retriever.getFrameAtTime(timeUs);
        } catch (Exception e) {
            ///e.printStackTrace();
            return null;
        }
    }

}
