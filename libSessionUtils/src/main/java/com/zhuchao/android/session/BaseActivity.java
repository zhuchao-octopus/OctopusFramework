package com.zhuchao.android.session;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
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
}
