package com.zhuchao.android.session.base;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;
import com.zhuchao.android.fbase.eventinterface.PermissionListener;
import com.zhuchao.android.fbase.eventinterface.TCourierEventListener;
import com.zhuchao.android.net.NetworkInformation;
import com.zhuchao.android.net.TNetUtils;
import com.zhuchao.android.session.Cabinet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseActivity extends AppCompatActivity implements TNetUtils.NetworkStatusListener,TCourierEventListener {

    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;
    private static final String ACTION_SHOW_STATUS_BAR = "android.intent.action.ACTION_SHOW_STATUS_BAR";
    private static final String ACTION_HIDE_STATUS_BAR = "android.intent.action.ACTION_HIDE_STATUS_BAR";
    private static final int REQUEST_PERMISSION_CODE = 1024;
    private PermissionListener mPermissionListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestMultiplePermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                Boolean readExternalStorageGranted = result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
                Boolean writeExternalStorageGranted = result.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false);

                if (readExternalStorageGranted != null && readExternalStorageGranted && writeExternalStorageGranted != null && writeExternalStorageGranted) {
                    ///Toast.makeText(BaseActivity.this, "Permissions Granted", Toast.LENGTH_SHORT).show();
                    /// Permission is granted, proceed with your logic
                    MMLog.d("BaseActivity", "Permission is granted, proceed with your logic");
                } else {
                    ///Toast.makeText(BaseActivity.this, "Permissions Denied", Toast.LENGTH_SHORT).show();
                    /// Permission is denied, show a message or handle it accordingly
                    ///showPermissionsDeniedDialog();
                    MMLog.e("BaseActivity", "Permission is denied, show a message or handle it accordingly");
                }
            }
        });
        if (!hasStoragePermissions()) {
            requestStoragePermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cabinet.getEventBus().registerEventObserver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Cabinet.getEventBus().unRegisterEventObserver(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void startLocalActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        ///intent.putExtra("EXTRA_DATA", "Some Data");  // 传递额外的数据
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            MMLog.e(getLocalClassName(), String.valueOf(e));
        }
    }

    public void startLocalActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        ///intent.putExtra("EXTRA_DATA", "Some Data");  // 传递额外的数据
        intent.putExtras(bundle);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            MMLog.e(getLocalClassName(), String.valueOf(e));
        }
    }

    public void startRemoteActivity(String targetPackageName, String targetActivityClassName) {
        Intent intent = new Intent();
        ComponentName cn = new ComponentName(targetPackageName, targetActivityClassName);
        intent.setComponent(cn);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            MMLog.e(getLocalClassName(), String.valueOf(e));
        }
    }

    public void SendMessage(String action, String packageName, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) intent.putExtras(bundle);
        if (FileUtils.NotEmptyString(packageName)) intent.setPackage(packageName);
        sendBroadcast(intent);
    }

    public void setColor(TextView textView, int colorResId) {
        textView.setTextColor(ContextCompat.getColor(this, colorResId));
    }

    public void replaceFragment(@IdRes int containerViewId, @NonNull Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment);
        fragmentTransaction.commit();
    }

    public void hideStatusBar() {
        SendMessage(ACTION_HIDE_STATUS_BAR, null, null);
    }

    public void showStatusBar() {
        SendMessage(ACTION_SHOW_STATUS_BAR, null, null);
    }

    public void switchFragment(@IdRes int containerViewId, @NonNull Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // 附加新的Fragment，如果它已添加过
        if (fragment.isAdded()) {
            fragmentTransaction.attach(fragment);
        } else {
            fragmentTransaction.add(containerViewId, fragment);
        }
        fragmentTransaction.commit();
    }

    public void requestRuntimePermission(Context context, String[] permissions, PermissionListener permissionListener) {
        mPermissionListener = permissionListener;
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                if (!permissionList.contains(permission)) {
                    permissionList.add(permission);
                }
            }
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) context, permissionList.toArray(new String[0]), REQUEST_PERMISSION_CODE);
        } else {
            if (mPermissionListener != null) {
                mPermissionListener.onGranted();  //权限都被授予了回调
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> deniedPermissionList = new ArrayList<>();
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        String permission = permissions[i];
                        int grantResult = grantResults[i];
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            if (!deniedPermissionList.contains(permission)) {
                                deniedPermissionList.add(permission);
                            }
                        }
                    }
                    if (deniedPermissionList.isEmpty()) {
                        if (mPermissionListener != null)
                            mPermissionListener.onGranted();

                    } else if (mPermissionListener != null) {
                        mPermissionListener.onDenied(deniedPermissionList);
                    }
                }
                break;
        }
    }

    public boolean hasStoragePermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStoragePermissions() {
        requestMultiplePermissionsLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    public void showPermissionsDeniedDialog() {
        new AlertDialog.Builder(this).setTitle("Permissions Required").setMessage("This app needs storage permissions to function correctly. Please grant the required permissions in the app settings.").setPositiveButton("Go to Settings", (dialog, which) -> {
            // Redirect to app settings
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }).setNegativeButton("Cancel", (dialog, which) -> {
            // User chose to cancel
            Toast.makeText(BaseActivity.this, "Permissions denied. The app may not function correctly.", Toast.LENGTH_SHORT).show();
        }).show();
    }

    @Override
    public void onNetStatusChanged(NetworkInformation networkInformation) {

    }

    @Override
    public boolean onCourierEvent(EventCourierInterface eventCourier) {
        return false;
    }
}
