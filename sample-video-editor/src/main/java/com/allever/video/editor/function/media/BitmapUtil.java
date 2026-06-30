
package com.allever.video.editor.function.media;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import com.android.absbase.App;
import com.android.absbase.helper.log.DLog;
import com.android.absbase.utils.DeviceUtils;
import com.allever.video.editor.utils.ImageHelper;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Bitmap 处理工具类
 */
public class BitmapUtil {

    private static final String TAG = BitmapUtil.class.getSimpleName();

    private static final float WATER_MARK_REF_SIZE = 1080 * 1860;

    private static final float DATE_MARK_REF_SIZE = 978 * 1302;

    private static final float DEFAULT_DATE_MARK_TEXT_SIZE = 30;

    private static final int DEFAULT_DATE_MARK_TEXT_COLOR = 0xFFFFFFFF;

    private static final int DEFAULT_DATE_MARK_SHADER_COLOR = 0x66000000;

    private static final int DEFAULT_DATE_MARK_SHADER_RADIUS = 3;

    /**
     * 把图片裁剪为正方形
     * @param bitmap
     *
     * @return
     */
    public static Bitmap cropSquareBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == height) {
            return bitmap;
        }
        int length = Math.min(width, height);
        int x = (width - length) / 2;
        int y = (height - length) / 2;
        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, x, y, length, length);
        if (squareBitmap != null) {
            bitmap.recycle();
            bitmap = squareBitmap;
        }
        return bitmap;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, float w, float h) {
        if (bitmap == null) {
            return null;
        }
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        float x = 0;
        float y = 0;
        float scaleWidth = width;
        float scaleHeight = height;
        Bitmap newbmp;
        if (w > h) {
            //比例宽度大于高度的情况
            float scale = w / h;
            float tempH = width / scale;
            if (height > tempH) {
                x = 0;
                y = (height - tempH) / 2;
                scaleWidth = width;
                scaleHeight = tempH;
            } else {
                scaleWidth = height * scale;
                x = (width - scaleWidth) / 2;
                y = 0;
            }
        } else if (w < h) {
            //比例宽度小于高度的情况
            float scale = h / w;
            float tempW = height / scale;
            if (width > tempW) {
                y = 0;
                x = (width - tempW) / 2;
                scaleWidth = tempW;
                scaleHeight = height;
            } else {
                scaleHeight = width * scale;
                y = (height - scaleHeight) / 2;
                x = 0;
                scaleWidth = width;
            }

        } else {
            //比例宽高相等的情况
            if (width > height) {
                x = (width - height) / 2;
                y = 0;
                scaleHeight = height;
                scaleWidth = height;
            } else {
                y = (height - width) / 2;
                x = 0;
                scaleHeight = width;
                scaleWidth = width;
            }
        }
        Matrix matrix = new Matrix();
        matrix.setScale(w / scaleWidth, h / scaleHeight);
        try {
            newbmp = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) scaleWidth, (int) scaleHeight, matrix, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newbmp;
    }

    /**
     * 裁剪图片
     *
     * @param bitmap
     * @param ratio
     * @return
     */
    public static Bitmap cropBitmap(Bitmap bitmap, float ratio) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Rect rect = cropRect(new Rect(0, 0, width, height), ratio);

        if (rect.width() == width && rect.height() == height) {
            return bitmap;
        }

        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
        if (squareBitmap != null) {
            bitmap.recycle();
            bitmap = squareBitmap;
        }
        return bitmap;
    }

    public static Bitmap cropBitmap(Bitmap bitmap, float ratio, int topH) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Rect rect = cropRect(new Rect(0, 0, width, height), ratio, topH);

        if (rect.width() == width && rect.height() == height) {
            return bitmap;
        }

        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
        if (squareBitmap != null) {
            bitmap.recycle();
            bitmap = squareBitmap;
        }
        return bitmap;
    }

    public static Bitmap cropBitmap(Bitmap bitmap, float ratio, int topH, int uiRotation, int previewH) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Rect rect = cropRect(new Rect(0, 0, width, height), ratio, topH, uiRotation, previewH);

        if (rect.width() == width && rect.height() == height) {
            return bitmap;
        }

        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
        if (squareBitmap != null) {
            bitmap.recycle();
            bitmap = squareBitmap;
        }
        return bitmap;
    }

    /**
     * 把矩形按比例最大裁剪
     * 
     * @param rect
     * @return
     */
    public static Rect cropRect(Rect rect, float ratio) {
        if (rect == null) {
            return null;
        }
        float width = rect.width();
        float height = rect.height();
        float originRatio = (float)width / height;
        if (originRatio == ratio) {
            return rect;
        }
        if (originRatio < ratio) {
            int maskHeight = (int) ((height - width / ratio) / 2f);
            return new Rect(rect.left, rect.top + maskHeight, rect.right, rect.bottom - maskHeight);
        } else {
            int maskWidth = (int) ((width - height * ratio) / 2f);
            return new Rect(rect.left + maskWidth, rect.top, rect.right - maskWidth, rect.bottom);
        }
    }

    /**
     * 把矩形按比例最大裁剪
     * @param rect
     * @param ratio
     * @param offsetY
     * @return
     */
    public static Rect cropRectWithOffsetY(Rect rect, float ratio, int offsetY) {
        if (rect == null) {
            return null;
        }
        float width = rect.width();
        float height = rect.height();
        float originRatio = (float)width / height;
        if (originRatio == ratio) {
            return rect;
        }
        if (originRatio < ratio) {
            int maskHeight = (int) ((height - width / ratio) / 2f);
            return new Rect(rect.left, rect.top + maskHeight + offsetY, rect.right, rect.bottom - maskHeight + offsetY);
        } else {
            int maskWidth = (int) ((width - height * ratio) / 2f);
            return new Rect(rect.left + maskWidth, rect.top, rect.right - maskWidth, rect.bottom);
        }
    }

    /**
     * 把矩形按比例最大裁剪
     *
     * @param rect
     * @return
     */
   /* public static Rect cropRect(Rect rect, float ratio, int topH) {
        if (rect == null) {
            return null;
        }
        float width = rect.width();
        float height = rect.height();
        float originRatio = width / height;
        if (originRatio == ratio) {
            return rect;
        }
        if (originRatio < ratio) {
            int maskHeight = (int) (rect.height() * ratio);
            return new Rect(rect.left, topH, rect.right, maskHeight - topH);
        } else {
            int top = (int) (width * topH / DeviceUtils.getScreenHeightPx(App.getContext()));
            int maskWidth = (int) ((width - height * ratio));
            return new Rect(rect.left + top, rect.top, rect.right - maskWidth + top, rect.bottom);
        }
    } */

    public static Rect cropRect(Rect rect, float ratio, int topH) {
        if (rect == null) {
            return null;
        }
        float width = rect.width();
        float height = rect.height();
        float originRatio = width / height;
        if (originRatio == ratio) {
            return rect;
        }
        if(originRatio < ratio) {
            int top = (int) (height * topH / DeviceUtils.getScreenHeightPx(App.getContext()));
            final int maskHeight = (int) (height - width / ratio);
            return new Rect(rect.left, rect.top + top, rect.right, rect.bottom - maskHeight + top);
        } else {
            int left = (int) (width * topH / DeviceUtils.getScreenHeightPx(App.getContext()));
            final int maskWidth = (int)(width - height * ratio);
            return new Rect(rect.left + left, rect.top, rect.right - maskWidth + left, rect.bottom);
        }
    }

    public static Rect cropRect(Rect rect, float ratio, int topH, int uiRotation, int previewH) {
        if (rect == null) {
            return null;
        }
        float width = rect.width();
        float height = rect.height();
        float originRatio = width / height;
        if (originRatio == ratio) {
            return rect;
        }
        if(originRatio < ratio) {
            if (uiRotation == 180) {
                int bottom = (int) (height * topH / previewH);
                final int maskHeight = (int) (height - width / ratio);
                return new Rect(rect.left, rect.top + maskHeight - bottom, rect.right, rect.bottom - bottom);
            } else {
                int top = (int) (height * topH / previewH);
                final int maskHeight = (int) (height - width / ratio);
                return new Rect(rect.left, rect.top + top, rect.right, rect.bottom - maskHeight + top);
            }
        } else {
            if (uiRotation == 90) {
                int right = (int) (width * topH / previewH);
                final int maskWidth = (int)(width - height * ratio);
                return new Rect(rect.left + maskWidth - right, rect.top, rect.right - right, rect.bottom);
            } else {
                int left = (int) (width * topH / previewH);
                final int maskWidth = (int) (width - height * ratio);
                return new Rect(rect.left + left, rect.top, rect.right - maskWidth + left, rect.bottom);
            }
        }
    }

    /**
     * 解析Jpeg图片
     * 
     * @param data
     * @return
     */
//    public static Bitmap decodeJpegData(byte[] data) {
//        Bitmap bitmap = null;
//        try {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 1;
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeStream(new ByteArrayInputStream(data), null, options);
//            if ((!options.mCancel) && (options.outWidth > 0) && options.outHeight > 0) {
//
//                int insamplesize = 1;
//                float scale = ImageHelper.getFitSampleSizeLarger(options.outWidth, options.outHeight);
//                scale = ImageHelper.checkCanvasAndTextureSize(options.outWidth, options.outHeight, scale);
//
//                int i = 1;
//                while (scale / Math.pow(i, 2) > 1.0f) {
//                    i *= 2;
//                }
//                if (i != 1) {
//                    i = i / 2;
//                }
//                insamplesize = i;
//
//                int targetDensity = App.getContext().getResources().getDisplayMetrics().densityDpi;
//                options.inScaled = true;
//                options.inDensity = (int)(targetDensity * Math.sqrt(scale / Math.pow(i, 2)) + 1);
//                options.inTargetDensity = targetDensity;
//
//                options.inSampleSize = insamplesize;
//                options.inJustDecodeBounds = false;
//                options.inDither = false;
//                options.inMutable = true;
//                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(data), null, options);
//            }
//        } catch (Throwable tr) {
//            DLog.e(TAG, "", tr);
//        }
//        return bitmap;
//    }

//    public static Bitmap decodeJpegDataBig(byte[] data, int rotation) {
//        Bitmap bitmap = null;
//        try {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 1;
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeStream(new ByteArrayInputStream(data), null, options);
//            if ((!options.mCancel) && (options.outWidth > 0) && options.outHeight > 0) {
//                int width = options.outWidth;
//                int height = options.outHeight;
//                if (rotation == 90 || rotation == 360) {
//                    width = options.outHeight;
//                    height = options.outWidth;
//                }
//                options.inSampleSize = ImageHelper.getFitSampleSizeNew(width, height);
//                options.inJustDecodeBounds = false;
//                options.inDither = false;
//                options.inMutable = true;
//                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(data), null, options);
//            }
//        } catch (Throwable tr) {
//            DLog.e(TAG, "", tr);
//        }
//        return bitmap;
//    }
    
    /**
     * 解析Jpeg图片
     * 
     * @param data
     * @return
     */
    public static Bitmap decodeJpegDataInBeauty(byte[] data) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new ByteArrayInputStream(data), null, options);
            if ((!options.mCancel) && (options.outWidth > 0) && options.outHeight > 0) {
                options.inSampleSize = ImageHelper.getFitSampleSize(options.outWidth,
                        options.outHeight, true);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inMutable = true;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(data), null, options);
            }
        } catch (Throwable tr) {
            DLog.e(TAG, "", tr);
        }
        return bitmap;
    }
    
    /**
     * 解析拍照图片，已经处理裁剪和旋转
     * 
     * @param context
     * @param data
     * @return
     */
//    public static Bitmap decodeJpegDataWithCutAndRotate(Context context, byte[] data,
//            boolean flipHorizontal, boolean flipVertical) {
//        int rotation = Exif.getOrientation(data);
//        Bitmap bitmap = BitmapUtil.decodeJpegDataInBeauty(data);
//
//        /**
//         * 1:1裁剪
//         */
//        if (SPDataManager.isCropSquare()) {
//            if (bitmap != null) {
//                bitmap = BitmapUtil.cropSquareBitmap(bitmap);
//            }
//        }
//
//        /**
//         * 图片旋转
//         */
//        if (bitmap != null && (rotation != 0 || flipHorizontal || flipVertical)) {
//            Bitmap tempBp = ImageHelper.rotating(bitmap, rotation, flipHorizontal, flipVertical);
//            if (tempBp != null) {
//                if (bitmap != null && bitmap != tempBp) {
//                    bitmap.recycle();
//                }
//                bitmap = tempBp;
//            }
//        }
//
//        return bitmap;
//    }
    
//    public static int getExifOrientation(File file) {
//        int exif_orientation = 0;
//        try {
//            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
//            String exif_orientation_s = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
//            // from http://jpegclub.org/exif_orientation.html
//            if( exif_orientation_s.equals("0") || exif_orientation_s.equals("1") ) {
//                // leave at 0
//            }
//            else if( exif_orientation_s.equals("3") ) {
//                exif_orientation = 180;
//            }
//            else if( exif_orientation_s.equals("6") ) {
//                exif_orientation = 90;
//            }
//            else if( exif_orientation_s.equals("8") ) {
//                exif_orientation = 270;
//            }
//            else {
//                // just leave at 0
//            }
//        } catch (Throwable tr) {
//            DLog.e(TAG, "", tr);
//        }
//        return exif_orientation;
//    }
    
    /**
	 * 把Bitmap转换成byteArray
	 * @param bmp
	 * @param needRecycle
	 * @return
	 */
//	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
//		ByteArrayOutputStream output = new ByteArrayOutputStream();
//		bmp.compress(CompressFormat.JPEG, 100, output);
//
//
//		if (needRecycle) {
//			bmp.recycle();
//		}
//
//		byte[] result = output.toByteArray();
//
//		try {
//			output.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return result;
//	}
	
	/**
	 * 把Bitmap转换成byteArray
	 * @param bmp
	 * @param needRecycle
	 * @return
	 */
//	public static byte[] bmpToPNGByteArray(final Bitmap bmp, final boolean needRecycle) {
//		ByteArrayOutputStream output = new ByteArrayOutputStream();
//		bmp.compress(CompressFormat.PNG, 100, output);
//
//
//		if (needRecycle) {
//			bmp.recycle();
//		}
//
//		byte[] result = output.toByteArray();
//
//		try {
//			output.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return result;
//	}
	
	/**
	 * 把Bitmap转换成byteArray
	 * @param bmp
	 * @param needRecycle
	 * @return
	 */
	public static byte[] bmpToJPGByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 100, output);
		
		
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

    /**
     * 把Bitmap转换成byteArray
     * @param bmp
     * @param image_quality
     * @param needRecycle
     * @return
     */
//    public static byte[] bmpToJPGByteArray(final Bitmap bmp, int image_quality, final boolean needRecycle) {
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        bmp.compress(CompressFormat.JPEG, image_quality, output);
//
//        if (needRecycle) {
//            bmp.recycle();
//        }
//
//        byte[] result = output.toByteArray();
//
//        try {
//            output.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }


    /**
     * 获取水印位置
     *
     * @param context
     * @param rect
     * @return
     */
//    public static RectF getWaterMarkRect(Context context, RectF rect, Watermark watermark) {
//        float scale = (float)Math.sqrt(rect.width() * rect.height() / WATER_MARK_REF_SIZE);
//        float markHeight = watermark.getHeight() * scale;
//        float markWidth = watermark.getWidth() * scale;
//        float marginX = 0;
//        float marginY = 0;
//        if (watermark.getType() == Watermark.TYPE_NORMAL) {
//            marginX = marginY = scale * 40;
//        }
//        RectF dst = new RectF(rect.left + rect.width() - markWidth - marginX,
//                rect.top + rect.height() - markHeight - marginY,
//                rect.left + rect.width() - marginX,
//                rect.top +rect.height() - marginY);
//        return dst;
//    }

    /**
     * 对图片添加水印
     *
     * @param context
     * @param bitmap
     * @return
     */
//    public static Bitmap getWaterMarkBitmap(Context context, Bitmap bitmap, WatermarkManager watermarkManager) {
//        Watermark watermark = watermarkManager.getCurrentWatermark();
//        Bitmap newBitmap = null;
//        Bitmap waterMarkBitmap = null;
//        try {
//            waterMarkBitmap = watermarkManager.getWatermarkBitmap(watermark);
//            if (waterMarkBitmap != null) {
//                newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(newBitmap);
//                canvas.drawColor(Color.TRANSPARENT);
//                Paint paint = new Paint();
//                paint.setFlags(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
//                Rect rect = canvas.getClipBounds();
//                float scale = (float)Math.sqrt(rect.width() * rect.height() / WATER_MARK_REF_SIZE);
//                float markHeight = watermark.getHeight() * scale;
//                float markWidth = watermark.getWidth() * scale;
//                float marginX = 0;
//                float marginY = 0;
//                if (watermark.getType() == Watermark.TYPE_NORMAL) {
//                    marginX = marginY = scale * 40;
//                }
//                RectF dst = new RectF(rect.width() - markWidth - marginX,
//                        rect.height() - markHeight - marginY,
//                        rect.width() - marginX,
//                        rect.height() - marginY);
//                canvas.drawBitmap(bitmap, 0, 0, paint);
//                canvas.drawBitmap(waterMarkBitmap, null, dst, paint);
//            }
//        } catch (Throwable tr) {
//            DLog.e(TAG, "", tr);
//        }
//        return newBitmap;
//    }

    
    /**
     * 对图片添加水印
     *
     * @param context
     * @param canvas
     * @return
     */
//    public static Bitmap getWaterMarkBitmap(Context context, Canvas canvas, WatermarkManager watermarkManager) {
//        Watermark watermark = watermarkManager.getCurrentWatermark();
//        Bitmap newBitmap = null;
//    	Bitmap waterMarkBitmap = null;
//        try {
//            waterMarkBitmap = watermarkManager.getWatermarkBitmap(watermark);
//            if (waterMarkBitmap != null) {
//                Paint paint = new Paint();
//                paint.setFlags(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
//                Rect rect = canvas.getClipBounds();
//                float scale = (float)Math.sqrt(rect.width() * rect.height() / WATER_MARK_REF_SIZE);
//                float markHeight = watermark.getHeight() * scale;
//                float markWidth = watermark.getWidth() * scale;
//                float marginX = 0;
//                float marginY = 0;
//                if (watermark.getType() == Watermark.TYPE_NORMAL) {
//                    marginX = marginY = scale * 40;
//                }
//                RectF dst = new RectF(rect.width() - markWidth - marginX,
//                        rect.height() - markHeight - marginY,
//                        rect.width() - marginX,
//                        rect.height() - marginY);
//                canvas.drawBitmap(waterMarkBitmap, null, dst, paint);
//            }
//        } catch (Throwable tr) {
//            DLog.e(TAG, "", tr);
//        } finally {
//            if (waterMarkBitmap != null && !waterMarkBitmap.isRecycled()) {
//                waterMarkBitmap.recycle();
//            }
//        }
//        return newBitmap;
//    }

    /**
     * 对图片添加文字水印
     * (文字可以是日期)
     *
     * @param bitmap
     * @param text
     * @param text
     * @return
     */
//    public static Bitmap getWaterMarkBitmap(Bitmap bitmap, String text, int rotation) {
//        if(TextUtils.isEmpty(text)) return null;
//        Bitmap newBitmap = null;
//        try {
//            rotation = (rotation + 360) % 360;
//            newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//
//            Canvas canvas = new Canvas(newBitmap);
//            Paint paint = new Paint();
//            paint.setFlags(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
//            Rect rect = canvas.getClipBounds();
//            float scale = (float)Math.sqrt(rect.width() * rect.height() / DATE_MARK_REF_SIZE);
//
//            TextPaint textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
//            textPaint.setTextSize(DEFAULT_DATE_MARK_TEXT_SIZE * scale);
//            textPaint.setColor(DEFAULT_DATE_MARK_TEXT_COLOR);
//            textPaint.setShadowLayer(DEFAULT_DATE_MARK_SHADER_RADIUS * scale, 0, 0, DEFAULT_DATE_MARK_SHADER_COLOR);
//            float textWidth = StaticLayout.getDesiredWidth(text, textPaint);
//            StaticLayout textLayout = new StaticLayout(text, 0, text.length(), textPaint, (int)(textWidth + 1), Layout.Alignment.ALIGN_NORMAL, 1f, 0.0f,
//                    false, TextUtils.TruncateAt.END, (int)(textWidth + 1));
//            int textHeight = textLayout.getHeight();
//
//            float distance = /*(textHeight - DEFAULT_DATE_MARK_TEXT_SIZE * scale) / 2*/0;
//
//            float marginX = scale * 30;
//            float marginY = scale * 30;
//            RectF dst = new RectF();
//            if(rotation == 90){
//                dst.set(rect.width() - textWidth - marginX,
//                        marginY - distance,
//                        rect.width() - marginX,
//                        textHeight + marginY - distance);
//            } else if(rotation == 180){
//                dst.set(marginX,
//                        marginY - distance,
//                        textWidth + marginX,
//                        textHeight + marginY - distance);
//            } else if(rotation == 270){
//                dst.set(marginX,
//                        rect.height() - textHeight - marginY + distance,
//                        textWidth + marginX,
//                        rect.height() - marginY + distance);
//            } else{//0
//                dst.set(rect.width() - textWidth - marginX,
//                        rect.height() - textHeight - marginY + distance,
//                        rect.width() - marginX,
//                        rect.height() - marginY + distance);
//            }
//
//            canvas.drawBitmap(bitmap, 0, 0, paint);
//            canvas.save();
//            if(rotation == 90) {
//                canvas.rotate(-rotation, dst.left + dst.width() - dst.height() / 2, dst.centerY());
//                canvas.translate(dst.left, dst.top);
//            } else if(rotation == 180){
//                canvas.rotate(-rotation, dst.centerX(), dst.centerY());
//                canvas.translate(dst.left, dst.top);
//            } else if(rotation == 270){
//                canvas.rotate(-90, dst.left + dst.height() / 2, dst.centerY());
//                canvas.rotate(-180, dst.centerX(), dst.centerY());
//                canvas.translate(dst.left, dst.top);
//            } else{//0
//                canvas.translate(dst.left, dst.top);
//            }
//            textLayout.draw(canvas);
//            canvas.restore();
//        } catch (Throwable tr) {
//            DLog.e(TAG, "", tr);
//        }
//        return newBitmap;
//    }
    
    /**
     * 把矩形按比例最大裁剪
     * 
     * @param rect
     * @return
     */
    public static void cropSetSquare(Rect rect, float ratio) {
        if (rect == null) {
            return;
        }
        float width = rect.width();
        float height = rect.height();
        float originRatio = (float)width / height;
        if (originRatio == ratio) {
            return;
        }
        if (originRatio < ratio) {
            int maskHeight = (int)(height - width / ratio) / 2;
            rect.set(rect.left, rect.top + maskHeight, rect.right, rect.bottom - maskHeight);
        } else {
            int maskWidth = (int)(width - height * ratio) / 2;
            rect.set(rect.left + maskWidth, rect.top, rect.right - maskWidth, rect.bottom);
        }
    }

    public static void saveOrginCurveTexture(String filePath) {
        byte[] toneCurveByteArray = new byte[256 * 4];
        for (int currentCurveIndex = 0; currentCurveIndex < 256; currentCurveIndex++) {
            // BGRA for upload to texture
            toneCurveByteArray[currentCurveIndex * 4 + 2] = (byte) ((int) Math.min(Math.max(currentCurveIndex, 0), 255) & 0xff);
            toneCurveByteArray[currentCurveIndex * 4 + 1] = (byte) ((int) Math.min(Math.max(currentCurveIndex, 0), 255) & 0xff);
            toneCurveByteArray[currentCurveIndex * 4] = (byte) ((int) Math.min(Math.max(currentCurveIndex, 0), 255) & 0xff);
            toneCurveByteArray[currentCurveIndex * 4 + 3] = (byte) ((int) Math.min(Math.max(currentCurveIndex, 0), 255) & 0xff);
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            Bitmap bitmap = Bitmap.createBitmap(256, 1, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(toneCurveByteArray));
            bitmap.compress(CompressFormat.PNG, 100, fos);
        } catch (Throwable tr) {
            tr.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height && reqHeight != -1) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else if (reqWidth != -1){
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static void saveBitmap(Bitmap bmp, String dstFile, int quality) {
        File f = new File(dstFile);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void savePngBitmap(Bitmap bmp, String dstFile, int quality) {
        File f = new File(dstFile);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(CompressFormat.PNG, quality, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveImgSizeExif(String filePath, int width, int height) {
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(filePath);
            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(width));
            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(height));
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static Bitmap copyBitmap(Bitmap bitmap) {
//        if (bitmap == null) {
//            return null;
//        }
//        Bitmap ret = null;
//        try {
//            String path = FileUtils.getExternalCacheDir(App.getContext(), "tmp") + File.separator + "tmp.jpg";
//            saveBitmap(bitmap, path, 100);
//            ret = BitmapFactory.decodeFile(path);
//            FileUtils.delete(new File(path));
//        } catch (Exception e) {
//            if (ret == null) {
//                ret = bitmap;
//            }
//        }
//        return ret;
//    }

//    public static Bitmap getImageFromAssetsFile(Resources resources, String fileName) {
//        Bitmap image = null;
//        AssetManager am = resources.getAssets();
//        try {
//            InputStream is = am.open(fileName);
//            image = BitmapFactory.decodeStream(is);
//            is.close();
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//        return image;
//
//    }

    public static void recycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}
