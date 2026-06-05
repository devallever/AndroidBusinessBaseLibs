package app.allever.android.lib.media.picker.ui

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import app.allever.android.lib.core.base.AbstractBindingActivity
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.R
import app.allever.android.lib.media.picker.databinding.ActivityMediaPreviewBinding
import app.allever.android.lib.media.picker.selection.SelectionManager
import app.allever.android.lib.media.picker.ui.adapter.PreviewAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 媒体预览 Activity
 *
 * 功能：
 * - 图片：ViewPager2 滑动浏览 + PhotoView 手势缩放
 * - 视频：ViewPager2 内 VideoView 播放 + 控制栏（进度条、播放/暂停、上下切换）
 * - 音频：底部播放栏，支持播放/暂停/进度
 * - 选中/取消选中当前项
 */
class MediaPreviewActivity : AbstractBindingActivity<ActivityMediaPreviewBinding>() {
    
    private lateinit var selectionManager: SelectionManager
    private var maxSelect: Int = 9
    private lateinit var mediaType: String // "image", "video", "audio"

    /** 当前展示的媒体列表（用于图片/视频滑动切换） */
    private val previewItems = mutableListOf<MediaItem>()
    /** 当前位置 */
    private var currentPosition: Int = 0

    /** 音频播放器 */
    private var mediaPlayer: MediaPlayer? = null
    private var isAudioPlaying = false
    private var audioUpdateJob: kotlinx.coroutines.Job? = null

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
        mBinding = ActivityMediaPreviewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // 优先从静态持有者取数据（避免 Binder 溢出），兼容旧方式
        val data = takePreviewData()
        val items = data?.items
            ?: intent?.getParcelableArrayListExtra<MediaItem>(KEY_ITEMS)
            ?: return

        currentPosition = data?.position ?: intent.getIntExtra(KEY_POSITION, 0)
        mediaType = data?.mediaType ?: intent.getStringExtra(KEY_MEDIA_TYPE) ?: "image"
        maxSelect = data?.maxSelect ?: intent.getIntExtra(KEY_MAX_SELECT, 9)
        val selectedIds = data?.selectedIds
            ?: intent.getLongArrayExtra(KEY_SELECTED_IDS)?.toSet()
            ?: emptySet()

        selectionManager = SelectionManager(maxSelect)

        // 恢复已选中状态
        for (item in items) {
            if (selectedIds.contains(item.id)) {
                selectionManager.forceAdd(item)
            }
        }

        when (mediaType) {
            "audio" -> setupAudioMode(items)
            else -> setupImageVideoMode(items)
        }
    }

    // ==================== 图片/视频模式 ====================

    private fun setupImageVideoMode(items: List<MediaItem>) {
        previewItems.clear()
        previewItems.addAll(items)

        // 隐藏音频栏，显示 ViewPager 和底部选中栏
        mBinding.layoutAudioBar.visibility = View.GONE
        mBinding.viewPagerPreview.visibility = View.VISIBLE
        mBinding.layoutBottomBar.visibility = View.VISIBLE

        setupViewPager()
        updateTopBar()
        updateBottomBar()
        setupClickListeners()
    }

    private fun setupViewPager() {
        val adapter = PreviewAdapter(
            items = previewItems,
            lifecycleOwner = this,
            onItemClick = { finishWithResult() },
            onNavigateTo = { position -> mBinding.viewPagerPreview.setCurrentItem(position, true) },
        )
        mBinding.viewPagerPreview.adapter = adapter
        mBinding.viewPagerPreview.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPosition = position
                updateTopBar()
            }
        })
        mBinding.viewPagerPreview.setCurrentItem(currentPosition, false)
    }

    // ==================== 音频模式 ====================

    private fun setupAudioMode(items: List<MediaItem>) {
        if (items.isEmpty()) { finish(); return }

        val audioItem = items[currentPosition] as? MediaItem.Audio ?: run { finish(); return }
        previewItems.clear()
        previewItems.addAll(items)

        // 显示音频栏，隐藏其他
        mBinding.viewPagerPreview.visibility = View.GONE
        mBinding.layoutAudioBar.visibility = View.VISIBLE
        mBinding.layoutBottomBar.visibility = View.GONE

        // 更新顶部标题
        mBinding.tvTitle.text = buildString {
            append(audioItem.title.ifEmpty { audioItem.name })
            if (items.size > 1) append(" (${currentPosition + 1}/${items.size})")
        }

        // 更新音频信息
        mBinding.tvAudioTitle.text = audioItem.title.ifEmpty { audioItem.name }
        mBinding.tvAudioArtist.text = buildString {
            if (audioItem.artist.isNotEmpty()) append(audioItem.artist)
            if (audioItem.album.isNotEmpty()) {
                if (isNotEmpty()) append(" · ")
                append(audioItem.album)
            }
        }.ifEmpty { "未知艺术家" }

        loadAudioCover(audioItem)
        initMediaPlayer(audioItem.uri)
        updateSelectToggle()

        mBinding.ivBack.setOnClickListener { finishWithResult() }
        mBinding.tvSelectToggle.setOnClickListener { toggleSelection(previewItems[currentPosition]) }
        mBinding.btnPlayPause.setOnClickListener { toggleAudioPlay() }
    }

    private fun initMediaPlayer(uri: Uri) {
        releaseMediaPlayer()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MediaPreviewActivity, uri)
                setOnPreparedListener {
                    log("MediaPreview", "音频准备完成")
                    it.start()
                    isAudioPlaying = true
                    updatePlayButton()
                    startProgressUpdate()
                }
                setOnCompletionListener {
                    isAudioPlaying = false
                    updatePlayButton()
                    audioUpdateJob?.cancel()
                    // 播放下一个（如果有）
                    if (this@MediaPreviewActivity.currentPosition < previewItems.size - 1) {
                        this@MediaPreviewActivity.currentPosition = this@MediaPreviewActivity.currentPosition + 1
                        val nextItem = previewItems[this@MediaPreviewActivity.currentPosition] as? MediaItem.Audio
                        if (nextItem != null) {
                            setupAudioMode(previewItems.toList())
                        }
                    }
                }
                setOnErrorListener { _, what, extra ->
                    logE("MediaPreview", "播放错误: what=$what extra=$extra")
                    isAudioPlaying = false
                    updatePlayButton()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            logE("MediaPreview", "初始化播放器失败: ${e.message}")
        }
    }

    private fun toggleAudioPlay() {
        val player = mediaPlayer ?: return
        if (isAudioPlaying) {
            player.pause()
            isAudioPlaying = false
            audioUpdateJob?.cancel()
        } else {
            player.start()
            isAudioPlaying = true
            startProgressUpdate()
        }
        updatePlayButton()
    }

    private fun updatePlayButton() {
        mBinding.btnPlayPause.setImageResource(
            if (isAudioPlaying) R.drawable.ic_media_picker_pause
            else R.drawable.ic_media_picker_play
        )
    }

    private fun startProgressUpdate() {
        audioUpdateJob?.cancel()
        audioUpdateJob = lifecycleScope.launch {
            while (isActive && isAudioPlaying) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val current = player.currentPosition
                        val duration = player.duration
                        if (duration > 0) {
                            mBinding.tvTitle.text = formatTime(current) + " / " + formatTime(duration)
                        }
                    }
                }
                delay(500)
            }
        }
    }

    // ==================== UI 更新 ====================

    private fun updateTopBar() {
        mBinding.tvTitle.text = "${currentPosition + 1}/${previewItems.size}"
        updateSelectToggle()
    }

    private fun updateSelectToggle() {
        val item = previewItems.getOrNull(currentPosition) ?: return
        val isSelected = selectionManager.isSelected(item)
        mBinding.tvSelectToggle.text = if (isSelected) "取消选择" else "选择"
    }

    private fun updateBottomBar() {
        val count = selectionManager.selectedCount
        if (count > 0) {
            mBinding.scrollSelected.visibility = View.VISIBLE
            renderSelectedPreview()
        } else {
            mBinding.scrollSelected.visibility = View.GONE
        }
    }

    private fun renderSelectedPreview() {
        mBinding.layoutSelectedItems.removeAllViews()
        val inflater = layoutInflater
        selectionManager.toList().forEachIndexed { index, item ->
            val view = inflater.inflate(R.layout.item_selected_preview, mBinding.layoutSelectedItems, false)

            view.findViewById<View>(R.id.tvIndex)?.let {
                (it as? TextView)?.text = "${index + 1}"
            }

            view.findViewById<View>(R.id.ivRemove)?.setOnClickListener {
                selectionManager.remove(item)
                updateSelectToggle()
                updateBottomBar()
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

    private fun setupClickListeners() {
        mBinding.ivBack.setOnClickListener { finishWithResult() }
        mBinding.tvSelectToggle.setOnClickListener {
            val item = previewItems.getOrNull(currentPosition) ?: return@setOnClickListener
            toggleSelection(item)
        }
    }

    private fun toggleSelection(item: MediaItem) {
        val toggled = selectionManager.toggle(item)
        if (!toggled && selectionManager.isFull) {
            Toast.makeText(this, "最多选择${maxSelect}个", Toast.LENGTH_SHORT).show()
        }
        updateSelectToggle()
        updateBottomBar()
    }

    // ==================== 工具方法 ====================

    private fun loadAudioCover(item: MediaItem.Audio) {
        if (item.albumId > 0) {
            val albumArtUri = Uri.parse("content://media/external/audio/albumart/${item.albumId}")
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

    private fun releaseMediaPlayer() {
        audioUpdateJob?.cancel()
        audioUpdateJob = null
        mediaPlayer?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
        isAudioPlaying = false
    }

    private fun finishWithResult() {
        val result = Intent().apply {
            putParcelableArrayListExtra(KEY_RESULT, ArrayList(selectionManager.toList()))
        }
        setResult(RESULT_OK, result)
        finish()
    }

    override fun onBackPressed() {
        finishWithResult()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    /** 格式化时间 mm:ss */
    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
