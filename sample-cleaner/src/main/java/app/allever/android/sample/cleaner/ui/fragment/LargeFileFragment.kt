package app.allever.android.sample.cleaner.ui.fragment

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.databinding.FragmentLargeFileBinding
import app.allever.android.sample.cleaner.file.FileInfo
import app.allever.android.sample.cleaner.file.FileManager
import app.allever.android.sample.cleaner.ui.adapter.LargeFileAdapter
import kotlinx.coroutines.launch

/**
 * 大文件扫描子页面
 *
 * 独立页面，负责扫描并展示 >= 10MB 的大文件列表。
 */
class LargeFileFragment :
    BaseFragment<FragmentLargeFileBinding, CleanerViewModel>() {

    private lateinit var adapter: LargeFileAdapter

    override fun inflate(): FragmentLargeFileBinding =
        FragmentLargeFileBinding.inflate(layoutInflater)

    override fun init() {
        setupRecyclerView()
        setupClickListeners()
        showEmptyState("暂无大文件，点击下方按钮扫描")
    }

    private fun setupRecyclerView() {
        adapter = LargeFileAdapter()
        mBinding.rvLargeFiles.adapter = adapter
        mBinding.rvLargeFiles.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    private fun setupClickListeners() {
        mBinding.btnScanLargeFiles.setOnClickListener { startScan() }
    }

    private fun startScan() {
        showScanningState()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val result = FileManager.scanLargeFiles()

                if (result.files.isNotEmpty()) {
                    showResult(result.files, result.totalSize)
                } else {
                    showEmptyState("未发现大文件 (>=10MB)")
                }
            }
        }
    }

    private fun showResult(files: List<FileInfo>, totalSize: Long) {
        setVisibility(mBinding.emptyLayout, false)
        setVisibility(mBinding.rvLargeFiles, true)

        adapter.setList(files)

        // 更新空状态提示文本（用于后续可能的状态切换）
        mBinding.tvEmptyHint.text = "共发现 ${files.size} 个大文件，总计 ${app.allever.android.sample.cleaner.core.CleanEngine.formatSize(totalSize)}"
    }

    private fun showEmptyState(message: String) {
        setVisibility(mBinding.emptyLayout, true)
        setVisibility(mBinding.rvLargeFiles, false)
        mBinding.tvEmptyHint.text = message
        setVisibility(mBinding.btnScanLargeFiles, true)
    }

    private fun showScanningState() {
        setVisibility(mBinding.emptyLayout, true)
        setVisibility(mBinding.rvLargeFiles, false)
        mBinding.tvEmptyHint.text = "正在扫描大文件..."
        setVisibility(mBinding.btnScanLargeFiles, false)
    }
}
