
package com.allever.app.gif.memes.ui.maker

import android.graphics.Rect
import android.view.View
import androidx.core.view.isVisible
import app.allever.android.lib.core.helper.DisplayHelper
import app.allever.android.lib.core.util.ResUtils
import app.allever.android.lib.mvvm.base.BaseMvvmActivity
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.databinding.ActivityPickBinding
import com.funny.gif.memes.event.GifMakeEvent
import com.allever.app.gif.memes.ui.maker.model.PickViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PickActivity : BaseMvvmActivity<ActivityPickBinding, PickViewModel>(){



    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeUpdate(gifMakeEvent: GifMakeEvent) {
        finish()
    }

    override fun inflate(): ActivityPickBinding = ActivityPickBinding.inflate(layoutInflater)

    override fun init() {
        initObserver()
        EventBus.getDefault().register(this)
        mBinding.ivLeft.setColorFilter(ResUtils.getColor(R.color.white))
        mBinding.tvLabel.setTextColor(ResUtils.getColor(R.color.white))
        mBinding.tvLabel.text = getString(R.string.choose_video)
        mBinding.ivLeft.setOnClickListener {
            finish()
        }


        val spacingInPixels = DisplayHelper.dip2px(2)
        val firstTopSpacing = DisplayHelper.dip2px(2)
        val bottomSpacing = DisplayHelper.dip2px(2)
        val middleSpacing = DisplayHelper.dip2px(2)
        mBinding.rvMedia.layoutManager = mViewModel.layoutManager
        mBinding.rvMedia.adapter = mViewModel.adapter
        mBinding.rvMedia.addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State
            ) {
                val COL = 3
                val pos = parent.getChildLayoutPosition(view)
                if (pos / COL == 0) {
                    //设置第一行
                    outRect.top = firstTopSpacing
                }

                outRect.bottom = bottomSpacing
            }
        })
    }

    private fun initObserver() {
        mViewModel.confirmClickAble.observe(this) {
            mBinding.btnConfirm.isEnabled = it
            if ( it) {
                mBinding.btnConfirm.setBackgroundResource(R.drawable.shape_0091ea_r4)
            } else {
                mBinding.btnConfirm.setBackgroundResource(R.drawable.shape_cccccc_r4)
            }
        }
        mBinding.btnConfirm.setOnClickListener {
            mViewModel.onClickConfirm()
        }

        mViewModel.confirmShow.observe(this) {
            mBinding.btnConfirm.isVisible = it
        }
    }
}