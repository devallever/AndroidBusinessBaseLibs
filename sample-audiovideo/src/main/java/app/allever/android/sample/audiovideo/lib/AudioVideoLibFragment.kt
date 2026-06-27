package app.allever.android.sample.audiovideo.lib

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.sample.audiovideo.android.AndroidMedia3PlayerSampleFragment
import app.allever.android.sample.audiovideo.android.AndroidMediaPlayerSampleFragment
import app.allever.android.sample.audiovideo.sdk.IjkVideoPlayerSampleFragment
import com.chad.library.adapter.base.BaseQuickAdapter

class AudioVideoLibFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("AndroidMediaPlayer: BaseVideoPlayer()", "MediaPlayerKernal\nMediaPlayer+VideoView/SurfaceView/TextureView") {
            //MediaPlayer
            FragmentActivity.start<AndroidMediaPlayerSampleFragment>(it.title)
        },
        TextDetailClickItem("AndroidMedia3Player: BaseVideoPlayer()", "Media3PlayerKernal\nExoPlayer+PlayerView/SurfaceView/TextureView") {
            //Media3Player
            FragmentActivity.start<AndroidMedia3PlayerSampleFragment>(it.title)
        },
        TextDetailClickItem("IjkVideoPlayer: BaseVideoPlayer()", "IjkPlayerKernal\nIjkMediaPlayer+SurfaceView/TextureView") {
            FragmentActivity.start<IjkVideoPlayerSampleFragment>(it.title)
        },
    )
}