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
 * DSL 用法演示
 *
 * 展示 ImageView 扩展函数的简洁 API，
 * 这是日常开发中最推荐的用法。
 *
 * 所有示例均使用 imageView.load(source) { ... } DSL 风格。
 */
class DslUsageFragment : BaseFragment<FragmentListBinding, BaseViewModel>() {

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
            setPadding(16.dp, 16.dp, 16.dp, 48.dp)
        }
        scrollView.addView(container)

        container.addTitle("DSL 用法示例")

        // ===== 最简用法 =====
        container.addSection("// 最简用法：一行代码加载网络图片")
        container.addCode("imageView.load(\"https://example.com/photo.jpg\")")
        container.addImage(220.dp, 140.dp).load(BasicLoadFragment.TEST_IMAGE_URL)

        // ===== 带占位图 =====
        container.addSection("// 带占位图和错误图")
        container.addCode("""imageView.load(url) {
    |    placeholder(R.drawable.loading)
    |    error(R.drawable.error)
    |}""".trimMargin())
        container.addImage(200.dp, 130.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            placeholder(android.R.drawable.ic_menu_upload_you_tube)
            error(android.R.drawable.ic_dialog_alert)
        }

        // ===== 圆角 =====
        container.addSection("// 圆角 + 占位图")
        container.addCode("""imageView.load(url) {
    |    roundedCorners(16f)
    |    placeholder(R.drawable.placeholder)
    |}""".trimMargin())
        container.addImage(180.dp, 120.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            roundedCorners(16.dp.toFloat())
            placeholder(android.R.drawable.ic_menu_report_image)
        }

        // ===== 圆形头像 =====
        container.addSection("// 圆形头像")
        container.addCode("""imageView.load(url) {
    |    circle()
    |    placeholder(R.drawable.avatar_placeholder)
    |}""".trimMargin())
        container.addImage(120.dp, 120.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            circle()
        }

        // ===== 高斯模糊背景 =====
        container.addSection("// 高斯模糊背景")
        container.addCode("""imageView.load(url) {
    |    blur(15)
    |}""".trimMargin())
        container.addImage(200.dp, 100.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            blur(15)
        }

        // ===== 组合效果 =====
        container.addSection("// 组合：圆角 + 灰度化")
        container.addCode("""imageView.load(url) {
    |    roundedCorners(24f)
    |    grayscale()
    |}""".trimMargin())
        container.addImage(180.dp, 110.dp).load(BasicLoadFragment.TEST_IMAGE_URL) {
            roundedCorners(24.dp.toFloat())
            grayscale()
        }

        // ===== ResId 加载 =====
        container.addSection("// 加载本地资源")
        container.addCode("imageView.load(R.drawable.icon)")
        container.addImage(100.dp, 100.dp).load(android.R.drawable.ic_menu_gallery)

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
            textSize = 13f
            setTextColor(Color.parseColor("#388E3C"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16.dp, 0, 6.dp)
        })
    }

    private fun LinearLayout.addCode(code: String) {
        addView(TextView(context).apply {
            text = code
            textSize = 11f
            setTextColor(Color.parseColor("#555555"))
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setPadding(12.dp, 8.dp, 12.dp, 8.dp)
            (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin = 8.dp
        })
    }

    private fun LinearLayout.addImage(w: Int, h: Int): ImageView {
        return ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(w, h).apply {
                topMargin = 4.dp
                bottomMargin = 12.dp
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#EEEEEE"))
        }.also { addView(it) }
    }
}
