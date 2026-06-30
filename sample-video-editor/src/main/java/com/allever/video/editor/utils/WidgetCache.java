//package com.photoeditor.utils;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.CompressFormat;
//import com.android.absbase.App;
//import android.text.TextUtils;
//
//import com.allever.video.editor.function.media.ImageCache;
//import com.photoeditor.function.gallery.utils.ImageWorker;
//
//import java.io.File;
//
///**
// * Widget图片的cache
// */
//public class WidgetCache extends ImageWorker {
//	public static final String IMAGE_CACHE_DIR = "widget";
//	private File mImageCacheDir;
//	private ImageCache mImageCache;
//    private ImageCache.ImageCacheParams mImageCacheParams;
//
//    private static boolean CHECK_JOURNAL = true;
//
// 	private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 100; // 100MB
//
// 	private static final int DEFAULT_MOMERY_CACHE_SIZE = 1024 * 1024 * 30; // 20MB
//
//
//	private void init(Context context) {
//		mImageCacheDir = ImageCache.getDiskCacheDir(context, IMAGE_CACHE_DIR);
//		if (!mImageCacheDir.exists()) {
//			mImageCacheDir.mkdirs();
//		}
//	}
//
//	public WidgetCache(Context context) {
//		super(context);
//		init(context);
//		/**
//		 * 传入目录名称
//		 */
//		mImageCacheParams = new ImageCache.ImageCacheParams(
//				App.getContext(), IMAGE_CACHE_DIR);
//		mImageCacheParams.compressFormat = CompressFormat.JPEG;
//        mImageCacheParams.compressQuality = 100;
//        mImageCacheParams.diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
//        mImageCacheParams.memCacheSize = DEFAULT_MOMERY_CACHE_SIZE;
//        // Set memory cache to 25% of mem class
//        mImageCacheParams.setMemCacheSizePercent(App.getContext(), 0.25f);
//        if (mImageCache == null) {
//            addImageCache(mImageCacheParams, CHECK_JOURNAL);
//            mImageCache = getImageCache();
//        } else {
//            setImageCache(mImageCache);
//            // because we can only use addImageCache to init Disk Cache, so here
//            // we call it again
//            addImageCache(mImageCacheParams, CHECK_JOURNAL);
//        }
//        CHECK_JOURNAL = false;
//	}
//
//	/**
//	 * do nothing
//	 */
//	@Override
//	protected Bitmap processBitmap(Object data, int degree,
//			AsyncTask<Object, Object, Bitmap> bitmapWorkerTask) {
//		return null;
//	}
//
//	/**
//	 * 从缓存中获取Bitmap
//	 * @param pathString
//	 * @return
//	 */
//	public Bitmap getBitmapFromCache(String pathString){
//		if(!TextUtils.isEmpty(pathString)){
//			Bitmap bitmap = mImageCache.getBitmapFromMemCache(pathString);
//			if(bitmap == null || bitmap.isRecycled()){
//				bitmap = mImageCache.getBitmapFromDiskCache(pathString);
//			}
//			return bitmap;
//		}
//		return null;
//	}
//
//	/**
//	 * 将Bitmap加入到缓存中
//	 * @param data
//	 * @param bitmap
//	 * @param addToMemoryCache
//	 * @param isNeedAddDiskCache
//	 */
//	public void addBitmapToCache(String data, Bitmap bitmap, boolean addToMemoryCache, boolean isNeedAddDiskCache){
//		if(bitmap != null && !bitmap.isRecycled() && !TextUtils.isEmpty(data)){
//			mImageCache.addBitmapToCache(data, bitmap, addToMemoryCache, isNeedAddDiskCache);
//		}
//	}
//
//}
