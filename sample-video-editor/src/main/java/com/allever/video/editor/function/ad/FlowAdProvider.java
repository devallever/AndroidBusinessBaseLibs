//package com.videoeditor.function.ad;
//
//import androidx.annotation.NonNull;
//import android.text.TextUtils;
//
//import com.android.absbase.utils.NetworkUtils;
//import com.qrcode.scanner.ad.AdItemBean;
//import com.rice.balls.ad.AdManager;
//import com.rice.balls.ad.thirdparty.AbstractThirdPartyAd;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// */
//
//public class FlowAdProvider {
//
//    private String mSceneName;
//    private int mFunId;
//    private int mAdTouchType;
//    private AdFlowPosition mAdFlowPosition;
//    private List<AdFlowPosition.Position> mAdLines;
//
//
//    private List<AdItemBean> adItemBeans = new ArrayList<>();
//
//    private String mFlowUnitAd;
//    private AdProvider mFlowAdProvider;
//    private int mFlowFreq = 0;
//
//    public static final String FLOW_AD_STATISTICS_NAME = "gallery_flow";
//    public static final String GRID_AD_STATISTICS_NAME = "gallery_grid_flow";
//
//
//    public FlowAdProvider(@NonNull String sceneName, @NonNull String flowUnitId, @NonNull String gridUnitId, int funId) {
//        mSceneName = sceneName;
//        mFlowUnitAd = flowUnitId;
//        mFunId = funId;
//        mFlowAdProvider = AdProvider.newProvider(mSceneName, flowUnitId, funId, FLOW_AD_STATISTICS_NAME);
//        mFlowAdProvider.setStatisticsInfo("where", sceneName);
//        mAdFlowPosition = AdFlowPosition.create("2");
//        mAdLines = mAdFlowPosition.getPositions();
//    }
//
//    public static FlowAdProvider newProvider(@NonNull String sceneName, @NonNull String flowUnitId, @NonNull String gridUnitId, int funId) {
//        return new FlowAdProvider(sceneName, flowUnitId, gridUnitId, funId);
//    }
//
//    /**
//     * 需要先调用这个接口做判断，除了判断是否需要请求外，还会初始化配置
//     *
//     * @return
//     */
//    public boolean needRequest() {
//        return NetworkUtils.isNetworkAvailable();
//    }
//
//    private void loadFlowNextAd() {
//        mFlowAdProvider.setSuffix(""+(mFlowFreq++));
//        mFlowAdProvider.loadAd();
//    }
//
//    public List<AdItemBean> getAllAdBeanAndLoad() {
//        if (adItemBeans.size() > 0) {
//            adItemBeans.clear();
//        }
//        mFlowAdProvider.setOnAdListener(new AdProvider.OnAdListener() {
//            @Override
//            public void onAdLoadFinish(String cacheKey) {
//                super.onAdLoadFinish(cacheKey);
//                refreshAd(cacheKey, 1);
//            }
//        });
//        if (mAdLines != null) {
//            for (AdFlowPosition.Position position : mAdLines) {
//                AdItemBean adItemBean = new AdItemBean();
//                adItemBean.setAdTouchType(mAdTouchType);
//                adItemBean.setAdPosition(position);
//
//                loadFlowNextAd();
//
//                adItemBeans.add(adItemBean);
////                adItemBean.createAdView(R.layout.ad_store_view);
//            }
//        }
//        return adItemBeans;
//    }
//
//    private void refreshAd(String cacheKey, int style) {
//        for (AdItemBean adItemBean : adItemBeans) {
//            if (((style == 1 && adItemBean.isBanner()) || (style == 2 && adItemBean.isIcon()))
//                    && adItemBean.get() == null) {
//                if (TextUtils.isEmpty(cacheKey) || !AdManager.getInstance().hasAd(cacheKey)) {
//                    return;
//                }
//                Object ad = AdManager.getInstance().getAd(cacheKey);
//                if (ad == null) {
//                    return;
//                }
//                adItemBean.set((AbstractThirdPartyAd) ad);
//                adItemBean.setAdCacheKey(cacheKey);
//                //TODO: 考虑是否提前创建View
//                adItemBean.refreshAd();
//                break;
//            }
//        }
//    }
//
//    public void destory() {
//        mFlowAdProvider.destory();
//        mFlowAdProvider.setOnAdListener(null);
//    }
//}
