package com.zhuchao.android.detect;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

public class VideoRecorder {

    private VideoWriter videoWriter;
    private Size frameSize;
    private int outputWidth;
    private int outputHeight;

    public VideoRecorder() {
        // 设置视频的分辨率和格式
        outputWidth = 640;
        outputHeight = 480;
        frameSize = new Size(outputWidth, outputHeight);
    }

    public Size getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(Size frameSize) {
        this.frameSize = frameSize;
    }

    public int getOutputWidth() {
        return outputWidth;
    }

    public void setOutputWidth(int outputWidth) {
        this.outputWidth = outputWidth;
    }

    public int getOutputHeight() {
        return outputHeight;
    }

    public void setOutputHeight(int outputHeight) {
        this.outputHeight = outputHeight;
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
        videoWriter = new VideoWriter(outputPath, fourcc, 30.0, frameSize);
    }

    public void encodeFrameRecord(Mat frame) {
        if (videoWriter != null) {
            // 调整帧大小，如果需要
            Mat resizedFrame = new Mat();
            if (frame.size().height != outputHeight || frame.size().width != outputWidth) {
                org.opencv.imgproc.Imgproc.resize(frame, resizedFrame, frameSize);
            } else {
                resizedFrame = frame;
            }
            // 写入帧到视频
            videoWriter.write(resizedFrame);
            // 释放资源
            resizedFrame.release();
        }
    }

    public void stopRecording() {
        if (videoWriter != null) {
            // 释放资源
            videoWriter.release();
        }
    }
}