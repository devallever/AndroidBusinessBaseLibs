package app.allever.android.lib.media.picker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.picker.databinding.ItemMediaPageBinding
import app.allever.android.lib.media.picker.selection.SelectionManager

/**
 * ViewPager2 页面 Adapter，每页对应一个媒体类型 Tab
 * - 图片/视频：Grid 3列
 * - 音频：List 列表
 */
class MediaPageAdapter(
    private val selectionManager: SelectionManager,
    private val onItemClick: (MediaItem) -> Unit,
) : RecyclerView.Adapter<MediaPageAdapter.PageViewHolder>() {

    /** 每页对应的媒体类型列表（与 Tab 一一对应） */
    val pageTypes = mutableListOf<MediaType.Type>()

    private val gridAdapters = mutableMapOf<Int, MediaGridAdapter>()
    private val listAdapters = mutableMapOf<Int, MediaListAdapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemMediaPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(pageTypes[position], position)
    }

    override fun getItemCount() = pageTypes.size

    /** 更新指定页的数据 */
    fun submitPageData(position: Int, items: List<MediaItem>) {
        if (position !in 0 until pageTypes.size) return
        val type = pageTypes[position]
        when (type) {
            MediaType.Type.IMAGE, MediaType.Type.VIDEO -> {
                gridAdapters[position]?.submitList(items)
            }
            MediaType.Type.AUDIO -> {
                listAdapters[position]?.submitList(items.filterIsInstance<MediaItem.Audio>())
            }
        }
    }

    /** 更新指定页的选中状态 */
    fun updateSelection(position: Int) {
        gridAdapters[position]?.updateSelection()
        listAdapters[position]?.updateSelection()
    }

    fun updateAllSelection() {
        gridAdapters.values.forEach { it.updateSelection() }
        listAdapters.values.forEach { it.updateSelection() }
    }

    inner class PageViewHolder(
        private val binding: ItemMediaPageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(type: MediaType.Type, position: Int) {
            when (type) {
                MediaType.Type.IMAGE, MediaType.Type.VIDEO -> {
                    binding.recyclerViewMedia.layoutManager = GridLayoutManager(binding.root.context, 3)
                    val adapter = MediaGridAdapter(selectionManager) { item, _ ->
                        onItemClick(item)
                    }
                    gridAdapters[position] = adapter
                    binding.recyclerViewMedia.adapter = adapter
                }
                MediaType.Type.AUDIO -> {
                    binding.recyclerViewMedia.layoutManager = LinearLayoutManager(binding.root.context)
                    val adapter = MediaListAdapter(selectionManager) { item, _ ->
                        onItemClick(item)
                    }
                    listAdapters[position] = adapter
                    binding.recyclerViewMedia.adapter = adapter
                }
            }
        }
    }
}
