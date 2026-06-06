package app.allever.android.learning.audiovideo.videoplayer.surfaceviewplayer

import android.content.Intent
import app.allever.android.sample.audiovideo.databinding.ActivitySurfaceViewPlayerBinding
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.mvvm.base.BaseViewModel

class SurfaceViewPlayerActivity :
    BaseActivity<ActivitySurfaceViewPlayerBinding, SurfaceViewPlayerViewModel>() {
    override fun init() {
        mViewModel.initExtra(intent)
        binding.videoPlayerView.setData(mViewModel.mediaBean ?: return)
    }

    override fun inflateChildBinding() = ActivitySurfaceViewPlayerBinding.inflate(layoutInflater)

    override fun showTopBar() = false

    override fun isSupportSwipeBack() = false
}

class SurfaceViewPlayerViewModel : BaseViewModel() {
    var mediaBean: MediaItem? = null
    override fun init() {
    }

    fun initExtra(intent: Intent?) {
        mediaBean = intent?.getParcelableExtra("MEDIA_BEAN") ?: return
    }
}