package app.allever.android.sample.cleaner.ui.fragment

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.core.CleanEngine
import app.allever.android.sample.cleaner.core.CleanResult
import app.allever.android.sample.cleaner.core.CleanType
import app.allever.android.sample.cleaner.databinding.FragmentStorageCleanBinding
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import app.allever.android.sample.cleaner.ui.adapter.JunkCategoryAdapter
import app.allever.android.sample.cleaner.ui.adapter.JunkFileAdapter
import kotlinx.coroutines.launch

/**
 * 存储清理 Fragment
 *
 * 负责展示存储清理界面：扫描 → 分类概览 → 选择分类查看详情 → 清理。
 * 扫描结果按 CleanType 归类展示，每个类别显示总大小和文件数量。
 */
class StorageCleanFragment :
    BaseFragment<FragmentStorageCleanBinding, CleanerViewModel>() {

    private lateinit var categoryAdapter: JunkCategoryAdapter
    private lateinit var fileAdapter: JunkFileAdapter
    private var allItems: List<JunkFileItem> = emptyList()

    override fun inflate(): FragmentStorageCleanBinding =
        FragmentStorageCleanBinding.inflate(layoutInflater)

    override fun init() {
        initCategoryRecyclerView()
        initFileRecyclerView()
        initClickListeners()
        observeEngineState()
    }

    // ========== 初始化 ==========

    private fun initCategoryRecyclerView() {
        categoryAdapter = JunkCategoryAdapter { type, files ->
            onCategorySelected(type, files)
        }
        mBinding.rvCategories.adapter = categoryAdapter
        mBinding.rvCategories.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    private fun initFileRecyclerView() {
        fileAdapter = JunkFileAdapter().apply {
            onSelectionChanged = { selectedCount, totalCount ->
                mBinding.tvSelectedCount.text = "已选 $selectedCount 项"
                mBinding.btnClean.isEnabled = selectedCount > 0

                // 反向同步全选按钮状态
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
        mBinding.rvJunkFiles.adapter = fileAdapter
        mBinding.rvJunkFiles.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    /** 注册全选监听器 */
    private fun setupSelectAllListener() {
        mBinding.cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            // 全选操作作用于当前显示的文件列表（可能是某个分类下的文件）
            fileAdapter.selectAll(isChecked)
        }
    }

    private fun initClickListeners() {
        mBinding.btnScan.setOnClickListener { startScan() }
        mBinding.btnStopScan.setOnClickListener { stopCurrentScan() }
        mBinding.btnClean.setOnClickListener { startClean() }
        setupSelectAllListener()
    }

    // ========== 分类选择 ==========

    /**
     * 用户点击了某个垃圾分类
     *
     * @param type 选中的 CleanType
     * @param files 该分类下的所有文件
     */
    private fun onCategorySelected(type: CleanType, files: List<JunkFileItem>) {
        if (files.isEmpty()) return

        // 显示该分类下的文件详情列表
        setVisibility(mBinding.actionBar, true)
        setVisibility(mBinding.rvJunkFiles, true)

        val sortedFiles = files.sortedDescending()
        fileAdapter.setList(sortedFiles)

        // 更新选中计数
        mBinding.tvSelectedCount.text = "已选 0 项"
        mBinding.btnClean.isEnabled = false
    }

    // ========== 扫描 & 清理逻辑 ==========

    private fun startScan() {
        resetUI()
        setVisibility(mBinding.progressBar, true)
        mBinding.tvScanStatus.text = "正在扫描垃圾文件..."

        // 切换按钮：隐藏扫描，显示停止
        setVisibility(mBinding.btnScan, false)
        setVisibility(mBinding.btnStopScan, true)

        lifecycleScope.launch {
            CleanEngine.fullScan()
        }
    }

    /**
     * 停止当前扫描
     */
    private fun stopCurrentScan() {
        CleanEngine.stopScan()
        mBinding.tvScanStatus.text = "正在停止扫描..."
    }

    private fun startClean() {
        val selectedItems = fileAdapter.getSelectedItems()
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

        // 从全量数据中移除已清理的文件，刷新分类列表
        val cleanedPaths = results.flatMap { it.cleanedFiles }.map { it.absolutePath }.toSet()
        allItems = allItems.filter { it.absolutePath !in cleanedPaths }

        if (allItems.isNotEmpty()) {
            showCategoryResults(allItems.groupBy { it.type })
        } else {
            resetUI()
            mBinding.tvScanStatus.text = "未发现垃圾文件"
        }

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
                // 切换按钮：隐藏扫描，显示停止
                setVisibility(mBinding.btnScan, false)
                setVisibility(mBinding.btnStopScan, true)
            }

            is CleanEngine.ScanState.Scanned -> {
                setVisibility(mBinding.progressBar, false)
                // 恢复按钮：显示扫描，隐藏停止
                setVisibility(mBinding.btnScan, true)
                setVisibility(mBinding.btnStopScan, false)
                onScanCompleted(state.results)
            }

            is CleanEngine.ScanState.Cancelled -> {
                setVisibility(mBinding.progressBar, false)
                // 恢复按钮
                setVisibility(mBinding.btnScan, true)
                setVisibility(mBinding.btnStopScan, false)
                mBinding.tvScanStatus.text = "扫描已取消"
                mBinding.btnScan.isEnabled = true
            }

            is CleanEngine.ScanState.Error -> {
                setVisibility(mBinding.progressBar, false)
                // 恢复按钮
                setVisibility(mBinding.btnScan, true)
                setVisibility(mBinding.btnStopScan, false)
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

    /**
     * 扫描完成，按类型归类展示结果
     */
    private fun onScanCompleted(
        results: Map<CleanType, List<JunkFileItem>>
    ) {
        allItems = results.values.flatten().sortedDescending()
        val totalSize = allItems.sumOf { it.size }
        val totalCount = allItems.size

        if (allItems.isEmpty()) {
            mBinding.tvScanStatus.text = "未发现垃圾文件"
            mBinding.tvTotalSize.text = "可清理：0 B"
            mBinding.tvItemCount.text = "共 0 项"
            setVisibility(mBinding.rvCategories, false)
            setVisibility(mBinding.actionBar, false)
            setVisibility(mBinding.rvJunkFiles, false)
            mBinding.btnClean.isEnabled = false
        } else {
            mBinding.tvScanStatus.text = "扫描完成"
            mBinding.tvTotalSize.text = "可清理：${CleanEngine.formatSize(totalSize)}"
            mBinding.tvItemCount.text = "共 $totalCount 项"
            mBinding.btnClean.isEnabled = true

            // 按类型分组展示
            showCategoryResults(results)
        }

        mBinding.btnScan.isEnabled = true
    }

    /**
     * 展示分类结果
     */
    private fun showCategoryResults(results: Map<CleanType, List<JunkFileItem>>) {
        val categories = results
            .filter { it.value.isNotEmpty() && it.key != CleanType.ALL }
            .map { (type, files) ->
                JunkCategoryAdapter.CategoryItem(
                    type = type,
                    files = files,
                    totalSize = files.sumOf { it.size }
                )
            }
            .sortedByDescending { it.totalSize }

        if (categories.isNotEmpty()) {
            setVisibility(mBinding.rvCategories, true)
            categoryAdapter.setList(categories)
        } else {
            setVisibility(mBinding.rvCategories, false)
        }

        // 默认不显示详情列表，等用户点击分类后再显示
        setVisibility(mBinding.actionBar, false)
        setVisibility(mBinding.rvJunkFiles, false)
        fileAdapter.setList(emptyList())
    }

    private fun resetUI() {
        allItems = emptyList()
        categoryAdapter.setList(emptyList())
        fileAdapter.setList(emptyList())

        setVisibility(mBinding.rvCategories, false)
        setVisibility(mBinding.actionBar, false)
        setVisibility(mBinding.rvJunkFiles, false)

        // 重置全选按钮
        mBinding.cbSelectAll.setOnCheckedChangeListener(null)
        mBinding.cbSelectAll.isChecked = false
        setupSelectAllListener()

        mBinding.tvTotalSize.text = "可清理：-- MB"
        mBinding.tvItemCount.text = "共 -- 项"
        mBinding.tvScanStatus.text = "点击扫描检测垃圾文件"

        // 恢复默认按钮状态
        setVisibility(mBinding.btnScan, true)
        setVisibility(mBinding.btnStopScan, false)
    }

    // ========== 生命周期 ==========

    override fun onDestroyView() {
        super.onDestroyView()
        // 退出页面时停止正在进行的扫描
        if (CleanEngine.isScanning) {
            CleanEngine.stopScan()
        }
    }
}
