package org.xm.app.virtual.call.ui

import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import app.allever.android.lib.core.helper.DisplayHelper
import com.allever.app.virtual.call.R
import org.xm.app.virtual.call.app.BaseActivity
import org.xm.app.virtual.call.ui.adapter.ViewPagerAdapter
import org.xm.app.virtual.call.ui.dialog.DialogHelper
import org.xm.app.virtual.call.ui.mvp.presenter.HomePresenter
import org.xm.app.virtual.call.ui.mvp.view.HomeView
import org.xm.app.virtual.call.ui.widget.tab.TabLayout

class HomeActivity : BaseActivity<HomeView, HomePresenter>(), HomeView,
    TabLayout.OnTabSelectedListener, View.OnClickListener {

    private lateinit var mVp: ViewPager
    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    private lateinit var mTab: TabLayout
    private lateinit var mTvTitle: TextView
    private var mainTabHighlight = 0
    private var mainTabUnSelectColor = 0

    private var mFragmentList = mutableListOf<Fragment>()

    private lateinit var mShakeAnimator: ObjectAnimator

    override fun getContentView(): Any = R.layout.vc_activity_home

    override fun initView() {
        //判断是否有刘海屏幕
        checkNotch(Runnable {
            val rootLayout = findViewById<ViewGroup>(R.id.rootLayout)
            val statusBarViewId = addStatusBar(rootLayout)
            if (rootLayout is RelativeLayout) {
                val topBar = findViewById<View>(R.id.top_bar).layoutParams as? RelativeLayout.LayoutParams
                topBar?.addRule(RelativeLayout.BELOW, statusBarViewId.id)
            }
        })


        mTab = findViewById(R.id.tab_layout)
        mVp = findViewById(R.id.id_main_vp)
        mTvTitle = findViewById(R.id.id_main_tv_title)

        mainTabHighlight = resources.getColor(R.color.vc_main_tab_highlight)
        mainTabUnSelectColor = resources.getColor(R.color.vc_main_tab_unselect_color)

        initViewPagerData()
        initViewPager()
        initTab()
    }

    override fun initData() {
        mPresenter?.requestPermission(this, Runnable {
            DialogHelper.createGuideDialog(this)
        })
    }

    private fun initViewPagerData() {
        mFragmentList.add(MainFragment())
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
                when (position) {
                    0 -> {
                        mTvTitle.text = getString(R.string.vc_app_name)
                    }
                    1 -> {
                        mTvTitle.text = getString(R.string.vc_tab_guide)
                    }
                    2 -> {
                        mTvTitle.text = getString(R.string.vc_setting)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_right -> {
            }
        }
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

    override fun createPresenter(): HomePresenter = HomePresenter()

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
                    imageView?.colorFilter = null
                    textView?.setTextColor(mainTabUnSelectColor)
                }
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabReselected(tab: TabLayout.Tab) {}

    private fun getTabView(position: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.vc_layout_bottom_tab, null)
        val imageView = view.findViewById<ImageView>(R.id.icon)
        val textView = view.findViewById<TextView>(R.id.text1)
        val tab = TabModel.getTab(position)
        textView.setText(tab.labelResId)
        imageView.setImageResource(tab.iconResId)
        return view
    }


    override fun onBackPressed() {
        checkExit()
    }
}