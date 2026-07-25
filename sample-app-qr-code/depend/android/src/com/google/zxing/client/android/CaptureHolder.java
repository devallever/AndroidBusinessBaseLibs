package com.google.zxing.client.android;

import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;


public interface CaptureHolder {

    ViewfinderView getViewfinderView();

    Handler getHandler();

    CameraManager getCameraManager();

    void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor);

    void drawViewfinder();

    void restartPreviewAfterDelay(long delay);
}
