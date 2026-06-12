package app.allever.android.sample.cleaner.ui.fragment

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.databinding.FragmentDuplicateBinding
import app.allever.android.sample.cleaner.file.FileInfo
import app.allever.android.sample.cleaner.file.FileManager
import app.allever.android.sample.cleaner.ui.adapter.DuplicateFileAdapter
import kotlinx.coroutines.launch

/**
 * 重复文件检测子页面
 *
 * 独立页面，负责检测并展示重复文件列表。
 */
class DuplicateFileFragment :
    BaseFragment<FragmentDuplicateBinding, CleanerViewModel>() {

    private lateinit var adapter: DuplicateFileAdapter

    override fun inflate(): FragmentDuplicateBinding =
        FragmentDuplicateBinding.inflate(layoutInflater)

    override fun init() {
        setupRecyclerView()
        setupClickListeners()
        showEmptyState("暂无重复文件，点击下方按钮检测")
    }

    private fun setupRecyclerView() {
        adapter = DuplicateFileAdapter()
        mBinding.rvDuplicateFiles.adapter = adapter
        mBinding.rvDuplicateFiles.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    private fun setupClickListeners() {
        mBinding.btnScanDuplicates.setOnClickListener { startDetect() }
    }

    private fun startDetect() {
        showScanningState()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val result = FileManager.detectDuplicates()

                if (result.groups.isNotEmpty()) {
                    val allDuplicates = result.groups.values.flatten()
                    showResult(allDuplicates, result.totalWastedSize, result.groups.size)
                } else {
                    showEmptyState("未发现重复文件")
                }
            }
        }
    }

    private fun showResult(
        files: List<FileInfo>,
        wastedSize: Long,
        groupCount: Int
    ) {
        setVisibility(mBinding.emptyLayout, false)
        setVisibility(mBinding.rvDuplicateFiles, true)

        adapter.setList(files)

        mBinding.tvEmptyHint.text =
            "发现 $groupCount 组重复文件，可释放 ${app.allever.android.sample.cleaner.core.CleanEngine.formatSize(wastedSize)}"
    }

    private fun showEmptyState(message: String) {
        setVisibility(mBinding.emptyLayout, true)
        setVisibility(mBinding.rvDuplicateFiles, false)
        mBinding.tvEmptyHint.text = message
        setVisibility(mBinding.btnScanDuplicates, true)
    }

    private fun showScanningState() {
        setVisibility(mBinding.emptyLayout, true)
        setVisibility(mBinding.rvDuplicateFiles, false)
        mBinding.tvEmptyHint.text = "正在检测重复文件（计算 MD5）..."
        setVisibility(mBinding.btnScanDuplicates, false)
    }
}
