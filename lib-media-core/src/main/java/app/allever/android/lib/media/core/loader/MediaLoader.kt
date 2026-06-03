package app.allever.android.lib.media.core.loader

import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.paginate
import app.allever.android.lib.media.core.query.FolderDetailQuery
import app.allever.android.lib.media.core.query.MediaFolderDetail
import app.allever.android.lib.media.core.query.MediaQuery
import app.allever.android.lib.media.core.source.MediaSource
import app.allever.android.lib.media.core.source.MediaStoreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * 媒体资源加载编排层
 * 职责：
 * 1. 管理数据源实例（工厂模式）
 * 2. 查询结果缓存（避免重复 I/O）
 * 3. 分页逻辑编排
 */
internal object MediaLoader {

    /** 数据源（懒加载单例） */
    private val source: MediaSource by lazy { MediaStoreSource() }

    /** 目录列表缓存：typeFlags+sortBy → List<MediaFolder> */
    private val folderCache = mutableMapOf<String, List<MediaFolder>>()
    private val folderCacheMutex = Mutex()

    // ==================== 目录列表查询 ====================

    /**
     * 查询目录列表
     * @param forceRefresh 是否强制刷新缓存
     */
    suspend fun queryFolders(query: MediaQuery, forceRefresh: Boolean = false): List<MediaFolder> =
        withContext(Dispatchers.IO) {
            if (!forceRefresh) {
                // 尝试从缓存读取并分页
                val cacheKey = folderCacheKey(query)
                folderCacheMutex.withLock {
                    folderCache[cacheKey]?.let { cached ->
                        return@withContext cached.paginate(query.pagination)
                    }
                }
            }

            // 缓存未命中或强制刷新，执行查询
            val allFolders = source.queryFolders(query)

            // 更新缓存（存全量数据）
            folderCacheMutex.withLock {
                folderCache[folderCacheKey(query)] = allFolders
            }

            // 返回分页结果
            allFolders.paginate(query.pagination)
        }

    /**
     * 清除目录列表缓存
     */
    suspend fun clearFolderCache() = withContext(Dispatchers.IO) {
        folderCacheMutex.withLock { folderCache.clear() }
    }

    // ==================== 目录详情查询 ====================

    suspend fun queryFolderDetail(query: FolderDetailQuery): MediaFolderDetail =
        withContext(Dispatchers.IO) {
            source.queryFolderDetail(query)
        }

    // ==================== 全局资源查询 ====================

    suspend fun queryAll(query: MediaQuery): List<MediaItem> =
        withContext(Dispatchers.IO) {
            source.queryAll(query)
        }

    // ==================== 缓存 Key ====================

    private fun folderCacheKey(query: MediaQuery): String {
        return "folders_${query.typeFlags}_${query.sortBy}_${query.bucketId}"
    }
}
