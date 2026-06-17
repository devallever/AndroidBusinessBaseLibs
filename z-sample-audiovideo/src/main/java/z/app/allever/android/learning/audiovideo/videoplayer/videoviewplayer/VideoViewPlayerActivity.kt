package z.app.allever.android.learning.audiovideo.videoplayer.videoviewplayer

import android.content.Intent
import android.os.Build
import z.app.allever.android.sample.audiovideo.databinding.ActivityVideoViewPlayerBinding
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.mvvm.base.BaseViewModel

class VideoViewPlayerActivity :
    BaseActivity<ActivityVideoViewPlayerBinding, VideoViewPlayerViewModel>() {

    override fun inflateChildBinding() = ActivityVideoViewPlayerBinding.inflate(layoutInflater)

    override fun enableAdaptStatusBar() = false

    override fun init() {
        mViewModel.initExtra(intent)
        binding.videoPlayerView.setData(mViewModel.mediaBean ?: return)
    }

    override fun showTopBar() = false

    override fun isSupportSwipeBack() = false

}


class VideoViewPlayerViewModel : BaseViewModel() {
    var mediaBean: MediaItem? = null
    override fun init() {

    }

    fun initExtra(intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaBean = intent?.getParcelableExtra("MEDIA_BEAN", MediaItem::class.java)
        } else {
            mediaBean = intent?.getParcelableExtra("MEDIA_BEAN")
        }
    }
}