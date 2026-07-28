package com.alsg.bakericon.vm

import androidx.fragment.app.Fragment
import app.allever.android.lib.core.app.App
import app.allever.android.lib.mvvm.base.BaseViewModel
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
        R.string.si_tab_icon,
        R.string.si_tab_sticker,
        R.string.si_tab_top,
        R.string.si_tab_more
    )
    private val mIconUnselectIds = intArrayOf(
        R.drawable.si_home,
        R.drawable.si_doge,
        R.drawable.si_top,
        R.drawable.si_more
    )
    private val mIconSelectIds = intArrayOf(
        R.drawable.si_home_selected,
        R.drawable.si_doge_selected,
        R.drawable.si_top_selected,
        R.drawable.si_more_selected,
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