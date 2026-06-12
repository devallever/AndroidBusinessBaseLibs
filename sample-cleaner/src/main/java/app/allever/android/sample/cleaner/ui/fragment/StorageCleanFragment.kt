package app.allever.android.sample.cleaner.ui.fragment

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.core.CleanEngine
import app.allever.android.sample.cleaner.core.CleanResult
import app.allever.android.sample.cleaner.databinding.FragmentStorageCleanBinding
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import app.allever.android.sample.cleaner.ui.adapter.JunkFileAdapter
import kotlinx.coroutines.launch

/**
 * 存储清理 Fragment
 *
 * 负责展示存储清理界面：扫描 → 预览列表 → 选择 → 清理。
 * 通过 CleanEngine 的响应式状态流驱动 UI 更新。
 */
class StorageCleanFragment :
    BaseFragment<FragmentStorageCleanBinding, CleanerViewModel>() {

    private lateinit var adapter: JunkFileAdapter
    private var allItems: List<JunkFileItem> = emptyList()

    override fun inflate(): FragmentStorageCleanBinding =
        FragmentStorageCleanBinding.inflate(layoutInflater)

    override fun init() {
        initRecyclerView()
        initClickListeners()
        observeEngineState()
    }

    // ========== 初始化 ==========

    private fun initRecyclerView() {
        adapter = JunkFileAdapter().apply {
            onSelectionChanged = { selectedCount, totalCount ->
                mBinding.tvSelectedCount.text = "已选 $selectedCount 项"
                mBinding.btnClean.isEnabled = selectedCount > 0

                // 反向同步全选按钮状态
                // 防止循环触发：只在用户操作子项时更新全选按钮
                if (selectedCount == totalCount && totalCount > 0) {
                    if (!mBinding.cbSelectAll.isChecked) {
                        mBinding.cbSelectAll.setOnCheckedChangeListener(null)
                        mBinding.cbSelectAll.isChecked = true
                        setupSelectAllListener()
                    }
                } else {
                    if (mBinding.cbSelectAll.isChecked) {
                        mBinding.cbSelectAll.setOnCheckedChangeListener(null)
                        mBinding.cbSelectAll.isChecked = false
                        setupSelectAllListener()
                    }
                }
            }
        }
        mBinding.rvJunkFiles.adapter = adapter
        mBinding.rvJunkFiles.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    /** 注册全选监听器（抽取方法避免重复代码） */
    private fun setupSelectAllListener() {
        mBinding.cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            adapter.selectAll(isChecked)
        }
    }

    private fun initClickListeners() {
        mBinding.btnScan.setOnClickListener { startScan() }
        mBinding.btnClean.setOnClickListener { startClean() }

        setupSelectAllListener()
    }

    // ========== 扫描 & 清理逻辑 ==========

    private fun startScan() {
        resetUI()
        setVisibility(mBinding.progressBar, true)
        mBinding.tvScanStatus.text = "正在扫描垃圾文件..."
        mBinding.btnScan.isEnabled = false

        lifecycleScope.launch {
            CleanEngine.fullScan()
        }
    }

    private fun startClean() {
        val selectedItems = adapter.getSelectedItems()
        if (selectedItems.isEmpty()) return

        setVisibility(mBinding.progressBar, true)
        mBinding.tvScanStatus.text = "正在清理..."
        mBinding.btnClean.isEnabled = false
        mBinding.btnScan.isEnabled = false

        lifecycleScope.launch {
            val results = CleanEngine.clean(selectedItems)
            onCleanCompleted(results)
        }
    }

    private fun onCleanCompleted(results: List<CleanResult>) {
        val totalSize = results.sumOf { it.cleanedSize }
        val totalCount = results.sumOf { it.cleanedCount }

        setVisibility(mBinding.progressBar, false)
        mBinding.tvScanStatus.text = "清理完成！"
        mBinding.tvTotalSize.text = "已释放 ${CleanEngine.formatSize(totalSize)}"
        mBinding.tvItemCount.text = "共清理 $totalCount 项文件"
        setVisibility(mBinding.actionBar, false)
        setVisibility(mBinding.rvJunkFiles, false)

        val cleanedFiles =
            results.flatMap { it.cleanedFiles }.map { it.absolutePath }.toSet()
        allItems = allItems.filter { it.absolutePath !in cleanedFiles }
        adapter.setList(allItems)

        mBinding.btnScan.isEnabled = true
    }

    // ========== 状态观察 ==========

    private fun observeEngineState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    CleanEngine.scanState.collect { onScanStateChanged(it) }
                }
                launch {
                    CleanEngine.cleanState.collect { onCleanStateChanged(it) }
                }
            }
        }
    }

    private fun onScanStateChanged(state: CleanEngine.ScanState) {
        when (state) {
            is CleanEngine.ScanState.Idle -> {}
            is CleanEngine.ScanState.Scanning -> {
                setVisibility(mBinding.progressBar, true)
                mBinding.tvScanStatus.text = "正在扫描垃圾文件..."
            }

            is CleanEngine.ScanState.Scanned -> {
                setVisibility(mBinding.progressBar, false)
                onScanCompleted(state.results)
            }

            is CleanEngine.ScanState.Error -> {
                setVisibility(mBinding.progressBar, false)
                mBinding.tvScanStatus.text = "扫描失败: ${state.message}"
                mBinding.btnScan.isEnabled = true
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onCleanStateChanged(state: CleanEngine.CleanState) {
        when (state) {
            is CleanEngine.CleanState.Idle -> {}
            is CleanEngine.CleanState.Cleaning -> {
                setVisibility(mBinding.progressBar, true)
                mBinding.tvScanStatus.text =
                    "正在清理... (${state.currentProgress}/${state.totalProgress})"
            }

            is CleanEngine.CleanState.Completed -> {} // 已在 onCleanCompleted 处理
            is CleanEngine.CleanState.Error -> {
                setVisibility(mBinding.progressBar, false)
                mBinding.tvScanStatus.text = "清理失败: ${state.message}"
                mBinding.btnScan.isEnabled = true
                mBinding.btnClean.isEnabled = true
            }
        }
    }

    // ========== UI 辅助方法 ==========

    private fun onScanCompleted(
        results: Map<app.allever.android.sample.cleaner.core.CleanType, List<JunkFileItem>>
    ) {
        allItems = results.values.flatten().sortedDescending()
        adapter.setList(allItems)

        val totalSize = allItems.sumOf { it.size }
        val totalCount = allItems.size

        if (allItems.isEmpty()) {
            mBinding.tvScanStatus.text = "未发现垃圾文件"
            mBinding.tvTotalSize.text = "可清理：0 B"
            mBinding.tvItemCount.text = "共 0 项"
            setVisibility(mBinding.actionBar, false)
            setVisibility(mBinding.rvJunkFiles, false)
            mBinding.btnClean.isEnabled = false
        } else {
            mBinding.tvScanStatus.text = "扫描完成"
            mBinding.tvTotalSize.text = "可清理：${CleanEngine.formatSize(totalSize)}"
            mBinding.tvItemCount.text = "共 $totalCount 项"
            setVisibility(mBinding.actionBar, true)
            setVisibility(mBinding.rvJunkFiles, true)
            mBinding.btnClean.isEnabled = true
        }

        mBinding.btnScan.isEnabled = true
    }

    private fun resetUI() {
        allItems = emptyList()
        adapter.setList(emptyList())
        setVisibility(mBinding.actionBar, false)
        setVisibility(mBinding.rvJunkFiles, false)

        // 重置全选按钮（先移除 listener 防止触发回调，重置后再恢复）
        mBinding.cbSelectAll.setOnCheckedChangeListener(null)
        mBinding.cbSelectAll.isChecked = false
        setupSelectAllListener()

        mBinding.tvTotalSize.text = "可清理：-- MB"
        mBinding.tvItemCount.text = "共 -- 项"
        mBinding.tvScanStatus.text = "点击扫描检测垃圾文件"
    }
}
