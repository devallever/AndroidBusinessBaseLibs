package app.allever.android.ai.qr.scanner.core.encode.custom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.lang.ref.SoftReference;
import java.util.HashMap;

public class CustomQrCodeUtils {
    /**
     * 圆形图片
     * @param sourceBitmap
     * @param radius
     * @return
     */
    public static  Bitmap getRoundedCornerBitmap(Bitmap sourceBitmap, float radius) {
        try {
            Bitmap targetBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(targetBitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Rect rect = new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());
            RectF rectF = new RectF(rect);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawRoundRect(rectF, radius, radius, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(sourceBitmap, rect, rect, paint);
            return targetBitmap;
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 根据外边框大小等比例缩放
     * @param path
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap getScaleBitmap(String path, int maxWidth, int maxHeight) {
        if (path == null) {
            return null;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int width = options.outWidth;
            int height = options.outHeight;
            int inSampleSize = 1;
            if (width > maxWidth || height > maxHeight) {
                int halfWidth = width / 2;
                int halfHeight = height / 2;
                while (halfWidth / inSampleSize >= maxWidth && halfHeight / inSampleSize >= maxHeight) {
                    inSampleSize *= 2;
                }
            }
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            Bitmap srcBitmap = BitmapFactory.decodeFile(path, options);

            Bitmap bitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            Rect dstRect = new Rect(0, 0, maxWidth, maxHeight);
            int x = Math.abs(maxWidth - srcBitmap.getWidth()) / 2;
            int y = Math.abs(maxHeight - srcBitmap.getHeight()) / 2;
            Rect srcRect = new Rect(x, y, x + maxWidth, y + maxHeight);
            canvas.drawBitmap(srcBitmap, srcRect, dstRect, new Paint());
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static HashMap<String, SoftReference<Bitmap>> photoCachesMap = new HashMap<>();
    public static Bitmap getBitmap(String path, int maxWidth, int maxHeight){
        SoftReference<Bitmap> reference = photoCachesMap.get(path);
        if(reference!=null){
            Bitmap bitmap = reference.get();
            if(bitmap != null){
                return bitmap;
            }
        }
        Bitmap scaleBitmap = getScaleBitmap(path, maxWidth, maxHeight);
        if(scaleBitmap != null){
            photoCachesMap.put(path,new SoftReference<Bitmap>(scaleBitmap));
        }
        return scaleBitmap;
    }
}
