
package com.allever.app.gif.memes.ui.maker

import android.graphics.Rect
import android.view.View
import com.allever.app.gif.memes.BR
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.BaseDataActivity2
import com.allever.app.gif.memes.databinding.ActivityPickBinding
import com.funny.gif.memes.event.GifMakeEvent
import com.allever.app.gif.memes.ui.maker.model.PickViewModel
import com.allever.lib.common.util.ResUtils
import com.android.absbase.utils.ResourcesUtils
import com.xm.lib.base.config.DataBindingConfig
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PickActivity : BaseDataActivity2<ActivityPickBinding, PickViewModel>(){

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_pick, BR.pickViewModel)

    override fun initDataAndEvent() {
        EventBus.getDefault().register(this)
        mBinding.includeTopBar.ivLeft.setColorFilter(ResUtils.getColor(R.color.white))
        mBinding.includeTopBar.tvLabel.setTextColor(ResUtils.getColor(R.color.white))
        mBinding.includeTopBar.tvLabel.text = getStringRes(R.string.choose_video)
        mBinding.includeTopBar.ivLeft.setOnClickListener {
            finish()
        }


        val spacingInPixels = ResourcesUtils.getDimension(R.dimen.item_cell_space_width).toInt()
        val firstTopSpacing = ResourcesUtils.getDimension(R.dimen.item_cell_space_top).toInt()
        val bottomSpacing = ResourcesUtils.getDimension(R.dimen.item_cell_space_width).toInt()
        val middleSpacing = ResourcesUtils.getDimension(R.dimen.item_cell_space_middle).toInt()
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

    override fun destroyView() {
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeUpdate(gifMakeEvent: GifMakeEvent) {
        finish()
    }
}