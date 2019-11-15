package com.zhuchao.android.libfilemanager;


import com.zhuchao.android.libfilemanager.bean.AppInfor;

public interface AppsChangedCallback {

    void OnAppsChanged(String Action, AppInfor appInfor);
}
