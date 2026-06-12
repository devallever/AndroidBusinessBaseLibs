package app.allever.android.sample.cleaner.scanner

import app.allever.android.sample.cleaner.core.CleanType
import java.io.File

/**
 * 扫描到的垃圾文件项
 *
 * 封装单个被识别为"可清理"的文件的完整信息，
 * 用于 UI 展示和后续清理操作。
 *
 * @param file 文件引用
 * @param type 对应的清理类型
 * @param size 文件大小（字节）
 * @param lastModified 最后修改时间戳
 * @param isDirectory 是否为目录
 * @param selected 用户是否选中（默认选中）
 */
data class JunkFileItem(
    val file: File,
    val type: CleanType,
    val size: Long = if (file.exists()) file.length() else 0L,
    val lastModified: Long = if (file.exists()) file.lastModified() else 0L,
    val isDirectory: Boolean = file.isDirectory,
    var selected: Boolean = true
) : Comparable<JunkFileItem> {

    /** 文件名 */
    val fileName: String get() = file.name

    /** 文件绝对路径 */
    val absolutePath: String get() = file.absolutePath

    /** 格式化后的文件大小 */
    val formattedSize: String get() = formatFileSize(size)

    override fun compareTo(other: JunkFileItem): Int {
        // 按大小降序排列
        return other.size.compareTo(this.size)
    }

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

        /**
         * 从 File 和 CleanType 创建实例
         */
        fun from(file: File, type: CleanType): JunkFileItem = JunkFileItem(
            file = file,
            type = type
        )
    }
}
