package app.allever.android.sample.audiovideo.lib

/**
 * 播放器错误代码定义
 *
 * 包含所有播放器可能遇到的错误类型及其对应的错误信息。
 * 使用常量对象而非硬编码，便于统一管理和国际化。
 *
 * 错误代码范围：
 * - 1xxx: 通用错误
 * - 2xxx: 数据源错误
 * - 3xxx: Surface/渲染错误
 * - 4xxx: 网络错误
 * - 5xxx: 系统错误
 */
object PlayerErrorCode {

    // ==================== 通用错误 (1000-1999) ====================

    /** 未知错误 (1000) */
    const val UNKNOWN = 1000

    /** 操作不支持 (1001) */
    const val UNSUPPORTED_OPERATION = 1001

    /** 无效参数 (1002) */
    const val INVALID_PARAMETER = 1002

    /** 无效状态转换 (1003) */
    const val INVALID_STATE_TRANSITION = 1003

    /** 播放器未初始化 (1004) */
    const val PLAYER_NOT_INITIALIZED = 1004

    /** Surface 未就绪 (1005) */
    const val SURFACE_NOT_READY = 1005

    /** 超时错误 (1006) */
    const val TIMEOUT = 1006

    /** 重试次数耗尽 (1007) */
    const val RETRY_EXHAUSTED = 1007

    // ==================== 数据源错误 (2000-2999) ====================

    /** 数据源为空 (2000) */
    const val SOURCE_EMPTY = 2000

    /** 数据源格式不支持 (2001) */
    const val SOURCE_FORMAT_UNSUPPORTED = 2001

    /** 文件不存在 (2002) */
    const val FILE_NOT_FOUND = 2002

    /** 文件读取失败 (2003) */
    const val FILE_READ_ERROR = 2003

    /** Assets 文件不存在 (2004) */
    const val ASSET_FILE_NOT_FOUND = 2004

    /** Assets 文件复制失败 (2005) */
    const val ASSET_COPY_FAILED = 2005

    /** URI 格式无效 (2006) */
    const val INVALID_URI = 2006

    /** Content Provider 访问失败 (2007) */
    const val CONTENT_PROVIDER_ERROR = 2007

    // ==================== Surface/渲染错误 (3000-3999) ====================

    /** Surface 创建失败 (3000) */
    const val SURFACE_CREATE_FAILED = 3000

    /** Surface 切换失败 (3001) */
    const val SURFACE_SWITCH_FAILED = 3001

    /** 视频解码失败 (3002) */
    const val VIDEO_DECODE_ERROR = 3002

    /** 音频解码失败 (3003) */
    const val AUDIO_DECODE_ERROR = 3003

    /** 渲染器初始化失败 (3004) */
    const val RENDERER_INIT_FAILED = 3004

    /** MediaCodec 配置失败 (3005) */
    const val MEDIA_CODEC_CONFIG_FAILED = 3005

    // ==================== 网络错误 (4000-4999) ====================

    /** 网络连接失败 (4000) */
    const val NETWORK_CONNECTION_FAILED = 4000

    /** 网络超时 (4001) */
    const val NETWORK_TIMEOUT = 4001

    /** DNS 解析失败 (4002) */
    const val DNS_RESOLUTION_FAILED = 4002

    /** HTTP 错误 (4003) */
    const val HTTP_ERROR = 4003

    /** SSL/TLS 错误 (4004) */
    const val SSL_ERROR = 4004

    /** 带宽不足 (4005) */
    const val INSUFFICIENT_BANDWIDTH = 4005

    /** 服务器返回错误 (4006) */
    const val SERVER_ERROR = 4006

    // ==================== 系统错误 (5000-5999) ====================

    /** 内存不足 (5000) */
    const val OUT_OF_MEMORY = 5000

    /** 存储空间不足 (5001) */
    const val INSUFFICIENT_STORAGE = 5001

    /** 权限被拒绝 (5002) */
    const val PERMISSION_DENIED = 5002

    /** MediaPlayer 内部错误 (5003) */
    const val MEDIA_PLAYER_INTERNAL_ERROR = 5003

    /** ExoPlayer 内部错误 (5004) */
    const val EXO_PLAYER_INTERNAL_ERROR = 5004

    /** IjkMediaPlayer 内部错误 (5005) */
    const val IJK_MEDIA_PLAYER_INTERNAL_ERROR = 5005

    // ==================== 准备阶段错误 (6000-6999) ====================

    /** 准备超时 (6000) */
    const val PREPARE_TIMEOUT = 6000

    /** 准备被中断 (6001) */
    const val PREPARE_INTERRUPTED = 6001

    /** 准备失败（未知原因）(6002) */
    const val PREPARE_FAILED = 6002

    /**
     * 获取错误信息
     *
     * @param errorCode 错误代码
     * @return 对应的错误消息字符串
     */
    fun getMessage(errorCode: Int): String {
        return when (errorCode) {
            // 通用错误
            UNKNOWN -> "Unknown error"
            UNSUPPORTED_OPERATION -> "Operation not supported"
            INVALID_PARAMETER -> "Invalid parameter"
            INVALID_STATE_TRANSITION -> "Invalid state transition"
            PLAYER_NOT_INITIALIZED -> "Player not initialized"
            SURFACE_NOT_READY -> "Surface is not ready"
            TIMEOUT -> "Operation timeout"
            RETRY_EXHAUSTED -> "Retry attempts exhausted"

            // 数据源错误
            SOURCE_EMPTY -> "Data source is empty"
            SOURCE_FORMAT_UNSUPPORTED -> "Data source format not supported"
            FILE_NOT_FOUND -> "File not found"
            FILE_READ_ERROR -> "Failed to read file"
            ASSET_FILE_NOT_FOUND -> "Asset file not found"
            ASSET_COPY_FAILED -> "Failed to copy asset file"
            INVALID_URI -> "Invalid URI format"
            CONTENT_PROVIDER_ERROR -> "Content provider access failed"

            // Surface/渲染错误
            SURFACE_CREATE_FAILED -> "Failed to create surface"
            SURFACE_SWITCH_FAILED -> "Failed to switch surface"
            VIDEO_DECODE_ERROR -> "Video decode error"
            AUDIO_DECODE_ERROR -> "Audio decode error"
            RENDERER_INIT_FAILED -> "Renderer initialization failed"
            MEDIA_CODEC_CONFIG_FAILED -> "MediaCodec configuration failed"

            // 网络错误
            NETWORK_CONNECTION_FAILED -> "Network connection failed"
            NETWORK_TIMEOUT -> "Network timeout"
            DNS_RESOLUTION_FAILED -> "DNS resolution failed"
            HTTP_ERROR -> "HTTP error occurred"
            SSL_ERROR -> "SSL/TLS error occurred"
            INSUFFICIENT_BANDWIDTH -> "Insufficient network bandwidth"
            SERVER_ERROR -> "Server returned an error"

            // 系统错误
            OUT_OF_MEMORY -> "Out of memory"
            INSUFFICIENT_STORAGE -> "Insufficient storage space"
            PERMISSION_DENIED -> "Permission denied"
            MEDIA_PLAYER_INTERNAL_ERROR -> "MediaPlayer internal error"
            EXO_PLAYER_INTERNAL_ERROR -> "ExoPlayer internal error"
            IJK_MEDIA_PLAYER_INTERNAL_ERROR -> "IjkMediaPlayer internal error"

            // 准备阶段错误
            PREPARE_TIMEOUT -> "Prepare timeout"
            PREPARE_INTERRUPTED -> "Prepare interrupted"
            PREPARE_FAILED -> "Prepare failed"

            else -> "Unknown error code: $errorCode"
        }
    }

    /**
     * 创建错误信息（包含错误代码和描述）
     *
     * @param errorCode 错误代码
     * @param additionalInfo 额外信息（可选）
     * @return 格式化后的错误字符串
     */
    fun formatError(errorCode: Int, additionalInfo: String? = null): String {
        val baseMessage = getMessage(errorCode)
        return if (additionalInfo != null) {
            "$baseMessage ($additionalInfo)"
        } else {
            baseMessage
        }
    }
}
