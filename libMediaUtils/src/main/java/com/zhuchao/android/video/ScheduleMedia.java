package com.zhuchao.android.video;

import com.zhuchao.android.libfileutils.DateTimeUtils;

public class ScheduleMedia extends OMedia {
    private int mID=0;
    private String mStartDate = null;
    private String mEndDate = null;
    private String mPlayTime = null;
    private String mStopTime = null;
    private String mLast =null;
    private int mStatus = 0;

    public ScheduleMedia(Movie mMovie) {
        super(mMovie);
    }

    public ScheduleMedia(String VideoPath) {
        super(VideoPath);
    }

    public ScheduleMedia(int ID, String VideoPath, String StartDateTime, String EndDateTime, String PlayTime, String StopTime, String Last, int Status) {
        super(VideoPath);
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
        a = DateTimeUtils.compare_date(DateTimeUtils.getCurrentDateStr("yyyy-MM-dd"), mStartDate,"yyyy-MM-dd");
        b = DateTimeUtils.compare_date(DateTimeUtils.getCurrentDateStr("yyyy-MM-dd"), mEndDate,"yyyy-MM-dd");

        return (a >= 0) && (b <= 0);
    }


    public boolean isPlayScheduled() {
        int a, b = 0;
        if (mPlayTime == null) return false;

        a = DateTimeUtils.compare_date(DateTimeUtils.getCurrentDateStr("HH:mm"), mPlayTime,"HH:mm");
        b = DateTimeUtils.compare_date(DateTimeUtils.getCurrentDateStr("HH:mm"), mStopTime,"HH:mm");

        return (a >= 0) && (b < 0);
    }

    public boolean isStopScheduled() {
        if (mStopTime == null) return false;
        return DateTimeUtils.compare_date(DateTimeUtils.getCurrentDateStr("HH:mm"), mStopTime, "HH:mm") == 0;
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
      return DateTimeUtils.getDateData(Data,"yy");
    }
    public int getMonth(String Data)
    {
        return DateTimeUtils.getDateData(Data,"MM");
    }
    public int getDay(String Data)
    {
        return DateTimeUtils.getDateData(Data,"dd");
    }
    public int getHH(String Data)
    {
        int hh = DateTimeUtils.getDateData(Data,"HH");
        return hh;
    }
    public int getmm(String Data)
    {
        int mm = DateTimeUtils.getDateData(Data,"mm");
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

