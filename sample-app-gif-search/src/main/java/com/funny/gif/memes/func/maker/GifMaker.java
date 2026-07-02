package com.funny.gif.memes.func.maker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.DrawableRes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beak on 2015/8/27.
 */
public class GifMaker {

    public static final String TAG = GifMaker.class.getSimpleName();

    private int mScale = 1;

    private OnGifListener mGifListener = null;

    public GifMaker(int scale) {
        if (scale < 1) {
            return;
        }
        mScale = scale;
    }

    public boolean makeGif (List<Bitmap> source, String outputPath) throws IOException {
        long start = System.currentTimeMillis();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encoder.start(bos);
        encoder.setRepeat(0);
        final int length = source.size();
        for (int i = 0; i < length; i++) {
            Bitmap bmp = source.get(i);
            if (bmp == null) {
                continue;
            }
            Bitmap thumb = ThumbnailUtils.extractThumbnail(bmp, bmp.getWidth() / mScale, bmp.getHeight() / mScale, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            try {
                encoder.addFrame(thumb);
                if (mGifListener != null) {
                    mGifListener.onMake(i, length, (int) ((i / (float)length) * 100));
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.gc();
                break;
            }
            //TODO how about releasing bitmap after addFrame
        }
        encoder.finish();
        source.clear();
        byte[] data = bos.toByteArray();
        File file = new File(outputPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(data);
        fileOutputStream.flush();
        fileOutputStream.close();
        long end = System.currentTimeMillis();
        Log.d(TAG, "makeGif: 耗时 -> " + (end - start) / 1000f);
        return file.exists();
    }

    public boolean makeGifFromPath (List<String> sourcePathList, String outputPath) throws IOException {
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        final int length = sourcePathList.size();
        for (int i = 0; i < length; i++) {
            bitmaps.add(BitmapFactory.decodeFile(sourcePathList.get(i)));
        }
        return makeGif(bitmaps, outputPath);
    }

    public boolean makeGifFromFile (List<File> sourceFileList, String outputPath) throws IOException {
        List<String> pathArray = new ArrayList<String>();
        final int length = sourceFileList.size();
        for (int i = 0; i < length; i++) {
            pathArray.add(sourceFileList.get(i).getAbsolutePath());
        }
        return makeGifFromPath(pathArray, outputPath);
    }

    public boolean makeGif (Resources resources, @DrawableRes int[] sourceDrawableId, String outputPath) throws IOException {
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        for (int i = 0; i < sourceDrawableId.length; i++) {
            bitmaps.add(BitmapFactory.decodeResource(resources, sourceDrawableId[i]));
        }
        return makeGif(bitmaps, outputPath);
    }

    public boolean makeGifFromVideo (String videoPath, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        return makeGifWithMediaMetadataRetriever(retriever, startMillSeconds, endMillSeconds, periodMillSeconds, outputPath);
    }

    public boolean makeGifFromVideo (Context context, Uri uri, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        retriever.setDataSource(context, uri);
        return makeGifWithMediaMetadataRetriever(retriever, startMillSeconds, endMillSeconds, periodMillSeconds, outputPath);
    }

    public boolean makeGifFromVideoDirectly (Context context, Uri uri, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        retriever.setDataSource(context, uri);

        if (startMillSeconds < 0 || endMillSeconds <= 0 || periodMillSeconds <= 0 || endMillSeconds <= startMillSeconds) {
            throw new IllegalArgumentException("startMillSecodes may < 0 or endMillSeconds or periodMillSeconds may <= 0, or endMillSeconds <= startMillSeconds");
        }
        try {

            long start = System.currentTimeMillis();
            long start2 = start;
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long duration = Long.parseLong(durationStr);
            long minDuration = Math.min(duration, endMillSeconds);
            int count = 1;
            //
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            encoder.start(bos);
            encoder.setRepeat(0);

            int total = (int) ((minDuration - startMillSeconds) / periodMillSeconds) + 1;
            for (long time = startMillSeconds; time < minDuration; time += periodMillSeconds) {
                long bitmapStart = System.currentTimeMillis();
                Bitmap bitmap = retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
                long bitmapEnd = System.currentTimeMillis();
                Log.d(TAG, "makeGifWithMediaMetadataRetriever: getFrameAtTime " + count + " 耗时 -> " + (bitmapEnd - bitmapStart) / 1000f);

                if (bitmap == null) {
                    count ++;
                    continue;
                }

                Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, bitmap.getWidth() / mScale, bitmap.getHeight() / mScale, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                try {
                    encoder.addFrame(thumb);
                    if (mGifListener != null) {
                        mGifListener.onMake(count, total, (int) ((count / (float)total) * 100));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.gc();
                    break;
                }

                count++;

            }
            retriever.release();
            long end = System.currentTimeMillis();
            Log.d(TAG, "makeGifWithMediaMetadataRetriever: 耗时 -> " + (end - start) / 1000f);
            encoder.finish();
            byte[] data = bos.toByteArray();
            File file = new File(outputPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
            long end2 = System.currentTimeMillis();
            Log.d(TAG, "makeGif: 耗时 -> " + (end2 - start2) / 1000f);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean makeGifWithMediaMetadataRetriever (MediaMetadataRetriever retriever, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        if (startMillSeconds < 0 || endMillSeconds <= 0 || periodMillSeconds <= 0 || endMillSeconds <= startMillSeconds) {
            throw new IllegalArgumentException("startMillSecodes may < 0 or endMillSeconds or periodMillSeconds may <= 0, or endMillSeconds <= startMillSeconds");
        }
        try {
            long start = System.currentTimeMillis();
            List<Bitmap> bitmaps = new ArrayList<Bitmap>();
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long duration = Long.parseLong(durationStr);
            long minDuration = Math.min(duration, endMillSeconds);
            int count = 1;
            for (long time = startMillSeconds; time < minDuration; time += periodMillSeconds) {
                long bitmapStart = System.currentTimeMillis();
                Bitmap bitmap = retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
                long bitmapEnd = System.currentTimeMillis();
                Log.d(TAG, "makeGifWithMediaMetadataRetriever: getFrameAtTime " + count + " 耗时 -> " + (bitmapEnd - bitmapStart) / 1000f);
                bitmaps.add(bitmap);
                count++;
            }
            retriever.release();
            long end = System.currentTimeMillis();
            Log.d(TAG, "makeGifWithMediaMetadataRetriever: 耗时 -> " + (end - start) / 1000f);
            return makeGif(bitmaps, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setOnGifListener (OnGifListener listener) {
        mGifListener = listener;
    }

    public interface OnGifListener {
        public void onMake (int current, int total, int percent);
    }
}
