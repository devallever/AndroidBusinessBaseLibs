package app.allever.android.lib.media.picker.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.R
import app.allever.android.lib.media.picker.databinding.ItemMediaGridBinding
import app.allever.android.lib.media.picker.selection.SelectionManager
import com.bumptech.glide.Glide

/**
 * 媒体网格 Adapter（用于图片和视频展示）
 */
class MediaGridAdapter(
    private val selectionManager: SelectionManager,
    private val onItemClick: (MediaItem, Int) -> Unit,
) : RecyclerView.Adapter<MediaGridAdapter.ViewHolder>() {

    private val items = mutableListOf<MediaItem>()

    fun submitList(data: List<MediaItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun updateSelection() {
        // 刷新所有可见项的选中状态
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTION)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_SELECTION)) {
            holder.updateSelection(items[position])
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    inner class ViewHolder(private val binding: ItemMediaGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos >= 0 && pos < items.size) {
                    onItemClick(items[pos], pos)
                }
            }
            binding.layoutCheck.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos >= 0 && pos < items.size) {
                    // 仅切换选中，不触发预览
                    selectionManager.toggle(items[pos])
                    updateSelection(items[pos])
                }
            }
        }

        fun bind(item: MediaItem, position: Int) {
            // 缩略图
            Glide.with(itemView)
                .load(item.uri)
                .placeholder(R.color.media_picker_placeholder)
                .centerCrop()
                .into(binding.ivThumbnail)

            // 视频时长标签
            when (item) {
                is MediaItem.Video -> {
                    binding.tvDuration.visibility = android.view.View.VISIBLE
                    binding.tvDuration.text = formatDuration(item.duration)
                }
                else -> {
                    binding.tvDuration.visibility = android.view.View.GONE
                }
            }

            updateSelection(item)
        }

        fun updateSelection(item: MediaItem) {
            val isSelected = selectionManager.isSelected(item)
            val index = selectionManager.selectedIndex(item)

            if (isSelected) {
                binding.layoutCheck.setBackgroundResource(R.drawable.bg_media_picker_check_selected)
                binding.tvCheckNum.visibility = android.view.View.VISIBLE
                binding.tvCheckNum.text = index.toString()
            } else {
                binding.layoutCheck.setBackgroundResource(R.drawable.bg_media_picker_check_unselected)
                binding.tvCheckNum.visibility = android.view.View.GONE
            }

            // 已满时降低未选项透明度
            itemView.alpha = if (!isSelected && selectionManager.isFull) 0.5f else 1.0f
        }
    }

    companion object {
        const val PAYLOAD_SELECTION = "selection"

        private fun formatDuration(ms: Long): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
