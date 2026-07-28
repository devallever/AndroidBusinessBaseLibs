package com.alsg.bakericon.base

import android.view.Gravity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import app.allever.android.lib.core.helper.ViewHelper
import app.allever.android.lib.core.util.BarUtils
import app.allever.android.lib.imageloader.core.load
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alsg.bakericon.R
import com.alsg.bakericon.databinding.ActivityBaseSlideMenuStyle2Binding
import com.alsg.bakericon.databinding.RvSlideMenuBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/22
 */
abstract class BaseSlideMenuActivityStyle2<DB : ViewBinding, VM : BaseViewModel> :
    AppActivity<ActivityBaseSlideMenuStyle2Binding, VM>() {

    abstract fun menuFragments(): MutableList<Fragment>
    abstract fun menuItemTitles(): MutableList<String>
    abstract fun menuItemIcons(): MutableList<Int>
    abstract fun menuIcon(): Int
    abstract fun menuTitle(): String

    override fun inflate() = ActivityBaseSlideMenuStyle2Binding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            ViewHelper.setMarginTop(topBar, BarUtils.getStatusBarHeight())
            ivLogo.load(menuIcon())
            tvTitleMenu.text = menuTitle()
            ivMenu.setOnClickListener {
                drawer.openDrawer(Gravity.LEFT)
            }

            val fragmentList = menuFragments()
            val titles = menuItemTitles()
            val icons = menuItemIcons()

            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragmentList[0])
                .commit()
            val adapter = SlideMenuAdapter()
            adapter.setList(mutableListOf<SlideMenuItem>(
            ).apply {
                List(fragmentList.size) { index ->
                    add(SlideMenuItem(titles[index], icons[index]))
                }
            })
            adapter.setOnItemClickListener { adt, view, position ->
                val item = adapter.getItem(position)
                lifecycleScope.launch {
                    delay(300)
                    drawer.closeDrawers()
                    delay(300)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragmentList[position])
                        .commit()
                    tvTitle.text = item.menu
                }
            }
            rvMenu.layoutManager = LinearLayoutManager(this@BaseSlideMenuActivityStyle2)
            rvMenu.adapter = adapter
        }
    }

    override fun onBackPressed() {
        if (mBinding.drawer.isDrawerOpen(Gravity.LEFT)) {
            mBinding.drawer.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    class SlideMenuItem(val menu: String, val icon: Int)

    class SlideMenuAdapter :
        BaseQuickAdapter<SlideMenuItem, BaseViewHolder>(R.layout.rv_slide_menu) {
        override fun convert(holder: BaseViewHolder, item: SlideMenuItem) {
            val binding = RvSlideMenuBinding.bind(holder.itemView)
            binding.apply {
                tvMenu.text = item.menu
                ivIcon.load(item.icon)
            }
        }
    }

    override fun isDarkMode(): Boolean {
        return true
    }

    override fun isSupportSwipeBack(): Boolean {
        return false
    }
}

