package app.allever.android.lib.player.core

import android.net.Uri

/**
 * 待执行的 prepare 参数（当 Surface 未就绪时缓存 setSource 调用）
 *
 * 使用场景：
 * 1. 用户调用 attach(surfaceView) → Surface 还在异步创建中
 * 2. 用户立即调用 setSource(url)
 * 3. 此时 Surface 未就绪 → 存入 pendingPrepare
 * 4. surfaceCreated / onSurfaceReady 回调触发 → 自动执行缓存的 prepare
 */
class PendingPrepare(
    val uri: Uri,
    val headers: Map<String, String>?,
    val assetPath: String?
)