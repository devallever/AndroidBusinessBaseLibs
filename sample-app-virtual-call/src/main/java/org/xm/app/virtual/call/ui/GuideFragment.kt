package org.xm.app.virtual.call.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allever.app.virtual.call.R
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.app.App
import com.allever.lib.common.app.BaseFragment
import org.xm.app.virtual.call.ad.AdContract

class GuideFragment : BaseFragment() {
    private var mBannerAd: IAd? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            LayoutInflater.from(App.context).inflate(R.layout.fragment_guide, container, false)

        mBannerContainer = view.findViewById<ViewGroup>(R.id.banner_container)
        mNativeAdContainer = view.findViewById(R.id.nativeAdContainer)

        mHandler.postDelayed({
            loadBanner()
        }, 2000)
        return view
    }

    override fun onResume() {
        super.onResume()
        mBannerAd?.onAdResume()
    }

    override fun onPause() {
        super.onPause()
        mBannerAd?.onAdPause()
    }

    override fun onDestroy() {
        mBannerAd?.destroy()
        mNativeAd?.destroy()
        super.onDestroy()
    }

    private lateinit var mNativeAdContainer: ViewGroup
    private var mNativeAd: IAd? = null
    private fun loadNativeAd() {
        AdChainHelper.loadAd(
            AdContract.AD_NAME_COMMON_NATIVE_SMALL,
            mNativeAdContainer,
            object : AdChainListener {
                override fun onLoaded(ad: IAd?) {
                    mNativeAd = ad
                    mNativeAd?.show()
                }

                override fun onFailed(msg: String) {
                    loadBanner()
                }

                override fun onShowed() {
                }

                override fun onDismiss() {
                }

            })

    }

    private lateinit var mBannerContainer: ViewGroup
    private fun loadBanner() {
        AdChainHelper.loadAd(
            AdContract.AD_NAME_GUIDE_BANNER,
            mBannerContainer,
            object : AdChainListener {
                override fun onLoaded(ad: IAd?) {
                    mBannerAd = ad
                }

                override fun onFailed(msg: String) {}
                override fun onShowed() {}
                override fun onDismiss() {}

            })
    }

}