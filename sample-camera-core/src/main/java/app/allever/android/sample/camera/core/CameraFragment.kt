package app.allever.android.sample.camera.core

import android.graphics.Bitmap
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.core.camera.CameraListener
import app.allever.android.lib.core.camera.CameraManager
import app.allever.android.lib.core.camera.CameraProxyImpl
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.imageloader.core.load
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.camera.core.databinding.FragmentCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CameraFragment : BaseMvvmFragment<FragmentCameraBinding, CameraViewModel>() {

    override fun inflate() = FragmentCameraBinding.inflate(layoutInflater)

    override fun init() {
        //inject
        CameraManager.injectProxy(CameraProxyImpl())
        mBinding.btnOpenFrontCamera.setOnClickListener {
            CameraManager.openCamera(1)
        }

        mBinding.btnOpenCamera.setOnClickListener {
            CameraManager.openCamera()
        }

        mBinding.btnCloseCamera.setOnClickListener {
            CameraManager.closeCamera()
        }

        mBinding.btnTackPicture.setOnClickListener {
            CameraManager.takePicture()
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.surfaceView.post {
            CameraManager.setPreview(mBinding.surfaceView)
        }
        CameraManager.setCameraListener(object : CameraListener {
            override fun onPreview(data: ByteArray, imageFormat: Int) {
            }

            override fun onTakePicture(data: ByteArray?, bitmap: Bitmap?, imageFormat: Int) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = CameraManager.saveBitmap2File(
                        bitmap
                    )
                    val msg = if (result?.isNotEmpty() == true) {
                        launch(Dispatchers.Main) {
                            mBinding.ivPreviewPic.load(result)
                        }
                        "保存成功："
                    } else {
                        "保存失败"
                    }
                    toast(msg)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        CameraManager.closeCamera()
        CameraManager.release()
    }
}

class CameraViewModel() : BaseViewModel() {
    override fun init() {

    }
}