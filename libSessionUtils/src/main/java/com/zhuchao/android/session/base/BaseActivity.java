package com.zhuchao.android.session.base;

import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_AIDL_PACKAGE_NAME;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_CAR_CLIENT;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_CAR_SERVICE;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_CAR_CLIENT;
import static com.zhuchao.android.fbase.MessageEvent.MESSAGE_EVENT_OCTOPUS_CAR_SERVICE;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.session.Cabinet;
import com.zhuchao.android.session.MApplication;

import java.util.Map;

public class BaseActivity extends AppCompatActivity {
    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;

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
        if (!hasPermissions()) {
            requestPermissions();
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

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        requestMultiplePermissionsLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    private void showPermissionsDeniedDialog() {
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

    public void openLocalActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        ///intent.putExtra("EXTRA_DATA", "Some Data");  // 传递额外的数据
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            MMLog.e(getLocalClassName(), String.valueOf(e));
        }
    }

    public void openLocalActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        ///intent.putExtra("EXTRA_DATA", "Some Data");  // 传递额外的数据
        intent.putExtras(bundle);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            MMLog.e(getLocalClassName(), String.valueOf(e));
        }
    }

    public void openActivity(String targetPackageName, String targetActivityClassName) {
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

}
