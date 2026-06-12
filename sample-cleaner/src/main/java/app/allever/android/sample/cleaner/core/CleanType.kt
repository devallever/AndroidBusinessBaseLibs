package app.allever.android.sample.cleaner.core

/**
 * 清理类型枚举
 *
 * 对应文档中的垃圾文件类型：应用缓存、系统缓存、日志文件、临时文件、残留文件、广告缓存
 * 以及扩展的大文件扫描和重复文件检测
 */
enum class CleanType(val displayName: String) {
    /** 应用缓存：图片/视频/网络缓存 */
    CACHE("应用缓存"),

    /** 日志文件：*.log 文件 */
    LOG("日志文件"),

    /** 临时文件：*.tmp / *.temp 等临时文件 */
    TEMP("临时文件"),

    /** 残留文件：应用卸载后残留的文件 */
    RESIDUAL("残留文件"),

    /** 广告缓存：Ad SDK 生成的缓存 */
    AD_CACHE("广告缓存"),

    /** 大文件扫描（>= 阈值） */
    LARGE_FILE("大文件"),

    /** 重复文件检测 */
    DUPLICATE_FILE("重复文件"),

    /** 全部清理 */
    ALL("全部清理")
}
