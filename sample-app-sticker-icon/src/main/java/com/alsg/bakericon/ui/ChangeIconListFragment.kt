package com.alsg.bakericon.ui

import androidx.viewbinding.ViewBinding
import com.allever.lib.base.ext.log
import com.allever.lib.base.ext.toast
import com.allever.lib.base.helper.AppHelper
import com.allever.lib.base.helper.AppItem
import com.allever.lib.base.mvvm.BaseViewModel
import com.alsg.bakericon.base.BaseListFragment
import com.alsg.bakericon.ui.adapter.AppItemAdapter
import com.alsg.bakericon.util.AssetsHelper
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 *@Description
 *@author: zq
 *@date: 2024/2/1
 */
class ChangeIconListFragment() : BaseListFragment<ViewBinding, BaseViewModel, AppItem>() {
    private var path = ""
    override fun init() {
        super.init()
        path = arguments?.getString("path") ?: ""
        log("path = $path")
    }

    override fun getAdapter(): BaseQuickAdapter<AppItem, *> = AppItemAdapter()

    override fun getList() = mutableListOf<AppItem>().apply {
        addAll(AppHelper.fetchLocalAppList2())
    }

    override fun initObserver() {

    }

    override fun onItemClick(position: Int, item: AppItem) {
        val result = AppHelper.createShortcut(
            item.pkg,
            item.launchActivity,
            item.name,
            AssetsHelper.toBitmap(path)
        )
        if (result) {
            toast("Create Shortcut success!")
            requireActivity().finish()
        } else {
            toast("Fail to change icon!")
        }


    }
}