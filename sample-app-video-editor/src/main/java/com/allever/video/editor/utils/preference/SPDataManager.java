package com.allever.video.editor.utils.preference;

import com.android.absbase.utils.SpUtils;
import com.allever.video.editor.utils.FileUtil;

import java.io.File;

/**
 *
 */

public class SPDataManager {

    private static boolean sIsFirstStart = false;

    public static void init() {
        sIsFirstStart = SpUtils.obtain().getBoolean(SpConstant.IS_APP_FIRST_START, true);
        SpUtils.obtain().save(SpConstant.IS_APP_FIRST_START, false);
    }

    public static void setPhotoSaveLocation(String location) {
        SpUtils.obtain().save(SpConstant.PHOTO_SAVE_LOCATION, location);
    }

    public static String getPhotoSaveLocation() {
        return SpUtils.obtain().getString(SpConstant.PHOTO_SAVE_LOCATION, FileUtil.DICM_ROOT_PATH + File.separator + "Camera");
    }

    public static int getMaxTextureSize() {
        return SpUtils.obtain().getInt(SpConstant.MAX_TEXTURE_SIZE, 0);
    }

    public static void setMaxTextureSize(int size) {
        SpUtils.obtain().save(SpConstant.MAX_TEXTURE_SIZE, size);
    }

    public static String getShareImageTool1PkgName() {
        return SpUtils.obtain().getString(SpConstant.LAST_SHARE_IMAGE_TOOL1_PKGNAME, null);
    }

    public static String getShareImageTool1ActivityName() {
        return SpUtils.obtain().getString(SpConstant.LAST_SHARE_IMAGE_TOOL1_ACTIVITY_NAME, null);
    }

    public static String getShareImageTool2PkgName() {
        return SpUtils.obtain().getString(SpConstant.LAST_SHARE_IMAGE_TOOL2_PKGNAME, null);
    }

    public static String getShareImageTool2ActivityName() {
        return SpUtils.obtain().getString(SpConstant.LAST_SHARE_IMAGE_TOOL2_ACTIVITY_NAME, null);
    }

    public static void setShareImageTool1PkgName(String name) {
        SpUtils.obtain().save(SpConstant.LAST_SHARE_IMAGE_TOOL1_PKGNAME, name);
    }

    public static void setShareImageTool1ActivityName(String name) {
        SpUtils.obtain().save(SpConstant.LAST_SHARE_IMAGE_TOOL1_ACTIVITY_NAME, name);
    }

    public static void setShareImageTool2PkgName(String name) {
        SpUtils.obtain().save(SpConstant.LAST_SHARE_IMAGE_TOOL2_PKGNAME, name);
    }

    public static void setShareImageTool2ActivityName(String name) {
        SpUtils.obtain().save(SpConstant.LAST_SHARE_IMAGE_TOOL2_ACTIVITY_NAME, name);
    }
}
