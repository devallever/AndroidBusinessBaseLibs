package app.allever.android.lib.media.picker.ui

import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.selection.SelectionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 媒体预览 ViewModel（业务逻辑层）
 *
 * 职责：
 * - 管理预览数据（媒体列表、当前位置、类型、配置）
 * - 选中状态管理（通过 SelectionManager）
 * - 音频播放控制（MediaPlayer 生命周期、进度更新）
 * - 通过 StateFlow 暴露所有 UI 状态，Activity 只负责观察和渲染
 */
class MediaPreviewViewModel : ViewModel() {

    lateinit var selectionManager: SelectionManager

    // ==================== 配置 ====================

    private var _maxSelect = MutableStateFlow(9)
    val maxSelect: StateFlow<Int> = _maxSelect.asStateFlow()

    private var _mediaType = MutableStateFlow("image")
    val mediaType: StateFlow<String> = _mediaType.asStateFlow()

    /** 是否为音频模式 */
    val isAudioMode: Boolean get() = _mediaType.value == "audio"

    // ==================== 数据状态 ====================

    private var _previewItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val previewItems: StateFlow<List<MediaItem>> = _previewItems.asStateFlow()

    private var _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    /** 当前展示的 item */
    val currentItem: MediaItem? get() = _previewItems.value.getOrNull(_currentPosition.value)

    /** 总数 */
    val totalCount: Int get() = _previewItems.value.size

    // ==================== 音频播放状态 ====================

    private var _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    private var _audioProgressText = MutableStateFlow("")
    val audioProgressText: StateFlow<String> = _audioProgressText.asStateFlow()

    private var _audioTitle = MutableStateFlow("")
    val audioTitle: StateFlow<String> = _audioTitle.asStateFlow()

    private var _audioArtist = MutableStateFlow("")
    val audioArtist: StateFlow<String> = _audioArtist.asStateFlow()

    private var _topBarTitle = MutableStateFlow("")
    val topBarTitle: StateFlow<String> = _topBarTitle.asStateFlow()

    private var _selectToggleText = MutableStateFlow("选择")
    val selectToggleText: StateFlow<String> = _selectToggleText.asStateFlow()

    private var _hasSelection = MutableStateFlow(false)
    val hasSelection: StateFlow<Boolean> = _hasSelection.asStateFlow()

    // ==================== MediaPlayer ====================

    private var mediaPlayer: MediaPlayer? = null
    private var audioUpdateJob: Job? = null

    // ==================== 初始化 ====================

    fun init(
        items: List<MediaItem>,
        position: Int,
        mediaType: String,
        maxSelect: Int,
        selectedIds: Set<Long>,
    ) {
        this.selectionManager = SelectionManager(maxSelect)

        _maxSelect.value = maxSelect
        _mediaType.value = mediaType
        _previewItems.value = items
        _currentPosition.value = position.coerceIn(0, (items.size - 1).coerceAtLeast(0))

        // 恢复已选中状态
        for (item in items) {
            if (selectedIds.contains(item.id)) {
                selectionManager.forceAdd(item)
            }
        }

        updateTopBarUI()
        updateSelectToggleUI()
        updateHasSelectionUI()
        setupAudioInfoIfNeeded()
    }

    // ==================== 页面切换 ====================

    fun onPageSelected(position: Int) {
        if (position in _previewItems.value.indices) {
            _currentPosition.value = position
            updateTopBarUI()
            updateSelectToggleUI()
            // 切页时停止视频由 PreviewAdapter 内部处理
        }
    }

    // ==================== 选中操作 ====================

    fun toggleSelection(): Boolean {
        val item = currentItem ?: return false
        val toggled = selectionManager.toggle(item)
        updateSelectToggleUI()
        updateHasSelectionUI()
        return toggled
    }

    fun isFullAndNotToggled(): Boolean {
        val item = currentItem
        return selectionManager.isFull && (item == null || !selectionManager.isSelected(item))
    }

    fun getSelectedList(): List<MediaItem> = selectionManager.toList()

    // ==================== 音频播放控制 ====================

    /** 使用 context 设置数据源并准备播放 */
    fun prepareAudioWithContext(context: android.content.Context, uri: Uri) {
        releaseMediaPlayer()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnPreparedListener { mp ->
                    log("MediaPreview", "音频准备完成")
                    mp.start()
                    _isAudioPlaying.value = true
                    startProgressUpdate()
                }
                setOnCompletionListener {
                    _isAudioPlaying.value = false
                    audioUpdateJob?.cancel()
                    autoPlayNext()
                }
                setOnErrorListener { _, what, extra ->
                    logE("MediaPreview", "播放错误: what=$what extra=$extra")
                    _isAudioPlaying.value = false
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            logE("MediaPreview", "初始化播放器失败: ${e.message}")
        }
    }

    fun toggleAudioPlay() {
        val player = mediaPlayer ?: return
        if (_isAudioPlaying.value) {
            player.pause()
            _isAudioPlaying.value = false
            audioUpdateJob?.cancel()
        } else {
            player.start()
            _isAudioPlaying.value = true
            startProgressUpdate()
        }
    }

    private fun startProgressUpdate() {
        audioUpdateJob?.cancel()
        audioUpdateJob = viewModelScope.launch {
            while (isActive && _isAudioPlaying.value) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val current = player.currentPosition
                        val duration = player.duration
                        if (duration > 0) {
                            _audioProgressText.value = formatTime(current) + " / " + formatTime(duration)
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun autoPlayNext() {
        val pos = _currentPosition.value
        val items = _previewItems.value
        if (pos < items.size - 1) {
            _currentPosition.value = pos + 1
            updateTopBarUI()
            setupAudioInfoIfNeeded()
            // 通知 Activity 需要重新初始化下一首的 MediaPlayer
            _needNextAudioPrepare.value = true
        }
    }

    /** 是否需要准备下一首音频（供 Activity 观察） */
    private var _needNextAudioPrepare = MutableStateFlow(false)
    val needNextAudioPrepare: StateFlow<Boolean> = _needNextAudioPrepare.asStateFlow()

    fun consumeNextAudioPrepare(): Boolean {
        val need = _needNextAudioPrepare.value
        if (need) _needNextAudioPrepare.value = false
        return need
    }

    // ==================== 释放资源 ====================

    fun releaseMediaPlayer() {
        audioUpdateJob?.cancel()
        audioUpdateJob = null
        mediaPlayer?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
        _isAudioPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
    }

    // ==================== UI 状态更新方法（内部） ====================

    private fun updateTopBarUI() {
        val pos = _currentPosition.value
        val total = _previewItems.value.size
        if (isAudioMode) {
            val audioItem = currentItem as? MediaItem.Audio
            _topBarTitle.value = buildString {
                append(audioItem?.title?.ifEmpty { audioItem?.name } ?: "")
                if (total > 1) append(" (${pos + 1}/$total)")
            }
        } else {
            _topBarTitle.value = "${pos + 1}/$total"
        }
    }

    private fun updateSelectToggleUI() {
        val item = currentItem
        _selectToggleText.value = if (item != null && selectionManager.isSelected(item)) "取消选择" else "选择"
    }

    private fun updateHasSelectionUI() {
        _hasSelection.value = selectionManager.selectedCount > 0
    }

    /** 根据当前 item 更新音频信息（仅音频模式） */
    private fun setupAudioInfoIfNeeded() {
        if (!isAudioMode) return
        val audioItem = currentItem as? MediaItem.Audio ?: return
        _audioTitle.value = audioItem.title.ifEmpty { audioItem.name }
        _audioArtist.value = buildString {
            if (audioItem.artist.isNotEmpty()) append(audioItem.artist)
            if (audioItem.album.isNotEmpty()) {
                if (isNotEmpty()) append(" · ")
                append(audioItem.album)
            }
        }.ifEmpty { "未知艺术家" }
    }

    /** 获取当前音频 item 的 albumId，用于加载封面 */
    fun currentAudioAlbumId(): Long {
        return (currentItem as? MediaItem.Audio)?.albumId ?: -1L
    }

    /** 获取当前音频 item 的 uri */
    fun currentAudioUri(): Uri? {
        return (currentItem as? MediaItem.Audio)?.uri
    }

    // ==================== 工具方法 ====================

    companion object {
        /** 格式化时间 mm:ss */
        fun formatTime(ms: Int): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
