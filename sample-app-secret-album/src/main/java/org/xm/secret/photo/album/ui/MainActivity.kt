package org.xm.secret.photo.album.ui

import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import app.allever.android.lib.core.helper.DisplayHelper
import org.xm.secret.photo.album.R
import org.xm.secret.photo.album.app.BaseActivity
import org.xm.secret.photo.album.ui.adapter.ViewPagerAdapter
import org.xm.secret.photo.album.ui.mvp.presenter.MainPresenter
import org.xm.secret.photo.album.ui.mvp.view.MainView
import org.xm.secret.photo.album.ui.widget.ShakeHelper
import org.xm.secret.photo.album.ui.widget.tab.TabLayout

class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView,
    TabLayout.OnTabSelectedListener, View.OnClickListener {

    private lateinit var mVp: ViewPager
    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    private lateinit var mTab: TabLayout
    private lateinit var mTvTitle: TextView
    private var mainTabHighlight = 0
    private var mainTabUnSelectColor = 0

    private var mFragmentList = mutableListOf<Fragment>()


    private lateinit var mShakeAnimator: ObjectAnimator

    override fun getContentView(): Any =
        R.layout.sa_activity_main

    override fun initView() {
        NotchCompat.adaptNotchWithFullScreen(window)
        checkNotch(Runnable {
            addStatusBar(findViewById<ViewGroup>(R.id.rootLayout), findViewById<View>(R.id.top_bar))
        })
        mShakeAnimator = ShakeHelper.createShakeAnimator(findViewById<View>(R.id.iv_right), true)
        mShakeAnimator.start()

        mTab = findViewById(R.id.tab_layout)
        mVp = findViewById(R.id.id_main_vp)
        mTvTitle = findViewById(R.id.id_main_tv_title)

        mainTabHighlight = resources.getColor(R.color.sa_main_tab_highlight)
        mainTabUnSelectColor = resources.getColor(R.color.sa_main_tab_unselect_color)

        initViewPagerData()
        initViewPager()
        initTab()

    }

    override fun initData() {
    }

    override fun createPresenter(): MainPresenter = MainPresenter()

    private fun initViewPagerData() {
        mFragmentList.add(AlbumFragment())
        mFragmentList.add(GuideFragment())
        mFragmentList.add(SettingFragment())
        mViewPagerAdapter = ViewPagerAdapter(supportFragmentManager, this, mFragmentList)
    }

    private fun initViewPager() {
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
                mTvTitle.text = getString(MainTabModel.getTabAt(position).labelResId)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun initTab() {
        //tab
        mVp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTab))
        mTab.setOnTabSelectedListener(this)

        val tabCount = MainTabModel.tabCount
        for (i in 0 until tabCount) {
            val MainTabModel = MainTabModel.getTab(i)
            val labelId = MainTabModel.labelResId
            val tab = mTab.newTab()
                .setTag(MainTabModel)
                .setCustomView(getTabView(i))
                .setContentDescription(labelId)
            val drawable = MainTabModel.drawable
            if (drawable != null) {
                tab.icon = drawable
            } else {
                tab.setIcon(MainTabModel.iconResId)
            }

            tab.setText(labelId)
            val imageView = tab.customView?.findViewById<ImageView>(R.id.icon)
            imageView?.colorFilter = null
            //解决首次tab文字颜色异常
//            val textView = tab.customView?.findViewById<TextView>(R.id.text1)
//            textView?.setTextColor(mTab.tabTextColors)
            mTab.addTab(tab)
        }

        mTab.setSelectedTabIndicatorWidth(DisplayHelper.dip2px(0))
        mTab.setSelectedTabIndicatorHeight(DisplayHelper.dip2px(0))
        mTab.setSelectedTabIndicatorColor(mainTabHighlight)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.iv_right -> {
//                RecommendActivity.start(this, UMeng.getChannel())
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        mVp.currentItem = tab.position

        MainTabModel.selectedTab = (tab.tag as MainTabModel.Tab)
        for (i in 0 until mTab.tabCount) {
            val aTab = mTab.getTabAt(i)
            if (aTab != null) {
                val imageView = aTab.customView?.findViewById<ImageView>(R.id.icon)
                val textView = aTab.customView?.findViewById<TextView>(R.id.text1)
                if (aTab === tab) {
                    imageView?.setColorFilter(mainTabHighlight, PorterDuff.Mode.SRC_IN)
                    textView?.setTextColor(mainTabHighlight)
                } else {
                    imageView?.colorFilter = null
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
        val tab = MainTabModel.getTab(position)
        textView.setText(tab.labelResId)
        imageView.setImageResource(tab.iconResId)
        return view
    }

    override fun onBackPressed() {

        if (isPasswordViewShowing()) {
            super.onBackPressed()
            return
        }

        checkExit()
    }
}