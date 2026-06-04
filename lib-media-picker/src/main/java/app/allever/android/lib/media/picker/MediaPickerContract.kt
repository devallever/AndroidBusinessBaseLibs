package app.allever.android.lib.media.picker

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.ui.MediaPickerActivity

/**
 * 媒体选择器 ActivityResultContract
 *
 * 使用方式：
 * ```kotlin
 * val picker = registerForActivityResult(MediaPickerContract()) { items ->
 *     // items: List<MediaItem>
 * }
 * picker.launch(MediaPickerConfig(
 *     types = setOf(MediaType.Type.IMAGE, MediaType.Type.VIDEO),
 *     maxSelect = 9,
 * ))
 * ```
 */
class MediaPickerContract : ActivityResultContract<MediaPickerConfig, List<MediaItem>>() {

    override fun createIntent(context: Context, input: MediaPickerConfig): Intent {
        return Intent(context, MediaPickerActivity::class.java).apply {
            putExtra(MediaPickerConfig.KEY_CONFIG, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<MediaItem> {
        if (resultCode != android.app.Activity.RESULT_OK || intent == null) return emptyList()
        @Suppress("DEPRECATION")
        return intent.getParcelableArrayListExtra(MediaPickerConfig.KEY_RESULT) ?: emptyList()
    }
}
