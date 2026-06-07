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
 * 基础加载演示
 *
 * 展示多种数据源的图片加载：
 * - 网络图片 (URL)
 * - 资源文件 (ResId)
 * - 占位图 / 错误图
 */
class BasicLoadFragment : BaseFragment<FragmentListBinding, BaseViewModel>() {

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

        // 标题
        container.addTitle("基础加载")

        // 1. URL 加载
        container.addSection("1. 网络图片 (URL)")
        val ivUrl = container.addImage(200.dp, 200.dp)
        ivUrl.load(TEST_IMAGE_URL) {
            placeholder(android.R.drawable.ic_menu_report_image)
            error(android.R.drawable.ic_dialog_alert)
        }

        // 2. ResId 加载
        container.addSection("2. 资源文件 (ResId)")
        val ivResId = container.addImage(160.dp, 160.dp)
        ivResId.load(android.R.drawable.ic_menu_gallery)

        // 3. 带占位图的 URL 加载
        container.addSection("3. 带占位图")
        val ivPlaceholder = container.addImage(200.dp, 120.dp)
        ivPlaceholder.load(TEST_IMAGE_URL) {
            placeholder(android.R.drawable.ic_menu_upload_you_tube)
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

    companion object {
        /** 示例网络图片 URL（可替换为任意有效图片地址） */
        const val TEST_IMAGE_URL = "https://picsum.photos/400/300"
    }
}
