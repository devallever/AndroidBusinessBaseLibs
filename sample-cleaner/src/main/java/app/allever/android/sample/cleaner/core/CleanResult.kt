package app.allever.android.sample.cleaner.core

import java.io.File

/**
 * 清理结果数据类
 *
 * @param type 清理类型
 * @param success 是否成功
 * @param cleanedSize 清理释放的字节数
 * @param cleanedCount 清理的文件数量
 * @param costTimeMs 耗时(毫秒)
 * @param cleanedFiles 已清理的文件列表（用于展示）
 */
data class CleanResult(
    val type: CleanType,
    val success: Boolean,
    val cleanedSize: Long = 0L,
    val cleanedCount: Int = 0,
    val costTimeMs: Long = 0L,
    val cleanedFiles: List<File> = emptyList()
) {

    companion object {
        /** 创建空结果 */
        fun empty(type: CleanType) = CleanResult(type, true)

        /** 创建失败结果 */
        fun failed(type: CleanType, message: String? = null) =
            CleanResult(type, false)
    }
}
