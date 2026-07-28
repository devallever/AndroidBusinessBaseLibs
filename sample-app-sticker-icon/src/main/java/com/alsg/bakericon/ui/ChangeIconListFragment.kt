package com.alsg.bakericon.ui

import androidx.viewbinding.ViewBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import com.alsg.bakericon.ui.adapter.data.AppItem
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alsg.bakericon.base.BaseListFragment
import com.alsg.bakericon.ui.adapter.AppItemAdapter
import com.alsg.bakericon.util.AssetsHelper
import com.alsg.bakericon.util.PackageHelper
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
        addAll(PackageHelper.fetchLocalAppList2())
    }


    override fun onItemClick(position: Int, item: AppItem) {
        val result = PackageHelper.createShortcut(
            item.pkg,
            item.launchActivity,
            item.name,
            AssetsHelper.toBitmap(path)
        )
        if (result) {
            toast("Create Shortcut success!")
            requireActivity().finish()
        } else {
            toast("Fail to change si_icon!")
        }


    }
}