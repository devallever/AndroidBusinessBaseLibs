package app.allever.android.lib.media.picker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.picker.R
import app.allever.android.lib.media.picker.databinding.ItemFolderBinding
import com.bumptech.glide.Glide

/**
 * 目录列表 Adapter（用于目录抽屉）
 */
class FolderAdapter(
    private val onItemClick: (MediaFolder) -> Unit,
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    private val items = mutableListOf<MediaFolder>()
    private var selectedBucketId: Long = -1L

    fun submitList(folders: List<MediaFolder>) {
        items.clear()
        items.addAll(folders)
        notifyDataSetChanged()
    }

    fun setSelectedBucketId(bucketId: Long) {
        val oldPos = items.indexOfFirst { it.bucketId == selectedBucketId }
        selectedBucketId = bucketId
        val newPos = items.indexOfFirst { it.bucketId == selectedBucketId }
        if (oldPos >= 0) notifyItemChanged(oldPos)
        if (newPos >= 0) notifyItemChanged(newPos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos >= 0 && pos < items.size) {
                    onItemClick(items[pos])
                }
            }
        }

        fun bind(folder: MediaFolder) {
            binding.tvFolderName.text = folder.name
            binding.tvFolderCount.text = folder.totalCount.toString()

            val isSelected = folder.bucketId == selectedBucketId
            binding.ivCheck.visibility = if (isSelected) android.view.View.VISIBLE else android.view.View.GONE

            // 加载封面缩略图
            Glide.with(itemView)
                .load(folder.coverUri)
                .placeholder(R.color.media_picker_placeholder)
                .centerCrop()
                .into(binding.ivCover)
        }
    }
}
