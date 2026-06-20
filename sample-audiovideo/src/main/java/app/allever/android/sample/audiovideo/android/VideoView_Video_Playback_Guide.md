# Android VideoView 视频播放完整指南

> **基于 `AndroidVideoViewPlayer` 封装类，详细描述 VideoView + MediaPlayer 播放视频的完整流程、API 使用及底层原理**

## 目录

- [1. 概述](#1-概述)
- [2. 核心类与接口](#2-核心类与接口)
  - [2.1 PlayerState 状态枚举](#21-playerstate-状态枚举)
  - [2.2 LoopMode 循环模式](#22-loopmode-循环模式)
  - [2.3 IVideoPlayerListener 监听接口](#23-ivideoplayerlistener-监听接口)
  - [2.4 AndroidVideoViewPlayer 封装类](#24-androidvideoviewplayer-封装类)
- [3. 状态机详解](#3-状态机详解)
  - [3.1 状态定义](#31-状态定义)
  - [3.2 状态转换图](#32-状态转换图)
  - [3.3 转换规则表](#33-转换规则表)
- [4. 完整播放流程](#4-完整播放流程)
  - [4.1 快速开始（6行代码）](#41-快速开始6行代码)
  - [4.2 完整生命周期流程](#42-完整生命周期流程)
  - [4.3 步骤一：创建实例并绑定 VideoView](#43-步骤一创建实例并绑定-videoview)
  - [4.4 步骤二：设置监听器](#44-步骤二设置监听器)
  - [4.5 步骤三：设置数据源](#45-步骤三设置数据源)
  - [4.6 步骤四：等待准备完成](#46-步骤四等待准备完成)
  - [4.7 步骤五：开始播放](#47-步骤五开始播放)
  - [4.8 步骤八：播放控制](#48-步骤八播放控制)
  - [4.9 步骤九：进度追踪](#49-步骤九进度追踪)
  - [4.10 步骤十：解绑与释放资源](#410-步骤十解绑与释放资源)
- [5. API 接口详解](#5-api-接口详解)
  - [5.1 绑定管理](#51-绑定管理)
  - [5.2 数据源设置](#52-数据源设置)
  - [5.3 播放控制](#53-播放控制)
  - [5.4 只读属性](#54-只读属性)
  - [5.5 可配置属性](#55-可配置属性)
- [6. 高级功能](#6-高级功能)
  - [6.1 变速播放](#61-变速播放)
  - [6.2 音量控制](#62-音量控制)
  - [6.3 循环模式](#63-循环模式)
  - [6.4 自动重试机制](#64-自动重试机制)
  - [6.5 SeekBar 拖动跳转](#65-seekbar-拖动跳转)
  - [6.6 视频尺寸自适应](#66-视频尺寸自适应)
- [7. 生命周期管理](#7-生命周期管理)
  - [7.1 Activity 中的正确使用](#71-activity-中的正确使用)
  - [7.2 Fragment 中的使用](#72-fragment-中的使用)
  - [7.3 注意事项](#73-注意事项)
- [8. 使用示例](#8-使用示例)
  - [8.1 示例一：简单视频播放器](#81-示例一简单视频播放器)
  - [8.2 示例二：带 UI 的视频播放器](#82-示例二带-ui-的视频播放器)
  - [8.3 示例三：全屏视频播放器](#83-示例三全屏视频播放器)
- [9. 常见问题与最佳实践](#9-常见问题与最佳实践)
- **[10. 纯 MediaPlayer + VideoView 原生 API 播放流程](#10-纯-mediaplayer--videoview-原生-api-播放流程)** ⭐
  - [10.1 快速开始](#101-快速开始)
  - [10.2 完整生命周期](#102-完整生命周期)
  - [10.3 步骤详解](#103-步骤详解)
  - [10.4 进度追踪实现](#104-进度追踪实现)
  - [10.5 监听器注册](#105-监听器注册)
  - [10.6 视频尺寸处理](#106-视频尺寸处理)
  - [10.7 资源释放（非常重要）](#107-资源释放非常重要)
  - [10.8 完整可运行示例](#108-完整可运行示例)
  - [10.9 与封装类对比](#109-与封装类对比)

---

## 1. 概述

VideoView 是 Android 提供的高层视频播放控件，内部封装了 MediaPlayer 和 SurfaceView。本文档基于项目中的 `AndroidVideoViewPlayer` 封装类，详细描述：

- **上层封装**：如何使用 `AndroidVideoViewPlayer` 进行视频播放（第 2~9 章）
- **底层原理**：直接使用原生 VideoView + MediaPlayer API 的完整流程（第 10 章）

### 封装类的核心特性

| 特性 | 说明 |
|------|------|
| 数据源支持 | HTTP/HTTPS URL、本地文件、Assets 目录、Content URI |
| 状态机管理 | 9 种状态的完整生命周期管理 |
| 进度追踪 | 基于 Coroutine 的定时回调（默认 200ms） |
| 变速播放 | 0.5x ~ 3.0x（API 23+） |
| 音量控制 | 0.0 ~ 1.0 |
| 循环模式 | 不循环 / 单曲循环 / 列表循环 |
| 自动重试 | 可配置的出错自动重试机制 |
| 线程安全 | 所有回调均在主线程触发 |
| VideoView 管理 | 支持绑定/解绑，灵活的生命周期管理 |
| Assets 支持 | 自动复制到缓存目录播放 |

### VideoView vs SurfaceView/TextureView

| 控件 | 复杂度 | 功能 | 适用场景 |
|------|--------|------|----------|
| **VideoView** | 低 | 基础播放 | 快速开发、简单需求 |
| **SurfaceView** | 中 | 高度自定义 | 需要自定义渲染 |
| **TextureView** | 高 | 完全控制 | 需要变换动画 |

---

## 2. 核心类与接口

### 2.1 PlayerState 状态枚举

```kotlin
enum class PlayerState {
    IDLE,           // 空闲 - 初始状态或 reset 后
    PREPARING,      // 准备中 - 正在加载媒体资源
    PREPARED,       // 准备就绪 - 可获取 duration，需调用 play()
    PLAYING,        // 播放中
    PAUSED,         // 已暂停
    STOPPED,        // 已停止 - 需重新 setSource
    COMPLETED,      // 播放完成
    ERROR,          // 出错
    RELEASED        // 已释放 - 终态，不可再使用
}
```

### 2.2 LoopMode 循环模式

```kotlin
enum class LoopMode {
    NONE,           // 不循环
    SINGLE,         // 单曲循环
    ALL             // 列表循环
}
```

### 2.3 IVideoPlayerListener 监听接口

```kotlin
interface IVideoPlayerListener : IPlayerListener {
    /** 视频尺寸变化 */
    fun onVideoSizeChanged(width: Int, height: Int) {}

    /** 播放器信息回调（如缓冲、渲染等），返回 true 表示已消费 */
    fun onInfo(what: Int, extra: Int): Boolean = false
}

// 继承自 IPlayerListener（音频基础回调）
interface IPlayerListener {
    /** 状态变化 */
    fun onStateChanged(from: PlayerState, to: PlayerState) {}

    /** 准备就绪（此时可获取 duration，需调用 play() 才会开始播放） */
    fun onPrepared(durationMs: Long) {}

    /** 进度更新（定时回调） */
    fun onProgress(currentMs: Long, durationMs: Long) {}

    /** 播放完成 */
    fun onComplete() {}

    /** 出错（返回 true 表示错误已被消费） */
    fun onError(what: Int, extra: Int): Boolean = false

    /** 缓冲进度 (0~100) */
    fun onBufferingUpdate(percent: Int) {}
}
```

**所有回调均在主线程触发。**

### 2.4 AndroidVideoViewPlayer 封装类

```
AndroidVideoViewPlayer
├── 成员变量
│   ├── videoView: VideoView?            // 外部传入的 VideoView
│   ├── mediaPlayer: MediaPlayer?        // 内部持有的 MediaPlayer 引用
│   ├── listener: IVideoPlayerListener?  // 事件监听器
│   ├── _state: PlayerState              // 当前状态（私有）
│   ├── progressJob: Job?                // 进度追踪协程
│   ├── currentUri: Uri?                 // 当前数据源 URI
│   ├── currentHeaders: Map<...>?        // HTTP 请求头
│   ├── retryLeft: Int                   // 剩余重试次数
│   ├── pendingSpeed: Float?             // 待应用的变速（MediaPlayer 未就绪时缓存）
│   └── pendingVolume: Float?            // 待应用的音量（MediaPlayer 未就绪时缓存）
│
├── 对外 API
│   ├── attach(videoView)                // 绑定 VideoView
│   ├── detach()                         // 解绑 VideoView
│   ├── setSource(url / uri / assetPath) // 设置数据源
│   ├── play() / pause() / stop()        // 播放控制
│   ├── seekTo(positionMs)              // 跳转位置
│   ├── setListener(listener?)           // 设置监听器
│   └── release()                        // 释放资源
│
├── 只读属性
│   ├── state: PlayerState               // 当前状态
│   ├── isPlaying: Boolean               // 是否正在播放
│   ├── currentPosition: Long            // 当前位置（毫秒）
│   └── duration: Long                   // 总时长（毫秒）
│
└── 可配置属性
    ├── loopMode: LoopMode               // 循环模式
    ├── progressIntervalMs: Int          // 进度回调间隔
    ├── retryCount: Int                  // 重试次数
    ├── speed: Float                     // 变速倍率
    └── volume: Float                    // 音量
```

**关键设计特点：**
- **VideoView 由外部传入**：本类不创建 UI 组件，支持灵活复用
- **attach/detach 模式**：支持页面切换时的解绑/重新绑定
- **缓存机制**：变速和音量在 MediaPlayer 未就绪时会缓存，prepared 后自动应用
- **Assets 文件处理**：自动复制到缓存目录后播放（VideoView 不直接支持 AssetFileDescriptor）

---

## 3. 状态机详解

### 3.1 状态定义

VideoView + MediaPlayer 有严格的 **状态转换规则**，错误的状态转换会抛出异常。

### 3.2 状态转换图

```
                         ┌─────────────────────────────────────┐
                         │                                     │
                         ▼                                     │
┌──────────┐  setSource()  ┌───────────┐  onPrepared()  ┌──────────┐
│   IDLE   ├──────────────►│ PREPARING  ├──────────────►│ PREPARED │
└────┬─────┘               └─────┬─────┘               └────┬─────┘
     │                           │                            │
     │                     onError()                      play()
     │                           ▼                            ▼
     │                     ┌──────────┐                ┌──────────┐
     └─────────────────────│  ERROR   │                │ PLAYING  │
                           └────┬─────┘                └────┬─────┘
                                │                          │
                                │                       pause()
                                │                          ▼
                                │                      ┌──────────┐
                                │                      │  PAUSED  │
                                │                      └────┬─────┘
                                │                          │
                                │                       play()
                                │                          │
                                │                  stop() / onComplete()
                                │                          ▼
                                │                  ┌───────────┐
                                │                  │ STOPPED / │
                                │                  │ COMPLETED │
                                │                  └─────┬─────┘
                                │                        │
                                │                 setSource()
                                │                        │
                                └────────────────────────┤
                                                         │
                                                  release()
                                                         ▼
                                                  ┌───────────┐
                                                  │ RELEASED  │
                                                  └───────────┘
```

### 3.3 转换规则表

| 当前状态 | 允许的操作 | 操作后状态 |
|---------|-----------|-----------|
| IDLE | attach() + setSource() | PREPARING |
| PREPARING | （等待中） | → PREPARED 或 ERROR |
| PREPARED | play() | PLAYING |
| PLAYING | pause() | PAUSED |
| PAUSED | play() | PLAYING |
| PLAYING/PAUSED/PREPARED/COMPLETED | stop() | STOPPED |
| STOPPED/COMPLETED/ERROR/IDLE | setSource() | PREPARING |
| 任意状态（除 RELEASED） | detach() | 保持当前状态（移除 VideoView 引用） |
| 任意状态（除 RELEASED） | release() | RELEASED |

---

## 4. 完整播放流程

### 4.1 快速开始（6行代码）

```kotlin
val player = AndroidVideoViewPlayer()

player.attach(videoView)  // 绑定 VideoView

player.setListener(object : IVideoPlayerListener {
    override fun onPrepared(durationMs: Long) {
        player.play()  // 准备完成后自动播放
    }
})

player.setSource("https://example.com/video.mp4")

// 不用时：
// player.release()
```

### 4.2 完整生命周期流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                   AndroidVideoViewPlayer 完整生命周期                │
└─────────────────────────────────────────────────────────────────────┘

① 创建实例
   val player = AndroidVideoViewPlayer()
   ↓ state = IDLE
   ↓ videoView = null

② 绑定 VideoView（必须！）
   player.attach(videoView)
   ↓ 内部执行：
     ├── this.videoView = videoView
     └── setupVideoViewListeners()  ← 注册 5 个监听器
   ↓ 此时可以调用 setSource()

③ 设置监听器（可选但推荐）
   player.setListener(listener)
   ↓

④ 设置数据源（自动进入准备阶段）
   player.setSource(url)
   ↓ 内部执行：
     ├── 根据 URI 类型判断是否为 assets 文件
     ├── 如果是 assets → copyAssetToCache() → setVideoPath(缓存文件路径)
     ├── 如果是 HTTP 带 headers → setVideoURI(uri, headers)
     └── 其他 → setVideoURI(uri)
   ↓ state = PREPARING

⑤ 等待准备完成（异步）
   回调 listener.onPrepared(durationMs)
   ↓ 同时获取 mediaPlayer 引用
   ↓ 应用缓存的 speed 和 volume 设置
   ↓ state = PREPARED
   此时可以获取 player.duration 和视频尺寸

⑥ 开始播放
   player.play()
   ↓ 内部执行：
     ├── videoView.start()
     ├── state = PLAYING
     └── startProgressTracking()  ← 启动协程定时回调

⑦ 播放中（定时回调）
   listener.onProgress(currentMs, durationMs)
   每 progressIntervalMs 回调一次（默认 200ms）

⑧ 暂停 / 恢复
   player.pause()  → state = PAUSED，停止进度追踪
   player.play()   → state = PLAYING，恢复进度追踪

⑨ 播放完成
   回调 listener.onComplete()
   ↓ 根据 loopMode 处理：
     ├── NONE/ALL  → state = COMPLETED
     └── SINGLE    → seekTo(0) + start()，自动重播

⑩ 页面不可见时（可选）
   player.detach()
   ↓ 内部执行：
     ├── stopProgressTracking()
     ├── removeVideoViewListeners()
     └── videoView = null
   ⚠️ 不释放内部资源，可通过 attach() 重新绑定

⑪ 重新进入页面（如果之前 detach 了）
   player.attach(newVideoView)
   ↓ setupVideoViewListeners()
   ↓ 可以继续操作

⑫ 停止（可选）
   player.stop()
   ↓ 内部执行：
     ├── stopProgressTracking()
     ├── videoView.stopPlayback()
     ├── state = STOPPED
     └── mediaPlayer = null
   ⚠️ 需要重新 setSource 才能再次播放

⑬ 释放资源（必须！）
   player.release()
   ↓ 内部执行：
     ├── detach()  ← 解绑 VideoView
     ├── videoView?.stopPlayback()
     ├── 清空所有引用
     └── state = RELEASED（终态）
```

### 4.3 步骤一：创建实例并绑定 VideoView

```kotlin
// 创建播放器实例
val player = AndroidVideoViewPlayer()
// 此时 state = PlayerState.IDLE
// videoView = null
// mediaPlayer = null

// 绑定 VideoView（必须在 setSource 之前调用！）
player.attach(binding.videoView)
// 此时 videoView 已绑定
// 已注册 OnPreparedListener、OnCompletionListener、OnErrorListener、OnInfoListener、OnLayoutChangeListener
```

**为什么需要先 attach？**
- VideoView 是外部创建的 UI 组件
- 本类不负责创建 VideoView，只负责管理其生命周期
- 必须先绑定才能设置数据源和监听器

**XML 布局示例：**

```xml
<VideoView
    android:id="@+id/videoView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

### 4.4 步骤二：设置监听器

```kotlin
player.setListener(object : IVideoPlayerListener {
    override fun onStateChanged(from: PlayerState, to: PlayerState) {
        Log.d("VideoPlayer", "状态变化: $from -> $to")
        
        // 更新 UI 状态显示
        when (to) {
            PlayerState.PREPARING -> showLoading(true)
            PlayerState.PLAYING -> updatePlayButton(isPlaying = true)
            PlayerState.PAUSED -> updatePlayButton(isPlaying = false)
            PlayerState.ERROR -> showErrorToast()
            else -> {}
        }
    }

    override fun onPrepared(durationMs: Long) {
        Log.d("VideoPlayer", "准备就绪, 时长=${durationMs}ms")
        hideLoading()
        updateDurationDisplay(durationMs)
        player.play()  // 准备完成后自动播放
    }

    override fun onProgress(currentMs: Long, durationMs: Long) {
        // 更新进度条和时间显示
        if (!isUserSeeking && durationMs > 0) {
            val progress = (currentMs.toFloat() / durationMs * 100).toInt()
            seekBar.progress = progress
            tvCurrentTime.text = formatTime(currentMs)
            tvTotalTime.text = formatTime(durationMs)
        }
    }

    override fun onComplete() {
        Log.d("VideoPlayer", "播放完成")
        updatePlayButton(isPlaying = false)
        // 可以自动播放下一个视频
    }

    override fun onError(what: Int, extra: Int): Boolean {
        Log.e("VideoPlayer", "错误: what=$what, extra=$extra")
        showErrorDialog("播放失败")
        return true  // 返回 true 表示已处理错误
    }

    override fun onBufferingUpdate(percent: Int) {
        // 更新缓冲进度条（二级进度）
        seekBar.secondaryProgress = percent
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        Log.d("VideoPlayer", "视频尺寸: ${width}x${height}")
        // 调整 VideoView 或容器布局以适应视频比例
        adjustVideoAspectRatio(width, height)
    }

    override fun onInfo(what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> showLoading(true)
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> showLoading(false)
            MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> Log.w("VideoPlayer", "视频帧滞后")
        }
        return false
    }
})
```

### 4.5 步骤三：设置数据源

支持多种方式：

#### 方式 A：URL 字符串

```kotlin
// 网络 URL
player.setSource("https://example.com/video.mp4")

// 本地文件
player.setSource("file:///sdcard/Movies/movie.mp4")

// Content URI
player.setSource("content://media/external/video/media/123")

// Assets 文件（通过 file 协议）
player.setSource("file:///android_asset/video/intro.mp4")
```

#### 方式 B：URI 对象 + 自定义 Headers

```kotlin
val headers = mapOf(
    "User-Agent" to "MyVideoPlayer",
    "Authorization" to "Bearer token123"
)
player.setSource(Uri.parse("https://example.com/video.mp4"), headers)
```

#### 方式 C：Assets 文件（推荐方式）

```kotlin
player.setAssetSource("video/intro.mp4")  // 相对于 assets 目录的路径
// 内部会自动复制到 cacheDir/video_cache/ 目录
```

**内部处理流程 (`doPrepare()`)**：

```
doPrepare()
│
├── 1. 检查 VideoView 是否已绑定
│   ├── 如果未绑定 → 记录日志并返回
│   └── 否则继续
│
├── 2. 根据 URI 类型处理
│   ├── Assets 文件:
│   │   ├── copyAssetToCache(assetPath)
│   │   │   ├── 创建 cacheDir/video_cache/ 目录
│   │   │   ├── 从 assets 复制文件到缓存
│   │   │   └── 返回缓存文件的 File 对象
│   │   └── videoView.setVideoPath(cacheFile.absolutePath)
│   │
│   ├── HTTP/HTTPS URL（带 headers）:
│   │   └── videoView.setVideoURI(uri, HashMap(headers))
│   │
│   └── 其他（本地文件/Content URI）:
│       └── videoView.setVideoURI(uri)
│
├── 3. 状态变为 PREPARING
│
└── 4. 异常处理
    └── handlePrepareError(e)
        ├── 状态变为 ERROR
        ├── 回调 listener.onError(-1, 0)
        └── 如果有剩余重试次数 → 500ms 后自动重试
```

**Assets 文件的特殊处理：**

VideoView **不支持直接播放 assets 目录下的文件**，因为 VideoView 内部使用的 MediaPlayer 无法直接访问 AssetFileDescriptor。解决方案：

```kotlin
private fun copyAssetToCache(assetPath: String): File {
    val cacheDir = File(App.context.cacheDir, "video_cache")
    if (!cacheDir.exists()) cacheDir.mkdirs()
    
    val outFile = File(cacheDir, assetPath.substringAfterLast("/"))
    if (outFile.exists()) return outFile  // 已存在则跳过
    
    App.context.assets.open(assetPath).use { input ->
        FileOutputStream(outFile).use { output ->
            input.copyTo(output)
        }
    }
    return outFile
}
```

### 4.6 步骤四：等待准备完成

**OnPreparedListener 回调处理：**

```kotlin
setupVideoViewListeners() {
    videoView?.setOnPreparedListener { mp ->
        mediaPlayer = mp  // 保存 MediaPlayer 引用（用于后续操作）
        _state = PlayerState.PREPARED
        
        // 应用之前缓存的变速和音量设置
        pendingSpeed?.let { applySpeed(); pendingSpeed = null }
        pendingVolume?.let {
            mp.setVolume(it, it)
            pendingVolume = null
        }
        
        listener?.onPrepared(mp.duration.toLong())
    }
}
```

**此时可以：**
- 获取总时长：`player.duration`
- 通过 `mediaPlayer` 获取视频宽高：`mediaPlayer.videoWidth`, `mediaPlayer.videoHeight`
- 调用 `player.play()` 开始播放
- 调用 `player.seekTo(position)` 跳转位置
- 设置音量：`player.volume = 0.8f`
- 设置速度：`player.speed = 1.5f`

### 4.7 步骤五：开始播放

```kotlin
player.play()
```

**内部实现：**

```kotlin
fun play() {
    when (_state) {
        PlayerState.PREPARED, PlayerState.COMPLETED -> {
            videoView?.start()
            _state = PlayerState.PLAYING
            startProgressTracking()  // 启动进度追踪协程
        }
        PlayerState.PAUSED -> {
            videoView?.start()     // 从暂停恢复
            _state = PlayerState.PLAYING
            startProgressTracking()
        }
        else -> {}  // 其他状态忽略操作
    }
}
```

**有效的前置状态：**
- `PREPARED` - 首次播放
- `PAUSED` - 从暂停恢复
- `COMPLETED` - 重新播放（如非单曲循环模式）

### 4.8 步骤八：播放控制

#### 暂停

```kotlin
player.pause()
// 仅在 PLAYING 状态有效
// → state = PAUSED, 停止进度追踪
```

#### 停止

```kotlin
player.stop()
// 在 PLAYING/PAUSED/PREPARED/COMPLETED 状态均可调用
// → state = STOPPED
// → mediaPlayer = null（清除引用）
// ⚠️ 注意：停止后必须重新 setSource() 才能再次播放
```

#### 跳转到指定位置

```kotlin
player.seekTo(30000)  // 跳转到 30 秒处
// 单位：毫秒
// 在非 IDLE/RELEASED 状态均可调用
```

### 4.9 步骤九：进度追踪

**启动机制（Coroutine 实现）：**

```kotlin
private fun startProgressTracking() {
    stopProgressTracking()  // 先停止旧的
    
    progressJob = CoroutineScope(Dispatchers.Main).launch {
        while (isActive && _state == PlayerState.PLAYING) {
            val pos = try { videoView?.currentPosition?.toLong() ?: 0L } catch (_: Exception) { 0L }
            val dur = try { videoView?.duration?.toLong() ?: 0L } catch (_: Exception) { 0L }
            listener?.onProgress(pos, dur)  // 回调给外部
            delay(progressIntervalMs.toLong())  // 默认 200ms
        }
    }
}
```

**特点：**
- 在主线程运行（`Dispatchers.Main`），可直接更新 UI
- 定时器间隔可配置（`progressIntervalMs`）
- 自动在非 `PLAYING` 状态停止
- 通过 `Job.cancel()` 安全取消
- **通过 VideoView 获取当前位置和时长**（而非直接访问 MediaPlayer）

### 4.10 步骤十：解绑与释放资源

#### 解绑 VideoView（页面不可见时）

```kotlin
player.detach()
```

**内部实现：**

```kotlin
fun detach() {
    stopProgressTracking()
    removeVideoViewListeners()  // 移除所有 VideoView 监听器
    videoView = null             // 清空引用
    // 注意：不解绑时 VideoView 可能仍在后台占用资源
    // 如果需要完全停止应先调 stop()
}
```

**使用场景：**
- Fragment 的 `onDestroyView()` 时
- Activity 切换到其他页面但可能返回时
- ViewPager 滑出屏幕时

**解绑后可以通过 `attach()` 重新绑定继续使用。**

#### 完全释放资源

```kotlin
player.release()
```

**释放流程：**

```kotlin
fun release() {
    // 1. 先解绑 VideoView
    detach()
    
    try {
        // 2. 停止 VideoView 播放
        videoView?.stopPlayback()
    } catch (_: Exception) {}  // 忽略异常
    
    // 3. 清空所有引用
    videoView = null
    mediaPlayer = null
    currentUri = null
    currentHeaders = null
    
    // 4. 更新状态为终态
    _state = PlayerState.RELEASED  // 不可再使用
}
```

**为什么必须释放？**

| 不释放的后果 | 说明 |
|-------------|------|
| 内存泄漏 | VideoView 和 MediaPlayer 持有 Context 引用 |
| CPU 浪费 | 后台解码线程仍在运行 |
| 电池消耗 | 持续消耗电量 |
| 音频冲突 | 可能导致其他应用无法播放音频 |
| 系统崩溃 | 严重时可能导致 ANR |

**最佳实践：**

```kotlin
class VideoFragment : Fragment() {

    private var player: AndroidVideoViewPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        player = AndroidVideoViewPlayer().apply {
            attach(binding.videoView)      // 绑定 VideoView
            setListener(playerListener)    // 设置监听器
            setSource("https://example.com/video.mp4")  // 开始加载
        }
    }

    override fun onPause() {
        super.onPause()
        // 页面不可见时暂停或解绑
        player?.pause()
        // 或者：player?.detach()  // 完全解绑
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Fragment 销毁视图时解绑
        player?.detach()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 彻底销毁时释放所有资源
        player?.release()
        player = null
    }
}
```

---

## 5. API 接口详解

### 5.1 绑定管理

| 方法 | 参数 | 说明 | 调用时机 |
|------|------|------|----------|
| `attach(videoView)` | VideoView 实例 | 绑定 VideoView 并注册监听器 | setSource() 之前 |
| `detach()` | 无 | 解绑 VideoView，移除监听器 | 页面不可见时 |
| `release()` | 无 | 完全释放所有资源 | 不再使用时 |

**attach/detach 的典型用法：**

```kotlin
// 场景 1：Activity 重建（如屏幕旋转）
override fun onCreate(savedInstanceState: Bundle?) {
    player.attach(findViewById(R.id.videoView))
}

override fun onDestroy() {
    if (!isChangingConfigurations) {
        player.release()  // 屏幕旋转时不释放
    }
}

// 场景 2：ViewPager 中的 Fragment
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    player?.attach(view.findViewById(R.id.videoView))
}

override fun onDestroyView() {
    player?.detach()  // 只解绑，不释放
}
```

### 5.2 数据源设置

| 方法 | 参数 | 说明 | 有效状态 |
|------|------|------|----------|
| `setSource(url: String)` | URL 字符串 | 支持 http/https/file/content/android_asset | 非 RELEASED 且已 attach |
| `setSource(uri: Uri, headers?)` | URI 对象 + 可选 Headers | 支持自定义 HTTP 请求头 | 同上 |
| `setAssetSource(path: String)` | Assets 相对路径 | 如 `"video/test.mp4"` | 同上 |

### 5.3 播放控制

| 方法 | 说明 | 有效状态 |
|------|------|----------|
| `play()` | 开始或恢复播放 | PREPARED, COMPLETED, PAUSED |
| `pause()` | 暂停 | PLAYING |
| `stop()` | 停止（同时清空 mediaPlayer 引用） | PLAYING, PAUSED, PREPARED, COMPLETED |
| `seekTo(positionMs: Long)` | 跳转位置 | 非 IDLE, RELEASED |
| `setListener(listener?)` | 设置事件监听器 | 任意 |
| `release()` | 释放资源 | 任意 |

### 5.4 只读属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `state` | `PlayerState` | 当前状态 |
| `isPlaying` | `Boolean` | 是否正在播放（state == PLAYING && videoView.isPlaying） |
| `currentPosition` | `Long` | 当前播放位置（毫秒），异常时返回 0 |
| `duration` | `Long` | 总时长（毫秒），PREPARED 后可用，异常时返回 0 |

### 5.5 可配置属性

| 属性 | 类型 | 默认值 | 范围 | 说明 |
|------|------|--------|------|------|
| `loopMode` | `LoopMode` | `NONE` | 枚举 | 循环模式 |
| `progressIntervalMs` | `Int` | `200` | > 0 | 进度回调间隔（毫秒） |
| `retryCount` | `Int` | `0` | >= 0 | 出错自动重试次数 |
| `speed` | `Float` | `1.0f` | 0.5~3.0 | 变速倍率（prepared 后生效） |
| `volume` | `Float` | `1.0f` | 0.0~1.0 | 音量（左右声道相同，prepared 后生效） |

**注意：** `speed` 和 `volume` 在 MediaPlayer 未就绪时会缓存，prepared 后自动应用。

---

## 6. 高级功能

### 6.1 变速播放

```kotlin
// 设置 1.5 倍速
player.speed = 1.5f

// 内部实现（带缓存机制）
private fun applySpeed() {
    val mp = mediaPlayer
    if (mp != null) {
        // MediaPlayer 已就绪，直接应用
        try {
            mp.playbackParams = PlaybackParams().apply { 
                speed = this@AndroidVideoViewPlayer.speed 
            }
        } catch (e: Exception) {
            log("VideoPlayer", "setSpeed error: ${e.message}")
        }
    } else {
        // MediaPlayer 还未就绪，缓存待应用
        pendingSpeed = speed
    }
}
```

**限制：**
- 需要 API >= 23 (Android 6.0)
- 支持 0.25x ~ 4x（本封装限制为 0.5x ~ 3.0x）
- 某些编解码器可能不支持特定速率
- **必须在 prepared 之后才能生效**（或在 prepared 之前设置会被缓存）

### 6.2 音量控制

```kotlin
// 设置音量为 50%
player.volume = 0.5f

// 内部实现（同样带缓存机制）
var volume: Float = 1.0f
    set(value) {
        field = value.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(field, field)  // 左右声道相同
        // 如果 mediaPlayer 为 null，会在 prepared 回调中应用
    }
```

### 6.3 循环模式

```kotlin
// 不循环（默认）
player.loopMode = LoopMode.NONE

// 单曲循环
player.loopMode = LoopMode.SINGLE
// 实现：onCompletion 时 seekTo(0) + start()

// 列表循环
player.loopMode = LoopMode.ALL
// 实现：onCompletion 时通知上层切换下一首
```

### 6.4 自动重试机制

```kotlin
// 设置出错时自动重试 3 次
player.retryCount = 3

// 内部逻辑（与 AudioPlayer 类似）
setOnErrorListener { _, what, extra ->
    _state = PlayerState.ERROR
    val handled = listener?.onError(what, extra) ?: false
    if (!handled && retryLeft > 0) {
        retryLeft--                              // 减少剩余次数
        postDelayed({ doPrepare() }, 500)        // 500ms 后重试
        true  // 已通过重试处理错误
    } else {
        handled  // 无重试次数或外部已处理
    }
}
```

### 6.5 SeekBar 拖动跳转

```kotlin
// 用户拖动 SeekBar 时
seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
    private var isUserSeeking = false

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        isUserSeeking = true  // 标记用户正在拖动
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (seekBar != null && player.duration > 0) {
            val position = (seekBar.progress.toFloat() / 100 * player.duration).toLong()
            player.seekTo(position)  // 跳转到目标位置
        }
        seekBar?.post { isUserSeeking = false }  // 延迟解除标记
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            // 实时显示预览位置
            val position = (progress.toFloat() / 100 * player.duration).toLong()
            tvCurrentTime.text = formatTime(position)
        }
    }
})

// 在 onProgress 回调中检查
override fun onProgress(currentMs: Long, durationMs: Long) {
    if (!isUserSeeking && durationMs > 0) {
        // 只有非用户拖动时才更新进度条
        val progress = (currentMs.toFloat() / durationMs * 100).toInt()
        seekBar.progress = progress
    }
}
```

### 6.6 视频尺寸自适应

**获取视频尺寸：**

```kotlin
override fun onVideoSizeChanged(width: Int, height: Int) {
    Log.d("VideoPlayer", "视频原始尺寸: ${width}x${height}")
    
    // 计算适配比例
    val containerWidth = binding.container.width
    val ratio = width.toFloat() / height.toFloat()
    val adaptedHeight = (containerWidth / ratio).toInt()
    
    // 调整 VideoView 高度
    val params = binding.videoView.layoutParams
    params.height = adaptedHeight
    binding.videoView.layoutParams = params
}
```

**保持视频比例：**

```xml
<!-- 方式 1：使用 ConstraintLayout -->
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <VideoView
        android:id="@+id/videoView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintDimensionRatio="${videoWidth}:${videoHeight}"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>

<!-- 方式 2：动态调整 -->
<FrameLayout
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <VideoView
        android:id="@+id/videoView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center" />

</FrameLayout>
```

---

## 7. 生命周期管理

### 7.1 Activity 中的正确使用

```kotlin
class VideoActivity : AppCompatActivity() {

    private lateinit var player: AndroidVideoViewPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        // 1. 创建播放器并绑定 VideoView
        player = AndroidVideoViewPlayer()
        player.attach(findViewById(R.id.videoView))
        player.setListener(playerListener)

        // 2. 设置数据源（自动进入准备阶段）
        player.setSource("https://example.com/video.mp4")
    }

    override fun onResume() {
        super.onResume()
        // 3. 恢复播放（如果之前在播放）
        // VideoView 会自动恢复，无需额外操作
    }

    override fun onPause() {
        super.onPause()
        // 4. 可选：暂停播放
        if (player.state == PlayerState.PLAYING) {
            player.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        // 5. 可选：完全解绑（节省资源）
        // player.detach()
    }

    override fun onRestart() {
        super.onRestart()
        // 6. 如果之前 detach 了，重新绑定
        // player.attach(findViewById(R.id.videoView))
    }

    override fun onDestroy() {
        super.onDestroy()
        // 7. 必须释放资源
        if (!isChangingConfigurations) {
            player.release()  // 屏幕旋转时不释放
        }
    }
}
```

### 7.2 Fragment 中的使用

```kotlin
class VideoFragment : Fragment() {

    private var player: AndroidVideoViewPlayer? = null
    private var isUserVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initPlayer(view)
    }

    private fun initPlayer(view: View) {
        player = AndroidVideoViewPlayer().apply {
            attach(view.findViewById(R.id.videoView))
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }
    }

    override fun onResume() {
        super.onResume()
        isUserVisible = true
        
        // 如果有未完成的任务，恢复播放
        player?.apply {
            if (state == PlayerState.PAUSED) {
                play()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isUserVisible = false
        
        // 用户离开时暂停
        player?.apply {
            if (state == PlayerState.PLAYING) {
                pause()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Fragment 销毁视图时解绑（不释放）
        player?.detach()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 彻底销毁时释放
        player?.release()
        player = null
    }
}
```

### 7.3 注意事项

1. **必须先 attach 再 setSource**：否则 doPrepare() 会因 videoView 为 null 而失败
2. **必须释放资源**：忘记调用 `release()` 会导致内存泄漏
3. **attach/detach 配对使用**：避免重复 attach 导致监听器重复注册
4. **避免重复创建**：在 Activity/Fragment 重建时检查是否需要释放旧实例
5. **线程安全**：所有回调都在主线程，可直接更新 UI
6. **异常捕获**：内部已对大部分操作进行 try-catch 保护
7. **配置更改**：使用 `isChangingConfigurations` 判断是否需要释放
8. **Assets 文件大小限制**：大文件复制到缓存可能耗时，建议异步处理或提示用户

---

## 8. 使用示例

### 8.1 示例一：简单视频播放器

```kotlin
class SimpleVideoPlayer(private val context: Context) {

    private val player = AndroidVideoViewPlayer()

    fun setup(videoView: VideoView, url: String) {
        player.apply {
            attach(videoView)
            setListener(simpleListener)
            loopMode = LoopMode.SINGLE  // 单曲循环
            volume = 0.8f               // 80% 音量
            retryCount = 3              // 失败重试 3 次
            
            // 开始播放
            setSource(url)
        }
    }

    private val simpleListener = object : IVideoPlayerListener {
        override fun onPrepared(durationMs: Long) {
            println("Duration: ${durationMs}ms")
            player.play()  // 准备完成后自动播放
        }

        override fun onComplete() {
            println("Playback completed")
        }

        override fun onError(what: Int, extra: Int): Boolean {
            println("Error: $what, $extra")
            return true  // 已处理错误
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            println("Video size: ${width}x${height}")
        }
    }

    fun play() = player.play()
    fun pause() = player.pause()
    fun stop() = player.stop()
    fun release() = player.release()
}
```

### 8.2 示例二：带 UI 的视频播放器

```kotlin
class VideoPlayerFragment : Fragment() {

    private lateinit var binding: FragmentVideoBinding
    private lateinit var player: AndroidVideoViewPlayer
    private var isUserSeeking = false

    override fun onCreateView(...): View {
        binding = FragmentVideoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initPlayer()
        initUI()
    }

    private fun initPlayer() {
        player = AndroidVideoViewPlayer().apply {
            attach(binding.videoView)
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }
    }

    private fun initUI() {
        // 播放按钮
        binding.btnPlay.setOnClickListener {
            when (player.state) {
                PlayerState.PAUSED -> player.play()
                else -> {
                    val url = binding.etUrl.text.toString()
                    if (url.isNotEmpty()) {
                        player.setSource(url)
                    }
                }
            }
        }

        // 暂停按钮
        binding.btnPause.setOnClickListener { player.pause() }

        // 停止按钮
        binding.btnStop.setOnClickListener {
            player.stop()
            resetUI()
        }

        // 全屏按钮
        binding.btnFullscreen.setOnClickListener {
            toggleFullscreen()
        }

        // 进度条
        binding.seekBar.setOnSeekBarChangeListener(seekBarListener)

        // 变速
        binding.seekSpeed.setOnSeekBarChangeListener { _, progress, fromUser ->
            val speed = 0.5f + (progress.toFloat() / 50 * 2.5f)
            binding.tvSpeed.text = "%.1fx".format(speed)
            if (fromUser) player.speed = speed
        }

        // 音量
        binding.seekVolume.setOnSeekBarChangeListener { _, progress, fromUser ->
            val volume = progress.toFloat() / 100
            binding.tvVolume.text = "${progress}%"
            if (fromUser) player.volume = volume
        }
    }

    private val playerListener = object : IVideoPlayerListener {
        override fun onStateChanged(from: PlayerState, to: PlayerState) {
            updateUI()
            
            // 显示/隐藏加载指示器
            when (to) {
                PlayerState.PREPARING -> showLoading(true)
                PlayerState.PREPARED,
                PlayerState.PLAYING,
                PlayerState.COMPLETED -> showLoading(false)
                PlayerState.ERROR -> {
                    showLoading(false)
                    showToast("播放失败")
                }
                else -> {}
            }
        }

        override fun onPrepared(durationMs: Long) {
            binding.tvDuration.text = formatTime(durationMs)
            player.play()
        }

        override fun onProgress(currentMs: Long, durationMs: Long) {
            if (!isUserSeeking && durationMs > 0) {
                binding.seekBar.progress = (currentMs.toFloat() / durationMs * 100).toInt()
                binding.tvCurrent.text = formatTime(currentMs)
                binding.tvTotal.text = formatTime(durationMs)
            }
        }

        override fun onComplete() {
            updateUI()
        }

        override fun onError(what: Int, extra: Int): Boolean {
            Toast.makeText(context, "播放错误", Toast.LENGTH_SHORT).show()
            return true
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            adjustVideoSize(width, height)
        }

        override fun onInfo(what: Int, extra: Int): Boolean {
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> showLoading(true)
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> showLoading(false)
            }
            return false
        }
    }

    private fun adjustVideoSize(videoWidth: Int, videoHeight: Int) {
        val containerWidth = binding.container.width
        if (containerWidth > 0 && videoWidth > 0 && videoHeight > 0) {
            val ratio = videoWidth.toFloat() / videoHeight.toFloat()
            val adaptedHeight = (containerWidth / ratio).toInt()
            
            val params = binding.videoView.layoutParams
            params.height = adaptedHeight
            binding.videoView.layoutParams = params
        }
    }

    private fun toggleFullscreen() {
        // TODO: 实现全屏逻辑
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.detach()  // 解绑，不释放
    }

    companion object {
        fun formatTime(ms: Long): String {
            val totalSeconds = ms / 1000
            return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        }
    }
}
```

### 8.3 示例三：全屏视频播放器

```kotlin
class FullscreenVideoActivity : AppCompatActivity() {

    private lateinit var player: AndroidVideoViewPlayer
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_video)

        setupPlayer()
        setupUI()
        handleIntent(intent)
    }

    private fun setupPlayer() {
        player = AndroidVideoViewPlayer().apply {
            attach(findViewById(R.id.fullscreenVideoView))
            setListener(fullscreenListener)
            loopMode = LoopMode.NONE
        }
    }

    private fun setupUI() {
        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            if (isFullscreen) exitFullscreen() else finish()
        }

        findViewById<ImageButton>(R.id.btnToggleFullscreen).setOnClickListener {
            if (isFullscreen) exitFullscreen() else enterFullscreen()
        }

        // 点击视频区域显示/隐藏控制栏
        findViewById<VideoView>(R.id.fullscreenVideoView).setOnClickListener {
            toggleControlsVisibility()
        }
    }

    private fun handleIntent(intent: Intent?) {
        val url = intent?.getStringExtra(EXTRA_VIDEO_URL)
        if (!url.isNullOrEmpty()) {
            player.setSource(url)
        }
    }

    private fun enterFullscreen() {
        isFullscreen = true
        
        // 隐藏系统 UI
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        // 切换到横屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // 显示退出全屏按钮
        findViewById<ImageButton>(R.id.btnClose).visibility = View.VISIBLE
    }

    private fun exitFullscreen() {
        isFullscreen = false
        
        // 显示系统 UI
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        
        // 切换回竖屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        // 隐藏退出全屏按钮
        findViewById<ImageButton>(R.id.btnClose).visibility = View.GONE
    }

    private val fullscreenListener = object : IVideoPlayerListener {
        override fun onPrepared(durationMs: Long) {
            player.play()
        }

        override fun onComplete() {
            exitFullscreen()
            finish()
        }

        override fun onError(what: Int, extra: Int): Boolean {
            Toast.makeText(this@FullscreenVideoActivity, "播放失败", Toast.LENGTH_SHORT).show()
            return true
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            Log.d("FullscreenPlayer", "Video: ${width}x${height}")
        }
    }

    override fun onBackPressed() {
        if (isFullscreen) {
            exitFullscreen()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    companion object {
        const val EXTRA_VIDEO_URL = "video_url"
    }
}
```

---

## 9. 常见问题与最佳实践

### Q1: 为什么 VideoView 不能直接播放 assets 文件？

**A:** VideoView 内部使用 MediaPlayer，而 MediaPlayer 的 `setDataSource()` 不支持 AssetFileDescriptor。解决方案是先将 assets 文件复制到内部存储（cacheDir），然后播放缓存文件。

```kotlin
// 封装类已内置此功能
player.setAssetSource("video/intro.mp4")  // 自动复制到缓存
```

### Q2: 如何处理视频缓冲？

**A:** 使用 `IVideoPlayerListener.onInfo()` 监听缓冲状态：

```kotlin
override fun onInfo(what: Int, extra: Int): Boolean {
    when (what) {
        MediaPlayer.MEDIA_INFO_BUFFERING_START -> showLoading(true)
        MediaPlayer.MEDIA_INFO_BUFFERING_END -> showLoading(false)
    }
    return false
}
```

### Q3: 如何获取视频的实际宽高？

**A:** 在 `onVideoSizeChanged()` 回调中获取：

```kotlin
override fun onVideoSizeChanged(width: Int, height: Int) {
    Log.d("Player", "视频尺寸: ${width}x${height}")
    // 调整布局以适应视频比例
}
```

**注意：** 此回调可能在 `onPrepared()` 之后触发。

### Q4: attach 和 detach 的区别？

**A:** 

| 操作 | 作用 | 是否保留内部状态 |
|------|------|------------------|
| `attach(videoView)` | 绑定 VideoView，注册监听器 | ✅ 保留 |
| `detach()` | 解绑 VideoView，移除监听器 | ✅ 保留（可重新 attach） |
| `stop()` | 停止播放，清空 mediaPlayer 引用 | ❌ 需重新 setSource |
| `release()` | 完全释放所有资源 | ❌ 不可再用 |

### Q5: 如何避免内存泄漏？

**A:** 必须在合适的时机释放资源：

```kotlin
// ✅ 正确：在 onDestroy 中释放
override fun onDestroy() {
    super.onDestroy()
    player.release()  // 移除所有监听器并释放 VideoView 和 MediaPlayer
}

// ❌ 错误：忘记释放
// 会导致 VideoView 和 MediaPlayer 实例泄漏
```

### Q6: 变速播放有什么限制？

**A:** 
- 最低 API 23 (Android 6.0)
- 支持 0.25x ~ 4x（本封装限制为 0.5x ~ 3.0x）
- **必须在 prepared 之后才能生效**（或在 prepared 之前设置会被缓存）
- 某些编解码器可能不支持特定速率

### Q7: 如何实现画中画（PiP）效果？

**A:** VideoView 本身不支持 PiP，需要使用 TextureView + MediaPlayer 或 Media3 库。如果需要 PiP 功能，建议迁移到 ExoPlayer。

### Q8: VideoView 与 SurfaceView/TextureView 如何选择？

**A:** 

| 控件 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **VideoView** | 简单易用、API 简洁 | 功能有限、不够灵活 | 快速原型、简单需求 |
| **SurfaceView** | 性能好、独立窗口 | 不能做动画变换 | 性能敏感场景 |
| **TextureView** | 可做动画变换、可移动 | 性能稍差 | 需要特效、列表视频 |

---

## 10. 纯 MediaPlayer + VideoView 原生 API 播放流程

> **本章展示如何直接使用 Android 原生 `VideoView` + `MediaPlayer` API 播放视频，**  
> **不依赖任何封装类，帮助你理解底层实现原理。**

### 10.1 快速开始

最简单的纯 VideoView 视频播放（5 行核心代码）：

```kotlin
// 1. 获取 VideoView（通常在 XML 中定义）
val videoView = findViewById<VideoView>(R.id.videoView)

// 2. 设置视频路径
videoView.setVideoPath("https://example.com/video.mp4")

// 3. 准备完成后播放
videoView.setOnPreparedListener { it.start() }

// 4. 播放完成后处理
videoView.setOnCompletionListener {
    Log.d("VideoView", "Playback completed!")
}

// 5. 不用时释放
// videoView.stopPlayback()
```

### 10.2 完整生命周期

```
┌─────────────────────────────────────────────────────────────────────┐
│                VideoView + MediaPlayer 原生 API 完整流程             │
└─────────────────────────────────────────────────────────────────────┘

                    ┌──────────┐
                    │  获取或创建 │  VideoView
                    │  VideoView  │
                    └────┬─────┘
                         │
                         ▼
              ┌─────────────────────┐
              │    设置视频路径/URI   │  setVideoPath() / setVideoURI()
              │  (URL/File/Content)  │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │   内部自动准备        │  VideoView 内部调用 prepareAsync()
              │   (状态: PREPARING)  │
              └──────────┬──────────┘
                         │
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
     ┌──────────────┐      ┌──────────────┐
     │ 准备成功       │      │ 准备失败       │
     │ onPrepared()  │      │ onError()    │
     │ (PREPARED)    │      │ (ERROR)      │
     └──────┬───────┘      └──────┬───────┘
            │                     │
            ▼                     ▼
     ┌──────────────┐      ┌──────────────┐
     │  start()     │      │ stopPlayback()│
     │  (PLAYING)   │      │ 或重试        │
     └──────┬───────┘      └──────────────┘
            │
     ┌──────┴───────┐
     │              │
     ▼              ▼
┌─────────┐   ┌──────────┐
│ pause() │   │ 播放完成  │
│(PAUSED) │   │onComplete│
└────┬────┘   │(STOPPED) │
     │        └────┬─────┘
     │             │
     ▼             │
┌─────────┐        │
│ start() │        │
│(恢复)   │        │
└────┬────┘        │
     │             │
     └──────┬──────┘
            │
            ▼
     ┌──────────────┐
     │ stopPlayback()│
     │  (RELEASED)   │
     └──────────────┘
```

### 10.3 步骤详解

#### 1. 创建或获取 VideoView

##### 方式 A：XML 布局定义（推荐）

```xml
<!-- activity_video.xml -->
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <VideoView
        android:id="@+id/videoView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true" />

    <!-- 加载指示器 -->
    <ProgressBar
        android:id="@+id/loadingIndicator"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:visibility="gone" />

</RelativeLayout>
```

##### 方式 B：代码动态创建

```kotlin
val videoView = VideoView(this).apply {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
container.addView(videoView)
```

**说明：**
- VideoView 内部封装了 MediaPlayer 和 SurfaceView
- 它是一个完整的视频播放组件，开箱即用
- **注意：** VideoView 不支持自定义渲染，如需高级功能应使用 SurfaceView 或 TextureView

#### 2. 设置视频数据源

VideoView 支持多种方式设置数据源：

##### 方式 A：本地文件路径

```kotlin
// 使用文件路径
videoView.setVideoPath("/sdcard/Movies/my_video.mp4")

// 或使用 File 对象
val file = File("/sdcard/Movies/my_video.mp4")
videoView.setVideoPath(file.absolutePath)
```

##### 方式 B：网络 URL（HTTP/HTTPS）

```kotlin
// 直接设置 URL
videoView.setVideoPath("https://example.com/video.mp4")

// 或使用 URI
videoView.setVideoURI(Uri.parse("https://example.com/video.mp4"))
```

##### 方式 C：Content URI

```kotlin
// 从系统媒体库获取的 URI
val uri = Uri.parse("content://media/external/video/media/123")
videoView.setVideoURI(uri)
```

##### 方式 D：带自定义 Headers 的 HTTP 请求

```kotlin
val uri = Uri.parse("https://secure.example.com/video.mp4")
val headers = HashMap<String, String>().apply {
    put("User-Agent", "MyVideoPlayer")
    put("Authorization", "Bearer token123")
}
videoView.setVideoURI(uri, headers)
```

##### 方式 E：Raw 资源

```kotlin
// res/raw 目录下的视频
val uri = Uri.parse("android.resource://${packageName}/${R.raw.intro}")
videoView.setVideoURI(uri)
```

##### 方式 F：Assets 文件（特殊处理）

**⚠️ VideoView 不支持直接播放 assets 文件！**

需要先复制到缓存目录：

```kotlin
fun playAssetVideo(context: Context, assetPath: String, videoView: VideoView) {
    // 1. 将 assets 文件复制到缓存目录
    val cacheFile = File(context.cacheDir, "video_cache/${assetPath.substringAfterLast("/")}")
    
    if (!cacheFile.exists()) {
        cacheFile.parentFile?.mkdirs()
        context.assets.open(assetPath).use { input ->
            FileOutputStream(cacheFile).use { output ->
                input.copyTo(output)
            }
        }
    }
    
    // 2. 播放缓存文件
    videoView.setVideoPath(cacheFile.absolutePath)
}
```

**注意事项：**
- 网络权限：需要在 AndroidManifest.xml 中声明 `<uses-permission android:name="android.permission.INTERNET" />`
- 大文件从 assets 复制可能耗时，应在后台线程执行
- 缓存文件应及时清理以节省空间

#### 3. 注册监听器（必须在设置数据源之前或之后立即注册）

VideoView 提供多个监听器用于不同的事件回调：

##### OnPreparedListener（准备完成）

```kotlin
videoView.setOnPreparedListener { mp ->
    // mp 就是内部的 MediaPlayer 实例
    Log.d("VideoView", "Prepared! Duration=${mp.duration}ms")
    Log.d("VideoView", "Video size: ${mp.videoWidth}x${mp.videoHeight}")
    
    // 此时可以：
    // - 获取总时长：mp.duration
    // - 获取视频宽高：mp.videoWidth, mp.videoHeight
    // - 开始播放：mp.start() 或 videoView.start()
    // - 跳转位置：mp.seekTo(position)
    // - 设置音量：mp.setVolume(left, right)
    // - 设置速度（API 23+）：mp.playbackParams = ...
    
    // 隐藏加载指示器
    loadingIndicator.visibility = View.GONE
    
    // 自动开始播放（可选）
    // mp.start()
}
```

**重要：** `mp` 参数就是 VideoView 内部的 MediaPlayer 实例，保存它以便后续操作！

##### OnCompletionListener（播放完成）

```kotlin
videoView.setOnCompletionListener {
    Log.d("VideoView", "Playback completed!")
    
    // 处理逻辑：
    // - 自动播放下一个视频
    // - 更新 UI 为"已完成"
    // - 退出全屏模式
    // - 如果 isLooping=true，会自动重新开始
    
    // 手动重播示例：
    // videoView.seekTo(0)
    // videoView.start()
    
    // 显示重播按钮
    btnReplay.visibility = View.VISIBLE
}
```

##### OnErrorListener（错误处理）

```kotlin
videoView.setOnErrorListener { mp, what, extra ->
    Log.e("VideoView", "Error occurred! what=$what, extra=$extra")
    
    // what: 错误类型
    //   - MEDIA_ERROR_UNKNOWN = 1
    //   - MEDIA_ERROR_SERVER_DIED = 100
    // extra: 额外错误信息
    //   - MEDIA_ERROR_IO = -1004
    //   - MEDIA_ERROR_MALFORMED = -1007
    //   - MEDIA_ERROR_UNSUPPORTED = -1010
    //   - MEDIA_ERROR_TIMED_OUT = -110
    
    // 显示错误信息
    showErrorDialog("播放失败 ($what)")
    
    // 隐藏加载指示器
    loadingIndicator.visibility = View.GONE
    
    // 返回 true 表示错误已被处理
    // 返回 false 会触发 OnCompletionListener
    true
}
```

##### OnInfoListener（信息回调）

```kotlin
videoView.setOnInfoListener { mp, what, extra ->
    when (what) {
        MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
            Log.d("VideoView", "Buffering started...")
            loadingIndicator.visibility = View.VISIBLE
        }
        MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
            Log.d("VideoView", "Buffering ended!")
            loadingIndicator.visibility = View.GONE
        }
        MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> {
            Log.w("VideoView", "Video track lagging!")
        }
        MediaPlayer.MEDIA_INFO_UNKNOWN -> {
            Log.d("VideoView", "Unknown info: $what (extra=$extra)")
        }
        else -> {
            Log.d("VideoView", "Info: $what (extra=$extra)")
        }
    }
    false  // 返回 false 表示未消费此信息
}
```

#### 4. 开始播放

```kotlin
// 在 onPrepared 回调中或之后调用
videoView.start()

// 或使用 MediaPlayer 实例
// mediaPlayer.start()
```

**检查是否正在播放：**

```kotlin
if (videoView.isPlaying) {
    Log.d("VideoView", "正在播放中...")
}
```

#### 5. 播放控制

##### 暂停播放

```kotlin
videoView.pause()
// 状态：PLAYING → PAUSED
```

##### 恢复播放

```kotlin
videoView.start()
// 状态：PAUSED → PLAYING
// 注意：VideoView 没有 resume() 方法，统一使用 start()
```

##### 停止播放

```kotlin
videoView.stopPlayback()
// 状态：PLAYING/PAUSED → STOPPED
// ⚠️ 注意：停止后必须重新 setVideoPath/setVideoURI 才能再次播放
// stopPlayback() 会释放内部 MediaPlayer
```

##### 跳转到指定位置

```kotlin
// 跳转到 30 秒位置
videoView.seekTo(30000)  // 单位：毫秒

// seekTo 是异步的，可通过 OnSeekCompleteListener 监听完成（需要获取 MediaPlayer 实例）
// videoView 不直接暴露 OnSeekCompleteListener，需要在 onPrepared 中对 MediaPlayer 设置
```

##### 设置音量

```kotlin
// 需要在 onPrepared 中获取 MediaPlayer 实例后设置
var mediaPlayerInstance: MediaPlayer? = null

videoView.setOnPreparedListener { mp ->
    mediaPlayerInstance = mp
    // 左声道、右声道（0.0 ~ 1.0）
    mp.setVolume(0.8f, 0.8f)  // 80% 音量
    
    // 静音
    // mp.setVolume(0f, 0f)
    
    // 仅左声道
    // mp.setVolume(1f, 0f)
}

// 后续调整音量
mediaPlayerInstance?.setVolume(volume, volume)
```

##### 变速播放（API 23+）

```kotlin
// 需要在 onPrepared 中获取 MediaPlayer 实例后设置
videoView.setOnPreparedListener { mp ->
    // 设置 1.5 倍速
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val params = PlaybackParams().apply {
            speed = 1.5f  // 支持 0.25x ~ 4x
        }
        mp.playbackParams = params
    }
}
```

**注意：** 
- 需要 API >= 23 (Android 6.0)
- 必须在 PREPARED 或 PLAYING 状态设置
- 某些编解码器可能不支持特定速率

##### 循环播放

```kotlin
// 单曲循环
// VideoView 不直接提供 setLooping() 方法，需要在 onCompletion 中手动实现
videoView.setOnCompletionListener {
    // 重新播放
    videoView.seekTo(0)
    videoView.start()
}

// 或者通过 MediaPlayer 实例设置
videoView.setOnPreparedListener { mp ->
    mp.isLooping = true  // 单曲循环（部分情况有效）
}
```

#### 6. 获取播放信息

```kotlin
// 当前位置（毫秒）
val currentPosition = videoView.currentPosition

// 总时长（毫秒）（prepared 后可用）
val totalDuration = videoView.duration

// 是否正在播放
val isCurrentlyPlaying = videoView.isPlaying

// 获取视频宽高（需要 MediaPlayer 实例）
val videoWidth = mediaPlayerInstance?.videoWidth ?: 0
val videoHeight = mediaPlayerInstance?.videoHeight ?: 0
```

### 10.4 进度追踪实现

VideoView **没有内置进度回调机制**，需要自己实现：

#### 方式 A：Handler 定时器（传统方式）

```kotlin
private val handler = Handler(Looper.getMainLooper())
private val progressRunnable = object : Runnable {
    override fun run() {
        if (videoView != null && videoView.isPlaying) {
            val currentPos = videoView.currentPosition   // 当前位置（毫秒）
            val duration = videoView.duration             // 总时长（毫秒）

            // 更新 UI
            tvCurrentTime.text = formatTime(currentPos)
            tvTotalTime.text = formatTime(duration)
            seekBar.progress = (currentPos.toFloat() / duration * 100).toInt()

            // 继续定时查询
            handler.postDelayed(this, 200)  // 每 200ms 更新一次
        }
    }
}

// 开始追踪
handler.post(progressRunnable)

// 停止追踪
handler.removeCallbacks(progressRunnable)
```

#### 方式 B：Coroutine 协程（推荐，现代方式）

```kotlin
private var progressJob: Job? = null

// 开始进度追踪
private fun startProgressTracking() {
    progressJob = CoroutineScope(Dispatchers.Main).launch {
        while (isActive) {
            if (videoView.isPlaying) {
                val currentPos = videoView.currentPosition   // 当前位置（毫秒）
                val duration = videoView.duration             // 总时长（毫秒）

                // 更新 UI
                tvCurrentTime.text = formatTime(currentPos)
                tvTotalTime.text = formatTime(duration)
                seekBar.progress = (currentPos.toFloat() / duration * 100).toInt()
            }
            delay(200)  // 每 200ms 更新一次
        }
    }
}

// 停止进度追踪
private fun stopProgressTracking() {
    progressJob?.cancel()
    progressJob = null
}
```

**推荐使用方式 B**，因为：
- 自动在主线程更新 UI
- 不需要手动管理 Handler 的生命周期
- 协程取消更安全可靠
- 代码更简洁易读

### 10.5 监听器注册

VideoView 提供了多个监听器接口：

```kotlin
// 1. 准备完成监听器（必须）
videoView.setOnPreparedListener { mp ->
    Log.d("VideoView", "视频准备完成，时长: ${mp.duration}ms")
    
    // 可以在这里获取 MediaPlayer 实例进行高级操作
    // mp.setVolume(0.8f, 0.8f)
    // mp.isLooping = true
    
    // 自动开始播放
    videoView.start()
}

// 2. 播放完成监听器
videoView.setOnCompletionListener {
    Log.d("VideoView", "播放完成")
    // 处理播放完成逻辑（如循环播放、切换下一个等）
}

// 3. 错误监听器（重要！）
videoView.setOnErrorListener { mp, what, extra ->
    Log.e("VideoView", "播放错误: what=$what, extra=$extra")
    
    // 返回 true 表示已处理错误，false 表示未处理
    // 常见错误码：
    // what: MEDIA_ERROR_UNKNOWN (-1), MEDIA_ERROR_SERVER_DIED (100), etc.
    // extra: MEDIA_ERROR_IO (-1004), MEDIA_ERROR_MALFORMED (-1007), etc.
    
    true  // 返回 true 表示我们自己处理了这个错误
}

// 4. 信息监听器（可选，用于缓冲状态等）
videoView.setOnInfoListener { mp, what, extra ->
    when (what) {
        MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
            Log.d("VideoView", "缓冲开始")
            showLoading(true)
        }
        MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
            Log.d("VideoView", "缓冲结束")
            showLoading(false)
        }
        MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> {
            Log.w("VideoView", "视频轨道延迟")
        }
    }
    false  // 返回 false 表示未消费此信息
}
```

### 10.6 视频尺寸处理

获取视频原始尺寸并自适应布局：

```kotlin
videoView.setOnPreparedListener { mp ->
    // 获取视频宽高
    val videoWidth = mp.videoWidth
    val videoHeight = mp.videoHeight
    
    Log.d("VideoView", "视频原始尺寸: ${videoWidth}x${videoHeight}")
    
    // 自适应布局（保持宽高比）
    if (videoWidth > 0 && videoHeight > 0) {
        val containerWidth = container.width  // 容器宽度
        val ratio = videoWidth.toFloat() / videoHeight.toFloat()
        val adaptedHeight = (containerWidth / ratio).toInt()
        
        // 调整 VideoView 高度
        val params = videoView.layoutParams
        params.height = adaptedHeight
        videoView.layoutParams = params
        
        Log.d("VideoView", "调整后高度: $adaptedHeight px")
    }
}

// 或者使用 VideoSizeChangedListener（API 23+）
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    videoView.setOnVideoSizeChangedListener { mp, width, height ->
        Log.d("VideoView", "视频尺寸变化: ${width}x${height}")
        adjustVideoLayout(width, height)
    }
}

private fun adjustVideoLayout(videoWidth: Int, videoHeight: Int) {
    if (videoWidth > 0 && videoHeight > 0) {
        val containerWidth = container.width
        val ratio = videoWidth.toFloat() / videoHeight.toFloat()
        val adaptedHeight = (containerWidth / ratio).toInt()
        
        val params = videoView.layoutParams
        params.height = adaptedHeight
        videoView.layoutParams = params
    }
}
```

### 10.7 资源释放（非常重要）

**⚠️ 必须正确释放资源，否则会导致内存泄漏！**

```kotlin
override fun onDestroy() {
    super.onDestroy()
    
    // 方式一：使用 VideoView 的 stopPlayback() 方法
    videoView.stopPlayback()  // 停止播放并释放内部 MediaPlayer
    
    // 方式二：如果持有 MediaPlayer 引用
    mediaPlayerInstance?.apply {
        if (isPlaying) {
            stop()
        }
        release()  // 释放 MediaPlayer 资源
    }
    mediaPlayerInstance = null
    
    // 停止进度追踪
    stopProgressTracking()
    
    // 移除所有监听器（防止内存泄漏）
    videoView.setOnPreparedListener(null)
    videoView.setOnCompletionListener(null)
    videoView.setOnErrorListener(null)
    videoView.setOnInfoListener(null)
    
    Log.d("VideoView", "资源已释放")
}
```

**释放时机建议：**

| 生命周期 | 操作 | 说明 |
|----------|------|------|
| `onPause()` | 暂停播放 | 用户离开时暂停 |
| `onStop()` | 可选择停止 | 根据需求决定 |
| `onDestroyView()` | 解绑 VideoView | Fragment 视图销毁时 |
| `onDestroy()` | **必须释放** | 彻底清理所有资源 |

**常见错误：**

```kotlin
// ❌ 错误：忘记释放资源
// 导致 VideoView 和 MediaPlayer 泄漏

// ❌ 错误：在后台线程操作 UI
Thread {
    videoView.start()  // 异常！只能在主线程调用
}

// ✅ 正确：完整的资源释放流程
override fun onDestroy() {
    super.onDestroy()
    releaseResources()
}

private fun releaseResources() {
    // 1. 停止进度追踪
    stopProgressTracking()
    
    // 2. 停止并释放 MediaPlayer
    mediaPlayerInstance?.release()
    mediaPlayerInstance = null
    
    // 3. 释放 VideoView
    videoView.stopPlayback()
    
    // 4. 清空监听器引用
    videoView.setOnPreparedListener(null)
    videoView.setOnCompletionListener(null)
    videoView.setOnErrorListener(null)
    videoView.setOnInfoListener(null)
}
```

### 10.8 完整可运行示例

以下是一个完整的 Activity 示例，展示了纯 VideoView + MediaPlayer API 的所有核心功能：

```kotlin
class NativeVideoPlayerActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlayPause: Button
    private lateinit var loadingIndicator: ProgressBar
    
    private var mediaPlayerInstance: MediaPlayer? = null
    private var progressJob: Job? = null
    private var isUserSeeking = false
    
    companion object {
        private const val VIDEO_URL = "https://example.com/video.mp4"
        private const val PROGRESS_INTERVAL_MS = 200L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_video_player)
        
        initViews()
        setupVideoView()
        setupListeners()
        loadVideo()
    }
    
    private fun initViews() {
        videoView = findViewById(R.id.videoView)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        seekBar = findViewById(R.id.seekBar)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        loadingIndicator = findViewById(R.id.loadingIndicator)
    }
    
    private fun setupVideoView() {
        // 设置 VideoView 监听器
        videoView.setOnPreparedListener { mp ->
            mediaPlayerInstance = mp
            Log.d("NativePlayer", "视频准备完成，时长: ${mp.duration}ms")
            
            // 显示总时长
            tvTotalTime.text = formatTime(mp.duration.toLong())
            
            // 隐藏加载指示器
            loadingIndicator.visibility = View.GONE
            
            // 获取视频尺寸并调整布局
            adjustVideoLayout(mp.videoWidth, mp.videoHeight)
            
            // 自动开始播放
            videoView.start()
            startProgressTracking()
            updatePlayButton(true)
        }
        
        videoView.setOnCompletionListener {
            Log.d("NativePlayer", "播放完成")
            updatePlayButton(false)
            stopProgressTracking()
            
            // 单曲循环示例
            // videoView.seekTo(0)
            // videoView.start()
        }
        
        videoView.setOnErrorListener { _, what, extra ->
            Log.e("NativePlayer", "播放错误: what=$what, extra=$extra")
            loadingIndicator.visibility = View.GONE
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show()
            true  // 已处理错误
        }
        
        videoView.setOnInfoListener { _, what, _ ->
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    loadingIndicator.visibility = View.VISIBLE
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    loadingIndicator.visibility = View.GONE
                }
            }
            false
        }
    }
    
    private fun setupListeners() {
        // 播放/暂停按钮
        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                updatePlayButton(false)
            } else {
                videoView.start()
                updatePlayButton(true)
                startProgressTracking()
            }
        }
        
        // SeekBar 拖动监听
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null && videoView.duration > 0) {
                    val position = (seekBar.progress.toFloat() / 100 * videoView.duration).toLong()
                    videoView.seekTo(position)
                }
                seekBar?.post { isUserSeeking = false }
            }
            
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && videoView.duration > 0) {
                    val position = (progress.toFloat() / 100 * videoView.duration).toLong()
                    tvCurrentTime.text = formatTime(position)
                }
            }
        })
    }
    
    private fun loadVideo() {
        loadingIndicator.visibility = View.VISIBLE
        videoView.setVideoPath(VIDEO_URL)
    }
    
    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (videoView.isPlaying) {
                    val currentPos = videoView.currentPosition.toLong()
                    val duration = videoView.duration.toLong()
                    
                    if (!isUserSeeking && duration > 0) {
                        tvCurrentTime.text = formatTime(currentPos)
                        seekBar.progress = (currentPos.toFloat() / duration * 100).toInt()
                    }
                }
                delay(PROGRESS_INTERVAL_MS)
            }
        }
    }
    
    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }
    
    private fun adjustVideoLayout(videoWidth: Int, videoHeight: Int) {
        if (videoWidth > 0 && videoHeight > 0) {
            val containerWidth = videoView.width
            val ratio = videoWidth.toFloat() / videoHeight.toFloat()
            val adaptedHeight = (containerWidth / ratio).toInt()
            
            val params = videoView.layoutParams
            params.height = adaptedHeight
            videoView.layoutParams = params
            
            Log.d("NativePlayer", "视频布局调整: ${videoWidth}x${videoHeight} -> 高度=$adaptedHeight")
        }
    }
    
    private fun updatePlayButton(isPlaying: Boolean) {
        btnPlayPause.text = if (isPlaying) "暂停" else "播放"
    }
    
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
    
    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
            updatePlayButton(false)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 完整的资源释放流程
        stopProgressTracking()
        
        mediaPlayerInstance?.release()
        mediaPlayerInstance = null
        
        videoView.stopPlayback()
        
        // 清空所有监听器
        videoView.setOnPreparedListener(null)
        videoView.setOnCompletionListener(null)
        videoView.setOnErrorListener(null)
        videoView.setOnInfoListener(null)
        
        Log.d("NativePlayer", "Activity 销毁，资源已完全释放")
    }
}
```

**对应的 XML 布局文件 (`activity_native_video_player.xml`)：**

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <!-- 视频显示区域 -->
    <VideoView
        android:id="@+id/videoView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_centerHorizontal="true" />

    <!-- 加载指示器 -->
    <ProgressBar
        android:id="@+id/loadingIndicator"
        style="?android:attr/progressBarStyle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:visibility="gone" />

    <!-- 控制面板 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@+id/videoView"
        android:layout_marginTop="16dp"
        android:orientation="vertical">

        <!-- 进度条和时间显示 -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <TextView
                android:id="@+id/tvCurrentTime"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentStart="true"
                android:text="00:00"
                android:textSize="14sp" />

            <SeekBar
                android:id="@+id/seekBar"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_toEndOf="@+id/tvCurrentTime"
                android:layout_toStartOf="@+id/tvTotalTime"
                android:layout_marginStart="8dp"
                android:layout_marginEnd="8dp"
                android:max="100"
                android:progress="0" />

            <TextView
                android:id="@+id/tvTotalTime"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentEnd="true"
                android:text="00:00"
                android:textSize="14sp" />
        </RelativeLayout>

        <!-- 播放/暂停按钮 -->
        <Button
            android:id="@+id/btnPlayPause"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:layout_marginTop="8dp"
            android:text="播放" />

    </LinearLayout>
</RelativeLayout>
```

### 10.9 与封装类对比

| 特性 | 封装类 AndroidVideoViewPlayer | 原生 VideoView + MediaPlayer |
|------|-------------------------------|------------------------------|
| **代码量** | 少（~6 行核心代码） | 多（需要手动管理所有细节） |
| **状态管理** | 自动（9 种状态机） | 手动（需自己跟踪状态） |
| **进度追踪** | 内置（Coroutine 定时回调） | 需自己实现 Handler 或协程 |
| **错误处理** | 内置自动重试机制 | 需手动处理 |
| **生命周期** | attach/detach/release 清晰 | 需手动管理所有资源 |
| **Assets 支持** | 内置自动复制到缓存 | 需手动复制 |
| **循环模式** | 内置三种模式 | 需手动实现 |
| **音量控制** | 统一接口 | 需获取 MediaPlayer 实例 |
| **变速播放** | 统一接口（带缓存） | 需获取 MediaPlayer 实例 |
| **线程安全** | 保证主线程回调 | 需自己注意线程切换 |
| **灵活性** | 中等（受限于封装设计） | 高（完全控制） |
| **学习成本** | 低 | 高 |
| **适用场景** | 快速开发、标准需求 | 特殊需求、深度定制 |

**选择建议：**

- **使用封装类** 当：
  - 快速开发原型或产品
  - 标准视频播放需求
  - 团队希望统一接口
  - 需要完善的状态管理和错误处理
  
- **使用原生 API** 当：
  - 需要特殊定制功能
  - 学习底层原理
  - 对性能有极致要求
  - 需要与现有架构深度集成

---

## 总结

本文档详细介绍了基于 VideoView 的视频播放两种方式：

1. **上层封装**（第 2~9 章）：通过 `AndroidVideoViewPlayer` 封装类快速实现视频播放功能
2. **底层原理**（第 10 章）：直接使用原生 VideoView + MediaPlayer API 理解实现机制

两种方式各有优劣，根据项目需求选择合适的方案。对于大多数应用场景，推荐使用封装类以提高开发效率；对于特殊需求，可以直接使用原生 API 进行深度定制。

**关键要点：**
- VideoView 是简单易用的视频播放控件，适合快速开发
- 必须正确管理生命周期和资源释放，避免内存泄漏
- 进度追踪需要自行实现（Handler 或 Coroutine）
- Assets 文件需要先复制到缓存目录才能播放
- 通过 onPrepared 回调可以获取 MediaPlayer 实例进行高级控制
- 原生 API 提供最大的灵活性，但需要更多的代码和管理

---

> **文档版本**: v1.0  
> **最后更新**: 2026-06-17  
> **适用平台**: Android API 21+ (Android 5.0 Lollipop)  
> **依赖库**: 无第三方依赖（仅使用 Android SDK 原生组件）