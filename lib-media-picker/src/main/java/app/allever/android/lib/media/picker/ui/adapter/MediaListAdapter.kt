package app.allever.android.lib.media.picker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.R
import app.allever.android.lib.media.picker.databinding.ItemMediaListBinding
import app.allever.android.lib.media.picker.selection.SelectionManager
import com.bumptech.glide.Glide

/**
 * 音频列表 Adapter（用于音频展示）
 */
class MediaListAdapter(
    private val selectionManager: SelectionManager,
    private val onItemClick: (MediaItem.Audio, Int) -> Unit,
) : RecyclerView.Adapter<MediaListAdapter.ViewHolder>() {

    private val items = mutableListOf<MediaItem.Audio>()

    fun submitList(data: List<MediaItem.Audio>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun updateSelection() {
        notifyItemRangeChanged(0, itemCount, MediaGridAdapter.PAYLOAD_SELECTION)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(MediaGridAdapter.PAYLOAD_SELECTION)) {
            holder.updateSelection(items[position])
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemMediaListBinding) :
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
                    selectionManager.toggle(items[pos])
                    updateSelection(items[pos])
                }
            }
        }

        fun bind(item: MediaItem.Audio, position: Int) {
            binding.tvTitle.text = item.title.ifEmpty { item.name }
            val artistAlbum = buildString {
                if (item.artist.isNotEmpty()) append(item.artist)
                if (item.album.isNotEmpty()) {
                    if (isNotEmpty()) append(" · ")
                    append(item.album)
                }
            }
            binding.tvArtist.text = artistAlbum.ifEmpty { "未知艺术家" }
            binding.tvDuration.text = formatDuration(item.duration)

            updateSelection(item)
        }

        fun updateSelection(item: MediaItem.Audio) {
            val isSelected = selectionManager.isSelected(item)
            val index = selectionManager.selectedIndex(item)

            if (isSelected) {
                binding.layoutCheck.setBackgroundResource(R.drawable.bg_media_picker_check_selected_small)
                binding.tvCheckNum.visibility = android.view.View.VISIBLE
                binding.tvCheckNum.text = index.toString()
            } else {
                binding.layoutCheck.setBackgroundResource(R.drawable.bg_media_picker_check_unselected_small)
                binding.tvCheckNum.visibility = android.view.View.GONE
            }

            itemView.alpha = if (!isSelected && selectionManager.isFull) 0.5f else 1.0f
        }
    }

    companion object {
        private fun formatDuration(ms: Long): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
