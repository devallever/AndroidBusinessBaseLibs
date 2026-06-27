package app.allever.android.sample.audiovideo.android

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.player.core.player.VideoPlayerActivity
import app.allever.android.sample.audiovideo.core.CustomStdVideoPlayerSampleFragment
import app.allever.android.sample.audiovideo.core.StdVideoPlayerSampleFragment
import app.allever.android.sample.audiovideo.core.VideoPlayerSampleFragment
import app.allever.android.sample.audiovideo.core.VideoPlayerViewSampleFragment
import app.allever.android.sample.audiovideo.sdk.IjkVideoPlayerSampleFragment
import com.chad.library.adapter.base.BaseQuickAdapter

class AndroidAudioVideoFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("MediaPlayer播放音频") {
            //MusicPlayer
            FragmentActivity.start<AndroidMusicPlayerSampleFragment>(it.title)
        },
        TextDetailClickItem("AndroidVideoViewPlayer", "MediaPlayer+VideoView") {
            //VideoViewPlayer
            FragmentActivity.start<AndroidVideoViewPlayerSampleFragment>(it.title)
        },
        TextDetailClickItem("AndroidSurfacePlayer", "MediaPlayer+SurfaceView") {
            //SurfaceViewPlayer
            FragmentActivity.start<AndroidSurfacePlayerSampleFragment>(it.title)
        },
        TextDetailClickItem("AndroidTexturePlayer", "MediaPlayer+TextureView") {
            //TextureViewPlayer
            FragmentActivity.start<AndroidTexturePlayerSampleFragment>(it.title)
        },
    )
}