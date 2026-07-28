package com.alsg.bakericon

import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alsg.bakericon.base.BaseSlideMenuActivityStyle2
import com.alsg.bakericon.ui.*

/**
 *@Description
 *@author: zq
 *@date: 2024/1/31
 */
class SlideMainActivity: BaseSlideMenuActivityStyle2<ViewBinding, BaseViewModel>() {
    override fun menuFragments(): MutableList<Fragment> = mutableListOf(
        IconFragment(), StickerFragment(), TopFragment(),FavouritesFragment(), MoreFragment()
    )

    override fun menuItemTitles() = mutableListOf(
        getString(R.string.si_tab_icon),
        getString(R.string.si_tab_sticker),
        getString(R.string.si_tab_top),
        getString(R.string.si_favourites),
                getString(R.string.si_tab_more),
    )

    override fun menuItemIcons() = mutableListOf(
        R.drawable.si_home_selected,
        R.drawable.si_doge_selected,
        R.drawable.si_top_selected,
        R.drawable.si_ic_favourite,
        R.drawable.si_more_selected,

    )

    override fun menuIcon() = R.drawable.si_logo

    override fun menuTitle() = getString(R.string.si_app_name)
}