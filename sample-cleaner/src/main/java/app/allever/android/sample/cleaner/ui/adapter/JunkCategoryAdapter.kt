package app.allever.android.sample.cleaner.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import app.allever.android.sample.cleaner.core.CleanType
import app.allever.android.sample.cleaner.databinding.ItemJunkCategoryBinding
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 垃圾文件分类列表适配器
 *
 * 展示按 CleanType 分组的垃圾文件摘要：
 * - 分类名称（应用缓存/日志文件/临时文件等）
 * - 该分类下的文件数量
 * - 该分类的总大小
 */
class JunkCategoryAdapter(
    private val onCategoryClick: ((CleanType, List<JunkFileItem>) -> Unit)? = null
) : BaseQuickAdapter<JunkCategoryAdapter.CategoryItem, BaseViewHolder>(0) {

    data class CategoryItem(
        val type: CleanType,
        val files: List<JunkFileItem>,
        val totalSize: Long,
        var isExpanded: Boolean = false
    ) {
        val formattedSize: String get() = formatSize(totalSize)
        val fileCount: Int get() = files.size

        companion object {
            fun formatSize(size: Long): String {
                if (size < 0) return "0B"
                val units = arrayOf("B", "KB", "MB", "GB", "TB")
                var unitIndex = 0
                var value = size.toDouble()
                while (value >= 1024 && unitIndex < units.lastIndex) {
                    value /= 1024
                    unitIndex++
                }
                return if (unitIndex == 0) "${size.toLong()}${units[unitIndex]}"
                else String.format("%.1f%s", value, units[unitIndex])
            }
        }
    }

    /** 当前选中的分类类型 */
    private var selectedType: CleanType? = null

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemJunkCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding.root)
    }

    override fun convert(holder: BaseViewHolder, item: CategoryItem) {
        val binding = ItemJunkCategoryBinding.bind(holder.itemView)

        // 分类名称和颜色
        binding.tvCategoryName.text = item.type.displayName
        binding.viewCategoryColor.setBackgroundColor(getCategoryColor(item.type))

        // 文件数量
        binding.tvCategoryCount.text = "${item.fileCount} 个文件"

        // 总大小
        binding.tvCategorySize.text = item.formattedSize

        // 展开指示器
        binding.ivExpandIndicator.rotation =
            if (item.isExpanded || selectedType == item.type) 180f else 0f

        // 选中高亮
        val isSelected = selectedType == item.type
        binding.root.setCardBackgroundColor(
            if (isSelected) Color.parseColor("#FFF3E0") else Color.WHITE
        )

        holder.itemView.setOnClickListener {
            if (selectedType == item.type) {
                // 再次点击取消选中
                selectedType = null
                item.isExpanded = false
            } else {
                // 取消之前的选中
                data.forEach { it.isExpanded = false }
                selectedType = item.type
                item.isExpanded = true
            }
            notifyDataSetChanged()
            onCategoryClick?.invoke(item.type, item.files)
        }
    }

    /**
     * 获取分类对应的主题色
     */
    private fun getCategoryColor(type: CleanType): Int = when (type) {
        CleanType.CACHE -> Color.parseColor("#FF5722")
        CleanType.LOG -> Color.parseColor("#795548")
        CleanType.TEMP -> Color.parseColor("#FF9800")
        CleanType.RESIDUAL -> Color.parseColor("#9C27B0")
        CleanType.AD_CACHE -> Color.parseColor("#F44336")
        CleanType.APK -> Color.parseColor("#9C27B0")
        CleanType.LARGE_FILE -> Color.parseColor("#2196F3")
        CleanType.DUPLICATE_FILE -> Color.parseColor("#009688")
        CleanType.ALL -> Color.parseColor("#607D8B")
    }

    /**
     * 清除选中状态
     */
    fun clearSelection() {
        selectedType = null
        data.forEach { it.isExpanded = false }
        notifyDataSetChanged()
    }

    /**
     * 获取当前选中的所有文件（跨所有分类）
     */
    fun getAllFiles(): List<JunkFileItem> = data.flatMap { it.files }
}
