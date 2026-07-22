# WebSocket 学习文档

## 目录

1. [WebSocket 基础知识](#1-websocket-基础知识)
   - 1.1 WebSocket 简介
   - 1.2 与 HTTP 的区别
   - 1.3 WebSocket 握手过程
   - 1.4 WebSocket 帧结构
   - 1.5 关闭码说明

2. [WebSocket 进阶知识](#2-websocket-进阶知识)
   - 2.1 心跳机制
   - 2.2 断线重连与指数退避
   - 2.3 线程安全
   - 2.4 内存泄漏防范
   - 2.5 消息协议设计

3. [Java-WebSocket 使用教程](#3-java-websocket-使用教程)
   - 3.1 依赖配置
   - 3.2 服务端实现
   - 3.3 客户端实现
   - 3.4 消息发送与接收
   - 3.5 生命周期管理

---

## 1. WebSocket 基础知识

### 1.1 WebSocket 简介

WebSocket 是一种在单个 TCP 连接上实现全双工通信的协议。它提供了一种机制，使得客户端和服务器之间可以实时地双向传递数据，而不需要客户端不断地发起 HTTP 请求。

**核心特点：**
- 全双工通信：服务器可以主动向客户端推送消息
- 低延迟：建立连接后无需每次请求都重新握手
- 轻量级：帧头开销小，适合实时通信场景

### 1.2 与 HTTP 的区别

| 特性 | HTTP | WebSocket |
|------|------|-----------|
| 通信方向 | 客户端发起，服务器响应 | 双向通信 |
| 连接方式 | 短连接，每次请求建立新连接 | 长连接，一次握手持续通信 |
| 数据格式 | 请求-响应模式 | 帧格式，支持文本和二进制 |
| 开销 | 每次请求携带完整 HTTP 头 | 仅首次握手有 HTTP 开销 |
| 实时性 | 轮询方式，延迟高 | 实时推送，延迟低 |

### 1.3 WebSocket 握手过程

WebSocket 协议基于 HTTP 协议进行握手，握手成功后转为 WebSocket 协议。

**客户端请求：**
```http
GET /chat HTTP/1.1
Host: example.com:8080
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
Sec-WebSocket-Version: 13
```

**服务器响应：**
```http
HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
```

关键字段说明：
- `Upgrade: websocket`：声明升级协议
- `Connection: Upgrade`：确认连接升级
- `Sec-WebSocket-Key`：客户端生成的随机密钥
- `Sec-WebSocket-Accept`：服务器对密钥进行 SHA-1 哈希后的结果

### 1.4 WebSocket 帧结构

WebSocket 数据以帧（Frame）的形式传输：

```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-------+-+-------------+-------------------------------+
|F|R|R|R| opcode|M| Payload len |    Extended payload length    |
|I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
|N|V|V|V|       |S|             |   (if payload len==126/127)   |
| |1|2|3|       |K|             |                               |
+-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
|     Extended payload length continued, if payload len == 127  |
+ - - - - - - - - - - - - - - - +-------------------------------+
|                               |Masking-key, if MASK set to 1  |
+-------------------------------+-------------------------------+
| Masking-key (continued)       |          Payload Data         |
+-------------------------------- - - - - - - - - - - - - - - - +
:                     Payload Data continued ...                :
+ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
|                     Payload Data (continued)                  |
+---------------------------------------------------------------+
```

**Opcode 类型：**
- `0x0`：续帧（Continuation frame）
- `0x1`：文本帧（Text frame）
- `0x2`：二进制帧（Binary frame）
- `0x8`：关闭帧（Connection Close）
- `0x9`：Ping 帧
- `0xA`：Pong 帧

### 1.5 关闭码说明

| 关闭码 | 名称 | 说明 |
|--------|------|------|
| 1000 | Normal Closure | 正常关闭 |
| 1001 | Going Away | 端点正在离开 |
| 1002 | Protocol Error | 协议错误 |
| 1003 | Unsupported Data | 不支持的数据类型 |
| 1005 | No Status Rcvd | 未收到状态码 |
| 1006 | Abnormal Closure | 异常关闭 |
| 1007 | Invalid Payload Data | 无效的载荷数据 |
| 1008 | Policy Violation | 策略违反 |
| 1009 | Message Too Big | 消息过大 |
| 1010 | Mandatory Ext | 缺少必需的扩展 |
| 1011 | Internal Error | 服务器内部错误 |

---

## 2. WebSocket 进阶知识

### 2.1 心跳机制

心跳机制用于检测连接是否仍然活跃，防止因网络问题导致的假死连接。

**实现策略：**
- 客户端定时发送 Ping 帧
- 服务端收到 Ping 后回复 Pong 帧
- 如果客户端在指定时间内未收到 Pong，视为连接断开

**示例代码（服务端）：**
```kotlin
private const val HEARTBEAT_PING = "HEARTBEAT_PING"
private const val HEARTBEAT_PONG = "HEARTBEAT_PONG"
private const val HEARTBEAT_TIMEOUT = 45 * 1000L

override fun onMessage(conn: WebSocket, message: String) {
    val username = getUsernameByConn(conn) ?: return
    heartbeatMap[username] = System.currentTimeMillis()
    
    if (message == HEARTBEAT_PING) {
        sendMessageToClient(username, HEARTBEAT_PONG)
        return
    }
    // 处理业务消息
}
```

**心跳检测线程：**
```kotlin
private fun startHeartbeatCheck() {
    heartbeatCheckJob = scope.launch {
        while (isActive && server != null) {
            delay(15 * 1000)
            val currentTime = System.currentTimeMillis()
            val timeoutUsers = mutableListOf<String>()
            
            heartbeatMap.forEach { (username, lastActiveTime) ->
                if (currentTime - lastActiveTime > HEARTBEAT_TIMEOUT) {
                    timeoutUsers.add(username)
                }
            }
            
            timeoutUsers.forEach { username ->
                clientsMap[username]?.close()
            }
        }
    }
}
```

### 2.2 断线重连与指数退避

当连接断开时，客户端需要自动重新连接。为了避免网络风暴，应使用指数退避策略。

**指数退避算法：**
```kotlin
private val initialReconnectDelay = 1000L   // 首次重连延迟 1 秒
private val maxReconnectDelay = 30 * 1000L  // 最大重连延迟 30 秒
private var currentReconnectDelay = initialReconnectDelay

private fun tryReconnect() {
    if (isManualClose || currentUrl == null) return
    if (isWaitingReconnect) return
    
    isWaitingReconnect = true
    reconnectJob = scope.launch {
        delay(currentReconnectDelay)
        currentReconnectDelay = (currentReconnectDelay * 2).coerceAtMost(maxReconnectDelay)
        isWaitingReconnect = false
        
        if (!isManualClose && currentUrl != null) {
            createAndConnect(currentUrl!!)
        }
    }
}
```

**退避时间序列：** `1s → 2s → 4s → 8s → 16s → 30s → 30s → ...`

### 2.3 线程安全

WebSocket 服务端是多线程的，多个客户端连接会在不同线程中处理。需要注意：

1. **使用线程安全的数据结构**：
```kotlin
private val clientsMap = ConcurrentHashMap<String, WebSocket>()
private val heartbeatMap = ConcurrentHashMap<String, Long>()
```

2. **避免在迭代时修改集合**：
```kotlin
// 错误做法：迭代时修改会抛出 ConcurrentModificationException
clientsMap.forEach { (username, conn) ->
    if (timeout) conn.close()  // 可能导致问题
}

// 正确做法：先收集再处理
val timeoutUsers = clientsMap.entries
    .filter { currentTime - heartbeatMap[it.key]!! > HEARTBEAT_TIMEOUT }
    .map { it.key }
timeoutUsers.forEach { clientsMap[it]?.close() }
```

3. **双重校验锁**：
```kotlin
fun startServer(port: Int) {
    if (server != null) return
    synchronized(this) {
        if (server != null) return  // 双重校验
        server = createServer(port)
    }
}
```

### 2.4 内存泄漏防范

WebSocket 使用中常见的内存泄漏场景：

1. **Listener 引用泄漏**：使用 WeakReference 包装监听器
```kotlin
@Volatile
private var serverListener: WeakReference<ServerListener>? = null
```

2. **生命周期管理**：在 Fragment/Activity 销毁时断开连接
```kotlin
override fun onDestroy() {
    super.onDestroy()
    IMWebSocketClient.disconnect()
    IMWebSocketClient.unregisterClientListener()
}
```

3. **协程取消**：在断开连接时取消所有协程任务
```kotlin
fun disconnect() {
    reconnectJob?.cancel()
    heartbeatJob?.cancel()
    // ...
}
```

### 2.5 消息协议设计

良好的消息协议应具备：
- 清晰的消息类型划分
- 支持扩展的内容格式
- 消息去重和排序能力

**消息分类（路由维度）：**
```kotlin
enum class MessageType(val value: Int) {
    PRIVATE(1),    // 单聊
    GROUP(2),      // 群聊
    SYSTEM(3),     // 系统消息
    BROADCAST(4)   // 广播消息
}
```

**内容类型（内容维度）：**
```kotlin
object ContentType {
    const val TEXT = "text"
    const val IMAGE = "image"
    const val VOICE = "voice"
    const val VIDEO = "video"
    const val FILE = "file"
}
```

**消息结构：**
```kotlin
open class Message(
    var messageId: String = "",
    var type: MessageType = MessageType.PRIVATE,
    var contentType: String = ContentType.TEXT,
    var fromUser: String = "",
    var toUser: String = "",
    var content: String = "",
    var timestamp: Long = 0,
    var status: MessageStatus = MessageStatus.SENDING,
    var extras: MutableMap<String, Any?> = mutableMapOf()
)
```

**自定义消息扩展机制：**

项目采用了一套灵活的自定义消息扩展机制，通过 `MessageTypeDef` 接口和伴生对象绑定实现，无需修改 SDK 代码即可扩展新的消息类型。

**1. 定义扩展接口：**
```kotlin
interface MessageTypeDef {
    val contentType: String
    val clazz: Class<out Message>
}
```

**2. 创建自定义消息类：**
```kotlin
class LocationMessage(
    messageId: String = "",
    type: MessageType = MessageType.PRIVATE,
    fromUser: String = "",
    toUser: String = "",
    content: String = "",
    timestamp: Long = 0,
    status: MessageStatus = MessageStatus.SENDING,
    extras: MutableMap<String, Any?> = mutableMapOf(),
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) : Message(
    messageId = messageId,
    type = type,
    contentType = Companion.contentType,  // 引用伴生对象定义，保证一致
    fromUser = fromUser,
    toUser = toUser,
    content = content,
    timestamp = timestamp,
    status = status,
    extras = extras
) {
    companion object : MessageTypeDef {
        override val contentType = "location"
        override val clazz = LocationMessage::class.java
    }
}
```

**3. 注册自定义消息：**
```kotlin
// 在 Application 初始化时注册
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MessageProtocol.register(LocationMessage::class.java)
    }
}
```

**4. 使用自定义消息：**
```kotlin
// 发送自定义消息
val locationMsg = LocationMessage(
    type = MessageType.PRIVATE,
    fromUser = "userA",
    toUser = "userB",
    content = "北京市朝阳区",
    latitude = 39.9042,
    longitude = 116.4074
)
IMWebSocketClient.sendMessage(locationMsg)

// 接收自定义消息
override fun onMessage(message: Message) {
    when (message.contentType) {
        "location" -> handleLocation(message as LocationMessage)
        ContentType.TEXT -> handleText(message)
    }
}
```

**设计优势：**
- 编译期保证一致：构造函数引用 `Companion.contentType`，不可能不一致
- SDK 零修改：新增业务消息类型，SDK 代码完全不需要改动
- 运行时注册：无需重新编译 SDK，只需在 Application 中注册

### 2.6 安全注意事项

#### 2.6.1 当前方案的局限性

**问题**：当前 WebSocket 连接认证仅通过 URL 参数 `?username=xxx` 校验，无任何 Token 机制。

```kotlin
// 当前实现
override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
    val username = getUsernameFromHandshake(handshake)  // 仅从 URL 获取用户名
    if (username.isBlank() || !UserRepository.isUserExists(username)) {
        conn.close(1008, "用户未登录或不存在")
        return
    }
    // ...
}
```

**风险**：
- 任何人知道用户名就能冒充连接
- 无法防止恶意用户伪造身份
- 缺乏会话管理机制

#### 2.6.2 改进方案

**方案一：Token 认证（推荐）**

1. HTTP 登录接口返回 Token
2. WebSocket 握手时携带 Token
3. 服务端验证 Token 后才允许连接

```kotlin
// 客户端连接时携带 Token
IMWebSocketClient.connect("ws://192.168.0.100:5400?token=xxx&username=userA")

// 服务端验证
override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
    val token = getParameterFromHandshake(handshake, "token")
    val username = getParameterFromHandshake(handshake, "username")
    
    if (!TokenManager.validateToken(token, username)) {
        conn.close(1008, "Token 无效")
        return
    }
    // ...
}
```

**方案二：Session Cookie**

利用 HTTP Session Cookie 机制，WebSocket 握手时自动携带 Cookie。

```kotlin
// HTTP 登录时设置 Session
session.setAttribute("username", username)

// WebSocket 握手时验证 Session
override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
    val sessionId = getCookieFromHandshake(handshake, "JSESSIONID")
    val httpSession = HttpSessionManager.getSession(sessionId)
    
    if (httpSession == null || httpSession.getAttribute("username") == null) {
        conn.close(1008, "未登录")
        return
    }
    // ...
}
```

#### 2.6.3 其他安全建议

1. **消息签名**：对重要消息进行签名，防止篡改
2. **消息加密**：敏感内容使用加密传输
3. **连接限流**：限制单 IP 连接数，防止 DDOS 攻击
4. **输入验证**：对所有用户输入进行严格验证，防止注入攻击

---

## 3. Java-WebSocket 使用教程

### 3.1 依赖配置

在 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
}
```

### 3.2 服务端实现

**基础服务端模板：**

```kotlin
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class MyWebSocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {
    
    override fun onStart() {
        println("服务端已启动，监听端口: $port")
    }
    
    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        println("新客户端连接: ${conn.remoteSocketAddress}")
    }
    
    override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
        println("客户端断开连接: ${conn.remoteSocketAddress}, code: $code")
    }
    
    override fun onMessage(conn: WebSocket, message: String) {
        println("收到消息: $message")
        // 回复消息
        conn.send("收到: $message")
    }
    
    override fun onError(conn: WebSocket?, ex: Exception) {
        println("连接异常: ${ex.message}")
    }
}

// 启动服务端
fun main() {
    val server = MyWebSocketServer(8080)
    server.start()
}
```

**完整服务端示例（IMWebSocketServer）：**

```kotlin
object IMWebSocketServer {
    private var server: WebSocketServer? = null
    private val clientsMap = ConcurrentHashMap<String, WebSocket>()
    private val connUsernameMap = ConcurrentHashMap<WebSocket, String>()
    private val heartbeatMap = ConcurrentHashMap<String, Long>()
    
    fun startServer(port: Int) {
        if (server != null) return
        synchronized(this) {
            if (server != null) return
            
            val newServer = object : WebSocketServer(InetSocketAddress(port)) {
                override fun onStart() {
                    println("IM 服务端已启动")
                    startHeartbeatCheck()
                }
                
                override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
                    val username = getUsernameFromHandshake(handshake)
                    clientsMap[username] = conn
                    connUsernameMap[conn] = username
                    heartbeatMap[username] = System.currentTimeMillis()
                }
                
                override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
                    val username = connUsernameMap[conn]
                    clientsMap.remove(username)
                    connUsernameMap.remove(conn)
                    heartbeatMap.remove(username)
                }
                
                override fun onMessage(conn: WebSocket, message: String) {
                    val username = connUsernameMap[conn] ?: return
                    heartbeatMap[username] = System.currentTimeMillis()
                    
                    if (message == HEARTBEAT_PING) {
                        conn.send(HEARTBEAT_PONG)
                        return
                    }
                    
                    // 处理业务消息
                    processMessage(username, message, conn)
                }
                
                override fun onError(conn: WebSocket?, ex: Exception) {
                    println("连接异常: ${ex.message}")
                }
            }
            
            server = newServer
            newServer.start()
        }
    }
    
    fun stopServer() {
        server?.stop()
        clientsMap.clear()
        connUsernameMap.clear()
        heartbeatMap.clear()
        server = null
    }
}
```

### 3.3 客户端实现

**基础客户端模板：**

```kotlin
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MyWebSocketClient(uri: URI) : WebSocketClient(uri) {
    
    override fun onOpen(handshakedata: ServerHandshake?) {
        println("连接成功")
    }
    
    override fun onMessage(message: String?) {
        println("收到消息: $message")
    }
    
    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("连接关闭: code=$code, reason=$reason")
    }
    
    override fun onError(ex: Exception?) {
        println("连接错误: ${ex?.message}")
    }
}

// 使用客户端
fun main() {
    val client = MyWebSocketClient(URI("ws://localhost:8080"))
    client.connect()
    
    // 发送消息
    client.send("Hello, WebSocket!")
    
    // 关闭连接
    // client.close()
}
```

**完整客户端示例（IMWebSocketClient）：**

```kotlin
object IMWebSocketClient {
    private var client: WebSocketClient? = null
    private var currentUrl: String? = null
    private var isManualClose = false
    
    fun connect(url: String) {
        if (client?.isOpen == true) return
        
        currentUrl = url
        isManualClose = false
        
        client = object : WebSocketClient(URI(url)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                println("连接成功")
                startHeartbeat()
            }
            
            override fun onMessage(message: String?) {
                if (message == HEARTBEAT_PONG) {
                    lastPongTime = System.currentTimeMillis()
                    return
                }
                // 处理消息
                processMessage(message)
            }
            
            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                println("连接关闭")
                if (!isManualClose) tryReconnect()
            }
            
            override fun onError(ex: Exception?) {
                println("连接错误")
                if (!isManualClose) tryReconnect()
            }
        }
        
        client?.connectBlocking()
    }
    
    fun disconnect() {
        isManualClose = true
        client?.closeBlocking()
        client = null
    }
    
    fun sendMessage(message: String): Boolean {
        return if (client?.isOpen == true) {
            client?.send(message)
            true
        } else {
            false
        }
    }
}
```

### 3.4 消息发送与接收

**发送消息：**

```kotlin
// 发送文本消息
IMWebSocketClient.sendMessage("Hello, World!")

// 发送对象消息（JSON 序列化）
val msg = MessageBuilder()
    .type(MessageType.PRIVATE)
    .fromUser("userA")
    .toUser("userB")
    .content("你好")
    .buildText()
IMWebSocketClient.sendMessage(msg)
```

**接收消息：**

```kotlin
override fun onMessage(message: String) {
    if (message.startsWith("{")) {
        try {
            val msg = Message.fromJson(message)
            handleMessage(msg)
        } catch (e: Exception) {
            // 解析失败，作为普通文本处理
            handleTextMessage(message)
        }
    } else {
        handleTextMessage(message)
    }
}

private fun handleMessage(msg: Message) {
    when (msg.type) {
        MessageType.PRIVATE -> showPrivateMessage(msg)
        MessageType.GROUP -> showGroupMessage(msg)
        MessageType.SYSTEM -> showSystemMessage(msg)
        MessageType.BROADCAST -> showBroadcastMessage(msg)
    }
}
```

### 3.5 生命周期管理

**服务端生命周期：**

```kotlin
// 启动服务
IMWebSocketServer.startServer(5400)

// 停止服务
IMWebSocketServer.stopServer()

// 检查状态
IMWebSocketServer.isStarted()
```

**客户端生命周期：**

```kotlin
// 连接服务端
IMWebSocketClient.connect("ws://192.168.0.100:5400?username=userA")

// 断开连接
IMWebSocketClient.disconnect()

// 检查连接状态
IMWebSocketClient.isConnected()
```

**Activity/Fragment 中的使用：**

```kotlin
class ChatFragment : Fragment() {
    
    override fun onStart() {
        super.onStart()
        IMWebSocketClient.connect("ws://192.168.0.100:5400?username=userA")
        IMWebSocketClient.registerClientListener(this)
    }
    
    override fun onStop() {
        super.onStop()
        IMWebSocketClient.disconnect()
        IMWebSocketClient.unregisterClientListener()
    }
}
```

---

## 附录：快速验证清单

在运行 WebSocket 应用之前，请确认以下配置是否正确：

### 环境配置

- [ ] 确认设备已连接到网络（局域网或 WiFi）
- [ ] 确认服务端和客户端在同一网络下
- [ ] 获取服务端设备的 IP 地址（使用 `ipconfig` 或 `ifconfig`）

### 依赖版本

- [ ] Java-WebSocket 版本 >= 1.5.6
- [ ] Gson 版本 >= 2.9.0
- [ ] Kotlin 版本 >= 1.8.0
- [ ] Kotlin Coroutines 版本 >= 1.6.0

### 权限声明（AndroidManifest.xml）

```xml
<!-- 网络权限 -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 访问网络状态 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 访问 WiFi 状态 -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

### 服务端配置

- [ ] 确认端口未被占用（默认 5400）
- [ ] 确认服务端已启动（日志显示 "IM 服务端已启动"）
- [ ] 确认防火墙未阻止端口访问

### 客户端配置

- [ ] 确认连接 URL 格式正确：`ws://IP:端口?username=xxx`
- [ ] 确认用户名已注册（通过 HTTP 注册接口）
- [ ] 确认连接回调正常触发

### 常见问题排查

| 问题现象 | 可能原因 | 解决方法 |
|----------|----------|----------|
| 连接被拒绝 | 服务端未启动 | 检查服务端日志，确认服务已启动 |
| 连接超时 | 网络不通 | 检查设备是否在同一网络，尝试 ping 测试 |
| 端口被占用 | 端口冲突 | 更换端口或关闭占用端口的程序 |
| 消息发送失败 | 连接未建立 | 等待 `onOpen` 回调后再发送消息 |
| 心跳超时 | 网络不稳定 | 增加心跳超时时间或检查网络质量 |

### 测试步骤

1. **启动服务端**：调用 `IMWebSocketServer.startServer(5400)`
2. **注册用户**：调用 HTTP 接口 `/api/user/register`
3. **客户端连接**：调用 `IMWebSocketClient.connect("ws://IP:5400?username=xxx")`
4. **发送消息**：调用 `IMWebSocketClient.sendTextMessage("Hello")`
5. **验证接收**：检查 `onMessage` 回调是否收到消息

---

## 附录：常用 API 参考

### Java-WebSocket 服务端 API

| 方法 | 说明 |
|------|------|
| `WebSocketServer(InetSocketAddress)` | 创建服务端 |
| `start()` | 启动服务 |
| `stop()` | 停止服务 |
| `broadcast(String)` | 广播消息给所有客户端 |
| `broadcast(String, Collection<WebSocket>)` | 广播消息给指定客户端 |

### WebSocket 连接 API

| 方法 | 说明 |
|------|------|
| `send(String)` | 发送文本消息 |
| `send(ByteBuffer)` | 发送二进制消息 |
| `close()` | 关闭连接 |
| `close(int code, String reason)` | 指定关闭码和原因 |
| `isOpen()` | 检查连接是否打开 |

### Java-WebSocket 客户端 API

| 方法 | 说明 |
|------|------|
| `WebSocketClient(URI)` | 创建客户端 |
| `connect()` | 异步连接 |
| `connectBlocking()` | 同步连接 |
| `close()` | 关闭连接 |
| `closeBlocking()` | 同步关闭 |
| `isOpen()` | 检查连接状态 |