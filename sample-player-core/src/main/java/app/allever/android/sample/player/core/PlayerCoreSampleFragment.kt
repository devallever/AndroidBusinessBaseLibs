package app.allever.android.sample.player.core

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.picker.MediaPickerContract
import app.allever.android.lib.media.picker.MediaPickerConfig
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * 播放器核心库示例列表页
 *
 * 功能入口：
 * - 从媒体选择器选取视频 → 使用 MediaPlayer 内核播放
 */
class PlayerCoreSampleFragment :
    ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("【MediaPlayer】选择本地视频并播放") { pickAndPlayVideo() },
        TextClickItem("【MediaPlayer】播放网络视频（示例 URL）") { playNetworkVideo() },
    )

    // ==================== 媒体选择器：选视频后播放 ====================

    private val videoPickerLauncher = registerForActivityResult(
        MediaPickerContract()
    ) { items ->
        items.firstOrNull()?.let { item ->
            when (item) {
                is MediaItem.Video -> openPlayer(item.uri.toString(), item.name)
                else -> toast("请选择视频文件")
            }
        } ?: toast("未选择任何资源")
    }

    /** 打开媒体选择器，仅限视频类型 */
    private fun pickAndPlayVideo() {
        videoPickerLauncher.launch(
            MediaPickerConfig(
                types = setOf(MediaType.Type.VIDEO),
                maxSelect = 1,
            )
        )
    }

    // ==================== 网络视频播放 ====================

    private fun playNetworkVideo() {
        // 示例网络视频 URL（可替换为实际地址）
        val testUrl = "https://www.w3schools.com/html/mov_bbb.mp4"
        openPlayer(testUrl, "Big Buck Bunny")
    }

    // ==================== 跳转播放器页面 ====================

    private fun openPlayer(url: String, title: String = "") {
        log("PlayerSample", "openPlayer: url=$url, title=$title")
        toast("openPlayer: url=$url, title=$title")
//        val extras = PlayerActivity.start(url, title)
//        requireContext().startActivity(
//            android.content.Intent(requireContext(), PlayerActivity::class.java).apply {
//                putExtras(extras)
//            }
//        )
    }
}
