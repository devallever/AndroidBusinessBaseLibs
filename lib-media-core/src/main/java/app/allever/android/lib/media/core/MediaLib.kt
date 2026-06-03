package app.allever.android.lib.media.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.media.core.loader.MediaLoader
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.core.model.Pagination
import app.allever.android.lib.media.core.permission.MediaPermission
import app.allever.android.lib.media.core.query.FolderDetailQueryBuilder
import app.allever.android.lib.media.core.query.FolderDetailQuery
import app.allever.android.lib.media.core.query.MediaFolderDetail
import app.allever.android.lib.media.core.query.MediaQuery
import app.allever.android.lib.media.core.query.MediaQueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * 媒体库组件 — 唯一对外入口（Facade 模式）
 *
 * 提供统一的 API 查询系统媒体库中的图片、视频、音频资源，
 * 支持任意类型组合查询、目录分组、分页加载。
 *
 * 三种返回方式：
 * - suspend 函数：直接返回原始数据，适合一次性操作
 * - Flow：流式/逐页/自动刷新，适合列表加载和 Compose UI
 * - LiveData：UI 直接观察，适合传统 Activity/Fragment
 *
 * 使用示例：
 * ```kotlin
 * // 1. 获取目录列表（原始数据）
 * val folders = MediaLib.queryFolders {
 *     type = MediaType.IMAGE or MediaType.VIDEO
 *     pagination = Pagination.All
 * }
 *
 * // 2. Flow 分页加载
 * MediaLib.queryFoldersFlow { type = MediaType.IMAGE; pagination = Pagination.Paged(0, 20) }
 *     .collect { pageFolders -> ... }
 *
 * // 3. LiveData 观察
 * MediaLib.queryFoldersLiveData { type = MediaType.ALL }.observe(this) { folders -> ... }
 *
 * // 4. 进入某个目录查看详情
 * val detail = MediaLib.queryFolderDetail {
 *     bucketId = folder.bucketId
 *     type = MediaType.ALL
 * }
 *
 * // 5. 加载缩略图
 * val bitmap = ThumbnailLoader.loadThumbnail(mediaItem.uri)
 * ```
 */
object MediaLib {

    // ==================== 目录列表查询 ====================

    /**
     * 查询目录列表 — 原始数据（suspend）
     *
     * @param block DSL 配置查询参数
     * @return 目录列表（已按分页条件切片）
     */
    suspend fun queryFolders(block: MediaQueryBuilder.() -> Unit): List<MediaFolder> {
        val query = MediaQueryBuilder().apply(block).build()
        log("MediaLib", "queryFolders → type=${typeFlagsLabel(query.typeFlags)}, ${paginationLabel(query.pagination)}, sortBy=${query.sortBy}")
        val result = MediaLoader.queryFolders(query, true)
        log("MediaLib", "queryFolders ← 返回 ${result.size} 个目录")
        return result
    }

    /**
     * 查询目录列表 — Flow 流式
     *
     * 全量模式(Pagination.All)：emit 一次完整结果
     * 分页模式(Pagination.Paged)：自动逐页 emit，直到无更多数据
     */
    fun queryFoldersFlow(block: MediaQueryBuilder.() -> Unit): Flow<List<MediaFolder>> {
        val query = MediaQueryBuilder().apply(block).build()
        log("MediaLib", "queryFoldersFlow → type=${typeFlagsLabel(query.typeFlags)}, ${paginationLabel(query.pagination)}")
        return flow {
            when (query.pagination) {
                is Pagination.All -> {
                    emit(MediaLoader.queryFolders(query))
                }
                is Pagination.Paged -> {
                    var currentPage = 0
                    while (true) {
                        val pageQuery = query.copyForPage(currentPage)
                        val pageResult = MediaLoader.queryFolders(pageQuery)
                        if (pageResult.isEmpty()) break
                        emit(pageResult)
                        currentPage++
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
            .catch { e -> logE("MediaLib", "queryFoldersFlow error: ${e.message}") }
    }

    /**
     * 查询目录列表 — LiveData
     *
     * 适合在 Activity/Fragment 中直接 observe
     */
    fun queryFoldersLiveData(block: MediaQueryBuilder.() -> Unit): LiveData<List<MediaFolder>> {
        val query = MediaQueryBuilder().apply(block).build()
        log("MediaLib", "queryFoldersLiveData → type=${typeFlagsLabel(query.typeFlags)}, ${paginationLabel(query.pagination)}")
        return liveData(Dispatchers.IO) {
            emit(MediaLoader.queryFolders(query))
        }
    }

    // ==================== 目录详情查询 ====================

    /**
     * 查询某个目录下的资源详情 — 原始数据（suspend）
     */
    suspend fun queryFolderDetail(block: FolderDetailQueryBuilder.() -> Unit): MediaFolderDetail {
        val query = FolderDetailQueryBuilder().apply(block).build()
        log("MediaLib", "queryFolderDetail → bucketId=${query.bucketId}, type=${typeFlagsLabel(query.typeFlags)}")
        val result = MediaLoader.queryFolderDetail(query)
        log("MediaLib", "queryFolderDetail ← img:${result.images.size} vid:${result.videos.size} aud:${result.audios.size}")
        return result
    }

    /**
     * 查询某个目录下的资源详情 — Flow
     */
    fun queryFolderDetailFlow(
        block: FolderDetailQueryBuilder.() -> Unit,
    ): Flow<MediaFolderDetail> {
        val query = FolderDetailQueryBuilder().apply(block).build()
        log("MediaLib", "queryFolderDetailFlow → bucketId=${query.bucketId}")
        return flow {
            emit(MediaLoader.queryFolderDetail(query))
        }.flowOn(Dispatchers.IO)
            .catch { e -> logE("MediaLib", "queryFolderDetailFlow error: ${e.message}") }
    }

    /**
     * 查询某个目录下的资源详情 — LiveData
     */
    fun queryFolderDetailLiveData(
        block: FolderDetailQueryBuilder.() -> Unit,
    ): LiveData<MediaFolderDetail> {
        val query = FolderDetailQueryBuilder().apply(block).build()
        log("MediaLib", "queryFolderDetailLiveData → bucketId=${query.bucketId}")
        return liveData(Dispatchers.IO) {
            emit(MediaLoader.queryFolderDetail(query))
        }
    }

    // ==================== 全局资源查询（不分目录）====================

    /**
     * 全局查询所有资源 — 原始数据（suspend）
     */
    suspend fun queryAll(block: MediaQueryBuilder.() -> Unit): List<MediaItem> {
        val query = MediaQueryBuilder().apply(block).build()
        log("MediaLib", "queryAll → type=${typeFlagsLabel(query.typeFlags)}, ${paginationLabel(query.pagination)}")
        val result = MediaLoader.queryAll(query)
        log("MediaLib", "queryAll ← 返回 ${result.size} 项资源")
        return result
    }

    /**
     * 全局查询所有资源 — Flow（逐条 emit）
     */
    fun queryAllFlow(block: MediaQueryBuilder.() -> Unit): Flow<MediaItem> {
        val query = MediaQueryBuilder().apply(block).build()
        log("MediaLib", "queryAllFlow → type=${typeFlagsLabel(query.typeFlags)}, ${paginationLabel(query.pagination)}")
        return flow {
            val items = MediaLoader.queryAll(query)
            items.forEach { emit(it) }
        }.flowOn(Dispatchers.IO)
            .catch { e -> logE("MediaLib", "queryAllFlow error: ${e.message}") }
    }

    /**
     * 全局查询所有资源 — LiveData
     */
    fun queryAllLiveData(block: MediaQueryBuilder.() -> Unit): LiveData<List<MediaItem>> {
        val query = MediaQueryBuilder().apply(block).build()
        log("MediaLib", "queryAllLiveData → type=${typeFlagsLabel(query.typeFlags)}")
        return liveData(Dispatchers.IO) {
            emit(MediaLoader.queryAll(query))
        }
    }

    // ==================== 缓存管理 ====================

    /** 清除目录列表缓存（下次查询将重新从系统读取） */
    suspend fun clearCache() {
        log("MediaLib", "clearCache → 清除目录列表缓存")
        MediaLoader.clearFolderCache()
        log("MediaLib", "clearCache ← 缓存已清除")
    }

    // ==================== 权限检查便捷方法 ====================

    /**
     * 检查是否拥有指定类型所需的权限
     */
    fun hasPermission(@MediaType.Type typeFlags: Int): Boolean {
        return MediaPermission.hasPermission(App.context, typeFlags)
    }

    /**
     * 检查是否拥有全部媒体类型的权限
     */
    fun hasAllPermission(): Boolean {
        return MediaPermission.hasAllPermission(App.context)
    }

    /**
     * 获取指定类型所需权限数组（用于动态申请）
     */
    fun requiredPermissions(@MediaType.Type typeFlags: Int): Array<String> {
        return MediaPermission.requiredPermissions(typeFlags)
    }

    // ==================== 日志辅助 ====================

    private fun typeFlagsLabel(@MediaType.Type typeFlags: Int): String = buildString {
        if (typeFlags == MediaType.ALL) { append("ALL"); return@buildString }
        if (MediaType.contains(typeFlags, MediaType.IMAGE)) append("IMG")
        if (MediaType.contains(typeFlags, MediaType.VIDEO)) append(if (isNotEmpty()) "+VID" else "VID")
        if (MediaType.contains(typeFlags, MediaType.AUDIO)) append(if (isNotEmpty()) "+AUD" else "AUD")
    }

    private fun paginationLabel(pagination: Pagination): String = when (pagination) {
        is Pagination.All -> "全量"
        is Pagination.Paged -> "分页(page=${pagination.page}, size=${pagination.pageSize})"
    }
}
