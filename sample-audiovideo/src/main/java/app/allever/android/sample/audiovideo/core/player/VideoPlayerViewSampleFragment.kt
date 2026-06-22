package app.allever.android.sample.audiovideo.core.player

import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentVideoPlayerViewSampleBinding

class VideoPlayerViewSampleFragment :
    BaseFragment<FragmentVideoPlayerViewSampleBinding, BaseViewModel>() {

    override fun inflate(): FragmentVideoPlayerViewSampleBinding =
        FragmentVideoPlayerViewSampleBinding.inflate(layoutInflater)

    override fun init() {
    }
}
