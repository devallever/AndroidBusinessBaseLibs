package app.allever.android.lib.media.picker.ui

import android.content.Intent
import app.allever.android.lib.core.base.AbstractBindingActivity
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.R
import app.allever.android.lib.media.picker.databinding.ActivityMediaPickerBinding

/**
 * 媒体选择器入口 Activity
 * 托管 MediaPickerFragment，处理结果返回
 */
class MediaPickerActivity : AbstractBindingActivity<ActivityMediaPickerBinding>() {

    private lateinit var config: MediaPickerConfig

    override fun inflate(): ActivityMediaPickerBinding = ActivityMediaPickerBinding.inflate(layoutInflater)

    override fun init() {
        adaptStatusBar(mBinding.fragmentContainer)
        config = intent?.getParcelableExtra(MediaPickerConfig.KEY_CONFIG) ?: MediaPickerConfig()

        val fragment = MediaPickerFragment.newInstance(config).apply {
            onConfirm = { items ->
                returnResult(items)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commitNow()

        // 设置标题
        title = buildString {
            append("选择媒体")
            if (config.maxSelect > 0) append(" (最多${config.maxSelect}个)")
        }
    }

    private fun returnResult(items: List<MediaItem>) {
        val result = Intent().apply {
            putParcelableArrayListExtra(MediaPickerConfig.KEY_RESULT, ArrayList(items))
        }
        items.forEach {
            log("选中媒体: ${it.toJson()}")
        }
        setResult(RESULT_OK, result)
        finish()
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}
