package app.allever.android.sample.audiovideo.android

import android.graphics.SurfaceTexture
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.TimeHelper.formatTime
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PendingPrepare
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.SurfaceType
import app.allever.android.sample.audiovideo.lib.VideoHelper
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * 视频播放器基类（模板方法模式）
 *
 * ## 职责
 * - 封装播放引擎的完整生命周期管理（创建 → 准备 → 播放 → 暂停 → 停止 → 释放）
 * - 统一状态机转换逻辑，确保线程安全和状态一致性
 * - 管理 Surface/TextureView 的绑定与解绑生命周期
 * - 提供进度追踪、变速、音量、循环、Seek 等通用能力
 * - 处理 Surface 异步就绪的 PendingPrepare 机制
 * - 实现安全的 Surface 切换方案（避免 MediaCodec 状态机竞态条件）
 *
 * ## 设计模式
 * **模板方法模式**：
 * - 本类定义了完整的播放流程骨架（算法骨架）
 * - 子类只需覆盖特定步骤（如 attach/detach/adjustSurfaceLayout）来适配不同的渲染视图
 * - 通用逻辑在基类中复用，子类专注于差异化实现
 *
 * ## 支持的渲染方式（通过子类实现）
 * - [AndroidMediaPlayer]：VideoView 渲染（最简单，适合快速集成）
 * - [AndroidSurfacePlayer]：SurfaceView 渲染（性能好，适合大多数场景）
 * - [AndroidTexturePlayer]：TextureView 渲染（支持动画和变换）
 *
 * ## 支持的数据源类型
 * - HTTP/HTTPS URL：在线视频流
 * - file:// 路径：本地视频文件
 * - content:// URI：Content Provider（如相册选取的视频）
 * - Assets 文件：自动复制到缓存目录后加载
 *
 * ## 状态机
 * ```
 * IDLE → PREPARING → PREPARED → PLAYING ↔ PAUSED
 *   ↑                  ↓           ↓
 *   └──────────────────┴───────────┘→ STOPPED → IDLE (可重新 prepare)
 *                                         ↓
 *                                       RELEASED (不可再使用)
 * ```
 *
 * ## 使用示例
 * ```kotlin
 * // 示例 1：使用 VideoView（推荐新手）
 * val player = AndroidMediaPlayer()
 * player.attach(videoView)
 * player.setListener(object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 *     override fun onComplete() { log("播放完成") }
 *     override fun onError(code: Int, msg: String) { log("错误: $msg") }
 * })
 * player.setSource("https://example.com/video.mp4")
 * // onPrepared 回调后会自动播放（如果配置了 autoPlay）
 *
 * // 示例 2：使用 SurfaceView
 * val player = AndroidSurfacePlayer()
 * player.attach(surfaceView)
 * player.setSource("/sdcard/video.mp4")
 * player.play() // 需要手动调用 play()
 *
 * // 页面生命周期管理
 * override fun onPause() {
 *     if (player.isPlaying) player.pause()
 *     player.detach() // 解绑 Surface
 * }
 *
 * override fun onResume() {
 *     player.attach(surfaceView) // 重新绑定
 * }
 *
 * override fun onDestroy() {
 *     player.release() // 释放资源
 * }
 * ```
 *
 * ## 重要提示
 * 1. **必须在 attach 之后才能 setSurface**：Surface 绑定是异步的，需等待就绪
 * 2. **PREPARING 状态不能调用 getDuration**：会触发 MediaPlayer 错误 (-38, 0)
 * 3. **切换 Surface 必须使用 safeSwitchToXxx 方法**：直接 detach+attach 会崩溃
 * 4. **release 后不可再使用**：必须创建新实例
 *
 * @see AndroidMediaPlayer VideoView 实现
 * @see AndroidSurfacePlayer SurfaceView 实现
 * @see AndroidTexturePlayer TextureView 实现
 * @see IVideoPlayerListener 事件回调接口
 * @see PlayerState 状态枚举
 */
abstract class BaseVideoPlayer {

    /** 日志标签，使用类名便于日志过滤和定位问题 */
    protected val TAG = this::class.java.simpleName

    /**
     * 播放引擎实例（策略模式）
     *
     * 通过依赖倒置原则，本类不关心具体使用哪种引擎，
     * 只通过 [IPlayerKernal] 接口与引擎交互。
     *
     * 子类负责创建具体的引擎实例：
     * - AndroidMediaPlayer → MediaPlayerKernal
     * - AndroidMedia3Player → ExoPlayer/Media3 引擎
     * - 未来可扩展：IjkPlayerKernal、VLCPlayerKernal 等
     *
     * @see IPlayerKernal 引擎接口定义
     */
    protected open lateinit var engine: IPlayerKernal<*>

    /**
     * 引擎事件监听器（统一处理所有引擎回调）
     *
     * 将底层引擎的事件转换为上层业务事件，
     * 并执行相应的状态管理和副作用操作。
     *
     * ## 回调处理清单
     * - **onPrepared**：准备完成 → 更新状态 + 自动恢复播放（Surface 切换场景）
     * - **onCompletion**：播放完成 → 更新状态 + 循环处理
     * - **onError**：错误发生 → 区分准备阶段/播放阶段的错误处理策略
     * - **onBufferingUpdate**：缓冲进度更新 → 转发给监听器
     * - **onVideoSizeChanged**：视频尺寸变化 → 保存尺寸 + 自适应布局
     * - **onInfo**：信息回调（日志记录）
     * - **onIsPlayingChanged**：播放状态变化 → 同步内部状态 + 进度追踪管理
     */
    protected val engineListener = object : IPlayerKernal.IListener {

        /**
         * 准备完成回调
         *
         * 触发时机：
         * - engine.prepareAsync() 异步准备完成后
         * - 数据源加载完毕，解码器已初始化
         *
         * 执行操作：
         * 1. 校验当前状态必须是 PREPARING（防止重复触发）
         * 2. 获取视频时长 duration
         * 3. 更新状态为 PREPARED
         * 4. 通知外部监听器 onPrepared
         * 5. 检查是否需要自动恢复播放（Surface 切换后的 reprepare 场景）
         */
        override fun onPrepared() {
            log(TAG, "onPrepared")
            // 防止重复触发（某些引擎可能多次调用 onPrepared）
            if (_state != PlayerState.PREPARING) return

            // 获取视频时长（此时已安全，因为已经进入 PREPARED 状态）
            val dur = duration
            _state = PlayerState.PREPARED

            // 检查是否需要自动恢复播放（Surface 切换后）
            // pendingSeekPosition > 0 表示之前保存了播放位置
            val shouldAutoResume = pendingSeekPosition >= 0
            val savedPos = pendingSeekPosition
            pendingSeekPosition = -1L  // 重置标记，防止下次误触发

            // 通知外部监听器（UI 可在此更新进度条最大值等）
            listener?.onPrepared(dur)
            log(TAG, "onPlaybackStateChanged: READY (duration=${dur}ms, autoResume=$shouldAutoResume)")

            // 如果是 Surface 切换后的 reprepare，自动恢复播放
            if (shouldAutoResume && savedPos!! >= 0) {
                log(TAG, "safeSwitchSurface [方案B]: 自动恢复播放 (position=${formatTime(savedPos)})")
                seekTo(savedPos)  // 先跳转到保存的位置
                play()             // 再开始播放
                log(TAG, "safeSwitchSurface [方案B]: 已恢复播放 (${formatTime(savedPos)})")
            }
        }

        /**
         * 播放完成回调
         *
         * 触发时机：
         * - 视频播放到末尾
         * - 注意：单曲循环时不会触发此回调（由引擎内部循环处理）
         *
         * 执行操作：
         * 1. 更新状态为 COMPLETED
         * 2. 停止进度追踪协程
         * 3. 通知外部监听器（可在此处理列表自动播放下一首等逻辑）
         */
        override fun onCompletion() {
            log(TAG, "onCompletion")
            if (_state == PlayerState.PLAYING) {
                _state = PlayerState.COMPLETED
                stopProgressTracking()
                listener?.onComplete()
            }
        }

        /**
         * 错误回调
         *
         * 根据当前状态采用不同的错误处理策略：
         *
         * **1. PREPARING 状态的错误（准备阶段失败）：**
         * - 可能原因：网络超时、文件不存在、格式不支持等
         * - 处理策略：尝试自动重试（如果 retryCount > 0）
         * - 重试耗尽后通知外部监听器
         *
         * **2. 其他状态的错误（播放过程中失败）：**
         * - 可能原因：网络中断、解码错误、文件损坏等
         * - 处理策略：立即通知外部监听器，不重试
         * - 外部可选择显示错误 UI 或尝试恢复
         *
         * @param code 错误代码（参考 [PlayerErrorCode]）
         * @param msg 错误信息描述
         */
        override fun onError(code: Int, msg: String) {
            log(TAG, "onError: $code, $msg")

            if (_state == PlayerState.PREPARING) {
                // 准备阶段出错，尝试重试
                handlePrepareError(Exception(msg))
            } else {
                // 播放阶段出错，直接通知外部
                _state = PlayerState.ERROR
                listener?.onError(code, msg)
            }
        }

        /**
         * 缓冲进度更新回调
         *
         * 触发时机：
         * - 网络视频加载过程中持续回调
         * - percent 范围：0 ~ 100
         *
         * 用途：
         * - 显示缓冲进度条
         * - 已缓冲区域高亮（如 YouTube 进度条的灰色部分）
         *
         * 优化：只在 percent > 0 时才通知（避免无意义的 0% 回调）
         */
        override fun onBufferingUpdate(percent: Int) {
//                log(TAG, "onBufferingUpdate: $percent")
            if (percent > 0) {
                listener?.onBufferingUpdate(percent)
            }
        }

        /**
         * 视频尺寸变化回调
         *
         * 触发时机：
         * - 准备完成后首次获取到视频尺寸
         * - 某些自适应码率流（HLS/DASH）切换清晰度时
         *
         * 执行操作：
         * 1. 保存视频原始宽高（用于后续布局计算）
         * 2. 通知外部监听器（可用于统计或日志）
         * 3. 触发布局自适应调整（根据 videoScaleMode 计算 View 尺寸）
         *
         * @param width 视频宽度（像素）
         * @param height 视频高度（像素）
         */
        override fun onVideoSizeChanged(width: Int, height: Int) {
            //log
            log(TAG, "onVideoSizeChanged: $width x $height")

            // 过滤无效值（某些设备可能返回 0 或负数）
            if (width > 0 && height > 0) {
                // 保存视频原始尺寸
                videoWidth = width
                videoHeight = height

                // 通知外部（可用于记录或统计）
                listener?.onVideoSizeChanged(width, height)

                // 根据当前缩放模式调整布局（FIT_CENTER/CROP_CENTER/STRETCH）
                adjustSurfaceLayout()
            }
        }

        /**
         * 信息回调（通用）
         *
         * 触发时机：
         * - 缓冲开始/结束
         * - 视频渲染首帧
         * - 视频轨道滞后警告
         *
         * 当前仅做日志记录，可根据需要扩展处理逻辑。
         */
        override fun onInfo() {
            log(TAG, "onInfo")
        }

        /**
         * 播放状态变化回调（重要！用于状态同步）
         *
         * 触发时机：
         * - engine.start() 后实际开始播放
         * - engine.pause() 后暂停
         * - Seek 操作导致临时暂停/恢复
         * - 缓冲不足导致自动暂停
         *
         * **核心作用：**
         * 解决某些情况下 onPrepared 未触发的状态不一致问题。
         * 当引擎报告 isPlaying=true 但内部状态还不是 PLAYING 时，
         * 自动修正状态并启动进度追踪。
         *
         * **特殊情况处理：**
         * - Seek 过程中的临时 isPlaying 变化会被忽略
         *   （通过 [isSeeking] 标志位控制）
         */
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // 如果正在 seek 操作中，忽略临时的 isPlaying 变化
            // 因为 seek 是异步的，中间会有短暂的暂停/恢复
            if (isSeeking) {
                log(TAG, "onIsPlayingChanged ignored during seeking: isPlaying=$isPlaying")
                return
            }

            if (isPlaying) {
                // 引擎报告正在播放，但内部状态还不是 PLAYING → 自动修正
                if (_state != PlayerState.PLAYING) {
                    _state = PlayerState.PLAYING
                    startProgressTracking()
                }
            } else {
                // 引擎报告未播放，但内部状态还是 PLAYING → 自动暂停
                if (_state == PlayerState.PLAYING) {
                    _state = PlayerState.PAUSED
                    stopProgressTracking()
                }
            }
        }
    }

    // ==================== Surface 绑定 ====================

    /** SurfaceView 实例（SurfaceView 渲染模式时使用）*/
    protected var surfaceView: SurfaceView? = null

    /** TextureView 实例（TextureView 渲染模式时使用）*/
    protected var textureView: TextureView? = null

    /**
     * 当前绑定的 Surface 类型
     *
     * 用于区分当前的渲染方式，影响：
     * - Surface 绑定/解绑逻辑
     * - 布局自适应计算
     * - Surface 切换时的行为
     *
     * @see SurfaceType
     */
    protected var currentSurfaceType: SurfaceType = SurfaceType.NONE

    /**
     * Surface 是否已就绪（可用于渲染）
     *
     * Surface 创建是异步过程：
     * - SurfaceView：需要在 surfaceCreated 回调后才可用
     * - TextureView：通常立即可用，但也可能在 View 未 attach 到 Window 时不可用
     * - VideoView：同 SurfaceView
     *
     * 此标志位用于 PendingPrepare 机制：
     * - Surface 未就绪时调用 setSource → 缓存参数
     * - Surface 就绪后 → 自动执行缓存的 prepare
     *
     * 使用 @Volatile 保证多线程可见性（Surface 回调可能在非主线程）
     */
    @Volatile
    protected var isSurfaceReady: Boolean = false

    /**
     * 是否正在执行 Seek 操作
     *
     * Seek 是异步操作，会导致临时的播放状态变化：
     * - Seek 开始 → isPlaying 变为 false（短暂）
     * - Seek 完成 → isPlaying 恢复为 true
     *
     * 此标志位用于：
     * 1. 阻止 Seek 过程中误停进度追踪
     * 2. 忽略 onIsPlayingChanged 的临时状态变化
     *
     * 使用 @Volatile 保证多线程可见性
     */
    @Volatile
    protected var isSeeking: Boolean = false

    // ==================== 状态管理 ====================

    /**
     * 内部状态（带日志的状态转换器）
     *
     * 每次状态变更都会：
     * 1. 记录日志（旧状态 → 新状态）
     * 2. 通知外部监听器（用于 UI 更新）
     *
     * 状态转换规则（必须遵守，否则会抛异常）：
     * ```
     * IDLE → PREPARING （调用 setSource 后）
     * PREPARING → PREPARED （onPrepared 回调）
     * PREPARED → PLAYING （调用 play）
     * PLAYING ↔ PAUSED （调用 pause/play）
     * ANY → STOPPED （调用 stop，除 IDLE/RELEASED）
     * STOPPED → IDLE （stop 后自动重置）
     * ANY → RELEASED （调用 release）
     * ```
     *
     * @see PlayerState 状态枚举定义
     */
    protected var _state: PlayerState = PlayerState.IDLE
        set(value) {
            val old = field
            if (old != value) {
                // 记录状态转换日志（便于调试）
                log(TAG, "state: $old -> $value")
                field = value
                // 通知外部监听器（UI 可根据状态更新按钮文字、显隐等）
                listener?.onStateChanged(old, value)
            }
        }

    /** 当前状态（只读，外部查询用）*/
    val state get() = _state

    /**
     * 是否正在播放（双重检查）
     *
     * 双重检查机制：
     * 1. 内部状态必须是 PLAYING
     * 2. 引擎实际确实在播放（isPlaying()）
     *
     * 为什么需要双重检查？
     * - 内部状态可能因异常而不同步
     * - 引擎可能因缓冲等原因临时暂停
     * - 两者结合更准确反映真实播放状态
     */
    val isPlaying: Boolean
        get() = _state == PlayerState.PLAYING && engine.isPlaying()

    /** 当前播放位置（毫秒），任何状态下都可安全调用（异常时返回 0）*/
    val currentPosition: Long
        get() = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }

    /**
     * 视频总时长（毫秒）
     *
     * ⚠️ 只有 PREPARED 及之后的状态才能获取到有效值
     * 在 IDLE/PREPARING 状态下调用会返回 0 或抛异常
     *
     * 已做异常捕获，不会崩溃，但返回值可能不准确
     */
    val duration: Long
        get() = try { engine.getDuration() } catch (_: Exception) { 0L }

    // ==================== 配置属性 ====================

    /**
     * 循环模式（默认不循环）
     *
     * 支持的模式：
     * - [LoopMode.NONE]：不循环，播放完停止
     * - [LoopMode.SINGLE]：单曲循环（播放完自动从头开始）
     * - [LoopMode.ALL]：列表循环（播放完触发 onComplete，由外部决定下一个）
     *
     * 设置时会同步应用到引擎
     */
    var loopMode: LoopMode = LoopMode.NONE
        set(value) {
            field = value
            engine.loopMode(value)
        }

    /**
     * 进度回调间隔（毫秒），默认 200ms
     *
     * 决定 onProgress 回调的频率。
     * 值越小越流畅，但 CPU 开销越大。
     * 推荐范围：100ms ~ 500ms
     */
    var progressIntervalMs: Int = 200

    /**
     * 自动重试次数（默认 0，不重试）
     *
     * 仅对 **准备阶段（PREPARING）** 的错误生效。
     * 播放过程中的错误不会自动重试。
     *
     * 重试机制：
     * - 每次 prepare 失败后延迟 500ms 重试
     * - 重试次数耗尽后通知外部 onError
     * - 典型用途：网络不稳定时的自动恢复
     */
    var retryCount: Int = 0

    /**
     * 变速倍率（0.5x ~ 3.0x），默认 1.0x（正常速度）
     *
     * 设置时会自动限制在合法范围内（coerceIn）。
     * 需要 API 23+ (Android 6.0) 才支持变速。
     *
     * 常用值：
     * - 0.5x：慢放（学习/分析动作）
     * - 1.0x：正常速度
     * - 1.25x / 1.5x / 2.0x：快进（节省时间）
     */
    var speed: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 3.0f)
            engine.speed(field)
        }

    /**
     * 音量（0.0 ~ 1.0），默认 1.0（最大音量）
     *
     * 设置时会自动限制在合法范围内。
     * 0.0 = 静音，1.0 = 最大音量
     */
    var volume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            engine.volume(field)
        }

    /**
     * 视频缩放模式（默认 FIT_CENTER）
     *
     * 控制视频如何在容器中显示：
     * - [VideoScaleMode.FIT_CENTER]：保持比例，完整显示（可能有黑边）
     * - [VideoScaleMode.CROP_CENTER]：保持比例，填满容器（可能裁剪边缘）
     * - [VideoScaleMode.STRETCH]：拉伸填满容器（可能变形）
     *
     * 设置时会立即触发布局重新计算。
     * 对于 SurfaceView/TextureView，需要等待 [onVideoSizeChanged] 获取到视频尺寸后才生效。
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            adjustSurfaceLayout()
        }

    // ==================== 内部状态 ====================

    /** 进度追踪协程（用于定期回调 onProgress）*/
    protected var progressJob: Job? = null

    /** 当前数据源 URI（用于 Surface 切换后重新 prepare）*/
    protected var currentUri: Uri? = null

    /** 当前数据源 HTTP 请求头（用于带鉴权的视频源）*/
    protected var currentHeaders: Map<String, String>? = null

    /** 当前数据源 Asset 路径（如果是 Assets 文件）*/
    protected var currentAssetPath: String? = null

    /** 剩余重试次数（每次 prepare 成功后重置，失败后递减）*/
    protected var retryLeft: Int = 0

    /** 视频原始宽度（像素），在 onVideoSizeChanged 中更新*/
    protected var videoWidth: Int = 0

    /** 视频原始高度（像素），在 onVideoSizeChanged 中更新*/
    protected var videoHeight: Int = 0

    /** 外部事件监听器（回调所有播放事件）*/
    protected var listener: IVideoPlayerListener? = null

    /**
     * 待执行的 Prepare 参数（PendingPrepare 机制）
     *
     * **使用场景：**
     * Surface 创建是异步的，如果在 Surface 就绪前调用了 setSource，
     * 此时无法执行 prepare（因为没有 Surface 可以绑定），
     * 所以将参数缓存到这里，等 Surface 就绪后再执行。
     *
     * **典型流程：**
     * ```
     * 1. attach(surfaceView) → Surface 还没准备好
     * 2. setSource(url) → 检测到 !isSurfaceReady → 存入 pendingPrepare
     * 3. ... 一段时间后 ...
     * 4. surfaceCreated() 回调 → Surface 就绪
     * 5. executePendingPrepare() → 取出参数并执行 prepare
     * ```
     */
    protected var pendingPrepare: PendingPrepare? = null

    /**
     * 切换 Surface 后待恢复的播放位置（毫秒）
     *
     * 默认值 -1 表示无需恢复。
     * 在 safeSwitchSurface 方法中设置，在 onPrepared 中消费。
     *
     * **为什么需要这个字段？**
     * Surface 切换需要 stop → 切换 → reprepare，
     * reprepare 后会丢失之前的播放位置，
     * 所以需要在切换前保存位置，在 onPrepared 后恢复。
     */
    protected var pendingSeekPosition: Long = -1L

    /**
     * 是否正在执行安全切换操作（防止重复调用）
     *
     * 用户快速连续点击切换按钮时，可能导致多次切换同时进行，
     * 此标志位用于防抖，确保同一时间只有一个切换操作在执行。
     *
     * 使用 @Volatile 保证多线程可见性
     */
    @Volatile
    protected var isSafeSwitching: Boolean = false

    /** PREPARING 状态监控协程（作为 onPrepared 的备用检测机制）*/
    protected var preparingMonitorJob: Job? = null

    /** 切换 Surface 后待恢复的播放位置（备用字段，用于不同的切换场景）*/
    protected var switchSurfacePendingPosition: Long = -1L


    /**
     * SurfaceView 的 SurfaceHolder 回调
     *
     * 监听 SurfaceView 的 Surface 生命周期：
     * - surfaceCreated：Surface 创建完成，可以绑定到引擎
     * - surfaceChanged：Surface 尺寸变化（屏幕旋转等）
     * - surfaceDestroyed：Surface 即将销毁，必须解绑
     */
    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
        /**
         * Surface 创建完成
         *
         * 此时 Surface 已经可以用于渲染，
         * 需要立即：
         * 1. 标记 Surface 就绪
         * 2. 将 Surface 绑定到引擎
         * 3. 执行之前缓存的 prepare（如果有）
         */
        override fun surfaceCreated(holder: SurfaceHolder) {
            log(TAG, "surfaceCreated")
            onSurfaceReady(holder.surface)
        }

        /**
         * Surface 尺寸变化
         *
         * 触发场景：
         * - 屏幕旋转
         * - 容器尺寸变化
         * - 从全屏返回小窗
         *
         * 通常需要重新计算布局以适应新的尺寸
         */
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            log(TAG, "surfaceChanged: ${width}x${height}")
        }

        /**
         * Surface 即将销毁
         *
         * ⚠️ 重要：必须在此时解绑 Surface，否则会崩溃！
         *
         * 执行操作：
         * 1. 标记 Surface 不可用
         * 2. 将引擎的 Surface 设为 null（释放资源）
         */
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            log(TAG, "surfaceDestroyed")
            isSurfaceReady = false
            engine.setSurface(null)
        }
    }


    // ==================== 绑定渲染 ====================

    /**
     * 绑定 SurfaceView（兼容方式）
     *
     * 会设置 SurfaceHolder.Callback 监听 Surface 创建/销毁/变化。
     * Surface 可能需要时间才能就绪，此时会使用 PendingPrepare 机制缓存操作。
     *
     * @param surfaceView 外部创建的 SurfaceView 实例
     */
    fun attach(surfaceView: SurfaceView) {
        detach()

        this.surfaceView = surfaceView
        this.currentSurfaceType = SurfaceType.SURFACE_VIEW
        this.isSurfaceReady = false  // Surface 需要异步创建

        log(TAG, "attach SurfaceView (waiting for surface)")

        initPlayer()
        setupSurfaceViewCallback()

        // 检查 Surface 是否已经可用（某些情况下立即可用）
        if (surfaceView.holder.surface.isValid) {
            onSurfaceReady(surfaceView.holder.surface)
        }
    }

    /**
     * 绑定 TextureView（高级方式）
     *
     * 会设置 SurfaceTextureListener 监听 Surface 可用/尺寸变化/销毁。
     * TextureView 的 Surface 通常比 SurfaceView 更快可用。
     *
     * @param textureView 外部创建的 TextureView 实例
     */
    fun attach(textureView: TextureView) {
        detach()

        this.textureView = textureView
        this.currentSurfaceType = SurfaceType.TEXTURE_VIEW
        this.isSurfaceReady = false  // Surface 需要异步准备

        log(TAG, "attach TextureView (waiting for surface)")

        initPlayer()
        setupTextureViewCallback()

        // 检查 Surface 是否已经可用
        if (textureView.isAvailable) {
            onSurfaceReady(Surface(textureView.surfaceTexture))
        }
    }


    /**
     * 解绑当前 Surface（页面 onPause/onDestroyView 时调用）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     * 不释放内部 ExoPlayer 和其他资源。
     */
    open fun detach() {
        when (currentSurfaceType) {
            SurfaceType.SURFACE_VIEW -> {
                detachSurfaceView()
            }
            SurfaceType.TEXTURE_VIEW -> {
                detachTextureView()
            }
            SurfaceType.NONE -> {}
            else -> {}
        }

        currentSurfaceType = SurfaceType.NONE
    }

    /**
     * 调整 SurfaceView/TextureView 的布局尺寸
     */
    protected open fun adjustSurfaceLayout() {
        when (currentSurfaceType) {
            SurfaceType.SURFACE_VIEW -> VideoHelper.adjustRenderViewLayout(surfaceView, videoWidth, videoHeight, videoScaleMode)
            SurfaceType.TEXTURE_VIEW -> VideoHelper.adjustRenderViewLayout(textureView, videoWidth, videoHeight, videoScaleMode)
            else -> {}
        }
    }

    protected fun detachSurfaceView() {
        surfaceView?.holder?.removeCallback(surfaceHolderCallback)
        surfaceView = null
        isSurfaceReady = false
        log(TAG, "detach SurfaceView")
    }

    protected fun detachTextureView() {
        textureView?.surfaceTextureListener = null
        textureView = null
        isSurfaceReady = false
        log(TAG, "detach TextureView")
    }

    // ==================== 数据源设置 ====================

    /**
     * 设置数据源并开始准备（不自动播放）
     *
     * 支持的数据源类型：
     * - HTTP/HTTPS URL：在线视频
     * - file:// 路径：本地文件
     * - content:// URI：Content Provider
     * - file:///android_asset/filename.mp4：Assets 目录（自动复制到缓存）
     *
     * 准备完成后回调 [IVideoPlayerListener.onPrepared]，此时需调用 [play] 开始播放。
     *
     * @param url 数据源地址
     */
    fun setSource(url: String) {
        val uri = Uri.parse(url)

        // 处理 Assets 文件（需特殊处理）
        if (uri.scheme == "file" && uri.path?.contains("/android_asset/") == true) {
            val assetPath = uri.path?.substringAfter("/android_asset/") ?: ""
            setAssetSource(assetPath)
            return
        }

        doSetSource(uri, null, null)
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
     * @param uri 视频 URI（支持 http/https/file/content 协议）
     * @param headers HTTP 请求头（仅对 http(s) 协议生效，可为 null）
     */
    fun setSource(uri: Uri, headers: Map<String, String>? = null) {
        // 处理 Assets 文件（需特殊处理）
        if (uri.scheme == "file" && uri.path?.contains("/android_asset/") == true) {
            val assetPath = uri.path?.substringAfter("/android_asset/") ?: ""
            setAssetSource(assetPath)
            return
        }

        doSetSource(uri, headers, null)
    }

    /**
     * 设置 assets 目录下的视频文件并准备（不自动播放）
     *
     * 由于 ExoPlayer 无法直接读取 Assets 中的文件，
     * 此方法会将文件复制到内部缓存目录后再加载。
     *
     * @param path Assets 中的相对路径（如 "video/test.mp4"）
     */
    fun setAssetSource(path: String) {
        try {
            val context = App.context
            val cacheFile = File(context.cacheDir, "asset_video_${path.hashCode()}")

            // 如果缓存文件不存在或 Assets 文件更新了，重新复制
            if (!cacheFile.exists()) {
                context.assets.open(path).use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                log(TAG, "copied asset to cache: ${cacheFile.absolutePath}")
            }

            doSetSource(Uri.fromFile(cacheFile), null, path)
        } catch (e: Exception) {
            log(TAG, "setAssetSource error: ${e.message}")
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.ASSET_COPY_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.ASSET_COPY_FAILED, e.message))
        }
    }

    // ==================== 播放控制 ====================

    /**
     * 开始播放 或 从暂停恢复播放
     *
     * - PREPARED/COMPLETED → 开始播放
     * - PAUSED → 恢复播放
     * - 其他状态 → 忽略
     */
    open fun play() {
        when (_state) {
            PlayerState.PREPARED, PlayerState.COMPLETED -> {
                engine.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                startPreparingStateMonitor()
                log(TAG, "play() -> PLAYING (from ${_state})")
            }
            PlayerState.PAUSED -> {
                engine.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                startPreparingStateMonitor()
                log(TAG, "play() -> PLAYING (from PAUSED)")
            }
            else -> {
                log(TAG, "play() ignored (current state: $_state)")
            }
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        log(TAG, "pause (state=$_state)")
        if (_state == PlayerState.PLAYING) {
            try {
                engine.pause()
                _state = PlayerState.PAUSED
                stopProgressTracking()
            } catch (e: Exception) {
                log(TAG, "pause error: ${e.message}")
            }
        }
    }

    /**
     * 停止播放（保留资源，可重新 prepare）
     */
    open fun stop() {
        log(TAG, "stop (state=$_state)")
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return
        try {
            stopProgressTracking()
            stopPreparingMonitor()
            engine.stop()
            _state = PlayerState.IDLE
        } catch (e: Exception) {
            log(TAG, "stop error: ${e.message}")
        }
    }

    /**
     * 跳转到指定位置
     *
     * @param positionMs 目标位置（毫秒）
     */
    open fun seekTo(positionMs: Long) {
        log(TAG, "seekTo $positionMs (state=$_state)")
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return
        try {
            isSeeking = true  // 标记正在 seek，防止误停进度追踪
            engine.seekTo(positionMs)
            // 延迟重置标志并确保进度追踪正常运行（seek 是异步操作）
            stopProgressTracking()
            App.mainHandler.postDelayed({
                isSeeking = false
                // 确保 seek 完成后进度追踪仍在运行
                if (_state == PlayerState.PLAYING && (progressJob == null || !progressJob!!.isActive)) {
                    log(TAG, "restart progress tracking after seek")
                    startProgressTracking()
                }
            }, 300)
        } catch (e: Exception) {
            log(TAG, "seekTo error: ${e.message}")
            isSeeking = false
        }
    }

    /**
     * 设置 SurfaceView 的 SurfaceHolder 回调
     */
    protected open fun setupSurfaceViewCallback() {
        surfaceView?.holder?.addCallback(surfaceHolderCallback)

        // 如果 Surface 已经可用（例如复用的情况）
        if (surfaceView?.holder?.surface?.isValid == true) {
            isSurfaceReady = true
            engine.setSurface(surfaceView?.holder?.surface)
            executePendingPrepare()
        }
    }

    /**
     * 设置 TextureView 的 SurfaceTextureListener 回调
     */
    protected open fun setupTextureViewCallback() {
        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                log(TAG, "onSurfaceTextureAvailable: ${width}x${height}")
                onSurfaceReady(Surface(surface))
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                log(TAG, "onSurfaceTextureSizeChanged: ${width}x${height}")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                log(TAG, "onSurfaceTextureDestroyed")
                isSurfaceReady = false
                engine.setSurface(null)
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        // 如果 SurfaceTexture 已经可用
        if (textureView?.isAvailable == true && textureView?.surfaceTexture != null) {
            isSurfaceReady = true
            engine.setSurface(Surface(textureView?.surfaceTexture))
            executePendingPrepare()
        }
    }

    /**
     * Surface 就绪处理（统一入口）
     */
    protected fun onSurfaceReady(surface: Surface) {
        isSurfaceReady = true
        log(TAG, "Surface ready")

        // 将 Surface 设置给 ExoPlayer
        try {
            engine.setSurface(surface)
        } catch (e: Exception) {
            log(TAG,"setSurface error: ${e.message}")
        }

        // 如果有待执行的 prepare，立即执行
        pendingPrepare?.let {
            executePendingPrepare()
        }
    }

    /**
     * 执行实际的 prepare 操作
     */
    protected open fun doPrepareInternal(uri: Uri?, headers: Map<String, String>?) {
        log(TAG, "doPrepareInternal: $uri")
        uri?: return

        try {
            _state = PlayerState.PREPARING

            engine.reset()
            engine.setSource(uri, headers)

            // 绑定当前 Surface
            when (currentSurfaceType) {
                SurfaceType.SURFACE_VIEW -> {
                    surfaceView?.holder?.let { engine.setSurface(it.surface) }
                }
                SurfaceType.TEXTURE_VIEW -> {
                    textureView?.let { engine.setSurface(Surface(it.surfaceTexture)) }
                }
                else -> {}
            }

            // 应用当前参数
            engine.volume(volume)
            engine.speed(speed)
            engine.loopMode(loopMode)

            // 异步准备
            engine.prepareAsync()

            // ✨ 关键修复：启动 PREPARING 状态监控协程
            // 防止 IJKPlayer 异步特性导致状态不一致：
            // - IjkMediaPlayer 可能在 onPrepared 回调前就开始播放
            //# - 此时需要主动检测并修正状态
            startPreparingStateMonitor()

        } catch (e: Exception) {
            log(TAG, "doPrepareInternal error: ${e.message}")
            handlePrepareError(e)
        }
    }

    /**
     * 执行缓存的 prepare 操作
     */
    protected open fun executePendingPrepare() {
        pendingPrepare?.let { pending ->
            log(TAG, "executing pending prepare: ${pending.uri}")
            pendingPrepare = null
            doPrepareInternal(pending.uri, pending.headers)
        }
    }

    // ==================== 私有方法：初始化 ====================

    /**
     * 初始化 Player 实例
     */
    protected fun initPlayer() {
        engine.loopMode(loopMode)
        engine.speed(speed)
        if (volume != 1.0f) {
            engine.volume(volume)
        }
    }


    /**
     * 处理准备错误（可能触发重试）
     */
    protected open fun handlePrepareError(e: Exception) {
        if (retryLeft > 0) {
            retryLeft--
            log(TAG, "retrying... ($retryLeft left)")
            App.mainHandler.postDelayed({
                doPrepareInternal(currentUri, currentHeaders)
            }, 1000)
        } else {
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.RETRY_EXHAUSTED, PlayerErrorCode.formatError(PlayerErrorCode.RETRY_EXHAUSTED, e.message))
        }
    }

    // ==================== 私有方法：资源释放 ====================

    /**
     * 释放 ExoPlayer 实例
     */
    protected open fun releasePlayer() {
        log(TAG, "released")
        stopProgressTracking()
        stopPreparingMonitor()
        engine.release()
    }

    /**
     * 启动进度追踪协程
     *
     * 定时获取 ExoPlayer 的当前位置和总时长，通过监听器回调。
     */
    protected fun startProgressTracking() {
        // 如果已经在运行且状态正确，不需要重启
        if (progressJob != null && progressJob!!.isActive && _state == PlayerState.PLAYING) {
            log(TAG, "progress tracking already running")
            return
        }

        stopProgressTracking()
        log(TAG, "starting progress tracking (state: $_state)")

        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _state == PlayerState.PLAYING) {
                val pos = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }
                val dur = try {
                    engine.getDuration()
                } catch (_: Exception) { 0L }
                listener?.onProgress(pos, dur)
                delay(progressIntervalMs.toLong())
            }
            log(TAG, "progress tracking stopped (loop exited, state: $_state)")
        }
    }

    /**
     * 停止进度追踪协程
     */
    protected fun stopProgressTracking() {
        if (progressJob != null) {
            log(TAG, "stopping progress tracking")
            progressJob?.cancel()
            progressJob = null
        }
    }

    /**
     * 释放所有资源，调用后不可再使用此实例
     */
    fun release() {
        detach()
        releasePlayer()
        listener = null
        currentUri = null
        currentHeaders = null
        currentAssetPath = null
        pendingPrepare = null
        pendingSeekPosition = -1L
        _state = PlayerState.RELEASED
        log(TAG, "release() -> RELEASED")
    }

    // ==================== 监听器设置 ====================

    /**
     * 设置播放事件监听器
     */
    fun setVideoPlayerListener(listener: IVideoPlayerListener?) {
        this.listener = listener
    }

    /**
     * 执行实际的 setSource 操作
     */
    protected fun doSetSource(uri: Uri, headers: Map<String, String>?, assetPath: String?) {
        if (_state == PlayerState.RELEASED) return

        // 停止当前的进度追踪（切换数据源前必须清理）
        stopProgressTracking()

        currentUri = uri
        currentHeaders = headers
        currentAssetPath = assetPath
        retryLeft = retryCount

        // 如果 Surface 未就绪，缓存待执行的 prepare
        if (!isSurfaceReady && currentSurfaceType != SurfaceType.NONE) {
            log(TAG, "Surface not ready, caching prepare request")
            pendingPrepare = PendingPrepare(uri, headers, assetPath)
            _state = PlayerState.PREPARING
            return
        }

        doPrepareInternal(uri, headers)
    }


    /**
     * 安全切换到 SurfaceView
     *
     * @param surfaceView 目标 SurfaceView 实例
     * @param delayMs 延迟时间（毫秒），默认 100ms，确保 MediaCodec 状态稳定
     */
    fun safeSwitchToSurfaceView(surfaceView: SurfaceView, delayMs: Long = 100L) {
        safeSwitchSurface(
            targetAction = { attach(surfaceView) },
            targetName = "SurfaceView",
            delayMs = delayMs
        )
    }

    /**
     * 安全切换到 TextureView
     *
     * @param textureView 目标 TextureView 实例
     * @param delayMs 延迟时间（毫秒），默认 100ms，确保 MediaCodec 状态稳定
     */
    fun safeSwitchToTextureView(textureView: TextureView, delayMs: Long = 100L) {
        safeSwitchSurface(
            targetAction = { attach(textureView) },
            targetName = "TextureView",
            delayMs = delayMs
        )
    }

    /**
     * 安全切换 Surface 的核心实现
     *
     * **方案 B：stop → 切换 → reprepare**
     *
     * 流程时间线：
     * T0: 用户点击切换
     *    ├─ 记录状态 (wasPlaying, savedPosition)
     *    └─ stop() → 清空所有缓冲区
     * T0+100ms:
     *    ├─ detach() + attach() → 安全切换 Surface
     *    └─ doSetSource(currentUri, currentHeaders) → 重新准备
     * T0+500ms~1s (异步):
     *    └─ onPrepared 触发
     *       ├─ seekTo(savedPosition) → 恢复位置
     *       └─ play() → 继续播放
     *
     * @param targetAction 实际的 attach 操作
     * @param targetName 目标名称（用于日志）
     * @param delayMs 延迟时间（毫秒），默认 100ms
     */
    protected fun safeSwitchSurface(
        targetAction: () -> Unit,
        targetName: String,
        delayMs: Long
    ) {
        // 防重复调用
        if (isSafeSwitching) {
            log(TAG,"safeSwitchSurface [方案B]: ⚠️ 忽略重复调用（正在切换到 $targetName）")
            return
        }

        isSafeSwitching = true

        try {
            // 1. 记录当前状态（扩展到 PREPARING 状态）
            val wasPlaying = (_state == PlayerState.PLAYING || _state == PlayerState.PAUSED ||
                    _state == PlayerState.PREPARING)
            val savedPosition = currentPosition

            log(TAG,"safeSwitchSurface [方案B]: 开始切换到 $targetName" +
                    " (wasPlaying=$wasPlaying, position=${formatTime(savedPosition)}, state=$_state)")

            // 2. 完全停止 MediaPlayer（清空所有缓冲区和渲染队列）
            if (_state != PlayerState.IDLE && _state != PlayerState.STOPPED && _state != PlayerState.RELEASED) {
                stop()
                log(TAG,"safeSwitchSurface [方案B]: 已 stop()，清空所有缓冲区")
            }

            // 3. 如果需要恢复播放，保存位置信息
            if (wasPlaying && savedPosition >= 0 && currentUri != null) {
                pendingSeekPosition = savedPosition
                log(TAG,"safeSwitchSurface [方案B]: 待恢复位置 ${formatTime(savedPosition)}")
            }

            // 4. 使用 postDelayed 延迟执行切换操作
            App.mainHandler.postDelayed({
                try {
                    log(TAG,"safeSwitchSurface [方案B]: 执行切换到 $targetName")

                    // 执行实际的切换操作（detach + attach）
                    targetAction()

                    // ✨ 确保 Surface 完全就绪后再 prepare
                    val surfaceReady = checkSurfaceValid()

                    if (!surfaceReady && currentSurfaceType != SurfaceType.NONE) {
                        log(TAG,"safeSwitchSurface [方案B]: ⚠️ Surface 未就绪，延迟 50ms 等待...")

                        App.mainHandler.postDelayed({
                            try {
                                performPrepareAfterSwitch()
                            } catch (e: Exception) {
                                handlePrepareFailure(e)
                            }
                        }, 50L)
                    } else {
                        log(TAG,"safeSwitchSurface [方案B]: Surface 已就绪，立即 prepare")
                        try {
                            performPrepareAfterSwitch()
                        } catch (e: Exception) {
                            handlePrepareFailure(e)
                        }
                    }
                } catch (e: Exception) {
                    log(TAG,"safeSwitchSurface [方案B]: 切换失败 - ${e.message}")
                    pendingSeekPosition = -1L
                    _state = PlayerState.ERROR
                    listener?.onError(PlayerErrorCode.SURFACE_SWITCH_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.SURFACE_SWITCH_FAILED, e.message))
                } finally {
                    isSafeSwitching = false
                }
            }, delayMs)

        } catch (e: Exception) {
            log(TAG,"safeSwitchSurface [方案B]: 准备阶段失败 - ${e.message}")
            isSafeSwitching = false
            pendingSeekPosition = -1L
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.PREPARE_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message))
        }
    }

    protected open fun checkSurfaceValid(): Boolean {
        when (currentSurfaceType) {
            SurfaceType.SURFACE_VIEW -> {
                surfaceView?.holder?.surface?.isValid == true
            }
            SurfaceType.TEXTURE_VIEW -> {
                textureView?.let { tv ->
                    tv.isAvailable && tv.surfaceTexture != null
                } == true
            }
            else -> false
        }
        return false
    }

    /**
     * 执行切换后的 prepare 操作（统一入口）
     */
    protected fun performPrepareAfterSwitch() {
        if (currentUri == null && currentAssetPath == null) {
            log(TAG,"safeSwitchSurface [方案B]: 切换完成（无数据源）")
            return
        }

        log(TAG,"safeSwitchSurface [方案B]: 重新准备数据源" +
                " (autoResume=${pendingSeekPosition >= 0})")

        if (currentAssetPath != null && currentAssetPath!!.isNotEmpty()) {
            // Assets 数据源：必须手动处理文件复制 + prepare
            log(TAG,"safeSwitchSurface [方案B]: 使用 setAssetSource (assets)")

            try {
                val context = App.context.applicationContext
                val cacheFile = File(context.cacheDir, "asset_video_${currentAssetPath!!.hashCode()}")

                // 如果缓存文件不存在，从 assets 重新复制
                if (!cacheFile.exists()) {
                    context.assets.open(currentAssetPath!!).use { input ->
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    log(TAG,"safeSwitchSurface [方案B]: 已重新复制 asset 文件")
                }

                // 直接使用 doPrepareInternal，跳过 isSurfaceReady 检查
                val cacheUri = Uri.fromFile(cacheFile)
                currentUri = cacheUri
                doPrepareInternal(cacheUri, currentHeaders)

            } catch (e: Exception) {
                log(TAG,"safeSwitchSurface [方案B]: Asset 处理失败 - ${e.message}")
                handlePrepareFailure(e)
            }

        } else if (currentUri != null) {
            // 普通 URI（HTTP/本地文件/Content Provider）：直接 prepare
            log(TAG,"safeSwitchSurface [方案B]: 使用 doPrepareInternal (uri=$currentUri)")
            doPrepareInternal(currentUri!!, currentHeaders)
        }

        log(TAG,"safeSwitchSurface [方案B]: prepare 完成，等待 onPrepared 或 PREPARING Monitor")
    }

    /**
     * 处理 prepare 失败的情况
     */
    private fun handlePrepareFailure(e: Exception) {
        log(TAG,"safeSwitchSurface [方案B]: prepare 失败 - ${e.message}")
        pendingSeekPosition = -1L
        _state = PlayerState.ERROR
        listener?.onError(PlayerErrorCode.PREPARE_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message))
    }


    /**
     * 启动 PREPARING 状态监控协程
     *
     * 作为 onPrepared 回调的备用方案：
     * 在某些情况下（特别是快速 stop+reprepare），
     * MediaPlayer 可能不触发 OnPreparedListener，
     * 此时会通过轮询检测播放是否已经开始。
     *
     * ⚠️ 重要：PREPARING 状态下不能调用 getDuration()，
     * 否则会触发 MediaPlayer 错误 (-38, 0)。
     * 只使用 isPlaying() 来检测是否已准备好。
     */
    protected fun startPreparingStateMonitor() {
        preparingMonitorJob?.cancel()
        preparingMonitorJob = CoroutineScope(Dispatchers.Main).launch {
            val maxCheckTime = 30000L  // 最大检查时间 30 秒
            val startTime = System.currentTimeMillis()

            while (isActive && System.currentTimeMillis() - startTime < maxCheckTime) {
                if (_state != PlayerState.PREPARING) return@launch

                delay(100)

                try {
                    // ✅ 只检查 isPlaying()，不在 PREPARING 状态调用 getDuration()
                    val actualIsPlaying = engine.isPlaying()

                    if (actualIsPlaying) {
                        // 正在播放 → 已准备就绪（延迟获取 duration 避免状态冲突）
                        log(TAG,"⚡ PREPARING Monitor: 检测到正在播放！修正状态")

                        // 延迟获取 duration（确保 MediaPlayer 已完全进入 PLAYING 状态）
                        var dur = 0L
                        try {
                            // 小延迟后获取，避免在状态转换临界点调用
                            delay(50)
                            dur = engine.getDuration()
                        } catch (_: Exception) {
                            log(TAG,"⚠️ PREPARING Monitor: 获取 duration 失败（可能还在准备中）")
                        }

                        val pos = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }
                        log(TAG,"PREPARING Monitor: duration=$dur, position=$pos")

                        // ✨ 检查是否需要自动恢复播放（Surface 切换后）
                        val shouldAutoResume = pendingSeekPosition >= 0
                        val savedPos = pendingSeekPosition
                        pendingSeekPosition = -1L  // 重置标记

                        log(TAG,"PREPARING Monitor: autoResume=$shouldAutoResume" +
                                (if (shouldAutoResume) ", savedPosition=${formatTime(savedPos)}" else ""))

                        // 通知准备完成（如果还没通知过）
                        listener?.onPrepared(dur.toLong())

                        // 如果是 Surface 切换后的 reprepare，自动恢复播放位置
                        if (shouldAutoResume && savedPos >= 0) {
                            log(TAG,"PREPARING Monitor [方案B]: 自动恢复播放 " +
                                    "(position=${formatTime(savedPos)})")

                            // Monitor 检测到 isPlaying=true，只需 seekTo
                            seekTo(savedPos.toLong())
                            log(TAG,"PREPARING Monitor [方案B]: 已恢复播放 " +
                                    "(${formatTime(savedPos)})")
                        }

                        // 更新为正确状态
                        _state = PlayerState.PLAYING
                        listener?.onStateChanged(PlayerState.PREPARING, PlayerState.PLAYING)

                        // 启动进度追踪
                        startProgressTracking()

                        return@launch  // 任务完成，退出监控
                    }
                } catch (_: Exception) {}
            }

            // 超时
            if (_state == PlayerState.PREPARING) {
                log(TAG,"PREPARING Monitor: 超时，仍处于 PREPARING 状态")
            }
        }
    }

    /**
     * 停止 PREPARING 状态监控
     */
    protected fun stopPreparingMonitor() {
        if (preparingMonitorJob != null) {
            log(TAG,"stopping PREPARING state monitor")
            preparingMonitorJob?.cancel()
            preparingMonitorJob = null
        }
    }

    // ==================== 内部：视频尺寸获取与自适应 ====================

    /**
     * 主动获取视频尺寸并触发画面自适应（备用方案）
     *
     * **核心修复：解决 OnVideoSizeChangedListener 不回调的问题**
     *
     * IJKPlayer 的已知问题：
     * - setOnVideoSizeChangedListener 在某些情况下不回调
     * - 特别是对于某些视频格式或网络视频
     *
     * 解决方案：
     * - 在 onPrepared 后主动调用此方法
     * - 通过 IjkMediaPlayer.getVideoWidth()/getVideoHeight() 获取尺寸
     * - 如果获取失败，延迟重试最多 5 次
     */
    protected fun tryFetchVideoSizeAndAdjustLayout(retryCount: Int = 0) {
        val maxRetries = 5

        try {
            // ✨ 主动从 IjkMediaPlayer 获取视频尺寸
            val w = engine.getVideoWidth()
            val h = engine.getVideoHeight()

            log(TAG, "tryFetchVideoSize: 尝试 #$retryCount, size=${w}x${h}")

            if (w > 0 && h > 0) {
                // ✅ 成功获取到有效尺寸

                // 检查是否与当前记录的尺寸不同（避免重复调整）
                if (w != videoWidth || h != videoHeight) {
                    log(TAG, "✨ 主动获取到视频尺寸: ${videoWidth}x${videoHeight} -> ${w}x${h}")

                    videoWidth = w
                    videoHeight = h

                    // 通知监听器
                    listener?.onVideoSizeChanged(w, h)

                    // 触发画面自适应
                    adjustSurfaceLayout()
                } else {
                    log(TAG, "视频尺寸未变化: ${w}x${h}")
                }

                return  // 成功，不需要重试
            } else {
                // ❌ 尺寸无效，需要重试
                if (retryCount < maxRetries) {
                    log(TAG, "视频尺寸无效 (${w}x${h})，将在 ${(retryCount + 1) * 200}ms 后重试...")

                    App.mainHandler.postDelayed({
                        tryFetchVideoSizeAndAdjustLayout(retryCount + 1)
                    }, (retryCount + 1) * 200L)  // 渐进式延迟：200ms, 400ms, 600ms...
                } else {
                    log(TAG, "⚠️ 已重试 $maxRetries 次仍无法获取视频尺寸")
                }
            }

        } catch (_: Exception) {
            // 异常时也尝试重试
            if (retryCount < maxRetries) {
                log(TAG, "获取视频尺寸异常，重试中...")
                App.mainHandler.postDelayed({
                    tryFetchVideoSizeAndAdjustLayout(retryCount + 1)
                }, (retryCount + 1) * 200L)
            }
        }
    }

}