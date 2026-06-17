package app.allever.android.sample.audiovideo.sdk

import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentSdkExoVideoPlayerSampleBinding

class ExoVideoPlayerSampleFragment :
    BaseFragment<FragmentSdkExoVideoPlayerSampleBinding, BaseViewModel>() {
    override fun inflate() = FragmentSdkExoVideoPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
    }
}