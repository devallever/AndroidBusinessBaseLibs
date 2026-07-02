package com.funny.gif.memes.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;

import app.allever.android.lib.core.app.App;


public class ImageLoader {

    public static CopyOnWriteArrayList<BitmapLoadListener> mListener = new CopyOnWriteArrayList<>();

    public static final String RESOURCE_PREFIX = "android.resource://";

    public static void loadImage(String url, ImageView imageView) {
        loadImage(url, -1, imageView);
    }

    public static void loadImage(String url, ImageView imageView, int width, int height) {
        loadImage(url, -1, imageView, width, height);
    }

    public static void loadImage(String url, int defaultIcon, ImageView imageView) {
        loadImage(url, defaultIcon, imageView, Priority.LOW);
    }

    public static void loadImage(String url, int defaultIcon, ImageView imageView, int width, int height) {
        loadImage(url, defaultIcon, imageView, Priority.LOW, width, height);
    }

    public static void loadImage(String url, int defaultIcon, ImageView imageView, Priority priority) {
        loadImage(url, defaultIcon, imageView, priority, null);
    }

    public static void loadImage(String url, int defaultIcon, ImageView imageView, Priority priority, int width, int height) {
        loadImage(url, defaultIcon, imageView, priority, null, width, height);
    }

    public static void loadImage(String url, int defaultIcon, ImageView imageView, Priority priority,
                                 RequestListener listener, int width, int height) {
        Glide.with(app.allever.android.lib.core.app.App.context)
                .load(url)
                .override(width, height)
                .priority(priority)
                .placeholder(defaultIcon)
                .listener(listener)
                .into(imageView);
    }

    public static void loadImage(String url, int defaultIcon, ImageView imageView, Priority priority,
                                 RequestListener listener) {
        Glide.with(App.context)
                .load(url)
                .priority(priority)
                .placeholder(defaultIcon)
                .listener(listener)
                .into(imageView);
    }

    public static void loadImage(Activity context, String url, int defaultIcon, ImageView imageView) {
        loadImage(context, url, defaultIcon, imageView, null);
    }
//    //缓存filter
//    public static HashMap<String, Bitmap> loadedFilterBitmap= new HashMap<>();
//    public static void loadFilterImage(@NotNull final Context context, @NotNull final FilterBean bean, @NotNull final Bitmap originalBitmap, @NotNull final ImageView mImageView, final ImageLoader.OnLoadBitmapListener listener) {
//        if(loadedFilterBitmap.containsKey(bean.getPackageName())){
//            Bitmap bitmap = loadedFilterBitmap.get(bean.getPackageName());
//            if(bitmap != null){
//                mImageView.setImageBitmap(bitmap);
//                if(listener != null){
//                    listener.onLoadBitmapFinish(bitmap);
//                }
//                return;
//            }
//        }
//        final GPUImageFilter filter = ImageFilterTools2.createFilterForType(context, bean);
//        final GPUImage gpuImage = new GPUImage(context);
//        new AsyncTask<Void,Void,Bitmap>(){
//            @Override
//            protected Bitmap doInBackground(Void... voids) {
//                Bitmap result = gpuImage.getExternalBitmapWithFilterApplied(originalBitmap, filter);
//                if(result != null){
//                    loadedFilterBitmap.put(bean.getPackageName(),result);
//                }
//                return result;
//            }
//
//            @Override
//            protected void onPostExecute(Bitmap bitmap) {
//                super.onPostExecute(bitmap);
//                mImageView.setImageBitmap(bitmap);
//                if(listener != null){
//                    listener.onLoadBitmapFinish(bitmap);
//                }
//            }
//        }.executeOnExecutor(AsyncTask.DATABASE_THREAD_EXECUTOR);
//    }
//    //缓存已解码文件
//    public static HashMap<String, SoftReference<Bitmap>> loadedBitmap= new HashMap<>();
//    public static void loadDeCodeImage(@NotNull final Activity context, @NotNull final ThumbnailBean bean, @NotNull final int defaultIcon, @NotNull final ImageView mImageView,final ImageLoader.OnLoadBitmapListener listener) {
//
//            File tempFile = new File(bean.getTempPath());
//            if (tempFile.exists()) {
//                //读流 图像获取bitmap、视频 获取第一帧
//                setDecodeBitmap(context, bean, defaultIcon, mImageView,listener);
//            } else {
//                //解码
//                String md5Path = MD5.getMD5Str(bean.getPath());
//                if(md5Path  == null)return;
//                PrivateHelper.Companion.decode(new File(PrivateHelper.Companion.getPATH_ENCODE_ORIGINAL(),md5Path).getAbsolutePath(), new DecodeListener() {
//                    @Override
//                    public void onDecodeStart() {
//                    }
//
//                    @Override
//                    public void onDecodeSuccess(@NotNull PrivateBean privateBean) {
//                        //读流 图像获取bitmap、视频 获取第一帧
//                        setDecodeBitmap(context, bean, defaultIcon, mImageView,listener);
//                    }
//
//                    @Override
//                    public void onDecodeFailed(@NotNull String msg) {
//                    }
//                });
//            }
//    }
//
//    public static void setDecodeBitmap(@NotNull final Activity context,@NotNull final ThumbnailBean bean, @NotNull final int defaultIcon, @NotNull final ImageView mImageView,final ImageLoader.OnLoadBitmapListener listener) {
//        if (bean.getType()!=MediaTypeUtil.TYPE_VIDEO) {
//            ImageLoader.loadImage(context,bean.getTempPath(),defaultIcon,mImageView,listener);
//        }else{
//            if(loadedBitmap.containsKey(bean.getTempPath())){
//                SoftReference<Bitmap> reference = loadedBitmap.get(bean.getTempPath());
//                Bitmap bitmap = reference.get();
//                if(bitmap != null){
//                    mImageView.setImageBitmap(bitmap);
//                    if(listener != null){
//                        listener.onLoadBitmapFinish(bitmap);
//                    }
//                    return;
//                }
//            }
//            AsyncTask<Void, Void, Bitmap> asyncTask = new AsyncTask<Void, Void, Bitmap>() {
//                @Override
//                protected void onPreExecute() {
//                    super.onPreExecute();
//                }
//
//                @Override
//                protected Bitmap doInBackground(Void... params) {
//                    //视频 获取第一帧
//                    Bitmap result = MediaThumbnailUtil.getInstance().getVideoThumbnail(bean.getTempPath(), DeviceUtils.dip2px(300));
//                    if(result != null){
//                        SoftReference<Bitmap> soft = new SoftReference<>(result);
//                        loadedBitmap.put(bean.getTempPath(),soft);
//                    }
//                    return result;
//                }
//
//                @Override
//                protected void onPostExecute(Bitmap result) {
//                    if (result != null) {
//                        mImageView.setImageBitmap(result);
//                        if(listener != null){
//                            listener.onLoadBitmapFinish(result);
//                        }
//                    }
//                }
//            };
//            asyncTask.executeOnExecutor(AsyncTask.DATABASE_THREAD_EXECUTOR);
//        }
//
//    }

//    public static Uri getImageContentUri(Context context, java.io.File imageFile) {
//        String filePath = imageFile.getAbsolutePath();
//        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
//                new String[]{filePath}, null);
//        if (cursor != null && cursor.moveToFirst()) {
//            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
//            Uri baseUri = Uri.parse("content://media/external/images/media");
//            return Uri.withAppendedPath(baseUri, "" + id);
//        } else {
//            if (imageFile.exists()) {
//                ContentValues values = new ContentValues();
//                values.put(MediaStore.Images.Media.DATA, filePath);
//                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            } else {
//                return null;
//            }
//        }
//    }

    /**
     * @param context
     * @param url
     * @param defaultIcon
     * @param imageView
     * @param listener    注意：listener会被强引用，需要加载网络图片时，不能使用此方法
     */
    public static void loadImage(Activity context, String url, int defaultIcon, ImageView imageView, final OnLoadBitmapListener listener) {
        try {
            Glide.with(context)
                    .load(url)
                    .placeholder(defaultIcon)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            if (listener != null) {
                                listener.onLoadBitmapFinish(null);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (listener != null) {
                                listener.onLoadBitmapFinish(resource);
                            }
                            return false;
                        }
                    })
                    .into(imageView);
        } catch (Exception e) {
        }
    }

    /**
     * 加载图片，先显示小图再显示大图
     *
     * @param context
     * @param url
     * @param defaultIcon
     * @param imageView
     * @param listener    注意：listener会被强引用，需要加载网络图片时，不能使用此方法
     */
    public static void loadImageWithThumbnail(Activity context, String url, int defaultIcon, ImageView imageView, final OnLoadBitmapListener listener) {
        try {
            Glide.with(context)
                    .load(url)
                    .fitCenter()
                    .placeholder(defaultIcon)
                    .thumbnail(0.1f)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            if (listener != null) {
                                listener.onLoadBitmapFinish(null);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (listener != null) {
                                listener.onLoadBitmapFinish(resource);
                            }
                            return false;
                        }
                    })
                    .into(imageView);
        } catch (Exception e) {
        }
    }

    public static void loadImage(Activity context, String url, int defaultIcon, ImageView imageView, boolean fromCache) {
        try {
            Glide.with(context)
                    .load(url)
                    .placeholder(defaultIcon)
                    .onlyRetrieveFromCache(true)
                    .into(imageView);
        } catch (Exception e) {
        }
    }

    public static void loadImageOnlyStrongRef(Activity context, String url, final OnLoadBitmapListener listener) {
        try {
            final String finalUrl = url;
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            if (listener != null) {
                                listener.onLoadBitmapFinish(null);
                            }
                        }

                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (listener != null) {
                                listener.onLoadBitmapFinish(resource);
                            }
                        }

                    });
        } catch (Exception e) {
        }
    }

    /**
     * 异步加载url，注意listener需要手动remove
     *
     * @param context
     * @param url
     * @param listener 结果回调，注意因内存问题listener只保留弱引用，外部使用时不能使用临时变量
     */
    public static void loadImageOnlyWeak(Context context, String url, OnLoadBitmapListener listener) {
        try {
            final String finalUrl = url;
            if (listener != null) {
                mListener.add(new BitmapLoadListener(finalUrl, listener));
            }
            Glide.with(context)
                    .asBitmap()
                    .load(finalUrl)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            notifyListener(finalUrl, null);
                        }

                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            notifyListener(finalUrl, resource);
                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadImageOnly(Context context, String url, @Nullable final OnLoadBitmapListener listener) {
        try {
            final String finalUrl = url;
            Glide.with(context)
                    .asBitmap()
                    .load(finalUrl)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            if (listener != null) {
                                listener.onLoadBitmapFinish(null);
                            }
                        }

                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (listener != null) {
                                listener.onLoadBitmapFinish(resource);
                            }
                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String resourceId2String(Context context, int resourceId) {
        return RESOURCE_PREFIX + context.getPackageName() + "/" + resourceId;
    }

    public static void downloadImageFile(Context context, String url) {
        Glide.with(context).downloadOnly().load(url).submit();
    }

    private static class BitmapLoadListener {
        String mUrl;
        WeakReference<OnLoadBitmapListener> mListener;

        public BitmapLoadListener(String url, OnLoadBitmapListener listener) {
            mUrl = url;
            mListener = new WeakReference<OnLoadBitmapListener>(listener);
        }
    }

    private static void notifyListener(String url, Bitmap bitmap) {
        for (BitmapLoadListener listener : mListener) {
            if (listener.mUrl.equals(url) && listener.mListener.get() != null) {
                listener.mListener.get().onLoadBitmapFinish(bitmap);
            }
        }
    }

    public static void removeListener(OnLoadBitmapListener listener) {
        for (BitmapLoadListener tmp : mListener) {
            if (tmp.mListener.get() == listener) {
                mListener.remove(tmp);
                break;
            }
        }
    }

    public interface OnLoadBitmapListener {
        void onLoadBitmapFinish(Object bitmap);
    }

    public interface OnLoadDecodeBitmapListener {
        void onLoadBitmapFinish(Object bitmap);
    }

    public static void clearMemoryCache() {
//        Sketch.with(App.getContext()).getConfiguration().getMemoryCache().clear();
        Glide.get(App.context).clearMemory();
    }

    public static void onTrimMemroy(int level) {
//        Sketch.with(App.getContext()).getConfiguration().getMemoryCache().clear();
        Glide.get(App.context).onTrimMemory(level);
    }

    public static void onLowMemory() {
        clearMemoryCache();
    }
}
