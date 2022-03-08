package com.zhuchao.android.libfileutils.bean;

import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

//import androidx.annotation.NonNull;

public class AppInfor  {

    /**
     * 程序的名字
     */
    private String Name;
    /**
     * 包名
     */
    private String PackageName;


    /**
     * 图片的icon
     */
    private Drawable Icon;
    /**
     * 程序大小
     */
    private long Size;

    /**
     * 表示到底是用户app还是系统app
     * 如果表示为true 就是用户app
     * 如果是false表示系统app
     */
    private boolean UserApp;

    /**
     * 放置的位置
     */
    private boolean Rom;
    private String SourceDir;

    private ApplicationInfo mApplicationInfo;
    private long VersionCode = 0;


    public AppInfor() {}

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPackageName() {
        return PackageName;
    }

    public void setPackageName(String packageName) {
        PackageName = packageName;
    }

    public Drawable getIcon() {
        if (this == null) return null;
        return Icon;
    }

    public void setIcon(Drawable icon) {
        this.Icon = icon;
    }

    public long getSize() {
        return Size;
    }

    public void setSize(long size) {
        Size = size;
    }

    public boolean isUserApp() {
        return UserApp;
    }

    public void setUserApp(boolean userApp) {
        UserApp = userApp;
    }

    public boolean isRom() {
        return Rom;
    }

    public void setRom(boolean rom) {
        Rom = rom;
    }

    public ApplicationInfo getApplicationInfo() {
        return mApplicationInfo;
    }

    public void setApplicationInfo(ApplicationInfo mApplicationInfo) {
        this.mApplicationInfo = mApplicationInfo;
    }

    public long getVersionCode() {
        return VersionCode;
    }

    public void setVersionCode(long versionCode) {
        VersionCode = versionCode;
    }

    public String getSourceDir() {
        return SourceDir;
    }

    public void setSourceDir(String sourceDir) {
        SourceDir = sourceDir;
    }

    @Override
    public String toString() {
        String str = "Name=" + Name +
                ",Version=" + VersionCode +
                ",PackageName=" + PackageName +
                ",Ico=" + Icon +
                ",Size=" + Size +
                ",User=" + UserApp +
                ",Rom=" + Rom +
                ",Dir=" +SourceDir;
        return str;
    }

}
