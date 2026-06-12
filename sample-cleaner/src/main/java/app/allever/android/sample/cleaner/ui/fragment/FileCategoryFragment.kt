package app.allever.android.sample.cleaner.ui.fragment

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.databinding.FragmentFileCategoryBinding
import app.allever.android.sample.cleaner.file.FileCategory
import app.allever.android.sample.cleaner.file.FileInfo
import app.allever.android.sample.cleaner.file.FileManager
import app.allever.android.sample.cleaner.ui.adapter.LargeFileAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.launch

/**
 * 文件分类浏览子页面
 *
 * 独立页面，展示分类选择网格 + 对应分类下的文件列表。
 */
class FileCategoryFragment :
    BaseFragment<FragmentFileCategoryBinding, CleanerViewModel>() {

    /** 当前选中的分类 */
    private var selectedCategory: FileCategory? = null

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var fileAdapter: LargeFileAdapter

    override fun inflate(): FragmentFileCategoryBinding =
        FragmentFileCategoryBinding.inflate(layoutInflater)

    override fun init() {
        setupCategoryGrid()
        setupFileList()
        showEmptyState("请选择上方分类查看文件")
    }

    // ========== 分类网格 ==========

    private fun setupCategoryGrid() {
        categoryAdapter = CategoryAdapter().apply { onSelected = ::onCategorySelected }
        mBinding.rvCategories.adapter = categoryAdapter
        categoryAdapter.setList(FileCategory.entries.filter { it != FileCategory.UNKNOWN })
    }

    // ========== 文件列表 ==========

    private fun setupFileList() {
        fileAdapter = LargeFileAdapter()
        mBinding.rvCategoryFiles.adapter = fileAdapter
        mBinding.rvCategoryFiles.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    private fun onCategorySelected(category: FileCategory) {
        selectedCategory = category
        loadFilesByCategory(category)
    }

    private fun loadFilesByCategory(category: FileCategory) {
        showScanningState()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val files = FileManager.getFilesByCategory(category)

                if (files.isNotEmpty()) {
                    showFileList(files)
                } else {
                    showEmptyState("${category.displayName} 分类下暂无文件")
                }
            }
        }
    }

    // ========== UI 状态 ==========

    private fun showFileList(files: List<FileInfo>) {
        setVisibility(mBinding.emptyLayout, false)
        setVisibility(mBinding.rvCategoryFiles, true)
        fileAdapter.setList(files)
    }

    private fun showEmptyState(message: String) {
        setVisibility(mBinding.emptyLayout, true)
        setVisibility(mBinding.rvCategoryFiles, false)
        mBinding.tvEmptyHint.text = message
    }

    private fun showScanningState() {
        setVisibility(mBinding.emptyLayout, true)
        setVisibility(mBinding.rvCategoryFiles, false)
        mBinding.tvEmptyHint.text = "正在扫描 ${selectedCategory?.displayName ?: ""} 文件..."
    }

    // ========== 分类选择 Adapter ==========

    /**
     * 分类选择网格适配器
     */
    class CategoryAdapter :
        BaseQuickAdapter<FileCategory, BaseViewHolder>(0) {

        var onSelected: ((FileCategory) -> Unit)? = null
        private var selectedPosition: Int = -1

        override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val tv = TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(8, 12, 8, 12)
            }
            return BaseViewHolder(tv)
        }

        override fun convert(holder: BaseViewHolder, item: FileCategory) {
            val tv = holder.itemView as TextView
            tv.text = item.displayName

            val isSelected = data.indexOf(item) == selectedPosition
            tv.isSelected = isSelected
            tv.setBackgroundColor(
                if (isSelected) 0xFFE3F2FD.toInt() else 0xFFF5F5F5.toInt()
            )

            holder.itemView.setOnClickListener {
                val pos = data.indexOf(item)
                if (pos == selectedPosition) return@setOnClickListener
                selectedPosition = pos
                notifyDataSetChanged()
                onSelected?.invoke(item)
            }
        }
    }
}
