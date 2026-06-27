package app.allever.android.sample.imageloader.core

import android.graphics.Color
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.imageloader.core.internal.DefaultLoader
import app.allever.android.lib.imageloader.core.ImageLoaderCore
import app.allever.android.lib.imageloader.core.load
import app.allever.android.lib.imageloader.core.loadBlur
import app.allever.android.lib.imageloader.core.loadCircle
import app.allever.android.lib.imageloader.core.loadRound
import app.allever.android.lib.imageloader.engine.coil.CoilLoader
import app.allever.android.lib.imageloader.engine.glide.GlideLoader
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.imageloader.core.databinding.FragmentBasicLoaderBinding

class BasicImageLoaderFragment: BaseFragment<FragmentBasicLoaderBinding, BaseViewModel>() {

    companion object {
        const val TEST_IMAGE_URL = "https://pic.rmb.bdstatic.com/bjh/bc11588b3af3/250605/807791a8084b32f96246b09d52011f8e.png"
    }

    private var mediaBean: MediaItem? = null

    private val imageLauncher = MediaPickerCore.registerPickerLauncher( this) {
        if (it.isEmpty()) {
            toast("未选择图片")
            return@registerPickerLauncher
        }

        val image = it[0]
        mediaBean = image
        mBinding.ivFile.load(image.path)
        mBinding.ivUri.load(image.uri)
    }

    override fun inflate(): FragmentBasicLoaderBinding = FragmentBasicLoaderBinding.inflate(layoutInflater)

    override fun init() {
        ImageLoaderCore.init(requireContext(), GlideLoader, ImageLoaderCore.Builder.create())
        mBinding.btnGlide.setOnClickListener {
            ImageLoaderCore.init(requireContext(), GlideLoader, ImageLoaderCore.Builder.create())
            loadAllImage()
        }
        mBinding.btnCoil.setOnClickListener {
            ImageLoaderCore.init(requireContext(), CoilLoader, ImageLoaderCore.Builder.create())
            loadAllImage()
        }
//        mBinding.btnDefault.setOnClickListener {
//            ImageLoaderCore.init(requireContext(), DefaultLoader, ImageLoaderCore.Builder.create())
//            loadAllImage()
//        }

        mBinding.btnSelectFile.setOnClickListener {
            MediaPickerCore.launchImage(imageLauncher)
        }


        loadAllImage()
    }

    fun loadAllImage() {
        mBinding.apply {
            // 本地资源
            ivLocal.load(R.drawable.ima_01)
            // 网络资源
            ivNet.load(TEST_IMAGE_URL)
            // 文件资源
            mediaBean?.let {
                ivFile.load(it.path)
                ivUri.load(it.uri)
            }
            // 圆角
            ivNetRound.loadRound(TEST_IMAGE_URL, 20f)
            // 圆形
            ivNetCircle.loadCircle(TEST_IMAGE_URL, 20, Color.parseColor("#000000"))
            // 模糊
            ivBlur.loadBlur(TEST_IMAGE_URL, 20f)
            //gif
//            ivGif.loadGif(R.drawable.gif_01)

        }
    }
}