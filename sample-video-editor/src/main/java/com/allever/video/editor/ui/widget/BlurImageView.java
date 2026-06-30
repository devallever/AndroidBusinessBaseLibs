package com.allever.video.editor.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.SoftReference;
import java.util.HashMap;

public class BlurImageView extends View {

    private Paint mPaint;
    /**
     * 缓存模糊图片
     */
    private HashMap<Drawable, SoftReference<Bitmap>> drawable2BitmapMap = new HashMap<>();
    private HashMap<Integer, SoftReference<Bitmap>> flurBitmapMap = new HashMap<>();

    public BlurImageView(Context context) {
        this(context, null);
    }

    public BlurImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlurImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mBitmap != null) {
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            setMeasuredDimension(width, height);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        final int hascode = bitmap.hashCode();
        SoftReference<Bitmap> bitmapSoftReference = flurBitmapMap.get(hascode);
        this.blurBitmap = bitmapSoftReference != null ? bitmapSoftReference.get() : null;
        if (this.blurBitmap == null) {
            new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected void onPreExecute() {

                }

                @Override
                protected Bitmap doInBackground(String... integers) {
                    Bitmap blurBitmap = fastBlur(mBitmap, 1f, 26);
                    flurBitmapMap.put(hascode, new SoftReference<Bitmap>(blurBitmap));
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void setDrawable(Drawable drawable) {
        SoftReference<Bitmap> softReference = drawable2BitmapMap.get(drawable);
        Bitmap bitmap = null;
        if (softReference != null && softReference.get() != null) {
            bitmap = softReference.get();
        } else {
            bitmap = drawable2bitmap(drawable);
            drawable2BitmapMap.put(drawable, new SoftReference<Bitmap>(bitmap));
        }
        setBitmap(bitmap);
    }

    private Bitmap mBitmap;
    private Bitmap blurBitmap;

    public void reset() {
        invalidate();
    }

    public void unReset() {
        invalidate();
    }

    private Bitmap getBlurBitmap() {
        Bitmap bitmap = mBitmap;
        Bitmap blurBitmap = this.blurBitmap;
        if (bitmap != null) {
            SoftReference<Bitmap> bitmapSoftReference = flurBitmapMap.get(bitmap.hashCode());
            if (bitmapSoftReference != null) {
                blurBitmap = bitmapSoftReference.get();
                this.blurBitmap = blurBitmap;
            }
        }
        return blurBitmap;
    }

    private int mBlurAlpha = 0;

    public void setBlurAlpha(int alpha) {
        mBlurAlpha = alpha;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(this.mBitmap, 0, 0, null);

        if (mBlurAlpha != 0) {
            Bitmap blurBitmap = getBlurBitmap();
            if (blurBitmap != null) {
                mPaint.setAlpha(mBlurAlpha);
                canvas.drawBitmap(blurBitmap, 0, 0, mPaint);
            }
        }
    }

    private Bitmap drawable2bitmap(Drawable item) {
        Drawable drawable = item;
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        try {
            Bitmap bitmap;
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private static Bitmap fastBlur(Bitmap sentBitmap, float scale, int radius) {
        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        if (width == 0 || height == 0) {
            return null;
        }
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        if (radius < 1) {
            return (null);
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;
        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }
        yw = yi = 0;
        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;
        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;

                bsum += binsum;


                stackpointer = (stackpointer + 1) % div;

                sir = stack[(stackpointer) % div];


                routsum += sir[0];

                goutsum += sir[1];

                boutsum += sir[2];


                rinsum -= sir[0];

                ginsum -= sir[1];

                binsum -= sir[2];


                yi++;

            }

            yw += w;

        }

        for (x = 0; x < w; x++) {

            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;

            yp = -radius * w;

            for (i = -radius; i <= radius; i++) {

                yi = Math.max(0, yp) + x;


                sir = stack[i + radius];


                sir[0] = r[yi];

                sir[1] = g[yi];

                sir[2] = b[yi];


                rbs = r1 - Math.abs(i);


                rsum += r[yi] * rbs;

                gsum += g[yi] * rbs;

                bsum += b[yi] * rbs;


                if (i > 0) {

                    rinsum += sir[0];

                    ginsum += sir[1];

                    binsum += sir[2];

                } else {

                    routsum += sir[0];

                    goutsum += sir[1];

                    boutsum += sir[2];

                }


                if (i < hm) {

                    yp += w;

                }

            }

            yi = x;

            stackpointer = radius;

            for (y = 0; y < h; y++) {

                // Preserve alpha channel: ( 0xff000000 & pix[yi] )

                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];


                rsum -= routsum;

                gsum -= goutsum;

                bsum -= boutsum;


                stackstart = stackpointer - radius + div;

                sir = stack[stackstart % div];


                routsum -= sir[0];

                goutsum -= sir[1];

                boutsum -= sir[2];


                if (x == 0) {

                    vmin[y] = Math.min(y + r1, hm) * w;

                }

                p = x + vmin[y];


                sir[0] = r[p];

                sir[1] = g[p];

                sir[2] = b[p];


                rinsum += sir[0];

                ginsum += sir[1];

                binsum += sir[2];


                rsum += rinsum;

                gsum += ginsum;

                bsum += binsum;


                stackpointer = (stackpointer + 1) % div;

                sir = stack[stackpointer];


                routsum += sir[0];

                goutsum += sir[1];

                boutsum += sir[2];


                rinsum -= sir[0];

                ginsum -= sir[1];

                binsum -= sir[2];


                yi += w;

            }

        }


        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);

    }
}
