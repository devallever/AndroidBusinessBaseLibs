package app.allever.android.sample.audiovideo.android

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
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
        },
        TextClickItem("MediaPlayer+SurfaceView播放视频") {
            //SurfaceViewPlayer
        },
        TextClickItem("MediaPlayer+TextureView播放视频") {
            //TextureViewPlayer
        },
        TextClickItem("Media3播放视频") {
        },
    )
}