package app.allever.android.sample.camera.core

import android.annotation.SuppressLint
import android.os.Environment
import app.allever.android.lib.camera.proxy.camerax.CameraXManager
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.camera.Camera1Manager
import app.allever.android.lib.core.camera.CameraResultCallback
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.util.TimeUtils
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.camera.core.databinding.FragmentCameraCoreBinding
import java.io.File
import kotlin.getValue

class CameraCoreFragment : BaseFragment<FragmentCameraCoreBinding, BaseViewModel>() {

    private val TAG = "CameraCoreFragment"

    private val mCameraManager by lazy {
        val engine = arguments?.getString("engine")
        when (engine) {
            "camera" -> Camera1Manager(requireContext(),  mBinding.previewContainer)
            "camera2" -> Camera1Manager(requireContext(),  mBinding.previewContainer)
            else -> CameraXManager(requireContext(), this, mBinding.previewContainer)
        }
        CameraXManager(requireContext(), this, mBinding.previewContainer)
    }

    override fun inflate() = FragmentCameraCoreBinding.inflate(layoutInflater)
    @SuppressLint("MissingPermission")
    override fun init() {
        mBinding.apply {
            btnOpen.setOnClickListener {
                mCameraManager.openCamera()
            }
            btnClose.setOnClickListener {
                mCameraManager.closeCamera()
            }
            btnSwitchCamera.setOnClickListener {
                mCameraManager.switchCamera()
            }
            btnCapture.setOnClickListener {
                mCameraManager.takePhoto(
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "IMG_${
                            TimeUtils.formatTime(
                                System.currentTimeMillis(),
                                "yyyyMMddHHmmss"
                            )
                        }.jpg"
                    ), object : CameraResultCallback {
                        override fun onSuccess(file: File) {
                            //log
                            log(TAG, "onSuccess: ${file.absolutePath}")
                            toast("保存成功: ${file.absolutePath}")
                        }

                        override fun onError(message: String) {
                            logE(TAG, "onError: $message")
                            toast("保存失败: $message")
                        }
                    })
            }
            btnRecordVideo.setOnClickListener {
                mCameraManager.startRecording(
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "VID_${
                            TimeUtils.formatTime(
                                System.currentTimeMillis(),
                                "yyyyMMddHHmmss"
                            )
                        }.mp4"
                    ), object : CameraResultCallback {
                        override fun onSuccess(file: File) {
                            //log
                            log(TAG, "onSuccess: ${file.absolutePath}")
                            toast("保存成功: ${file.absolutePath}")
                        }

                        override fun onError(message: String) {
                            logE(TAG, "onError: $message")
                            toast("保存失败: $message")
                        }
                    }
                )
            }
            btnStopRecordVideo.setOnClickListener {
                mCameraManager.stopRecording()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCameraManager.closeCamera()
    }
}