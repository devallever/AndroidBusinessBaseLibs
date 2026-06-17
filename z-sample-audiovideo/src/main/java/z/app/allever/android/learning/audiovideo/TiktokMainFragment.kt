package z.app.allever.android.learning.audiovideo

import z.app.allever.android.learning.audiovideo.kernel.AndroidPlayerFactory
import z.app.allever.android.learning.audiovideo.kernel.internal.AbsPlayerFactory
import z.app.allever.android.learning.audiovideo.render.internal.AbsRenderFactory
import z.app.allever.android.learning.audiovideo.render.IJKRenderFactory
import z.app.allever.android.learning.audiovideo.render.internal.IRenderView
import z.app.allever.android.learning.audiovideo.render.SurfaceRenderFactory
import z.app.allever.android.learning.audiovideo.render.TextureRenderFactory
import z.app.allever.android.learning.audiovideo.tiktok.RvJzIjkTiktokFragment
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

