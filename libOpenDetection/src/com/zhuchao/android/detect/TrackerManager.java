package com.zhuchao.android.detect;

import android.graphics.RectF;
import org.opencv.core.Mat;
import org.opencv.core.Rect2d;

import java.util.ArrayList;
import java.util.List;

public class TrackerManager {
    /*
    private List<Tracker> trackers;
    private List<Rect2d> trackedBoxes;

    public TrackerManager() {
        trackers = new ArrayList<>();
        trackedBoxes = new ArrayList<>();
    }

    public void initTrackers(Mat frame, List<YOLOModel.DetectionResult> detections) {
        trackers.clear();
        trackedBoxes.clear();
        for (YOLOModel.DetectionResult detection : detections) {
            RectF rect = detection.getRect();
            Rect2d rect2d = new Rect2d(rect.left, rect.top, rect.width(), rect.height());
            Tracker tracker = TrackerKCF.create();
            tracker.init(frame, rect2d);
            trackers.add(tracker);
            trackedBoxes.add(rect2d);
        }
    }

    public List<Rect2d> updateTrackers(Mat frame) {
        List<Rect2d> newTrackedBoxes = new ArrayList<>();
        for (int i = 0; i < trackers.size(); i++) {
            Tracker tracker = trackers.get(i);
            Rect2d trackedBox = trackedBoxes.get(i);
            if (tracker.update(frame, trackedBox)) {
                newTrackedBoxes.add(trackedBox);
            } else {
                newTrackedBoxes.add(new Rect2d());
            }
        }
        trackedBoxes = newTrackedBoxes;
        return trackedBoxes;
    }*/
}