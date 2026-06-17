package app.allever.android.sample.audiovideo.sdk

import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentSdkJzVideoPlayerSampleBinding

class JzVideoPlayerSampleFragment :
    BaseFragment<FragmentSdkJzVideoPlayerSampleBinding, BaseViewModel>() {
    override fun inflate() = FragmentSdkJzVideoPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
    }
}