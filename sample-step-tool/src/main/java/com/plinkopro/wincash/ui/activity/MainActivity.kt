package com.plinkopro.wincash.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.carefree.steplib.utils.StepTracker
import com.google.android.material.tabs.TabLayout.Tab
import com.plinkopro.wincash.BuildConfig
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseActivity
import com.plinkopro.wincash.base.BaseApplication
import com.plinkopro.wincash.business.step.StepBusiness
import com.plinkopro.wincash.databinding.ActivityMainBinding
import com.plinkopro.wincash.databinding.MainTabViewBinding
import com.plinkopro.wincash.event.ChangeShowPage
import com.plinkopro.wincash.event.RequestPermissionEvent
import com.plinkopro.wincash.event.TabLayoutShowEvent
import com.plinkopro.wincash.init.AdIndex
import com.plinkopro.wincash.init.Constance
import com.plinkopro.wincash.ui.fragment.HomeFragment
import com.plinkopro.wincash.ui.fragment.LottoFragment
import com.plinkopro.wincash.ui.fragment.LuckyWheelFragment
import com.plinkopro.wincash.ui.fragment.WebFragment
import com.plinkopro.wincash.utils.InterAdUtil
import com.plinkopro.wincash.utils.MusicUtil
import com.plinkopro.wincash.utils.SimpleTabSelectedListener
import com.plinkopro.wincash.utils.SoundRawId
import com.plinkopro.wincash.utils.SoundUtil
import com.plinkopro.wincash.utils.dp2px
import com.plinkopro.wincash.utils.setVisible
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {
        const val TAB_HOME = 2

        // 缩放倍率 = 58dp / 48dp
        private const val ICON_SCALE_SELECTED = 58f / 48f
        private const val TEXT_SP_SELECTED = 15f
        private const val TEXT_SP_NORMAL = 12f
        private const val TAB_ANIM_DURATION = 150L
    }

    data class TabInfo(
        val title: String,
        val iconRes: Int,
        val fragmentTag: String
    )

    private val tabs by lazy {
        listOf(
            TabInfo("FUN", R.drawable.ic_tab_fun, LottoFragment::class.java.name),
            TabInfo("HOT", R.drawable.ic_tab_hot, LuckyWheelFragment::class.java.name),
            TabInfo("HOME", R.drawable.ic_tab_home, HomeFragment::class.java.name),
            TabInfo("JOY", R.drawable.ic_tab_joy, WebFragment::class.java.name + "1"),
            TabInfo("WIN", R.drawable.ic_tab_win, WebFragment::class.java.name + "2")
        )
    }

    private val homeFragment = HomeFragment()
    private val lottoFragment = LottoFragment()
    private val luckyWheelFragment = LuckyWheelFragment()
    private val webFragment1 = WebFragment.newInstance(Constance.OKSPIN_URL)
    private val webFragment2 = WebFragment.newInstance(Constance.OKSPIN_URL)

    private var currentFragment: Fragment? = null
    private var currentTabIndex = TAB_HOME

    private var isChangeEvent = true

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerEventbus()
        initTab()
        if (StepBusiness.hasRequirePermission(this)) {
            StepTracker.startTrackingService()
        }
        BaseApplication.timer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.timer.stop()
    }

    /** 初始化 TabLayout（创建、样式、监听） */
    private fun initTab() {

//        binding.tabLayout.removeAllTabs()
//        binding.tabLayout.setPadding(0, 0, 0, 0)

        // 创建自定义 tab
        tabs.forEachIndexed { index, info ->
            val tab = binding.tabLayout.newTab().apply {
                customView = MainTabViewBinding.inflate(layoutInflater).apply {
                    ivIcon.setImageResource(info.iconRes)
                    tvName.text = info.title
                }.root
            }
            binding.tabLayout.addTab(tab)
        }

//        updateAllTabs(binding.tabLayout.selectedTabPosition.takeIf { it >= 0 } ?: currentTabIndex,
//            animate = false)

        // 监听选中
        binding.tabLayout.addOnTabSelectedListener(object : SimpleTabSelectedListener() {
            override fun onTabSelected(tab: Tab?) {
                logBottomMenuClickEvent(tab?.position ?:currentTabIndex)
                tab ?: return
                val pos = tab.position
                currentTabIndex = pos

                SoundUtil.play(SoundRawId.CLICK.id)
                if (BuildConfig.LOG_OUTPUT) Log.d("tab", "tab position : $currentTabIndex")

                updateAllTabs(pos, animate = false)
                showFragment(tabs[pos].fragmentTag)
            }

            override fun onTabReselected(tab: Tab?) {
                super.onTabReselected(tab)
                logBottomMenuClickEvent(tab?.position ?:currentTabIndex)
            }
        })

//        showFragment(HomeFragment::class.java.name)
        // 选中默认 Tab
        binding.tabLayout.getTabAt(currentTabIndex)?.select()
    }

    /** 统一更新所有 Tab 的图标缩放与文字大小（可选动画） */
    private fun updateAllTabs(selectedPos: Int, animate: Boolean) {
        repeat(binding.tabLayout.tabCount) { index ->
            val tab = binding.tabLayout.getTabAt(index) ?: return@repeat
            val custom = tab.customView ?: return@repeat

            val tv = custom.findViewById<TextView>(R.id.tvName)
            val iv = custom.findViewById<ImageView>(R.id.ivIcon)

            val selected = index == selectedPos
            val targetScale = if (selected) ICON_SCALE_SELECTED else 1f
            val targetTextSp = if (selected) TEXT_SP_SELECTED else TEXT_SP_NORMAL

            if (animate) {
                iv.animate()
                    .scaleX(targetScale)
                    .scaleY(targetScale)
                    .setDuration(TAB_ANIM_DURATION)
                    .start()
            } else {
                iv.scaleX = targetScale
                iv.scaleY = targetScale
            }

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, targetTextSp)

            //
            val marginTop = if (selected) {
                dp2px(3f)
            } else {
                0
            }
            val lp = iv.layoutParams as ViewGroup.MarginLayoutParams
            lp.bottomMargin = marginTop
            iv.layoutParams = lp
        }
    }

    override fun onResume() {
        super.onResume()
        handleMusic()
        if (currentFragment is HomeFragment){
            binding.tabLayout.setVisible(true)
        }
    }

    private fun handleMusic() {
        if (currentFragment == null || currentFragment is HomeFragment) {
            MusicUtil.play()
        } else {
            MusicUtil.pause()
        }
    }

    private fun showFragment(tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        hideOtherFragment(transaction, tag)

        var fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = when (tag) {
                HomeFragment::class.java.name -> homeFragment
                LottoFragment::class.java.name -> lottoFragment
                LuckyWheelFragment::class.java.name -> luckyWheelFragment
                WebFragment::class.java.name + "1" -> webFragment1
                WebFragment::class.java.name + "2" -> webFragment2
                else -> homeFragment
            }
            if (!fragment.isAdded) {
                transaction.add(R.id.fragment_container, fragment, tag)
            }
        } else {
            transaction.show(fragment)
        }
        if (fragment!=homeFragment){
            EventBus.getDefault().post(TabLayoutShowEvent(false))
        }else{
            EventBus.getDefault().post(TabLayoutShowEvent(true))
        }

        if (fragment is WebFragment){
            MusicUtil.pause()
        }else{
            MusicUtil.play()
        }
        transaction.commitAllowingStateLoss()
        currentFragment = fragment
    }

    @SuppressLint("CommitTransaction")
    private fun hideOtherFragment(transaction: FragmentTransaction, tag: String) {
        val fragmentTag = listOf(
            HomeFragment::class.java.name,
            LottoFragment::class.java.name,
            LuckyWheelFragment::class.java.name,
            WebFragment::class.java.name + "1",
            WebFragment::class.java.name + "2"
        )
        fragmentTag.forEach {
            if (it != tag) {
                val fragment = supportFragmentManager.findFragmentByTag(it)
                if (fragment != null) {
                    transaction.hide(fragment)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        StepBusiness.handlePermissionResult(this, requestCode, grantResults) {
            StepTracker.startTrackingService()
        }
        if (requestCode == StepBusiness.RC_SETTING) {
            StepTracker.startTrackingService()
        }
    }

    private fun logBottomMenuClickEvent(index: Int  ) {
        if (!isChangeEvent) {
            currentFragment ?: return
            SdkManager.dot("menu_click", mapOf("menu_name" to tabs[index].title))
        }else{
            isChangeEvent = false
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeShowPageEvent(event: ChangeShowPage) {
        if (event.position in tabs.indices) {
            isChangeEvent = true
            // 选中默认 Tab
            binding.tabLayout.getTabAt(event.position)?.select()

            if (event.position == 2) {
                binding.root.postDelayed({
                    if (InterAdUtil.showAd()) {
                        AdManager.showInterAd(this, AdIndex.ADMOB_INTER_INDEX)
                    }
                }, 500)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabLayoutShowEvent(event: TabLayoutShowEvent) {
        binding.tabLayout.setVisible(event.show)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceivePermissionEvent(event: RequestPermissionEvent) {

    }
}
