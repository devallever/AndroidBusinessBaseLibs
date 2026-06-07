package app.allever.android.sample.imageloader.core

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.common.databinding.FragmentListBinding
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
class TransformationFragment : BaseFragment<FragmentListBinding, BaseViewModel>() {

    override fun inflate() = FragmentListBinding.inflate(layoutInflater)

    override fun init() {
        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(16.dp, 16.dp, 16.dp, 32.dp)
        }
        scrollView.addView(container)

        container.addTitle("变换效果")

        // 1. 原图
        container.addSection("原图")
        container.addImage(160.dp, 160.dp).load(BasicLoadFragment.TEST_IMAGE_URL)

        // 2. 圆角
        container.addSection("圆角 (radius=24dp)")
        container.addImage(160.dp, 160.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            roundedCorners(24.dp.toFloat())
        }

        // 3. 大圆角
        container.addSection("大圆角 (radius=40dp)")
        container.addImage(160.dp, 160.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            roundedCorners(40.dp.toFloat())
        }

        // 4. 圆形
        container.addSection("圆形")
        container.addImage(140.dp, 140.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            circle()
        }

        // 5. 高斯模糊
        container.addSection("高斯模糊 (radius=15)")
        container.addImage(160.dp, 100.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            blur(15)
        }

        // 6. 高斯模糊 (强)
        container.addSection("高斯模糊 (radius=25)")
        container.addImage(160.dp, 100.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            blur(25)
        }

        // 7. 灰度化
        container.addSection("灰度化")
        container.addImage(160.dp, 100.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            grayscale()
        }

        // 8. 组合：圆角 + 模糊
        container.addSection("组合：圆角 + 模糊")
        container.addImage(160.dp, 100.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            roundedCorners(16.dp.toFloat())
            blur(10)
        }

        mBinding.root.removeAllViews()
        mBinding.root.addView(scrollView)
    }

    /** dp 转 px */
    private val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            requireContext().resources.displayMetrics
        ).toInt()

    private fun LinearLayout.addTitle(text: String) {
        addView(TextView(context).apply {
            this.text = text
            textSize = 20f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 12.dp)
        })
    }

    private fun LinearLayout.addSection(text: String) {
        addView(TextView(context).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 16.dp, 0, 8.dp)
        })
    }

    private fun LinearLayout.addImage(w: Int, h: Int): ImageView {
        return ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(w, h).apply {
                topMargin = 8.dp
                bottomMargin = 8.dp
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#EEEEEE"))
        }.also { addView(it) }
    }
}
