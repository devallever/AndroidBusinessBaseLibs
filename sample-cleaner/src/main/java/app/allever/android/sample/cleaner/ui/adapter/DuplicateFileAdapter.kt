package app.allever.android.sample.cleaner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import app.allever.android.sample.cleaner.databinding.ItemJunkFileBinding
import app.allever.android.sample.cleaner.file.FileInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 重复文件列表适配器
 *
 * 展示检测到的重复文件。
 */
class DuplicateFileAdapter :
    BaseQuickAdapter<FileInfo, BaseViewHolder>(0) {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemJunkFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding.root)
    }

    override fun convert(holder: BaseViewHolder, item: FileInfo) {
        val binding = ItemJunkFileBinding.bind(holder.itemView)

        binding.tvFileName.text = item.fileName

        val path = item.absolutePath
        binding.tvFilePath.text =
            if (path.length > 50) "..." + path.takeLast(47) else path

        binding.tvFileSize.text = item.formattedSize
        binding.tvFileType.text = "${item.category.displayName} (重复)"

        // 不显示 CheckBox
        binding.cbSelected.visibility = View.GONE
    }
}
