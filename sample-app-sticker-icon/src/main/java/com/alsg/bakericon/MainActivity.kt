package com.alsg.bakericon

import androidx.viewpager2.widget.ViewPager2
import app.allever.android.lib.core.base.adapter.Pager2Adapter
import app.allever.android.lib.core.ext.modifyTouchSlop
import com.alsg.bakericon.base.AppActivity
import com.alsg.bakericon.databinding.ActivityMainBinding
import com.alsg.bakericon.vm.MainViewModel
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener


class MainActivity : AppActivity<ActivityMainBinding, MainViewModel>() {


    override fun inflate() = ActivityMainBinding.inflate(layoutInflater)

    override fun init() {
        initTab()
    }

    private fun initTab() {
        mBinding.apply {
            viewPager.adapter = Pager2Adapter(this@MainActivity, mViewModel.fragmentList)
            viewPager.modifyTouchSlop()
            viewPager.offscreenPageLimit = mViewModel.fragmentList.size
            tabLayout.setTabData(mViewModel.tabEntities)
            tabLayout.setOnTabSelectListener(object : OnTabSelectListener {
                override fun onTabSelect(position: Int) {
                    viewPager.currentItem = position
                }

                override fun onTabReselect(position: Int) {
                }

            })
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    tabLayout.currentTab = position
                }
            })
        }

    }

}

class TabEntity(var title: String, var selectedIcon: Int, var unSelectedIcon: Int) :
    CustomTabEntity {
    override fun getTabTitle(): String {
        return title
    }

    override fun getTabSelectedIcon(): Int {
        return selectedIcon
    }

    override fun getTabUnselectedIcon(): Int {
        return unSelectedIcon
    }
}