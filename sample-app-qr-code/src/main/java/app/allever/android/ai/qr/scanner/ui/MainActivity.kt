package app.allever.android.ai.qr.scanner.ui

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.allever.android.ai.qr.scanner.AppActivity
import app.android.base.lib.tab.TabLayout
import com.allever.app.qr.code.scaner.R
import app.allever.android.ai.qr.scanner.core.RateGuide
import app.allever.android.lib.recommend.ui.RecommendDialog
import com.allever.app.qr.code.scaner.databinding.ActivityMainBinding
import app.android.base.lib.notchcompat.NotchCompat


class MainActivity : AppActivity(), TabLayout.OnTabSelectedListener, View.OnClickListener {

    private lateinit var container: androidx.viewpager.widget.ViewPager
    //    lateinit var toolbar: Toolbar
    private lateinit var mTab: TabLayout

    private lateinit var mSectionsPagerAdapter: FragmentTabPagerAdapter

    private var mainTabHighlight: Int = 0
    private var mainTabUnSelectColor: Int = 0

    private lateinit var mBinding: ActivityMainBinding

    private val mRecommendDialog by lazy {
        RecommendDialog(this) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // 解决Fragment使用SurfaceView出现闪屏的问题（启动页进入会闪屏一次）
//        window.setFormat(PixelFormat.TRANSLUCENT)

        mainTabHighlight = resources.getColor(R.color.main_tab_highlight)
        mainTabUnSelectColor = resources.getColor(R.color.main_tab_unselect_color)

        mSectionsPagerAdapter = FragmentTabPagerAdapter(this, supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container = findViewById(R.id.container)
        container.offscreenPageLimit = 4
        container.adapter = mSectionsPagerAdapter

        mTab = findViewById<TabLayout>(R.id.tab_layout)
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTab))
        mTab.setOnTabSelectedListener(this)

        mBinding.ivRecommend.visibility = View.GONE
        mBinding.ivRecommend.setOnClickListener(this)


        initTab()

        NotchCompat.adaptNotchWithFullScreen(window)
        val v = findViewById<View>(R.id.top_bar)
        checkNotch(Runnable {
            addStatusBar(mBinding.rootLayout, v)
        })
        mBinding.ivHistory.setOnClickListener {
            // 跳转HistoryActivity
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        mBinding.ivSetting.setOnClickListener {
            // 跳转HistoryActivity
            startActivity(Intent(this, SettingActivity::class.java))
        }

    }

    private fun initTab() {
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

        mTab.setSelectedTabIndicatorWidth(0)
        mTab.setSelectedTabIndicatorHeight(0)
        mTab.setSelectedTabIndicatorColor(mainTabHighlight)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.ivRecommend -> {
            }
        }
    }

    private fun getTabView(position: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.tab_item, null)
        val imageView = view.findViewById<ImageView>(R.id.icon)
        val textView = view.findViewById<TextView>(R.id.text1)
        val tab = TabModel.getTab(position)
        textView.setText(tab.labelResId)
        imageView.setImageResource(tab.iconResId)
        return view
    }

    override fun onStart() {
        super.onStart()
        RateGuide.Builder().show(this)
    }

    private var mIsAdLoaded = false

    override fun onTabSelected(tab: TabLayout.Tab) {
        container.currentItem = tab.position
        TabModel.selectedTab = tab.tag as TabModel.Tab

        TabModel.selectedTab = (tab.tag as TabModel.Tab)
        for (i in 0 until mTab.tabCount) {
            val aTab = mTab.getTabAt(i)
            if (aTab != null) {
                val imageView = aTab.customView?.findViewById<ImageView>(R.id.icon)
                val textView = aTab.customView?.findViewById<TextView>(R.id.text1)
                if (aTab === tab) {
                    imageView?.colorFilter = null
                    textView?.setTextColor(mainTabHighlight)
                } else {
                    imageView?.setColorFilter(mainTabUnSelectColor, PorterDuff.Mode.SRC_IN)
                    textView?.setTextColor(mainTabUnSelectColor)
                }
            }
        }
//        mBinding.tvTitle.text = getString(TabModel.selectedTab.labelResId)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        // No-op
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        // No-op
    }

    override fun onBackPressed() {
        if (mRecommendDialog.isShowing) {
            mRecommendDialog.dismiss()
        } else {
            mRecommendDialog.show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currentFragment = mSectionsPagerAdapter.getCurrentFragment() as BaseFragment
        if (currentFragment.onKeyDown(keyCode, event)) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    companion object {
        private const val RC_RECOMMEND_BACK = 0x01
    }
}
