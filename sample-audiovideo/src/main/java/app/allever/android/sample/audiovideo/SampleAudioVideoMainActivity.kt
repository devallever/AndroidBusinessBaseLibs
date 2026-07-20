package app.allever.android.sample.audiovideo

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.player.core.engine.EngineRegistry
import app.allever.android.lib.player.core.engine.ijk.IjkPlayerEngine
import app.allever.android.lib.player.core.engine.media3.ExoPlayerViewRender
import app.allever.android.lib.player.core.engine.media3.Media3PlayerEngine
import app.allever.android.lib.player.core.render.RenderRegistry
import app.allever.android.sample.audiovideo.android.AndroidAudioVideoFragment
import app.allever.android.sample.audiovideo.core.PlayerCoreSampleFragment
import app.allever.android.sample.audiovideo.knowledge.AudioVideoKnowledgeFragment
import app.allever.android.sample.audiovideo.lib.AudioVideoLibFragment
import app.allever.android.sample.audiovideo.sdk.SDKAudioVideoFragment
import com.therouter.router.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/audiovideo/main")
class SampleAudioVideoMainActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "音视频"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("音视频基础知识") {
            FragmentActivity.start<AudioVideoKnowledgeFragment>(it.title)
        },
        TextDetailClickItem("Android音视频", "MediaPlayer + VideoView/SurfaceView/TextureView") {
            FragmentActivity.start< AndroidAudioVideoFragment>(it.title)
        },
        TextDetailClickItem("音视频Lib", "BaseVideoPlayer->AndroidMediaPlayer/AndroidMedia3Player/IjkVideoPlayer\nIPlayerKernal->MediaPlayerKernal/Media3PlayerKernal/IjkPlayerKernal") {
            FragmentActivity.start<AudioVideoLibFragment>(it.title)
        },
        TextDetailClickItem("Player-Core", "VideoPlayer/StdVideoPlayer/CustomVideoPlayer\nIPlayerEngine->MediaPlayerEngine/Media3PlayerEngine/IjkPlayerEngine\nIVideoRender->PlayerViewRender/TextureViewRender/SurfaceViewRender/VideoViewRender\nIVideoUiController->StdVideoController/CustomStdVideoController") {
            FragmentActivity.start<PlayerCoreSampleFragment>(it.title)
        },
        TextDetailClickItem("SDK", "ExoPlayer/IjkPlayer") {
            FragmentActivity.start<SDKAudioVideoFragment>(it.title)
        },

    )

    override fun init() {
        super.init()
        // 初始化渲染器注册表
        RenderRegistry.registerBuiltInRenders()
        RenderRegistry.register(ExoPlayerViewRender.NAME, { ExoPlayerViewRender() })
        EngineRegistry.registerBuiltInEngines()
        EngineRegistry.register(Media3PlayerEngine.NAME, { Media3PlayerEngine() })
        EngineRegistry.register(IjkPlayerEngine.NAME, { IjkPlayerEngine() })
    }
}