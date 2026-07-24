package app.allever.android.sample.camera.core

import android.content.Intent
import android.net.Uri
import app.allever.android.lib.camera.core.CameraConfig
import app.allever.android.lib.camera.core.CameraCore
import app.allever.android.lib.camera.core.CameraFacing
import app.allever.android.lib.camera.core.ResultCallback
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.imageloader.core.load
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.camera.core.databinding.FragmentCameraEngineBinding
import java.io.File

class CameraEngineSampleFragment : BaseMvvmFragment<FragmentCameraEngineBinding, CameraEngineViewModel>() {

    private var lastPhotoFile: File? = null
    private var lastVideoFile: File? = null

    override fun inflate() = FragmentCameraEngineBinding.inflate(layoutInflater)

    override fun init() {
        val config = CameraConfig.Builder()
            .setCameraFacing(CameraFacing.FACE_BACK)
            .build()
        CameraCore.setConfig(config)

        mBinding.btnOpenCamera.setOnClickListener {
            CameraCore.openCamera(CameraFacing.FACE_BACK)
        }

        mBinding.btnOpenFrontCamera.setOnClickListener {
            CameraCore.openCamera(CameraFacing.FACE_FRONT)
        }

        mBinding.btnCloseCamera.setOnClickListener {
            CameraCore.closeCamera()
        }

        mBinding.btnSwitchCamera.setOnClickListener {
            CameraCore.switchCamera()
        }

        mBinding.btnTackPicture.setOnClickListener {
            takePicture()
        }

        mBinding.btnStartRecord.setOnClickListener {
            startRecordVideo()
        }

        mBinding.btnStopRecord.setOnClickListener {
            stopRecordVideo()
        }

        mBinding.btnPlayVideo.setOnClickListener {
            playVideo()
        }

        mBinding.btnStopVideo.setOnClickListener {
            stopVideo()
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.surfaceView.post {
            CameraCore.bindPreview(mBinding.surfaceView)
        }
    }

    private fun takePicture() {
        CameraCore.takePicture(object : ResultCallback {
            override fun onSuccess(file: File) {
                lastPhotoFile = file
                toast("拍照成功: ${file.absolutePath}")
                mBinding.ivPreviewPic.load(file.absolutePath)
            }

            override fun onFailure(message: String) {
                toast("拍照失败: $message")
            }
        })
    }

    private fun startRecordVideo() {
        CameraCore.startRecordVideo(object : ResultCallback {
            override fun onSuccess(file: File) {
                lastVideoFile = file
                toast("录像保存成功: ${file.absolutePath}")
            }

            override fun onFailure(message: String) {
                toast("录像失败: $message")
            }
        })
        toast("开始录制视频")
    }

    private fun stopRecordVideo() {
        CameraCore.stopRecordVideo()
        toast("停止录制视频")
    }

    private fun playVideo() {
        lastVideoFile?.let { file ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(file), "video/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context?.startActivity(intent)
        } ?: toast("没有录制的视频")
    }

    private fun stopVideo() {
        toast("视频播放已停止")
    }

    override fun onDestroy() {
        super.onDestroy()
        CameraCore.release()
    }
}

class CameraEngineViewModel : BaseViewModel() {
    override fun init() {}
}