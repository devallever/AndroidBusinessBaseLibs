package app.allever.android.lib.media.picker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.allever.android.lib.core.base.AbstractBindingFragment
import app.allever.android.lib.core.base.AbstractFragment
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.media.core.MediaCore
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.R
import app.allever.android.lib.media.picker.databinding.FragmentMediaPickerBinding
import app.allever.android.lib.media.picker.selection.SelectionManager
import app.allever.android.lib.media.picker.ui.adapter.MediaGridAdapter
import app.allever.android.lib.media.picker.ui.adapter.MediaListAdapter
import app.allever.android.lib.media.picker.ui.widget.FolderDrawerDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

/**
 * 媒体选择器主界面 Fragment
 *
 * 功能：
 * - Tab 切换媒体类型（图片/视频/音频）
 * - 目录栏点击弹出目录抽屉
 * - 图片/视频用网格展示，音频用列表展示
 * - 最多选中 N 个（默认9个）
 * - 底部已选预览条 + 确认按钮
 */
class MediaPickerFragment : AbstractBindingFragment<FragmentMediaPickerBinding>() {

    private lateinit var config: MediaPickerConfig
    private lateinit var selectionManager: SelectionManager

    private var currentType: MediaType.Type = MediaType.Type.IMAGE
    private var currentBucketId: Long? = null // null = 全部目录
    private var allFolders: List<MediaFolder> = emptyList()

    /** 当前目录下的图片列表 */
    private val images = mutableListOf<MediaItem.Image>()
    /** 当前目录下的视频列表 */
    private val videos = mutableListOf<MediaItem.Video>()
    /** 当前目录下的音频列表 */
    private val audios = mutableListOf<MediaItem.Audio>()

    private var gridAdapter: MediaGridAdapter? = null
    private var listAdapter: MediaListAdapter? = null
    private var folderDrawerDialog: FolderDrawerDialog? = null

    var onConfirm: ((List<MediaItem>) -> Unit)? = null

    override fun inflate() = FragmentMediaPickerBinding.inflate(layoutInflater)

    override fun init() {
        config = arguments?.getParcelable(MediaPickerConfig.KEY_CONFIG) ?: MediaPickerConfig()
        selectionManager = SelectionManager(config.maxSelect)

        setupTabs()
        setupDirectoryBar()
        setupRecyclerView()
        setupBottomBar()
        loadFolders()

        // 监听选中状态变化，更新 UI
        selectionManager.selectionChanged.observe(viewLifecycleOwner) {
            updateBottomBar()
            gridAdapter?.updateSelection()
            listAdapter?.updateSelection()
        }
    }

    // ==================== Tab 切换 ====================

    private fun setupTabs() {
        mBinding.tabLayoutType.removeAllTabs()
        if (config.hasImage) {
            mBinding.tabLayoutType.addTab(
                mBinding.tabLayoutType.newTab().setText(R.string.media_picker_tab_image)
            )
        }
        if (config.hasVideo) {
            mBinding.tabLayoutType.addTab(
                mBinding.tabLayoutType.newTab().setText(R.string.media_picker_tab_video)
            )
        }
        if (config.hasAudio) {
            mBinding.tabLayoutType.addTab(
                mBinding.tabLayoutType.newTab().setText(R.string.media_picker_tab_audio)
            )
        }

        // 默认选中第一个 Tab
        if (mBinding.tabLayoutType.tabCount > 0) {
            currentType = when (mBinding.tabLayoutType.getTabAt(0)?.text.toString()) {
                getString(R.string.media_picker_tab_image) -> MediaType.Type.IMAGE
                getString(R.string.media_picker_tab_video) -> MediaType.Type.VIDEO
                else -> MediaType.Type.AUDIO
            }
        }

        mBinding.tabLayoutType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { onTypeTabChanged(it.position) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun onTypeTabChanged(position: Int) {
        // 根据 Tab position 确定类型
        var idx = 0
        if (config.hasImage && position == idx++) {
            switchToType(MediaType.Type.IMAGE)
        } else if (config.hasVideo && position == idx++) {
            switchToType(MediaType.Type.VIDEO)
        } else {
            switchToType(MediaType.Type.AUDIO)
        }
    }

    private fun switchToType(type: MediaType.Type) {
        if (currentType == type) return
        currentType = type
        refreshContent()
    }

    // ==================== 目录栏 ====================

    private fun setupDirectoryBar() {
        mBinding.layoutDirectoryBar.setOnClickListener {
            showFolderDrawer()
        }
    }

    private fun updateDirectoryBar(folderName: String) {
        mBinding.tvDirectoryName.text = folderName
    }

    private fun showFolderDrawer() {
        val dialog = folderDrawerDialog ?: FolderDrawerDialog(requireContext()).also { folderDrawerDialog = it }

        // 在第一项插入"全部目录"，取最新资源作为封面
        val allCoverUri = (videos.firstOrNull()?.uri ?: images.firstOrNull()?.uri ?: audios.firstOrNull()?.uri)
        val allFolderItem = MediaFolder(
            bucketId = -1L,
            name = getString(R.string.media_picker_all_folders),
            path = "",
            coverUri = allCoverUri,
            images = images.toList(),
            videos = videos.toList(),
            audios = audios.toList(),
        )
        val displayFolders = listOf(allFolderItem) + allFolders

        dialog.showWithFolders(
            displayFolders,
            currentBucketId ?: -1L,
        ) { folder ->
            if (folder.bucketId == -1L) {
                // 全部目录
                currentBucketId = null
                updateDirectoryBar(getString(R.string.media_picker_all_folders))
                loadAllItems()
            } else {
                currentBucketId = folder.bucketId
                updateDirectoryBar(folder.name)
                loadFolderDetail()
            }
        }
    }

    // ==================== RecyclerView ====================

    private fun setupRecyclerView() {
        // 图片/视频：网格布局（3列）
        gridAdapter = MediaGridAdapter(selectionManager) { item, _ ->
            handleItemClick(item)
        }
        mBinding.recyclerViewMedia.layoutManager = GridLayoutManager(context, 3)
        mBinding.recyclerViewMedia.adapter = gridAdapter

        // 音频：列表布局
        listAdapter = MediaListAdapter(selectionManager) { item, _ ->
            handleItemClick(item)
        }
    }

    /**
     * 根据当前媒体类型切换 RecyclerView 的 Adapter 和 LayoutManager
     */
    private fun refreshContent() {
        when (currentType) {
            MediaType.Type.IMAGE, MediaType.Type.VIDEO -> {
                mBinding.recyclerViewMedia.layoutManager = GridLayoutManager(context, 3)
                mBinding.recyclerViewMedia.adapter = gridAdapter
                val items = if (currentType == MediaType.Type.IMAGE) images.map { it } else videos.map { it }
                gridAdapter?.submitList(items)
            }
            MediaType.Type.AUDIO -> {
                mBinding.recyclerViewMedia.layoutManager = LinearLayoutManager(context)
                mBinding.recyclerViewMedia.adapter = listAdapter
                listAdapter?.submitList(audios)
            }
        }
        updateEmptyVisibility()
    }

    private fun updateEmptyVisibility() {
        val isEmpty = when (currentType) {
            MediaType.Type.IMAGE -> images.isEmpty()
            MediaType.Type.VIDEO -> videos.isEmpty()
            MediaType.Type.AUDIO -> audios.isEmpty()
        }
        setVisibility(mBinding.layoutEmpty, isEmpty)
        setVisibility(mBinding.recyclerViewMedia, !isEmpty)
    }

    // ==================== 底部操作栏 ====================

    private fun setupBottomBar() {
        mBinding.btnConfirm.setOnClickListener {
            val selected = selectionManager.toList()
            if (selected.isNotEmpty()) {
                onConfirm?.invoke(selected)
            }
        }
        updateBottomBar()
    }

    private fun updateBottomBar() {
        val count = selectionManager.selectedCount
        mBinding.tvSelectCount.text =
            getString(R.string.media_picker_selected_format, count, config.maxSelect)

        // 更新底部预览条
        if (count > 0) {
            mBinding.scrollSelected.visibility = View.VISIBLE
            renderSelectedPreview()
        } else {
            mBinding.scrollSelected.visibility = View.GONE
        }
    }

    private fun renderSelectedPreview() {
        mBinding.layoutSelectedItems.removeAllViews()
        val inflater = LayoutInflater.from(context)
        selectionManager.toList().forEachIndexed { index, item ->
            val view = inflater.inflate(R.layout.item_selected_preview, mBinding.layoutSelectedItems, false)
            // TODO: 用 Glide 加载缩略图
            view.findViewById<View>(R.id.tvIndex)?.let {
                (it as? android.widget.TextView)?.text = "${index + 1}"
            }
            view.findViewById<View>(R.id.ivRemove)?.setOnClickListener {
                selectionManager.remove(item)
            }
            mBinding.layoutSelectedItems.addView(view)
        }
    }

    // ==================== 数据加载 ====================

    private fun loadFolders() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                allFolders = MediaCore.queryFolders {
                    types = config.types
                    pagination = app.allever.android.lib.media.core.model.Pagination.All
                }
                log("MediaPicker", "loadFolders → ${allFolders.size} 个目录")

                // 默认选中"全部目录"
                updateDirectoryBar(getString(R.string.media_picker_all_folders))

                // 加载全部目录的资源
                loadAllItems()
            } catch (e: Exception) {
                log("MediaPicker", "loadFolders error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun loadFolderDetail() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val bucketId = currentBucketId ?: return@launch
                val detail = MediaCore.queryFolderDetail {
                    this.bucketId = bucketId
                    types = config.types
                    pagination = app.allever.android.lib.media.core.model.Pagination.All
                }
                images.clear(); images.addAll(detail.images)
                videos.clear(); videos.addAll(detail.videos)
                audios.clear(); audios.addAll(detail.audios)
                log("MediaPicker", "loadFolderDetail → img=${images.size} vid=${videos.size} aud=${audios.size}")
                refreshContent()
            } catch (e: Exception) {
                log("MediaPicker", "loadFolderDetail error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun loadAllItems() {
        lifecycleScope.launch {
            try {
                // 按类型分别查询全部资源
                if (config.types.contains(MediaType.Type.IMAGE)) {
                    val imgItems = MediaCore.queryAll {
                        types = setOf(MediaType.Type.IMAGE)
                        pagination = app.allever.android.lib.media.core.model.Pagination.All
                    }
                    images.clear()
                    imgItems.filterIsInstance<MediaItem.Image>().forEach { images.add(it) }
                }
                if (config.types.contains(MediaType.Type.VIDEO)) {
                    val vidItems = MediaCore.queryAll {
                        types = setOf(MediaType.Type.VIDEO)
                        pagination = app.allever.android.lib.media.core.model.Pagination.All
                    }
                    videos.clear()
                    vidItems.filterIsInstance<MediaItem.Video>().forEach { videos.add(it) }
                }
                if (config.types.contains(MediaType.Type.AUDIO)) {
                    val audItems = MediaCore.queryAll {
                        types = setOf(MediaType.Type.AUDIO)
                        pagination = app.allever.android.lib.media.core.model.Pagination.All
                    }
                    audios.clear()
                    audItems.filterIsInstance<MediaItem.Audio>().forEach { audios.add(it) }
                }
                log("MediaPicker", "loadAllItems → img=${images.size} vid=${videos.size} aud=${audios.size}")
                refreshContent()
            } catch (e: Exception) {
                log("MediaPicker", "loadAllItems error: ${e.message}")
            }
        }
    }

    // ==================== 交互处理 ====================

    private fun handleItemClick(item: MediaItem) {
        // 切换选中状态
        val toggled = selectionManager.toggle(item)
        if (!toggled && selectionManager.isFull) {
            // 已满提示
            context?.let {
                android.widget.Toast.makeText(
                    it,
                    getString(R.string.media_picker_max_select, config.maxSelect),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        setVisibility(mBinding.progressBarLoading, show)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gridAdapter = null
        listAdapter = null
    }

    companion object {
        fun newInstance(config: MediaPickerConfig): MediaPickerFragment {
            return MediaPickerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(MediaPickerConfig.KEY_CONFIG, config)
                }
            }
        }
    }
}
