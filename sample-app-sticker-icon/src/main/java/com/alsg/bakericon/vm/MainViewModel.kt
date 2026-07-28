package com.alsg.bakericon.vm

import androidx.fragment.app.Fragment
import com.allever.lib.base.app.App
import com.allever.lib.base.mvvm.BaseViewModel
import com.alsg.bakericon.R
import com.alsg.bakericon.TabEntity
import com.alsg.bakericon.ui.IconFragment
import com.alsg.bakericon.ui.MoreFragment
import com.alsg.bakericon.ui.StickerFragment
import com.alsg.bakericon.ui.TopFragment
import com.flyco.tablayout.listener.CustomTabEntity

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class MainViewModel : BaseViewModel() {
    val fragmentList = mutableListOf<Fragment>().apply {
        add(IconFragment())
        add(StickerFragment())
        add(TopFragment())
        add(MoreFragment())
    }

    private val mTitles = arrayOf(
        R.string.tab_icon,
        R.string.tab_sticker,
        R.string.tab_top,
        R.string.tab_more
    )
    private val mIconUnselectIds = intArrayOf(
        R.drawable.home,
        R.drawable.doge,
        R.drawable.top,
        R.drawable.more
    )
    private val mIconSelectIds = intArrayOf(
        R.drawable.home_selected,
        R.drawable.doge_selected,
        R.drawable.top_selected,
        R.drawable.more_selected,
    )
    val tabEntities: ArrayList<CustomTabEntity> =
        arrayListOf<CustomTabEntity>().apply {
            for (i in mTitles.indices) {
                add(
                    TabEntity(
                        App.context.getString(mTitles[i]),
                        mIconSelectIds[i],
                        mIconUnselectIds[i]
                    )
                )
            }
        }
}