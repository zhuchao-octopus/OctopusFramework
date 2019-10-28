package com.zhuchao.android.shapeloading;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.zhuchao.android.dialog.R;

public class ShapeLoadingDialog extends Dialog{

    private LoadingView mLoadingView;

    private Builder mBuilder;

    public ShapeLoadingDialog(Builder builder) {
        super(builder.mContext, R.style.custom_dialog);
        mBuilder = builder;
        setCancelable(mBuilder.mCancelable);
        setCanceledOnTouchOutside(mBuilder.mCanceledOnTouchOutside);
        this.getWindow().setDimAmount(0f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog);

        mLoadingView = (LoadingView) findViewById(R.id.loadView);

        mLoadingView.setDelay(mBuilder.mDelay);
        mLoadingView.setLoadingText(mBuilder.mLoadText);

        setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mLoadingView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void show() {
        super.show();
        mLoadingView.setVisibility(View.VISIBLE);
    }

    public Builder getBuilder() {
        return mBuilder;
    }

    public static class Builder{

        private Context mContext;

        private int mDelay = 80;

        private CharSequence mLoadText;

        private boolean mCancelable = false;

        private boolean mCanceledOnTouchOutside = false;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder delay(int delay) {
            mDelay = delay;
            return this;
        }

        public Builder loadText(CharSequence loadText) {
            mLoadText = loadText;
            return this;
        }

        public Builder loadText(int resId) {
            mLoadText = mContext.getString(resId);
            return this;
        }

        public Builder cancelable(boolean cancelable) {
            mCancelable = cancelable;
            mCanceledOnTouchOutside = cancelable;
            return this;
        }

        public Builder canceledOnTouchOutside(boolean canceledOnTouchOutside) {
            mCanceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        public ShapeLoadingDialog build(){
            return new ShapeLoadingDialog(this);
        }

        public ShapeLoadingDialog show(){
            ShapeLoadingDialog dialog = build();
            dialog.show();
            return dialog;
        }
    }
}
