package app.allever.android.lib.media.picker.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import app.allever.android.lib.core.base.AbstractBindingActivity
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.R
import app.allever.android.lib.media.picker.databinding.ActivityMediaPreviewBinding
import app.allever.android.lib.media.picker.ui.adapter.PreviewAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 媒体预览 Activity（纯 View 层）
 *
 * 职责：
 * - UI 初始化与渲染（ViewPager2、音频栏、顶部栏、底部选中栏）
 * - 观察 ViewModel 的 StateFlow，数据变化时刷新界面
 * - 用户交互事件 → 委托给 ViewModel 处理
 *
 * 所有业务逻辑由 [MediaPreviewViewModel] 管理
 */
class MediaPreviewActivity : AbstractBindingActivity<ActivityMediaPreviewBinding>() {

    private val viewModel: MediaPreviewViewModel by viewModels()

    companion object {
        const val KEY_ITEMS = "preview_items"
        const val KEY_POSITION = "preview_position"
        const val KEY_MEDIA_TYPE = "preview_media_type"
        const val KEY_MAX_SELECT = "preview_max_select"
        const val KEY_SELECTED_IDS = "preview_selected_ids"
        const val KEY_RESULT = "preview_result"

        /** 静态持有者，避免 Intent 传递大数据导致 Binder 溢出 */
        private var previewData: PreviewData? = null

        private data class PreviewData(
            val items: List<MediaItem>,
            val position: Int,
            val mediaType: String,
            val maxSelect: Int,
            val selectedIds: Set<Long>,
        )

        fun setPreviewData(items: List<MediaItem>, position: Int, mediaType: String, maxSelect: Int, selectedIds: Set<Long>) {
            previewData = PreviewData(items, position, mediaType, maxSelect, selectedIds)
        }

        /** 取出并清除，避免内存泄漏 */
        private fun takePreviewData(): PreviewData? {
            return previewData?.also { previewData = null }
        }
    }

    override fun inflate() = ActivityMediaPreviewBinding.inflate(layoutInflater)

    override fun init() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 读取数据（优先静态持有者，兼容旧 Intent 方式）
        val data = takePreviewData()
        val items = data?.items
            ?: intent?.getParcelableArrayListExtra<MediaItem>(KEY_ITEMS)
            ?: return

        // 初始化 ViewModel（所有业务数据交给它管理）
        viewModel.init(
            items = items,
            position = data?.position ?: intent.getIntExtra(KEY_POSITION, 0),
            mediaType = data?.mediaType ?: intent.getStringExtra(KEY_MEDIA_TYPE) ?: "image",
            maxSelect = data?.maxSelect ?: intent.getIntExtra(KEY_MAX_SELECT, 9),
            selectedIds = data?.selectedIds
                ?: intent.getLongArrayExtra(KEY_SELECTED_IDS)?.toSet()
                ?: emptySet(),
        )

        setupUI()
        observeViewModel()
        adaptStatusBar(mBinding.layoutTopBar)
    }

    // ==================== UI 初始化（根据模式） ====================

    private fun setupUI() {
        if (viewModel.isAudioMode) {
            setupAudioMode()
        } else {
            setupImageVideoMode()
        }
    }

    /** 图片/视频模式：显示 ViewPager + 底部选中栏 */
    private fun setupImageVideoMode() {
        mBinding.layoutAudioBar.visibility = View.GONE
        mBinding.viewPagerPreview.visibility = View.VISIBLE
        mBinding.layoutBottomBar.visibility = View.VISIBLE

        setupViewPager()
        setupClickListeners()
    }

    /** 音频模式：显示音频播放栏 */
    private fun setupAudioMode() {
        mBinding.viewPagerPreview.visibility = View.GONE
        mBinding.layoutAudioBar.visibility = View.VISIBLE
        mBinding.layoutBottomBar.visibility = View.GONE

        // 初始化当前音频的 MediaPlayer
        val uri = viewModel.currentAudioUri()
        if (uri != null) {
            viewModel.prepareAudioWithContext(this, uri)
        }

        // 加载封面
        loadAudioCover()

        mBinding.ivBack.setOnClickListener { finishWithResult() }
        mBinding.tvSelectToggle.setOnClickListener { handleToggleSelection() }
        mBinding.btnPlayPause.setOnClickListener { viewModel.toggleAudioPlay() }
    }

    private fun setupViewPager() {
        val adapter = PreviewAdapter(
            items = viewModel.previewItems.value,
            lifecycleOwner = this,
            onItemClick = { finishWithResult() },
            onNavigateTo = { position -> mBinding.viewPagerPreview.setCurrentItem(position, true) },
        )
        mBinding.viewPagerPreview.adapter = adapter
        mBinding.viewPagerPreview.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.onPageSelected(position)
            }
        })
        mBinding.viewPagerPreview.setCurrentItem(viewModel.currentPosition.value, false)
    }

    private fun setupClickListeners() {
        mBinding.ivBack.setOnClickListener { finishWithResult() }
        mBinding.tvSelectToggle.setOnClickListener { handleToggleSelection() }
        mBinding.btnConfirm.setOnClickListener { finishWithResult() }
    }

    // ==================== 观察 ViewModel 状态（唯一的数据来源） ====================

    private fun observeViewModel() {
        // 顶部标题变化
        lifecycleScope.launch {
            viewModel.topBarTitle.collect { text ->
                mBinding.tvTitle.text = text
            }
        }

        // 选择按钮文字
        lifecycleScope.launch {
            viewModel.selectToggleText.collect { text ->
                mBinding.tvSelectToggle.text = text
            }
        }

        // 底部选中栏：显隐 + 渲染（由 selectCountText 驱动，确保每次选中变化都刷新）
        lifecycleScope.launch {
            viewModel.selectCountText.collect { text ->
                mBinding.tvSelectCount.text = text
                val hasSelection = viewModel.hasSelection.value
                mBinding.scrollSelected.visibility = if (hasSelection) View.VISIBLE else View.GONE
                if (hasSelection) renderSelectedPreview()
            }
        }

        // 音频模式专用状态
        if (viewModel.isAudioMode) {
            lifecycleScope.launch {
                viewModel.audioTitle.collect { text ->
                    mBinding.tvAudioTitle.text = text
                }
            }
            lifecycleScope.launch {
                viewModel.audioArtist.collect { text ->
                    mBinding.tvAudioArtist.text = text
                }
            }
            lifecycleScope.launch {
                viewModel.isAudioPlaying.collect { playing ->
                    mBinding.btnPlayPause.setImageResource(
                        if (playing) R.drawable.ic_media_picker_pause
                        else R.drawable.ic_media_picker_play
                    )
                }
            }
            lifecycleScope.launch {
                viewModel.audioProgressText.collect { text ->
                    mBinding.tvTitle.text = text.ifEmpty { viewModel.topBarTitle.value }
                }
            }
            // 自动播放下一首
            lifecycleScope.launch {
                viewModel.needNextAudioPrepare.collect { need ->
                    if (need) {
                        val uri = viewModel.currentAudioUri()
                        if (uri != null) {
                            viewModel.prepareAudioWithContext(this@MediaPreviewActivity, uri)
                            loadAudioCover()
                        }
                    }
                }
            }
        }
    }

    // ==================== 渲染方法（纯 UI 操作） ====================

    private fun renderSelectedPreview() {
        mBinding.layoutSelectedItems.removeAllViews()
        val inflater = layoutInflater
        viewModel.getSelectedList().forEachIndexed { index, item ->
            val view = inflater.inflate(R.layout.item_selected_preview, mBinding.layoutSelectedItems, false)

            view.findViewById<View>(R.id.tvIndex)?.let {
                (it as? TextView)?.text = "${index + 1}"
            }

            view.findViewById<View>(R.id.ivRemove)?.setOnClickListener {
                viewModel.removeSelection(item)
            }

            view.findViewById<ImageView>(R.id.ivThumbnail)?.let { iv ->
                when (item) {
                    is MediaItem.Image, is MediaItem.Video -> {
                        Glide.with(this)
                            .load(item.uri)
                            .placeholder(R.color.media_picker_placeholder)
                            .centerCrop()
                            .into(iv)
                    }
                    is MediaItem.Audio -> {
                        iv.setImageResource(R.drawable.ic_media_picker_audio)
                        iv.scaleType = ImageView.ScaleType.CENTER
                    }
                }
            }

            mBinding.layoutSelectedItems.addView(view)
        }
    }

    private fun loadAudioCover() {
        val albumId = viewModel.currentAudioAlbumId()
        if (albumId > 0) {
            val albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
            Glide.with(this)
                .load(albumArtUri)
                .placeholder(R.drawable.ic_media_picker_audio)
                .error(R.drawable.ic_media_picker_audio)
                .centerCrop()
                .into(mBinding.ivAudioCover)
        } else {
            mBinding.ivAudioCover.setImageResource(R.drawable.ic_media_picker_audio)
        }
    }

    // ==================== 交互事件处理 ====================

    private fun handleToggleSelection() {
        val toggled = viewModel.toggleSelection()
        if (!toggled && viewModel.isFullAndNotToggled()) {
            Toast.makeText(this, "最多选择${viewModel.maxSelect.value}个", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finishWithResult() {
        val result = Intent().apply {
            putParcelableArrayListExtra(KEY_RESULT, ArrayList(viewModel.getSelectedList()))
        }
        setResult(RESULT_OK, result)
        finish()
    }

    // ==================== 生命周期 ====================

    override fun onBackPressed() {
        finishWithResult()
    }

    override fun onDestroy() {
        super.onDestroy()
        // MediaPlayer 由 ViewModel.onCleared() 统一释放
    }
}
