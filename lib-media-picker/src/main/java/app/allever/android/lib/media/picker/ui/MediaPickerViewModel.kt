package app.allever.android.lib.media.picker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.media.core.MediaCore
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.core.model.Pagination
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.selection.SelectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 媒体选择器 ViewModel（业务逻辑层）
 *
 * 职责：
 * - 管理配置、Tab 类型、当前选中状态
 * - 数据加载（目录列表、媒体列表）
 * - 目录抽屉数据构造（全部目录 + 实际目录列表）
 * - 预览参数封装
 * - 通过 StateFlow 暴露所有状态，Fragment 只负责观察和渲染
 */
class MediaPickerViewModel : ViewModel() {

    lateinit var config: MediaPickerConfig
    lateinit var selectionManager: SelectionManager

    /** 可用的 Tab 类型列表 */
    val tabTypes: List<MediaType.Type> get() = _tabTypes
    private val _tabTypes = mutableListOf<MediaType.Type>()

    /** 当前选中的 Tab 位置 */
    private val _currentTabPosition = MutableStateFlow(0)
    val currentTabPosition: StateFlow<Int> = _currentTabPosition.asStateFlow()

    /** 当前媒体类型（与 currentTabPosition 同步） */
    val currentType: MediaType.Type get() = _tabTypes[_currentTabPosition.value]

    // ==================== 目录相关 StateFlow ====================

    /** 当前选中的目录 ID，null 表示全部目录 */
    private val _currentBucketId = MutableStateFlow<Long?>(null)
    val currentBucketId: StateFlow<Long?> = _currentBucketId.asStateFlow()

    /** 原始目录列表（不含"全部目录"） */
    private val _allFolders = MutableStateFlow<List<MediaFolder>>(emptyList())
    val allFolders: StateFlow<List<MediaFolder>> = _allFolders.asStateFlow()

    /** 当前目录显示名称 */
    private val _directoryName = MutableStateFlow("")
    val directoryName: StateFlow<String> = _directoryName.asStateFlow()

    /** 目录抽屉展示列表（全部目录 + 原始目录列表），实时计算 */
    val displayFolders: List<MediaFolder>
        get() {
            val imgs = _images.value
            val vids = _videos.value
            val auds = _audios.value
            val coverUri = vids.firstOrNull()?.uri ?: imgs.firstOrNull()?.uri ?: auds.firstOrNull()?.uri
            val allItem = MediaFolder(
                bucketId = ALL_FOLDERS_ID,
                name = DEFAULT_ALL_FOLDERS_NAME,
                path = "",
                coverUri = coverUri,
                images = imgs,
                videos = vids,
                audios = auds,
            )
            return listOf(allItem) + _allFolders.value
        }

    // ==================== 媒体数据 StateFlow ====================

    private val _images = MutableStateFlow<List<MediaItem.Image>>(emptyList())
    val images: StateFlow<List<MediaItem.Image>> = _images.asStateFlow()

    private val _videos = MutableStateFlow<List<MediaItem.Video>>(emptyList())
    val videos: StateFlow<List<MediaItem.Video>> = _videos.asStateFlow()

    private val _audios = MutableStateFlow<List<MediaItem.Audio>>(emptyList())
    val audios: StateFlow<List<MediaItem.Audio>> = _audios.asStateFlow()

    /** 加载中状态 */
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // ==================== 初始化 ====================

    fun init(config: MediaPickerConfig, selectionManager: SelectionManager) {
        this.config = config
        this.selectionManager = selectionManager
        buildTabTypes()
        _directoryName.value = DEFAULT_ALL_FOLDERS_NAME
    }

    private fun buildTabTypes() {
        _tabTypes.clear()
        if (config.hasImage) _tabTypes.add(MediaType.Type.IMAGE)
        if (config.hasVideo) _tabTypes.add(MediaType.Type.VIDEO)
        if (config.hasAudio) _tabTypes.add(MediaType.Type.AUDIO)
    }

    // ==================== Tab 切换 ====================

    fun switchTab(position: Int) {
        if (position in _tabTypes.indices) {
            _currentTabPosition.value = position
        }
    }

    // ==================== 目录切换 ====================

    fun selectAllFolders() {
        _currentBucketId.value = null
        _directoryName.value = DEFAULT_ALL_FOLDERS_NAME
        loadAllItems()
    }

    fun selectFolder(folder: MediaFolder) {
        _currentBucketId.value = folder.bucketId
        _directoryName.value = folder.name
        loadFolderDetail(folder.bucketId)
    }

    // ==================== 数据加载 ====================

    /** 加载目录列表，然后自动加载全部媒体 */
    fun loadFolders() {
        setLoading(true)
        viewModelScope.launch {
            try {
                val folders = MediaCore.queryFolders {
                    types = config.types
                    pagination = Pagination.All
                }
                _allFolders.value = folders
                log("MediaPicker", "loadFolders → ${folders.size} 个目录")
                loadAllItems()
            } catch (e: Exception) {
                log("MediaPicker", "loadFolders error: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    /** 加载指定目录下的媒体 */
    fun loadFolderDetail(bucketId: Long) {
        setLoading(true)
        viewModelScope.launch {
            try {
                val detail = MediaCore.queryFolderDetail {
                    this.bucketId = bucketId
                    types = config.types
                    pagination = Pagination.All
                }
                _images.value = detail.images
                _videos.value = detail.videos
                _audios.value = detail.audios
                log("MediaPicker", "loadFolderDetail → img=${detail.images.size} vid=${detail.videos.size} aud=${detail.audios.size}")
            } catch (e: Exception) {
                log("MediaPicker", "loadFolderDetail error: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    /** 加载全部媒体（按类型分别查询） */
    fun loadAllItems() {
        viewModelScope.launch {
            try {
                if (config.types.contains(MediaType.Type.IMAGE)) {
                    val items = MediaCore.queryAll {
                        types = setOf(MediaType.Type.IMAGE)
                        pagination = Pagination.All
                    }
                    _images.value = items.filterIsInstance<MediaItem.Image>()
                }
                if (config.types.contains(MediaType.Type.VIDEO)) {
                    val items = MediaCore.queryAll {
                        types = setOf(MediaType.Type.VIDEO)
                        pagination = Pagination.All
                    }
                    _videos.value = items.filterIsInstance<MediaItem.Video>()
                }
                if (config.types.contains(MediaType.Type.AUDIO)) {
                    val items = MediaCore.queryAll {
                        types = setOf(MediaType.Type.AUDIO)
                        pagination = Pagination.All
                    }
                    _audios.value = items.filterIsInstance<MediaItem.Audio>()
                }
                log("MediaPicker", "loadAllItems → img=${_images.value.size} vid=${_videos.value.size} aud=${_audios.value.size}")
            } catch (e: Exception) {
                log("MediaPicker", "loadAllItems error: ${e.message}")
            }
        }
    }

    // ==================== 预览参数封装 ====================

    /** 获取当前 Tab 对应的媒体列表（用于预览等场景） */
    fun getCurrentPageItems(): List<MediaItem> {
        return when (currentType) {
            MediaType.Type.IMAGE -> _images.value.map { it }
            MediaType.Type.VIDEO -> _videos.value.map { it }
            MediaType.Type.AUDIO -> _audios.value.map { it }
        }
    }

    /** 当前媒体类型的字符串标识 */
    val mediaTypeString: String
        get() = when (currentType) {
            MediaType.Type.IMAGE -> "image"
            MediaType.Type.VIDEO -> "video"
            MediaType.Type.AUDIO -> "audio"
        }

    // ==================== 内部工具 ====================

    private fun setLoading(show: Boolean) {
        _loading.value = show
    }

    companion object {
        const val ALL_FOLDERS_ID = -1L
        const val DEFAULT_ALL_FOLDERS_NAME = "全部目录"
    }
}
