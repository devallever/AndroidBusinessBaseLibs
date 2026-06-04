package app.allever.android.sample.media.core

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.media.core.model.MediaType
import com.chad.library.adapter.base.BaseQuickAdapter

class MediaPickerSampleFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        // 媒体选择器
        TextClickItem("【选择器】打开媒体选择器（图片+视频+音频，最多9个）") { openMediaPickerAllTypes() },
        TextClickItem("【选择器】仅选图片（最多6个）") { openMediaPickerImageOnly() },
        TextClickItem("【选择器】仅选视频（最多3个）") { openMediaPickerVideoOnly() },
    )

    // ==================== 媒体选择器示例 ====================

    private val mediaPickerLauncher = registerForActivityResult(
        app.allever.android.lib.media.picker.MediaPickerContract()
    ) { items ->
        if (items.isNotEmpty()) {
            val sb = StringBuilder("选择器返回 ${items.size} 项:\n")
            items.forEachIndexed { index, item ->
                sb.append("  [$index+1] [${item::class.simpleName}] ${item.name}\n")
            }
            log("MediaSample", sb.toString())
            toast("已选 ${items.size} 项资源")
        } else {
            toast("未选择任何资源")
        }
    }

    private fun openMediaPickerAllTypes() {
        mediaPickerLauncher.launch(
            app.allever.android.lib.media.picker.MediaPickerConfig(
                types = setOf(MediaType.Type.IMAGE, MediaType.Type.VIDEO, MediaType.Type.AUDIO),
                maxSelect = 9,
            )
        )
    }

    private fun openMediaPickerImageOnly() {
        mediaPickerLauncher.launch(
            app.allever.android.lib.media.picker.MediaPickerConfig(
                types = setOf(MediaType.Type.IMAGE),
                maxSelect = 6,
            )
        )
    }

    private fun openMediaPickerVideoOnly() {
        mediaPickerLauncher.launch(
            app.allever.android.lib.media.picker.MediaPickerConfig(
                types = setOf(MediaType.Type.VIDEO),
                maxSelect = 3,
            )
        )
    }
}