package com.zhuchao.android.detect;

import android.graphics.RectF;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.tracking.TrackerCSRT;
import org.opencv.tracking.TrackerKCF;
import org.opencv.video.Tracker;
import org.opencv.video.TrackerMIL;

import java.util.ArrayList;
import java.util.List;

public class TrackerManager {
    private List<Tracker> mTrackers;
    private List<Rect> mTrackedBoxes;
    private String mTrackerType = "TrackerKCF";

    public List<Tracker> getTrackers() {
        return mTrackers;
    }

    public void setTrackers(List<Tracker> mTrackers) {
        this.mTrackers = mTrackers;
    }

    public List<Rect> getTrackedBoxes() {
        return mTrackedBoxes;
    }

    public void setTrackedBoxes(List<Rect> mTrackedBoxes) {
        this.mTrackedBoxes = mTrackedBoxes;
    }

    public String getTrackerType() {
        return mTrackerType;
    }

    public void setTrackerType(String mTrackerType) {
        this.mTrackerType = mTrackerType;
    }

    public TrackerManager(String trackerType) {
        mTrackers = new ArrayList<>();
        mTrackedBoxes = new ArrayList<>();
        mTrackerType = trackerType;
    }

    private Tracker getTracker(String trackerType) {
        Tracker tracker = null;
        switch (trackerType) {
            case "TrackerMedianFlow":
                //tracker = TrackerMedianFlow.create();
                break;
            case "TrackerCSRT":
                tracker = TrackerCSRT.create();
                break;
            case "TrackerKCF":
                tracker = TrackerKCF.create();
                break;
            case "TrackerMOSSE":
                //tracker = TrackerMOSSE.create();
                break;
            case "TrackerTLD":
                //tracker = TrackerTLD.create();
                break;
            case "TrackerMIL":
                tracker = TrackerMIL.create();
                break;
        }
        return tracker;
    }

    public void initTrackers_yolo(Mat frame, List<YOLOModel.DetectionResult> detections) {
        mTrackers.clear();
        mTrackedBoxes.clear();
        for (YOLOModel.DetectionResult detection : detections) {
            RectF rect = detection.getRect();
            Rect rect2d = new Rect((int) rect.left, (int) rect.top, (int) rect.width(), (int) rect.height());
            Tracker tracker = getTracker(mTrackerType);
            tracker.init(frame, rect2d);
            mTrackers.add(tracker);
            mTrackedBoxes.add(rect2d);
        }
    }

    public List<Rect> updateTrackers(Mat frame) {
        List<Rect> newTrackedBoxes = new ArrayList<>();
        for (int i = 0; i < mTrackers.size(); i++)
        {
            Tracker tracker = mTrackers.get(i);
            Rect trackedBox = mTrackedBoxes.get(i);
            if (tracker.update(frame, trackedBox)) {
                newTrackedBoxes.add(trackedBox);
            } else {
                newTrackedBoxes.add(new Rect());
            }
        }
        mTrackedBoxes = newTrackedBoxes;
        return mTrackedBoxes;
    }
}