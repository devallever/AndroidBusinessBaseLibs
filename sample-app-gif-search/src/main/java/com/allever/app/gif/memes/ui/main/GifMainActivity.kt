package com.allever.app.gif.memes.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.os.Build
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.allever.app.gif.memes.BR
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.databinding.ActivityGifMainBinding
import com.allever.app.gif.memes.ui.SettingActivity
import com.allever.app.gif.memes.ui.TabModel
import com.allever.app.gif.memes.ui.ViewHelper
import com.allever.app.gif.memes.ui.adapter.ViewPagerAdapter
import com.allever.app.gif.memes.ui.like.LikedFragment
import com.allever.app.gif.memes.ui.main.model.GifMainViewModel
import com.allever.app.gif.memes.ui.maker.MineFragment
import com.allever.app.gif.memes.ui.maker.PickActivity
import com.allever.app.gif.memes.ui.search.SearchFragment
import com.allever.lib.common.app.BaseFragment
import com.allever.lib.common.ui.widget.tab.TabLayout
import com.allever.lib.common.util.ActivityCollector
import com.allever.lib.common.util.DisplayUtils
import com.allever.lib.common.util.ResUtils
import com.allever.lib.common.util.toast
import com.funny.gif.memes.app.BaseDataActivity2
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.func.download.DownloadManager
import com.funny.gif.memes.util.ImageLoader
import com.xm.lib.base.config.DataBindingConfig
import com.xm.lib.manager.IntentManager
import com.xm.lib.manager.statusbar.BarUtils
import com.xm.lib.permission.PermissionCompat
import com.xm.netmodel.helder.ExceptionHandle

class GifMainActivity : BaseDataActivity2<ActivityGifMainBinding, GifMainViewModel>(),
    View.OnClickListener, TabLayout.OnTabSelectedListener {

    private lateinit var mVp: ViewPager
    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    private lateinit var mTab: TabLayout
    private var mainTabHighlight = 0
    private var mainTabUnSelectColor = 0

    private var mFragmentList = mutableListOf<Fragment>()

    private var mShakeAnimator: ObjectAnimator? = null

//    private var mBannerAd: IAd? = null
//    private var mExitInsertAd: IAd? = null

    lateinit var ivRight: ImageView
    private lateinit var topBarContainer: View

    override fun isPaddingTop(): Boolean = false
    override fun statusColor(): Int = R.color.trans
    override fun isStatusBarDark() = true

    override fun initDataBindingConfig() =
        DataBindingConfig(R.layout.activity_gif_main, BR.gifMainViewModel)

    override fun initDataAndEvent() {
//        BillingHelper.connect()
        ViewHelper.setMarginTop(mBinding.topBar, BarUtils.getStatusBarHeight())
        ivRight = findViewById(R.id.ivRight)
        topBarContainer = findViewById(R.id.topBarContainer)
            ivRight.setOnClickListener(this)
//        ivRecommend.setOnClickListener(this)
//        mShakeAnimator = ShakeHelper.createShakeAnimator(ivRecommend, true)
//        mShakeAnimator?.start()

        mTab = findViewById(R.id.tab_layout)
        mVp = findViewById(R.id.id_main_vp)

        mainTabHighlight = (ResUtils.getColor(R.color.black))
        mainTabUnSelectColor = (ResUtils.getColor(R.color.gray_66))

        initViewPagerData()
        initViewPager()
        initTab()

//        findViewById<View>(R.id.ivRecommend).setOnClickListener {
//            BillingHelper.checkScribeStatus { success, code, message ->
//                if (success) {
//                    toast("已经订阅")
//                } else {
//                    BillingHelper.subScribe(
//                        this,
//                        BillingConfig.PRODUCT_WEEKLY
//                    ) { success, code, message ->
//                        if (success) {
//                            toast("订阅成功")
//                        } else {
//                            toast("订阅失败")
//                        }
//                    }
//                }
//            }
//        }

//        HandlerHelper.mainHandler.postDelayed({
//            loadBanner()
//            loadExitInsert()
//        }, 10000)
    }


    override fun destroyView() {
    }


    private fun initViewPagerData() {
        mFragmentList.add(TrendFragment())
        mFragmentList.add(SearchFragment())
        mFragmentList.add(LikedFragment())
        mFragmentList.add(MineFragment())
        mViewPagerAdapter = ViewPagerAdapter(supportFragmentManager, mFragmentList)
    }

    private fun initViewPager() {
        ivRight.setColorFilter(ResUtils.getColor(R.color.black))
        mVp.offscreenPageLimit = 3
        mVp.adapter = mViewPagerAdapter

        mVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        topBarContainer.visibility = View.VISIBLE
//                        if (mBannerAd != null) {
//                            bannerContainer.visibility = View.VISIBLE
//                        } else {
//                            bannerContainer.visibility = View.GONE
//                        }
                        ivRight.setImageResource(R.drawable.ic_setting)
//                        mTvTitle.text = getString(R.string.app_name)
                        ivRight.setColorFilter(ResUtils.getColor(R.color.black))
                    }
                    1 -> {
                        ivRight.setImageResource(R.drawable.ic_setting)
                        topBarContainer.visibility = View.GONE
                        ivRight.setColorFilter(ResUtils.getColor(R.color.black))
//                        mTvTitle.text = getString(R.string.tab_guide)
                    }
                    2 -> {
                        ivRight.setImageResource(R.drawable.ic_setting)
                        ivRight.setColorFilter(ResUtils.getColor(R.color.black))
                        topBarContainer.visibility = View.VISIBLE
//                        bannerContainer.visibility = View.GONE
                    }
                    3 -> {
                        ivRight.setImageResource(R.drawable.icon_add)
                        topBarContainer.visibility = View.VISIBLE
                        ivRight.setColorFilter(ResUtils.getColor(R.color.black))
//                        bannerContainer.visibility = View.GONE
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }


    private fun initTab() {
        //tab
        mVp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTab))
        mTab.setOnTabSelectedListener(this)

        val tabCount = TabModel.tabCount
        for (i in 0 until tabCount) {
            val tabModel = TabModel.getTab(i)
            val labelId = tabModel.labelResId
            val tab = mTab.newTab()
                .setTag(tabModel)
                .setCustomView(getTabView(i))
                .setContentDescription(labelId)
            val drawable = tabModel.drawable
            if (drawable != null) {
                tab.icon = drawable
            } else {
                tab.setIcon(tabModel.iconResId)
            }

            tab.setText(labelId)
            val imageView = tab.customView?.findViewById<ImageView>(R.id.icon)
            imageView?.setColorFilter(mainTabUnSelectColor, PorterDuff.Mode.SRC_IN)
            //解决首次tab文字颜色异常
            val textView = tab.customView?.findViewById<TextView>(R.id.text1)
            textView?.setTextColor(mainTabUnSelectColor)
            mTab.addTab(tab)
        }

        mTab.setSelectedTabIndicatorWidth(DisplayUtils.dip2px(20))
        mTab.setSelectedTabIndicatorHeight(DisplayUtils.dip2px(2))
        mTab.setSelectedTabIndicatorColor(mainTabHighlight)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        mVp.currentItem = tab.position

        TabModel.selectedTab = (tab.tag as TabModel.Tab)
        for (i in 0 until mTab.tabCount) {
            val aTab = mTab.getTabAt(i)
            if (aTab != null) {
                val imageView = aTab.customView?.findViewById<ImageView>(R.id.icon)
                val textView = aTab.customView?.findViewById<TextView>(R.id.text1)
                if (aTab === tab) {
                    imageView?.setColorFilter(mainTabHighlight, PorterDuff.Mode.SRC_IN)
                    textView?.setTextColor(mainTabHighlight)
                } else {
                    imageView?.setColorFilter(mainTabUnSelectColor, PorterDuff.Mode.SRC_IN)
                    textView?.setTextColor(mainTabUnSelectColor)
                }
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}
    override fun onTabReselected(tab: TabLayout.Tab) {}

    private fun getTabView(position: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_bottom_tab, null)
        val imageView = view.findViewById<ImageView>(R.id.icon)
        val textView = view.findViewById<TextView>(R.id.text1)
        val tab = TabModel.getTab(position)
        textView.setText(tab.labelResId)
        imageView.setImageResource(tab.iconResId)
        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivRight -> {
                when (mVp.currentItem) {
                    0 -> {
                        ActivityCollector.startActivity(this, SettingActivity::class.java)
                    }
                    2 -> {
                        ActivityCollector.startActivity(this, SettingActivity::class.java)
                    }
                    3 -> {
                        requestPermission {
                            IntentManager.startActivity(this, PickActivity::class.java)
                        }
                    }
                }

            }
        }
    }

    private fun requestPermission(block: () -> Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            PermissionCompat.with(this)
                .permission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
                .onExplain(ExceptionHandle.getStringRes(R.string.permission_tips))
                .onSetting(getString(R.string.mamual_permission))
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        Global.createDir()
                        block.invoke()
                    }
                }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionCompat.with(this)
                    .permission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_MEDIA
                    )
                    .onExplain(ExceptionHandle.getStringRes(R.string.permission_tips))
                    .onSetting(getString(R.string.mamual_permission))
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            Global.createDir()
                            block.invoke()
                        }
                    }
            } else {
                PermissionCompat.with(this)
                    .permission(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    .onExplain(ExceptionHandle.getStringRes(R.string.permission_tips))
                    .onSetting(getString(R.string.mamual_permission))
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            Global.createDir()
                            block.invoke()
                        }
                    }
            }
        }


    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currentFragment = mFragmentList[mVp.currentItem] as? BaseFragment
        if (currentFragment?.onKeyDown(keyCode, event) == true) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }


    override fun onDestroy() {
        super.onDestroy()
        DownloadManager.getInstance().cancelAllTask()
//        mBannerAd?.destroy()
//        mExitInsertAd?.destroy()
        ImageLoader.clearMemoryCache()
        mShakeAnimator?.cancel()
//        BillingHelper.disConnect()
    }

//
//    private fun loadBanner() {
//        HandlerHelper.mainHandler.postDelayed({
//            val container = findViewById<ViewGroup>(R.id.bannerContainer)
//            AdChainHelper.loadAd(AdConstants.AD_NAME_BANNER, container, object : AdChainListener {
//                override fun onLoaded(ad: IAd?) {
//                    mBannerAd = ad
//                    if (mVp.currentItem == 0) {
//                        bannerContainer.visibility = View.VISIBLE
//                    } else {
//                        bannerContainer.visibility = View.GONE
//                    }
//                }
//
//                override fun onFailed(msg: String) {}
//                override fun onShowed() {}
//                override fun onDismiss() {}
//
//            })
//        }, 3000)
//    }

    private var mIsAdLoaded = false
//    private fun loadExitInsert() {
//        AdChainHelper.loadAd(AdConstants.AD_NAME_EXIT_INSERT, null, object : AdChainListener {
//            override fun onLoaded(ad: IAd?) {
//                mExitInsertAd = ad
//                mIsAdLoaded = true
//            }
//
//            override fun onFailed(msg: String) {}
//            override fun onShowed() {
//                mIsAdLoaded = false
//            }
//
//            override fun onDismiss() {}
//
//        })
//    }

    override fun onResume() {
        super.onResume()
//        mBannerAd?.onAdResume()
    }

    override fun onPause() {
        super.onPause()
//        mBannerAd?.onAdPause()
    }

    override fun onBackPressed() {
        checkExit()
    }

}