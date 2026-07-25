/*
 * Copyright 2008 ZXing authors
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

package com.google.zxing.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.Map;

/**
 * This object renders a QR Code as a BitMatrix 2D array of greyscale values.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class QRCodeWriter implements Writer {

  private static final int QUIET_ZONE_SIZE = 4;

  @Override
  public BitMatrix encode(String contents, BarcodeFormat format, int width, int height)
      throws WriterException {

    return encode(contents, format, width, height, null);
  }

  @Override
  public BitMatrix encode(String contents,
                          BarcodeFormat format,
                          int width,
                          int height,
                          Map<EncodeHintType,?> hints) throws WriterException {

    if (contents.isEmpty()) {
      throw new IllegalArgumentException("Found empty contents");
    }

    if (format != BarcodeFormat.QR_CODE) {
      throw new IllegalArgumentException("Can only encode QR_CODE, but got " + format);
    }

    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Requested dimensions are too small: " + width + 'x' +
          height);
    }

    ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;
    int quietZone = QUIET_ZONE_SIZE;
    if (hints != null) {
      if (hints.containsKey(EncodeHintType.ERROR_CORRECTION)) {
        errorCorrectionLevel = ErrorCorrectionLevel.valueOf(hints.get(EncodeHintType.ERROR_CORRECTION).toString());
      }
      if (hints.containsKey(EncodeHintType.MARGIN)) {
        quietZone = Integer.parseInt(hints.get(EncodeHintType.MARGIN).toString());
      }
    }

    QRCode code = Encoder.encode(contents, errorCorrectionLevel, hints);
    return renderResult(code, width, height, quietZone);
  }

  // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
  // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
  private static BitMatrix renderResult(QRCode code, int width, int height, int quietZone) {
    ByteMatrix input = code.getMatrix();
    if (input == null) {
      throw new IllegalStateException();
    }
    int inputWidth = input.getWidth();
    int inputHeight = input.getHeight();
    int qrWidth = inputWidth + (quietZone * 2);
    int qrHeight = inputHeight + (quietZone * 2);
    // 白边过多时, 缩放
    int minSize = Math.min(width, height);
    int scale = calculateScale(qrWidth, minSize);
    if (scale > 0) {
      int padding, tmpValue;
      // 计算边框留白
      padding = (minSize - qrWidth * scale) / QUIET_ZONE_SIZE * quietZone;
      tmpValue = qrWidth * scale + padding;
      if (width == height) {
        width = tmpValue;
        height = tmpValue;
      } else if (width > height) {
        width = width * tmpValue / height;
        height = tmpValue;
      } else {
        height = height * tmpValue / width;
        width = tmpValue;
      }
    }
    int outputWidth = Math.max(width, qrWidth);
    int outputHeight = Math.max(height, qrHeight);

    int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
    // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
    // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
    // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
    // handle all the padding from 100x100 (the actual QR) up to 200x160.
    int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
    int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

    BitMatrix output = new BitMatrix(outputWidth, outputHeight);

    for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
      // Write the contents of this row of the barcode
      for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
        if (input.get(inputX, inputY) == 1) {
          output.setRegion(outputX, outputY, multiple, multiple);
        }
      }
    }

    return output;
  }

  /**
   * 如果留白超过1% , 则需要缩放
   * (1% 可以根据实际需要进行修改)
   * @param qrCodeSize 二维码大小
   * @param expectSize 期望输出大小
   * @return 返回缩放比例, <= 0 则表示不缩放, 否则指定缩放参数
   */
  private static int calculateScale(int qrCodeSize, int expectSize) {
    if (qrCodeSize >= expectSize) {
      return 0;
    }
    int scale = expectSize / qrCodeSize;
    int abs = expectSize - scale * qrCodeSize;
    if (abs < expectSize * 0.01) {
      return 0;
    }

    return scale;
  }
}
