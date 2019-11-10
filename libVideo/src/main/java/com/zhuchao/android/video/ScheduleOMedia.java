package com.zhuchao.android.video;

import com.zhuchao.android.callbackevent.PlayerCallBackInterface;
import com.zhuchao.android.netutil.TimeDateUtils;

import java.util.ArrayList;

public class ScheduleOMedia extends OMedia {
    private int mID=0;
    private String mStartDate = null;
    private String mEndDate = null;
    private String mPlayTime = null;
    private String mStopTime = null;
    private String mLast =null;
    private int mStatus = 0;

    public ScheduleOMedia(Movie mMovie) {
        super(mMovie);
    }

    public ScheduleOMedia(ArrayList<String> Options, PlayerCallBackInterface Callback) {
        super(Options, Callback);
    }

    public ScheduleOMedia(Movie mMovie, ArrayList<String> mOptions, PlayerCallBackInterface mCallback) {
        super(mMovie, mOptions, mCallback);
    }

    public ScheduleOMedia(String VideoPath, ArrayList<String> Options, PlayerCallBackInterface Callback) {
        super(VideoPath, Options, Callback);
    }

    public ScheduleOMedia(int ID, String VideoPath, String StartDateTime, String EndDateTime, String PlayTime, String StopTime, String Last, int Status, ArrayList<String> Options, PlayerCallBackInterface Callback) {
        super(VideoPath, Options, Callback);
        this.mID = ID;
        this.mStartDate = StartDateTime;
        this.mEndDate = EndDateTime;
        this.mPlayTime = PlayTime;
        this.mStopTime = StopTime;
        this.mLast = Last;
        this.mStatus = Status;
    }

    public boolean isInScheduleDate() {
        int a, b = 0;

        if (mStartDate == null) return false;
        a = TimeDateUtils.compare_date(TimeDateUtils.getCurrentDateStr("yyyy-MM-dd"), mStartDate,"yyyy-MM-dd");
        b = TimeDateUtils.compare_date(TimeDateUtils.getCurrentDateStr("yyyy-MM-dd"), mEndDate,"yyyy-MM-dd");

        if ((a >= 0) && ( b <= 0))
            return true;
        else
            return false;
    }


    public boolean isPlayScheduled() {
        int a, b = 0;
        if (mPlayTime == null) return false;

        a = TimeDateUtils.compare_date(TimeDateUtils.getCurrentDateStr("HH:mm"), mPlayTime,"HH:mm");
        b = TimeDateUtils.compare_date(TimeDateUtils.getCurrentDateStr("HH:mm"), mStopTime,"HH:mm");

        if ((a >= 0) && ( b < 0))
            return true;
        else
            return false;
    }

    public boolean isStopScheduled() {
        if (mStopTime == null) return false;
        if (TimeDateUtils.compare_date(TimeDateUtils.getCurrentDateStr("HH:mm"), mStopTime,"HH:mm") == 0)
            return true;
        else
            return false;
    }

    public int getID() {
        return mID;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int mStatus) {
        this.mStatus = mStatus;
    }
    public int getYear(String Data)
    {
      return TimeDateUtils.getDateData(Data,"yy");
    }
    public int getMonth(String Data)
    {
        return TimeDateUtils.getDateData(Data,"MM");
    }
    public int getDay(String Data)
    {
        return TimeDateUtils.getDateData(Data,"dd");
    }
    public int getHH(String Data)
    {
        int hh = TimeDateUtils.getDateData(Data,"HH");
        return hh;
    }
    public int getmm(String Data)
    {
        int mm = TimeDateUtils.getDateData(Data,"mm");
        return mm;
    }

    public String getmStartDate() {
        return mStartDate;
    }

    public String getmEndDate() {
        return mEndDate;
    }

    public String getmPlayTime() {
        return mPlayTime;
    }

    public String getmStopTime() {
        return mStopTime;
    }
}

