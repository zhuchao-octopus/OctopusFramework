package com.zhuchao.android.libfileutils;

import com.zhuchao.android.libfileutils.bean.AppInfor;

public interface AppsChangedCallback {

    void OnAppsChanged(String Action, AppInfor appInfor);
}