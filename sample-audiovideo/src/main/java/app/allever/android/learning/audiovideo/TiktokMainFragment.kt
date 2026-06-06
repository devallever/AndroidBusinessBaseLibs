package app.allever.android.learning.audiovideo

import app.allever.android.learning.audiovideo.kernel.AndroidPlayerFactory
import app.allever.android.learning.audiovideo.kernel.internal.AbsPlayerFactory
import app.allever.android.learning.audiovideo.render.AbsRenderFactory
import app.allever.android.learning.audiovideo.render.IJKRenderFactory
import app.allever.android.learning.audiovideo.render.IRenderView
import app.allever.android.learning.audiovideo.render.SurfaceRenderFactory
import app.allever.android.learning.audiovideo.render.TextureRenderFactory
import app.allever.android.learning.audiovideo.tiktok.RvJzIjkTiktokFragment
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding

class TiktokMainFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter() = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("RV+系统播放器内核") {

        },
        TextClickItem("RV+ijk播放器内核") {

        },
        TextClickItem("RV+饺子播放器+ijk内核") {
            FragmentActivity.start<RvJzIjkTiktokFragment>("", showTopBar = false, darkMode = true)
        },


        )
}

class MediaRenderFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    private var renderView: IRenderView? = null
    private val player = AbsPlayerFactory.Companion.create<AndroidPlayerFactory>().createPlayer()
    override fun getAdapter() = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("使用SurfaceView渲染") {
            renderView =
                AbsRenderFactory.Companion.create<SurfaceRenderFactory>().createRender(requireContext())
        },
        TextClickItem("使用TextureView渲染") {
            renderView =
                AbsRenderFactory.Companion.create<TextureRenderFactory>().createRender(requireContext())
        },
        TextClickItem("使用IJKVideoView渲染") {
            renderView =
                AbsRenderFactory.Companion.create<IJKRenderFactory>().createRender(requireContext())
        },
        TextClickItem("绑定播放器") {
            renderView?.attachToPlayer(player)
        }
    )
}