//package com.photoeditor.utils;
//
//
//import android.graphics.Bitmap.CompressFormat;
//import com.android.absbase.App;
//import com.android.absbase.utils.DeviceUtils;
//
//import com.allever.video.editor.function.media.BitmapCache;
//import com.allever.video.editor.function.media.ImageCache;
//import com.allever.video.editor.function.media.ImageFetcher;
//import com.allever.video.editor.function.media.ImageUtil;
//import com.photoeditor.R;
//
//public class FetcherHolder {
//    private static ImageFetcher LARGE_IMAGE_FETCHER;
//    private static ImageFetcher SMALL_IMAGE_FETCHER;
//    private static BitmapCache PREVIEW_IMAGE_FETCHER;
//    private static WidgetCache WIDGET_IMAGE_FETCHER;
//
//    // public static final String LARGE_IMAGE_CACHE_DIR = "lcache";
//    // public static final String SMALL_IMAGE_CACHE_DIR = "scache";
//
//    public static final String PI_IMAGE_CACHE_DIR = "lcache";
//
//    private static ImageCache IMAGE_CACHE;
//    private static boolean CHECK_JOURNAL = true;
//
//    // TODO : jjz considering to upgrade the image cache to use same fetcher and
//    // same imagecache, bitmap should be with the size info, when upgrade getcai
//    private static void addImageCacheToFetcher(ImageFetcher fetcher) {
//        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
//                App.getContext(), PI_IMAGE_CACHE_DIR);
//        cacheParams.compressFormat = CompressFormat.JPEG;
//        cacheParams.compressQuality = 100;
//        // Set memory cache to 25% of mem class
////        cacheParams.setMemCacheSizePercent(MmsApp.getApplication(), 0.25f);
//        cacheParams.setMemCacheSizePercent(App.getContext(), 0.25f);
//        if (IMAGE_CACHE == null) {
//            fetcher.addImageCache(cacheParams, CHECK_JOURNAL);
//            IMAGE_CACHE = fetcher.getImageCache();
//        } else {
//            fetcher.setImageCache(IMAGE_CACHE);
//            // because we can only use addImageCache to init Disk Cache, so here
//            // we call it again
//            fetcher.addImageCache(cacheParams, CHECK_JOURNAL);
//        }
//        CHECK_JOURNAL = false;
//    }
//
//    public static ImageFetcher getLargeImageFetcher() {
//        if (LARGE_IMAGE_FETCHER == null) {
//            // In fact we don't use this size at all. Because inPurgeable will
//            // make bitmap smaller than reducing size
//            LARGE_IMAGE_FETCHER = new ImageFetcher(App.getContext(),
//                    Math.min(DeviceUtils.getScreenWidthPx(),
//                            ImageUtil.IMAGE_WIDTH));
//            LARGE_IMAGE_FETCHER.setLoadingImage(R.drawable.album_icon_default);
//            addImageCacheToFetcher(LARGE_IMAGE_FETCHER);
//        }
//        return LARGE_IMAGE_FETCHER;
//    }
//
//    public static ImageFetcher getSmallImageFetcher() {
//        // The ImageFetcher takes care of loading images into our ImageView
//        // children asynchronously
//        if (SMALL_IMAGE_FETCHER == null) {
//            SMALL_IMAGE_FETCHER = new ImageFetcher(App.getContext(),
//                    ImageUtil.getSmallImageCacheWidth());
//            SMALL_IMAGE_FETCHER.setLoadingImage(R.drawable.album_icon_default);
//            addImageCacheToFetcher(SMALL_IMAGE_FETCHER);
//        }
//        return SMALL_IMAGE_FETCHER;
//    }
//
//
//    public static void closeImageFetchers() {
//        if (FetcherHolder.LARGE_IMAGE_FETCHER != null) {
//            FetcherHolder.LARGE_IMAGE_FETCHER.closeCache();
//            FetcherHolder.LARGE_IMAGE_FETCHER = null;
//        }
//        if (FetcherHolder.SMALL_IMAGE_FETCHER != null) {
//            FetcherHolder.SMALL_IMAGE_FETCHER.closeCache();
//            FetcherHolder.SMALL_IMAGE_FETCHER = null;
//        }
//
//        if (FetcherHolder.PREVIEW_IMAGE_FETCHER != null) {
//            FetcherHolder.PREVIEW_IMAGE_FETCHER.closeCache();
//            FetcherHolder.PREVIEW_IMAGE_FETCHER = null;
//        }
//
//        if (FetcherHolder.WIDGET_IMAGE_FETCHER != null) {
//            FetcherHolder.WIDGET_IMAGE_FETCHER.closeCache();
//            FetcherHolder.WIDGET_IMAGE_FETCHER = null;
//        }
//        IMAGE_CACHE = null;
//        CHECK_JOURNAL = true;
//    }
//
//    public static BitmapCache getPreviewBitmapCache() {
//        // The ImageFetcher takes care of loading images into our ImageView
//        // children asynchronously
//        if (PREVIEW_IMAGE_FETCHER == null) {
//        	PREVIEW_IMAGE_FETCHER = new BitmapCache(App.getContext());
//        }
//        return PREVIEW_IMAGE_FETCHER;
//    }
//
//    public static WidgetCache getWidgetBitmapCache() {
//        // The ImageFetcher takes care of loading images into our ImageView
//        // children asynchronously
//        if (WIDGET_IMAGE_FETCHER == null) {
//            WIDGET_IMAGE_FETCHER = new WidgetCache(App.getContext());
//        }
//        return WIDGET_IMAGE_FETCHER;
//    }
//}
