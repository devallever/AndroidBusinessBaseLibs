package app.allever.android.sample.imageloader.core

import android.util.Log
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.imageloader.core.databinding.FragmentImageLoaderAdvancedBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.imageloader.core.ext.ImageLoaderConfig
import app.allever.android.lib.imageloader.core.ext.load
import app.allever.android.lib.imageloader.core.request.ImageListener
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
class AdvancedFragment : BaseFragment<FragmentImageLoaderAdvancedBinding, BaseViewModel>() {

    override fun inflate() = FragmentImageLoaderAdvancedBinding.inflate(layoutInflater)

    override fun init() {
        // 初始化 ImageLoader（实际项目中在 Application.onCreate 调用）
        try {
            ImageLoaderConfig.init(requireContext()) {
                memoryCacheSize(50 * 1024 * 1024)
                threadPoolSize(3)
            }
        } catch (_: Exception) {
            // 已初始化则忽略
        }

        val url = BasicLoadFragment.TEST_IMAGE_URL

        // 1. Builder 模式 — 缓存策略 NONE
        ImageLoader.getInstance().load(
            ImageRequest.Builder(ImageSource.Url(url))
                .into(mBinding.ivNoCache)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .cache(ImageRequest.CachePolicy.NONE)
                .build()
        )

        // 2. 仅内存缓存
        ImageLoader.getInstance().load(
            ImageRequest.Builder(ImageSource.Url(url))
                .into(mBinding.ivMemOnly)
                .cache(ImageRequest.CachePolicy.MEMORY_ONLY)
                .build()
        )

        // 3. 带监听器的加载
        ImageLoader.getInstance().load(
            ImageRequest.Builder(ImageSource.Url(url))
                .into(mBinding.ivListener)
                .listener(object : ImageListener {
                    override fun onStart() { Log.d(TAG, "onStart: 开始加载") }
                    override fun onSuccess(bitmap: android.graphics.Bitmap) { Log.d(TAG, "onSuccess: ${bitmap.width}x${bitmap.height}") }
                    override fun onError(error: Throwable) { Log.e(TAG, "onError: ${error.message}") }
                })
                .build()
        )

        // 4. 回调模式
        mBinding.btnCallback.setOnClickListener { loadViaCallback() }

        // 5. 清除 / 取消
        mBinding.ivCancel.load(url)
        mBinding.btnClear.setOnClickListener {
            mBinding.ivCancel.setImageDrawable(null)
            Log.d(TAG, "图片已清除")
        }
    }

    private fun loadViaCallback() {
        ImageLoader.getInstance().load(
            ImageRequest.Builder(ImageSource.Url(BasicLoadFragment.TEST_IMAGE_URL))
                .intoCallback(
                    onSuccess = { bitmap ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) {
                                Log.d(TAG,
                                    "回调成功: ${bitmap.width}x${bitmap.height}, size=${bitmap.byteCount / 1024}KB")
                            }
                        }
                    },
                    onError = { error -> Log.e(TAG, "回调失败: ${error.message}") }
                ).build()
        )
    }

    companion object {
        private const val TAG = "ImageLoaderSample"
    }
}
