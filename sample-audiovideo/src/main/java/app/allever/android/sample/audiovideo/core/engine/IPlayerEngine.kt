package app.allever.android.sample.audiovideo.core.engine

import android.net.Uri
import android.view.Surface
import app.allever.android.sample.audiovideo.core.render.IVideoRender
import app.allever.android.sample.audiovideo.lib.LoopMode

/**
 * 播放引擎接口（纯播放逻辑，不涉及渲染）
 *
 * ## 职责
 * - 管理播放引擎的完整生命周期（初始化 → 准备 → 播放 → 暂停 → 停止 → 释放）
 * - 处理数据源设置（URI、Assets）
 * - 提供播放控制（play、pause、stop、seek、speed、volume）
 * - 查询播放状态和进度
 *
 * ## 设计原则
 * **单一职责**：本接口只关注播放逻辑，不关心视频如何渲染到屏幕上。
 * Surface 的设置通过 [setSurface] 方法提供，但引擎不管理 Surface 的生命周期。
 *
 * ## 实现类
 * - [MediaPlayerEngine]：基于 Android MediaPlayer 的实现
 * - ExoPlayerEngine：基于 Google ExoPlayer/Media3 的实现（未来）
 * - IjkPlayerEngine：基于 Bilibili IJKPlayer 的实现（未来）
 *
 * ## 使用方式
 * 引擎通常不直接使用，而是通过 [app.allever.android.sample.audiovideo.core.player.VideoPlayer] 协调器组合使用：
 * ```kotlin
 * val player = VideoPlayer(
 *     engine = MediaPlayerEngine(),
 *     render = SurfaceViewRender()
 * )
 * ```
 */
interface IPlayerEngine {

    val TAG: String
        get() = this::class.java.simpleName

    // ==================== 生命周期管理 ====================

    /**
     * 初始化引擎
     *
     * 在此方法中创建底层播放器实例、配置参数等。
     * 必须在调用其他方法之前调用。
     */
    fun init()

    /**
     * 异步准备数据源
     *
     * 调用前必须先调用 [setSource] 或 [setAssetSource] 设置数据源。
     * 准备完成后会通过 [IPlayerEngineListener.onPrepared] 回调通知。
     */
    fun prepareAsync()

    /**
     * 开始播放 或 从暂停恢复播放
     */
    fun start()

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 停止播放（保留资源，可重新 prepare）
     */
    fun stop()

    /**
     * 重置引擎状态（回到 IDLE，可重新 setSource）
     */
    fun reset()

    /**
     * 释放所有资源，调用后不可再使用此实例
     */
    fun release()

    // ==================== 数据源设置 ====================

    /**
     * 设置视频数据源
     *
     * @param uri 视频 URI（支持 http/https/file/content 协议）
     * @param headers HTTP 请求头（仅对 http(s) 协议生效，可为 null）
     */
    fun setSource(uri: Uri, headers: Map<String, String>?)

    /**
     * 设置 assets 目录下的视频文件
     *
     * @param path Assets 中的相对路径（如 "video/test.mp4"）
     */
    fun setAssetSource(path: String)

    // ==================== 播放控制 ====================

    /**
     * 跳转到指定位置
     *
     * @param positionMs 目标位置（毫秒）
     */
    fun seekTo(positionMs: Long)

    /**
     * 设置播放速度
     *
     * @param speed 变速倍率（0.5x ~ 3.0x），默认 1.0x
     */
    fun setSpeed(speed: Float)

    /**
     * 设置音量
     *
     * @param volume 音量（0.0 ~ 1.0），默认 1.0
     */
    fun setVolume(volume: Float)

    /**
     * 设置循环模式
     *
     * @param mode 循环模式
     */
    fun setLoopMode(mode: LoopMode)

    /**
     * 设置渲染 Surface
     *
     * 由外部（Render 层）调用，引擎只负责将 Surface 绑定到底层播放器。
     * 引擎不管理 Surface 的生命周期。
     *
     * @param surface Surface 实例，传 null 表示解绑
     */
    fun setSurface(surface: Surface?, render: IVideoRender)

    // ==================== 状态查询 ====================

    /**
     * 获取当前播放位置（毫秒）
     */
    fun getCurrentPosition(): Long

    /**
     * 获取视频总时长（毫秒）
     *
     * ⚠️ 只有 PREPARED 及之后的状态才能获取到有效值
     */
    fun getDuration(): Long

    /**
     * 是否正在播放
     */
    fun isPlaying(): Boolean

    /**
     * 获取视频宽度（像素）
     */
    fun getVideoWidth(): Int

    /**
     * 获取视频高度（像素）
     */
    fun getVideoHeight(): Int

    /**
     * 获取网络下载速度（字节/秒）
     *
     * 仅对支持此功能的引擎有效（如 IJKPlayer）
     */
    fun getTcpSpeed(): Long

    fun getCurrentUri(): Uri?

    // ==================== 监听器 ====================

    /**
     * 设置引擎事件监听器
     */
    fun setListener(listener: IPlayerEngineListener?)

    /**
     * 移除引擎事件监听器
     */
    fun removeListener(listener: IPlayerEngineListener)
}

/**
 * 播放引擎事件监听器
 *
 * 用于接收引擎的异步回调事件。
 * 所有回调都在主线程触发。
 */
interface IPlayerEngineListener {

    /**
     * 准备完成回调
     *
     * 触发时机：[IPlayerEngine.prepareAsync] 异步准备完成后
     */
    fun onPrepared()

    /**
     * 播放完成回调
     *
     * 触发时机：视频播放到末尾（单曲循环时不触发）
     */
    fun onCompletion()

    /**
     * 错误回调
     *
     * @param code 错误代码（参考 [app.allever.android.sample.audiovideo.lib.PlayerErrorCode]）
     * @param msg 错误信息描述
     */
    fun onError(code: Int, msg: String)

    /**
     * 缓冲进度更新回调
     *
     * @param percent 缓冲百分比（0 ~ 100）
     */
    fun onBufferingUpdate(percent: Int)

    /**
     * 视频尺寸变化回调
     *
     * @param width 视频宽度（像素）
     * @param height 视频高度（像素）
     */
    fun onVideoSizeChanged(width: Int, height: Int)

    /**
     * 信息回调（通用）
     *
     * 触发时机：缓冲开始/结束、首帧渲染等
     */
    fun onInfo()

    /**
     * 播放状态变化回调
     *
     * @param isPlaying 是否正在播放
     */
    fun onIsPlayingChanged(isPlaying: Boolean)
}
