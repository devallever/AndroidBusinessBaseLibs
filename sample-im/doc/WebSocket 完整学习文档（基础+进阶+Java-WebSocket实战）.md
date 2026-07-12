# WebSocket 完整学习文档（基础\+进阶\+Java\-WebSocket实战）

# 目录

- [1\. 基础概念](https://www.doubao.cn)

- [2\. WebSocket vs HTTP 核心区别](https://www.doubao.cn)

- [3\. WebSocket 握手原理](https://www.doubao.cn)

- [4\. WebSocket 协议帧详解](https://www.doubao.cn)

- [5\. 进阶核心机制（心跳、超时、重连）](https://www.doubao.cn)

- [6\. Java\-WebSocket 框架介绍](https://www.doubao.cn)

- [7\. 服务端完整实战（Android可用）](https://www.doubao.cn)

- [8\. 客户端完整实战（Android可用）](https://www.doubao.cn)

- [9\. 常见 Bug 与生产避坑指南](https://www.doubao.cn)

- [10\. 生产最佳实践](https://www.doubao.cn)

# 1\. 基础概念

## 1\.1 什么是 WebSocket

WebSocket 是一种 **全双工、长连接、基于 TCP 的应用层协议**，在单个 TCP 连接上实现客户端与服务端双向实时数据传输。

**核心特性**：

- 一次握手，永久连接（直至主动断开/网络异常）

- 全双工：服务端可主动推消息，无需客户端轮询

- 协议轻量：头部极小，开销远低于 HTTP

- 同源友好、支持跨域

## 1\.2 适用场景

- 即时通讯：聊天室、私聊、消息推送

- 实时数据：股票、行情、设备状态、日志实时打印

- 在线协同：在线文档、联机游戏

- 物联网设备长连接通信

# 2\. WebSocket vs HTTP

|特性|HTTP|WebSocket|
|---|---|---|
|连接模式|短连接，请求响应后断开|长连接，一次握手持续通信|
|通信方向|单向：客户端请求、服务端响应|全双工：双方随时互发|
|头部开销|大，每次请求携带 Header|极小，二进制帧头部|
|实时性|差，依赖轮询|极高，实时推送|
|状态|无状态|有状态，维持连接会话|

## 2\.1 为什么不用 HTTP 轮询？

- 轮询频繁创建销毁连接，资源消耗大

- 延迟高、实时性差

- 大量无效空请求，浪费带宽和服务端性能

# 3\. WebSocket 握手原理

WebSocket **基于 HTTP 协议完成握手**，握手成功后升级为 WS 协议通信。

## 3\.1 握手流程

1. 客户端发送 **HTTP 升级请求**，携带固定 Header

2. 服务端校验 Header，返回 101 Switching Protocols

3. TCP 连接保留，协议正式升级为 WebSocket

## 3\.2 客户端握手请求头（核心）

```http
GET ws://192.168.1.100:5400 HTTP/1.1
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: xxxxxxxx==
Sec-WebSocket-Version: 13

```

## 3\.3 服务端响应头

```http
HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: yyyyyyyy==

```

## 3\.4 Sec\-WebSocket\-Accept 算法

服务端将客户端 Key 拼接固定 GUID：`258EAFA5-E914-47DA-95CA-C5AB0DC85B11`，SHA1 加密后 Base64 编码返回，客户端校验一致则握手成功。

# 4\. WebSocket 协议帧详解

握手结束后，所有数据以 **二进制帧** 传输，不再是 HTTP 报文。

## 4\.1 帧结构关键字段

- **FIN**：是否为最后一帧（分包传输使用）

- **Opcode**：帧类型
        

    - 0x0：继续帧

    - 0x1：文本帧

    - 0x2：二进制帧

    - 0x8：关闭帧

    - 0x9：Ping 心跳

    - 0xA：Pong 心跳应答

- **MASK**：客户端发送数据必须掩码，服务端可不掩码

- **Payload**：真实消息内容

## 4\.2 心跳帧作用

TCP 连接静默时，系统不会主动断开死连接，导致**僵尸连接堆积**。
Ping/Pong 心跳用于：

- 检测对方存活状态

- 穿透防火墙空闲连接回收策略

- 主动清理超时无效连接

# 5\. 进阶核心机制（生产必备）

## 5\.1 心跳保活机制

**标准方案（你当前项目采用的最优方案）**：

- 客户端：30s 发送一次 Ping

- 服务端：收到 Ping 立即回复 Pong

- 服务端：45s 无任何消息判定超时踢线

- 客户端：90s 无 Pong 判定服务端掉线，触发重连

## 5\.2 断线重连机制（指数退避）

避免网络抖动时频繁重连打崩服务端：

- 初始 1s、2s、4s、8s 递增

- 最大延迟限制 30s

- 主动断开不重连，被动断连自动重连

## 5\.3 连接状态管理

必须区分四种状态：

- 未连接

- 连接中

- 已连接

- 主动断开/被动断开

# 6\. Java\-WebSocket 框架介绍

## 6\.1 简介

`Java-WebSocket` 是轻量、高性能、纯 Java 实现的 WebSocket 框架，**无第三方依赖、适配 Android**，非常适合移动端搭建 WS 服务端/客户端。

## 6\.2 Gradle 依赖

```gradle
implementation 'org.java-websocket:Java-WebSocket:1.5.5'

```

## 6\.3 核心类

- `WebSocketServer`：服务端基类

- `WebSocketClient`：客户端基类

- `WebSocket`：单个连接会话

# 7\. 服务端完整实战（优化最终版）

基于你之前的代码，修复所有竞态、私聊 Bug、并发问题，**可直接生产使用**。

```kotlin
package app.allever.android.sample.im.websocket.server

import android.util.Log
import kotlinx.coroutines.*
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.ref.WeakReference
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentHashMap

/**
 * 稳定版 WebSocket 服务端
 * 修复：竞态启动、私聊解析冲突、并发修改、发送异常、内存泄漏
 */
object IMWebSocketServer {
    private val TAG = IMWebSocketServer::class.java.simpleName
    private var server: WebSocketServer? = null

    @Volatile
    private var port: Int = 5400
    private val clientsMap = ConcurrentHashMap<String, WebSocket>()
    private val heartbeatMap = ConcurrentHashMap<String, Long>()

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var serverListener: WeakReference<ServerListener>? = null

    // 心跳常量
    private const val HEARTBEAT_PING = "HEARTBEAT_PING"
    private const val HEARTBEAT_PONG = "HEARTBEAT_PONG"
    private const val HEARTBEAT_TIMEOUT = 45 * 1000L

    private var heartbeatCheckJob: Job? = null

    fun startServer(port: Int) {
        if (server != null) {
            log("服务端已经在运行中，请勿重复启动")
            return
        }
        synchronized(this) {
            if (server != null) return
            this.port = port

            val newServer = object : WebSocketServer(InetSocketAddress(port)) {
                override fun onStart() {
                    log("IM 服务端已启动，监听端口: $port")
                    val url = getConnectUrl()
                    log("IM 服务端已启动，监听地址: $url")
                    scope.launch(Dispatchers.Main) {
                        serverListener?.get()?.onStarted(url)
                    }
                    startHeartbeatCheck()
                }

                override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
                    val clientId = getClientId(conn)
                    clientsMap[clientId] = conn
                    heartbeatMap[clientId] = System.currentTimeMillis()
                    log("客户端连接成功: $clientId, 在线人数: ${clientsMap.size}")
                    sendMessageToClient(clientId, "系统消息：欢迎加入聊天室！")
                    broadcastToOthers("系统消息：$clientId 上线了", conn)
                }

                override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
                    val clientId = getClientId(conn)
                    clientsMap.remove(clientId)
                    heartbeatMap.remove(clientId)
                    log("客户端断开连接: $clientId, 剩余在线人数: ${clientsMap.size}")
                    broadcastToOthers("系统消息：$clientId 离线了", conn)
                }

                override fun onMessage(conn: WebSocket, message: String) {
                    val clientId = getClientId(conn)
                    heartbeatMap[clientId] = System.currentTimeMillis()

                    if (message == HEARTBEAT_PING) {
                        log("收到 $clientId 心跳包")
                        sendMessageToClient(clientId, HEARTBEAT_PONG)
                        return
                    }
                    log("收到 $clientId 消息: $message")
                    processMessage(clientId, message, conn)
                }

                override fun onError(conn: WebSocket?, ex: Exception) {
                    val clientId = conn?.let { getClientId(it) } ?: "全局"
                    if (ex is java.net.BindException) {
                        logE("端口 $port 被占用，启动失败")
                        synchronized(this@IMWebSocketServer) {
                            server = null
                        }
                    } else {
                        logE("异常($clientId): ${ex.message}")
                    }
                }
            }
            newServer.isReuseAddr = true
            server = newServer

            scope.launch {
                try {
                    newServer.start()
                } catch (e: Exception) {
                    synchronized(this@IMWebSocketServer) {
                        if (server === newServer) server = null
                    }
                    logE("服务启动失败: ${e.message}")
                }
            }
        }
    }

    private fun startHeartbeatCheck() {
        heartbeatCheckJob?.cancel()
        heartbeatCheckJob = scope.launch {
            while (isActive && server != null) {
                delay(15 * 1000)
                val currentTime = System.currentTimeMillis()
                val timeoutList = heartbeatMap.filter {
                    currentTime - it.value > HEARTBEAT_TIMEOUT
                }.keys

                timeoutList.forEach { clientId ->
                    logE("心跳超时，断开僵尸连接: $clientId")
                    clientsMap[clientId]?.close()
                }
            }
        }
    }

    private fun processMessage(clientId: String, message: String, senderConn: WebSocket) {
        // 修复私聊冲突：使用第二个冒号分割
        if (message.startsWith("@")) {
            val firstColon = message.indexOf(":", 1)
            if (firstColon == -1) return
            val secondColon = message.indexOf(":", firstColon + 1)
            if (secondColon == -1) return

            val targetId = message.substring(1, secondColon)
            val realMsg = message.substring(secondColon + 1).trim()
            sendPrivateMessage(targetId, "$clientId 私聊你: $realMsg")
        } else {
            broadcastToOthers("$clientId: $message", senderConn)
        }
    }

    private fun getClientId(conn: WebSocket): String {
        val address = conn.remoteSocketAddress ?: return "unknown"
        return "${address.address.hostAddress}:${address.port}"
    }

    fun sendMessageToClient(clientId: String, message: String) {
        clientsMap[clientId]?.takeIf { it.isOpen }?.let { conn ->
            try {
                conn.send(message)
            } catch (e: Exception) {
                logE("发送消息失败 $clientId: ${e.message}")
            }
        }
    }

    fun sendPrivateMessage(targetClientId: String, message: String) = sendMessageToClient(targetClientId, message)

    private fun broadcastToOthers(message: String, senderConn: WebSocket) {
        val targets = clientsMap.values.filter { it != senderConn && it.isOpen }
        if (targets.isNotEmpty()) {
            try {
                server?.broadcast(message, targets)
            } catch (e: Exception) {
                logE("广播消息异常: ${e.message}")
            }
        }
    }

    fun stopServer() {
        if (!isStarted()) return
        scope.launch {
            val currentServer = server ?: return@launch
            heartbeatCheckJob?.cancel()
            heartbeatCheckJob = null

            clientsMap.values.forEach {
                try { it.close() } catch (_: Exception) {}
            }
            clientsMap.clear()
            heartbeatMap.clear()

            try {
                currentServer.stop(1000)
            } catch (e: Exception) {
                logE("停止服务异常: ${e.message}")
            } finally {
                synchronized(this@IMWebSocketServer) {
                    server = null
                }
            }

            log("IM 服务端已关闭")
            scope.launch(Dispatchers.Main) { serverListener?.get()?.onStopped() }
        }
    }

    fun isStarted(): Boolean = server != null
    fun getOnlineCount(): Int = clientsMap.size

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val ni = interfaces.nextElement()
                if (ni.isLoopback || !ni.isUp) continue
                val addresses = ni.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return "未知IP"
    }

    fun getConnectUrl(): String = if (server != null) "ws://${getLocalIpAddress()}:$port" else ""

    private fun log(message: String) {
        Log.d(TAG, message)
        scope.launch(Dispatchers.Main) { serverListener?.get()?.onLog(message) }
    }

    private fun logE(message: String) {
        Log.e(TAG, message)
        scope.launch(Dispatchers.Main) { serverListener?.get()?.onLog(message) }
    }

    fun registerServerListener(listener: ServerListener?) {
        serverListener = WeakReference(listener)
    }

    fun unregisterServerListener() {
        serverListener = null
    }

    interface ServerListener {
        fun onLog(log: String)
        fun onStarted(url: String)
        fun onStopped()
    }
}

```

# 8\. 客户端完整实战（优化最终版）

```kotlin
package app.allever.android.sample.im.websocket.client

import android.util.Log
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.ref.WeakReference
import java.net.URI

object IMWebSocketClient {
    private val TAG = IMWebSocketClient::class.java.simpleName
    private var client: WebSocketClient? = null

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var clientListener: WeakReference<ClientListener>? = null

    @Volatile
    private var currentUrl: String? = null
    @Volatile
    private var isManualClose = false
    @Volatile
    private var isWaitingReconnect = false

    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null

    @Volatile
    private var lastPongTime: Long = 0L

    private const val HEARTBEAT_INTERVAL = 30 * 1000L
    private const val HEARTBEAT_TIMEOUT = 90 * 1000L
    private const val HEARTBEAT_PING = "HEARTBEAT_PING"
    private const val HEARTBEAT_PONG = "HEARTBEAT_PONG"

    private val initialReconnectDelay = 1000L
    private val maxReconnectDelay = 30 * 1000L
    @Volatile
    private var currentReconnectDelay = initialReconnectDelay

    fun connect(url: String) {
        if (isConnected()) {
            log("已连接，无需重连")
            return
        }
        synchronized(this) {
            if (isConnected()) return
            currentUrl = url
            isManualClose = false
            currentReconnectDelay = initialReconnectDelay
            isWaitingReconnect = false
            reconnectJob?.cancel()
            scope.launch { createAndConnect(url) }
        }
    }

    private fun createAndConnect(url: String) {
        // 销毁旧连接
        client?.let { old ->
            if (old.isOpen) try { old.closeBlocking(1000) } catch (_: Exception) {}
        }

        val uri = try {
            URI(url)
        } catch (e: Exception) {
            logE("地址格式错误: ${e.message}")
            tryReconnect()
            return
        }

        client = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                currentReconnectDelay = initialReconnectDelay
                log("连接服务端成功: $url")
                lastPongTime = System.currentTimeMillis()
                startHeartbeat()
                notifyOpen()
            }

            override fun onMessage(message: String?) {
                if (message == HEARTBEAT_PONG) {
                    lastPongTime = System.currentTimeMillis()
                    return
                }
                message?.let { notifyMessage(it) }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                log("连接关闭 code=$code")
                heartbeatJob?.cancel()
                notifyClose(code, reason, remote)
                if (!isManualClose) tryReconnect()
            }

            override fun onError(ex: Exception?) {
                logE("连接异常: ${ex?.message}")
                notifyError(ex)
                if (!isManualClose) tryReconnect()
            }
        }

        try {
            val success = client?.connectBlocking() ?: false
            if (!success && !isManualClose) {
                logE("连接失败，准备重连")
                tryReconnect()
            }
        } catch (e: Exception) {
            logE("连接异常: ${e.message}")
            tryReconnect()
        }
    }

    private fun tryReconnect() {
        if (isManualClose || currentUrl.isNullOrEmpty() || isWaitingReconnect) return
        isWaitingReconnect = true

        reconnectJob = scope.launch {
            try {
                log("${currentReconnectDelay / 1000}s 后重连")
                delay(currentReconnectDelay)
                currentReconnectDelay = (currentReconnectDelay * 2).coerceAtMost(maxReconnectDelay)
                isWaitingReconnect = false
                currentUrl?.let { createAndConnect(it) }
            } catch (_: CancellationException) {
                log("重连任务取消")
            } catch (e: Exception) {
                logE("重连异常: ${e.message}")
            } finally {
                isWaitingReconnect = false
            }
        }
    }

    fun sendMessage(message: String): Boolean {
        val curr = client ?: return false
        if (!curr.isOpen) return false
        return try {
            curr.send(message)
            log("发送消息: $message")
            true
        } catch (e: Exception) {
            logE("发送失败: ${e.message}")
            false
        }
    }

    fun disconnect() {
        isManualClose = true
        reconnectJob?.cancel()
        heartbeatJob?.cancel()
        currentReconnectDelay = initialReconnectDelay
        isWaitingReconnect = false

        scope.launch {
            val curr = client ?: return@launch
            if (curr.isOpen) {
                try {
                    curr.closeBlocking(1000)
                } catch (_: Exception) {}
            }
            client = null
            log("主动断开连接")
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive && !isManualClose) {
                delay(HEARTBEAT_INTERVAL)
                val curr = client ?: continue
                if (!curr.isOpen) continue

                try {
                    curr.send(HEARTBEAT_PING)
                } catch (e: Exception) {
                    logE("心跳发送失败，断开连接")
                    curr.close()
                    break
                }

                if (System.currentTimeMillis() - lastPongTime > HEARTBEAT_TIMEOUT) {
                    logE("心跳超时")
                    curr.close()
                    break
                }
            }
        }
    }

    fun isConnected(): Boolean {
        val curr = client ?: return false
        return curr.isOpen
    }

    fun registerClientListener(listener: ClientListener?) {
        clientListener = WeakReference(listener)
    }

    fun unregisterClientListener() {
        clientListener = null
    }

    private fun notifyOpen() = scope.launch(Dispatchers.Main) { clientListener?.get()?.onOpen() }
    private fun notifyMessage(msg: String) = scope.launch(Dispatchers.Main) { clientListener?.get()?.onMessage(msg) }
    private fun notifyClose(code: Int, reason: String?, remote: Boolean) = scope.launch(Dispatchers.Main) { clientListener?.get()?.onClose(code, reason, remote) }
    private fun notifyError(ex: Exception?) = scope.launch(Dispatchers.Main) { clientListener?.get()?.onError(ex) }

    private fun log(msg: String) {
        Log.d(TAG, msg)
        scope.launch(Dispatchers.Main) { clientListener?.get()?.onLog(msg) }
    }

    private fun logE(msg: String) {
        Log.e(TAG, msg)
        scope.launch(Dispatchers.Main) { clientListener?.get()?.onLog(msg) }
    }

    interface ClientListener {
        fun onLog(log: String)
        fun onOpen()
        fun onMessage(message: String)
        fun onClose(code: Int, reason: String?, remote: Boolean)
        fun onError(ex: Exception?)
    }

    fun destroy() {
        disconnect()
        scope.cancel()
    }
}

```

# 9\. 常见 Bug 与避坑指南

## 9\.1 端口占用无法重启服务

解决方案：开启 `isReuseAddr = true` \+ 异常时主动置空 server 引用。

## 9\.2 私聊失效（IP:Port 冒号冲突）

错误：只取第一个冒号

    正确：**取第二个冒号作为分隔符**

## 9\.3 发送消息崩溃

原因：半关闭状态下 send 抛异常

    解决方案：所有 send 操作必须 try\-catch

## 9\.4 内存泄漏

解决方案：Listener 弱引用、页面销毁 cancel 所有 Job、主动断开清空资源

## 9\.5 并发重连、重复创建连接

解决方案：同步锁 \+ 状态位防并发

# 10\. 生产最佳实践总结

1. **必须做心跳保活**，否则大量僵尸连接堆积

2. **必须做指数退避重连**，防止雪崩

3. **所有网络发送必须捕获异常**

4. **所有状态修改必须保证线程安全**（Volatile \+ 同步锁）

5. **客户端主动断开不重连，被动断连自动重连**

6. **服务端定时清理超时连接**

7. **禁止在回调中直接操作 UI**，统一切主线程

8. **页面销毁释放协程、连接、监听**

# 11\. 扩展学习方向

- WS 消息 JSON 协议标准化（type、from、to、content、time）

- 接入 Android 前台 Service 保活长连接

- 消息防抖、限流、防刷屏

- 断线消息队列补发

- SSL 加密 wss 协议改造

- 用户 userId 绑定连接，替代 IP:Port 客户端 ID

> （注：部分内容可能由 AI 生成）
