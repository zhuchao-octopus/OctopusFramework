package com.zhuchao.android.okan;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zhuchao.android.callbackevent.PlayerCallback;
import com.zhuchao.android.playsession.OPlayerSessionManager;
import com.zhuchao.android.playsession.SessionCompleteCallback;
import com.zhuchao.android.video.OMedia;

import java.util.List;


public class MainActivity extends Activity implements SessionCompleteCallback, PlayerCallback {
    private RecyclerView mVideoListInDetailRv;
    private VideoListAdapter mVideoListInDetailAdapter;
    private List<OMedia> mMediaList = null;
    private int mCurrentVideoPosition = -1;

    private OPlayerSessionManager mSessionManager;// = new OPlayerSessionManager(this.getApplicationContext(),null,null);

    private TextView mTextMessage;
    private SurfaceView mSurfaceView;
    private OMedia oMedia;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:

                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //StatusBarUtil.mTransparentStatusBar(MainActivity.this);
        mVideoListInDetailRv = (RecyclerView) findViewById(R.id.media_list_rv);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        mSurfaceView = findViewById(R.id.surfaceView);
        //mTextMessage = findViewById(R.id.message);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //OMedia = new OMedia("http://ivi.bupt.edu.cn/hls/cctv10.m3u8",null,null);
        //OMedia.with(this).playInto(mSurfaceView);


        mVideoListInDetailRv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        mVideoListInDetailRv.setLayoutManager(llm);
        mVideoListInDetailAdapter = new VideoListAdapter(mVideoListInDetailRv, VideoListAdapter.LIST_IN_DETAIL);
        mVideoListInDetailRv.setAdapter(mVideoListInDetailAdapter);
        mVideoListInDetailAdapter.setData(mMediaList);
        mVideoListInDetailAdapter.notifyDataSetChanged();
        mVideoListInDetailRv.setFocusable(false);

        mSessionManager = new OPlayerSessionManager(this,null,null);
    }

    public void initVideoListt() {

        //if(mSessionManager.isInitComplete()) {
            //mSessions = mSessionManager.getmSessions();//获取板块分类集合
            //mMediaList = ;//从集合中得到直播视频列表
            //mMediaList = mSessionManager.getAllVideoList();
        mVideoListInDetailRv.setAdapter(mVideoListInDetailAdapter);
        mVideoListInDetailAdapter.setData(mMediaList);
        mVideoListInDetailAdapter.notifyDataSetChanged();
        //}
    }

    private void videoSelected(int position) {
        OMedia OMedia = null;
        if (position >= 0 && position < mMediaList.size()) {
            OMedia = mMediaList.get(position);
            OMedia.with(this).playOn(mSurfaceView).callback(this);
        } else {
            return;
        }
        mCurrentVideoPosition = position;
    }

    @Override
    public void OnSessionComplete(int sessionId, String result) {
        initVideoListt();
    }

    @Override
    public void OnEventCallBack(int EventType, long TimeChanged, long LengthChanged, float PositionChanged, int outCount, int ChangedType, int ChangedID, float Buffering, long Length) {

    }

    // 自定义RecyclerView的数据Adapter
    class VideoListAdapter extends RecyclerView.Adapter {
        private static final int LIST_IN_PLAYER = 1;
        private static final int LIST_IN_DETAIL = 2;
        private int mListType;
        private List<OMedia> mData;
        private RecyclerView mRecyclerView;

        public VideoListAdapter(RecyclerView view, int type) {
            this.mRecyclerView = view;
            this.mListType = type;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (mListType) {
                case LIST_IN_DETAIL:
                    view = LayoutInflater.from(MainActivity.this)
                            .inflate(R.layout.item_video_in_detail, parent, false);
                    break;
                default:
                    view = LayoutInflater.from(MainActivity.this)
                            .inflate(R.layout.item_video_in_player, parent, false);
                    break;
            }
            VideoViewHolder viewHolder = new VideoViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final VideoViewHolder myHolder = (VideoViewHolder) holder;
            OMedia itemData = mData.get(position);
            if (myHolder.mItemTv != null) {
                myHolder.mItemTv.setText(itemData.getMovie().getMovieName());
            }
            boolean isSelected = position == mCurrentVideoPosition;
            myHolder.itemView.setSelected(isSelected);
            myHolder.mItemTv.setSelected(isSelected);
            myHolder.mTextPaint.setFakeBoldText(isSelected);
            // 为RecyclerView的item view设计事件监听机制
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    videoSelected(position);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void setData(List<OMedia> data) {
            this.mData = data;
        }
    }

    // 自定义的ViewHolder，持有每个Item的的所有界面元素
    class VideoViewHolder extends RecyclerView.ViewHolder {
        public TextView mItemTv;
        public TextPaint mTextPaint;

        public VideoViewHolder(View view) {
            super(view);
            mItemTv = (TextView) view.findViewById(R.id.rv_video_item_text);
            mTextPaint = mItemTv.getPaint();
        }
    }
}
