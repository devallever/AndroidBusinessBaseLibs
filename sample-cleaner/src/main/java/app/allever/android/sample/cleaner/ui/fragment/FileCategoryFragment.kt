package app.allever.android.sample.cleaner.ui.fragment

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.databinding.FragmentFileCategoryBinding
import app.allever.android.sample.cleaner.file.FileCategory
import app.allever.android.sample.cleaner.ui.activity.FileCategoryDetailActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 文件分类浏览子页面（主入口）
 *
 * 展示文件分类选择网格。
 * 点击某个分类后，跳转到 FileCategoryDetailFragment 显示该分类的详细文件列表。
 */
class FileCategoryFragment :
    BaseFragment<FragmentFileCategoryBinding, CleanerViewModel>() {

    private lateinit var categoryAdapter: CategoryAdapter

    override fun inflate(): FragmentFileCategoryBinding =
        FragmentFileCategoryBinding.inflate(layoutInflater)

    override fun init() {
        setupCategoryGrid()
    }

    // ========== 分类网格 ==========

    private fun setupCategoryGrid() {
        categoryAdapter = CategoryAdapter { category ->
            onCategorySelected(category)
        }
        mBinding.rvCategories.adapter = categoryAdapter
        categoryAdapter.setList(FileCategory.entries.filter { it != FileCategory.UNKNOWN })
    }

    /**
     * 用户点击了某个分类 → 启动详情 Activity
     */
    private fun onCategorySelected(category: FileCategory) {
        ActivityHelper.startActivity<FileCategoryDetailActivity>() {
            putExtra(FileCategoryDetailActivity.ARG_CATEGORY, category.name)
        }
    }

    // ========== 分类选择 Adapter ==========

    /**
     * 分类选择网格适配器
     */
    class CategoryAdapter(
        private val onSelected: ((FileCategory) -> Unit)? = null
    ) : BaseQuickAdapter<FileCategory, BaseViewHolder>(0) {

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
