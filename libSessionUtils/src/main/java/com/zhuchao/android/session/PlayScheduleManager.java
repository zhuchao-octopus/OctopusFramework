package com.zhuchao.android.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.eventinterface.SessionCallback;
import com.zhuchao.android.session.BeanItem.ScheduleVideoBean;
import com.zhuchao.android.session.BeanItem.ScheduleVideoRootBean;
import com.zhuchao.android.video.ScheduleMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayScheduleManager implements SessionCallback {
    private final String TAG = "SchedulePlaySession-->";
    private SessionCallback userSessionCallback = null;//会话回调
    //private ImplementProxy Ilpr = null;//new ImplementProxy();执行代理
    private List<ScheduleMedia> videoList;
    //private CountDownTimer mCountDownTimer;
    private boolean mEnableScheduled = true;
    //private ScheduleMedia mCurrentScheduleVideo =null;
    private Context mContext = null;

    public PlayScheduleManager(Context context, SessionCallback callback) {
        userSessionCallback = callback;
        videoList = new ArrayList<>();
        this.mContext = context;

        new Thread() {
            public void run() {
                try {
                    copySchedulePlay("/storage/0000-006F/", mContext.getCacheDir().getAbsolutePath());
                    copySchedulePlay("/storage/card/", mContext.getCacheDir().getAbsolutePath());
                    copySchedulePlay("/storage/udisk/", mContext.getCacheDir().getAbsolutePath());
                    initFromExternalStorageDirectoryFile();//DownloadCache
                    initFromDirectoryFile(mContext.getCacheDir().getAbsolutePath() + "/");

                    //if (userSessionCallback != null)
                    //    userSessionCallback.OnSessionComplete(DataID.SESSION_TYPE_SCHEDULEPLAYBACK, "SchedulePlaybackSession");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


        //Ilpr = new ImplementProxy(this);
        //Ilpr.performanceUrl(Data.SESSION_TYPE_SCHEDULEPLAYBACK, "http://test.jhzdesign.cn:8005/getPushList");

    }

    public void updateSchedulePlaybackSession() {
        try {
            copySchedulePlay("/storage/0000-006F/", mContext.getCacheDir().getAbsolutePath());
            copySchedulePlay("/storage/card/", mContext.getCacheDir().getAbsolutePath());
            copySchedulePlay("/storage/udisk/", mContext.getCacheDir().getAbsolutePath());
            initFromExternalStorageDirectoryFile();
            initFromDirectoryFile(mContext.getCacheDir().getAbsolutePath() + "/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnSessionComplete(int session_id, String result, int count) {
        int Count = 0;
        if (session_id == DataID.TASK_STATUS_SUCCESS) {
            ScheduleVideoRootBean scheduleVideoRootBean = null;
            try {
                scheduleVideoRootBean = new Gson().fromJson(result, ScheduleVideoRootBean.class);
                if (scheduleVideoRootBean != null) {
                    List<ScheduleVideoBean> data = scheduleVideoRootBean.getData();

                    for (ScheduleVideoBean scheduleVideoBean : data) {
                        ScheduleMedia scheduleVideo = new ScheduleMedia(Count, scheduleVideoBean.getUrl(), scheduleVideoBean.getStart_date(), scheduleVideoBean.getEnd_date(), scheduleVideoBean.getPlay_time(), scheduleVideoBean.getStop_time(), scheduleVideoBean.getLast(), scheduleVideoBean.getStatus());

                        if (!videoList.contains(scheduleVideo)) {
                            //scheduleVideo.getmOPlayer().setPlayMode(1);
                            videoList.add(scheduleVideo);
                        }

                        //MMLog.log(TAG,scheduleVideo.getmStartDate() +","+ scheduleVideo.getmEndDate()+","+ scheduleVideo.getPlayTime() +","+
                        //        scheduleVideo.getmStopTime()+","+ scheduleVideo.getStatus()+ ","+ scheduleVideo.getMovie().getsUrl());
                        Count++;
                    }

                }

                if (userSessionCallback != null) userSessionCallback.OnSessionComplete(session_id, result, videoList.size());

            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }

        }
    }

    public void initFromExternalStorageDirectoryFile() {
        String path = getExternalStorageDirectoryDownloadCacheDir(mContext);
        List<String> opf = FileUtils.ReadTxtFile(path + "/SchedulePlay.text");
        int Count = 0;
        for (String str : opf) {
            String[] all = str.split(",");
            if (all.length >= 6) {
                ScheduleMedia scheduleVideo = new ScheduleMedia(Count, path + all[0],//scheduleVideoBean.getUrl(),
                        all[1],//scheduleVideoBean.getStart_date(),
                        all[2],//scheduleVideoBean.getEnd_date(),
                        all[3],//scheduleVideoBean.getPlay_time(),
                        all[4],//scheduleVideoBean.getStop_time(),
                        all[5], 1//scheduleVideoBean.getStatus(),
                );
                if (!videoList.contains(scheduleVideo)) {
                    videoList.add(scheduleVideo);
                }
                //MMLog.log(TAG,scheduleVideo.getmStartDate() +","+ scheduleVideo.getmEndDate()+","+ scheduleVideo.getPlayTime() +","+
                //        scheduleVideo.getmStopTime()+","+ scheduleVideo.getStatus()+ ","+ scheduleVideo.getMovie().getsUrl());
                Count++;
            } else if (all.length == 5) {
                ScheduleMedia scheduleVideo = new ScheduleMedia(Count, path + all[0],//scheduleVideoBean.getUrl(),
                        all[1],//scheduleVideoBean.getStart_date(),
                        all[2],//scheduleVideoBean.getEnd_date(),
                        all[3],//scheduleVideoBean.getPlay_time(),
                        all[4],//scheduleVideoBean.getStop_time(),
                        "0", 1//scheduleVideoBean.getStatus(),
                );
                if (!videoList.contains(scheduleVideo)) {
                    videoList.add(scheduleVideo);
                }
                //MMLog.log(TAG,scheduleVideo.getmStartDate() +","+ scheduleVideo.getmEndDate()+","+ scheduleVideo.getPlayTime() +","+
                //        scheduleVideo.getmStopTime()+","+ scheduleVideo.getStatus()+ ","+ scheduleVideo.getMovie().getsUrl());
                Count++;
            }

        }

    }

    public void initFromDirectoryFile(String path) {
        List<String> opf = FileUtils.ReadTxtFile(path + "/SchedulePlay.text");
        int Count = 0;
        for (String str : opf) {
            String[] all = str.split(",");
            ScheduleMedia scheduleVideo = new ScheduleMedia(Count, path + all[0],//scheduleVideoBean.getUrl(),
                    all[1],//scheduleVideoBean.getStart_date(),
                    all[2],//scheduleVideoBean.getEnd_date(),
                    all[3],//scheduleVideoBean.getPlay_time(),
                    all[4],//scheduleVideoBean.getStop_time(),
                    all[5], 1//scheduleVideoBean.getStatus(),
            );

            if (!videoList.contains(scheduleVideo)) {
                videoList.add(scheduleVideo);
            }

            //MMLog.log(TAG,scheduleVideo.getmStartDate() +","+ scheduleVideo.getmEndDate()+","+ scheduleVideo.getPlayTime() +","+
            //        scheduleVideo.getmStopTime()+","+ scheduleVideo.getStatus()+ ","+ scheduleVideo.getMovie().getsUrl());
            Count++;
        }

    }

    public void copySchedulePlay(String SourDir, String DesDir) {
        if (!FileUtils.existFile(SourDir + "/SchedulePlay.text")) return;
        File file = new File(DesDir);
        if (!file.exists()) file.mkdir();

        List<String> npf = FileUtils.ReadTxtFile(SourDir + "/SchedulePlay.text");
        FileUtils.streamCopy(SourDir + "/SchedulePlay.text", DesDir + "/SchedulePlay.text");

        for (String str : npf) {
            String[] all = str.split(",");
            FileUtils.streamCopy(SourDir + "/" + all[0], DesDir + "/" + all[0]);
        }

    }

    public String getExternalStorageDirectoryDownloadCacheDir(Context context) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File sdCard = new File(filePath);//new File("/mnt/media_rw/1716-1E0A/");//
        File file = new File(sdCard, "DownloadCache");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath() + "/";
    }

    public boolean hasScheduleSession() {
        if (videoList == null) return false;
        return videoList.size() > 0;
    }

    public ScheduleMedia pollingScheudulePlay() {
        for (ScheduleMedia scheduleVideo : videoList) {
            if (scheduleVideo.isInScheduleDate()) {
                if (scheduleVideo.isPlayScheduled() && scheduleVideo.getStatus() != 0) {
                    saveDataToSharedPreferences("StartDate", scheduleVideo.getmStartDate());
                    saveDataToSharedPreferences("EndDate", scheduleVideo.getmEndDate());
                    saveDataToSharedPreferences("PlayTime", scheduleVideo.getmPlayTime());
                    saveDataToSharedPreferences("StopTime", scheduleVideo.getmStopTime());
                    saveDataToSharedPreferences("SourceUrl", scheduleVideo.getMovie().getSrcUrl());
                    //DownloadManager.getInstance().with(mContext).downloadFrom(scheduleVideo.getmMovie().getSourceUrl());
                    return scheduleVideo;
                }
            }
        }


        for (ScheduleMedia scheduleVideo : videoList) {
            if (scheduleVideo.isInScheduleDate()) {
                if (scheduleVideo.isStopScheduled()) {
                    return scheduleVideo;
                }
            }
        }
        return null;
    }


    public void saveDataToSharedPreferences(String key, String value) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("oplayertv", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void ClearDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("oplayertv", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public String getDataFromSharedPreferences(String key) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("oplayertv", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public List<ScheduleMedia> getVideoList() {
        return videoList;
    }
}


