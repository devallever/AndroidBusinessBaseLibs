package com.clean.wood.vm

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.clean.wood.R
import com.clean.wood.WoodApp
import com.clean.wood.data.AdManager
import com.clean.wood.data.model.AppItem
import com.clean.wood.ui.adapter.AppItemAdapter
import com.clean.wood.utils.Constant
import com.clean.wood.utils.toast

class AppListViewModel : ViewModel() {

    val selectedList by lazy {
        mutableListOf<AppItem>()
    }

    var jumpResult = false

    val adapter by lazy {
        AppItemAdapter(WoodApp.appInfoLost).apply {
            itemClickListener = object : AppItemAdapter.ItemClickListener {
                override fun onItemClick(item: AppItem) {
                    if (item.select) {
                        selectedList.add(item)
                    } else {
                        selectedList.remove(item)
                    }
                }
            }
        }
    }

    fun openSystemAppManage() {
        if (selectedList.isEmpty()) {
            toast(WoodApp.context.getString(R.string.please_select_app))
            return
        }

        jumpResult = true

        openSystemAppManagePage()
    }

    private fun openSystemAppManagePage() {
        //Show settings to manage installed applications.
        if (openSystemAppManagePageSafety(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)) {
            return
        }
        //Show settings to manage all applications.
        if (openSystemAppManagePageSafety(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)) {
            return
        }
        //Show settings to allow configuration of application-related settings.
        if (openSystemAppManagePageSafety(Settings.ACTION_APPLICATION_SETTINGS)) {
            return
        }
        //show setting page
        openSystemAppManagePageSafety(Settings.ACTION_SETTINGS)
    }

    private fun openSystemAppManagePageSafety(action: String): Boolean {
        try {
            val intent = Intent(action)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            WoodApp.context.startActivity(intent)
            return true
        } catch (_: ActivityNotFoundException) {

        }
        return false
    }

    private fun openAppDetailsPage(context: Context, pkgName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri: Uri = Uri.fromParts("package", pkgName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun checkAd() {
        AdManager.ins.checkAd(Constant.AdPosition.OptimizingInter)
    }
}