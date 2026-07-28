package com.alsg.bakericon.vm

import android.Manifest
import android.os.Build
import androidx.lifecycle.viewModelScope
import app.allever.lib.billing.BillingHelper
import com.allever.lib.base.app.App
import com.allever.lib.base.ext.toast
import com.allever.lib.base.helper.PermissionHelper
import com.allever.lib.base.mvvm.BaseViewModel
import com.alsg.bakericon.Constant
import com.alsg.bakericon.R
import com.alsg.bakericon.ad.AdConstants
import com.alsg.bakericon.ad.AdRepository
import com.alsg.bakericon.db.DBRepo
import com.alsg.bakericon.ui.PreviewActivity
import com.alsg.bakericon.ui.adapter.PreviewItemAdapter
import com.alsg.bakericon.ui.adapter.data.PreviewItem
import com.alsg.bakericon.logic.SaveRepo
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
class PreviewViewModel : BaseViewModel() {
    var path = ""
    val adapter = PreviewItemAdapter()
    val previewList = mutableListOf<PreviewItem>().apply {
        //白色 white: #ffffff
        //红色 red: #df1414;
        //蓝色 blue: #1d37e3;
        //黑色 black: #000000;
        //绿色 green: #16e722;
        //橙色 orange: #ffa500;
        //紫色 purple: #c013e4;
        //金色 gold: #ffd700;
        //青色 cyan: #14ebeb;
        add(PreviewItem(R.drawable.preview_white, R.drawable.preview_white_small))
        add(PreviewItem(R.drawable.preview_red, R.drawable.preview_red_small))
        add(PreviewItem(R.drawable.preview_blue, R.drawable.preview_blue_small))
        add(PreviewItem(R.drawable.preview_black, R.drawable.preview_black_small))
        add(PreviewItem(R.drawable.preview_green, R.drawable.preview_green_small))
        add(PreviewItem(R.drawable.preview_orange, R.drawable.preview_orange_small))
        add(PreviewItem(R.drawable.preview_purple, R.drawable.preview_purple_small))
        add(PreviewItem(R.drawable.preview_goal, R.drawable.preview_goal_small))
        add(PreviewItem(R.drawable.preview_cyan, R.drawable.preview_cyan_small))
    }


    private val likeFlowData = MutableStateFlow(false)
    val likeStateFlow = likeFlowData.asStateFlow()

    private val permissionsList = java.util.ArrayList<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun fetchLikeStatus() {
        viewModelScope.launch {
            likeFlowData.value = DBRepo.isLike(path)
        }
    }

    fun handleClickSave(previewActivity: PreviewActivity) {
        if (PermissionHelper.hasPermissionOrigin(previewActivity, permissionsList)) {
            //save
            viewModelScope.launch {
                SaveRepo.save(path) {
                    BillingHelper.checkScribeStatus { success, code, message ->
                        if (!success) {
                            AdRepository.instance.triggerShowInterAd(AdConstants.INTER_AD)
                        }
                    }
                }
            }
        } else {
            PermissionX
                .init(previewActivity)
                .permissions(permissionsList)
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
//                                toast("同意权限")
                        //save
                        viewModelScope.launch {
                            SaveRepo.save(path)
                        }
                    } else {
                        if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(previewActivity, permissionsList)) {
                            toast(App.context.getString(R.string.grant_permission_manually))
                            PermissionHelper.gotoSettingOrigin(previewActivity)
                        } else {
                            toast(App.context.getString(R.string.cant_save_without_permission))
                        }
                    }
                }
        }
    }

    suspend fun todayCanDownload(): Boolean {
        return DBRepo.getTodayDownloadCount() < Constant.DOWNLOAD_COUNT_EVERY_DAY
    }

}