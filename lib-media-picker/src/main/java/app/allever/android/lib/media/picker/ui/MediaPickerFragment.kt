package app.allever.android.lib.media.picker.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import app.allever.android.lib.core.base.AbstractBindingFragment
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 媒体选择器主界面 Fragment（纯 View 层）
 *
 * 职责：
 * - UI 初始化与渲染（Tab、ViewPager2、目录栏、底部栏）
 * - 观察 ViewModel 的 LiveData/StateFlow，数据变化时刷新界面
 * - 用户交互事件 → 委托给 ViewModel 处理
 *
 * 不包含任何业务逻辑，所有状态由 [MediaPickerViewModel] 管理
 */
class MediaPickerFragment : AbstractBindingFragment<FragmentMediaPickerBinding>() {

    private val viewModel: MediaPickerViewModel by activityViewModels()

    /** ViewPager2 页面 Adapter */
    private lateinit var pageAdapter: MediaPageAdapter
    private var folderDrawerDialog: FolderDrawerDialog? = null

    /** 预览结果回调 */
    private val previewLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val items = result.data?.getParcelableArrayListExtra<MediaItem>(MediaPreviewActivity.KEY_RESULT)
            if (!items.isNullOrEmpty()) {
                viewModel.selectionManager.clear()
                items.forEach { viewModel.selectionManager.forceAdd(it) }
            }
        }
    }

    var onConfirm: ((List<MediaItem>) -> Unit)? = null

    override fun inflate() = FragmentMediaPickerBinding.inflate(layoutInflater)

    override fun init() {
        val config = arguments?.getParcelable(MediaPickerConfig.KEY_CONFIG) ?: MediaPickerConfig()
        val selectionManager = SelectionManager(config.maxSelect)
        viewModel.init(config, selectionManager)

        setupTabs()
        setupDirectoryBar()
        setupViewPager()
        setupBottomBar()
        observeViewModel()

        // 触发数据加载（由 ViewModel 负责）
        viewModel.loadFolders()
    }

    // ==================== 观察 ViewModel 状态（唯一的数据来源） ====================

    private fun observeViewModel() {
        // 选中状态变化 → 更新底部栏 + 刷新 Adapter
        viewModel.selectionManager.selectionChanged.observe(viewLifecycleOwner) {
            updateBottomBar()
            pageAdapter.updateAllSelection()
        }

        // 加载中状态
        lifecycleScope.launch {
            viewModel.loading.collect { show -> showLoading(show) }
        }

        // 数据变化 → 刷新当前页 ViewPager 内容
        lifecycleScope.launch {
            launch { viewModel.images.collect { refreshCurrentPageData() } }
            launch { viewModel.videos.collect { refreshCurrentPageData() } }
            launch { viewModel.audios.collect { refreshCurrentPageData() } }
        }

        // 目录名称变化 → 更新目录栏文字
        lifecycleScope.launch {
            viewModel.directoryName.collect { name ->
                mBinding.tvDirectoryName.text = name
            }
        }
    }

    // ==================== Tab 设置（纯 UI 初始化） ====================

    private fun setupTabs() {
        mBinding.tabLayoutType.removeAllTabs()
        for (type in viewModel.tabTypes) {
            val textRes = when (type) {
                MediaType.Type.IMAGE -> R.string.media_picker_tab_image
                MediaType.Type.VIDEO -> R.string.media_picker_tab_video
                MediaType.Type.AUDIO -> R.string.media_picker_tab_audio
            }
            mBinding.tabLayoutType.addTab(mBinding.tabLayoutType.newTab().setText(textRes))
        }
    }

    // ==================== ViewPager2 设置（纯 UI 初始化） ====================

    private fun setupViewPager() {
        pageAdapter = MediaPageAdapter(viewModel.selectionManager) { item ->
            handleItemClick(item)
        }
        pageAdapter.pageTypes.addAll(viewModel.tabTypes)

        mBinding.viewPagerContent.adapter = pageAdapter
        mBinding.viewPagerContent.offscreenPageLimit = viewModel.tabTypes.size.coerceAtMost(3)

        TabLayoutMediator(mBinding.tabLayoutType, mBinding.viewPagerContent) { tab, position ->
            tab.text = when (viewModel.tabTypes[position]) {
                MediaType.Type.IMAGE -> getString(R.string.media_picker_tab_image)
                MediaType.Type.VIDEO -> getString(R.string.media_picker_tab_video)
                MediaType.Type.AUDIO -> getString(R.string.media_picker_tab_audio)
            }
        }.attach()

        mBinding.viewPagerContent.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.switchTab(position)
                refreshCurrentPageData()
            }
        })

        mBinding.viewPagerContent.setCurrentItem(0, false)
    }

    /** 从 ViewModel 读取当前页数据并提交给 Adapter */
    private fun refreshCurrentPageData() {
        val items = when (viewModel.currentType) {
            MediaType.Type.IMAGE -> viewModel.images.value.map { it }
            MediaType.Type.VIDEO -> viewModel.videos.value.map { it }
            MediaType.Type.AUDIO -> viewModel.audios.value.map { it }
        }
        pageAdapter.submitPageData(viewModel.currentTabPosition.value, items)
    }

    // ==================== 目录栏（纯 UI：点击打开抽屉） ====================

    private fun setupDirectoryBar() {
        mBinding.layoutDirectoryBar.setOnClickListener {
            showFolderDrawer()
        }
    }

    /** 显示目录抽屉，数据全部来自 ViewModel */
    private fun showFolderDrawer() {
        val dialog = folderDrawerDialog ?: FolderDrawerDialog(requireContext()).also { folderDrawerDialog = it }

        // displayFolders 由 ViewModel 实时计算（全部目录 + 原始目录列表）
        dialog.showWithFolders(
            viewModel.displayFolders,
            viewModel.currentBucketId.value ?: MediaPickerViewModel.ALL_FOLDERS_ID,
        ) { folder ->
            // 直接委托给 ViewModel，不在此做任何判断
            if (folder.bucketId == MediaPickerViewModel.ALL_FOLDERS_ID) {
                viewModel.selectAllFolders()
            } else {
                viewModel.selectFolder(folder)
            }
        }
    }

    // ==================== 底部操作栏（纯 UI 渲染 + 事件转发） ====================

    private fun setupBottomBar() {
        mBinding.btnConfirm.setOnClickListener {
            val selected = viewModel.selectionManager.toList()
            if (selected.isNotEmpty()) {
                onConfirm?.invoke(selected)
            }
        }
        updateBottomBar()
    }

    /** 根据 selectionManager 状态渲染底部栏 */
    private fun updateBottomBar() {
        val count = viewModel.selectionManager.selectedCount
        mBinding.tvSelectCount.text =
            getString(R.string.media_picker_selected_format, count, viewModel.config.maxSelect)

        if (count > 0) {
            mBinding.scrollSelected.visibility = View.VISIBLE
            renderSelectedPreview()
        } else {
            mBinding.scrollSelected.visibility = View.GONE
        }
    }

    /** 渲染已选预览缩略图 */
    private fun renderSelectedPreview() {
        mBinding.layoutSelectedItems.removeAllViews()
        val inflater = LayoutInflater.from(context)
        viewModel.selectionManager.toList().forEachIndexed { index, item ->
            val view = inflater.inflate(R.layout.item_selected_preview, mBinding.layoutSelectedItems, false)

            view.findViewById<View>(R.id.tvIndex)?.let {
                (it as? android.widget.TextView)?.text = "${index + 1}"
            }

            view.findViewById<View>(R.id.ivRemove)?.setOnClickListener {
                viewModel.selectionManager.remove(item)
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

    // ==================== 交互事件（委托 ViewModel） ====================

    private fun handleItemClick(item: MediaItem) {
        openPreview(item)
    }

    /** 打开预览，参数全部从 ViewModel 获取 */
    private fun openPreview(item: MediaItem) {
        val items = viewModel.getCurrentPageItems()
        val position = items.indexOf(item).coerceAtLeast(0)
        val selectedIds = viewModel.selectionManager.toList().map { it.id }.toSet()

        MediaPreviewActivity.setPreviewData(items, position, viewModel.mediaTypeString, viewModel.config.maxSelect, selectedIds)

        val intent = Intent(requireContext(), MediaPreviewActivity::class.java)
        previewLauncher.launch(intent)
    }

    // ==================== UI 工具方法 ====================

    private fun showLoading(show: Boolean) {
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
