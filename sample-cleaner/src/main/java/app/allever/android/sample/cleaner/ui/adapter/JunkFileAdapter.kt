package app.allever.android.sample.cleaner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import app.allever.android.sample.cleaner.databinding.ItemJunkFileBinding
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 垃圾文件列表适配器
 *
 * 使用 BaseQuickAdapter + ViewBinding，
 * 支持选中/取消操作，展示文件名、路径、大小和类型。
 */
class JunkFileAdapter :
    BaseQuickAdapter<JunkFileItem, BaseViewHolder>(0) {

    /** 选中状态变化回调 */
    var onSelectionChanged: ((selectedCount: Int, totalCount: Int) -> Unit)? = null

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemJunkFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BaseViewHolder(binding.root)
    }

    override fun convert(holder: BaseViewHolder, item: JunkFileItem) {
        val binding = ItemJunkFileBinding.bind(holder.itemView)

        // 文件名
        binding.tvFileName.text = item.fileName

        // 文件路径（截断显示）
        val path = item.absolutePath
        binding.tvFilePath.text =
            if (path.length > 50) "..." + path.takeLast(47) else path

        // 文件大小
        binding.tvFileSize.text = item.formattedSize

        // 文件类型标签
        binding.tvFileType.text = item.type.displayName

        // 选中状态
        binding.cbSelected.setOnCheckedChangeListener(null)
        binding.cbSelected.isChecked = item.selected
        binding.cbSelected.setOnCheckedChangeListener { _, isChecked ->
            item.selected = isChecked
            onSelectionChanged?.invoke(
                data.count { it.selected },
                data.size
            )
        }
    }

    /**
     * 全选 / 取消全选
     */
    fun selectAll(selected: Boolean) {
        data.forEach { it.selected = selected }
        notifyDataSetChanged()
        onSelectionChanged?.invoke(
            if (selected) data.size else 0,
            data.size
        )
    }

    /**
     * 获取已选中项
     */
    fun getSelectedItems(): List<JunkFileItem> = data.filter { it.selected }
}
