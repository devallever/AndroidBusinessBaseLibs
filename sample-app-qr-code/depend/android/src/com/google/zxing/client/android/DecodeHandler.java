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

package com.google.zxing.client.android;

import android.app.Activity;
import android.graphics.Bitmap;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.BitmapLuminanceSource;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Map;

final class DecodeHandler extends Handler {

  private static final String TAG = DecodeHandler.class.getSimpleName();

  private final Activity activity;
  private final CaptureHolder captureHolder;
  private final MultiFormatReader multiFormatReader;
  private boolean running = true;

  DecodeHandler(Activity activity, CaptureHolder captureHolder, Map<DecodeHintType,Object> hints) {
      this.captureHolder = captureHolder;
      multiFormatReader = new MultiFormatReader();
      multiFormatReader.setHints(hints);
      this.activity = activity;
  }

  @Override
  public void handleMessage(Message message) {
    if (message == null || !running) {
      return;
    }
    try {
      if (message.what == R.id.decode) {
        decode((byte[]) message.obj, message.arg1, message.arg2);//
      } else if (message.what == R.id.decode_img) {
        String imgPath = message.obj instanceof String ? (String) message.obj : null;
        decodeImg(imgPath);
      } else if (message.what == R.id.quit) {
        running = false;
        Looper.myLooper().quit();

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void decodeImg(String imgPath) {
    if (TextUtils.isEmpty(imgPath)) {
      return;
    }
    BitmapLuminanceSource source = null;
    try {
      Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
      source = new BitmapLuminanceSource(bitmap);
    } catch (Exception ignored) {
    }
    decodeImpl(source);
  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
  private void decode(byte[] data, int width, int height) {
    long start = System.currentTimeMillis();
    Result rawResult = null;
    PlanarYUVLuminanceSource source = captureHolder.getCameraManager().buildLuminanceSource(data, width, height);
    decodeImpl(source);
  }

//  Window mWindow;
  private void decodeImpl(LuminanceSource source) {
    long start = System.currentTimeMillis();
    Result rawResult = null;
    if (source != null) {
      // 用来调试时浏览取景的内容
//      {
//        if (mWindow == null) {
//          mWindow = new Window(activity);
//        }
//        mWindow.showSource(source, source.getWidth(), source.getHeight());
////      mWindow.showBitmap(data, width, height);
//      }

      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
      try {
        rawResult = multiFormatReader.decodeWithState(bitmap);
      } catch (ReaderException re) {
        // continue
      } finally {
        multiFormatReader.reset();
      }
    }

    Handler handler = captureHolder.getHandler();
    if (rawResult != null) {
      // Don't log the barcode contents for security.
      long end = System.currentTimeMillis();
      Log.d(TAG, "Found barcode in " + (end - start) + " ms");
      if (handler != null) {
        Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);//
        Bundle bundle = new Bundle();
        bundleThumbnail(source, bundle);
        message.setData(bundle);
        message.sendToTarget();
      }
    } else {
      if (handler != null) {
        Message message = Message.obtain(handler, R.id.decode_failed);
        message.sendToTarget();
      }
    }
  }

  private static void bundleThumbnail(LuminanceSource source, Bundle bundle) {
    bundle.putByteArray(DecodeThread.BARCODE_BITMAP, source.getThumbnailByteArray());
    bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, source.getThumbnailScaleFactor());
  }

}
