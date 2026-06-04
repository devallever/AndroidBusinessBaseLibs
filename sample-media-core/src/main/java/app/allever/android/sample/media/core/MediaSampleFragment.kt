package app.allever.android.sample.media.core

import android.os.Build
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.function.permission.JumpPermissionSettingDialog
import app.allever.android.lib.core.function.permission.PermissionHelper
import app.allever.android.lib.media.core.MediaCore
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.core.model.Pagination
import app.allever.android.lib.media.core.model.SortBy
import app.allever.android.lib.media.core.thumbnail.ThumbnailLoader
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 媒体库组件示例 — 覆盖所有功能
 */
class MediaSampleFragment :
    ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    private val requestMediaPermissionLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                toast("媒体权限已授予")
                log("MediaSample", "媒体权限已授予")
            } else {
                val denied = permissions.filter { !it.value }.keys.joinToString(", ")
                toast("权限被拒绝: $denied")
                logE("MediaSample", "权限被拒绝: $denied")
                val deniedList = permissions.filter { !it.value }.keys.toList()
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(requireContext(), message = "媒体权限总是被拒绝，请手动授权").show()
                }
            }
        }

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> {
        return mutableListOf(
            // 权限
            TextClickItem("【权限】检查并请求全部媒体权限") { checkAndRequestAllMediaPermissions() },
            TextClickItem("【权限】仅请求图片+视频权限") { checkAndRequestImageVideoPermissions() },
            TextClickItem("【权限】仅请求音频权限") { checkAndRequestAudioPermissions() },
            TextClickItem("【权限】打印当前版本所需权限") { printRequiredPermissions() },

            // 目录列表查询
            TextClickItem("【目录】查询全部类型目录（全量/suspend）") { queryFoldersAllTypes() },
            TextClickItem("【目录】查询图片+视频目录（全量）") { queryFoldersImageAndVideo() },
            TextClickItem("【目录】查询纯图片目录（全量）") { queryFoldersImageOnly() },
            TextClickItem("【目录】查询纯视频目录（全量）") { queryFoldersVideoOnly() },
            TextClickItem("【目录】查询纯音频目录（全量）") { queryFoldersAudioOnly() },

            TextClickItem("【目录】分页查询目录（第1页，每页3个）") { queryFoldersPaged(0) },
            TextClickItem("【目录】分页查询目录（第2页）") { queryFoldersPaged(1) },
            TextClickItem("【目录】Flow 流式加载目录（自动翻页）") { queryFoldersFlow() },
            TextClickItem("【目录】LiveData 观察目录列表") { queryFoldersLiveData() },

            // 排序
            TextClickItem("【排序】按时间降序查询目录") { queryFoldersWithSort(SortBy.DATE_DESC) },
            TextClickItem("【排序】按时间升序查询目录") { queryFoldersWithSort(SortBy.DATE_ASC) },
            TextClickItem("【排序】按名称升序查询目录") { queryFoldersWithSort(SortBy.NAME_ASC) },

            // 目录详情
            TextClickItem("【详情】查询第一个目录的详情（全量）") { queryFirstFolderDetailAll() },
            TextClickItem("【详情】查询第一个目录详情（分页前5条）") { queryFirstFolderDetailPaged() },
            TextClickItem("【详情】Flow 加载第一个目录详情") { queryFolderDetailFlow() },

            // 全局资源查询
            TextClickItem("【全局】查询所有图片（全量）") { queryAllImages() },
            TextClickItem("【全局】查询所有视频（全量）") { queryAllVideos() },
            TextClickItem("【全局】查询所有音频（全量）") { queryAllAudios() },
            TextClickItem("【全局】分页查询图片（每页10条）") { queryAllImagesPaged() },
            TextClickItem("【全局】Flow 逐条加载图片") { queryAllImagesFlow() },

            // 缩略图
            TextClickItem("【缩略图】加载第一张图片的缩略图") { loadFirstImageThumbnail() },
            TextClickItem("【缩略图】批量加载前10张缩略图") { loadBatchThumbnails() },
            TextClickItem("【缩略图】清除缩略图缓存") { clearThumbnailCache() },

            // 缓存管理
            TextClickItem("【缓存】清除目录列表缓存") { clearFolderCache() },

            // 媒体选择器
            TextClickItem("【选择器】打开媒体选择器（图片+视频+音频，最多9个）") { openMediaPickerAllTypes() },
            TextClickItem("【选择器】仅选图片（最多6个）") { openMediaPickerImageOnly() },
            TextClickItem("【选择器】仅选视频（最多3个）") { openMediaPickerVideoOnly() },
        )
    }

    // ==================== 权限 ====================

    private fun checkAndRequestAllMediaPermissions() {
        if (MediaCore.hasPermission(MediaType.ALL)) {
            toast("已有全部媒体权限")
            return
        }
        requestMediaPermissionLauncher.launch(MediaCore.requiredPermissions(MediaType.ALL))
    }

    private fun checkAndRequestImageVideoPermissions() {
        if (MediaCore.hasPermission(MediaType.IMAGE_AND_VIDEO)) {
            toast("已有图片+视频权限")
            return
        }
        requestMediaPermissionLauncher.launch(MediaCore.requiredPermissions(MediaType.IMAGE_AND_VIDEO))
    }

    private fun checkAndRequestAudioPermissions() {
        if (MediaCore.hasPermission(setOf(MediaType.Type.AUDIO))) {
            toast("已有音频权限")
            return
        }
        requestMediaPermissionLauncher.launch(MediaCore.requiredPermissions(setOf(MediaType.Type.AUDIO)))
    }

    private fun printRequiredPermissions() {
        val sb = StringBuilder()
        sb.appendLine("=== SDK ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE}) ===")
        sb.appendLine("ALL: ${MediaCore.requiredPermissions(MediaType.ALL).contentToString()}")
        sb.appendLine("IMAGE: ${MediaCore.requiredPermissions(setOf(MediaType.Type.IMAGE)).contentToString()}")
        sb.appendLine("VIDEO: ${MediaCore.requiredPermissions(setOf(MediaType.Type.VIDEO)).contentToString()}")
        sb.appendLine("AUDIO: ${MediaCore.requiredPermissions(setOf(MediaType.Type.AUDIO)).contentToString()}")
        sb.appendLine("hasAll=${MediaCore.hasAllPermission()} img=${MediaCore.hasPermission(setOf(MediaType.Type.IMAGE))} vid=${MediaCore.hasPermission(setOf(MediaType.Type.VIDEO))} aud=${MediaCore.hasPermission(setOf(MediaType.Type.AUDIO))}")
        log("MediaSample", sb.toString())
        toast(sb.toString())
    }

    // ==================== 工具方法 ====================

    /** 有权限返回 true，无权限请求后返回 false */
    private fun ensurePermissionOrReturn(types: Set<MediaType.Type>): Boolean {
        return if (MediaCore.hasPermission(types)) true else {
            requestMediaPermissionLauncher.launch(MediaCore.requiredPermissions(types))
            false
        }
    }

    /** IO 协程中执行查询并 toast 结果 */
    private fun ioQuery(tag: String, block: suspend () -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try { block() } catch (e: Exception) { logE("MediaSample", "$tag error: ${e.message}") }
        }
    }

    /** 切到主线程 toast */
    private suspend fun showToast(msg: String) = withContext(Dispatchers.Main) { toast(msg) }

    // ==================== 目录列表查询 ====================

    private fun queryFoldersAllTypes() {
        ensurePermissionOrReturn(MediaType.ALL) || return
        ioQuery("queryFoldersAllTypes") {
            val folders = MediaCore.queryFolders {
                types = MediaType.ALL; pagination = Pagination.All; sortBy = SortBy.DATE_DESC
            }
            logFolderList(folders, "全部类型目录")
            showToast("共 ${folders.size} 个目录")
        }
    }

    private fun queryFoldersImageAndVideo() {
        ensurePermissionOrReturn(MediaType.IMAGE_AND_VIDEO) || return
        ioQuery("queryFoldersImageAndVideo") {
            val folders = MediaCore.queryFolders {
                types = MediaType.IMAGE_AND_VIDEO; pagination = Pagination.All
            }
            logFolderList(folders, "图片+视频目录")
            showToast("共 ${folders.size} 个目录")
        }
    }

    private fun queryFoldersImageOnly() {
        ensurePermissionOrReturn(setOf(MediaType.Type.IMAGE)) || return
        ioQuery("queryFoldersImageOnly") {
            val folders = MediaCore.queryFolders {
                types = setOf(MediaType.Type.IMAGE); pagination = Pagination.All
            }
            logFolderList(folders, "纯图片目录")
            showToast("共 ${folders.size} 个图片目录")
        }
    }

    private fun queryFoldersVideoOnly() {
        ensurePermissionOrReturn(setOf(MediaType.Type.VIDEO)) || return
        ioQuery("queryFoldersVideoOnly") {
            val folders = MediaCore.queryFolders {
                types = setOf(MediaType.Type.VIDEO); pagination = Pagination.All
            }
            logFolderList(folders, "纯视频目录")
            showToast("共 ${folders.size} 个视频目录")
        }
    }

    private fun queryFoldersAudioOnly() {
        ensurePermissionOrReturn(setOf(MediaType.Type.AUDIO)) || return
        ioQuery("queryFoldersAudioOnly") {
            val folders = MediaCore.queryFolders {
                types = setOf(MediaType.Type.AUDIO); pagination = Pagination.All
            }
            logFolderList(folders, "纯音频目录")
            showToast("共 ${folders.size} 个音频目录")
        }
    }

    private fun queryFoldersPaged(page: Int) {
        ensurePermissionOrReturn(MediaType.ALL) || return
        val pageSize = 3
        ioQuery("queryFoldersPaged") {
            val folders = MediaCore.queryFolders {
                types = MediaType.ALL; pagination = Pagination.Paged(page, pageSize); sortBy = SortBy.DATE_DESC
            }
            logFolderList(folders, "目录 第${page + 1}页")
            showToast("第${page + 1}页: ${folders.size} 个目录")
        }
    }

    private fun queryFoldersFlow() {
        ensurePermissionOrReturn(MediaType.ALL) || return
        lifecycleScope.launch {
            var pageCount = 0
            var totalFolders = 0
            MediaCore.queryFoldersFlow {
                types = MediaType.ALL; pagination = Pagination.Paged(0, 3); sortBy = SortBy.DATE_DESC
            }.catch { e -> logE("MediaSample", "queryFoldersFlow error: ${e.message}") }
             .collect { pageFolders ->
                 pageCount++
                 totalFolders += pageFolders.size
                 log("MediaSample", "Flow 第${pageCount}页: ${pageFolders.size}个 | 累计:$totalFolders")
                 for (f in pageFolders) {
                     log("MediaSample", "  [${f.name}] img:${f.images.size} vid:${f.videos.size} aud:${f.audios.size}")
                 }
             }
            showToast("Flow 完成: $pageCount 页, $totalFolders 个目录")
        }
    }

    private fun queryFoldersLiveData() {
        ensurePermissionOrReturn(MediaType.ALL) || return
        MediaCore.queryFoldersLiveData {
            types = MediaType.ALL; pagination = Pagination.All; sortBy = SortBy.DATE_DESC
        }.observe(viewLifecycleOwner) { folders ->
            logFolderList(folders, "LiveData 更新")
            toast("LiveData: ${folders.size} 个目录")
        }
    }

    // ==================== 排序 ====================

    private fun queryFoldersWithSort(@SortBy.Type sortBy: Int) {
        ensurePermissionOrReturn(setOf(MediaType.Type.IMAGE)) || return
        val name = when (sortBy) {
            SortBy.DATE_DESC -> "时间降序"
            SortBy.DATE_ASC -> "时间升序"
            SortBy.NAME_ASC -> "名称A-Z"
            SortBy.NAME_DESC -> "名称Z-A"
            else -> "未知"
        }
        ioQuery("queryFoldersWithSort") {
            val folders = MediaCore.queryFolders {
                types = setOf(MediaType.Type.IMAGE); pagination = Pagination.All; this.sortBy = sortBy
            }
            logFolderList(folders, "图片目录 ($name)")
            showToast("$name: ${folders.size} 个目录")
        }
    }

    // ==================== 目录详情 ====================

    private fun queryFirstFolderDetailAll() {
        ensurePermissionOrReturn(MediaType.ALL) || return
        ioQuery("queryFirstFolderDetailAll") {
            val folders = MediaCore.queryFolders {
                types = MediaType.ALL; pagination = Pagination.Paged(0, 1)
            }
            if (folders.isEmpty()) { showToast("没有找到任何目录"); return@ioQuery }
            val detail = MediaCore.queryFolderDetail {
                bucketId = folders[0].bucketId; types = MediaType.ALL; pagination = Pagination.All
            }
            logFolderDetail(detail, "${folders[0].name} 详情(全量)")
            showToast("[${folders[0].name}] 共 ${detail.totalCount(MediaType.ALL)} 项")
        }
    }

    private fun queryFirstFolderDetailPaged() {
        ensurePermissionOrReturn(MediaType.ALL) || return
        ioQuery("queryFirstFolderDetailPaged") {
            val folders = MediaCore.queryFolders {
                types = MediaType.ALL; pagination = Pagination.Paged(0, 1)
            }
            if (folders.isEmpty()) { showToast("没有找到任何目录"); return@ioQuery }
            val detail = MediaCore.queryFolderDetail {
                bucketId = folders[0].bucketId; types = MediaType.ALL; pagination = Pagination.Paged(0, 5)
            }
            logFolderDetail(detail, "${folders[0].name} 详情(分页)")
            showToast("[${folders[0].name}] img:${detail.images.size} vid:${detail.videos.size} aud:${detail.audios.size}")
        }
    }

    private fun queryFolderDetailFlow() {
        ensurePermissionOrReturn(MediaType.ALL) || return
        lifecycleScope.launch {
            val folders = MediaCore.queryFolders {
                types = MediaType.ALL; pagination = Pagination.Paged(0, 1)
            }
            if (folders.isEmpty()) { toast("没有找到任何目录"); return@launch }
            MediaCore.queryFolderDetailFlow {
                bucketId = folders[0].bucketId; types = setOf(MediaType.Type.IMAGE); pagination = Pagination.All
            }.collect { detail ->
                logFolderDetail(detail, "Flow-${folders[0].name}")
                toast("Flow: ${detail.images.size} 张图片")
            }
        }
    }

    // ==================== 全局资源查询 ====================

    private fun queryAllImages() {
        ensurePermissionOrReturn(setOf(MediaType.Type.IMAGE)) || return
        ioQuery("queryAllImages") {
            val items = MediaCore.queryAll { types = setOf(MediaType.Type.IMAGE); pagination = Pagination.All }
            logItems(items, "全局图片")
            showToast("共 ${items.size} 张图片")
        }
    }

    private fun queryAllVideos() {
        ensurePermissionOrReturn(setOf(MediaType.Type.VIDEO)) || return
        ioQuery("queryAllVideos") {
            val items = MediaCore.queryAll { types = setOf(MediaType.Type.VIDEO); pagination = Pagination.All }
            logItems(items, "全局视频")
            showToast("共 ${items.size} 个视频")
        }
    }

    private fun queryAllAudios() {
        ensurePermissionOrReturn(setOf(MediaType.Type.AUDIO)) || return
        ioQuery("queryAllAudios") {
            val items = MediaCore.queryAll { types = setOf(MediaType.Type.AUDIO); pagination = Pagination.All }
            logItems(items, "全局音频")
            showToast("共 ${items.size} 个音频")
        }
    }

    private fun queryAllImagesPaged() {
        ensurePermissionOrReturn(setOf(MediaType.Type.IMAGE)) || return
        ioQuery("queryAllImagesPaged") {
            val items = MediaCore.queryAll { types = setOf(MediaType.Type.IMAGE); pagination = Pagination.Paged(0, 10) }
            logItems(items, "全局图片(第1页)")
            showToast("第1页: ${items.size} 张图片")
        }
    }

    private fun queryAllImagesFlow() {
        ensurePermissionOrReturn(setOf(MediaType.Type.IMAGE)) || return
        lifecycleScope.launch {
            var count = 0
            MediaCore.queryAllFlow { types = setOf(MediaType.Type.IMAGE); pagination = Pagination.Paged(0, 20) }
                .catch { e -> logE("MediaSample", "queryAllImagesFlow error: ${e.message}") }
                .collect { item ->
                    count++
                    log("MediaSample", "[$count] ${item.name} | ${item.path}")
                }
            showToast("Flow 完成: $count 条")
        }
    }

    // ==================== 缩略图 ====================

    private fun loadFirstImageThumbnail() {
        ensurePermissionOrReturn(setOf(MediaType.Type.IMAGE)) || return
        ioQuery("loadThumbnail") {
            val items = MediaCore.queryAll { types = setOf(MediaType.Type.IMAGE); pagination = Pagination.Paged(0, 1) }
            if (items.isEmpty()) { showToast("没有图片"); return@ioQuery }
            val bitmap = ThumbnailLoader.loadThumbnail(items[0].uri)
            if (bitmap != null && !bitmap.isRecycled) {
                showToast("缩略图: ${bitmap.width}x${bitmap.height}")
                log("MediaSample", "缩略图 OK: ${bitmap.width}x${bitmap.height}")
            } else {
                showToast("缩略图加载失败")
            }
        }
    }

    private fun loadBatchThumbnails() {
        ensurePermissionOrReturn(setOf(MediaType.Type.IMAGE)) || return
        ioQuery("loadBatchThumbnails") {
            val items = MediaCore.queryAll { types = setOf(MediaType.Type.IMAGE); pagination = Pagination.Paged(0, 10) }
            if (items.isEmpty()) { showToast("没有图片"); return@ioQuery }
            val uris = items.map { it.uri }
            val result = ThumbnailLoader.loadThumbnails(uris)
            log("MediaSample", "批量缩略图: ${uris.size}请求, ${result.size}成功")
            showToast("批量缩略图: ${result.size}/${uris.size} 成功")
        }
    }

    private fun clearThumbnailCache() {
        ThumbnailLoader.clearCache()
        toast("缩略图缓存已清除")
    }

    // ==================== 缓存管理 ====================

    private fun clearFolderCache() {
        lifecycleScope.launch { MediaCore.clearCache(); toast("目录缓存已清除") }
    }

    // ==================== 日志工具 ====================

    private fun logFolderList(folders: List<MediaFolder>, tag: String) {
        val sb = StringBuilder("========== $tag ==========\n共 ${folders.size} 个目录:\n")
        for ((i, f) in folders.withIndex()) {
            sb.append("  [$i] ${f.name} | path=${f.path} | img:${f.images.size} vid:${f.videos.size} aud:${f.audios.size}\n")
        }
        log("MediaSample", sb.toString())
    }

    private fun logFolderDetail(d: app.allever.android.lib.media.core.query.MediaFolderDetail, tag: String) {
        val sb = StringBuilder("========== $tag ==========\n文件夹: ${d.folder.name}\n路径: ${d.folder.path}\n")
        sb.append("--- 图片(${d.images.size}) ---\n")
        for (img in d.images.take(10)) { sb.append("  ${img.name} | ${img.width}x${img.height} | ${formatSize(img.size)}\n") }
        if (d.images.size > 10) sb.append("  ... 还有${d.images.size - 10}张\n")
        sb.append("--- 视频(${d.videos.size}) ---\n")
        for (vid in d.videos.take(10)) { sb.append("  ${vid.name} | ${vid.duration}ms | ${vid.width}x${vid.height}\n") }
        if (d.videos.size > 10) sb.append("  ... 还有${d.videos.size - 10}个\n")
        sb.append("--- 音频(${d.audios.size}) ---\n")
        for (aud in d.audios.take(10)) { sb.append("  ${aud.name} | ${aud.duration}ms | ${aud.artist}\n") }
        if (d.audios.size > 10) sb.append("  ... 还有${d.audios.size - 10}个\n")
        sb.append("总计: ${d.totalCount(MediaType.ALL)} 项\n")
        log("MediaSample", sb.toString())
    }

    private fun logItems(items: List<MediaItem>, tag: String) {
        val sb = StringBuilder("========== $tag ==========\n共 ${items.size} 项:\n")
        for ((i, item) in items.take(20).withIndex()) {
            when (item) {
                is MediaItem.Image ->
                    sb.append("  [$i] [IMG] ${item.name} | ${item.width}x${item.height} | ${formatSize(item.size)}\n")
                is MediaItem.Video ->
                    sb.append("  [$i] [VID] ${item.name} | ${item.duration}ms | ${item.width}x${item.height}\n")
                is MediaItem.Audio ->
                    sb.append("  [$i] [AUD] ${item.name} | ${item.duration}ms | ${item.artist}\n")
            }
        }
        if (items.size > 20) sb.append("  ... 还有 ${items.size - 20} 项\n")
        log("MediaSample", sb.toString())
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        else -> "${bytes / 1024 / 1024}MB"
    }

    // ==================== 媒体选择器示例 ====================

    private val mediaPickerLauncher = registerForActivityResult(
        app.allever.android.lib.media.picker.MediaPickerContract()
    ) { items ->
        if (items.isNotEmpty()) {
            val sb = StringBuilder("选择器返回 ${items.size} 项:\n")
            items.forEachIndexed { index, item ->
                sb.append("  [$index+1] [${item::class.simpleName}] ${item.name}\n")
            }
            log("MediaSample", sb.toString())
            toast("已选 ${items.size} 项资源")
        } else {
            toast("未选择任何资源")
        }
    }

    private fun openMediaPickerAllTypes() {
        mediaPickerLauncher.launch(
            app.allever.android.lib.media.picker.MediaPickerConfig(
                types = setOf(MediaType.Type.IMAGE, MediaType.Type.VIDEO, MediaType.Type.AUDIO),
                maxSelect = 9,
            )
        )
    }

    private fun openMediaPickerImageOnly() {
        mediaPickerLauncher.launch(
            app.allever.android.lib.media.picker.MediaPickerConfig(
                types = setOf(MediaType.Type.IMAGE),
                maxSelect = 6,
            )
        )
    }

    private fun openMediaPickerVideoOnly() {
        mediaPickerLauncher.launch(
            app.allever.android.lib.media.picker.MediaPickerConfig(
                types = setOf(MediaType.Type.VIDEO),
                maxSelect = 3,
            )
        )
    }
}
