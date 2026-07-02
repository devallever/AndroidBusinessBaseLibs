package com.allever.app.gif.memes.ui.main

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import app.allever.android.lib.core.base.AbstractFragment
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.databinding.GsActivityGifMainBinding
import com.allever.app.gif.memes.ui.SettingActivity
import com.allever.app.gif.memes.ui.TabModel
import com.allever.app.gif.memes.ui.ViewHelper
import com.allever.app.gif.memes.ui.adapter.ViewPagerAdapter
import com.allever.app.gif.memes.ui.like.LikedFragment
import com.allever.app.gif.memes.ui.main.model.GifMainViewModel
import com.allever.app.gif.memes.ui.maker.MineFragment
import com.allever.app.gif.memes.ui.maker.PickActivity
import com.allever.app.gif.memes.ui.search.SearchFragment
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.core.helper.DisplayHelper
import app.allever.android.lib.core.util.BarUtils
import app.allever.android.lib.core.util.ResUtils
import app.allever.android.lib.mvvm.base.BaseMvvmActivity
import com.allever.app.gif.memes.ui.widget.tab.TabLayout
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.func.download.DownloadManager
import com.funny.gif.memes.util.ImageLoader

class GifMainActivity : BaseMvvmActivity<GsActivityGifMainBinding, GifMainViewModel>(),
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
//
//    override fun isPaddingTop(): Boolean = false
//    override fun statusColor(): Int = R.color.trans
//    override fun isStatusBarDark() = true

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
                        ivRight.setImageResource(R.drawable.gs_ic_setting)
//                        mTvTitle.text = getString(R.string.app_name)
                        ivRight.setColorFilter(ResUtils.getColor(R.color.black))
                    }
                    1 -> {
                        ivRight.setImageResource(R.drawable.gs_ic_setting)
                        topBarContainer.visibility = View.GONE
                        ivRight.setColorFilter(ResUtils.getColor(R.color.black))
//                        mTvTitle.text = getString(R.string.tab_guide)
                    }
                    2 -> {
                        ivRight.setImageResource(R.drawable.gs_ic_setting)
                        ivRight.setColorFilter(ResUtils.getColor(R.color.black))
                        topBarContainer.visibility = View.VISIBLE
//                        bannerContainer.visibility = View.GONE
                    }
                    3 -> {
                        ivRight.setImageResource(R.drawable.gs_icon_add)
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

        mTab.setSelectedTabIndicatorWidth(DisplayHelper.dip2px(20))
        mTab.setSelectedTabIndicatorHeight(DisplayHelper.dip2px(2))
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
        val view = LayoutInflater.from(this).inflate(R.layout.gs_layout_bottom_tab, null)
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
                        ActivityHelper.startActivity<SettingActivity>()
                    }
                    2 -> {
                        ActivityHelper.startActivity<SettingActivity>()
                    }
                    3 -> {
                        requestPermission {
                            ActivityHelper.startActivity<PickActivity>()
                        }
                    }
                }

            }
        }
    }

    private fun requestPermission(block: () -> Unit) {
        Global.createDir()
        block.invoke()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currentFragment = mFragmentList[mVp.currentItem] as? AbstractFragment
        if (currentFragment?.onKeyDown(keyCode, event) == true) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }


    override fun onDestroy() {
        super.onDestroy()
        DownloadManager.getInstance().cancelAllTask()
        ImageLoader.clearMemoryCache()
        mShakeAnimator?.cancel()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        checkExit()
    }

    override fun inflate(): GsActivityGifMainBinding = GsActivityGifMainBinding.inflate(layoutInflater)

    override fun init() {
        ViewHelper.setMarginTop(mBinding.topBar, BarUtils.getStatusBarHeight())
        ivRight = findViewById(R.id.ivRight)
        topBarContainer = findViewById(R.id.topBarContainer)
        ivRight.setOnClickListener(this)

        mTab = findViewById(R.id.tab_layout)
        mVp = findViewById(R.id.id_main_vp)

        mainTabHighlight = (ResUtils.getColor(R.color.black))
        mainTabUnSelectColor = (ResUtils.getColor(R.color.gray_66))

        initViewPagerData()
        initViewPager()
        initTab()
    }

}