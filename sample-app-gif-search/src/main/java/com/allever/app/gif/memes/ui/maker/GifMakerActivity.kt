package com.allever.app.gif.memes.ui.maker

import android.content.Context
import android.os.Bundle
import com.allever.app.gif.memes.BR
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.BaseDataActivity2
import com.allever.app.gif.memes.databinding.ActivityGifMakerBinding
import com.funny.gif.memes.func.media.MediaBean
import com.allever.app.gif.memes.ui.maker.model.GifMakerViewModel
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.xm.lib.base.config.DataBindingConfig
import com.xm.lib.manager.IntentManager

class GifMakerActivity : BaseDataActivity2<ActivityGifMakerBinding, GifMakerViewModel>() {

    lateinit var mVideoViewHolder: VideoViewHolder

    companion object {
        private const val EXTRA_DATA = "EXTRA_DATA"
        fun start(context: Context, data: MediaBean) {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_DATA, data)
            IntentManager.startActivity(context, GifMakerActivity::class.java, bundle)
        }
    }

    override fun statusColor() = R.color.trans

    override fun initDataAndEvent() {
        mViewModel.mediaBean = intent?.getParcelableExtra(EXTRA_DATA)
        mVideoViewHolder = VideoViewHolder()
        mVideoViewHolder.initVideo(
            mBinding.videoView, mViewModel.mediaBean?.uri, mViewModel.mediaBean?.path, mBinding.ivPlayPause
        )
        mBinding.ivPlayPause
        mBinding.rangeSeekBar.setRange(0F, mViewModel.mediaBean?.duration?.toFloat() ?: 0F, 1F)
        mBinding.rangeSeekBar.setProgress(0F, mViewModel.mediaBean?.duration?.toFloat() ?: 0F)
        mViewModel.startPosition = 0
        mViewModel.endPosition = mViewModel.mediaBean?.duration?.toInt()?:1
        mViewModel.startText.value = "0"
        mViewModel.endText.value = (mViewModel.endPosition / 1000f).toString()
        mViewModel.durationText.value = "${(mViewModel.endPosition - mViewModel.startPosition) / 1000f}秒"

        mBinding.rangeSeekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                mViewModel.startPosition = leftValue.toInt()
                mViewModel.endPosition = rightValue.toInt()
                mVideoViewHolder.seekTo(leftValue.toInt())
                mViewModel.startText.value = (leftValue / 1000).toString()
                mViewModel.endText.value = (rightValue / 1000).toString()
                mViewModel.durationText.value = "${(mViewModel.endPosition - mViewModel.startPosition) / 1000f}秒"
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                mVideoViewHolder.pause()
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                mVideoViewHolder.play(mViewModel.endPosition)
            }

        })
    }

    fun pause() {
        mVideoViewHolder.pause()
    }


    override fun destroyView() {
        mVideoViewHolder.stop()
        mVideoViewHolder.destroy()
    }

    override fun initDataBindingConfig() =
        DataBindingConfig(R.layout.activity_gif_maker, BR.gifMakerViewModel)
}