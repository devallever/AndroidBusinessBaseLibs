package app.allever.android.sample.cleaner.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.allever.android.lib.common.BaseActivity
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.databinding.ActivityFileCategoryDetailBinding
import app.allever.android.sample.cleaner.file.FileCategory
import app.allever.android.sample.cleaner.file.FileInfo
import app.allever.android.sample.cleaner.file.FileManager
import app.allever.android.sample.cleaner.ui.adapter.FileDetailAdapter
import kotlinx.coroutines.launch

/**
 * 文件分类详情 Activity
 *
 * 独立页面展示某个分类下的文件列表，根据不同类型显示不同的关键信息：
 * - 视频：缩略图 + 时长 + 分辨率 + 路径 + 大小
 * - 音频：图标 + 时长 + 路径 + 大小
 * - 图片：缩略图 + 尺寸 + 路径 + 大小
 * - 文档：图标 + 文档类型 + 路径 + 大小
 * - APK：图标 + 包名 + 版本 + 大小
 * - 压缩包/其他：图标 + 类型 + 路径 + 大小
 */
class FileCategoryDetailActivity :
    BaseActivity<ActivityFileCategoryDetailBinding, CleanerViewModel>() {

    companion object {
        const val ARG_CATEGORY = "arg_category"
    }

    private lateinit var adapter: FileDetailAdapter
    private lateinit var currentCategory: FileCategory

    override fun inflateChildBinding(): ActivityFileCategoryDetailBinding =
        ActivityFileCategoryDetailBinding.inflate(layoutInflater)

    override fun init() {
        // 从参数获取分类
        val categoryName = intent.getStringExtra(ARG_CATEGORY) ?: FileCategory.VIDEO.name
        currentCategory = FileCategory.valueOf(categoryName)

        // 设置标题栏（带返回按钮）
        initTopBar(title = "${currentCategory.displayName} 列表", showBackIcon = true)

        setupRecyclerView()
        loadFiles()
    }

    private fun setupRecyclerView() {
        adapter = FileDetailAdapter(currentCategory)
        binding.rvFiles.adapter = adapter
        binding.rvFiles.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    private fun loadFiles() {
        setVisibility(binding.progressBar, true)
        setVisibility(binding.emptyLayout, false)
        setVisibility(binding.rvFiles, false)

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                val files = FileManager.getFilesByCategory(currentCategory)

                if (files.isNotEmpty()) {
                    showResults(files)
                } else {
                    showEmpty()
                }

                setVisibility(binding.progressBar, false)
            }
        }
    }

    private fun showResults(files: List<FileInfo>) {
        setVisibility(binding.emptyLayout, false)
        setVisibility(binding.rvFiles, true)
        adapter.setList(files)
        binding.tvCount.text = "共 ${files.size} 个文件"
    }

    private fun showEmpty() {
        setVisibility(binding.emptyLayout, true)
        setVisibility(binding.rvFiles, false)
        binding.tvEmptyHint.text = "${currentCategory.displayName} 分类下暂无文件"
    }

    override fun onDestroy() {
        adapter.release()
        super.onDestroy()
    }
}
