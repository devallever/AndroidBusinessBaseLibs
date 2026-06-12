package app.allever.android.sample.cleaner.scanner

import app.allever.android.sample.cleaner.core.CleanType
import java.io.File

/**
 * 垃圾文件识别规则
 *
 * 对应文档中的5种垃圾文件识别规则：
 * - 路径匹配：根据路径正则匹配
 * - 文件扩展名：根据后缀名匹配
 * - 文件名模式：根据文件名正则匹配
 * - 文件大小：根据大小阈值过滤
 * - 文件年龄：根据时间阈值过滤
 *
 * 单一职责原则：每个 Rule 只负责一种类型的垃圾文件定义
 */
data class JunkRule(
    /** 规则对应的清理类型 */
    val type: CleanType,

    /** 路径匹配正则列表，如 ".*[/\\\\]cache[/\\\\].*" 匹配 cache 目录下的文件 */
    val pathPatterns: List<Regex> = emptyList(),

    /** 文件扩展名集合，如 log, tmp, temp */
    val extensions: Set<String> = emptySet(),

    /** 文件名正则列表 */
    val namePatterns: List<Regex> = emptyList(),

    /** 最小文件大小过滤（字节），null 表示不限制 */
    val minSize: Long? = null,

    /** 最大文件大小过滤（字节），null 表示不限制 */
    val maxSize: Long? = null,

    /** 最大文件年龄（天），超过此天数视为过期，null 表示不限制 */
    val maxAgeDays: Int? = null
) {

    /**
     * 判断文件是否匹配本规则
     *
     * @param file 待检测文件
     * @param rootPath 扫描根目录（用于相对路径匹配）
     * @return 是否匹配
     */
    fun matches(file: File, rootPath: String = ""): Boolean {
        if (!file.isFile) return false

        // 路径匹配：使用绝对路径，避免相对路径截断导致模式失效
        if (pathPatterns.isNotEmpty()) {
            val absolutePath = file.absolutePath
            val matched = pathPatterns.any { it.containsMatchIn(absolutePath) }
            if (!matched) return false
        }

        // 扩展名匹配
        if (extensions.isNotEmpty()) {
            val ext = file.extension.lowercase()
            if (ext !in extensions) return false
        }

        // 文件名匹配
        if (namePatterns.isNotEmpty()) {
            val nameMatched = namePatterns.any { it.containsMatchIn(file.name) }
            if (!nameMatched) return false
        }

        // 大小过滤
        minSize?.let { if (file.length() < it) return false }
        maxSize?.let { if (file.length() > it) return false }

        // 年龄过滤
        maxAgeDays?.let { maxAge ->
            val ageMs = System.currentTimeMillis() - file.lastModified()
            val ageDays = ageMs / (1000L * 60 * 60 * 24)
            if (ageDays <= maxAge) return false
        }

        return true
    }

    companion object {
        /** 应用缓存规则：cache 目录下的所有文件 */
        fun cacheRule(): JunkRule = JunkRule(
            type = CleanType.CACHE,
            pathPatterns = listOf(
                Regex(".*[/\\\\]cache[/\\\\].*", RegexOption.IGNORE_CASE),
                Regex(".*[/\\\\]Cache[/\\\\].*", RegexOption.IGNORE_CASE)
            )
            // 不限制扩展名，cache 目录下的文件均可清理
        )

        /** 日志文件规则 */
        fun logRule(): JunkRule = JunkRule(
            type = CleanType.LOG,
            extensions = setOf("log"),
            pathPatterns = listOf(Regex(".*\\.log$", RegexOption.IGNORE_CASE))
        )

        /** 临时文件规则 */
        fun tempRule(): JunkRule = JunkRule(
            type = CleanType.TEMP,
            extensions = setOf("tmp", "temp", "bak", "swp"),
            pathPatterns = listOf(
                Regex(".*[/\\\\](temp|tmp)[/\\\\].*", RegexOption.IGNORE_CASE)
            )
        )

        /** 广告缓存规则 */
        fun adCacheRule(): JunkRule = JunkRule(
            type = CleanType.AD_CACHE,
            pathPatterns = listOf(
                Regex(".*[/\\\\]ad[_-]?cache[/\\\\].*", RegexOption.IGNORE_CASE),
                Regex(".*[/\\\\]ads[/\\\\].*", RegexOption.IGNORE_CASE),
                Regex(".*[/\\\\]admob[/\\\\].*", RegexOption.IGNORE_CASE),
                Regex(".*[/\\\\]applovin[/\\\\].*", RegexOption.IGNORE_CASE),
                Regex(".*[/\\\\]csj[/\\\\].*", RegexOption.IGNORE_CASE),
                Regex(".*[/\\\\]pangle[/\\\\].*", RegexOption.IGNORE_CASE)
            )
        )

        /** 残留文件规则（已卸载应用的遗留目录） */
        fun residualRule(): JunkRule = JunkRule(
            type = CleanType.RESIDUAL,
            pathPatterns = listOf(
                Regex(".*/Android/data/[^/]+/$", RegexOption.IGNORE_CASE),
                Regex(".*/Android/obb/[^/]+/$", RegexOption.IGNORE_CASE)
            )
        )

        /** 安装包规则：*.apk 文件（Download 目录及外部存储根目录） */
        fun apkRule(): JunkRule = JunkRule(
            type = CleanType.APK,
            extensions = setOf("apk"),
            minSize = 1024 * 100,  // 最小 100KB，排除空文件
            pathPatterns = listOf(
                Regex(".*/Download/.*\\.apk$", RegexOption.IGNORE_CASE),
                Regex(".*/download/.*\\.apk$", RegexOption.IGNORE_CASE),
                // 外部存储根目录下的 APK
                Regex("^/storage/emulated/0/[^/]+\\.apk$", RegexOption.IGNORE_CASE)
            )
        )

        /** 获取所有内置规则 */
        fun builtInRules(): List<JunkRule> = listOf(
            cacheRule(),
            logRule(),
            tempRule(),
            adCacheRule(),
            residualRule(),
            apkRule()
        )
    }
}
