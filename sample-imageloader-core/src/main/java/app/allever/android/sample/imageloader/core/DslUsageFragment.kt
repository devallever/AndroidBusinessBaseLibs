package app.allever.android.sample.imageloader.core

import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.imageloader.core.databinding.FragmentImageLoaderDslBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.imageloader.core.ext.load

/**
 * DSL 用法演示
 *
 * 展示 ImageView 扩展函数的简洁 API，
 * 这是日常开发中最推荐的用法。
 *
 * 所有示例均使用 imageView.load(source) { ... } DSL 风格。
 */
class DslUsageFragment : BaseFragment<FragmentImageLoaderDslBinding, BaseViewModel>() {

    override fun inflate() = FragmentImageLoaderDslBinding.inflate(layoutInflater)

    override fun init() {
        val url = BasicLoadFragment.TEST_IMAGE_URL

        // ===== 最简用法 =====
        mBinding.ivSimple.load(url)

        // ===== 带占位图和错误图 =====
        mBinding.ivPlaceholderError.load(url) {
            placeholder(android.R.drawable.ic_menu_upload_you_tube)
            error(android.R.drawable.ic_dialog_alert)
        }

        // ===== 圆角 + 占位图 =====
        mBinding.ivRounded.load(url) {
            roundedCorners(16f)
            placeholder(android.R.drawable.ic_menu_report_image)
        }

        // ===== 圆形头像 =====
        mBinding.ivCircleAvatar.load(url) { circle() }

        // ===== 高斯模糊背景 =====
        mBinding.ivBlurBg.load(url) { blur(15) }

        // ===== 组合效果：圆角 + 灰度化 =====
        mBinding.ivCombined.load(url) {
            roundedCorners(24f)
            grayscale()
        }

        // ===== ResId 加载 =====
        mBinding.ivResId.load(android.R.drawable.ic_menu_gallery)
    }
}
