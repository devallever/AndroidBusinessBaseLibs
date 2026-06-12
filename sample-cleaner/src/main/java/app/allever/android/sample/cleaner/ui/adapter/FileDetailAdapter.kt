package app.allever.android.sample.cleaner.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.view.LayoutInflater
import android.view.ViewGroup
import app.allever.android.sample.cleaner.file.FileCategory
import app.allever.android.sample.cleaner.file.FileInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * 文件详情适配器
 *
 * 根据文件类型使用不同的布局和填充逻辑，
 * 展示每种文件类型最关键的信息：
 * - 视频：缩略图 + 时长 + 分辨率 + 路径 + 大小（异步加载）
 * - 音频：图标 + 时长 + 路径 + 大小（异步加载）
 * - 图片：缩略图 + 尺寸 + 路径 + 大小
 * - 文档：图标 + 文档类型 + 路径 + 大小
 * - APK：图标 + 包名 + 版本 + 大小
 * - 压缩包/其他：图标 + 类型 + 路径 + 大小
 */
class FileDetailAdapter(
    private val category: FileCategory
) : BaseQuickAdapter<FileInfo, BaseViewHolder>(0) {

    // ========== 视频元数据缓存 ==========

    /** 视频元数据：时长、分辨率、缩略图 */
    data class VideoMeta(
        val durationMs: Long,
        val width: Int,
        val height: Int,
        val thumbnail: Bitmap?
    )

    /** 音频元数据：时长 */
    data class AudioMeta(val durationMs: Long)

    /** 图片元数据：尺寸、缩略图 */
    data class ImageMeta(
        val width: Int,
        val height: Int,
        val thumbnail: Bitmap?
    )

    companion object {
        // 缩略图目标尺寸
        private const val THUMB_WIDTH = 140
        private const val THUMB_HEIGHT = 98

        // 最大缓存数量（约 20 个视频/音频项）
        private const val MAX_CACHE_SIZE = 30

        /** 视频元数据缓存 */
        private val videoMetaCache = object : LruCache<String, VideoMeta>(
            (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt().coerceAtLeast(MAX_CACHE_SIZE)
        ) {
            override fun sizeOf(key: String, value: VideoMeta): Int {
                var size = 128L // 元数据本身
                value.thumbnail?.let { size += it.allocationByteCount / 1024 }
                return size.toInt()
            }
        }

        /** 音频元数据缓存 */
        private val audioMetaCache = ConcurrentHashMap<String, AudioMeta>()

        /** 图片元数据缓存 */
        private val imageMetaCache = object : LruCache<String, ImageMeta>(
            MAX_CACHE_SIZE
        ) {
            override fun sizeOf(key: String, value: ImageMeta): Int {
                var size = 64L
                value.thumbnail?.let { size += it.allocationByteCount / 1024 }
                return size.toInt()
            }
        }

        /** 主线程 Handler，用于回调更新 UI */
        private val mainHandler = Handler(Looper.getMainLooper())
    }

    /** 协程作用域，用于后台任务 */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 记录正在加载中的路径，防止重复提交任务 */
    private val loadingPaths = mutableSetOf<String>()

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val ctx = parent.context
        val inflater = LayoutInflater.from(ctx)

        return when (category) {
            FileCategory.VIDEO -> {
                val binding = app.allever.android.sample.cleaner.databinding.ItemFileVideoBinding.inflate(inflater, parent, false)
                BaseViewHolder(binding.root)
            }
            FileCategory.AUDIO -> {
                val binding = app.allever.android.sample.cleaner.databinding.ItemFileAudioBinding.inflate(inflater, parent, false)
                BaseViewHolder(binding.root)
            }
            FileCategory.IMAGE -> {
                val binding = app.allever.android.sample.cleaner.databinding.ItemFileImageBinding.inflate(inflater, parent, false)
                BaseViewHolder(binding.root)
            }
            FileCategory.DOCUMENT -> {
                val binding = app.allever.android.sample.cleaner.databinding.ItemFileDocumentBinding.inflate(inflater, parent, false)
                BaseViewHolder(binding.root)
            }
            FileCategory.APK -> {
                val binding = app.allever.android.sample.cleaner.databinding.ItemFileApkBinding.inflate(inflater, parent, false)
                BaseViewHolder(binding.root)
            }
            else -> {
                val binding = app.allever.android.sample.cleaner.databinding.ItemFileGenericBinding.inflate(inflater, parent, false)
                BaseViewHolder(binding.root)
            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: FileInfo) {
        when (category) {
            FileCategory.VIDEO -> bindVideo(holder, item)
            FileCategory.AUDIO -> bindAudio(holder, item)
            FileCategory.IMAGE -> bindImage(holder, item)
            FileCategory.DOCUMENT -> bindDocument(holder, item)
            FileCategory.APK -> bindApk(holder, item)
            else -> bindGeneric(holder, item)
        }
    }

    /**
     * 清理资源，在 Activity/Fragment 销毁时调用
     */
    fun release() {
        scope.cancel()
        loadingPaths.clear()
    }

    // ========== 视频绑定：异步加载缩略图 + 时长 + 分辨率 ==========

    private fun bindVideo(holder: BaseViewHolder, item: FileInfo) {
        val binding = app.allever.android.sample.cleaner.databinding.ItemFileVideoBinding.bind(holder.itemView)
        val path = item.absolutePath

        // 1. 主线程只做轻量操作：设置文字信息
        binding.tvFileName.text = item.fileName
        binding.tvFilePath.text = formatPath(path)
        binding.tvFileSize.text = item.formattedSize

        // 2. 检查缓存
        val cached = videoMetaCache.get(path)
        if (cached != null) {
            // 缓存命中 → 直接设置（主线程，<1ms）
            applyVideoMeta(binding, cached)
            return
        }

        // 3. 无缓存 → 先显示占位状态，再提交后台任务
        binding.tvDuration.text = "--:--"
        binding.tvResolution.visibility = android.view.View.GONE
        binding.ivThumbnail.setImageBitmap(null) // 清除旧图或显示占位

        if (!loadingPaths.contains(path)) {
            loadingPaths.add(path)
            submitVideoExtractTask(path, holder.layoutPosition)
        }
    }

    /**
     * 将视频元数据应用到 UI（仅主线程调用）
     */
    private fun applyVideoMeta(
        binding: app.allever.android.sample.cleaner.databinding.ItemFileVideoBinding,
        meta: VideoMeta
    ) {
        binding.tvDuration.text = formatDuration(meta.durationMs)
        if (meta.width > 0 && meta.height > 0) {
            binding.tvResolution.text = "${meta.width}x${meta.height}"
            binding.tvResolution.visibility = android.view.View.VISIBLE
        } else {
            binding.tvResolution.visibility = android.view.View.GONE
        }
        meta.thumbnail?.let { binding.ivThumbnail.setImageBitmap(it) }
    }

    /**
     * 提交视频元数据提取到后台线程
     */
    private fun submitVideoExtractTask(path: String, position: Int) {
        scope.launch {
            try {
                val meta = extractVideoMeta(path)
                videoMetaCache.put(path, meta)

                // 回到主线程更新对应位置的 Item
                mainHandler.post {
                    loadingPaths.remove(path)
                    // 只有当该位置仍然显示同一数据时才更新（避免错位）
                    if (position < data.size && data[position].absolutePath == path) {
                        val holder = recyclerView?.findViewHolderForLayoutPosition(position)
                        if (holder != null) {
                            val binding = app.allever.android.sample.cleaner.databinding.ItemFileVideoBinding.bind(holder.itemView)
                            applyVideoMeta(binding, meta)
                        }
                    }
                }
            } catch (_: Exception) {
                mainHandler.post { loadingPaths.remove(path) }
            }
        }
    }

    /**
     * 在 IO 线程提取视频元数据和缩略图
     */
    private fun extractVideoMeta(path: String): VideoMeta {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(path)

            // 时长
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

            // 分辨率
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0

            // 缩略图（解码后立即采样压缩至目标尺寸）
            val rawFrame = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            val thumbnail = scaleBitmap(rawFrame, THUMB_WIDTH, THUMB_HEIGHT)

            return VideoMeta(durationMs, width, height, thumbnail)
        } finally {
            retriever.release()
        }
    }

    // ========== 音频绑定：异步加载时长 ==========

    private fun bindAudio(holder: BaseViewHolder, item: FileInfo) {
        val binding = app.allever.android.sample.cleaner.databinding.ItemFileAudioBinding.bind(holder.itemView)
        val path = item.absolutePath

        binding.tvFileName.text = item.fileName
        binding.tvFilePath.text = formatPath(path)
        binding.tvFileSize.text = item.formattedSize

        // 检查缓存
        val cached = audioMetaCache[path]
        if (cached != null) {
            binding.tvDuration.text = "时长: ${formatDuration(cached.durationMs)}"
            return
        }

        binding.tvDuration.text = "时长: --:--"

        if (!loadingPaths.contains("audio:$path")) {
            loadingPaths.add("audio:$path")
            scope.launch {
                try {
                    val durationMs = extractAudioDuration(path)
                    audioMetaCache[path] = AudioMeta(durationMs)

                    mainHandler.post {
                        loadingPaths.remove("audio:$path")
                        val pos = data.indexOfFirst { it.absolutePath == path }
                        if (pos >= 0) {
                            val holder = recyclerView?.findViewHolderForLayoutPosition(pos)
                            if (holder != null) {
                                val b = app.allever.android.sample.cleaner.databinding.ItemFileAudioBinding.bind(holder.itemView)
                                b.tvDuration.text = "时长: ${formatDuration(durationMs)}"
                            }
                        }
                    }
                } catch (_: Exception) {
                    mainHandler.post { loadingPaths.remove("audio:$path") }
                }
            }
        }
    }

    private fun extractAudioDuration(path: String): Long {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(path)
            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } finally {
            retriever.release()
        }
    }

    // ========== 图片绑定：缩略图 + 尺寸 ==========

    private fun bindImage(holder: BaseViewHolder, item: FileInfo) {
        val binding = app.allever.android.sample.cleaner.databinding.ItemFileImageBinding.bind(holder.itemView)
        val path = item.absolutePath

        binding.tvFileName.text = item.fileName
        binding.tvFilePath.text = formatPath(path)
        binding.tvFileSize.text = item.formattedSize

        // 检查缓存
        val cached = imageMetaCache.get(path)
        if (cached != null) {
            binding.tvResolution.text = "${cached.width} x ${cached.height}"
            cached.thumbnail?.let { binding.ivThumbnail.setImageBitmap(it) }
            return
        }

        binding.tvResolution.text = "..."
        binding.ivThumbnail.setImageBitmap(null)

        if (!loadingPaths.contains("img:$path")) {
            loadingPaths.add("img:$path")
            scope.launch {
                try {
                    val meta = extractImageMeta(path)
                    imageMetaCache.put(path, meta)

                    mainHandler.post {
                        loadingPaths.remove("img:$path")
                        val pos = data.indexOfFirst { it.absolutePath == path }
                        if (pos >= 0) {
                            val h = recyclerView?.findViewHolderForLayoutPosition(pos)
                            if (h != null) {
                                val b = app.allever.android.sample.cleaner.databinding.ItemFileImageBinding.bind(h.itemView)
                                b.tvResolution.text = "${meta.width} x ${meta.height}"
                                meta.thumbnail?.let { b.ivThumbnail.setImageBitmap(it) }
                            }
                        }
                    }
                } catch (_: Exception) {
                    mainHandler.post { loadingPaths.remove("img:$path") }
                }
            }
        }
    }

    private fun extractImageMeta(path: String): ImageMeta {
        // 先获取尺寸
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, boundsOptions)
        val w = boundsOptions.outWidth
        val h = boundsOptions.outHeight

        // 采样压缩解码
        val sampleSize = calculateInSampleSize(w, h, THUMB_WIDTH, THUMB_HEIGHT)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val raw = BitmapFactory.decodeFile(path, decodeOptions)
        val thumb = scaleBitmap(raw, THUMB_WIDTH, THUMB_HEIGHT)

        return ImageMeta(w, h, thumb)
    }

    // ========== 文档绑定：类型标签 ==========

    private fun bindDocument(holder: BaseViewHolder, item: FileInfo) {
        val binding = app.allever.android.sample.cleaner.databinding.ItemFileDocumentBinding.bind(holder.itemView)

        binding.tvFileName.text = item.fileName
        binding.tvFilePath.text = formatPath(item.absolutePath)
        binding.tvFileSize.text = item.formattedSize
        binding.tvFileType.text = getDocumentTypeLabel(item.file.extension.lowercase())
    }

    // ========== APK 绑定：包名 + 版本 ==========

    private fun bindApk(holder: BaseViewHolder, item: FileInfo) {
        val binding = app.allever.android.sample.cleaner.databinding.ItemFileApkBinding.bind(holder.itemView)

        binding.tvFileName.text = item.fileName
        binding.tvFileSize.text = item.formattedSize

        try {
            val packageManager = context?.packageManager ?: return
            val packageInfo = packageManager.getPackageArchiveInfo(
                item.absolutePath,
                android.content.pm.PackageManager.GET_ACTIVITIES
            )
            if (packageInfo != null) {
                val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
                binding.tvPackageName.text = "包名: ${packageInfo.packageName}"
                @Suppress("DEPRECATION")
                binding.tvVersionName.text = "版本: ${packageInfo.versionName} (${packageInfo.versionCode})"
                binding.tvFileName.text = if (appName.isNotEmpty()) "$appName (${item.fileName})" else item.fileName
            } else {
                binding.tvPackageName.text = "无法读取包信息"
                binding.tvVersionName.text = ""
            }
        } catch (_: Exception) {
            binding.tvPackageName.text = "无法读取包信息"
            binding.tvVersionName.text = ""
        }
    }

    // ========== 通用绑定（压缩包/其他） ==========

    private fun bindGeneric(holder: BaseViewHolder, item: FileInfo) {
        val binding = app.allever.android.sample.cleaner.databinding.ItemFileGenericBinding.bind(holder.itemView)

        binding.tvFileName.text = item.fileName
        binding.tvFilePath.text = formatPath(item.absolutePath)
        binding.tvFileSize.text = item.formattedSize
        binding.tvFileType.text = getFileTypeLabel(category, item.file.extension.lowercase())
    }

    // ========== 工具方法 ==========

    /**
     * 缩放 Bitmap 到目标尺寸以内（保持宽高比）
     */
    private fun scaleBitmap(source: Bitmap?, maxWidth: Int, maxHeight: Int): Bitmap? {
        if (source == null) return null
        if (source.width <= maxWidth && source.height <= maxHeight) return source

        val ratio = minOf(maxWidth.toFloat() / source.width, maxHeight.toFloat() / source.height)
        val w = (source.width * ratio).toInt()
        val h = (source.height * ratio).toInt()

        val scaled = Bitmap.createScaledBitmap(source, w, h, true)
        if (scaled !== source) source.recycle()
        return scaled
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun formatPath(path: String): String {
        return if (path.length > 50) "..." + path.takeLast(47) else path
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getDocumentTypeLabel(ext: String): String = when (ext) {
        "pdf" -> "PDF 文档"
        "doc", "docx" -> "Word 文档"
        "xls", "xlsx" -> "Excel 表格"
        "ppt", "pptx" -> "PowerPoint 演示"
        "txt" -> "文本文件"
        "md" -> "Markdown 文档"
        else -> "${ext.uppercase()} 文件"
    }

    private fun getFileTypeLabel(category: FileCategory, ext: String): String = when (category) {
        FileCategory.ARCHIVE -> when (ext) {
            "zip" -> "ZIP 压缩包"
            "rar" -> "RAR 压缩包"
            "7z" -> "7Z 压缩包"
            else -> "${ext.uppercase()} 压缩包"
        }
        else -> "${ext.uppercase()} 文件"
    }
}
