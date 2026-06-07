package app.allever.android.sample.imageloader.core

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.imageloader.core.ext.ImageLoaderConfig
import app.allever.android.lib.imageloader.core.ext.load
import app.allever.android.lib.imageloader.core.request.ImageLoader
import app.allever.android.lib.imageloader.core.request.ImageRequest
import app.allever.android.lib.imageloader.core.source.ImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 高级用法演示
 *
 * 展示：
 * - ImageRequest Builder 完整用法
 * - 回调模式获取 Bitmap
 * - 加载事件监听器 (ImageListener)
 * - 缓存策略配置
 * - 手动初始化 ImageLoaderConfig
 */
class AdvancedFragment : BaseFragment<FragmentListBinding, BaseViewModel>() {

    override fun inflate() = FragmentListBinding.inflate(layoutInflater)

    override fun init() {
        // 初始化 ImageLoader（实际项目中在 Application.onCreate 调用）
        try {
            ImageLoaderConfig.init(requireContext()) {
                memoryCacheSize(50 * 1024 * 1024)   // 50MB 内存缓存
                threadPoolSize(3)
            }
        } catch (_: Exception) {
            // 已初始化则忽略
        }

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

        container.addTitle("高级用法")

        // 1. Builder 模式 — 缓存策略 NONE
        container.addSection("1. 不使用缓存 (CachePolicy.NONE)")
        val ivNoCache = container.addImage(180.dp, 120.dp)
        ImageLoader.getInstance().load(
            ImageRequest.Builder(ImageSource.Url(BasicLoadFragment.TEST_IMAGE_URL))
                .into(ivNoCache)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .cache(ImageRequest.CachePolicy.NONE)
                .build()
        )

        // 2. 仅内存缓存
        container.addSection("2. 仅内存缓存 (MEMORY_ONLY)")
        val ivMemOnly = container.addImage(180.dp, 120.dp)
        ImageLoader.getInstance().load(
            ImageRequest.Builder(ImageSource.Url(BasicLoadFragment.TEST_IMAGE_URL))
                .into(ivMemOnly)
                .cache(ImageRequest.CachePolicy.MEMORY_ONLY)
                .build()
        )

        // 3. 带监听器的加载
        container.addSection("3. 带事件监听器")
        val ivListener = container.addImage(180.dp, 120.dp)
        ImageLoader.getInstance().load(
            ImageRequest.Builder(ImageSource.Url(BasicLoadFragment.TEST_IMAGE_URL))
                .into(ivListener)
                .listener(object : app.allever.android.lib.imageloader.core.request.ImageListener {
                    override fun onStart() {
                        android.util.Log.d(TAG, "onStart: 开始加载")
                    }

                    override fun onSuccess(bitmap: android.graphics.Bitmap) {
                        android.util.Log.d(TAG, "onSuccess: 加载成功 ${bitmap.width}x${bitmap.height}")
                    }

                    override fun onError(error: Throwable) {
                        android.util.Log.e(TAG, "onError: ${error.message}")
                    }
                })
                .build()
        )

        // 4. 回调模式 — 通过 intoCallback 获取 Bitmap
        container.addSection("4. 回调模式 (intoCallback)")
        container.addView(TextView(context).apply {
            text = "点击下方按钮通过回调获取 Bitmap"
            textSize = 12f
            setTextColor(Color.GRAY)
            setPadding(0, 4.dp, 0, 8.dp)
        })

        val btnCallback = TextView(context).apply {
            text = "回调加载图片"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundResource(android.R.drawable.btn_default)
            setPadding(24.dp, 12.dp, 24.dp, 12.dp)
            setOnClickListener { loadViaCallback() }
        }
        container.addView(btnCallback)

        // 5. 取消加载演示
        container.addSection("5. 清除 / 取消")
        val ivCancel = container.addImage(180.dp, 120.dp)
        ivCancel.load(BasicLoadFragment.TEST_IMAGE_URL)

        val btnCancel = TextView(context).apply {
            text = "清除该图片"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundResource(android.R.drawable.btn_default)
            setPadding(24.dp, 12.dp, 24.dp, 12.dp)
            (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = 8.dp
            setOnClickListener {
                ivCancel.setImageDrawable(null)
                android.util.Log.d(TAG, "图片已清除")
            }
        }
        container.addView(btnCancel)

        mBinding.root.removeAllViews()
        mBinding.root.addView(scrollView)
    }

    /**
     * 通过回调模式加载图片，获取 Bitmap 后做进一步处理
     */
    private fun loadViaCallback() {
        ImageLoader.getInstance().load(
            ImageRequest.Builder(ImageSource.Url(BasicLoadFragment.TEST_IMAGE_URL))
                .intoCallback(
                    onSuccess = { bitmap ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) {
                                android.util.Log.d(TAG,
                                    "回调成功: ${bitmap.width}x${bitmap.height}, size=${bitmap.byteCount / 1024}KB")
                            }
                        }
                    },
                    onError = { error ->
                        android.util.Log.e(TAG, "回调失败: ${error.message}")
                    }
                ).build()
        )
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
        private const val TAG = "ImageLoaderSample"
    }
}
