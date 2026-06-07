package app.allever.android.sample.imageloader.core

import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.imageloader.core.databinding.FragmentImageLoaderBasicBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.imageloader.core.ext.load

/**
 * 基础加载演示
 *
 * 展示多种数据源的图片加载：
 * - 网络图片 (URL)
 * - 资源文件 (ResId)
 * - 占位图 / 错误图
 */
class BasicLoadFragment : BaseFragment<FragmentImageLoaderBasicBinding, BaseViewModel>() {

    override fun inflate() = FragmentImageLoaderBasicBinding.inflate(layoutInflater)

    override fun init() {
        // 1. URL 加载（带占位图 + 错误图）
        mBinding.ivUrl.load(TEST_IMAGE_URL) {
            placeholder(android.R.drawable.ic_menu_report_image)
            error(android.R.drawable.ic_dialog_alert)
        }

        // 2. ResId 加载
        mBinding.ivResId.load(android.R.drawable.ic_menu_gallery)

        // 3. 带占位图的 URL 加载
        mBinding.ivPlaceholder.load(TEST_IMAGE_URL) {
            placeholder(android.R.drawable.ic_menu_upload_you_tube)
        }
    }

    companion object {
        /** 示例网络图片 URL */
        const val TEST_IMAGE_URL = "https://picsum.photos/400/300"
    }
}
