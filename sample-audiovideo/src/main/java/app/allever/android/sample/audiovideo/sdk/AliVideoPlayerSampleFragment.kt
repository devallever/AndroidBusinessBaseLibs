package app.allever.android.sample.audiovideo.sdk

import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentSdkAliVideoPlayerSampleBinding

class AliVideoPlayerSampleFragment :
    BaseFragment<FragmentSdkAliVideoPlayerSampleBinding, BaseViewModel>() {
    override fun inflate() = FragmentSdkAliVideoPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
    }
}