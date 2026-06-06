package app.allever.android.learning.audiovideo

import android.content.Intent
import app.allever.android.learning.audiovideo.surfaceviewplayer.SurfaceViewPlayerActivity
import app.allever.android.learning.audiovideo.textureviewplayer.TextureViewPlayerActivity
import app.allever.android.learning.audiovideo.videoviewplayer.VideoViewPlayerActivity
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.MediaPickerContract
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.ActivitySelectMediaBinding

class SelectMediaActivity : BaseActivity<ActivitySelectMediaBinding, SelectMediaViewModel>() {
    private val launcher = registerForActivityResult(
        MediaPickerContract()
    ) { items ->
        if (items.isEmpty()) {
            toast("未选择任何资源")
            return@registerForActivityResult
        }
        items.firstOrNull()?.let { item ->
            when (item) {
                is MediaItem.Video -> {
                    when (mViewModel.type) {
                        0 -> {
                            ActivityHelper.startActivity<VideoViewPlayerActivity> {
                                putExtra("MEDIA_BEAN", item)
                            }
                        }

                        1 -> {
                            ActivityHelper.startActivity<TextureViewPlayerActivity> {
                                putExtra("MEDIA_BEAN", item)
                            }
                        }

                        2 -> {
                            ActivityHelper.startActivity<SurfaceViewPlayerActivity> {
                                putExtra("MEDIA_BEAN", item)
                            }
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
            launcher.launch(
                MediaPickerConfig(
                    types = setOf(MediaType.Type.VIDEO),
                    maxSelect = 1,
                )
            )
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