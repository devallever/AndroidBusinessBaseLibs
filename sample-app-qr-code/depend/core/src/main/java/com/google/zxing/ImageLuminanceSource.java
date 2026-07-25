package com.google.zxing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.utils.BitmapUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class ImageLuminanceSource extends LuminanceSource {
    private Bitmap mBitmap;
    private byte[] mYUVData;

    public static ImageLuminanceSource create(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ImageLuminanceSource imageLuminanceSource = create(is);
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageLuminanceSource;
    }

    public static ImageLuminanceSource create(InputStream is) {
        if (is == null) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap != null ? new ImageLuminanceSource(bitmap) : null;
    }

    public ImageLuminanceSource(Bitmap bitmap) {
        super(bitmap.getWidth(), bitmap.getHeight());
        mBitmap = bitmap;
        mYUVData = BitmapUtils.getYUVByBitmap(mBitmap);
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        int offset = y * width;
        System.arraycopy(mYUVData, offset, row, 0, width);
        return row;
    }

    @Override
    public byte[] getMatrix() {
        return mYUVData;
    }

    public void destory() {
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }
}