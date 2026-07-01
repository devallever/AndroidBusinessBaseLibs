//package com.qrcode.scanner.ad
//
//import android.content.Context
//import com.android.absbase.App
//import com.android.absbase.utils.TaskRunnable
//import com.android.absbase.utils.TimeUtils
//import com.rice.balls.ad.AdDisplay
//import com.rice.balls.ad.AdListener
//import com.rice.balls.ad.AdManager
//import com.rice.balls.ad.AdRequest
//import com.rice.balls.ad.thirdparty.Ad
//import com.rice.balls.statistics.SceneStatistics
//import com.rice.balls.statistics.StatisticsConstant
//import com.allever.video.editor.ConfigManager
//import java.util.concurrent.atomic.AtomicLong
//
//class FullScreenAdProvider {
//    companion object {
//        /**
//         * 场景间的广告间隔
//         */
//        private const val SCENE_BETWEEN_INTERVAL = TimeUtils.TimeConstant.ONE_MIN
//        /**
//         * 场景内的广告间隔
//         */
//        private const val SENE_INSIDE_INTERVAL = TimeUtils.TimeConstant.ONE_MIN * 3
//
//        private var prevShowAdTime = AtomicLong(ConfigManager.prevShowFullScreenAdTimeMillis)
//
//    }
//
//    private var prevShowAdTimeByScene = 0L
//    private var adCacheKey = ""
//    private var followUpActionRunnable: Runnable? = null
//
//    var singleTimeoutMillis = 0L
//
//    fun loadAd(sceneName: String, unitID: String): String {
//        if (!ConfigManager.needAd) {
//            return ""
//        }
//        prevShowAdTimeByScene = ConfigManager.getPrevShowFullSceneAdTimeMillisByScene(sceneName)
//        val currentTimeMillis = System.currentTimeMillis()
//        if (currentTimeMillis - prevShowAdTimeByScene < SENE_INSIDE_INTERVAL
//                || currentTimeMillis - prevShowAdTime.get() < SCENE_BETWEEN_INTERVAL) {
//            return ""
//        }
//        val appContext = App.getContext()
//        val adRequest = AdRequest()
//        adRequest.setSceneName(sceneName)
//                .addInterstitial(appContext)
//                .setVirtualUnitId(unitID)
////                .setSingleTimeoutMillis(singleTimeoutMillis)
//        adRequest.setAdListener(object : AdListener {
//            override fun onAdRequest() {
//                SceneStatistics.statistics(sceneName + StatisticsConstant.EVENT_SUFFIX_AD_REQUEST,
//                        "unitid", unitID)
//            }
//
//            override fun onAdClose(var1: Ad) {
//
//                followUpActionRunnable?.run()
//                followUpActionRunnable = null
//
//            }
//
//            override fun onError(err: String) {
//                SceneStatistics.statistics(sceneName + StatisticsConstant.EVENT_SUFFIX_AD_FALID,
//                        "unitid", unitID, "reqerror", err)
//            }
//
//            override fun onAdLoaded(ad: Ad) {
//                val key = adRequest.key
//                SceneStatistics.statistics(sceneName + StatisticsConstant.EVENT_SUFFIX_AD_FILL,
//                        "unitid", unitID)
//            }
//
//            override fun onAdClicked(ad: Ad) {
//                val key = adRequest.key
//
//                SceneStatistics.statistics(sceneName + StatisticsConstant.EVENT_SUFFIX_AD_CLICK,
//                        "unitid", unitID)
//            }
//
//            override fun onLoggingImpression(ad: Ad) {
//                SceneStatistics.statistics(sceneName + StatisticsConstant.EVENT_SUFFIX_AD_SHOW,
//                        "unitid", unitID)
//
//                val currentTimeMillis1 = System.currentTimeMillis()
//                prevShowAdTimeByScene = currentTimeMillis1
//                prevShowAdTime.set(currentTimeMillis1)
//                ConfigManager.prevShowFullScreenAdTimeMillis = currentTimeMillis1
//                ConfigManager.setPrevShowFullSceneAdTimeMillisByScene(sceneName, currentTimeMillis1)
//            }
//        })
//
//        adCacheKey = AdManager.instance.loadAd(appContext, adRequest)
//        return adCacheKey
//    }
//
//    fun hasAd(): Boolean {
//        return AdManager.instance.hasAd(adCacheKey)
//    }
//
//    fun showAd(context: Context, runnable: Runnable?): Boolean {
//        val ret = if (ConfigManager.needAd) {
//            followUpActionRunnable = runnable
//            val ad = AdManager.instance.getAd(adCacheKey)
//            if (ad != null) {
//                AdDisplay.Builder()
//                        .setFakeAdOwner(false)
//                        .show(context, ad)
//            } else false
//        } else false
//        if (ret) {
//            TaskRunnable.run(object : Runnable {
//                override fun run() {
//                    followUpActionRunnable?.run()
//                    followUpActionRunnable = null
//                }
//            }, TimeUtils.TimeConstant.ONE_MIN, TaskRunnable.TYPE_MAIN)
//        } else {
//            // 展示失败由外面来做,内部只展示后再做
////            followUpActionRunnable?.run()
////            followUpActionRunnable = null
//        }
//        return ret
//    }
//
//    fun destroyAd() {
//        AdManager.instance.destoryAd(adCacheKey)
//    }
//}