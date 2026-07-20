package app.allever.android.sample.camera.core

import android.graphics.Bitmap
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.camera.proxy.camera2.Camera2ProxyImpl
import app.allever.android.lib.core.camera.proxy.CameraListener
import app.allever.android.lib.core.camera.proxy.CameraProxyManager
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.imageloader.core.load
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.camera.core.databinding.FragmentCamera2Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Camera2Fragment : BaseMvvmFragment<FragmentCamera2Binding, Camera2ViewModel>() {

    override fun inflate() = FragmentCamera2Binding.inflate(layoutInflater)

    override fun init() {
        CameraProxyManager.injectProxy(Camera2ProxyImpl())
        mBinding.btnOpenFrontCamera.setOnClickListener {
            CameraProxyManager.openCamera(1)
        }

        mBinding.btnOpenCamera.setOnClickListener {
            CameraProxyManager.openCamera()
        }

        mBinding.btnCloseCamera.setOnClickListener {
            CameraProxyManager.closeCamera()
        }

        mBinding.btnTackPicture.setOnClickListener {
            CameraProxyManager.takePicture()
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.surfaceView.post {
            CameraProxyManager.setPreview(mBinding.surfaceView)
        }

        CameraProxyManager.setCameraListener(object : CameraListener {
            override fun onPreview(data: ByteArray, imageFormat: Int) {
            }

            override fun onTakePicture(data: ByteArray?, bitmap: Bitmap?, imageFormat: Int) {
                lifecycleScope.launch(Dispatchers.IO) {
//                    val path =
                    val result = CameraProxyManager.saveBitmap2File(
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
        CameraProxyManager.closeCamera()
        CameraProxyManager.release()
    }
}

class Camera2ViewModel() : BaseViewModel() {
    override fun init() {

    }
}