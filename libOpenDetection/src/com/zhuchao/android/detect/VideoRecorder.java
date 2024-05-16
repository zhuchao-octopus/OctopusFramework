package com.zhuchao.android.detect;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import java.util.concurrent.LinkedBlockingQueue;

//一、添加元素
//1、add 方法：如果队列已满，报java.lang.IllegalStateException: Queue full 错误
//2、offer 方法：如果队列已满，程序正常运行，只是不再新增元素
//3、put 方法：如果队列已满，阻塞
//二、取元素
//1、poll 方法：弹出队顶元素，队列为空时返回null
//2、peek 方法：返回队列顶元素，但顶元素不弹出，队列为空时返回null
//3、take 方法：当队列为空，阻塞

public class VideoRecorder {
    private VideoWriter mVideoWriter;
    private Size mFrameSize;
    private double mFrameRate;
    private int mOutputWidth;
    private int mOutputHeight;
    LinkedBlockingQueue mFrameToRecordQueue = new LinkedBlockingQueue<>(10);
    public VideoRecorder() {
        // 设置视频的分辨率和格式
        mOutputWidth = 640;
        mOutputHeight = 480;
        mFrameRate = 30;
        mFrameSize = new Size(mOutputWidth, mOutputHeight);
    }

    public VideoRecorder(int width, int height, double frameRate) {
        // 设置视频的分辨率和格式
        mOutputWidth = width;
        mOutputHeight = height;
        mFrameRate = frameRate;
        mFrameSize = new Size(mOutputWidth, mOutputHeight);
    }

    public Size getFrameSize() {
        return mFrameSize;
    }

    public void setFrameSize(Size mFrameSize) {
        this.mFrameSize = mFrameSize;
    }

    public int getOutputWidth() {
        return mOutputWidth;
    }

    public void setOutputWidth(int mOutputWidth) {
        this.mOutputWidth = mOutputWidth;
    }

    public int getOutputHeight() {
        return mOutputHeight;
    }

    public void setOutputHeight(int mOutputHeight) {
        this.mOutputHeight = mOutputHeight;
    }

    public double getFrameRate() {
        return mFrameRate;
    }

    public void setFrameRate(int mFrameRate) {
        this.mFrameRate = mFrameRate;
    }
    //cv2.VideoWriter_fourcc('I','4','2','0')
    //这个选项是一个未压缩的YUV编码，4:2:0色度子采样。这种编码广泛兼容，但会产生大文件。文件扩展名应为.avi。
    //cv2.VideoWriter_fourcc('P','I','M','1')
    //此选项为MPEG-1。文件扩展名应为.avi。
    //cv2.VideoWriter_fourcc('X','V','I','D')
    //此选项是一个相对较旧的MPEG-4编码。如果要限制结果视频的大小，这是一个很好的选择。文件扩展名应为.avi。
    //cv2.VideoWriter_fourcc('m','p','4','v')
    //此选项是另一个相对较旧的MPEG-4编码。如果要限制结果视频的大小，这是一个很好的选择。文件扩展名应为.m4v。
    //cv2.VideoWriter_fourcc('X','2','6','4'):
    //这个选项是一种比较新的MPEG-4编码方式。如果你想限制结果视频的大小，这可能是最好的选择。文件扩展名应为.mp4。
    //cv2.VideoWriter_fourcc('H','2','6','4'):
    //这个选项是传统的H264编码方式。如果你想限制结果视频的大小，这可能是很好的选择。文件扩展名应为.mp4。
    //cv2.VideoWriter_fourcc('T','H','E','O')
    //这个选项是Ogg Vorbis。文件扩展名应为.ogv。
    //cv2.VideoWriter_fourcc('F','L','V','1')
    //此选项为Flash视频。文件扩展名应为.flv。
    //cv2.VideoWriter_fourcc('M','J','P','G')
    //此选项为motion-jpeg视频。文件扩展名应为.avi。

    public void startRecording(String outputPath) {
        // 设置视频编码器和输出视频的路径
        int fourcc = VideoWriter.fourcc('M', 'J', 'P', 'G'); // 或者使用 'X', 'V', 'I', 'D' 尝试其他格式
        mVideoWriter = new VideoWriter(outputPath, fourcc, mFrameRate, mFrameSize);
    }

    public void encodeFrameRecord(Mat frame) {
        if (mVideoWriter != null) {
            // 调整帧大小，如果需要
            Mat resizedFrame = new Mat();
            if (frame.size().height != mOutputHeight || frame.size().width != mOutputWidth) {
                org.opencv.imgproc.Imgproc.resize(frame, resizedFrame, mFrameSize);
            } else {
                resizedFrame = frame;
            }
            // 写入帧到视频
            mVideoWriter.write(resizedFrame);
            // 释放资源
            resizedFrame.release();
        }
    }

    public void stopRecording() {
        if (mVideoWriter != null) {
            // 释放资源
            mVideoWriter.release();
        }
    }
}