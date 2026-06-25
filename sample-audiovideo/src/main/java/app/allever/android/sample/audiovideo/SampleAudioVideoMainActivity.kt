package app.allever.android.sample.audiovideo

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.player.core.engine.EngineRegistry
import app.allever.android.lib.player.core.engine.ijk.IjkPlayerEngine
import app.allever.android.lib.player.core.engine.media3.ExoPlayerViewRender
import app.allever.android.lib.player.core.engine.media3.Media3PlayerEngine
import app.allever.android.lib.player.core.render.RenderRegistry
import app.allever.android.sample.audiovideo.android.AndroidAudioVideoFragment
import app.allever.android.sample.audiovideo.knowledge.AudioVideoKnowledgeFragment
import app.allever.android.sample.audiovideo.lib.AudioVideoLibFragment
import app.allever.android.sample.audiovideo.sdk.SDKAudioVideoFragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/audiovideo/main")
class SampleAudioVideoMainActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "音视频"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("音视频基础知识") {
            FragmentActivity.start<AudioVideoKnowledgeFragment>(it.title)
        },
        TextClickItem("Android音视频") {
            FragmentActivity.start< AndroidAudioVideoFragment>(it.title)
        },
        TextClickItem("SDK音视频") {
            FragmentActivity.start<SDKAudioVideoFragment>(it.title)
        },
        TextClickItem("音视频Lib") {
            FragmentActivity.start<AudioVideoLibFragment>(it.title)
        }
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