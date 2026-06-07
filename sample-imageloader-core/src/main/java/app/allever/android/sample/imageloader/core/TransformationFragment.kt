package app.allever.android.sample.imageloader.core

import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.imageloader.core.databinding.FragmentImageLoaderTransformationBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.imageloader.core.ext.load

/**
 * 变换效果演示
 *
 * 展示内置的图片变换效果：
 * - 圆角 (RoundedCorners)
 * - 圆形裁切 (CircleTransformation)
 * - 高斯模糊 (BlurTransformation)
 * - 灰度化 (GrayscaleTransformation)
 * - 组合变换
 */
class TransformationFragment : BaseFragment<FragmentImageLoaderTransformationBinding, BaseViewModel>() {

    override fun inflate() = FragmentImageLoaderTransformationBinding.inflate(layoutInflater)

    override fun init() {
        val url = BasicLoadFragment.TEST_IMAGE_URL

        // 1. 原图
        mBinding.ivOriginal.load(url)

        // 2. 圆角 (24dp)
        mBinding.ivRounded24.load(url) { roundedCorners(24f) }

        // 3. 大圆角 (40dp)
        mBinding.ivRounded40.load(url) { roundedCorners(40f) }

        // 4. 圆形
        mBinding.ivCircle.load(url) { circle() }

        // 5. 高斯模糊 (radius=15)
        mBinding.ivBlur15.load(url) { blur(15) }

        // 6. 强模糊 (radius=25)
        mBinding.ivBlur25.load(url) { blur(25) }

        // 7. 灰度化
        mBinding.ivGrayscale.load(url) { grayscale() }

        // 8. 组合：圆角 + 模糊
        mBinding.ivCombined.load(url) {
            roundedCorners(16f)
            blur(10)
        }
    }
}
