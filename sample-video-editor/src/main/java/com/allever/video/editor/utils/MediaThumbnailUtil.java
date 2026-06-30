package com.allever.video.editor.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.allever.video.editor.function.media.MediaTypeUtil;
import com.allever.video.editor.function.editor.bean.VideoBean;
import com.allever.video.editor.ui.bean.BitmapBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import pl.droidsonroids.gif.GifDrawable;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;
import static android.media.MediaMetadataRetriever.OPTION_NEXT_SYNC;

/**
 * @author dell
 */
public class MediaThumbnailUtil {
    public static void loadVideoInfo(VideoBean bean) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(bean.getPath());
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String width = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String degreesString = null;
            try {
                degreesString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String hasAudio = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
            if (!TextUtils.isEmpty(degreesString)) {
                long d = Long.parseLong(duration);
                bean.getVideoTime().setSrcStartTime(0);
                bean.getVideoTime().setSrcEndTime(d);
                bean.getVideoTime().setDstStartTime(0);
                bean.getVideoTime().setDstEndTime(d);
                bean.setDuration(d);
            }
            RectF position = bean.getPosition();
            if (!TextUtils.isEmpty(width)) {
                position.right = Integer.parseInt(width);
            }
            if (!TextUtils.isEmpty(height)) {
                position.bottom = Integer.parseInt(height);
            }
            if (!TextUtils.isEmpty(degreesString)) {
                int degree = Integer.valueOf(degreesString);
                if (degree == 90 || degree == 270) {
                    int tmp = (int) position.width();
                    position.right= position.height();
                    position.bottom = tmp;
                }
                bean.setAngle(degree);
            }
            if (!TextUtils.isEmpty(hasAudio)) {
                bean.setHasAudio(hasAudio.endsWith("yes"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据时间获取视频的帧
     * @param filePath
     * @param timeUs    微秒
     * @param boundSize
     * @return
     */
    public static Bitmap createVideoThumbnail(String filePath, long timeUs, int boundSize) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(timeUs);
        } catch (IllegalArgumentException ex) {
        } catch (RuntimeException ex) {
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }

        if (bitmap == null) return null;

        // Scale down the bitmap if it's too large.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int max = Math.max(width, height);
        if (max > boundSize) {
            float scale = boundSize * 1.0f / max;
            int w = Math.round(scale * width);
            int h = Math.round(scale * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }


    private static final String TAG = com.allever.video.editor.function.media.MediaThumbnailUtil.class.getSimpleName();

    /* Maximum pixels size for created bitmap. */
    private static final int UNCONSTRAINED = -1;

    /* Options used internally. */
    private static final int OPTIONS_NONE = 0x0;

    /**
     * Constant used to indicate we should recycle the input in
     * {@link #extractThumbnail(Bitmap, int, int, int)} unless the output is the input.
     */
    public static final int OPTIONS_RECYCLE_INPUT = 0x2;

    /**
     * Constant used to indicate the dimension of mini thumbnail.
     * @hide Only used by media framework and media provider internally.
     */
    public static final int TARGET_SIZE_MINI_THUMBNAIL = 168;

    /**
     * Constant used to indicate the dimension of micro thumbnail.
     * @hide Only used by media framework and media provider internally.
     */
    public static final int TARGET_SIZE_MICRO_THUMBNAIL = 168;

    private static com.allever.video.editor.function.media.MediaThumbnailUtil mInstance;

    public static com.allever.video.editor.function.media.MediaThumbnailUtil getInstance() {
        if (mInstance == null)
            mInstance = new com.allever.video.editor.function.media.MediaThumbnailUtil();
        return mInstance;
    }

    private MediaThumbnailUtil(){
    }

    /**
     * 获取视频的缩略图
     * @param filePath
     * @param boundSize
     * @return
     */
    public Bitmap getVideoThumbnail(String filePath, int boundSize) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= 8) {
            bitmap = createVideoThumbnail(filePath, boundSize);
        }
        if ( bitmap == null ) {
            return null;
        }
        return bitmap;
    }

    /**
     * 获取视频的第一帧
     * @param filePath
     * @param boundSize
     * @return
     */
    public static Bitmap createVideoThumbnail(String filePath, int boundSize) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(0);
        } catch (IllegalArgumentException ex) {
        } catch (RuntimeException ex) {
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }

        if (bitmap == null) return null;

        // Scale down the bitmap if it's too large.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int max = Math.max(width, height);
        if (max > boundSize) {
            float scale = boundSize * 1.0f / max;
            int w = Math.round(scale * width);
            int h = Math.round(scale * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }

    public static Bitmap getCompressBitmap(String filePath,int maxPixels,int boundSize){
        File f = new File(filePath);
        if (!f.exists()) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int targetSize = boundSize;
//		// 设置缩放比例
        options.inSampleSize = computeSampleSize(options, targetSize, maxPixels);
//		if ( Loger.isD() ) {
//			Loger.e("inSampleSize===========>", ""+options.inSampleSize);
//		}
        // 图片质量
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 设置为false,解析Bitmap对象加入到内存中
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        // 获取资源图片
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            bitmap = BitmapFactory.decodeStream(fis, null,
                    options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
        }finally{
            if ( fis != null ) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        int degree = readPictureDegree(filePath);
        bitmap = extractThumbnail(bitmap, targetSize, targetSize,
                OPTIONS_RECYCLE_INPUT,degree);
        return bitmap;
    }

    public Bitmap getImageThumbnail(String filePath, int boundSize, int degree) {
        File f = new File(filePath);
        if (!f.exists()) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        // 设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        int targetSize = boundSize;


//		// 设置缩放比例
        if (options.outHeight > targetSize && options.outWidth > targetSize) {
            options.inSampleSize = computeSampleSize(options, targetSize, 100 * 1024);
        } else {
            options.inSampleSize = 1;
            targetSize = Math.min(options.outHeight, options.outWidth);
        }

//		if ( Loger.isD() ) {
//			Loger.e("inSampleSize===========>", ""+options.inSampleSize);
//		}
//		if ( options.inSampleSize < 1 ) {
//			options.inSampleSize = 4;
//		}

        // 图片质量
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 设置为false,解析Bitmap对象加入到内存中
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        // 获取资源图片
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            bitmap = BitmapFactory.decodeStream(fis, null,
                    options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
        }finally{
            if ( fis != null ) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//		bitmap = extractThumbnail(bitmap, targetSize, targetSize,
//				OPTIONS_RECYCLE_INPUT,degree);
        bitmap = ImageHelper.rotating(bitmap, degree);

        return bitmap;
    }

    /**
     * 获取指定尺寸的图片
     * @param filePath
     * @param boundSize
     * @param degree
     * @return
     */
    public Bitmap getImage(String filePath, int boundSize, int degree) {
        File f = new File(filePath);
        if (!f.exists()) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        // 设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        int targetSize = boundSize;


//		// 设置缩放比例
        if (options.outHeight > targetSize && options.outWidth > targetSize) {
            options.inSampleSize = computeSampleSize(options, targetSize, UNCONSTRAINED);
        } else {
            options.inSampleSize = 1;
            targetSize = Math.min(options.outHeight, options.outWidth);
        }

//		if ( Loger.isD() ) {
//			Loger.e("inSampleSize===========>", ""+options.inSampleSize);
//		}
//		if ( options.inSampleSize < 1 ) {
//			options.inSampleSize = 4;
//		}

        // 图片质量
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 设置为false,解析Bitmap对象加入到内存中
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        // 获取资源图片
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            bitmap = BitmapFactory.decodeStream(fis, null,
                    options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
        }finally{
            if ( fis != null ) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//		bitmap = extractThumbnail(bitmap, targetSize, targetSize,
//				OPTIONS_RECYCLE_INPUT,degree);
        bitmap = ImageHelper.rotating(bitmap, degree);

        return bitmap;
    }

    /**
     * 获取指定尺寸的加密图片
     * @param filePath
     * @param boundSize
     * @param degree
     * @return
     */
    public Bitmap getEncryptImage(String filePath, int boundSize, int degree) {

        File f = new File(filePath);
        if (!f.exists()) {
            return null;
        }
        EntryptFileInputStream data = null;
        EntryptFileInputStream dataTrue = null;
        // 获取资源图片
        Bitmap bitmap = null;
        int targetSize = boundSize;
        try {
            data = EncryptUtil.decrypt(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            // 设置为true,表示解析Bitmap对象，该对象不占内存
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(data, null, options);

            // 设置缩放比例
            if (options.outHeight > targetSize && options.outWidth > targetSize) {
                options.inSampleSize = computeSampleSize(options, targetSize, UNCONSTRAINED);
            } else {
                options.inSampleSize = 1;
                targetSize = Math.min(options.outHeight, options.outWidth);
            }

            // 图片质量
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            // 设置为false,解析Bitmap对象加入到内存中
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;
            options.inInputShareable = true;

            dataTrue = EncryptUtil.decrypt(filePath);
            bitmap = BitmapFactory.decodeStream(dataTrue, null, options);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally{
            try {
                if(data != null){
                    data.close();
                }
                if(dataTrue != null){
                    dataTrue.close();
                }
            } catch (Throwable e) {
            }
        }

//		bitmap = extractThumbnail(bitmap, targetSize, targetSize,
//				OPTIONS_RECYCLE_INPUT,degree);
        bitmap = ImageHelper.rotating(bitmap, degree);

        return bitmap;
    }

    /**
     * 获取被加密图片的缩略图
     * @param filePath
     * @param boundSize
     * @param degree
     * @return
     */
    public Bitmap getEncryptImageThumbnail(String filePath, int boundSize, int degree) {

        File f = new File(filePath);
        if (!f.exists()) {
            return null;
        }
        EntryptFileInputStream data = null;
        EntryptFileInputStream dataTrue = null;
        // 获取资源图片
        Bitmap bitmap = null;
        int targetSize = boundSize;
        try {
            data = EncryptUtil.decrypt(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            // 设置为true,表示解析Bitmap对象，该对象不占内存
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(data, null, options);

            // 设置缩放比例
            if (options.outHeight > targetSize && options.outWidth > targetSize) {
                options.inSampleSize = computeSampleSize(options, targetSize, 100 * 1024);
            } else {
                options.inSampleSize = 1;
                targetSize = Math.min(options.outHeight, options.outWidth);
            }

            // 图片质量
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            // 设置为false,解析Bitmap对象加入到内存中
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;
            options.inInputShareable = true;

            dataTrue = EncryptUtil.decrypt(filePath);
            bitmap = BitmapFactory.decodeStream(dataTrue, null, options);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally{
            try {
                if(data != null){
                    data.close();
                }
                if(dataTrue != null){
                    dataTrue.close();
                }
            } catch (Throwable e) {
            }
        }

//		bitmap = extractThumbnail(bitmap, targetSize, targetSize,
//				OPTIONS_RECYCLE_INPUT,degree);
        bitmap = ImageHelper.rotating(bitmap, degree);

        return bitmap;
    }

    public static Bitmap extractThumbnail(
            Bitmap source, int width, int height,int degree) {
        return extractThumbnail(source, width, height, OPTIONS_NONE,degree);
    }

    /**
     * Creates a centered bitmap of the desired size.
     *
     * @param source original bitmap source
     * @param width targeted width
     * @param height targeted height
     * @param options options used during thumbnail extraction
     */
    public static Bitmap extractThumbnail(
            Bitmap source, int width, int height, int options,int degree) {
        if (source == null) {
            return null;
        }

        float scale;
        if (source.getWidth() < source.getHeight()) {
            scale = width / (float) source.getWidth();
        } else {
            scale = height / (float) source.getHeight();
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap thumbnail = transform(matrix, source, width, height,
                options,degree);
        return thumbnail;
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8 ) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
//	        return Math.min(lowerBound, upperBound);
        if ((maxNumOfPixels == UNCONSTRAINED) &&
                (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    private static Bitmap transform(Matrix scaler,
                                    Bitmap source,
                                    int targetWidth,
                                    int targetHeight,
                                    int options,
                                    int degree) {

        boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;
//
        float bitmapWidthF = source.getWidth();
        float bitmapHeightF = source.getHeight();

        float bitmapAspect = bitmapWidthF / bitmapHeightF;
        float viewAspect   = (float) targetWidth / targetHeight;

        if (bitmapAspect > viewAspect) {
            float scale = targetHeight / bitmapHeightF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            }
        } else {
            float scale = targetWidth / bitmapWidthF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            }
        }

        if ( degree > 0 ) {
            scaler.postRotate(degree);
        }

        Bitmap b1 = null;
        if (scaler != null) {
            // this is used for minithumb and crop, so we want to filter here.
            try {
                b1 = Bitmap.createBitmap(source, 0, 0,
                        source.getWidth(), source.getHeight(), scaler, true);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
            }
        } else {
            b1 = source;
        }

        if (recycle && b1 != source) {
            source.recycle();
        }

        int dx1 = Math.max(0, b1.getWidth() - targetWidth);
        int dy1 = Math.max(0, b1.getHeight() - targetHeight);

        Bitmap b2 = null;
        try {
            b2 = Bitmap.createBitmap(
                    b1,
                    dx1 / 2,
                    dy1 / 2,
                    targetWidth,
                    targetHeight);
        } catch (OutOfMemoryError e) {
        } catch (Exception e) {
//				Loger.e(TAG, "Exception: " + e.getLocalizedMessage());
        }

        if (b2 != b1) {
            if (recycle || b1 != source) {
                b1.recycle();
            }
        }

        return b2;

    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0 || null == bitmap) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        } catch (Exception e) {
//				Loger.e(TAG, "Exception: " + e.getLocalizedMessage());
        }
        if (null != bitmap) {
            bitmap.recycle();
        }
        return bmp;
    }

    /**
     * 获取图片的角度
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return degree;
        }
        return degree;
    }

    /**
     * 获取图片的角度
     * @param exif
     * @return
     */
    public static int readPictureDegree(ExifInterface exif) {
        int degree = 0;
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }

    /**
     * 读取图片的时间
     * @param useLocalTime true如果读取不到就会用当前时间  false就是如果读取不到就返回0
     * @param exif
     * @return
     */
    public static long readPictureDateTime(ExifInterface exif, boolean useLocalTime){
        String dataTime = exif.getAttribute(ExifInterface.TAG_DATETIME);//2015:08:02 20:07:41
        long time = 0;
        if(!TextUtils.isEmpty(dataTime)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            ParsePosition pos = new ParsePosition(0);
            Date d = sdf.parse(dataTime, pos);
            if(d != null){
                time = d.getTime();
            } else{
                if(useLocalTime){
                    time = System.currentTimeMillis();
                }
            }
        } else{
            if(useLocalTime){
                time = System.currentTimeMillis();
            }
        }
        return time;
    }

    public static Bitmap getFullBitmap(String filepath) throws Exception {
        Bitmap retBitmap = null;
        int sampleSize = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();

        // 取像sampleSize的初始值，该作用是要限制一张图片的大小超过2M
        options.inJustDecodeBounds = true;
        retBitmap = BitmapFactory.decodeFile(filepath, options);
        sampleSize = (int)(Math.sqrt(options.outWidth * options.outHeight
                / (double)BITMAP_MAX_NUM_PIXELS) + 1);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = false;

        do {
            try {
                // 背景图片像素的宽和高取原来的1/sampleSize
                options.inSampleSize = sampleSize;
                retBitmap = BitmapFactory.decodeFile(filepath, options);
                break;
            } catch (OutOfMemoryError e) {
                //BgDataEx.add(BgDataEx.ERROR_OUT_OF_MEMMORY);
                //Toast.makeText(this, R.string.backgroudn_too_large, Toast.LENGTH_SHORT);
                ++sampleSize;
            } catch (Exception e) {
//				Loger.e(TAG, "Exception: " + e.getLocalizedMessage());
                throw e;
            }
        } while (true);

        int degree = readPictureDegree(filepath);
        return rotateBitmap(retBitmap, degree);
    }

    //retBitmap = BitmapFactory.decodeByteArray(imageBytes, 0,
//	imageBytes.length, options);
    public static Bitmap getFullBitmap(byte[] bytes) throws Exception {
        if ( bytes == null ) {
            return null;
        }
        Bitmap retBitmap = null;
        int sampleSize = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();

        // 取像sampleSize的初始值，该作用是要限制一张图片的大小超过2M
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0,
                bytes.length, options);
        sampleSize = (int)(Math.sqrt(options.outWidth * options.outHeight
                / (double)BITMAP_MAX_NUM_PIXELS) + 1);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = false;

        do {
            try {
                // 背景图片像素的宽和高取原来的1/sampleSize
                options.inSampleSize = sampleSize;
                retBitmap = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length, options);
                break;
            } catch (OutOfMemoryError e) {
                //BgDataEx.add(BgDataEx.ERROR_OUT_OF_MEMMORY);
                //Toast.makeText(this, R.string.backgroudn_too_large, Toast.LENGTH_SHORT);
                ++sampleSize;
            } catch (Exception e) {
//				Loger.e(TAG, "Exception: " + e.getLocalizedMessage());
                throw e;
            }
        } while (true);

        return retBitmap;
    }


    public static final int BITMAP_MAX_NUM_PIXELS = 524288 * 2;

    public static Bitmap createPreViewVideoThumbnail(String filePath, int kind) {
        return createPreViewVideoThumbnail(filePath, kind, 0);
    }

    /**
     * Create a video thumbnail for a video. May return null if the video is
     * corrupt or the format is not supported.
     *
     * @param filePath the path of video file
     * @param kind could be MINI_KIND or MICRO_KIND
     */
    public static Bitmap createPreViewVideoThumbnail(String filePath, int kind, long time) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(time, OPTION_CLOSEST);
            if (bitmap == null) {
                bitmap = retriever.getFrameAtTime(time, OPTION_NEXT_SYNC);
            }
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }

        if (bitmap == null) return null;

        if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {
            // Scale down the bitmap if it's too large.
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 512) {
                float scale = 512f / max;
                int w = Math.round(scale * width);
                int h = Math.round(scale * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            }
        } else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
            bitmap = extractThumbnail(bitmap,
                    TARGET_SIZE_MICRO_THUMBNAIL,
                    TARGET_SIZE_MICRO_THUMBNAIL,
                    OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    public static void loadVideoInfo(BitmapBean bean) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(bean.mPath);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String width = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String degreesString = null;
            try {
                degreesString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String hasAudio = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
            if (!TextUtils.isEmpty(degreesString)) {
                bean.mDuration = Long.parseLong(duration);
            }
            if (!TextUtils.isEmpty(width)) {
                bean.mWidth = Integer.parseInt(width);
            }
            if (!TextUtils.isEmpty(height)) {
                bean.mHeight = Integer.parseInt(height);
            }
            if (!TextUtils.isEmpty(degreesString)) {
                int degree = Integer.valueOf(degreesString);
                if (degree == 90 || degree == 270) {
                    int tmp = bean.mWidth;
                    bean.mWidth = bean.mHeight;
                    bean.mHeight = tmp;
                }
            }
            if (!TextUtils.isEmpty(hasAudio)) {
                bean.mHasAudio = hasAudio.endsWith("yes");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadGifInfo(BitmapBean bean) {
        if (bean != null && bean.mType == MediaTypeUtil.TYPE_GIF) {
            // Movie 获取gif时长不正确，多一倍
//			Movie movie = Movie.decodeFile(bean.mPath);
//			bean.mDuration = movie.duration();
            try {
                GifDrawable drawable = new GifDrawable(bean.mPath);
                bean.mDuration = drawable.getDuration();
                drawable.recycle();
            } catch (Exception e) {
            }
        }
    }
}
