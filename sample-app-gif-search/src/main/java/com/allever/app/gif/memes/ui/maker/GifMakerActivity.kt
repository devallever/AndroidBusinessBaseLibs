package com.allever.app.gif.memes.ui.maker

import android.content.Context
import android.os.Bundle
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.mvvm.base.BaseMvvmActivity
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.databinding.ActivityGifMakerBinding
import com.funny.gif.memes.func.media.MediaBean
import com.allever.app.gif.memes.ui.maker.model.GifMakerViewModel
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar

class GifMakerActivity : BaseMvvmActivity<ActivityGifMakerBinding, GifMakerViewModel>() {

    lateinit var mVideoViewHolder: VideoViewHolder

    companion object {
        private const val EXTRA_DATA = "EXTRA_DATA"
        fun start(context: Context, data: MediaBean) {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_DATA, data)
            ActivityHelper.startActivity<GifMakerActivity>() {
                putExtras(bundle)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mVideoViewHolder.stop()
        mVideoViewHolder.destroy()
    }

    fun pause() {
        mVideoViewHolder.pause()
    }

    override fun inflate(): ActivityGifMakerBinding = ActivityGifMakerBinding.inflate(layoutInflater)

    override fun init() {
        initObserver()
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

    private fun initObserver() {
        mViewModel.startText.observe(this) {
            mBinding.tvStart.text = it
        }
        mViewModel.endText.observe(this) {
            mBinding.tvEnd.text = it
        }
        mViewModel.durationText.observe(this) {
            mBinding.tvDuration.text = it
        }
        //confirmClickAble
        mViewModel.confirmClickAble.observe(this) {
            mBinding.btnConfirm.isEnabled = it
            if ( it) {
                mBinding.btnConfirm.setBackgroundResource(R.drawable.shape_0091ea_r4)
            } else {
                mBinding.btnConfirm.setBackgroundResource(R.drawable.shape_cccccc_r4)
            }
        }
        mViewModel.confirmText.observe(this) {
            mBinding.btnConfirm.text = it
        }

        mBinding.btnConfirm.setOnClickListener {
            mViewModel.onClickConfirm(this)
        }
    }
}