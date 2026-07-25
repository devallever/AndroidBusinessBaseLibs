/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.absbase.helper.log.DLog;
import com.google.zxing.utils.BitmapUtils;

@SuppressWarnings("deprecation") // camera APIs
final class PreviewCallback implements Camera.PreviewCallback {

  private static final String TAG = PreviewCallback.class.getSimpleName();

  private final CameraConfigurationManager configManager;
  private Handler previewHandler;
  private int previewMessage;

  PreviewCallback(CameraConfigurationManager configManager) {
    this.configManager = configManager;
  }

  void setHandler(Handler previewHandler, int previewMessage) {
    this.previewHandler = previewHandler;
    this.previewMessage = previewMessage;
  }

  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    // 视频出来的数据是横着的，需要根据屏幕方向给予旋转
    Point cameraResolution = configManager.getCameraResolution();
    int width = cameraResolution.x;
    int height = cameraResolution.y;
    try {
      switch (configManager.getCWNeededRotation()) {
        case 0:
          break;
        case 90:
          data = BitmapUtils.rotateYUV420Degree90(data, width, height);
          width = cameraResolution.y;
          height = cameraResolution.x;
          break;
        case 180:
          data = BitmapUtils.rotateYUV420Degree180(data, width, height);
          break;
        case 270:
          data = BitmapUtils.rotateYUV420Degree270(data, width, height);
          width = cameraResolution.y;
          height = cameraResolution.x;
          break;
        default:
      }
    } catch (Exception e) {
      DLog.printStackTrace(e);
    }

    Handler thePreviewHandler = previewHandler;
    if (cameraResolution != null && thePreviewHandler != null) {
      Message message = thePreviewHandler.obtainMessage(previewMessage, width,
              height, data);
//      DLog.d("allever", "msg what = " + previewMessage);
      message.sendToTarget();
      previewHandler = null;
    } else {
//      Log.d(TAG, "Got preview callback, but no handler or resolution available");
    }
  }
}