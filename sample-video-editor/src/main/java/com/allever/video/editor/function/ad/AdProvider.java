//package com.videoeditor.function.ad;
//
//import android.content.Context;
//import androidx.annotation.NonNull;
//
//import com.android.absbase.App;
//
//import com.android.absbase.ui.activity.DialogSuggestActivity;
//import com.rice.balls.ad.AdDisplay;
//import com.rice.balls.ad.AdManager;
//import com.rice.balls.ad.AdRequest;
//import com.allever.video.editor.R;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//
//public class AdProvider {
//    private final String TAG = AdProvider.class.getName();
//    private String mSceneName;
//    private String mStatisticsName;
//    private String mUnitAd;
//    private String mSuffix = "";
//    private int mFunId;
//    private int mRequestTimes = 0;
//
//    public static final String REASON_NO_VALID_AD = "no_has_ad";
//
//    private List<String> mAdCacheNames = new CopyOnWriteArrayList<>();
//
//    private OnAdListener mOnAdListener;
//    private List<String> mStatisticsInfoList = new ArrayList<>();
//    final Object lock = new Object();
//
//    public AdProvider(@NonNull String sceneName, @NonNull String unitId, int funId) {
//        this(sceneName, unitId, funId, sceneName);
//    }
//
//    public AdProvider(@NonNull String sceneName, @NonNull String unitId, int funId, String statisticsName) {
//        mSceneName = sceneName;
//        mStatisticsName = statisticsName;
//        mUnitAd = unitId;
//        mFunId = funId;
//    }
//
//    public static AdProvider newProvider(@NonNull String sceneName, @NonNull String unitId, int funId) {
//        return new AdProvider(sceneName, unitId, funId);
//    }
//
//    public static AdProvider newProvider(@NonNull String sceneName, @NonNull String unitId, int funId, String statisticsName) {
//        return new AdProvider(sceneName, unitId, funId, statisticsName);
//    }
//
//    public void setStatisticsInfo(String... info) {
//        mStatisticsInfoList.clear();
//        mStatisticsInfoList.addAll(Arrays.asList(info));
//    }
//
//    public void setUnitAd(@NonNull String unitId) {
//        mUnitAd = unitId;
//    }
//
//    public void setFunId(int funId) {
//        mFunId = funId;
//    }
//
//    public void setSuffix(String suffix) {
//        if (suffix == null) {
//            suffix = "";
//        }
//        mSuffix = suffix;
//    }
//
//    public void setOnAdListener(OnAdListener listener) {
//        mOnAdListener = listener;
//    }
//
//    public OnAdListener getOnAdListener() {
//        return mOnAdListener;
//    }
//
//    public void loadAd(final AdRequest adRequest) {
////        if (!ConfigManager.INSTANCE.getNeedAd()) {
////            return;
////        }
////        adRequest.setSuffix(mSuffix)
////                .setFunId(mFunId)
////                .setVirtualUnitId(mUnitAd);
////
////    public void setStatisticsInfo(String... info) {
////        mStatisticsInfoList.clear();
////        mStatisticsInfoList.addAll(Arrays.asList(info));
////    }
////
////    public void setUnitAd(@NonNull String unitId) {
////        mUnitAd = unitId;
////    }
////
////    public void setFunId(int funId) {
////        mFunId = funId;
////    }
////
////    public void setSuffix(String suffix) {
////        if (suffix == null) {
////            suffix = "";
////        }
////        mSuffix = suffix;
////    }
////
////    public void setOnAdListener(OnAdListener listener) {
////        mOnAdListener = listener;
////    }
////
////    public OnAdListener getOnAdListener() {
////        return mOnAdListener;
////    }
////
////    public void loadAd(final AdRequest adRequest) {
//////        if (!ConfigManager.INSTANCE.getNeedAd()) {
//////            return;
//////        }
//////        adRequest.setSuffix(mSuffix)
//////                .setFunId(mFunId)
//////                .setVirtualUnitId(mUnitAd);
//////
//////        adRequest.setAdListener(new AdListener() {
//////            @Override
//////            public void onAdRequest() {
//////                List<String> statisticsInfo = new ArrayList<>(mStatisticsInfoList);
//////                statisticsInfo.addAll(Arrays.asList(
//////                        StatisticsConstant.EVENT_KEY_UID, mUnitAd,
//////                        com.statistics.StatisticsConstant.EVENT_KEY_STATE, String.valueOf(true)));
//////                SceneStatistics.statistics(mStatisticsName + StatisticsConstant.EVENT_SUFFIX_AD_REQUEST,
//////                        statisticsInfo.toArray(new String[statisticsInfo.size()]));
//////            }
//////
//////            @Override
//////            public void onAdClose(Ad ad) {
//////                final String key = adRequest.getKey();
//////                if (mOnAdListener != null) {
//////                    ThreadPool.runUITask(new Runnable() {
//////                        @Override
//////                        public void run() {
//////                            if (mOnAdListener != null) {
//////                                mOnAdListener.onAdClosed(key);
//////                            }
//////                        }
//////                    });
//////
//////                }
//////            }
//////
//////            @Override
//////            public void onError(String err) {
//////                if (mOnAdListener != null ) {
//////                    mOnAdListener.onAdLoadFailure(err);
//////                }
////////                if (DebugUtil.isDebuggable()) {
////////                    DLog.d(TAG, mSceneName + " onError:" + err);
////////                }
//////                SceneStatistics.statistics(StatisticsConstant.EVENT_SCENE_TRIGGER_FAIL, mStatisticsName, err);
//////            }
//////
//////            @Override
//////            public void onAdLoaded(final Ad ad) {
//////                final String key = adRequest.getKey();
////////                if (DebugUtil.isDebuggable()) {
////////                    DLog.d(TAG, mSceneName + " onThirdPartyAdLoaded:" + key);
////////                }
//////
//////                synchronized (lock) {
//////                    mAdCacheNames.add(key);
//////                }
//////                if (mOnAdListener != null) {
//////                    ThreadPool.runUITask(new Runnable() {
//////                        @Override
//////                        public void run() {
//////                            if (mOnAdListener != null ) {
//////                                mOnAdListener.onAdLoadFinish(key);
//////                            }
//////                        }
//////                    });
//////
//////                }
//////
//////                int type = AdManager.getAdType(ad);
//////                List<String> statisticsInfo = new ArrayList<>(mStatisticsInfoList);
//////                statisticsInfo.addAll(Arrays.asList(
//////                        StatisticsConstant.EVENT_KEY_UID, mUnitAd,
//////                        StatisticsConstant.EVENT_KEY_ADSOURCE, "ads_"+type));
//////                SceneStatistics.statistics(mStatisticsName + StatisticsConstant.EVENT_SUFFIX_AD_FILL,
//////                        statisticsInfo.toArray(new String[statisticsInfo.size()]));
//////            }
//////
//////            @Override
//////            public void onAdClicked(Ad ad) {
//////                final String key = adRequest.getKey();
//////
////////                if (DebugUtil.isDebuggable()) {
////////                    DLog.d(TAG, mSceneName + " onThirdPartyAdClicked");
////////                }
//////                if (mOnAdListener != null) {
//////                    ThreadPool.runUITask(new Runnable() {
//////                        @Override
//////                        public void run() {
//////                            if(mOnAdListener != null) {
//////                                mOnAdListener.onAdClicked(key);
//////                            }
//////                        }
//////                    });
//////
//////                }
//////                int type = AdManager.getAdType(ad);
//////                List<String> statisticsInfo = new ArrayList<>(mStatisticsInfoList);
//////                statisticsInfo.addAll(Arrays.asList(
//////                        StatisticsConstant.EVENT_KEY_UID, mUnitAd,
//////                        StatisticsConstant.EVENT_KEY_ADSOURCE, "ads_" + type));
//////                SceneStatistics.statistics(mStatisticsName + StatisticsConstant.EVENT_SUFFIX_AD_CLICK,
//////                        statisticsInfo.toArray(new String[statisticsInfo.size()]));
//////            }
//////
//////            @Override
//////            public void onLoggingImpression(Ad ad) {
////////                if (DebugUtil.isDebuggable()) {
////////                    DLog.d(TAG, mSceneName + " onLoggingImpression");
////////                }
//////                final String key = adRequest.getKey();
//////                if (mOnAdListener != null) {
//////                    ThreadPool.runUITask(new Runnable() {
//////                        @Override
//////                        public void run() {
//////                            if (mOnAdListener != null) {
//////                                mOnAdListener.onAdImpression(key);
//////                            }
//////                        }
//////                    });
//////
//////                }
//////                int type = AdManager.getAdType(ad);
//////                List<String> statisticsInfo = new ArrayList<>(mStatisticsInfoList);
//////                statisticsInfo.addAll(Arrays.asList(
//////                        StatisticsConstant.EVENT_KEY_UID, mUnitAd,
//////                        com.statistics.StatisticsConstant.EVENT_KEY_STATE, String.valueOf(true),
//////                        StatisticsConstant.EVENT_KEY_ADSOURCE, "ads_" + type));
//////                SceneStatistics.statistics(mStatisticsName + StatisticsConstant.EVENT_SUFFIX_AD_SHOW,
//////                        statisticsInfo.toArray(new String[statisticsInfo.size()]));
//////            }
//////
//////            @Override
//////            public void onRewarded(@NotNull String s, int i) {
//////
//////            }
//////        });
//////
//////        AdManager.getInstance().loadAd(App.getContext(), adRequest);
////    }
////
////    public void statisticsNoRequestAd(String reason) {
//////        List<String> statisticsInfo = new ArrayList<>(mStatisticsInfoList);
//////        statisticsInfo.addAll(Arrays.asList(
//////                SceneStatistics.EVENT_KEY_UID, mUnitAd,
//////                "reason", reason == null ? "un_know" : reason,
//////                SceneStatistics.EVENT_KEY_STATE, String.valueOf(false)));
//////        SceneStatistics.statistics(mStatisticsName + SceneStatistics.EVENT_SUFFIX_AD_REQUEST,
//////                statisticsInfo.toArray(new String[statisticsInfo.size()]));
////    }
////
////    public static void statisticsNoRequestAd(String statisticsName, String reason) {
//////        List<String> statisticsInfo = new ArrayList<>();
//////        statisticsInfo.addAll(Arrays.asList(
//////                "reason", reason == null ? "un_know" : reason,
//////                SceneStatistics.EVENT_KEY_STATE, String.valueOf(false)));
//////        SceneStatistics.statistics(statisticsName + SceneStatistics.EVENT_SUFFIX_AD_REQUEST,
//////                statisticsInfo.toArray(new String[statisticsInfo.size()]));
////    }
////
////    public void statisticsNoShowAd(String reason) {
//////        List<String> statisticsInfo = new ArrayList<>(mStatisticsInfoList);
//////        statisticsInfo.addAll(Arrays.asList(
//////                SceneStatistics.EVENT_KEY_UID, mUnitAd,
//////                "reason", reason == null ? "un_know" : reason,
//////                "hasAd", String.valueOf(hasAd()),
//////                SceneStatistics.EVENT_KEY_STATE, String.valueOf(false)));
//////        SceneStatistics.statistics(mStatisticsName + SceneStatistics.EVENT_SUFFIX_AD_SHOW,
//////                statisticsInfo.toArray(new String[statisticsInfo.size()]));
////    }
////
////    public static void statisticsNoShowAd(String statisticsName, String reason) {
//////        List<String> statisticsInfo = new ArrayList<>();
//////        statisticsInfo.addAll(Arrays.asList(
//////                "reason", reason == null ? "un_know" : reason,
//////                SceneStatistics.EVENT_KEY_STATE, String.valueOf(false)));
//////        SceneStatistics.statistics(statisticsName + SceneStatistics.EVENT_SUFFIX_AD_SHOW,
//////                statisticsInfo.toArray(new String[statisticsInfo.size()]));
////    }
////
////    public void loadAd() {
////        Context context = App.getContext();
////        final AdRequest adRequest = new AdRequest();
////        adRequest.setSceneName(mSceneName)
////                .setSuffix(mSuffix)
////                .addFacebookBannerAd(context, 300, 250)
////                .addFacebookNativeAd(context)
////                .addAdmobBannerAd(context, 340, 320)
////                .addAdmobNativeAd(context, 340, 320)
////                .addAmazonBannerAd(context)
////                .addMopubBannerAd(context)
////                .addMopubNativeAd(context)
////                .addApplovinBannerAd(context)
////                .addApplovinNativeAd(context);
////        loadAd(adRequest);
////    }
////
////    public String getAdCacheKey() {
////        synchronized (lock) {
////            if (mAdCacheNames.size() > 0) {
////                return mAdCacheNames.remove(0);
////            }
////        }
////        return "";
////    }
////
////    public int size() {
////        synchronized (lock) {
////            return mAdCacheNames.size();
////        }
////    }
////
////    public boolean hasAd() {
////        synchronized (lock) {
////            return !mAdCacheNames.isEmpty();
////        }
////    }
////
////    public void destory() {
////        synchronized (lock) {
////            for (String key : mAdCacheNames) {
////                AdManager.getInstance().destoryAd(key);
////            }
////            mOnAdListener = null;
////            mAdCacheNames.clear();
////        }
////    }
////
////    public abstract static class OnAdListener {
////        public void onAdLoadFinish(String cacheKey) {
////        }
////
////        public void onAdLoadFailure(String err) {
////        }
////
////        public void onAdClicked(String cacheKey) {
////        }
////
////        public void onAdImpression(String cacheKey) {
////        }
////
////        public void onAdClosed(String cacheKey) {
////        }
////    }
////
////    public void showNextAd(Context context) {
////
////        String adCacheKey = getAdCacheKey();
////        if (!AdManager.getInstance().hasAd(adCacheKey)) {
////            return;
////        }
////        Object ad = AdManager.getInstance().getAd(adCacheKey);
////        if(ad == null) {
////            return;
////        }
////        new AdDisplay.Builder(DialogSuggestActivity.class)
////                .setFakeAdOwner(false)
////                .setKey(adCacheKey)
////                .setLayoutId(R.layout.sc_layout_style)
////                .setDefaultResId(R.drawable.sc_ad_icon_default)
////                .show(context, ad);
////    }
////}
