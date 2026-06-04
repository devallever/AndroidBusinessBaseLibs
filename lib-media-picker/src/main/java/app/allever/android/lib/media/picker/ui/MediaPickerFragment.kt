package app.allever.android.lib.media.picker.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import app.allever.android.lib.core.base.AbstractBindingFragment
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.media.core.MediaCore
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.R
import app.allever.android.lib.media.picker.databinding.FragmentMediaPickerBinding
import app.allever.android.lib.media.picker.selection.SelectionManager
import app.allever.android.lib.media.picker.ui.adapter.MediaPageAdapter
import app.allever.android.lib.media.picker.ui.widget.FolderDrawerDialog
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

/**
 * 媒体选择器主界面 Fragment
 *
 * 功能：
 * - Tab 切换媒体类型（图片/视频/音频），支持左右滑动切换
 * - 目录栏点击弹出目录抽屉
 * - 图片/视频用网格展示，音频用列表展示
 * - 最多选中 N 个（默认9个）
 * - 底部已选预览条 + 确认按钮
 */
class MediaPickerFragment : AbstractBindingFragment<FragmentMediaPickerBinding>() {

    private lateinit var config: MediaPickerConfig
    private lateinit var selectionManager: SelectionManager

    /** 当前可用的媒体类型列表（与 Tab 一一对应） */
    private val tabTypes = mutableListOf<MediaType.Type>()
    /** 当前选中的 Tab 索引 */
    private var currentTabPosition: Int = 0
    /** 当前媒体类型 */
    private var currentType: MediaType.Type = MediaType.Type.IMAGE

    private var currentBucketId: Long? = null // null = 全部目录
    private var allFolders: List<MediaFolder> = emptyList()

    /** 各类型的数据 */
    private val images = mutableListOf<MediaItem.Image>()
    private val videos = mutableListOf<MediaItem.Video>()
    private val audios = mutableListOf<MediaItem.Audio>()

    /** ViewPager2 页面 Adapter */
    private lateinit var pageAdapter: MediaPageAdapter
    private var folderDrawerDialog: FolderDrawerDialog? = null

    /** 预览结果回调 */
    private val previewLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val items = result.data?.getParcelableArrayListExtra<MediaItem>(MediaPreviewActivity.KEY_RESULT)
            if (!items.isNullOrEmpty()) {
                selectionManager.clear()
                items.forEach { selectionManager.forceAdd(it) }
            }
        }
    }

    var onConfirm: ((List<MediaItem>) -> Unit)? = null

    override fun inflate() = FragmentMediaPickerBinding.inflate(layoutInflater)

    override fun init() {
        config = arguments?.getParcelable(MediaPickerConfig.KEY_CONFIG) ?: MediaPickerConfig()
        selectionManager = SelectionManager(config.maxSelect)

        buildTabTypes()
        setupTabs()
        setupDirectoryBar()
        setupViewPager()
        setupBottomBar()
        loadFolders()

        // 监听选中状态变化，更新 UI
        selectionManager.selectionChanged.observe(viewLifecycleOwner) {
            updateBottomBar()
            pageAdapter.updateAllSelection()
        }
    }

    // ==================== Tab 类型构建 ====================

    private fun buildTabTypes() {
        tabTypes.clear()
        if (config.hasImage) tabTypes.add(MediaType.Type.IMAGE)
        if (config.hasVideo) tabTypes.add(MediaType.Type.VIDEO)
        if (config.hasAudio) tabTypes.add(MediaType.Type.AUDIO)
        currentType = tabTypes.firstOrNull() ?: MediaType.Type.IMAGE
    }

    // ==================== Tab 设置 ====================

    private fun setupTabs() {
        mBinding.tabLayoutType.removeAllTabs()
        for (type in tabTypes) {
            val textRes = when (type) {
                MediaType.Type.IMAGE -> R.string.media_picker_tab_image
                MediaType.Type.VIDEO -> R.string.media_picker_tab_video
                MediaType.Type.AUDIO -> R.string.media_picker_tab_audio
            }
            mBinding.tabLayoutType.addTab(mBinding.tabLayoutType.newTab().setText(textRes))
        }

        // TabLayout 与 ViewPager2 联动在 setupViewPager 中通过 TabLayoutMediator 完成
    }

    // ==================== ViewPager2 ====================

    private fun setupViewPager() {
        pageAdapter = MediaPageAdapter(selectionManager) { item ->
            handleItemClick(item)
        }
        // 每页对应的类型
        pageAdapter.pageTypes.addAll(tabTypes)

        mBinding.viewPagerContent.adapter = pageAdapter
        mBinding.viewPagerContent.offscreenPageLimit = tabTypes.size.coerceAtMost(3)

        // TabLayout 与 ViewPager2 联动：滑动切换 Tab，点击 Tab 切换页面
        TabLayoutMediator(mBinding.tabLayoutType, mBinding.viewPagerContent) { tab, position ->
            tab.text = when (tabTypes[position]) {
                MediaType.Type.IMAGE -> getString(R.string.media_picker_tab_image)
                MediaType.Type.VIDEO -> getString(R.string.media_picker_tab_video)
                MediaType.Type.AUDIO -> getString(R.string.media_picker_tab_audio)
            }
        }.attach()

        mBinding.viewPagerContent.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentTabPosition = position
                currentType = tabTypes[position]
                refreshCurrentPageData()
            }
        })

        // 默认显示第一页
        mBinding.viewPagerContent.setCurrentItem(0, false)
    }

    /** 根据当前 Tab 位置刷新对应页的数据 */
    private fun refreshCurrentPageData() {
        val type = currentType
        val items = when (type) {
            MediaType.Type.IMAGE -> images.map { it }
            MediaType.Type.VIDEO -> videos.map { it }
            MediaType.Type.AUDIO -> audios.map { it }
        }
        pageAdapter.submitPageData(currentTabPosition, items)
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

            view.findViewById<View>(R.id.tvIndex)?.let {
                (it as? android.widget.TextView)?.text = "${index + 1}"
            }

            view.findViewById<View>(R.id.ivRemove)?.setOnClickListener {
                selectionManager.remove(item)
            }

            val ivThumbnail = view.findViewById<ImageView>(R.id.ivThumbnail)
            when (item) {
                is MediaItem.Image, is MediaItem.Video -> {
                    Glide.with(this)
                        .load(item.uri)
                        .placeholder(R.color.media_picker_placeholder)
                        .centerCrop()
                        .error(R.color.media_picker_placeholder)
                        .into(ivThumbnail)
                }
                is MediaItem.Audio -> {
                    ivThumbnail.setImageResource(R.drawable.ic_media_picker_audio)
                    ivThumbnail.scaleType = ImageView.ScaleType.CENTER
                }
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

                updateDirectoryBar(getString(R.string.media_picker_all_folders))
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
                refreshCurrentPageData()
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
                refreshCurrentPageData()
            } catch (e: Exception) {
                log("MediaPicker", "loadAllItems error: ${e.message}")
            }
        }
    }

    // ==================== 交互处理 ====================

    private fun handleItemClick(item: MediaItem) {
        openPreview(item)
    }

    /**
     * 打开媒体预览
     */
    private fun openPreview(item: MediaItem) {
        val items = when (currentType) {
            MediaType.Type.IMAGE -> images.map { it }
            MediaType.Type.VIDEO -> videos.map { it }
            MediaType.Type.AUDIO -> audios.map { it }
        }
        val position = items.indexOf(item).coerceAtLeast(0)
        val mediaType = when (currentType) {
            MediaType.Type.IMAGE -> "image"
            MediaType.Type.VIDEO -> "video"
            MediaType.Type.AUDIO -> "audio"
        }
        val selectedIds = selectionManager.toList().map { it.id }.toSet()

        MediaPreviewActivity.setPreviewData(items, position, mediaType, config.maxSelect, selectedIds)

        val intent = Intent(requireContext(), MediaPreviewActivity::class.java)
        previewLauncher.launch(intent)
    }

    private fun showLoading(show: Boolean) {
        // 遍历 ViewPager2 所有已创建的页面，更新 loading 状态
        for (i in 0 until mBinding.viewPagerContent.childCount) {
            val child = mBinding.viewPagerContent.getChildAt(i)
            child.findViewById<View>(R.id.progressBarLoading)?.visibility =
                if (show) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        folderDrawerDialog = null
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
