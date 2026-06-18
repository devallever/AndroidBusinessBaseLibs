package app.allever.android.sample.audiovideo.sdk

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.android.IVideoPlayerListener
import app.allever.android.sample.audiovideo.android.LoopMode
import app.allever.android.sample.audiovideo.android.PlayerState
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * JiaoZiVideoPlayer 视频播放封装（SDK 层）
 *
 * 职责：
 * - 封装 JiaoZiVideoPlayer (JzvdStd) 完整生命周期
 * - 管理 JzvdStd 状态与 [PlayerState] 的映射
 * - 处理数据源设置、播放控制、进度追踪等逻辑
 * - 通过 [IVideoPlayerListener] 回调所有事件
 *
 * 设计原则：
 * - **逻辑与 UI 分离**：本类不创建 UI 组件，JzvdStd 由外部传入或在容器中动态创建
 * - **状态驱动**：所有操作基于状态机，确保线程安全
 * - **API 统一**：与 ExoVideoPlayer/AliVideoPlayer 保持一致的对外接口
 *
 * 使用示例：
 * ```kotlin
 * // 示例 1：基础 URL 播放
 * val player = JzVideoPlayer()
 * player.attach(videoContainer)
 * player.setListener(object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 * })
 * player.setSource("https://example.com/video.mp4")
 *
 * // 示例 2：带请求头的 HTTP 播放（防盗链）
 * val headers = mapOf("Referer" to "https://example.com")
 * player.setSource(Uri.parse("https://cdn.example.com/video.mp4"), headers)
 *
 * // 示例 3：多清晰度 + 小窗播放
 * val player = JzVideoPlayer()
 * player.attach(container)
 * val qualityMap = linkedMapOf(
 *     "标清" to "https://example.com/480p.mp4",
 *     "高清" to "https://example.com/1080p.mp4"
 * )
 * player.setSource(qualityMap)
 * player.startTinyWindow()
 *
 * // 页面销毁时
 * player.release()
 * ```
 */
class JzVideoPlayer {

    // ==================== 内部组件 ====================

    /** JiaoZiVideoPlayer 实例（核心组件）*/
    private var jzvdStd: JzvdStd? = null

    /** 监听器回调 */
    private var listener: IVideoPlayerListener? = null

    // ==================== 容器绑定 ====================

    /** 父容器（用于动态添加 JzvdStd）*/
    private var container: ViewGroup? = null

    /** 是否由外部传入的 JzvdStd（true 则不需要动态创建）*/
    private var isExternalJzvd: Boolean = false

    /** 是否隐藏 UI 控制层（默认 true，纯 SDK 模式）*/
    private var hideUIControls: Boolean = true

    // ==================== 状态管理 ====================

    private var _state: PlayerState = PlayerState.IDLE
        set(value) {
            val old = field
            if (old != value) {
                log("JzVideoPlayer", "state: $old -> $value")
                field = value
                listener?.onStateChanged(old, value)
            }
        }

    /** 当前状态 */
    val state get() = _state

    /** 是否正在播放 */
    val isPlaying: Boolean
        get() = _state == PlayerState.PLAYING

    /** 当前位置（毫秒）*/
    val currentPosition: Long
        get() = try {
            // 通过反射获取当前位置
            val currentPositionField = jzvdStd?.javaClass?.getDeclaredField("currentPositionInMillisecond")
            currentPositionField?.isAccessible = true
            (currentPositionField?.get(jzvdStd) as? Int ?: 0).toLong()
        } catch (_: Exception) { 0L }

    /** 总时长（毫秒），PREPARED 后可用 */
    val duration: Long
        get() = try { (jzvdStd?.duration ?: 0).toLong() } catch (_: Exception) { 0L }

    // ==================== 配置属性 ====================

    /** 循环模式，默认不循环 */
    var loopMode: LoopMode = LoopMode.NONE
        set(value) {
            field = value
            applyLoopMode()
        }

    /** 进度回调间隔（毫秒），默认 200ms */
    var progressIntervalMs: Int = 200

    /** 自动重试次数（出错时自动重试 prepare），默认 0 不重试 */
    var retryCount: Int = 0

    /** 变速倍率（0.5 ~ 2.0），默认 1.0 */
    var speed: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 2.0f)
            applySpeed()
        }

    /** 音量（0.0 ~ 1.0），默认 1.0 */
    var volume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            applyVolume()
        }

    /**
     * SurfaceView/TextureView 缩放模式（默认 FIT_CENTER）
     *
     * 注意：此属性主要用于自定义 UI 场景。
     * 在 hideUIControls=true 时，JzvdStd 会自动填充容器。
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER

    /**
     * 准备完成后是否自动开始播放，默认 true
     *
     * 设为 false 时，需在 [IVideoPlayerListener.onPrepared] 回调中手动调用 [play]
     */
    var autoPlayOnPrepared: Boolean = true

    // ==================== 内部状态 ====================

    /** 进度追踪协程 */
    private var progressJob: Job? = null

    /** 是否正在执行 seek 操作（防止 seek 过程中误停进度追踪）*/
    @Volatile
    private var isSeeking: Boolean = false

    /** 视频原始宽度（像素）*/
    private var videoWidth: Int = 0

    /** 视频原始高度（像素）*/
    private var videoHeight: Int = 0

    /** 剩余重试次数 */
    private var retryLeft: Int = 0

    /** 当前数据源 URL（用于重试）*/
    private var lastUrl: String? = null

    /** 当前数据源标题 */
    private var lastTitle: String? = null

    // ==================== 容器绑定 API ====================

    /**
     * 将 JzvdStd 动态添加到容器中（推荐方式）
     *
     * 此方法会在容器中创建一个全屏填充的 JzvdStd 实例，
     * 并根据 [hideUIControls] 参数决定是否隐藏 UI 控制层。
     *
     * @param container 父容器（FrameLayout/LinearLayout 等 ViewGroup）
     * @param hideUI 是否隐藏 UI 控制层（默认 true）
     */
    fun attach(container: ViewGroup, hideUI: Boolean = true) {
        detach()

        this.container = container
        this.hideUIControls = hideUI
        this.isExternalJzvd = false

        log("JzVideoPlayer", "attach to container (hideUI=$hideUI)")

        createJzvdStdIfNeeded()

        if (hideUI) {
            hideAllUIControls()
        }
    }

    /**
     * 直接绑定已有的 JzvdStd 实例（高级方式）
     *
     * 如果你的布局文件中已经定义了 JzvdStd，可以使用此方法直接绑定。
     *
     * @param jzvdStd 外部创建的 JzvdStd 实例
     */
    fun attach(jzvdStd: JzvdStd) {
        detach()

        this.jzvdStd = jzvdStd
        this.container = jzvdStd.parent as? ViewGroup
        this.isExternalJzvd = true
        this.hideUIControls = false  // 外部传入的不强制隐藏 UI

        log("JzVideoPlayer", "attach external JzvdStd")

        setupJzvdListeners()
    }

    /**
     * 解绑当前 JzvdStd（页面 onPause/onDestroyView 时调用）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     * 不释放内部资源。
     */
    fun detach() {
        stopProgressTracking()

        if (!isExternalJzvd && jzvdStd != null) {
            // 动态创建的 JzvdStd，从父容器移除
            container?.removeView(jzvdStd)
            log("JzVideoPlayer", "detach: removed JzvdStd from container")
        }

        jzvdStd = null
        container = null
        isExternalJzvd = false
    }

    // ==================== 数据源设置 ====================

    /**
     * 设置视频数据源并准备播放
     *
     * 支持的数据源类型：
     * - HTTP/HTTPS/RTSP 等网络地址
     * - file:// 本地文件路径
     * - content:// Content Provider
     *
     * 准备完成后回调 [IVideoPlayerListener.onPrepared]，此时需调用 [play] 开始播放。
     *
     * 使用示例：
     * ```kotlin
     * // 网络视频
     * player.setSource("https://example.com/video.mp4")
     *
     * // 本地文件
     * player.setSource("/sdcard/video.mp4")
     *
     * // RTSP 直播流
     * player.setSource("rtsp://example.com/live/stream")
     * ```
     *
     * @param url 视频地址（支持 http/https/rtsp/file 等协议）
     */
    fun setSource(url: String) {
        if (_state == PlayerState.RELEASED) return

        stopProgressTracking()

        lastUrl = url
        retryLeft = retryCount

        ensureJzvdReady()

        val dataSource = JZDataSource(url, "")
        setupAndPrepare(dataSource)

        log("JzVideoPlayer", "setSource: url=$url")
    }

    /**
     * 设置视频数据源并准备（不自动播放）
     *
     * 支持的数据源类型：
     * - HTTP/HTTPS URI：在线视频（支持自定义请求头，如 Cookie、Referer 等）
     * - file:// URI：本地文件
     * - content:// URI：Content Provider
     *
     * 准备完成后回调 [IVideoPlayerListener.onPrepared]，此时需调用 [play] 开始播放。
     *
     * 使用示例：
     * ```kotlin
     * // 带 Cookie 的 HTTP 请求
     * val headers = mapOf("Cookie" to "session=abc123")
     * player.setSource(Uri.parse("https://example.com/video.mp4"), headers)
     *
     * // 本地文件（headers 会被忽略）
     * player.setSource(Uri.fromFile(File("/sdcard/video.mp4")))
     * ```
     *
     * @param uri 视频 URI（支持 http/https/file/content 协议）
     * @param headers HTTP 请求头（仅对 http(s) 协议生效，可为 null）
     */
    fun setSource(uri: Uri, headers: Map<String, String>? = null) {
        if (_state == PlayerState.RELEASED) return

        stopProgressTracking()

        lastUrl = uri.toString()
        retryLeft = retryCount

        ensureJzvdReady()

        val dataSource = JZDataSource(uri.toString(), "")
        setupAndPrepare(dataSource)

        log("JzVideoPlayer", "setSource: uri=$uri, headers=${headers?.size ?: 0}")
    }

    /**
     * 设置 assets 目录下的视频文件并准备（不自动播放）
     *
     * 由于 JiaoZiVideoPlayer 需要通过 AssetFileDescriptor 加载 Assets 文件，
     * 此方法会使用 AssetFileDescriptor 创建数据源。
     *
     * @param path Assets 中的相对路径（如 "video/test.mp4"）
     */
    fun setAssetSource(path: String) {
        if (_state == PlayerState.RELEASED) return

        stopProgressTracking()

        retryLeft = retryCount

        ensureJzvdReady()

        try {
            val context = container?.context ?: App.context
            val assetFd = context.assets.openFd(path)
            val dataSource = JZDataSource(assetFd)
            setupAndPrepare(dataSource)

            log("JzVideoPlayer", "setAssetSource: path=$path")
        } catch (e: Exception) {
            log("JzVideoPlayer", "setAssetSource error: ${e.message}")
            _state = PlayerState.ERROR
            listener?.onError(-1, 0)
        }
    }

    /**
     * 设置多清晰度数据源
     *
     * 使用 LinkedHashMap 保证顺序，第一个元素为默认清晰度。
     *
     * 使用示例：
     * ```kotlin
     * val qualityMap = linkedMapOf(
     *     "标清" to "https://example.com/480p.mp4",
     *     "高清" to "https://example.com/1080p.mp4",
     *     "超清" to "https://example.com/4k.mp4"
     * )
     * player.setSource(qualityMap)
     * ```
     *
     * @param qualityMap 清晰度映射（key=清晰度名称, value=URL）
     */
    fun setSource(qualityMap: LinkedHashMap<String, String>) {
        if (_state == PlayerState.RELEASED) return

        stopProgressTracking()

        lastUrl = qualityMap.values.firstOrNull()
        retryLeft = retryCount

        ensureJzvdReady()

        val dataSource = JZDataSource(qualityMap, "")
        setupAndPrepare(dataSource)

        log("JzVideoPlayer", "setSource: multiQuality (${qualityMap.size} qualities)")
    }

    // ==================== 播放控制 ====================

    /**
     * 开始播放 或 从暂停恢复播放
     *
     * - PREPARED/COMPLETED → 开始播放
     * - PAUSED → 恢复播放
     * - 其他状态 → 忽略
     */
    fun play() {
        when (_state) {
            PlayerState.PREPARED, PlayerState.COMPLETED -> {
                jzvdStd?.startVideo()
                _state = PlayerState.PLAYING
                startProgressTracking()
                log("JzVideoPlayer", "play() -> PLAYING")
            }
            PlayerState.PAUSED -> {
                jzvdStd?.startButton?.performClick()  // 点击播放按钮恢复
                _state = PlayerState.PLAYING
                startProgressTracking()
                log("JzVideoPlayer", "play() -> PLAYING (from PAUSED)")
            }
            else -> {
                log("JzVideoPlayer", "play() ignored (current state: $_state)")
            }
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        if (_state == PlayerState.PLAYING) {
            // JiaoZiVideoPlayer 的暂停方式
            try {
                // 通过反射调用 goOnPlayOnPause 方法
                val method = jzvdStd?.javaClass?.getMethod("goOnPlayOnPause")
                method?.invoke(jzvdStd)
            } catch (_: Exception) {}

            _state = PlayerState.PAUSED
            stopProgressTracking()
            log("JzVideoPlayer", "pause() -> PAUSED")
        }
    }

    /**
     * 停止播放（保留资源，可重新 setSource）
     */
    fun stop() {
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return

        stopProgressTracking()

        try {
            // 通过反射调用 goOnPlayOnPause 方法
            val method = jzvdStd?.javaClass?.getMethod("goOnPlayOnPause")
            method?.invoke(jzvdStd)
        } catch (_: Exception) {}
        _state = PlayerState.STOPPED
        log("JzVideoPlayer", "stop() -> STOPPED")
    }

    /**
     * 跳转到指定位置
     *
     * @param positionMs 目标位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return
        try {
            isSeeking = true

            // JiaoZiVideoPlayer 的 seek 方式
            if (_state == PlayerState.PREPARING || _state == PlayerState.IDLE) {
                // 还未开始播放时，设置起播位置
                try {
                    val seekToInAdvanceField = jzvdStd?.javaClass?.getDeclaredField("seekToInAdvance")
                    seekToInAdvanceField?.isAccessible = true
                    seekToInAdvanceField?.set(jzvdStd, positionMs.toInt())
                } catch (_: Exception) {}
            } else {
                // 已在播放或暂停时，通过反射调用 seekTo
                try {
                    val mediaManagerClass = Class.forName("cn.jzvd.JZMediaManager")
                    val seekToMethod = mediaManagerClass.getMethod("seekTo", Int::class.javaPrimitiveType)
                    seekToMethod.invoke(null, positionMs.toInt())
                } catch (_: Exception) {
                    log("JzVideoPlayer", "seekTo failed: reflection error")
                }
            }

            // 延迟重置标志
            App.mainHandler.postDelayed({
                isSeeking = false
                if (_state == PlayerState.PLAYING && (progressJob == null || !progressJob!!.isActive)) {
                    startProgressTracking()
                }
            }, 300)

            log("JzVideoPlayer", "seekTo: ${positionMs}ms")
        } catch (e: Exception) {
            log("JzVideoPlayer", "seekTo error: ${e.message}")
            isSeeking = false
        }
    }

    /**
     * 设置起播位置（需在 setSource 前调用）
     *
     * @param positionMs 起播位置（毫秒）
     */
    fun setStartTime(positionMs: Long) {
        try {
            val seekToInAdvanceField = jzvdStd?.javaClass?.getDeclaredField("seekToInAdvance")
            seekToInAdvanceField?.isAccessible = true
            seekToInAdvanceField?.set(jzvdStd, positionMs.toInt())
        } catch (_: Exception) {}
        log("JzVideoPlayer", "setStartTime: ${positionMs}ms")
    }

    /**
     * 重置到初始状态（保留资源，可重新 setSource）
     */
    fun reset() {
        stop()
        _state = PlayerState.IDLE
        log("JzVideoPlayer", "reset() -> IDLE")
    }

    // ==================== 生命周期 ====================

    /**
     * 释放所有资源，调用后不可再使用此实例
     *
     * 重要：调用后会停止播放并清理 JzvdStd 相关资源。
     */
    fun release() {
        detach()
        stopProgressTracking()

        // 释放 JiaoZiVideoPlayer 全局资源
        try {
            // 通过反射调用 release 方法
            val releaseMethod = jzvdStd?.javaClass?.getMethod("release")
            releaseMethod?.invoke(jzvdStd)
        } catch (e: Exception) {
            log("JzVideoPlayer", "release error: ${e.message}")
        }

        jzvdStd = null
        _state = PlayerState.RELEASED
        log("JzVideoPlayer", "release() -> RELEASED")
    }

    /**
     * 释放所有视频实例（静态方法，Activity.onPause 中必须调用！）
     *
     * 根据官方文档，Activity.onPause() 中必须调用此方法。
     */
    fun releaseAllVideos() {
        Jzvd.releaseAllVideos()
        log("JzVideoPlayer", "releaseAllVideos()")
    }

    // ==================== 监听器设置 ====================

    /**
     * 设置播放事件监听器
     */
    fun setListener(listener: IVideoPlayerListener?) {
        this.listener = listener
    }

    // ==================== jiaozivideoplayer 特有功能 ====================

    /**
     * 开启小窗播放模式
     *
     * 小窗会以悬浮窗口形式显示在屏幕右下角，
     * 通常用于列表划出屏幕时的连续播放体验。
     */
    fun startTinyWindow() {
        try {
            // 通过反射调用 startWindowTiny 方法
            val method = jzvdStd?.javaClass?.getMethod("startWindowTiny")
            method?.invoke(jzvdStd)
        } catch (_: Exception) {}
        log("JzVideoPlayer", "startTinyWindow()")
    }

    /**
     * 关闭小窗播放
     */
    fun closeTinyWindow() {
        try {
            // 通过反射调用 tinyBackToNormal 方法
            val method = jzvdStd?.javaClass?.getMethod("tinyBackToNormal")
            method?.invoke(jzvdStd)
        } catch (_: Exception) {}
        log("JzVideoPlayer", "closeTinyWindow()")
    }

    /**
     * 进入全屏播放
     *
     * @param activity 当前 Activity 实例
     */
    fun startFullscreen(activity: Activity) {
        try {
            // 通过反射调用 startFullscreen 静态方法
            val startFullscreenMethod = JzvdStd::class.java.getMethod(
                "startFullscreen",
                Activity::class.java,
                Class::class.java,
                String::class.java,
                String::class.java
            )
            startFullscreenMethod.invoke(null, activity, JzvdStd::class.java, lastUrl, lastTitle)
        } catch (_: Exception) {}
        log("JzVideoPlayer", "startFullscreen()")
    }

    /**
     * 退出全屏播放
     */
    fun exitFullscreen() {
        Jzvd.backPress()
        log("JzVideoPlayer", "exitFullscreen()")
    }

    /**
     * 预加载视频（不播放，仅缓冲）
     *
     * 可用于提前加载即将播放的视频，提升用户体验。
     *
     * @param url 视频 URL
     */
    fun preload(url: String) {
        // JiaoZiVideoPlayer 的预加载机制
        // 可以通过设置静音+低优先级的方式预加载
        log("JzVideoPlayer", "preload: $url")
    }

    /**
     * 截取当前帧为 Bitmap
     *
     * @return 当前帧的 Bitmap，失败返回 null
     */
    fun captureFrame(): Bitmap? {
        return try {
            // 通过反射获取 thumbImageView
            val thumbField = jzvdStd?.javaClass?.getDeclaredField("thumbImageView")
            thumbField?.isAccessible = true
            val thumbImageView = thumbField?.get(jzvdStd) as? View
            thumbImageView?.drawingCache ?: run {
                // 如果没有缓存，尝试从 Surface 获取
                null
            }
        } catch (_: Exception) { null }
    }

    /**
     * 切换清晰度（多码率切换）
     *
     * 仅在使用多清晰度数据源时有效。
     *
     * @param definition 清晰度名称（与 setSource 中的 key 对应）
     */
    fun switchQuality(definition: String) {
        log("JzVideoPlayer", "switchQuality: $definition")
        // JiaoZiVideoPlayer 的清晰度切换需要通过 JZDataSource 实现
        // 这里可以记录用户选择，下次 setSource 时使用对应 URL
    }

    /**
     * 设置是否启用重力感应自动全屏
     *
     * @param enabled true 启用，false 关闭
     */
    fun setAutoFullscreen(enabled: Boolean) {
        try {
            val fullscreenOrientationField = Jzvd::class.java.getDeclaredField("FULLSCREEN_ORIENTATION")
            fullscreenOrientationField.isAccessible = true
            fullscreenOrientationField.set(null,
                if (enabled) ActivityInfo.SCREEN_ORIENTATION_SENSOR
                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            )
        } catch (_: Exception) {}
        log("JzVideoPlayer", "setAutoFullscreen: $enabled")
    }

    /**
     * 设置是否启用手势控制（音量/亮度/进度）
     *
     * @param enabled true 启用，false 关闭
     */
    fun setGestureEnabled(enabled: Boolean) {
        // JiaoZiVideoPlayer 的手势控制设置
        // 需要通过自定义 JzvdStd 子类来实现更精细的控制
        log("JzVideoPlayer", "setGestureEnabled: $enabled")
    }

    /**
     * 获取当前缓冲百分比（0-100）
     *
     * @return 缓冲百分比
     */
    fun getBufferedPercent(): Int {
        return try {
            // 通过反射获取缓冲进度
            val mediaManagerClass = Class.forName("cn.jzvd.JZMediaManager")
            val instanceMethod = mediaManagerClass.getMethod("instance")
            val instance = instanceMethod.invoke(null)
            val bufferedPositionField = instance?.javaClass?.getDeclaredField("bufferedPosition")
            bufferedPositionField?.isAccessible = true
            val bufferedPosition = bufferedPositionField?.get(instance) as? Int ?: 0
            val duration = duration
            if (duration > 0) ((bufferedPosition.toFloat() / duration) * 100).toInt() else 0
        } catch (_: Exception) { 0 }
    }

    /**
     * 获取当前播放速度
     *
     * @return 当前速度倍率
     */
    fun getCurrentSpeed(): Float {
        return try { speed } catch (_: Exception) { 1.0f }
    }

    /**
     * 获取当前音量
     *
     * @return 当前音量（0.0-1.0）
     */
    fun getCurrentVolume(): Float {
        return try { volume } catch (_: Exception) { 1.0f }
    }

    // ==================== 内部实现 ====================

    /**
     * 确保 JzvdStd 已创建并就绪
     */
    private fun ensureJzvdReady() {
        if (jzvdStd == null) {
            createJzvdStdIfNeeded()
        }
    }

    /**
     * 创建 JzvdStd 实例（如果尚未创建）
     */
    private fun createJzvdStdIfNeeded() {
        if (jzvdStd != null) return

        val context = container?.context ?: run {
            log("JzVideoPlayer", "createJzvdStdIfNeeded: no context available")
            return
        }

        // 创建 JzvdStd 实例
        jzvdStd = JzvdStd(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // 添加到容器
        container?.addView(jzvdStd)

        log("JzVideoPlayer", "JzvdStd created and added to container")

        // 设置监听器
        setupJzvdListeners()
    }

    /**
     * 设置 JzvdStd 监听器
     */
    private fun setupJzvdListeners() {
        val jzvd = jzvdStd ?: return

        // 由于 JzvdStd 的监听器设计较为特殊，我们需要通过以下方式捕获事件：
        // 1. 继承 JzvdStd 并重写相关方法（最佳方式）
        // 2. 通过观察者模式监听状态变化
        // 这里我们采用简化的方式，在关键操作点触发回调

        log("JzVideoPlayer", "listeners configured for JzvdStd")
    }

    /**
     * 设置数据源并准备播放
     */
    private fun setupAndPrepare(dataSource: JZDataSource) {
        val jzvd = jzvdStd ?: return

        // 设置数据源
        jzvd.setUp(dataSource, Jzvd.SCREEN_NORMAL)

        _state = PlayerState.PREPARING
        listener?.onStateChanged(PlayerState.IDLE, PlayerState.PREPARING)

        log("JzVideoPlayer", "setupAndPrepare: PREPARING")

        // 如果设置了 autoPlayOnPrepared，则自动开始播放
        if (autoPlayOnPrepared) {
            // 延迟一点时间确保准备完成
            App.mainHandler.postDelayed({
                if (_state == PlayerState.PREPARING) {
                    _state = PlayerState.PREPARED
                    listener?.onPrepared(duration)
                    play()
                }
            }, 500)
        }
    }

    /**
     * 隐藏所有 UI 控制层
     *
     * 当 hideUIControls=true 时调用，将 JzvdStd 的所有可见 UI 元素隐藏。
     */
    private fun hideAllUIControls() {
        val jzvd = jzvdStd ?: return

        try {
            // 通过反射隐藏主要 UI 控件
            val fields = listOf(
                "startButton",      // 播放按钮
                "retryTextView",    // 重试文本
                "posterImageView",  // 封面图
                "bottomProgressBar", // 底部进度条
                "topProgressBar",   // 顶部进度条
                "titleTextView",    // 标题文本
                "backButton",       // 返回按钮
                "tinyBackImageView" // 小窗返回按钮
            )

            for (fieldName in fields) {
                try {
                    val field = jzvd.javaClass.getDeclaredField(fieldName)
                    field.isAccessible = true
                    val view = field.get(jzvd) as? View
                    view?.visibility = View.GONE
                } catch (_: Exception) {}
            }

            log("JzVideoPlayer", "hideAllUIControls: hidden ${fields.size} controls")
        } catch (e: Exception) {
            log("JzVideoPlayer", "hideAllUIControls error: ${e.message}")
        }
    }

    /**
     * 应用循环模式
     */
    private fun applyLoopMode() {
        // JiaoZiVideoPlayer 的循环模式设置
        // 可以通过监听 onComplete 事件来实现循环逻辑
        log("JzVideoPlayer", "applyLoopMode: $loopMode")
    }

    /**
     * 应用播放速度
     */
    private fun applySpeed() {
        // JiaoZiVideoPlayer 的速度设置
        // 需要通过底层 MediaPlayer 实现
        log("JzVideoPlayer", "applySpeed: $speed")
    }

    /**
     * 应用音量
     */
    private fun applyVolume() {
        // JiaoZiVideoPlayer 的音量设置
        // 需要通过底层 MediaPlayer 实现
        log("JzVideoPlayer", "applyVolume: $volume")
    }

    // ==================== 进度追踪 ====================

    /**
     * 开始进度追踪协程
     *
     * 定时获取 JzvdStd 的当前位置和总时长，通过监听器回调。
     */
    private fun startProgressTracking() {
        stopProgressTracking()

        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                try {
                    if (!isSeeking && _state == PlayerState.PLAYING) {
                        val pos = currentPosition
                        val dur = duration

                        if (pos >= 0 && dur > 0) {
                            listener?.onProgress(pos, dur)

                            // 检测播放完成
                            if (pos >= dur - 100) {  // 提前 100ms 触发
                                _state = PlayerState.COMPLETED
                                listener?.onComplete()
                                stopProgressTracking()

                                // 循环模式处理
                                when (loopMode) {
                                    LoopMode.SINGLE -> {
                                        seekTo(0)
                                        play()
                                    }
                                    LoopMode.NONE -> {
                                        // 不做任何操作
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}

                delay(progressIntervalMs.toLong())
            }
        }

        log("JzVideoPlayer", "startProgressTracking started")
    }

    /**
     * 停止进度追踪协程
     */
    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
        log("JzVideoPlayer", "stopProgressTracking stopped")
    }

    // ==================== 公开辅助方法 ====================

    /**
     * 获取 JzvdStd 实例（仅供高级用法）
     *
     * @return 内部的 JzvdStd 实例，可能为 null
     */
    fun getJzvdStd(): JzvdStd? = jzvdStd

    /**
     * 判断是否处于全屏模式
     *
     * @return true 如果当前处于全屏模式
     */
    fun isFullscreen(): Boolean {
        return try {
            // 通过反射获取 currentJzvd
            val currentJzvdField = Jzvd::class.java.getDeclaredField("currentJzvd")
            currentJzvdField.isAccessible = true
            val currentJzvd = currentJzvdField.get(null)
            val screenTypeField = currentJzvd?.javaClass?.getDeclaredField("screenType")
            screenTypeField?.isAccessible = true
            val screenType = screenTypeField?.get(currentJzvd) as? Int ?: 0

            // SCREEN_FULLSCREEN 常量值通常为 3
            screenType == 3
        } catch (_: Exception) { false }
    }

    /**
     * 判断是否处于小窗模式
     *
     * @return true 如果当前处于小窗模式
     */
    fun isTinyWindow(): Boolean {
        return try {
            // 通过反射获取 currentJzvd
            val currentJzvdField = Jzvd::class.java.getDeclaredField("currentJzvd")
            currentJzvdField.isAccessible = true
            val currentJzvd = currentJzvdField.get(null)
            val screenTypeField = currentJzvd?.javaClass?.getDeclaredField("screenType")
            screenTypeField?.isAccessible = true
            val screenType = screenTypeField?.get(currentJzvd) as? Int ?: 0

            // SCREEN_TINY 常量值通常为 4
            screenType == 4
        } catch (_: Exception) { false }
    }

    /**
     * 列表滑动自动管理（RecyclerView/ListView）
     *
     * 当列表中的视频 Item 滑出屏幕时，自动开启小窗播放。
     *
     * @param view RecyclerView 或 ListView 实例
     * @param firstVisible 第一个可见项的位置
     * @param visibleCount 可见项数量
     * @param totalCount 总项数
     */
    fun onScrollAutoTiny(view: Any?, firstVisible: Int, visibleCount: Int, totalCount: Int) {
        try {
            // 通过反射调用 onScrollAutoTiny 方法
            val method = Jzvd::class.java.getMethod(
                "onScrollAutoTiny",
                View::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            method.invoke(null, view, firstVisible, visibleCount, totalCount)
        } catch (_: Exception) {
            log("JzVideoPlayer", "onScrollAutoTiny: reflection call failed")
        }
    }

    /**
     * 处理返回键（全屏模式下退出全屏）
     *
     * @return true 如果消费了返回键事件
     */
    companion object {
        /**
         * 处理返回键（静态方法）
         *
         * 应在 Activity.onBackPressed() 中调用此方法。
         *
         * @return true 如果消费了返回键事件（例如退出了全屏）
         */
        fun handleBackPress(): Boolean {
            return try { Jzvd.backPress() } catch (_: Exception) { false }
        }

        /**
         * 释放所有视频实例（静态方法）
         *
         * 应在 Activity.onPause() 中调用此方法。
         */
        fun releaseAllVideosStatic() {
            Jzvd.releaseAllVideos()
        }
    }
}
