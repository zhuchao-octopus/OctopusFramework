package com.zhuchao.android.fbase.bean;

import android.graphics.Bitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderBean {
    private final int MAX_COUNT_FOR_FRAME = 20;
    private FolderBean mParent;
    private String mName;
    private String mPathName;
    private final List<String> mFilesList;
    private long mSize;
    private boolean mIsFileBean;
    private Bitmap mVideoFileFrame;

    public FolderBean(String name, String pathName) {
        this.mName = name;
        this.mPathName = pathName;
        this.mFilesList = new ArrayList<>();
        this.mIsFileBean = false;
    }

    public void add(String filePath) {
        mFilesList.add(filePath);
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getPathName() {
        return mPathName;
    }

    public void setPath(String mPath) {
        this.mPathName = mPath;
    }

    public int getSubItemCount() {
        return mFilesList.size();
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long mSize) {
        this.mSize = mSize;
    }

    public boolean isFileBean() {
        return mIsFileBean;
    }

    public void setIsFileBean(boolean isFileBean) {
        this.mIsFileBean = isFileBean;
    }

    public List<String> getFileList() {
        return mFilesList;
    }

    public Bitmap getVideoFileFrame() {
        return mVideoFileFrame;
    }

    public void setVideoFileFrame(Bitmap mVideoFileFrame) {
        this.mVideoFileFrame = mVideoFileFrame;
    }

    public FolderBean getParent() {
        return mParent;
    }

    public void setParent(FolderBean mParent) {
        this.mParent = mParent;
    }

    public List<FolderBean> fileListToFolderBean() {
        List<FolderBean> list = new ArrayList<>();
        if (getSubItemCount() > 0) list.add(new FolderBean("..", getPathName()));
        for (String str : mFilesList) {
            File file = new File(str);
            if (file.isFile() && file.exists()) {
                FolderBean folderBean = new FolderBean(file.getName(), str);
                folderBean.setIsFileBean(true);
                folderBean.setParent(this);
                /*if (MediaFile.isVideoFile(str) && (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q))
                 {
                    if(getSubItemCount() < MAX_COUNT_FOR_FRAME) {
                        Bitmap bitmap = FileUtils.getVideoFrame(0, str);
                        setVideoFileFrame(bitmap);
                    }
                }*/
                list.add(folderBean);
            }
        }
        return list;
    }
}
