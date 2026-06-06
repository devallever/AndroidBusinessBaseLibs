package app.allever.android.learning.audiovideo.ijkplayer

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.media.picker.MediaPickerCore

class IJKPlayerMainFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    private val videoPickerLauncher = MediaPickerCore.registerPickerLauncher(this) { mediaItems ->
        if (mediaItems.isEmpty()) {
            toast("请选择视频")
            return@registerPickerLauncher
        }
        val item = mediaItems.first()
        FragmentActivity.start<IJKBasePlayerFragment>(item.path) {
            it.putString("path", item.path)
            it.putParcelable("uri", item.uri)
        }
    }

    override fun getAdapter() = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("ijkPlayer基础播放器") {
            MediaPickerCore.launchVideo(videoPickerLauncher, 1)
        }
    )
}