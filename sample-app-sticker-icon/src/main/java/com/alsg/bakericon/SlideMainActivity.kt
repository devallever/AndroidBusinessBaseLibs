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
        getString(R.string.tab_icon),
        getString(R.string.tab_sticker),
        getString(R.string.tab_top),
        getString(R.string.favourites),
                getString(R.string.tab_more),
    )

    override fun menuItemIcons() = mutableListOf(
        R.drawable.home_selected,
        R.drawable.doge_selected,
        R.drawable.top_selected,
        R.drawable.ic_favourite,
        R.drawable.more_selected,

    )

    override fun menuIcon() = R.drawable.logo

    override fun menuTitle() = getString(R.string.app_name)
}