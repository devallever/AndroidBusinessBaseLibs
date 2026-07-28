package com.alsg.bakericon.ui

import android.Manifest
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.allever.lib.billing.BillingHelper
import com.allever.lib.base.ext.launchAndCollectIn
import com.allever.lib.base.ext.log
import com.allever.lib.base.ext.toast
import com.allever.lib.base.function.imageloader.load
import com.allever.lib.base.helper.ActivityHelper
import com.allever.lib.base.helper.AppHelper
import com.allever.lib.base.helper.PermissionHelper
import com.alsg.bakericon.R
import com.alsg.bakericon.ad.AdConstants
import com.alsg.bakericon.ad.AdRepository
import com.alsg.bakericon.base.AppActivity
import com.alsg.bakericon.base.AppFragmentActivity
import com.alsg.bakericon.databinding.ActivityPreviewBinding
import com.alsg.bakericon.db.DBRepo
import com.alsg.bakericon.vm.PreviewViewModel
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
class PreviewActivity : AppActivity<ActivityPreviewBinding, PreviewViewModel>() {
    companion object {
        private const val EXTRA_PATH = "path"
        fun start(path: String) {
            ActivityHelper.startActivity<PreviewActivity>() {
                putExtra(EXTRA_PATH, path)
            }
        }
    }

    override fun inflate() = ActivityPreviewBinding.inflate(layoutInflater)

    override fun init() {
        AppHelper.preLoad(this)

        BillingHelper.checkScribeStatus { success, code, message ->
            if (!success) {
                AdRepository.instance.loadInterAd(AdConstants.INTER_AD)
            }
        }
        mBinding.apply {
            mViewModel.path = intent?.getStringExtra(EXTRA_PATH) ?: ""
//            log("preview path = ${mViewModel.path}")
            mBinding.ivImage.load(mViewModel.path)

            initPreviewColorList()

            ivBack.setOnClickListener {
                finish()
            }

            btnUse.setOnClickListener {
                //仅订阅能够更改图标
                BillingHelper.checkScribeStatus { success, code, message ->
                    if (success) {
                        val hasPermission = PermissionHelper.hasPermissionOrigin(
                            this@PreviewActivity,
                            listOf(Manifest.permission.INSTALL_SHORTCUT)
                        )

                        if (hasPermission) {
                            AppFragmentActivity.start<ChangeIconListFragment>("Select App") {
                                // file:///android_asset/icon/2/6.png
                                val path = mViewModel.path.split("android_asset/")[1]
                                it.putString("path", path)
                                log("put path = $path")
                            }

                        } else {
                            toast("no permission")
                        }
                    } else {
                        //弹出订阅界面
                        ActivityHelper.startActivity<BillingActivity>(this@PreviewActivity) { }
                    }
                }
            }

            btnSave.setOnClickListener {
                //检查订阅状态
                BillingHelper.checkScribeStatus { success, code, message ->
                    if (success) {
                        mViewModel.handleClickSave(this@PreviewActivity)
                    } else {
                        //检查每天下载次数
                        lifecycleScope.launch {
                            if (mViewModel.todayCanDownload()) {
                                mViewModel.handleClickSave(this@PreviewActivity)
                            } else {
                                //弹出订阅界面
                                ActivityHelper.startActivity<BillingActivity>(this@PreviewActivity) { }
                            }
                        }
                    }
                }

            }

            ivLike.setOnClickListener {
                updateLikeState()
            }
        }
    }

    private fun initPreviewColorList() {
        mBinding.apply {
            rvPreview.layoutManager =
                LinearLayoutManager(this@PreviewActivity, LinearLayoutManager.HORIZONTAL, false)
            rvPreview.adapter = mViewModel.adapter
            mViewModel.adapter.data = mViewModel.previewList
            mViewModel.adapter.setOnItemClickListener { adapter, view, position ->
                val item = mViewModel.adapter.data[position]
                centerPreview.setBackgroundResource(item.previewBackground)
            }
        }
    }

    override fun initObserver() {
        mViewModel.likeStateFlow.launchAndCollectIn(this) {
            mBinding.ivLike.setImageResource(
                if (it) {
                    R.drawable.liked
                } else {
                    R.drawable.like
                }
            )
        }
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            mBinding.ivLike.setImageResource(
                if (DBRepo.isLike(mViewModel.path)) {
                    R.drawable.liked
                } else {
                    R.drawable.like
                }
            )
        }
        mViewModel.fetchLikeStatus()
    }

    private fun updateLikeState() {
        mBinding.apply {
            lifecycleScope.launch {
                val isLike = DBRepo.isLike(mViewModel.path)
                if (isLike) {
                    val success = DBRepo.disLike(mViewModel.path)
                    if (success) ivLike.setImageResource(R.drawable.like)
                } else {
                    val success = DBRepo.like(mViewModel.path)
                    if (success) ivLike.setImageResource(R.drawable.liked)
                }
            }
        }
    }
}