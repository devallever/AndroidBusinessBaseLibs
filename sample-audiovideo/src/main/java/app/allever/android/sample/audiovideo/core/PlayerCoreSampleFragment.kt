package app.allever.android.sample.audiovideo.core

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.player.core.player.VideoPlayerActivity
import com.chad.library.adapter.base.BaseQuickAdapter

class PlayerCoreSampleFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("VideoPlayer", "封装IPlayerEngine+IVideoRender\nMediaPlayerEngine/Media3PlayerEngine/IjkPlayerEngine\nSurfaceViewRender/TextureViewRender/VideoViewRender/PlayerRender") {
            //AudioVideoPlayer
            FragmentActivity.start<VideoPlayerSampleFragment>(it.title)
        },
        TextDetailClickItem("VideoPlayerView", " UI控制 + Render + Engine") {
            //Media3Player
            FragmentActivity.start<VideoPlayerViewSampleFragment>(it.title)
        },
        TextDetailClickItem("StdVideoPlayer", "封装IVideoUiController+VideoPlayer") {
            FragmentActivity.start<StdVideoPlayerSampleFragment>(it.title)
        },
        TextDetailClickItem("CustomStdVideoPlayer", "CustomStdVideoController自定义ui控制层") {
            //Media3Player
            FragmentActivity.start<CustomStdVideoPlayerSampleFragment>(it.title)
        },
        TextDetailClickItem("VideoPlayerActivity", "全屏播放") {
            //Media3Player
            VideoPlayerActivity.start(requireContext(), assetPath = "output.mp4")
        },
    )
}