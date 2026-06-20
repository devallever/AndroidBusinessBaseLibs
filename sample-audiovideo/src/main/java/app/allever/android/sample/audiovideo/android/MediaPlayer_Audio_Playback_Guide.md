# Android MediaPlayer 音频播放完整指南

> **基于 `AndroidMusicPlayer` 封装类，详细描述 MediaPlayer 播放音频的完整流程、API 使用及底层原理**

## 目录

- [1. 概述](#1-概述)
- [2. 核心类与接口](#2-核心类与接口)
  - [2.1 PlayerState 状态枚举](#21-playerstate-状态枚举)
  - [2.2 LoopMode 循环模式](#22-loopmode-循环模式)
  - [2.3 IPlayerListener 监听接口](#23-iplayerlistener-监听接口)
  - [2.4 AndroidMusicPlayer 封装类](#24-androidmusicplayer-封装类)
- [3. 状态机详解](#3-状态机详解)
  - [3.1 状态定义](#31-状态定义)
  - [3.2 状态转换图](#32-状态转换图)
  - [3.3 转换规则表](#33-转换规则表)
- [4. 完整播放流程](#4-完整播放流程)
  - [4.1 快速开始（5行代码）](#41-快速开始5行代码)
  - [4.2 完整生命周期流程](#42-完整生命周期流程)
  - [4.3 步骤一：创建实例](#43-步骤一创建实例)
  - [4.4 步骤二：设置监听器](#44-步骤二设置监听器)
  - [4.5 步骤三：设置数据源](#45-步骤三设置数据源)
  - [4.6 步骤四：等待准备完成](#46-步骤四等待准备完成)
  - [4.7 步骤五：开始播放](#47-步骤五开始播放)
  - [4.8 步骤六：播放控制](#48-步骤六播放控制)
  - [4.9 步骤七：进度追踪](#49-步骤七进度追踪)
  - [4.10 步骤八：释放资源](#410-步骤八释放资源)
- [5. API 接口详解](#5-api-接口详解)
  - [5.1 数据源设置](#51-数据源设置)
  - [5.2 播放控制](#52-播放控制)
  - [5.3 只读属性](#53-只读属性)
  - [5.4 可配置属性](#54-可配置属性)
- [6. 高级功能](#6-高级功能)
  - [6.1 变速播放](#61-变速播放)
  - [6.2 音量控制](#62-音量控制)
  - [6.3 循环模式](#63-循环模式)
  - [6.4 自动重试机制](#64-自动重试机制)
  - [6.5 SeekBar 拖动跳转](#65-seekbar-拖动跳转)
- [7. 生命周期管理](#7-生命周期管理)
  - [7.1 Activity 中的正确使用](#71-activity-中的正确使用)
  - [7.2 Fragment 中的使用](#72-fragment-中的使用)
  - [7.3 注意事项](#73-注意事项)
- [8. 使用示例](#8-使用示例)
  - [8.1 示例一：简单音乐播放器](#81-示例一简单音乐播放器)
  - [8.2 示例二：带 UI 的音乐播放器](#82-示例二带-ui-的音乐播放器)
  - [8.3 示例三：后台音乐播放服务](#83-示例三后台音乐播放服务)
- [9. 常见问题与最佳实践](#9-常见问题与最佳实践)
- **[10. 纯 MediaPlayer 原生 API 播放流程](#10-纯-mediaplayer-原生-api-播放流程)** ⭐
  - [10.1 快速开始](#101-快速开始)
  - [10.2 完整生命周期](#102-完整生命周期)
  - [10.3 步骤详解](#103-步骤详解)
  - [10.4 进度追踪的三种实现方式](#104-进度追踪的三种实现方式)
  - [10.5 监听器注册](#105-监听器注册)
  - [10.6 资源释放（非常重要）](#106-资源释放非常重要)
  - [10.7 完整可运行示例](#107-完整可运行示例)
  - [10.8 与封装类对比](#108-与封装类对比)

---

## 1. 概述

MediaPlayer 是 Android 原生的多媒体播放框架，支持音频和视频播放。本文档基于项目中的 `AndroidMusicPlayer` 封装类，详细描述：

- **上层封装**：如何使用 `AndroidMusicPlayer` 进行音频播放（第 2~9 章）
- **底层原理**：直接使用原生 MediaPlayer API 的完整流程（第 10 章）

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

### 2.3 IPlayerListener 监听接口

```kotlin
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

### 2.4 AndroidMusicPlayer 封装类

```
AndroidMusicPlayer
├── 成员变量
│   ├── mediaPlayer: MediaPlayer?        // 原生 MediaPlayer 实例
│   ├── listener: IPlayerListener?       // 事件监听器
│   ├── _state: PlayerState              // 当前状态（私有）
│   ├── progressJob: Job?                // 进度追踪协程
│   ├── currentUri: Uri?                 // 当前数据源 URI
│   ├── currentHeaders: Map<...>?        // HTTP 请求头
│   └── retryLeft: Int                   // 剩余重试次数
│
├── 对外 API
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

---

## 3. 状态机详解

### 3.1 状态定义

MediaPlayer 有严格的 **状态转换规则**，错误的状态转换会抛出异常。

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
| IDLE | setSource() | PREPARING |
| PREPARING | （等待中） | → PREPARED 或 ERROR |
| PREPARED | play() | PLAYING |
| PLAYING | pause() | PAUSED |
| PAUSED | play() | PLAYING |
| PLAYING/PAUSED/PREPARED/COMPLETED | stop() | STOPPED |
| STOPPED/COMPLETED/ERROR/IDLE | setSource() | PREPARING |
| 任意状态（除 RELEASED） | release() | RELEASED |

---

## 4. 完整播放流程

### 4.1 快速开始（5行代码）

```kotlin
val player = AndroidMusicPlayer()

player.setListener(object : IPlayerListener {
    override fun onPrepared(durationMs: Long) {
        player.play()  // 准备完成后自动播放
    }
})

player.setSource("https://example.com/audio.mp3")

// 不用时：
// player.release()
```

### 4.2 完整生命周期流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                    AndroidMusicPlayer 完整生命周期                    │
└─────────────────────────────────────────────────────────────────────┘

① 创建实例
   val player = AndroidMusicPlayer()
   ↓ state = IDLE

② 设置监听器（可选但推荐）
   player.setListener(listener)
   ↓

③ 设置数据源（自动进入准备阶段）
   player.setSource(url)
   ↓ 内部执行：
     ├── initMediaPlayer()  → 创建/复用 MediaPlayer
     ├── 配置 AudioAttributes
     ├── 注册 4 个监听器
     ├── 调用 setDataSource()
     └── 调用 prepareAsync()
   ↓ state = PREPARING

④ 等待准备完成（异步）
   回调 listener.onPrepared(durationMs)
   ↓ state = PREPARED
   此时可以获取 player.duration

⑤ 开始播放
   player.play()
   ↓ 内部执行：
     ├── mediaPlayer.start()
     ├── state = PLAYING
     └── startProgressTracking()  ← 启动协程定时回调

⑥ 播放中（定时回调）
   listener.onProgress(currentMs, durationMs)
   每 progressIntervalMs 回调一次（默认 200ms）

⑦ 暂停 / 恢复
   player.pause()  → state = PAUSED，停止进度追踪
   player.play()   → state = PLAYING，恢复进度追踪

⑧ 播放完成
   回调 listener.onComplete()
   ↓ 根据 loopMode 处理：
     ├── NONE/ALL  → state = COMPLETED
     └── SINGLE    → seekTo(0) + start()，自动重播

⑨ 停止（可选）
   player.stop()
   ↓ state = STOPPED
   ⚠️ 需要重新 setSource 才能再次播放

⑩ 释放资源（必须！）
   player.release()
   ↓ 内部执行：
     ├── 移除所有监听器（防止内存泄漏）
     ├── stop() + release()
     ├── 取消进度追踪协程
     └── state = RELEASED（终态）
```

### 4.3 步骤一：创建实例

```kotlin
val player = AndroidMusicPlayer()
// 此时 state = PlayerState.IDLE
// mediaPlayer = null
```

**内部操作：**
- 初始化所有成员变量为默认值
- `_state = PlayerState.IDLE`
- `mediaPlayer = null`
- `loopMode = LoopMode.NONE`, `speed = 1.0f`, `volume = 1.0f`

### 4.4 步骤二：设置监听器

```kotlin
player.setListener(object : IPlayerListener {
    override fun onStateChanged(from: PlayerState, to: PlayerState) {
        Log.d("Player", "状态变化: $from -> $to")
    }

    override fun onPrepared(durationMs: Long) {
        Log.d("Player", "准备就绪, 时长=${durationMs}ms")
        player.play()  // 准备完成后自动播放
    }

    override fun onProgress(currentMs: Long, durationMs: Long) {
        // 更新 UI 进度条
        updateProgressBar(currentMs, durationMs)
    }

    override fun onComplete() {
        Log.d("Player", "播放完成")
    }

    override fun onError(what: Int, extra: Int): Boolean {
        Log.e("Player", "错误: what=$what, extra=$extra")
        return false  // 返回 true 表示已处理错误
    }

    override fun onBufferingUpdate(percent: Int) {
        Log.d("Player", "缓冲进度: $percent%")
    }
})
```

### 4.5 步骤三：设置数据源

支持多种方式：

#### 方式 A：URL 字符串

```kotlin
// 网络 URL
player.setSource("https://example.com/audio.mp3")

// 本地文件
player.setSource("file:///sdcard/Music/song.mp3")

// Content URI
player.setSource("content://media/external/audio/media/123")

// Assets 文件（通过 file 协议）
player.setSource("file:///android_asset/audio/test.mp3")
```

#### 方式 B：URI 对象 + 自定义 Headers

```kotlin
val headers = mapOf(
    "User-Agent" to "MyAudioPlayer",
    "Authorization" to "Bearer token123"
)
player.setSource(Uri.parse("https://example.com/audio.mp3"), headers)
```

#### 方式 C：Assets 文件（推荐方式）

```kotlin
player.setAssetSource("audio/test.mp3")  // 相对于 assets 目录的路径
```

**内部处理流程 (`doPrepare()`)**：

```
doPrepare()
│
├── 1. 检查当前状态
│   ├── 如果已有实例且处于非 IDLE/RELEASED 状态 → reset()
│   └── 否则继续
│
├── 2. 初始化 MediaPlayer (initMediaPlayer())
│   ├── 如果为空或处于 IDLE/RELEASED → 创建新实例
│   ├── 设置 AudioAttributes:
│   │   └── USAGE_MEDIA + CONTENT_TYPE_MUSIC
│   ├── 注册 OnPreparedListener
│   │   └── 状态变为 PREPARED → applySpeed() → 回调 onPrepared()
│   ├── 注册 OnCompletionListener
│   │   └── 根据 loopMode 处理（SINGLE 自动重播 / 其他回调 onComplete）
│   ├── 注册 OnErrorListener
│   │   └── 状态变为 ERROR → 如有剩余重试次数则 500ms 后自动重试
│   └── 注册 OnBufferingUpdateListener
│       └── 直接转发 percent 给 listener
│
├── 3. 根据 URI 类型调用 setDataSource()
│   ├── Assets 文件:
│   │   ├── 打开 AssetFileDescriptor
│   │   ├── setDataSource(fd, startOffset, length)
│   │   └── 关闭 fd
│   │
│   ├── HTTP/HTTPS URL（带 headers）:
│   │   └── setDataSource(context, uri, headers)
│   │
│   └── 其他（本地文件/Content URI）:
│       └── setDataSource(context, uri)
│
├── 4. 调用 prepareAsync()
│   └── 状态变为 PREPARING
│
└── 5. 异常处理
    └── handlePrepareError(e)
        ├── 状态变为 ERROR
        ├── 回调 listener.onError(-1, 0)
        └── 如果有剩余重试次数 → 500ms 后自动重试
```

### 4.6 步骤四：等待准备完成

**OnPreparedListener 回调处理：**

```kotlin
setOnPreparedListener { mp ->
    _state = PlayerState.PREPARED
    applySpeed()  // 应用初始速度设置
    listener?.onPrepared(mp.duration.toLong())
    // 注意：此处不会自动播放，需要外部调用 play()
}
```

**此时可以：**
- 获取总时长：`player.duration`
- 调用 `player.play()` 开始播放
- 调用 `player.seekTo(position)` 跳转位置

### 4.7 步骤五：开始播放

```kotlin
player.play()
```

**内部实现：**

```kotlin
fun play() {
    when (_state) {
        PlayerState.PREPARED, PlayerState.COMPLETED -> {
            mediaPlayer?.start()
            _state = PlayerState.PLAYING
            startProgressTracking()  // 启动进度追踪协程
        }
        PlayerState.PAUSED -> {
            mediaPlayer?.start()     // 从暂停恢复
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

### 4.8 步骤六：播放控制

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
// ⚠️ 注意：停止后必须重新 setSource() 才能再次播放
```

#### 跳转到指定位置

```kotlin
player.seekTo(30000)  // 跳转到 30 秒处
// 单位：毫秒
// 在非 IDLE/RELEASED 状态均可调用
```

### 4.9 步骤七：进度追踪

**启动机制（Coroutine 实现）：**

```kotlin
private fun startProgressTracking() {
    stopProgressTracking()  // 先停止旧的
    
    progressJob = CoroutineScope(Dispatchers.Main).launch {
        while (isActive && _state == PlayerState.PLAYING) {
            val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
            val dur = mediaPlayer?.duration?.toLong() ?: 0L
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

### 4.10 步骤八：释放资源

```kotlin
player.release()
```

**释放流程：**

```kotlin
fun release() {
    // 1. 停止进度追踪协程
    stopProgressTracking()
    
    try {
        // 2. 移除所有监听器（防止内存泄漏）
        mediaPlayer?.setOnPreparedListener(null)
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.setOnErrorListener(null)
        mediaPlayer?.setOnBufferingUpdateListener(null)
        
        // 3. 停止并释放 MediaPlayer
        mediaPlayer?.stop()
        mediaPlayer?.release()
    } catch (_: Exception) {}  // 忽略释放时的异常
    
    // 4. 清空引用
    mediaPlayer = null
    currentUri = null
    currentHeaders = null
    
    // 5. 更新状态为终态
    _state = PlayerState.RELEASED  // 不可再使用
}
```

**为什么必须释放？**

| 不释放的后果 | 说明 |
|-------------|------|
| 内存泄漏 | MediaPlayer 持有 Context 引用 |
| CPU 浪费 | 后台解码线程仍在运行 |
| 音频冲突 | 可能导致其他应用无法播放音频 |
| 电池消耗 | 持续消耗电量 |
| 系统崩溃 | 严重时可能导致 ANR |

---

## 5. API 接口详解

### 5.1 数据源设置

| 方法 | 参数 | 说明 | 有效状态 |
|------|------|------|----------|
| `setSource(url: String)` | URL 字符串 | 支持 http/https/file/content/android_asset | 非 RELEASED |
| `setSource(uri: Uri, headers?)` | URI 对象 + 可选 Headers | 支持自定义 HTTP 请求头 | 非 RELEASED |
| `setAssetSource(path: String)` | Assets 相对路径 | 如 `"audio/test.mp3"` | 非 RELEASED |

### 5.2 播放控制

| 方法 | 说明 | 有效状态 |
|------|------|----------|
| `play()` | 开始或恢复播放 | PREPARED, COMPLETED, PAUSED |
| `pause()` | 暂停 | PLAYING |
| `stop()` | 停止 | PLAYING, PAUSED, PREPARED, COMPLETED |
| `seekTo(positionMs: Long)` | 跳转位置 | 非 IDLE, RELEASED |
| `setListener(listener?)` | 设置事件监听器 | 任意 |
| `release()` | 释放资源 | 任意 |

### 5.3 只读属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `state` | `PlayerState` | 当前状态 |
| `isPlaying` | `Boolean` | 是否正在播放（state == PLAYING && mediaPlayer.isPlaying） |
| `currentPosition` | `Long` | 当前播放位置（毫秒），异常时返回 0 |
| `duration` | `Long` | 总时长（毫秒），PREPARED 后可用，异常时返回 0 |

### 5.4 可配置属性

| 属性 | 类型 | 默认值 | 范围 | 说明 |
|------|------|--------|------|------|
| `loopMode` | `LoopMode` | `NONE` | 枚举 | 循环模式 |
| `progressIntervalMs` | `Int` | `200` | > 0 | 进度回调间隔（毫秒） |
| `retryCount` | `Int` | `0` | >= 0 | 出错自动重试次数 |
| `speed` | `Float` | `1.0f` | 0.5~3.0 | 变速倍率 |
| `volume` | `Float` | `1.0f` | 0.0~1.0 | 音量（左右声道相同） |

---

## 6. 高级功能

### 6.1 变速播放

```kotlin
// 设置 1.5 倍速
player.speed = 1.5f

// 内部实现
private fun applySpeed() {
    try {
        mediaPlayer?.playbackParams = PlaybackParams().apply {
            speed = this@AndroidMusicPlayer.speed
        }
    } catch (e: Exception) {
        log("MusicPlayer", "setSpeed error: ${e.message}")
    }
}
```

**限制：**
- 需要 API >= 23 (Android 6.0)
- 支持 0.25x ~ 4x（本封装限制为 0.5x ~ 3.0x）
- 某些编解码器可能不支持特定速率

### 6.2 音量控制

```kotlin
// 设置音量为 50%
player.volume = 0.5f

// 内部实现
mediaPlayer?.setVolume(volume, volume)  // 左右声道相同
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

// 内部逻辑
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

---

## 7. 生命周期管理

### 7.1 Activity 中的正确使用

```kotlin
class MusicActivity : AppCompatActivity() {

    private lateinit var player: AndroidMusicPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 创建播放器
        player = AndroidMusicPlayer()
        player.setListener(playerListener)

        // 2. 设置数据源（自动进入准备阶段）
        player.setSource("https://example.com/audio.mp3")
    }

    override fun onResume() {
        super.onResume()
        // 3. 恢复播放（如果之前在播放）
        // MediaPlayer 会自动恢复，无需额外操作
    }

    override fun onPause() {
        super.onPause()
        // 4. 可选：暂停播放
        if (player.state == PlayerState.PLAYING) {
            player.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 5. 必须释放资源
        if (!isChangingConfigurations) {
            player.release()  // 屏幕旋转时不释放
        }
    }
}
```

### 7.2 Fragment 中的使用

```kotlin
class MusicFragment : Fragment() {

    private var player: AndroidMusicPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player = AndroidMusicPlayer().apply {
            setListener(listener)
            setSource("https://example.com/audio.mp3")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Fragment 销毁视图时释放
        player?.release()
        player = null
    }
}
```

### 7.3 注意事项

1. **必须释放资源**：忘记调用 `release()` 会导致内存泄漏
2. **避免重复创建**：在 Activity/Fragment 重建时检查是否需要释放旧实例
3. **线程安全**：所有回调都在主线程，可直接更新 UI
4. **异常捕获**：内部已对大部分操作进行 try-catch 保护
5. **配置更改**：使用 `isChangingConfigurations` 判断是否需要释放

---

## 8. 使用示例

### 8.1 示例一：简单音乐播放器

```kotlin
class SimpleMusicPlayer {

    private val player = AndroidMusicPlayer()

    init {
        setupPlayer()
    }

    private fun setupPlayer() {
        player.setListener(object : IPlayerListener {
            override fun onPrepared(durationMs: Long) {
                println("Duration: ${durationMs}ms")
                player.play()  // 准备完成后自动播放
            }

            override fun onComplete() {
                println("Playback completed")
            }

            override fun onError(what: Int, extra: Int): Boolean {
                println("Error: $what, $extra")
                return false
            }
        })

        // 配置
        player.loopMode = LoopMode.SINGLE  // 单曲循环
        player.volume = 0.8f              // 80% 音量
        player.retryCount = 3             // 失败重试 3 次

        // 开始播放
        player.setSource("https://example.com/music.mp3")
    }

    fun play() = player.play()
    fun pause() = player.pause()
    fun stop() = player.stop()
    fun release() = player.release()
}
```

### 8.2 示例二：带 UI 的音乐播放器

```kotlin
class MusicPlayerFragment : Fragment() {

    private lateinit var binding: FragmentMusicBinding
    private lateinit var player: AndroidMusicPlayer
    private var isUserSeeking = false

    override fun onCreateView(...): View {
        binding = FragmentMusicBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initPlayer()
        initUI()
    }

    private fun initPlayer() {
        player = AndroidMusicPlayer().apply {
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

    private val playerListener = object : IPlayerListener {
        override fun onStateChanged(from: PlayerState, to: PlayerState) {
            updateUI()
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
            return false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
    }

    companion object {
        fun formatTime(ms: Long): String {
            val totalSeconds = ms / 1000
            return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        }
    }
}
```

### 8.3 示例三：后台音乐播放服务

```kotlin
class MusicService : Service() {

    private var player: AndroidMusicPlayer? = null
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getPlayer(): AndroidMusicPlayer? = player
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        player = AndroidMusicPlayer().apply {
            setListener(serviceListener)
            loopMode = LoopMode.ALL
        }

        // 前台服务通知
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    private val serviceListener = object : IPlayerListener {
        override fun onPrepared(durationMs: Long) {
            player?.play()
            updateNotification(isPlaying = true)
        }

        override fun onComplete() {
            playNext()  // 播放下一首
        }

        override fun onStateChanged(from: PlayerState, to: PlayerState) {
            updateNotification(to == PlayerState.PLAYING)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
```

---

## 9. 常见问题与最佳实践

### Q1: 为什么使用 prepareAsync() 而不是 prepare()？

**A:** `prepare()` 是同步阻塞的，会卡住主线程导致 ANR。`prepareAsync()` 是异步的，不阻塞线程，通过回调通知准备完成。

```kotlin
// ❌ 错误：会卡住主线程
mediaPlayer.prepare()

// ✅ 正确：异步准备
mediaPlayer.prepareAsync()
mediaPlayer.setOnPreparedListener { it.start() }
```

### Q2: 如何处理网络音频缓冲？

**A:** 使用 `IPlayerListener.onBufferingUpdate(percent)` 监听缓冲进度：

```kotlin
override fun onBufferingUpdate(percent: Int) {
    progressBar.secondaryProgress = percent  // 更新二级进度条
}
```

### Q3: 如何实现精确的进度显示？

**A:** 使用 Coroutine 定时器而非 MediaPlayer 自身回调：

```kotlin
// ✅ 推荐：使用协程定时查询（已在封装内实现）
private fun startProgressTracking() {
    CoroutineScope(Dispatchers.Main).launch {
        while (isActive && isPlaying) {
            val pos = mediaPlayer.currentPosition
            val dur = mediaPlayer.duration
            listener.onProgress(pos.toLong(), dur.toLong())
            delay(200)  // 每 200ms 更新一次
        }
    }
}
```

### Q4: 如何避免内存泄漏？

**A:** 必须在合适的时机释放资源：

```kotlin
// ✅ 正确：在 onDestroy 中释放
override fun onDestroy() {
    super.onDestroy()
    player.release()  // 移除所有监听器并释放 MediaPlayer
}

// ❌ 错误：忘记释放
// 会导致 MediaPlayer 实例泄漏，甚至导致 Activity 无法被 GC
```

### Q5: 如何处理配置更改（如屏幕旋转）？

**A:** 使用 `isChangingConfigurations` 判断：

```kotlin
override fun onDestroy() {
    super.onDestroy()
    // 屏幕旋转时不释放，保留播放状态
    if (!isChangingConfigurations) {
        player.release()
    }
}
```

### Q6: 变速播放有什么限制？

**A:** 
- 最低 API 23 (Android 6.0)
- 支持 0.25x ~ 4x（本封装限制为 0.5x ~ 3.0x）
- 某些编解码器可能不支持特定速率

### Q7: 如何同时播放多个音频？

**A:** 创建多个 `AndroidMusicPlayer` 实例：

```kotlin
val bgmPlayer = AndroidMusicPlayer()  // 背景音乐
val sfxPlayer = AndroidMusicPlayer()  // 音效

bgmPlayer.setSource("bgm.mp3")
sfxPlayer.setSource("sfx.mp3")

bgmPlayer.play()
// 稍后播放音效
sfxPlayer.play()
```

**注意:** 同时播放多个音频会消耗更多 CPU 和内存资源。

---

## 10. 纯 MediaPlayer 原生 API 播放流程

> **本章展示如何直接使用 Android 原生 `MediaPlayer` API 播放音频，**  
> **不依赖任何封装类，帮助你理解底层实现原理。**

### 10.1 快速开始

最简单的纯 MediaPlayer 音频播放（5 行核心代码）：

```kotlin
// 1. 创建
val mediaPlayer = MediaPlayer()

// 2. 设置数据源
mediaPlayer.setDataSource("https://example.com/audio.mp3")

// 3. 准备（异步）
mediaPlayer.prepareAsync()

// 4. 准备完成后播放
mediaPlayer.setOnPreparedListener {
    it.start()  // it 就是 mediaPlayer 本身
}

// 5. 不用时释放
// mediaPlayer.release()
```

### 10.2 完整生命周期

```
┌─────────────────────────────────────────────────────────────────────┐
│                    MediaPlayer 原生 API 完整流程                      │
└─────────────────────────────────────────────────────────────────────┘

                    ┌──────────┐
                    │   创建    │  MediaPlayer()
                    └────┬─────┘
                         │
                         ▼
              ┌─────────────────────┐
              │    设置数据源         │  setDataSource()
              │  (URL/File/Assets)  │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │   异步准备           │  prepareAsync()
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
     │  start()     │      │ reset() /    │
     │  (PLAYING)   │      │ release()    │
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
     │   release()   │
     │  (RELEASED)   │
     └──────────────┘
```

### 10.3 步骤详解

#### 1. 创建实例

```kotlin
val mediaPlayer = MediaPlayer()
```

**说明：**
- 此时状态为 **IDLE**
- 可以多次创建不同实例同时播放多个音频
- **注意：** 不要在主线程执行耗时操作

#### 2. 设置数据源

MediaPlayer 支持多种数据源类型：

##### 方式 A：网络 URL

```kotlin
// HTTP/HTTPS URL
mediaPlayer.setDataSource("https://example.com/audio.mp3")

// 或带自定义 Headers
val headers = HashMap<String, String>().apply {
    put("User-Agent", "MyAudioPlayer")
    put("Authorization", "Bearer token123")
}
mediaPlayer.setDataSource(context, Uri.parse(url), headers)
```

##### 方式 B：本地文件

```kotlin
// file:// 协议
mediaPlayer.setDataSource("/sdcard/Music/song.mp3")

// 或使用 File 对象
val file = File("/sdcard/Music/song.mp3")
mediaPlayer.setDataSource(FileInputStream(file).fd)

// 或使用 URI
val uri = Uri.fromFile(file)
mediaPlayer.setDataSource(context, uri)
```

##### 方式 C：Assets 目录

```kotlin
// assets 目录下的音频文件
val afd = context.assets.openFd("audio/background.mp3")
mediaPlayer.setDataSource(
    afd.fileDescriptor,
    afd.startOffset,  // 起始偏移量
    afd.length        // 文件长度
)
afd.close()  // 记得关闭！
```

##### 方式 D：Content URI

```kotlin
// 从 MediaStore 获取的 URI
val uri = Uri.parse("content://media/external/audio/media/123")
mediaPlayer.setDataSource(context, uri)
```

##### 方式 E：Raw 资源

```kotlin
// res/raw 目录下的音频
val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.music}")
mediaPlayer.setDataSource(context, uri)
```

**注意事项：**
- `setDataSource()` 可能在 IDLE、STOPPED 状态调用
- **必须在 prepare() / prepareAsync() 之前调用**
- Assets 方式需要手动关闭 AssetFileDescriptor
- 网络权限：需要在 AndroidManifest.xml 中声明 `<uses-permission android:name="android.permission.INTERNET" />`

#### 3. 异步准备

```kotlin
// ✅ 推荐：异步准备（不阻塞线程）
mediaPlayer.prepareAsync()

// ❌ 不推荐：同步准备（会阻塞当前线程）
// mediaPlayer.prepare()
```

**注册准备完成的监听器：**

```kotlin
mediaPlayer.setOnPreparedListener { mp ->
    // mp 就是 mediaPlayer 本身
    Log.d("MediaPlayer", "Prepared! Duration: ${mp.duration}ms")
    
    // 此时可以：
    // - 获取总时长：mp.duration
    // - 开始播放：mp.start()
    // - 跳转位置：mp.seekTo(position)
}
```

**此时状态变化：**
```
IDLE → setDataSource() → IDLE → prepareAsync() → PREPARING → onPrepared() → PREPARED
```

#### 4. 开始播放

```kotlin
// 在 onPrepared 回调中或之后调用
mediaPlayer.start()

// 状态：PREPARED → PLAYING
```

**检查是否正在播放：**

```kotlin
if (mediaPlayer.isPlaying) {
    Log.d("MediaPlayer", "正在播放中...")
}
```

#### 5. 播放控制

##### 暂停播放

```kotlin
mediaPlayer.pause()
// 状态：PLAYING → PAUSED
```

##### 恢复播放

```kotlin
mediaPlayer.start()
// 状态：PAUSED → PLAYING
```

##### 停止播放

```kotlin
mediaPlayer.stop()
// 状态：PLAYING/PAUSED → STOPPED
// ⚠️ 注意：停止后必须重新 setDataSource + prepare 才能再次播放
```

##### 跳转到指定位置

```kotlin
// 跳转到 30 秒位置
mediaPlayer.seekTo(30000)  // 单位：毫秒

// seekTo 是异步的，可通过 OnSeekCompleteListener 监听完成
mediaPlayer.setOnSeekCompleteListener {
    Log.d("MediaPlayer", "Seek completed!")
}
```

##### 设置音量

```kotlin
// 左声道、右声道（0.0 ~ 1.0）
mediaPlayer.setVolume(0.8f, 0.8f)  // 80% 音量

// 静音
mediaPlayer.setVolume(0f, 0f)

// 仅左声道
mediaPlayer.setVolume(1f, 0f)
```

##### 变速播放（API 23+）

```kotlin
// 设置 1.5 倍速
val params = PlaybackParams().apply {
    speed = 1.5f  // 支持 0.25x ~ 4x
}
mediaPlayer.playbackParams = params
```

**注意：** 
- 需要 API >= 23 (Android 6.0)
- 必须在 PREPARED 或 PLAYING 状态设置
- 某些编解码器可能不支持特定速率

##### 循环播放

```kotlin
// 单曲循环
mediaPlayer.isLooping = true

// 不循环（默认）
mediaPlayer.isLooping = false
```

### 10.4 进度追踪的三种实现方式

MediaPlayer **没有内置进度回调机制**，需要自己实现：

#### 方式 A：Handler 定时器（传统方式）

```kotlin
private val handler = Handler(Looper.getMainLooper())
private val progressRunnable = object : Runnable {
    override fun run() {
        if (mediaPlayer != null && mediaPlayer.isPlaying) {
            val currentPos = mediaPlayer.currentPosition  // 当前位置（毫秒）
            val duration = mediaPlayer.duration          // 总时长（毫秒）

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
import kotlinx.coroutines.*

private var progressJob: Job? = null

private fun startProgressTracking() {
    stopProgressTracking()
    
    progressJob = CoroutineScope(Dispatchers.Main).launch {
        while (isActive && mediaPlayer?.isPlaying == true) {
            val currentPos = mediaPlayer?.currentPosition ?: 0
            val duration = mediaPlayer?.duration ?: 0
            
            // 更新 UI
            updateProgressBar(currentPos, duration)
            
            delay(200)  // 每 200ms 更新一次
        }
    }
}

private fun stopProgressTracking() {
    progressJob?.cancel()
    progressJob = null
}

// 使用
startProgressTracking()  // 开始播放时调用
stopProgressTracking()  // 暂停/停止时调用
```

#### 方式 C：Timer 定时器

```kotlin
import java.util.Timer
import java.util.TimerTask

private var timer: Timer? = null

private fun startProgressTracking() {
    stopProgressTracking()
    
    timer = Timer().scheduleAtFixedRate(object : TimerTask {
        override fun run() {
            if (mediaPlayer?.isPlaying == true) {
                runOnUiThread {
                    val pos = mediaPlayer?.currentPosition ?: 0
                    val dur = mediaPlayer?.duration ?: 0
                    updateUI(pos, dur)
                }
            }
        }
    }, 0, 200)  // 初始延迟 0ms，间隔 200ms
}

private fun stopProgressTracking() {
    timer?.cancel()
    timer = null
}
```

**三种方式对比：**

| 特性 | Handler | Coroutine | Timer |
|------|---------|-----------|-------|
| **复杂度** | 中等 | 低 | 低 |
| **性能** | 好 | 最好 | 一般 |
| **取消便利性** | 一般 | 很好 | 一般 |
| **内存泄漏风险** | 有 | 无 | 有 |
| **推荐场景** | 旧项目 | 新项目 | 简单场景 |

### 10.5 监听器注册

##### 播放完成监听

```kotlin
mediaPlayer.setOnCompletionListener { mp ->
    Log.d("MediaPlayer", "Playback completed!")
    
    // 处理逻辑：
    // - 自动播放下一首
    // - 更新 UI 为"已完成"
    // - 如果 isLooping=true，会自动重新开始
    
    // 手动重播示例：
    // mp.seekTo(0)
    // mp.start()
}
```

##### 错误监听

```kotlin
mediaPlayer.setOnErrorListener { mp, what, extra ->
    Log.e("MediaPlayer", "Error occurred! what=$what, extra=$extra")
    
    // what: 错误类型
    //   - MEDIA_ERROR_UNKNOWN = 1
    //   - MEDIA_ERROR_SERVER_DIED = 100
    // extra: 额外错误信息
    //   - MEDIA_ERROR_IO = -1004
    //   - MEDIA_ERROR_MALFORMED = -1007
    //   - MEDIA_ERROR_UNSUPPORTED = -1010
    //   - MEDIA_ERROR_TIMED_OUT = -110
    
    // 返回 true 表示错误已被处理
    // 返回 false 会触发 OnCompletionListener
    
    true  // 已处理错误
}
```

##### 缓冲进度监听（网络音频）

```kotlin
mediaPlayer.setOnBufferingUpdateListener { mp, percent ->
    // percent: 0 ~ 100
    Log.d("MediaPlayer", "Buffering: $percent%")
    
    // 更新缓冲进度条
    progressBar.secondaryProgress = percent
}
```

##### 视频尺寸监听（视频播放时有用）

```kotlin
mediaPlayer.setOnVideoSizeChangedListener { mp, width, height, sarNum, sarDen ->
    Log.d("MediaPlayer", "Video size: ${width}x${height}")
    // 调整 SurfaceView/TextureView 尺寸
}
```

##### 信息监听

```kotlin
mediaPlayer.setOnInfoListener { mp, what, extra ->
    when (what) {
        MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
            Log.d("MediaPlayer", "Buffering started...")
            showLoadingIndicator(true)
        }
        MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
            Log.d("MediaPlayer", "Buffering ended!")
            showLoadingIndicator(false)
        }
        MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> {
            Log.w("MediaPlayer", "Video track lagging!")
        }
        else -> {
            Log.d("MediaPlayer", "Info: $what (extra=$extra)")
        }
    }
    false
}
```

### 10.6 资源释放（非常重要！）

```kotlin
fun releaseMediaPlayer(mediaPlayer: MediaPlayer?) {
    if (mediaPlayer == null) return
    
    try {
        // 1. 移除所有监听器（防止内存泄漏）
        mediaPlayer.setOnPreparedListener(null)
        mediaPlayer.setOnCompletionListener(null)
        mediaPlayer.setOnErrorListener(null)
        mediaPlayer.setOnBufferingUpdateListener(null)
        mediaPlayer.setOnSeekCompleteListener(null)
        mediaPlayer.setOnVideoSizeChangedListener(null)
        mediaPlayer.setOnInfoListener(null)
        
        // 2. 如果正在播放，先停止
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        
        // 3. 释放 MediaPlayer
        mediaPlayer.release()
        
        Log.d("MediaPlayer", "Released successfully!")
    } catch (e: Exception) {
        Log.e("MediaPlayer", "Release error: ${e.message}")
    }
}

// 使用
releaseMediaPlayer(mediaPlayer)
mediaPlayer = null  // 清空引用
```

**为什么必须释放？**

| 不释放的后果 | 说明 |
|-------------|------|
| **内存泄漏** | MediaPlayer 持有 Context 引用 |
| **CPU 浪费** | 后台解码线程仍在运行 |
| **音频冲突** | 可能导致其他应用无法播放音频 |
| **电池消耗** | 持续消耗电量 |
| **系统崩溃** | 严重时可能导致 ANR |

**最佳实践：在 Activity/Fragment 的 onDestroy 中释放**

```kotlin
override fun onDestroy() {
    super.onDestroy()
    
    // 释放 MediaPlayer
    releaseMediaPlayer(mediaPlayer)
    mediaPlayer = null
    
    // 取消进度追踪
    stopProgressTracking()
}
```

### 10.7 完整可运行示例

#### 示例 1：极简版（10 行代码）

```kotlin
class SimpleAudioPlayer(private val context: Context) {

    private val mediaPlayer = MediaPlayer()

    init {
        initPlayer()
    }

    private fun initPlayer() {
        try {
            // 设置音频属性
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            // 设置数据源（本地文件）
            val afd = context.assets.openFd("music.mp3")
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            // 异步准备
            mediaPlayer.prepareAsync()

            // 准备完成后自动播放
            mediaPlayer.setOnPreparedListener { it.start() }

            // 播放完成后释放
            mediaPlayer.setOnCompletionListener {
                Log.d("Player", "Completed!")
                it.release()
            }

        } catch (e: Exception) {
            Log.e("Player", "Init error: ${e.message}")
        }
    }
}
```

#### 示例 2：带完整控制的播放器类

```kotlin
class CompleteAudioPlayer(
    private val context: Context,
    private val onProgress: ((Long, Long) -> Unit)? = null,
    private val onComplete: (() -> Unit)? = null,
    private val onError: ((Int, Int) -> Unit)? = null
) {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    // ==================== 公开 API ====================

    val isPlaying: Boolean get() = mediaPlayer?.isPlaying == true

    val currentPosition: Long
        get() = try { mediaPlayer?.currentPosition?.toLong() ?: 0L } catch (_: Exception) { 0L }

    val duration: Long
        get() = try { mediaPlayer?.duration?.toLong() ?: 0L } catch (_: Exception) { 0L }

    fun setSource(url: String) {
        releaseInternal()
        initAndPrepare(Uri.parse(url))
    }

    fun setAssetSource(path: String) {
        releaseInternal()
        try {
            val afd = context.assets.openFd(path)
            initAndPrepare(assetFd = afd)
        } catch (e: Exception) {
            onError?.invoke(-1, 0)
        }
    }

    fun play() {
        mediaPlayer?.apply {
            if (!isPlaying) {
                start()
                startProgressTracking()
            }
        }
    }

    fun pause() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                stopProgressTracking()
            }
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                stopProgressTracking()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
    }

    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f))
    }

    fun setSpeed(speed: Float) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.playbackParams = PlaybackParams().apply {
                    this.speed = speed.coerceIn(0.5f, 3.0f)
                }
            } catch (e: Exception) {
                Log.e("Player", "Set speed error: ${e.message}")
            }
        }
    }

    fun release() {
        stopProgressTracking()
        releaseInternal()
    }

    // ==================== 内部实现 ====================

    private fun initAndPrepare(uri: Uri? = null, assetFd: AssetFileDescriptor? = null) {
        try {
            mediaPlayer = MediaPlayer().apply {
                // 1. 设置音频属性
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                // 2. 设置数据源
                when {
                    assetFd != null -> {
                        setDataSource(assetFd.fileDescriptor, assetFd.startOffset, assetFd.length)
                        assetFd.close()
                    }
                    uri != null -> {
                        val scheme = uri.scheme
                        if (scheme?.startsWith("http") == true) {
                            setDataSource(context, uri)
                        } else {
                            setDataSource(context, uri)
                        }
                    }
                    else -> throw IllegalArgumentException("No data source provided")
                }

                // 3. 注册监听器
                setOnPreparedListener { mp ->
                    Log.d("Player", "Prepared! Duration=${mp.duration}ms")
                }

                setOnCompletionListener {
                    Log.d("Player", "Completed!")
                    stopProgressTracking()
                    onComplete?.invoke()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("Player", "Error: what=$what, extra=$extra")
                    stopProgressTracking()
                    onError?.invoke(what, extra)
                    true
                }

                setOnBufferingUpdateListener { _, percent ->
                    Log.d("Player", "Buffering: $percent%")
                }

                // 4. 异步准备
                prepareAsync()
            }

        } catch (e: Exception) {
            Log.e("Player", "Init error: ${e.message}")
            onError?.invoke(-1, 0)
        }
    }

    private fun releaseInternal() {
        try {
            mediaPlayer?.apply {
                setOnPreparedListener(null)
                setOnCompletionListener(null)
                setOnErrorListener(null)
                setOnBufferingUpdateListener(null)
                
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val pos = try { mediaPlayer?.currentPosition?.toLong() ?: 0L } catch (_: Exception) { 0L }
                val dur = try { mediaPlayer?.duration?.toLong() ?: 0L } catch (_: Exception) { 0L }
                onProgress?.invoke(pos, dur)
                delay(200)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }
}
```

#### 示例 3：在 Activity 中使用原生 MediaPlayer

```kotlin
class NativeMusicActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var isUserSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)

        initViews()
        loadAndPlayAudio("https://example.com/music.mp3")
    }

    private fun loadAndPlayAudio(url: String) {
        releaseMediaPlayer()
        
        try {
            mediaPlayer = MediaPlayer().apply {
                // 1. 音频属性
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                // 2. 数据源
                setDataSource(url)

                // 3. 监听器
                setOnPreparedListener { mp ->
                    Log.d("NativePlayer", "Prepared! Duration=${mp.duration}ms")
                    btnPlay.isEnabled = true
                }

                setOnCompletionListener {
                    Log.d("NativePlayer", "Completed!")
                    stopProgressTracking()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("NativePlayer", "Error: $what, $extra")
                    true
                }

                setOnBufferingUpdateListener { _, percent ->
                    tvBuffering.text = "缓冲: $percent%"
                }

                // 4. 异步准备
                prepareAsync()
            }

        } catch (e: Exception) {
            Log.e("NativePlayer", "Init error: ${e.message}")
        }
    }

    // 播放控制
    fun onPlayClick() {
        mediaPlayer?.apply {
            if (!isPlaying) {
                start()
                startProgressTracking()
            }
        }
    }

    fun onPauseClick() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                stopProgressTracking()
            }
        }
    }

    fun onStopClick() {
        mediaPlayer?.stop()
        stopProgressTracking()
    }

    // 进度追踪
    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val pos = mediaPlayer?.currentPosition ?: 0
                val dur = mediaPlayer?.duration ?: 0
                
                if (!isUserSeeking && dur > 0) {
                    val progress = (pos.toFloat() / dur * 100).toInt()
                    seekBar.progress = progress
                    tvCurrent.text = formatTime(pos.toLong())
                    tvTotal.text = formatTime(dur.toLong())
                }
                
                delay(200)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    // SeekBar 拖动
    private fun initSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null && mediaPlayer != null) {
                    val dur = mediaPlayer?.duration ?: 0
                    if (dur > 0) {
                        val position = (seekBar.progress.toFloat() / 100 * dur).toLong()
                        mediaPlayer?.seekTo(position.toInt())
                    }
                }
                seekBar?.post { isUserSeeking = false }
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
        })
    }

    private fun initViews() {
        initSeekBar()
        
        // 变速控制
        seekSpeed.setOnSeekBarChangeListener { _, progress, fromUser ->
            val speed = 0.5f + (progress.toFloat() / 50 * 2.5f)
            tvSpeed.text = "%.1fx".format(speed)
            if (fromUser && android.os.Build.VERSION.SDK_INT >= 23) {
                mediaPlayer?.playbackParams = PlaybackParams().apply { this.speed = speed }
            }
        }

        // 音量控制
        seekVolume.setOnSeekBarChangeListener { _, progress, fromUser ->
            val volume = progress.toFloat() / 100
            tvVolume.text = "${progress}%"
            if (fromUser) mediaPlayer?.setVolume(volume, volume)
        }
    }

    private fun formatTime(ms: Long): String {
        val seconds = ms / 1000
        return "%02d:%02d".format(seconds / 60, seconds % 60)
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        stopProgressTracking()
        try {
            mediaPlayer?.apply {
                setOnPreparedListener(null)
                setOnCompletionListener(null)
                setOnErrorListener(null)
                setOnBufferingUpdateListener(null)
                
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
```

### 10.8 与封装类对比

| 特性 | 纯 MediaPlayer | AndroidMusicPlayer 封装 |
|------|---------------|------------------------|
| **代码量** | 较多（需手动管理） | 少（封装了细节） |
| **状态管理** | 需自己维护 | 内置状态机（9 种状态） |
| **监听器注册** | 每次手动注册 | 自动处理 |
| **进度追踪** | 自己实现 | 内置协程 |
| **异常处理** | 需全面保护 | 已内置 try-catch |
| **资源释放** | 容易遗漏 | 自动清理 |
| **灵活性** | 最高 | 受限于接口 |
| **学习成本** | 高（需理解底层） | 低（简单易用） |
| **适用场景** | 学习理解、特殊需求 | 快速开发、标准场景 |

---

## 总结

### MediaPlayer 播放音频的核心要点

1. **状态机管理**：严格遵守 MediaPlayer 的状态转换规则
2. **异步准备**：始终使用 `prepareAsync()` 而非 `prepare()`
3. **资源释放**：必须在不再使用时调用 `release()`
4. **线程安全**：所有 UI 操作应在主线程执行
5. **异常处理**：对 MediaPlayer 操作进行 try-catch 保护
6. **进度追踪**：使用协程定时器实现平滑的进度更新
7. **生命周期**：与 Activity/Fragment 生命周期绑定

### 与 Media3 (ExoPlayer) 的对比

| 特性 | MediaPlayer | ExoPlayer (Media3) |
|------|-------------|---------------------|
| **复杂度** | 简单 | 较复杂 |
| **功能** | 基础播放 | 高级功能丰富 |
| **HLS/DASH** | 不支持 | 原生支持 |
| **缓存** | 无内置缓存 | 强大的缓存机制 |
| **自定义性** | 有限 | 高度可定制 |
| **性能** | 较好 | 优秀 |
| **适用场景** | 简单音频播放 | 专业视频/流媒体应用 |

---

*文档版本：2.0*  
*生成日期：2026-06-17*  
*基于 AndroidMusicPlayer 封装类 & MediaPlayer 原生 API*
