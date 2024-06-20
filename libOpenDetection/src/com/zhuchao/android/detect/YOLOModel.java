package com.zhuchao.android.detect;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class YOLOModel {
    private final Interpreter interpreter;
    private final int inputSize;

    public YOLOModel(AssetManager assetManager, String modelPath, int inputSize) throws IOException {
        this.inputSize = inputSize;
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath));
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public List<DetectionResult> detect(Bitmap bitmap) {
        // Resize the bitmap to the required input size
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false);
        float[][][][] input = new float[1][inputSize][inputSize][3];
        float[][][] output = new float[1][25200][85]; // The output shape depends on the YOLO version

        // Preprocess the image data
        preprocessBitmap(resizedBitmap, input);

        // Run the interpreter
        interpreter.run(input, output);

        // Process the output
        return processOutput(output, bitmap.getWidth(), bitmap.getHeight());
    }

    private void preprocessBitmap(Bitmap bitmap, float[][][][] input) {
        for (int y = 0; y < inputSize; y++) {
            for (int x = 0; x < inputSize; x++) {
                int pixel = bitmap.getPixel(x, y);
                input[0][y][x][0] = (pixel >> 16 & 0xFF) / 255.0f;
                input[0][y][x][1] = (pixel >> 8 & 0xFF) / 255.0f;
                input[0][y][x][2] = (pixel & 0xFF) / 255.0f;
            }
        }
    }

    private List<DetectionResult> processOutput(float[][][] output, int originalWidth, int originalHeight) {
        List<DetectionResult> results = new ArrayList<>();
        for (int i = 0; i < output[0].length; i++) {
            float[] result = output[0][i];
            float confidence = result[4];
            if (confidence > 0.5) { // Threshold for detection confidence
                float xCenter = result[0] * originalWidth;
                float yCenter = result[1] * originalHeight;
                float width = result[2] * originalWidth;
                float height = result[3] * originalHeight;
                float left = xCenter - width / 2;
                float top = yCenter - height / 2;

                RectF rect = new RectF(left, top, left + width, top + height);
                results.add(new DetectionResult(rect, confidence));
            }
        }
        return results;
    }

    public static class DetectionResult {
        private final RectF rect;
        private final float confidence;

        public DetectionResult(RectF rect, float confidence) {
            this.rect = rect;
            this.confidence = confidence;
        }

        public RectF getRect() {
            return rect;
        }

        public float getConfidence() {
            return confidence;
        }
    }
}
