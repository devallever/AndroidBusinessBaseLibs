package app.allever.android.sample.cleaner.file

import java.io.File

/**
 * 文件分类枚举
 *
 * 对应文档"文件管理-文件分类"章节。
 * 按类型、大小、时间等维度对文件进行分类。
 */
enum class FileCategory(val displayName: String) {
    /** 视频文件 */
    VIDEO("视频"),

    /** 音频文件 */
    AUDIO("音频"),

    /** 图片文件 */
    IMAGE("图片"),

    /** 文档文件 */
    DOCUMENT("文档"),

    /** 安装包 */
    APK("安装包"),

    /** 压缩包 */
    ARCHIVE("压缩包"),

    /** 其他文件 */
    OTHER("其他"),

    /** 未知类型 */
    UNKNOWN("未知");

    /**
     * 获取该分类对应的扩展名集合（用于过滤）
     */
    fun extensions(): Set<String> = when (this) {
        VIDEO -> setOf("mp4", "avi", "mkv", "mov", "3gp", "flv")
        AUDIO -> setOf("mp3", "wav", "aac", "flac", "ogg")
        IMAGE -> setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
        DOCUMENT -> setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
        APK -> setOf("apk")
        ARCHIVE -> setOf("zip", "rar", "7z", "tar", "gz")
        else -> emptySet()
    }

    companion object {
        /**
         * 根据文件扩展名判断分类
         */
        fun fromExtension(extension: String): FileCategory = when (extension.lowercase()) {
            "mp4", "avi", "mkv", "mov", "3gp", "flv", "wmv", "rmvb" -> VIDEO
            "mp3", "wav", "aac", "flac", "ogg", "wma", "m4a" -> AUDIO
            "jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic" -> IMAGE
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md" -> DOCUMENT
            "apk" -> APK
            "zip", "rar", "7z", "tar", "gz", "bz2" -> ARCHIVE
            else -> OTHER
        }
    }
}

/**
 * 文件信息数据类
 *
 * @param file 文件引用
 * @param category 文件分类
 * @param size 文件大小（字节）
 * @param lastModified 最后修改时间
 */
data class FileInfo(
    val file: java.io.File,
    val category: FileCategory,
    val size: Long = if (file.exists()) file.length() else 0L,
    val lastModified: Long = if (file.exists()) file.lastModified() else 0L
) : Comparable<FileInfo> {

    val fileName: String get() = file.name
    val absolutePath: String get() = file.absolutePath
    val formattedSize: String get() = formatFileSize(size)

    override fun compareTo(other: FileInfo): Int = other.size.compareTo(this.size)

    companion object {
        /**
         * 格式化文件大小为人类可读字符串
         */
        fun formatFileSize(size: Long): String {
            if (size < 0) return "0B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var unitIndex = 0
            var value = size.toDouble()
            while (value >= 1024 && unitIndex < units.lastIndex) {
                value /= 1024
                unitIndex++
            }
            return if (unitIndex == 0) "${size.toLong()}${units[unitIndex]}"
            else String.format("%.1f%s", value, units[unitIndex])
        }
    }
}
