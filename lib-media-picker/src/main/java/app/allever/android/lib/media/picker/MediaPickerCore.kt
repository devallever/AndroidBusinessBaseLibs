package app.allever.android.lib.media.picker

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType

object MediaPickerCore {

    /**
     * 在 Fragment/Activity 的 onCreate() 或初始化阶段调用，返回 Launcher
     */
    fun <I, O> register(
        call: ActivityResultCaller,
        contract: ActivityResultContract<I, O>,
        callback: (O) -> Unit
    ): ActivityResultLauncher<I> {
        return call.registerForActivityResult(contract) { callback(it) }
    }

    /**
     * 注册媒体选择器 Launcher（在 onCreate 阶段调用）
     */
    fun registerPickerLauncher(
        call: ActivityResultCaller,
        callback: (List<MediaItem>) -> Unit
    ): ActivityResultLauncher<MediaPickerConfig> {
        return register(call, MediaPickerContract(), callback)
    }

    fun launchVideo(launcher: ActivityResultLauncher<MediaPickerConfig>, max: Int = 1) {
        launcher.launch(MediaPickerConfig(setOf(MediaType.Type.VIDEO), max))
    }

    /**
     * 直接启动（已废弃，保留兼容）
     * @Deprecated 使用 registerPicker() 替代
     */
    @Deprecated("请在 onCreate 阶段使用 registerPicker() 获取 Launcher 后调用 launcher.launch()")
    fun start(call: ActivityResultCaller, config: MediaPickerConfig, callback: (List<MediaItem>) -> Unit) {
        val launcher = call.registerForActivityResult(
            MediaPickerContract()
        ) {
            callback(it)
            return@registerForActivityResult
        }
        launcher.launch(config)
    }
}