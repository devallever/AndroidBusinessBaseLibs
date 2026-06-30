package app.android.allever.gp.quick.project.ui

import androidx.viewpager2.widget.ViewPager2
import app.allever.android.lib.core.base.adapter.Pager2Adapter
import app.allever.android.lib.core.ext.modifyTouchSlop
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.NstActivityHomeBinding
import app.android.allever.gp.quick.project.vm.HomeViewModel
import com.flyco.tablayout.listener.OnTabSelectListener

/**
 *@Description
 *@author: zq
 *@date: 2024/1/20
 */
class NSTHomeActivity :
    AppActivity<NstActivityHomeBinding, HomeViewModel>() {
    override fun inflate() = NstActivityHomeBinding.inflate(layoutInflater)

    override fun init() {
        adaptStatusBar(mBinding.viewPager)
        initTab()
    }

    private fun initTab() {
        mBinding.apply {
            viewPager.adapter = Pager2Adapter(this@NSTHomeActivity, mViewModel.fragmentList)
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