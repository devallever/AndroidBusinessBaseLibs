package app.allever.android.lib.player.core

/**
 * 播放器状态机
 */
enum class PlayerState {
    IDLE,           // 空闲
    PREPARING,      // 准备中
    PREPARED,       // 准备就绪（可播放）
    PLAYING,        // 播放中
    PAUSED,         // 已暂停
    STOPPED,        // 已停止
    COMPLETED,      // 播放完成
    ERROR,          // 出错
    RELEASED        // 已释放（终态）
}
