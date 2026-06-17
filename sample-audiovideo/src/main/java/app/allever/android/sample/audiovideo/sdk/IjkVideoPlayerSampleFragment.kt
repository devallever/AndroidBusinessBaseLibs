package app.allever.android.sample.audiovideo.sdk

import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentSdkIjkVideoPlayerSampleBinding

class IjkVideoPlayerSampleFragment :
    BaseFragment<FragmentSdkIjkVideoPlayerSampleBinding, BaseViewModel>() {
    override fun inflate() = FragmentSdkIjkVideoPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
    }
}