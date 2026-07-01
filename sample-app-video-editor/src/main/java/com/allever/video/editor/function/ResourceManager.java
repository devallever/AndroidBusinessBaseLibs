package com.allever.video.editor.function;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.absbase.App;
import com.android.absbase.utils.LimitedLinkedHashMap;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

public class ResourceManager {

    private static final String TAG = ResourceManager.class.getSimpleName();

    private static ResourceManager mInstance;
    private LinkedHashMap<String, Resources> mResources;

    private ResourceManager() {
        mResources = new LimitedLinkedHashMap<>(2, 0.5f, true);
    }

    public synchronized static ResourceManager getInstance() {
        if (mInstance == null) {
            mInstance = new ResourceManager();
        }
        return mInstance;
    }

    public Resources getResources(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return null;
        }
        final Context context = App.getContext();
        if (App.getPackageName().equals(pkgName)) {
            return context.getResources();
        }
        Resources res = mResources.get(pkgName);
        if (res == null) {
            Context mApkContext = null;
            try {
                mApkContext = context.createPackageContext(pkgName, Context.CONTEXT_IGNORE_SECURITY);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (mApkContext != null) {
                res = mApkContext.getResources();
                mResources.put(pkgName, res);
                return res;
            }
        }
        return res;
    }

    /**
     * 获取未安装Apk的信息
     *
     * @param path
     * @return
     */
    public static Resources getApkResource(String path, String packageName) {
        File file = new File(path);
        if (file.exists()) {
            Log.d(TAG, "getApkResource: apkFileExist");
            Resources resources = getApkResources(App.getContext(), path);
            return resources;
        }
        return null;
    }

    /**
     * 获取指定路径的未安装包的资源引用
     *
     * @param context
     * @param path
     * @return
     */
    public static Resources getApkResources(Context context, String path) {
        Resources res = null;
        if (new File(path).exists()) {
            try {
                Class<?> clsAssetManager = Class.forName("android.content.res.AssetManager");
                Object assetMag = clsAssetManager.newInstance();
                Method methodaddAssetPath = clsAssetManager.getDeclaredMethod("addAssetPath",
                        String.class);
                methodaddAssetPath.invoke(assetMag, path);
                res = context.getResources();
                Constructor<?> constructorResources = Resources.class.getConstructor(
                        clsAssetManager, res.getDisplayMetrics().getClass(), res.getConfiguration()
                                .getClass());
                res = (Resources) constructorResources.newInstance(assetMag, res.getDisplayMetrics(),
                        res.getConfiguration());
            } catch (Exception ignored) {
            }
        }
        return res;
    }

    private static final String ANDROID_RESOURCE = "android.resource://";
    private static final String FOREWARD_SLASH = "/";

    public static Uri resourceIdToUri(Context context, int resourceId) {
        return Uri.parse(ANDROID_RESOURCE + context.getPackageName() + FOREWARD_SLASH + resourceId);
    }
}
