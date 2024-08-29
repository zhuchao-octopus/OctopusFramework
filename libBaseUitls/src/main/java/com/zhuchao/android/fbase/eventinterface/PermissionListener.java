package com.zhuchao.android.fbase.eventinterface;

import java.util.List;

public interface PermissionListener {
    void onGranted();

    void onDenied(List<String> deniedPermissions);
}
