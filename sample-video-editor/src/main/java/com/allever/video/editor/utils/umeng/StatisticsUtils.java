package com.allever.video.editor.utils.umeng;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//import com.photoeditor.function.camera.ui.CameraFragment;


/**
 *
 */

public class StatisticsUtils {


    public static void statisicsCustom(String prefix, String event, @Nullable String key, @Nullable String value) {
//        String buyuser = BuyUserManager.getInstance().getBuyUserSource();
//        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
//            com.statistics.StatisticsUtils.statisics(prefix, event,
//                    StatisticsConstant.EVENT_KEY_BUYUSERSOURCE, buyuser,
//                    "usertype", String.valueOf(InappAdManager.getInstance().isBuyUser()));
//        } else {
//            com.statistics.StatisticsUtils.statisics(prefix, event,
//                    key, value,
//                    StatisticsConstant.EVENT_KEY_BUYUSERSOURCE, buyuser,
//                    "usertype", String.valueOf(InappAdManager.getInstance().isBuyUser()));
//        }
    }

    public static void statisicsCustom(String prefix, String event, @Nullable String state) {
//        statisicsCustom(prefix, event, StatisticsConstant.EVENT_KEY_STATE, state);
    }


    public static void statisicsCustomFunction(String event, @Nullable String state) {
//        statisicsCustom(StatisticsConstant.EVENT_PREFIX_FUNCTION, event,
//                state);
    }

    public static void statisicsCustomFunction(String event) {
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION, event);
    }

    public static void statisicsCustomFunction(String event, String key, String value) {
//        statisicsCustom(StatisticsConstant.EVENT_PREFIX_FUNCTION, event,
//                key, value);
    }

    // 主界面事件——点击
    public static void statisticsCustomCampageClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_CAMPAGE, StatisticsConstant.EVENT_KEY_CLICK, who);
    }
    // 主界面事件——show
    public static void statisticsCustomCampageShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_CAMPAGE, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    // Photo editor 主界面事件——点击
    public static void statisticsCustomMainpageClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // Photo editor 主界面事件——展示
    public static void statisticsCustomMainpageShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    // 图片预览（编辑）事件——点击
    public static void statisticsCustomPreviewClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_PREVIEW, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomPreviewShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_PREVIEW, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    // 滤镜商店事件——点击
    public static void statisticsCustomStoreClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE, StatisticsConstant.EVENT_KEY_CLICK, who);
    }
    // 滤镜商店事件——展示
    public static void statisticsCustomStoreShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    // 暂存页面点击统计
    public static void statisticsStashClick(String who) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STASH, StatisticsConstant.EVENT_KEY_CLICK, who);
    }
    // 暂存页面状态统计
    public static void statisticsStashStatus(String who) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STASH, StatisticsConstant.EVENT_KEY_STATUS, who);

    }
    // 视频暂存页面点击统计
    public static void statisticsVideoStashClick(String who) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_VIDEO_STASH, StatisticsConstant.EVENT_KEY_CLICK, who);
    }
    // 设置页面点击统计
    public static void statisticsSettingClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_SETTING, StatisticsConstant.EVENT_KEY_CLICK, who);
    }


    // 主界面beauty按钮事件——点击
    public static void statisticsCustomBeautyBtnClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_BTN_BEAUTY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 主界面collage按钮事件——点击
    public static void statisticsCustomCollageBtnClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_BTN_COLLAGE, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomFreestyleBtnClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_BTN_FREESTYLE, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 主界面eidt按钮事件——点击
    public static void statisticsCustomEditBtnClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_BTN_EDIT, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 主界面selfie按钮事件——点击
    public static void statisticsCustomSelfieBtnClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_BTN_SELFIE, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 主界面gallery按钮事件——点击
    public static void statisticsCustomGalleryBtnClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_BTN_GALLERY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 主界面effect按钮事件——点击
    public static void statisticsCustomEffectBtnClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_BTN_EFFECT, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 资源应用次数 包名区分 all
    public static void statisticsCustomStoreAllApply(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_ALL_APPLY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreAllDownload(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_ALL_DOWNLOAD, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreAllShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_ALL_SHOW, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    public static void statisticsCustomStoreAllClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_ALL_CLICK, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 资源应用次数 包名区分 主界面
    public static void statisticsCustomStoreMainApply(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_MAIN_APPLY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreMainDownload(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_MAIN_DOWNLOAD, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreMainShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_MAIN_SHOW, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    public static void statisticsCustomStoreMainClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_MAIN_CLICK, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 资源应用次数 包名区分 贴纸商店
    public static void statisticsCustomStoreStickerApply(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_STICKER_APPLY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreStickerDownload(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_STICKER_DOWNLOAD, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreStickerShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_STICKER_SHOW, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    public static void statisticsCustomStoreStickerClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_STICKER_CLICK, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 资源应用次数 包名区分 滤镜商店
    public static void statisticsCustomStoreFilterApply(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_FILTER_APPLY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreFilterDownload(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_FILTER_DOWNLOAD, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreFilterShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_FILTER_SHOW, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    public static void statisticsCustomStoreFilterClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_FILTER_CLICK, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    // 资源应用次数 包名区分 背景商店
    public static void statisticsCustomStoreBackgroundApply(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_BACKGROUND_APPLY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreBackgroundDownload(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_BACKGROUND_DOWNLOAD, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreBackgroundShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_BACKGROUND_SHOW, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    public static void statisticsCustomStoreBackgroundClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_BACKGROUND_CLICK, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreTemplateClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_TEMPLATE_CLICK, StatisticsConstant.EVENT_KEY_CLICK, who);
    }
    public static void statisticsCustomStoreTemplateApply(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_TEMPLATE_APPLY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }
    public static void statisticsCustomStoreTemplateDownload(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_TEMPLATE_DOWNLOAD, StatisticsConstant.EVENT_KEY_CLICK, who);
    }
    public static void statisticsCustomStoreTemplateShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_TEMPLATE_SHOW, StatisticsConstant.EVENT_KEY_SHOW, who);
    }



    //商店gif
    public static void statisticsCustomStoreGifApply(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_GIF_APPLY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomStoreGifClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_GIF_CLICK, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    public static void statisticsCustomStoreGifShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_STORE_GIF_SHOW, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    //主页gif
    public static void statisticsCustomMainGifApply(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAIN_GIF_APPLY, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomMainGifClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAIN_GIF_CLICK, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsCustomMainGifShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAIN_GIF_SHOW, StatisticsConstant.EVENT_KEY_SHOW, who);
    }

    // Freestyle——点击
    public static void statisticsCustomFreestyleClick(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_FREESTYLE, StatisticsConstant.EVENT_KEY_CLICK, who);
    }
    // Freestyle——展示
    public static void statisticsCustomFreestyleShow(String who){
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_FREESTYLE, StatisticsConstant.EVENT_KEY_SHOW, who);
    }


    public static void statisicsCustomFunction(String event, int duration) {
//        statisicsCustom(StatisticsConstant.EVENT_PREFIX_FUNCTION, event,
//                duration);
    }

    public static void statisicsCustom(String prefix, String event, int duration) {
//        com.statistics.StatisticsUtils.statisics(prefix, event, duration);
    }


    public static void statisicsCustomSettings(String event, @Nullable String state) {
//        statisicsCustom(StatisticsConstant.EVENT_PREFIX_SETTING, event,
//                state);
    }

    public static void statisicsCustomLockscreen(@NonNull String type) {
//        statisicsCustom(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_LOCKSCREEN,
//                StatisticsConstant.EVENT_KEY_TYPE, type);
    }

    public static void statisicsCustomBuyUser(String source, String type) {
//        com.statistics.StatisticsUtils.statisics(
//                StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_BUYUSER,
//                StatisticsConstant.EVENT_KEY_TYPE, type,
//                StatisticsConstant.EVENT_FUNC_BUYUSER_KEY_SOURCE, source);
    }

    public static void statisicsCustomNotificationBar(String type) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_NOTIFICATIONBAR,
//                StatisticsConstant.EVENT_KEY_TYPE, type);
    }

    public static void statisicsCustomMainPage(String action) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAIN_PAGE,
//                StatisticsConstant.EVENT_KEY_ACTION, action);
    }

    public static void statisicsCustomRateGuide(String action) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_RATE_GUIDE,
//                StatisticsConstant.EVENT_KEY_ACTION, action);
    }

    public static void statisicsCustomDesktopWidget(String action) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_WIDGET,
//                StatisticsConstant.EVENT_KEY_ACTION, action);
    }

    public static void statisicsCustomIncallGuide(String action) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_INCALL_GUIDE,
//                StatisticsConstant.EVENT_KEY_ACTION, action);
    }

    public static void statisticsShareDialogClick(String from, String who){
//        statisicsCustomFunction(from, StatisticsConstant.EVENT_KEY_CLICK, who);
    }

    public static void statisticsSaveImgError(String msg) {
//        String model = android.os.Build.BRAND + ":" + android.os.Build.MODEL + ":" + Build.VERSION.SDK_INT;
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_SAVE_IMG_ERROR,
//                StatisticsConstant.EVENT_FUNC_ERROR_MSG, msg,
//                StatisticsConstant.EVENT_FUNC_ERROR_PHONE, model,
//                EVENT_FUNC_STORAGE_SIZE, FileUtil.getExternalStorageInfoSize());
    }

    public static void statisticsEditResult(boolean result) {
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_EDIT_RESULT,
//                StatisticsConstant.EVENT_FUNC_RESULT, String.valueOf(result));
    }

    public static void statisticsGridResult(boolean result) {
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_GRID_RESULT,
//                StatisticsConstant.EVENT_FUNC_RESULT, String.valueOf(result));
    }

    public static void statisticsFreeStyleResult(boolean result) {
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_FREESTYLE_RESULT,
//                StatisticsConstant.EVENT_FUNC_RESULT, String.valueOf(result));
    }

    public static void statisticsMakeVideoResult(boolean result) {
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_MAKE_VIDEO_RESULT,
//                StatisticsConstant.EVENT_FUNC_RESULT, String.valueOf(result));
    }

    public static void statisticsMakeVideoError(String msg, int videoW, int videoH) {
//        String model = android.os.Build.BRAND + ":" + android.os.Build.MODEL + ":" + Build.VERSION.SDK_INT + ":" + Build.CPU_ABI;
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_MAKE_VIDEO_ERROR,
//                StatisticsConstant.EVENT_FUNC_ERROR_MSG, msg,
//                StatisticsConstant.EVENT_FUNC_ERROR_PHONE, model,
//                StatisticsConstant.EVENT_FUNC_RES_SIZE, videoW + "_" + videoH,
//                EVENT_FUNC_STORAGE_SIZE, FileUtil.getExternalStorageInfoSize());
    }

    public static void statisticsFfmpegError(String cmd, String msg, boolean fileExist, boolean ffmpegExec) {
//        String model = android.os.Build.BRAND + ":" + android.os.Build.MODEL + ":" + Build.VERSION.SDK_INT + ":" + Build.CPU_ABI;
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_MAKE_VIDEO_ERROR_FFMPEG,
//                StatisticsConstant.EVENT_FUNC_CMD, cmd,
//                StatisticsConstant.EVENT_FUNC_ERROR_MSG, msg,
//                StatisticsConstant.EVENT_FUNC_ERROR_PHONE, model,
//                StatisticsConstant.EVENT_FUNC_OUTPUT_FILE_EXIST, String.valueOf(fileExist),
//                StatisticsConstant.EVENT_FUNC_FFMPEG_EXEC, String.valueOf(ffmpegExec),
//                EVENT_FUNC_STORAGE_SIZE, FileUtil.getExternalStorageInfoSize());
    }

    public static void statisticsFfmpegDTEntryError(String cmd, String msg, boolean fileExist, boolean ffmpegExec) {
//        String model = android.os.Build.BRAND + ":" + android.os.Build.MODEL + ":" + Build.VERSION.SDK_INT + ":" + Build.CPU_ABI;
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_FFMPEG_DT_ENTRY_ERROR,
//                StatisticsConstant.EVENT_FUNC_CMD, cmd,
//                StatisticsConstant.EVENT_FUNC_ERROR_MSG, msg,
//                StatisticsConstant.EVENT_FUNC_ERROR_PHONE, model,
//                StatisticsConstant.EVENT_FUNC_OUTPUT_FILE_EXIST, String.valueOf(fileExist),
//                StatisticsConstant.EVENT_FUNC_FFMPEG_EXEC, String.valueOf(ffmpegExec),
//                EVENT_FUNC_STORAGE_SIZE, FileUtil.getExternalStorageInfoSize());
    }

    public static void statisticsFfmpegNoFileError(String cmd, String msg, boolean fileExist, boolean ffmpegExec, String noFileName) {
//        String model = android.os.Build.BRAND + ":" + android.os.Build.MODEL + ":" + Build.VERSION.SDK_INT + ":" + Build.CPU_ABI;
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_FFMPEG_NO_FILE_ERROR,
//                StatisticsConstant.EVENT_FUNC_CMD, cmd,
//                StatisticsConstant.EVENT_FUNC_ERROR_MSG, msg,
//                StatisticsConstant.EVENT_FUNC_ERROR_PHONE, model,
//                StatisticsConstant.EVENT_FUNC_OUTPUT_FILE_EXIST, String.valueOf(fileExist),
//                StatisticsConstant.EVENT_FUNC_NO_FILE_NAME, noFileName,
//                StatisticsConstant.EVENT_FUNC_FFMPEG_EXEC, String.valueOf(ffmpegExec),
//                EVENT_FUNC_STORAGE_SIZE, FileUtil.getExternalStorageInfoSize());
    }

    public static void statisticsTakeImgResult(boolean result) {
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_TAKE_IMG_RESULT,
//                StatisticsConstant.EVENT_FUNC_RESULT, String.valueOf(result));
    }

    public static void statisticsTakeVideoResult(boolean result) {
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_TAKE_VIDEO_RESULT,
//                StatisticsConstant.EVENT_FUNC_RESULT, String.valueOf(result));
    }

    public static void statisticsBrokenImg() {
//        com.statistics.StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION,
//                StatisticsConstant.EVENT_FUNC_PREVIEW_IMG_ERROR);
    }

    /**
     * banner统计
     */

    public static void statisticsAllBannerShow() {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_BANNER_ALL_SHOW);
    }

    public static void statisticsAllBannerClick() {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_BANNER_ALL_CLICK);
    }

    public static void statisticsAllBannerDownload() {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_BANNER_ALL_DOWNLOAD);
    }

    public static void statisticsAllBannerApply() {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_BANNER_ALL_APPLY);
    }

    public static void statisticsBannerShow(String where, int position) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_BANNER_ + where + "_" + (position + 1) + StatisticsConstant.EVENT_FUNC_SHOW);
    }

    public static void statisticsBannerClick(String where, int position) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_BANNER_ + where + "_" + (position + 1) + StatisticsConstant.EVENT_FUNC_CLICK);
    }

    public static void statisticsBannerDownload(String where, int position) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_BANNER_ + where + "_" + (position + 1) + StatisticsConstant.EVENT_FUNC_DOWNLOAD);
    }

    public static void statisticsBannerApply(String where, int position) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_BANNER_ + where + "_" + (position + 1) + StatisticsConstant.EVENT_FUNC_APPLY);
    }

    /**
     * 主页按钮子页面统计
     */

    public static void statisticsMainBtnAlbumBack(String where) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_ + where + StatisticsConstant.EVENT_FUNC_ALBUM_BACK);
    }

    public static void statisticsMainBtnAlbumStart(String where) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_ + where + StatisticsConstant.EVENT_FUNC_ALBUM_START);
    }

    public static void statisticsMainBtnEditCancel(String where) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_ + where + StatisticsConstant.EVENT_FUNC_EDIT_CANCEL);
    }

    public static void statisticsMainBtnEditSave(String where, String useWhat) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_ + where + StatisticsConstant.EVENT_FUNC_EDIT_SAVE, StatisticsConstant.EVENT_KEY_USE, useWhat);
    }

    public static void statisticsMainBtnPreviewDelete(String where) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_ + where + StatisticsConstant.EVENT_FUNC_SAVE_DELETE);
    }

    public static void statisticsMainBtnPreviewEdit(String where) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_ + where + StatisticsConstant.EVENT_FUNC_SAVE_EDIT);
    }

    public static void statisticsMainBtnPreviewShare(String where, String useWhat) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_MAINPAGE_ + where + StatisticsConstant.EVENT_FUNC_SAVE_SHARE, StatisticsConstant.EVENT_KEY_USE, useWhat);
    }

    public static void statisticsNotificationContentUpdateDownload(String name) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_NOTIF_STORE_UPDATE_DOWNLOAD, StatisticsConstant.EVENT_KEY_USE, name);
    }

    public static void statisticsNotificationContentUpdateApply(String name) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_NOTIF_STORE_UPDATE_APPLY, StatisticsConstant.EVENT_KEY_USE, name);
    }

    public static void statisticsNotificationContentUpdateSave(String name) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_NOTIF_STORE_UPDATE_SAVE, StatisticsConstant.EVENT_KEY_USE, name);
    }

    public static void statisticsNotificationContentUpdateShare(String name, String share) {
//        statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_NOTIF_STORE_UPDATE_SHARE, StatisticsConstant.EVENT_KEY_USE, name + "_" + share);
    }

//    public static final class OfflineStatistics implements JobAssignmentService.Job {
//        private long mLastCanRunTimeMillis = 0;
//
//        @Override
//        public long getIntervalTime() {
//            return TimeConstant.ONE_DAY;
//        }
//
//        private static void statisicsCustomOffline(String event, String key, String value) {
//            statisicsCustom(StatisticsConstant.EVENT_PREFIX_OFFLINE, event,
//                    key, value);
//        }
//
//        private static void statisicsCustomOffline(String event, boolean open) {
//            statisicsCustom(StatisticsConstant.EVENT_PREFIX_OFFLINE, event,
//                    StatisticsConstant.EVENT_KEY_STATE,
//                    open ? StatisticsConstant.EVENT_VALUE_OPEN
//                            : StatisticsConstant.EVENT_VALUE_CLOSE);
//        }
//
//        @Override
//        public void doJob() {
//
//            boolean frontCamera = SPDataManager.isFrontCamera();
//
//            //暗角状态
//            boolean vignetteOn = SPDataManager.isVignetteOn();
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_FILTER_VIGNETTE_STATUS, vignetteOn);
//
//            //模糊状态
//            int blurState = SPDataManager.getBlurState();
//            String blurStr;
//            if (blurState == CameraFragment.TILTSHIFT_MODE_LINEAR) {
//                blurStr = StatisticsConstant.EVENT_FUNC_CAMPAGE_FILTER_BLUR_STATUS_VALUE_LINEAR;
//            } else if (blurState == CameraFragment.TILTSHIFT_MODE_RADIAL) {
//                blurStr = StatisticsConstant.EVENT_FUNC_CAMPAGE_FILTER_BLUR_STATUS_VALUE_RADIAL;
//            } else {
//                blurStr = StatisticsConstant.EVENT_FUNC_CAMPAGE_FILTER_BLUR_STATUS_VALUE_OFF;
//            }
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_FILTER_BLUR_STATUS,
//                    StatisticsConstant.EVENT_KEY_STATUS, blurStr);
//
//            //HDR状态
//            boolean hdrOn = SPDataManager.isHdrOn();
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_MORE_HDR_STATUS, hdrOn);
//
//            //9宫格状态
//            String preference_grid = SPDataManager.getGridInfo();
//            boolean gridOn = (preference_grid != null && preference_grid.equals("preference_grid_3x3"));
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_MORE_NINE_BOX_STATUS, gridOn);
//
//            //点击拍照状态
//            boolean touchTakePhoto = SPDataManager.isTouchTakePhoto();
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_MORE_SCREENCAP_STATUS, touchTakePhoto);
//
////            boolean playShutterSound = SPDataManager.isPlayShutterSound();
//
//            //尺寸切换状态
//            int onlyTwoCollageRatio = SPDataManager.getOnlyTwoCollageRatio(CameraFragment.COLLAGE_RATIO_SHOW_DEFAULT);
//            String state;
//            if (onlyTwoCollageRatio == CameraFragment.COLLAGE_RATIO_SHOW_DEFAULT) {
//                if (SPDataManager.isCropSquare()) {
//                    state = StatisticsConstant.EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS_VALUE_1_1;
//                } else if (SPDataManager.isCropRect()) {
//                    state = StatisticsConstant.EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS_VALUE_3_4;
//                } else {
//                    state = StatisticsConstant.EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS_VALUE_16_9;
//                }
//            } else {
//                if (SPDataManager.isCropSquare() || SPDataManager.isCropRect()) {
//                    state = StatisticsConstant.EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS_VALUE_3_4;
//                } else {
//                    state = StatisticsConstant.EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS_VALUE_16_9;
//                }
//            }
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS, StatisticsConstant.EVENT_KEY_STATUS,state);
//
//            //边框开启状态
//            boolean layoutBorder = SPDataManager.isCollageWithFrame();
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_LAYOUT_BORDER_STATUS, layoutBorder);
//
//            //定时器状态
//            String s = SPDataManager.getTakePhotoTimer();
//            String timerState;
//            if (s.equals(CameraFragment.TIMER_10S)) {
//                timerState = StatisticsConstant.EVENT_FUNC_CAMPAGE_TIME_STATUS_10S;
//            } else if (s.equals(CameraFragment.TIMER_3S)) {
//                timerState = StatisticsConstant.EVENT_FUNC_CAMPAGE_TIME_STATUS_3S;
//            } else {
//                timerState = StatisticsConstant.EVENT_FUNC_CAMPAGE_TIME_STATUS_NONE;
//            }
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_TIME_STATUS, StatisticsConstant.EVENT_KEY_STATUS, timerState);
//
//            //闪光灯状态
//            String flashState;
//            if(frontCamera) {
//                if (SPDataManager.getFillInLight()) {
//                    flashState = StatisticsConstant.EVENT_FUNC_CAMPAGE_FLASH_STATUS_LIGHT_ON;
//                } else {
//                    flashState = StatisticsConstant.EVENT_FUNC_CAMPAGE_FLASH_STATUS_LIGHT_OFF;
//                }
//            } else {
//                String flashValue = SPDataManager.getFlashValue();
//                // 闪光灯状态顺序改为：flash_auto、flash_on、flash_off、flash_torch
//                if ("flash_off".equals(flashValue)) {
//                    flashState = StatisticsConstant.EVENT_FUNC_CAMPAGE_FLASH_STATUS_OFF;
//                } else if ("flash_torch".equals(flashValue)) {
//                    flashState = StatisticsConstant.EVENT_FUNC_CAMPAGE_FLASH_STATUS_TORCH;
//                } else if ("flash_auto".equals(flashValue)) {
//                    flashState = StatisticsConstant.EVENT_FUNC_CAMPAGE_FLASH_STATUS_AUTO;
//                } else if ("flash_on".equals(flashValue)) {
//                    flashState = StatisticsConstant.EVENT_FUNC_CAMPAGE_FLASH_STATUS_ON;
//                } else {
//                    flashState = StatisticsConstant.EVENT_FUNC_CAMPAGE_FLASH_STATUS_OFF;
//                }
//            }
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_FLASH_STATUS, StatisticsConstant.EVENT_KEY_STATUS, flashState);
//
//            //镜头切换状态
//            String camState = frontCamera ? StatisticsConstant.EVENT_FUNC_CAMPAGE_CAMCHANGE_STATUS_VALUE_FRONT
//                    : StatisticsConstant.EVENT_FUNC_CAMPAGE_CAMCHANGE_STATUS_VALUE_BACK;
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_CAMCHANGE_STATUS, StatisticsConstant.EVENT_KEY_STATUS, camState);
//
//            // 获取所有需要统计的配置信息
//            SettingConfig settingConfig = SettingConfig.getInstance();
//            // 设置项：
//            // 自动扫描
//            // 锁屏设置-开关
//            boolean openScreen = settingConfig.isOpenLockScreen();
//            statisicsCustomOffline(StatisticsConstant.EVENT_OFFLINE_SCREENLOCK, openScreen);
//            // Shutter Sound
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_CAMPAGE_CAMSOUND_STATUS, SPDataManager.isPlayShutterSound());
//            //下载次数
//            int downloadCount = SPDataManager.getDownloadCountOfDay();
//            String strDownloadCount;
//            if(downloadCount == 0) {
//                strDownloadCount = StatisticsConstant.EVENT_FUNC_STORE_DOWNLOAD_0;
//            } else if(downloadCount <= 2) {
//                strDownloadCount = StatisticsConstant.EVENT_FUNC_STORE_DOWNLOAD_1_2;
//            } else if(downloadCount <= 5) {
//                strDownloadCount = StatisticsConstant.EVENT_FUNC_STORE_DOWNLOAD_3_5;
//            } else if(downloadCount <= 9) {
//                strDownloadCount = StatisticsConstant.EVENT_FUNC_STORE_DOWNLOAD_6_9;
//            } else if(downloadCount <= 19) {
//                strDownloadCount = StatisticsConstant.EVENT_FUNC_STORE_DOWNLOAD_10_19;
//            } else if(downloadCount <= 49) {
//                strDownloadCount = StatisticsConstant.EVENT_FUNC_STORE_DOWNLOAD_20_49;
//            } else {
//                strDownloadCount = StatisticsConstant.EVENT_FUNC_STORE_DOWNLOAD_50;
//            }
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_STORE_DOWNLOAD, "count", strDownloadCount);
//            SPDataManager.resetDownloadCountOfDay();
//            //关闭声音状态
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_SETTING_SOUND_RATE, SPDataManager.isPlayShutterSound());
//            //通知开关
//            statisicsCustomOffline(StatisticsConstant.EVENT_FUNC_SETTING_NOTIFY_RATE, SPDataManager.enableNotifacation());
//
//            mLastCanRunTimeMillis = System.currentTimeMillis();
//        }
//
//        @Override
//        public boolean canRun() {
//            return System.currentTimeMillis() - mLastCanRunTimeMillis > getIntervalTime();
//        }
//    }
}
