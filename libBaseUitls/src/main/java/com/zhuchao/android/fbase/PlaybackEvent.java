package com.zhuchao.android.fbase;

public class PlaybackEvent {
      /*
        from  public static class Event extends AbstractVLCEvent
        public static final int MediaChanged = 256;
        public static final int Opening = 258;
        public static final int Buffering = 259;
        public static final int Playing = 260;
        public static final int Paused = 261;
        public static final int Stopped = 262;
        public static final int EndReached = 265;
        public static final int EncounteredError = 266;
        public static final int TimeChanged = 267;
        public static final int PositionChanged = 268;
        public static final int SeekableChanged = 269;
        public static final int PausableChanged = 270;
        public static final int LengthChanged = 273;
        public static final int Vout = 274;
        public static final int ESAdded = 276;
        public static final int ESDeleted = 277;
        public static final int ESSelected = 278;
        public static final int RecordChanged = 286;
     */
    ///////////////////////////////////////////////////////////
    //Event code
    public static final int MediaChanged = 256;
    public static final int Opening = 258;
    public static final int Buffering = 259;
    public static final int Playing = 260;
    public static final int Paused = 261;
    public static final int Stopped = 262;
    public static final int EndReached = 265;
    public static final int EncounteredError = 266;
    public static final int TimeChanged = 267;
    public static final int PositionChanged = 268;
    public static final int SeekableChanged = 269;
    public static final int PausableChanged = 270;
    public static final int LengthChanged = 273;
    public static final int Vout = 274;
    public static final int ESAdded = 276;
    public static final int ESDeleted = 277;
    public static final int ESSelected = 278;
    //////////////////////////////////////////////////////////////////
    //Event Type
    public static final int Status_NothingIdle = 0;
    public static final int Status_Opening = 1;//打开文件
    public static final int Status_Buffering = 2;//异步开始准备
    public static final int Status_Playing = 3;
    public static final int Status_Paused = 4;
    public static final int Status_Stopped = 5;
    public static final int Status_Error = 6;
    public static final int Status_Ended = 7;

    public static final int Status_Prev = 8;
    public static final int Status_Next = 9;
    public static final int Status_Changed = 10;

    public static final int Status_HasPrepared = 20;
    public static final int Status_FreeDoNothing = 21;

    public static final int Status_SurfaceChanged = 100;
    public static final int Status_SurfaceCreated = 101;
    public static final int Status_SurfaceDestroyed = 102;
    public static final int Status_VideoSizeChanged = 103;


    public static final int Status_INTERNAL = 200;
    public static final int Status_SEEKING = 201;
    public static final int Status_RESETING = 202;
}
