package com.zhuchao.android.session;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Cabinet.getEventBus().registerEventObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Cabinet.getEventBus().unRegisterEventObserver(this);
    }
}
