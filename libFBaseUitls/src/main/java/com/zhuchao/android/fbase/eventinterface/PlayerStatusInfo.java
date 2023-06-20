package com.zhuchao.android.fbase.eventinterface;

public class PlayerStatusInfo {
    private Object obj;
    private int eventType;
    private long timeChanged;
    private long lengthChanged;
    private float positionChanged;
    private float buffering;
    private int outCount;
    private int changedType;
    private int changedID;
    private boolean surfacePrepared;
    private boolean sourcePrepared;
    private int surfaceW;
    private int surfaceH;
    private int videoW;
    private int videoH;
    private int volume;
    private float playRate;
    private long length;

    private int lastError;

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public long getTimeChanged() {
        return timeChanged;
    }

    public void setTimeChanged(long timeChanged) {
        this.timeChanged = timeChanged;
    }

    public long getLengthChanged() {
        return lengthChanged;
    }

    public void setLengthChanged(long lengthChanged) {
        this.lengthChanged = lengthChanged;
    }

    public float getPositionChanged() {
        return positionChanged;
    }

    public void setPositionChanged(float positionChanged) {
        this.positionChanged = positionChanged;
    }

    public float getBuffering() {
        return buffering;
    }

    public void setBuffering(float buffering) {
        this.buffering = buffering;
    }

    public int getOutCount() {
        return outCount;
    }

    public void setOutCount(int outCount) {
        this.outCount = outCount;
    }

    public int getChangedType() {
        return changedType;
    }

    public void setChangedType(int changedType) {
        this.changedType = changedType;
    }

    public int getChangedID() {
        return changedID;
    }

    public void setChangedID(int changedID) {
        this.changedID = changedID;
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

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String toString() {
        String str = "EventType = " + eventType;
        str += " TimeChanged = " + timeChanged;
        str += " PositionChanged = " + positionChanged;
        str += " buffering = " + buffering;
        str += " LengthChanged = " + lengthChanged;
        str += " Length = " + length;

        str += " OutCount = " + outCount;
        str += " ChangedID = " + changedID;
        str += " ChangedType = " + changedType;

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
