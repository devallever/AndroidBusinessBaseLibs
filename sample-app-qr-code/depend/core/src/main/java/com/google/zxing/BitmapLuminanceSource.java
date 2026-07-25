package com.google.zxing;

import android.graphics.Bitmap;

public class BitmapLuminanceSource extends LuminanceSource {

    private static final int THUMBNAIL_SCALE_FACTOR = 1;

    private byte[] mBytePixels;

    public BitmapLuminanceSource(Bitmap bitmap) {
        super(bitmap.getWidth(), bitmap.getHeight());
        int[] pixels = new int[(bitmap.getWidth() * bitmap.getHeight())];
        this.mBytePixels = new byte[(bitmap.getWidth() * bitmap.getHeight())];
        bitmap.getPixels(pixels, 0, getWidth(), 0, 0, getWidth(), getHeight());
        for (int i = 0; i < pixels.length; i++) {
            this.mBytePixels[i] = (byte) pixels[i];
        }
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        System.arraycopy(this.mBytePixels, y * getWidth(), row, 0, getWidth());
        return row;
    }

    @Override
    public byte[] getMatrix() {
        return this.mBytePixels;
    }


    @Override
    public byte[] getThumbnailByteArray() {
        return mBytePixels;
    }

    @Override
    public float getThumbnailScaleFactor() {
        return THUMBNAIL_SCALE_FACTOR;
    }
}
