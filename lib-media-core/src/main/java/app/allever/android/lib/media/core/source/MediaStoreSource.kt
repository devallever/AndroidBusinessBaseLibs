package app.allever.android.lib.media.core.source

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaStoreColumn
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.core.model.SortBy
import app.allever.android.lib.media.core.model.paginate
import app.allever.android.lib.media.core.query.FolderDetailQuery
import app.allever.android.lib.media.core.query.MediaFolderDetail
import app.allever.android.lib.media.core.query.MediaQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 基于 MediaStore.Files 的数据源实现
 * 统一入口查询图片/视频/音频，通过 MEDIA_TYPE 列区分并映射到对应的 sealed class
 *
 * 性能特点：
 * - 一次 Cursor 查询处理任意类型组合
 * - 内存中按 bucket_id 分组构建目录树
 * - 支持分页切片（在内存中对全量结果进行 offset/limit）
 */
internal class MediaStoreSource : MediaSource {

    private val contentResolver get() = App.context.contentResolver

    /** 统一查询 Uri（Files 表包含所有媒体类型） */
    private val baseUri: Uri get() = MediaStore.Files.getContentUri("external")

    // ==================== 目录列表查询 ====================

    override suspend fun queryFolders(query: MediaQuery): List<MediaFolder> =
        withContext(Dispatchers.IO) {
            val selection = buildSelection(query)
            val selectionArgs = buildSelectionArgs(query.typeFlags)
            val orderBy = SortBy.toOrderByClause(query.sortBy, bucketGrouped = true)
            log("MediaStoreSource", "queryFolders → selection=$selection, args=${selectionArgs.contentToString()}, orderBy=$orderBy")

            val cursor = executeQuery(
                projection = ProjectionBuilder.buildForFolders(query.typeFlags),
                selection = selection,
                selectionArgs = selectionArgs,
                orderBy = orderBy,
            )
            if (cursor == null) {
                logE("MediaStoreSource", "queryFolders ← cursor 为 null, 返回空列表")
                return@withContext emptyList()
            }
            log("MediaStoreSource", "queryFolders ← cursor.count=${cursor.count}")
            cursor.use { buildFoldersFromCursor(it, query.typeFlags) }
        }

    // ==================== 目录详情查询 ====================

    override suspend fun queryFolderDetail(query: FolderDetailQuery): MediaFolderDetail =
        withContext(Dispatchers.IO) {
            val selection = StringBuilder().apply {
                append("${MediaStoreColumn.BUCKET_ID} = ?")
                append(" AND ")
                append(buildTypeCondition(query.typeFlags))
            }.toString()

            val args = buildList {
                add(query.bucketId.toString())
                addAll(buildTypeArgs(query.typeFlags))
            }.toTypedArray()

            val cursor = executeQuery(
                projection = ProjectionBuilder.buildForFolders(query.typeFlags),
                selection = selection,
                selectionArgs = args,
                orderBy = SortBy.toOrderByClause(query.sortBy),
            )

            cursor?.use { c ->
                val allItems = parseCursorToTypedLists(c, query.typeFlags)
                val folder = resolveFolder(c, query.bucketId, query.typeFlags, allItems)
                MediaFolderDetail(
                    folder = folder,
                    images = allItems.images.paginate(query.pagination),
                    videos = allItems.videos.paginate(query.pagination),
                    audios = allItems.audios.paginate(query.pagination),
                )
            } ?: MediaFolderDetail(
                folder = MediaFolder(bucketId = query.bucketId, name = "", path = "", coverUri = null),
                images = emptyList(),
                videos = emptyList(),
                audios = emptyList(),
            )
        }

    // ==================== 全局查询（不分目录）====================

    override suspend fun queryAll(query: MediaQuery): List<MediaItem> =
        withContext(Dispatchers.IO) {
            val cursor = executeQuery(
                projection = ProjectionBuilder.buildForAll(query.typeFlags),
                selection = buildSelection(query),
                selectionArgs = buildSelectionArgs(query.typeFlags),
                orderBy = SortBy.toOrderByClause(query.sortBy),
            )
            cursor?.use { c ->
                val items = mutableListOf<MediaItem>()
                while (c.moveToNext()) {
                    c.toMediaItem()?.let { items.add(it) }
                }
                items.paginate(query.pagination)
            } ?: emptyList()
        }

    // ==================== 查询执行 ====================

    private fun executeQuery(
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        orderBy: String?,
    ): Cursor? {
        return try {
            val cursor = contentResolver.query(baseUri, projection, selection, selectionArgs, orderBy)
            log("MediaStoreSource", "query → selection=$selection, args=${selectionArgs?.contentToString()}, orderBy=$orderBy")
            cursor
        } catch (e: SecurityException) {
            logE("MediaStoreSource", "query SecurityException: 权限不足 | ${e.message}")
            null
        } catch (e: Exception) {
            logE("MediaStoreSource", "query Exception: ${e.message}")
            null
        }
    }

    // ==================== Selection 构建 ====================

    /**
     * 构建完整的 WHERE 条件（含类型、bucketId、时长过滤）
     */
    private fun buildSelection(query: MediaQuery): String {
        return buildString {
            append(buildTypeCondition(query.typeFlags))
            append(" AND ${MediaStoreColumn.SIZE} > 0")

            query.bucketId?.let {
                append(" AND ${MediaStoreColumn.BUCKET_ID} = $it")
            }
            query.mimeTypePattern?.let {
                append(" AND ${MediaStoreColumn.MIME_TYPE} = ?")
            }
            if (query.minDuration > 0 || query.maxDuration < Long.MAX_VALUE) {
                append(" AND ${MediaStoreColumn.DURATION} BETWEEN ? AND ?")
            }
        }
    }

    /**
     * 构建 SelectionArgs
     */
    private fun buildSelectionArgs(typeFlags: Int): Array<String> {
        return buildTypeArgs(typeFlags).toTypedArray()
    }

    /**
     * 类型条件：MEDIA_TYPE IN (?, ?, ...)
     */
    private fun buildTypeCondition(typeFlags: Int): String {
        val types = mutableListOf<Int>()
        if (MediaType.contains(typeFlags, MediaType.IMAGE))
            types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
        if (MediaType.contains(typeFlags, MediaType.VIDEO))
            types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        if (MediaType.contains(typeFlags, MediaType.AUDIO))
            types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)

        val placeholders = types.joinToString(",") { "?" }
        return "${MediaStoreColumn.MEDIA_TYPE} IN ($placeholders)"
    }

    private fun buildTypeArgs(typeFlags: Int): List<String> {
        return buildList {
            if (MediaType.contains(typeFlags, MediaType.IMAGE))
                add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
            if (MediaType.contains(typeFlags, MediaType.VIDEO))
                add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
            if (MediaType.contains(typeFlags, MediaType.AUDIO))
                add(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
        }
    }

    // ==================== Cursor → 数据模型映射 ====================

    /**
     * 从 Cursor 构建目录列表
     * 核心逻辑：一次遍历，按 bucket_id 分组，组内按 media_type 分类
     */
    private fun buildFoldersFromCursor(cursor: Cursor, typeFlags: Int): List<MediaFolder> {
        // bucketId → 文件夹构建器
        val totalCount = cursor.count
        log("MediaStoreSource", "buildFoldersFromCursor → cursor 总行数=$totalCount")
        if (totalCount <= 0) return emptyList()

        val folderMap = LinkedHashMap<Long, MutableMediaFolder>()
        var skippedBucketId = 0

        while (cursor.moveToNext()) {
            val bucketId = cursor.getLongOrDefault(MediaStoreColumn.BUCKET_ID, -1L)
            if (bucketId == (-1).toLong()) {
                val dirPath = cursor.getStringOrDefault(MediaStoreColumn.DATA, "NULL")
                logE("MediaStoreSource", "buildFoldersFromCursor → 忽略无效 bucketId=$bucketId, dirPath = $dirPath")
                skippedBucketId++
                continue
            }

            val mediaType = cursor.getIntOrDefault(MediaStoreColumn.MEDIA_TYPE, 0)
            val typeFlag = MediaType.fromMediaStoreMediaType(mediaType)
            if (!MediaType.contains(typeFlags, typeFlag)) continue

            // 获取或创建文件夹
            val folder = folderMap.getOrPut(bucketId) {
                val name = cursor.getStringOrDefault(MediaStoreColumn.BUCKET_DISPLAY_NAME, "")
                val dataPath = cursor.getStringOrDefault(MediaStoreColumn.DATA, "")
                val dirPath = extractDirPath(dataPath)
                MutableMediaFolder(
                    bucketId = bucketId,
                    name = name.ifEmpty { extractDirName(dirPath) },
                    path = dirPath,
                    coverUri = cursor.toContentUri(),
                )
            }

            // 按类型添加到对应列表
            when (mediaType) {
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                    folder.addImage(cursor.toImage())
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                    folder.addVideo(cursor.toVideo())
                MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO ->
                    folder.addAudio(cursor.toAudio())
            }
        }

        return folderMap.values.map { it.build() }.also { result ->
            val totalItems = folderMap.values.sumOf { it.images.size + it.videos.size + it.audios.size }
            log("MediaStoreSource", "buildFoldersFromCursor → 解析完成: ${result.size} 个目录, 总记录=$totalCount, 跳过(无效bucketId)=$skippedBucketId, 有效记录=$totalItems")
            if (result.isNotEmpty()) {
                result.take(5).forEach { f ->
                    log("MediaStoreSource", "  目录: bucketId=${f.bucketId}, name=${f.name}, path=${f.path}, img=${f.images.size} vid=${f.videos.size} aud=${f.audios.size}")
                }
                if (result.size > 5) log("MediaStoreSource", "  ... 还有 ${result.size - 5} 个目录")
            }
        }
    }

    /**
     * 解析 Cursor 为按类型分类的三个列表
     */
    private fun parseCursorToTypedLists(cursor: Cursor, typeFlags: Int): TypedLists {
        val images = mutableListOf<MediaItem.Image>()
        val videos = mutableListOf<MediaItem.Video>()
        val audios = mutableListOf<MediaItem.Audio>()

        while (cursor.moveToNext()) {
            val mediaType = cursor.getIntOrDefault(MediaStoreColumn.MEDIA_TYPE, 0)
            when (mediaType) {
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
                    if (MediaType.contains(typeFlags, MediaType.IMAGE)) {
                        cursor.toImage()?.let { images.add(it) }
                    }
                }
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                    if (MediaType.contains(typeFlags, MediaType.VIDEO)) {
                        cursor.toVideo()?.let { videos.add(it) }
                    }
                }
                MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO -> {
                    if (MediaType.contains(typeFlags, MediaType.AUDIO)) {
                        cursor.toAudio()?.let { audios.add(it) }
                    }
                }
            }
        }
        return TypedLists(images, videos, audios)
    }

    /**
     * 从已解析的数据中构建单个文件夹信息
     */
    private fun resolveFolder(
        cursor: Cursor,
        bucketId: Long,
        typeFlags: Int,
        items: TypedLists,
    ): MediaFolder {
        // 尝试从第一行获取文件夹元信息
        return if (cursor.moveToFirst()) {
            val name = cursor.getStringOrDefault(MediaStoreColumn.BUCKET_DISPLAY_NAME, "")
            val dataPath = cursor.getStringOrDefault(MediaStoreColumn.DATA, "")
            val dirPath = extractDirPath(dataPath)
            MediaFolder(
                bucketId = bucketId,
                name = name.ifEmpty { extractDirName(dirPath) },
                path = dirPath,
                coverUri = if (items.allItems.isNotEmpty()) items.allItems.first().uri else null,
                images = items.images,
                videos = items.videos,
                audios = items.audios,
            )
        } else {
            MediaFolder(bucketId = bucketId, name = "", path = "", coverUri = null)
        }
    }

    // ==================== Cursor 行 → 具体类型转换 ====================

    private fun Cursor.toMediaItem(): MediaItem? {
        return when (val mt = getIntOrDefault(MediaStoreColumn.MEDIA_TYPE, 0)) {
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> toImage()
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> toVideo()
            MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO -> toAudio()
            else -> {
                // 未知类型跳过
                null
            }
        }
    }

    private fun Cursor.toImage(): MediaItem.Image? {
        return try {
            val id = getLongOrDefault(MediaStoreColumn.ID, -1L)
            if (id < 0) return null
            MediaItem.Image(
                id = id,
                uri = toContentUri(),
                path = safeGetData(),
                name = getStringOrDefault(MediaStoreColumn.DISPLAY_NAME, ""),
                dateAdded = getLongOrDefault(MediaStoreColumn.DATE_ADDED, 0L),
                size = getLongOrDefault(MediaStoreColumn.SIZE, 0L),
                mimeType = getStringOrDefault(MediaStoreColumn.MIME_TYPE, ""),
                width = getIntOrDefault(MediaStoreColumn.WIDTH, 0),
                height = getIntOrDefault(MediaStoreColumn.HEIGHT, 0),
                orientation = getIntOrDefault(MediaStoreColumn.ORIENTATION, 0),
            )
        } catch (e: Exception) { null }
    }

    private fun Cursor.toVideo(): MediaItem.Video? {
        return try {
            val id = getLongOrDefault(MediaStoreColumn.ID, -1L)
            if (id < 0) return null
            MediaItem.Video(
                id = id,
                uri = toContentUri(),
                path = safeGetData(),
                name = getStringOrDefault(MediaStoreColumn.DISPLAY_NAME, ""),
                dateAdded = getLongOrDefault(MediaStoreColumn.DATE_ADDED, 0L),
                size = getLongOrDefault(MediaStoreColumn.SIZE, 0L),
                mimeType = getStringOrDefault(MediaStoreColumn.MIME_TYPE, ""),
                duration = getLongOrDefault(MediaStoreColumn.DURATION, 0L),
                width = getIntOrDefault(MediaStoreColumn.WIDTH, 0),
                height = getIntOrDefault(MediaStoreColumn.HEIGHT, 0),
            )
        } catch (e: Exception) { null }
    }

    private fun Cursor.toAudio(): MediaItem.Audio? {
        return try {
            val id = getLongOrDefault(MediaStoreColumn.ID, -1L)
            if (id < 0) return null
            MediaItem.Audio(
                id = id,
                uri = toContentUri(),
                path = safeGetData(),
                name = getStringOrDefault(MediaStoreColumn.DISPLAY_NAME, ""),
                dateAdded = getLongOrDefault(MediaStoreColumn.DATE_ADDED, 0L),
                size = getLongOrDefault(MediaStoreColumn.SIZE, 0L),
                mimeType = getStringOrDefault(MediaStoreColumn.MIME_TYPE, ""),
                duration = getLongOrDefault(MediaStoreColumn.DURATION, 0L),
                title = getStringOrDefault(MediaStoreColumn.TITLE, ""),
                artist = getStringOrDefault(MediaStoreColumn.ARTIST, ""),
                album = getStringOrDefault(MediaStoreColumn.ALBUM, ""),
                albumId = getLongOrDefault(MediaStoreColumn.ALBUM_ID, -1L),
            )
        } catch (e: Exception) { null }
    }

    /**
     * 将 _id 转换为 ContentUri
     */
    private fun Cursor.toContentUri(): Uri {
        val id = getLongOrDefault(MediaStoreColumn.ID, -1L)
        val mediaType = getIntOrDefault(MediaStoreColumn.MEDIA_TYPE, 0)
        val baseUri = when (mediaType) {
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> baseUri
        }
        return ContentUris.withAppendedId(baseUri, id)
    }

    // ==================== 工具方法 ====================

    /**
     * 安全读取 _data 列（Android 10+ 可能抛异常或返回空）
     */
    private fun Cursor.safeGetData(): String {
        return try {
            val index = getColumnIndex(MediaStoreColumn.DATA)
            if (index >= 0) getString(index) ?: "" else ""
        } catch (e: Exception) { "" }
    }

    /** 从文件路径提取目录路径 */
    private fun extractDirPath(filePath: String): String {
        val lastSep = filePath.lastIndexOf('/')
        return if (lastSep > 0) filePath.substring(0, lastSep) else filePath
    }

    /** 从目录路径提取目录名 */
    private fun extractDirName(dirPath: String): String {
        val lastSep = dirPath.lastIndexOf('/')
        return if (lastSep >= 0 && lastSep < dirPath.length - 1) {
            dirPath.substring(lastSep + 1)
        } else dirPath
    }

    // ==================== 内部数据结构 ====================

    /**
     * 可变的文件夹构建器（用于 Cursor 遍历时逐步填充）
     */
    private class MutableMediaFolder(
        val bucketId: Long,
        var name: String,
        var path: String,
        var coverUri: Uri?,
    ) {
        val images = mutableListOf<MediaItem.Image>()
        val videos = mutableListOf<MediaItem.Video>()
        val audios = mutableListOf<MediaItem.Audio>()

        fun addImage(item: MediaItem.Image?) { item?.let { images.add(it) } }
        fun addVideo(item: MediaItem.Video?) { item?.let { videos.add(it) } }
        fun addAudio(item: MediaItem.Audio?) { item?.let { audios.add(it) } }

        fun build(): MediaFolder = MediaFolder(
            bucketId = bucketId,
            name = name,
            path = path,
            coverUri = coverUri,
            images = images.toList(),
            videos = videos.toList(),
            audios = audios.toList(),
        )
    }

    /**
     * 按类型分类的三个列表
     */
    private class TypedLists(
        val images: List<MediaItem.Image>,
        val videos: List<MediaItem.Video>,
        val audios: List<MediaItem.Audio>,
    ) {
        val allItems: List<MediaItem>
            get() = buildList {
                addAll(images)
                addAll(videos)
                addAll(audios)
            }
    }

    companion object {
        /** 安全获取 long 列值 */
        private fun Cursor.getLongOrDefault(column: String, default: Long): Long {
            return try {
                val idx = getColumnIndexOrThrow(column)
                if (isNull(idx)) default else getLong(idx)
            } catch (e: Exception) { default }
        }

        /** 安全获取 int 列值 */
        private fun Cursor.getIntOrDefault(column: String, default: Int): Int {
            return try {
                val idx = getColumnIndexOrThrow(column)
                if (isNull(idx)) default else getInt(idx)
            } catch (e: Exception) { default }
        }

        /** 安全获取 string 列值 */
        private fun Cursor.getStringOrDefault(column: String, default: String): String {
            return try {
                val idx = getColumnIndexOrThrow(column)
                if (isNull(idx)) default else (getString(idx) ?: default)
            } catch (e: Exception) { default }
        }
    }
}
