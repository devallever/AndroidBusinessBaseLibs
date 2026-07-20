package app.allever.android.sample.camera.core

import android.graphics.Bitmap
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.camera.proxy.camerax.CameraXProxyImpl
import app.allever.android.lib.core.camera.proxy.CameraFacing
import app.allever.android.lib.core.camera.proxy.CameraListener
import app.allever.android.lib.core.camera.proxy.CameraProxyManager
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.imageloader.core.load
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.camera.core.databinding.FragmentCameraXBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraXFragment : BaseMvvmFragment<FragmentCameraXBinding, CameraXViewModel>() {

    override fun inflate() = FragmentCameraXBinding.inflate(layoutInflater)

    override fun init() {
        CameraProxyManager.injectProxy(CameraXProxyImpl())
        mBinding.btnOpenFrontCamera.setOnClickListener {
            CameraProxyManager.openCamera(CameraFacing.Companion.FACE_BACK)
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
            CameraProxyManager.setLifeCycleOwner(this)
        }

        CameraProxyManager.setCameraListener(object : CameraListener {
            override fun onPreview(data: ByteArray, imageFormat: Int) {

            }

            override fun onTakePicture(data: ByteArray?, bitmap: Bitmap?, imageFormat: Int) {
                lifecycleScope.launch(Dispatchers.IO) {
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

class CameraXViewModel() : BaseViewModel() {
    override fun init() {

    }
}