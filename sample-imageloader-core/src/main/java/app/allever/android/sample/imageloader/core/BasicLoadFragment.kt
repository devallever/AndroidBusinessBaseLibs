package app.allever.android.sample.imageloader.core

import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toast
import app.allever.android.sample.imageloader.core.databinding.FragmentImageLoaderBasicBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.imageloader.core.ext.load
import app.allever.android.lib.media.picker.MediaPickerCore

/**
 * 基础加载演示
 *
 * 展示多种数据源的图片加载：
 * - 网络图片 (URL)
 * - 资源文件 (ResId)
 * - 占位图 / 错误图
 */
class BasicLoadFragment : BaseFragment<FragmentImageLoaderBasicBinding, BaseViewModel>() {

    private val imageLauncher = MediaPickerCore.registerPickerLauncher(this) {
        if (it.isEmpty()) {
            toast("请选择图片")
            return@registerPickerLauncher
        }
        // 4. File path 加载
        mBinding.ivFile.load(it[0].path) {
        }
        // 5. uri 加载
        mBinding.ivUri.load(it[0].uri) {}
    }

    override fun inflate() = FragmentImageLoaderBasicBinding.inflate(layoutInflater)

    override fun init() {
        // 1. URL 加载（带占位图 + 错误图）
        mBinding.ivUrl.load(TEST_IMAGE_URL) {
            placeholder(android.R.drawable.ic_menu_report_image)
            error(android.R.drawable.ic_dialog_alert)
        }

        // 2. ResId 加载
        mBinding.ivResId.load(R.drawable.ima_01)

        // 3. 带占位图的 URL 加载
        mBinding.ivPlaceholder.load(TEST_IMAGE_URL) {
            placeholder(android.R.drawable.ic_menu_upload_you_tube)
        }

        mBinding.btnSelectPhoto.setOnClickListener {
            MediaPickerCore.launchImage(imageLauncher)
        }
    }

    companion object {
        /** 示例网络图片 URL */
        const val TEST_IMAGE_URL = "https://pic.rmb.bdstatic.com/bjh/bc11588b3af3/250605/807791a8084b32f96246b09d52011f8e.png"
    }
}
