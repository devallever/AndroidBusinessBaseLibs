package z.app.allever.android.learning.audiovideo

import android.content.Intent
import z.app.allever.android.learning.audiovideo.videoplayer.surfaceviewplayer.SurfaceViewPlayerActivity
import z.app.allever.android.learning.audiovideo.videoplayer.textureviewplayer.TextureViewPlayerActivity
import z.app.allever.android.learning.audiovideo.videoplayer.videoviewplayer.VideoViewPlayerActivity
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import z.app.allever.android.sample.audiovideo.databinding.ActivitySelectMediaBinding

class SelectMediaActivity : BaseActivity<ActivitySelectMediaBinding, SelectMediaViewModel>() {

    val videoPickerLauncher = MediaPickerCore.registerPickerLauncher( this) {items ->
        if (items.isEmpty()) {
            toast("请选择视频文件")
            return@registerPickerLauncher
        }
        items.firstOrNull()?.let { item ->
            when (item) {
                is MediaItem.Video -> {
                    when (mViewModel.type) {
                        0 -> {
                            ActivityHelper.startActivity<VideoViewPlayerActivity> {
                                putExtra("MEDIA_BEAN", item)
                                finish()
                            }
                        }

                        1 -> {
                            ActivityHelper.startActivity<TextureViewPlayerActivity> {
                                putExtra("MEDIA_BEAN", item)
                            }
                            finish()
                        }

                        2 -> {
                            ActivityHelper.startActivity<SurfaceViewPlayerActivity> {
                                putExtra("MEDIA_BEAN", item)
                            }
                            finish()
                        }
                    }
                }

                else -> toast("请选择视频文件")
            }
        }
    }
    override fun init() {
        initTopBar("选择视频")
        mViewModel.initExtra(intent)

        binding.btnSelectMedia.setOnClickListener {
            MediaPickerCore.launchVideo(videoPickerLauncher)
        }

    }

    override fun inflateChildBinding() = ActivitySelectMediaBinding.inflate(layoutInflater)

}

class SelectMediaViewModel : BaseViewModel() {
    var type: Int = 0
    override fun init() {

    }

    fun initExtra(intent: Intent?) {
        type = intent?.getIntExtra("TYPE", 0) ?: 0
    }
}