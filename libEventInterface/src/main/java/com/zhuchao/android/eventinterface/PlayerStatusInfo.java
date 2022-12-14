package com.zhuchao.android.eventinterface;

public class PlayerStatusInfo {
    private int EventType;
    private long TimeChanged;
    private long LengthChanged;
    private float PositionChanged;
    private float buffering;
    private int OutCount;
    private int ChangedType;
    private int ChangedID;
    private boolean surfacePrepared;
    private boolean sourcePrepared;
    private int surfaceW;
    private int surfaceH;
    private int videoW;
    private int videoH;
    private int volume;
    private float playRate;


    private int lastError;

    public int getEventType() {
        return EventType;
    }

    public void setEventType(int eventType) {
        EventType = eventType;
    }

    public long getTimeChanged() {
        return TimeChanged;
    }

    public void setTimeChanged(long timeChanged) {
        TimeChanged = timeChanged;
    }

    public long getLengthChanged() {
        return LengthChanged;
    }

    public void setLengthChanged(long lengthChanged) {
        LengthChanged = lengthChanged;
    }

    public float getPositionChanged() {
        return PositionChanged;
    }

    public void setPositionChanged(float positionChanged) {
        PositionChanged = positionChanged;
    }

    public float getBuffering() {
        return buffering;
    }

    public void setBuffering(float buffering) {
        this.buffering = buffering;
    }

    public int getOutCount() {
        return OutCount;
    }

    public void setOutCount(int outCount) {
        OutCount = outCount;
    }

    public int getChangedType() {
        return ChangedType;
    }

    public void setChangedType(int changedType) {
        ChangedType = changedType;
    }

    public int getChangedID() {
        return ChangedID;
    }

    public void setChangedID(int changedID) {
        ChangedID = changedID;
    }

    public boolean isSurfacePrepared() {
        return surfacePrepared;
    }

    public void setSurfacePrepared(boolean surfacePrepared) {
        this.surfacePrepared = surfacePrepared;
    }

    public boolean isSourcePrepared() {
        return sourcePrepared;
    }

    public void setSourcePrepared(boolean sourcePrepared) {
        this.sourcePrepared = sourcePrepared;
    }

    public int getSurfaceW() {
        return surfaceW;
    }

    public void setSurfaceW(int surfaceW) {
        this.surfaceW = surfaceW;
    }

    public int getSurfaceH() {
        return surfaceH;
    }

    public void setSurfaceH(int surfaceH) {
        this.surfaceH = surfaceH;
    }

    public int getVideoW() {
        return videoW;
    }

    public void setVideoW(int videoW) {
        this.videoW = videoW;
    }

    public int getVideoH() {
        return videoH;
    }

    public void setVideoH(int videoH) {
        this.videoH = videoH;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public float getPlayRate() {
        return playRate;
    }

    public void setPlayRate(float playRate) {
        this.playRate = playRate;
    }

    public int getLastError() {
        return lastError;
    }

    public void setLastError(int lastError) {
        this.lastError = lastError;
    }

    public String toString() {
        String str = "EventType = " + EventType;
        str += " TimeChanged = " + TimeChanged;
        str += " PositionChanged = " + PositionChanged;
        str += " buffering = " + buffering;
        str += " LengthChanged = " + LengthChanged;

        str += " OutCount = " + OutCount;
        str += " ChangedID = " + ChangedID;
        str += " ChangedType = " + ChangedType;

        str += " surfaceStatus = " + surfacePrepared;
        str += " sourceStatus = " + sourcePrepared;
        str += " surfaceW = " + surfaceW;
        str += " surfaceH = " + surfaceH;

        str += " videoW = " + videoW;
        str += " videoH = " + videoH;

        str += " volume = " + volume;
        str += " playRate = " + playRate;
        return str;
    }
}
