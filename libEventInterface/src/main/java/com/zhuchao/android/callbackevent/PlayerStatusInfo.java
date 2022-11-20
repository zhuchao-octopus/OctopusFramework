package com.zhuchao.android.callbackevent;

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
    private int lastError;

    public int getEventType() {
        return EventType;
    }

    public long getTimeChanged() {
        return TimeChanged;
    }

    public long getLengthChanged() {
        return LengthChanged;
    }

    public float getPositionChanged() {
        return PositionChanged;
    }

    public int getOutCount() {
        return OutCount;
    }

    public int getChangedType() {
        return ChangedType;
    }

    public int getChangedID() {
        return ChangedID;
    }

    public boolean isSurfacePrepared() {
        return surfacePrepared;
    }

    public boolean isSourcePrepared() {
        return sourcePrepared;
    }

    public int getSurfaceW() {
        return surfaceW;
    }

    public int getSurfaceH() {
        return surfaceH;
    }

    public int getVideoW() {
        return videoW;
    }

    public int getVideoH() {
        return videoH;
    }

    public int getVolume() {
        return volume;
    }

    public void setEventType(int eventType) {
        EventType = eventType;
    }

    public void setTimeChanged(long timeChanged) {
        TimeChanged = timeChanged;
    }

    public void setLengthChanged(long lengthChanged) {
        LengthChanged = lengthChanged;
    }

    public void setPositionChanged(float positionChanged) {
        PositionChanged = positionChanged;
    }

    public void setOutCount(int outCount) {
        OutCount = outCount;
    }

    public void setChangedType(int changedType) {
        ChangedType = changedType;
    }

    public void setChangedID(int changedID) {
        ChangedID = changedID;
    }

    public void setSurfacePrepared(boolean surfacePrepared) {
        this.surfacePrepared = surfacePrepared;
    }

    public void setSourcePrepared(boolean sourcePrepared) {
        this.sourcePrepared = sourcePrepared;
    }

    public void setSurfaceW(int surfaceW) {
        this.surfaceW = surfaceW;
    }

    public void setSurfaceH(int surfaceH) {
        this.surfaceH = surfaceH;
    }

    public void setVideoW(int videoW) {
        this.videoW = videoW;
    }

    public void setVideoH(int videoH) {
        this.videoH = videoH;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public float getBuffering() {
        return buffering;
    }

    public void setBuffering(float buffering) {
        this.buffering = buffering;
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
        return str;
    }
}
