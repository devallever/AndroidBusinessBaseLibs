package app.allever.android.learning.audiovideo

import android.content.Intent
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.ActivitySelectMediaBinding

class SelectMediaActivity : BaseActivity<ActivitySelectMediaBinding, SelectMediaViewModel>() {
    override fun init() {
        initTopBar("选择视频")
        mViewModel.initExtra(intent)
        /*
        binding.btnSelectMedia.setOnClickListener {
            MediaPicker.launchPickerActivity(
                MediaHelper.TYPE_VIDEO,
                mediaPickerListener = object : MediaPickerListener {
                    override fun onPicked(
                        all: MutableList<MediaBean>,
                        imageList: MutableList<MediaBean>,
                        videoList: MutableList<MediaBean>,
                        audioList: MutableList<MediaBean>
                    ) {
                        log("path = ${videoList[0]}")
                        if (videoList.isNotEmpty()) {
                            videoList[0].date
                            when (mViewModel.type) {
                                0 -> {
                                    ActivityHelper.startActivity<VideoViewPlayerActivity> {
                                        putExtra("MEDIA_BEAN", videoList[0])
                                    }
                                }
                                1 -> {
                                    ActivityHelper.startActivity<TextureViewPlayerActivity> {
                                        putExtra("MEDIA_BEAN", videoList[0])
                                    }
                                }

                                2 -> {
                                    ActivityHelper.startActivity<SurfaceViewPlayerActivity> {
                                        putExtra("MEDIA_BEAN", videoList[0])
                                    }
                                }
                            }

                        }
                    }

                })
        }

         */
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