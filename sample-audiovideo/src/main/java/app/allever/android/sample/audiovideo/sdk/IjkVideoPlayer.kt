package app.allever.android.sample.audiovideo.sdk

import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.android.BaseVideoPlayer
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.android.base.IjkPlayerKernal

/**
 * IJKPlayer 视频播放器实现（继承自 [BaseVideoPlayer]）
 *
 * ## 职责
 * - 封装 IJKPlayer (基于 FFmpeg) 的完整生命周期管理
 * - 处理 SurfaceView/TextureView 的绑定与解绑
 * - 实现 [BaseVideoPlayer] 的抽象方法以适配 IJKPlayer 的特殊行为
 * - 提供 IJKPlayer 特有的高级功能（调试信息、TCP 速度等）
 *
 * ## IJKPlayer 是什么？
 * **IJKPlayer** 是 Bilibili 开源的视频播放器框架，
 * 基于 FFmpeg 开发，支持 Android/iOS 平台。
 *
 * **项目地址：** https://github.com/Bilibili/ijkplayer
 *
 * **核心特点：**
 * - 基于 FFmpeg：支持几乎所有视频格式和编解码器
 * - 跨平台：Android/iOS 共用一套代码
 * - 可定制：可编译时选择需要的模块（减小体积）
 * - 活跃维护：Bilibili 持续更新，社区活跃
 *
 * ## IJKPlayer vs MediaPlayer vs ExoPlayer
 *
 * | 特性 | MediaPlayer | ExoPlayer (Media3) | **IJKPlayer** |
 * |------|-----------|-------------------|---------------|
 * | **开发者** | Google | Google | **Bilibili** |
 * | **底层引擎** | Android 原生 | 自研 (Exo) | **FFmpeg** |
 * | **格式支持** | 有限 | 较好 | **极好 (FFmpeg 全格式)** |
 * | **协议支持** | HTTP/本地文件 | DASH/HLS/SS | **RTMP/RTSP/HLS/DASH 等** |
 * | **体积** | 0KB | ~5-10MB | **~5-20MB (取决于编译选项)** |
 * | **性能** | 一般 | 优秀 | **优秀 (FFmpeg 优化)** |
 * | **定制性** | 差 | 好 | **极好 (源码级定制)** |
 * | **学习成本** | 低 | 中 | **高 (需了解 FFmpeg)** |
 * | **维护者** | Google | Google | **社区 + Bilibili** |
 *
 * ## IJKPlayer 的优势
 * ✅ **格式支持最广**：基于 FFmpeg，支持几乎所有视频格式
 *    - AVI、MKV、MOV、FLV、WMV、RMVB 等老旧格式
 *    - H.264、H.265/HEVC、VP8/VP9、AV1 等现代编码
 *    - AAC、MP3、FLAC、OPUS 等音频格式
 *
 * ✅ **协议支持丰富**：
 *    - RTMP / RTSP（直播流，MediaPlayer 不支持）
 *    - HLS (m3u8)
 *    - DASH (mpd)
 *    - 自定义协议（可通过 FFmpeg 扩展）
 *
 * ✅ **跨平台一致性**：Android/iOS 行为一致
 *    - 适合需要多端统一的项目
 *    - 减少平台差异带来的 Bug
 *
 * ✅ **高度可定制**：
 *    - 编译时可选择模块（如去掉不需要的解码器以减小体积）
 *    - 可修改 FFmpeg 源码实现特殊需求
 *    - 可集成第三方库（如 librtmp）
 *
 * ✅ **调试功能强大**：
 *    - 内置详细的日志输出（showlog）
 *    - 支持 VLC 风格的调试命令（:command）
 *    - 可获取 TCP 下载速度、缓冲区状态等详细信息
 *
 * ## IJKPlayer 的劣势
 * ❌ **编译复杂**：
 *    - 需要 NDK 环境
 *    - 编译时间长（首次编译可能需要 1-2 小时）
 *    - 需要配置多个脚本和参数
 *
 * ❌ **体积较大**：
 *    - 完整编译约 15-20MB
 *    - 即使精简也有 5-10MB
 *    - 比 MediaPlayer 大很多
 *
 * ❌ **API 不够友好**：
 *    - 接口设计较底层（类似 FFmpeg 的 C API）
 *    - 文档相对较少（主要靠源码和示例）
 *    - 错误信息不够清晰
 *
 * ❌ **维护风险**：
 *    - 主要依赖 Bilibili 维护（更新频率不如 Google）
 *    - 社区贡献不稳定
 *    - 新版 Android 兼容性需自行测试
 *
 * ❌ **学习成本高**：
 *    - 需要了解 FFmpeg 基础概念
 *    - 调试问题需要查看 FFmpeg 日志
 *    - 性能优化需要理解音视频原理
 *
 * ## 适用场景
 * **强烈推荐使用 IJKPlayer 的场景：**
 * - 🎥 **播放老旧/特殊格式的视频**（AVI、RMVB、FLV 等 MediaPlayer 不支持的）
 * - 📡 **直播应用**（RTMP/RTSP 协议，ExoPlayer 和 MediaPlayer 都不支持）
 * - 🎯 **需要极致格式兼容性**（用户可能上传各种格式的视频）
 * - 🔄 **跨平台项目**（Android + iOS 需要保持一致行为）
 * - 🔧 **需要深度定制**（如自定义解码器、特殊滤镜等）
 * - 📊 **需要详细的调试信息**（分析卡顿原因、网络状况等）
 *
 * **不推荐使用 IJKPlayer 的场景：**
 * - 只播放 MP4/H.264 等常见格式（MediaPlayer 或 ExoPlayer 更简单）
 * - 对 APK 体积有严格限制的应用
 * - 快速原型开发 / MVP 验证
 * - 团队没有音视频经验（学习成本太高）
 * - 只需要基本的在线视频播放功能（ExoPlayer 更合适）
 *
 * ## 架构说明
 * 本类采用**模板方法模式**，继承 [BaseVideoPlayer] 基类：
 * - **基类负责**：状态管理、数据源设置、播放控制、进度追踪、错误处理等通用逻辑
 * - **本类负责**：
 *   - IJKPlayer 引擎的创建和管理
 *   - Surface 绑定/解绑的特殊处理
 *   - IJKPlayer 特有的查询接口（如 TCP 速度）
 *
 * **引擎实现**：使用 [IjkPlayerKernal] 封装 IJKPlayer API
 * - 提供统一的 IPlayerKernal 接口
 * - 内置线程安全、异常处理等机制
 * - 处理 IJKPlayer 特有的回调和行为
 *
 * ## 使用示例
 * ```kotlin
 * // 基本用法（与 BaseVideoPlayer 完全一致）
 * val player = IjkVideoPlayer()
 * player.attach(surfaceView)  // 绑定 SurfaceView
 * player.setSource("rtmp://live.example.com/stream")  // RTMP 直播流
 * player.listener = object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 *     override fun onComplete() { log("直播结束或点播完成") }
 *     override fun onError(code: Int, msg: String) {
 *         log("错误: $msg")
 *         // IJKPlayer 的错误信息通常包含 FFmpeg 错误码
 *     }
 *     override fun onBufferingUpdate(percent: Int) {
 *         // 显示缓冲进度
 *         progressBar.progress = percent
 *     }
 * }
 *
 * // IJKPlayer 特有功能：获取 TCP 下载速度
 * val speedBytesPerSec = player.tcpSpeed
 * val speedKBps = speedBytesPerSec / 1024.0
 * log("当前下载速度: ${String.format("%.2f", speedKBps)} KB/s")
 *
 * // 高级用法：配置 IJKPlayer 参数
 * // （需要在 attach 之前配置，具体见 IjkPlayerKernal）
 *
 * // 切换 Surface（安全方式）
 * player.safeSwitchToSurfaceView(newSurfaceView)
 *
 * // 页面生命周期管理
 * override fun onPause() {
 *     if (player.isPlaying) player.pause()
 *     player.detach()  // 解绑 Surface
 * }
 *
 * override fun onResume() {
 *     player.attach(surfaceView)  // 重新绑定
 * }
 *
 * override fun onDestroy() {
 *     player.release()  // 释放 IJKPlayer（必须调用！）
 * }
 * ```
 *
 * ## IJKPlayer 配置建议
 *
 * ### 1. 编译时选择（减少体积）
 * ```bash
 * # 只编译必需的模块
 * ./compile-ijk.sh "armv7a"        # 只支持 ARMv7
 * # 或者进一步精简
 * ./compile-ijk.sh "armv7a" all   # 包含所有编解码器
 * ```
 *
 * ### 2. 运行时配置（性能优化）
 * ```kotlin
 * // 在 IjkPlayerKernal 或本类的 initPlayer() 中配置
 * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-all-videos", 1)
 * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1)
 * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 1)
 * ```
 *
 * ### 3. 调试模式
 * ```kotlin
 * // 开启详细日志（开发阶段）
 * IjkMediaPlayer.setLogLevel(IjkMediaPlayer.LOG_DEBUG)
 *
 * // 生产环境关闭
 * IjkMediaPlayer.setLogLevel(IjkMediaPlayer.LOG_INFO)
 * ```
 *
 * ## 注意事项
 * ⚠️ **必须调用 release()！**
 * IJKPlayer 底层持有 native 资源（FFmpeg 解码器、线程池等），
 * 如果不调用 release()，会导致：
 * - 内存泄漏（native 内存无法回收）
 * - 句柄泄漏（文件句柄、网络 socket 未关闭）
 * - 线程泄漏（FFmpeg 工作线程未停止）
 * - 最终导致应用崩溃或系统变慢
 *
 * ⚠️ **RTMP 直播流的特殊性：**
 * - 没有 duration（总时长为 0 或未知）
 * - 无法 seek（除非服务器支持录像回放）
 * - onCompletion 可能不会触发（直播流不会"结束"）
 * - 需要处理网络中断重连逻辑
 *
 * ⚠️ **IJKPlayer 的已知问题：**
 * - 某些设备上硬解可能有绿屏/花屏（需切换软解）
 * - 部分 HEVC 视频可能不支持（取决于编译选项）
 * - 音视频同步在某些情况下可能有问题（需调整 sync 参数）
 *
 * @see BaseVideoPlayer 基类，包含完整的播放流程实现
 * @see IjkPlayerKernal IJKPlayer 引擎封装
 * @see IPlayerKernal 统一的引擎接口定义
 */
class IjkVideoPlayer: BaseVideoPlayer() {

    /**
     * IJKPlayer 引擎实例（使用 IjkPlayerKernal 封装）
     *
     * 通过 [IjkPlayerKernal] 封装 IJKPlayer 的复杂性，
     * 提供统一的 [IPlayerKernal] 接口。
     *
     * 为什么使用 IjkPlayerKernal？
     * 1. **接口统一**：与其他引擎（MediaPlayer、ExoPlayer）保持一致
     * 2. **异常安全**：内置 try-catch 和错误恢复机制
     * 3. **线程安全**：确保回调在主线程执行
     * 4. **IJKPlayer 特性封装**：处理 IJKPlayer 特有的行为和配置
     *
     * 初始化时注册 [engineListener] 以接收所有引擎事件。
     */
//    private var ijkMediaPlayer: IjkMediaPlayer? = null
    override var engine: IPlayerKernal<*> = IjkPlayerKernal().apply {
        registerListener(engineListener)
    }

    /**
     * 解绑当前 Surface 并清理资源
     *
     * **与基类 detach() 的区别：**
     * 本类额外调用了 `engine.setSurface(null)`，
     * 确保 IJKPlayer 的 Surface 引用被清除。
     *
     * **为什么需要这个额外步骤？**
     * IJKPlayer 基于 FFmpeg，其 Surface 管理与原生 MediaPlayer 不同：
     * - MediaPlayer：detach 时自动释放 Surface
     * - IJKPlayer：可能持有 Surface 的强引用，需手动清空
     *
     * **调用时机：**
     * - 页面 onPause/onDestroyView 时
     * - 切换 Surface 前（先 detach 旧的）
     * - Activity.onDestroy 时（配合 release 使用）
     *
     * **注意：此方法不会释放 IJKPlayer 本身！**
     * 仅解绑 Surface，IJKPlayer 实例仍可复用。
     * 彻底销毁请调用 release()。
     */
    override fun detach() {
       super.detach()
        try {
            // 清除 Surface 引用（防止内存泄漏和渲染异常）
            engine.setSurface(null)
        } catch (e: Exception) {
           log(TAG, "detach error: ${e.message}")
        }
    }

    // ==================== 公共查询接口 ====================

    /**
     * 获取 TCP 下载速度（字节/秒）
     *
     * **这是 IJKPlayer 特有的功能！**
     * MediaPlayer 和 ExoPlayer 都不提供此接口。
     *
     * **用途：**
     * - 显示实时下载速度 UI（如 "2.5 MB/s"）
     * - 监控网络状况（速度过慢时提示用户切换网络）
     * - 统计和分析（记录用户的平均下载速度）
     * - ABR 参考（自适应码率可根据下载速度调整）
     *
     * **返回值含义：**
     * - > 0：正常下载中，值为每秒下载的字节数
     * - 0：未在下载（暂停、缓冲完成、或出错）
     * - 异常情况也会返回 0（已做异常捕获）
     *
     * **典型用法：**
     * ```kotlin
     * // 定期更新下载速度显示
     * CoroutineScope(Dispatchers.Main).launch {
     *     while (isActive) {
     *         val speed = player.tcpSpeed
     *         if (speed > 0) {
     *             val kbps = speed / 1024.0
     *             tvDownloadSpeed.text = "${String.format("%.1f", kbps)} KB/s"
     *         } else {
     *             tvDownloadSpeed.text = "-- KB/s"
     *         }
     *         delay(1000)  // 每秒更新一次
     *     }
     * }
     * ```
     *
     * **单位换算：**
     * - B/s (bytes per second)：原始值
     * - KB/s：÷ 1024
     * - MB/s：÷ (1024 * 1024)
     * - Mbps (megabits)：× 8 ÷ (1024 * 1024)
     *
     * **注意：**
     * - 此值是瞬时速度，波动较大，建议取平均值或使用滑动窗口平滑
     * - 本地文件播放时返回 0（不涉及网络下载）
     * - 缓冲充足时可能短暂为 0（IJKPlayer 暂停下载）
     *
     * @return TCP 下载速度（字节/秒），不可用时返回 0
     */
    val tcpSpeed: Long
        get() = try { engine.getTcpSpeed() } catch (_: Exception) { 0L }

}
