package app.allever.android.sample.audiovideo.android

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.sample.audiovideo.core.player.StdVideoPlayerSampleFragment
import app.allever.android.sample.audiovideo.core.player.VideoPlayerSampleFragment
import app.allever.android.sample.audiovideo.core.player.VideoPlayerViewSampleFragment
import com.chad.library.adapter.base.BaseQuickAdapter

class AndroidAudioVideoFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("MediaPlayer播放音频") {
            //MusicPlayer
            FragmentActivity.start<AndroidMusicPlayerSampleFragment>(it.title)
        },
        TextClickItem("MediaPlayer+VideoView播放视频") {
            //VideoViewPlayer
            FragmentActivity.start<AndroidVideoViewPlayerSampleFragment>(it.title)
        },
        TextClickItem("MediaPlayer+SurfaceView播放视频") {
            //SurfaceViewPlayer
            FragmentActivity.start<AndroidSurfacePlayerSampleFragment>(it.title)
        },
        TextClickItem("MediaPlayer+TextureView播放视频") {
            //TextureViewPlayer
            FragmentActivity.start<AndroidTexturePlayerSampleFragment>(it.title)
        },
        TextClickItem("Media3Player(ExoPlayer)播放视频") {
            //Media3Player
            FragmentActivity.start<AndroidMedia3PlayerSampleFragment>(it.title)
        },
        TextClickItem("MediaPlayer+VideoView/SurfaceView/TextureView") {
            //MediaPlayer
            FragmentActivity.start<AndroidMediaPlayerSampleFragment>(it.title)
        },
        TextClickItem("Engine+Render") {
            //AudioVideoPlayer
            FragmentActivity.start<VideoPlayerSampleFragment>(it.title)
        },
        TextClickItem("UI controller + Render + Engine") {
            //Media3Player
            FragmentActivity.start<VideoPlayerViewSampleFragment>(it.title)
        },
        TextClickItem("StdVideoPlayer") {
            //Media3Player
            FragmentActivity.start<StdVideoPlayerSampleFragment>(it.title)
        },
    )
}