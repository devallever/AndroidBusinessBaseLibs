package z.app.allever.android.learning.audiovideo.tiktok.adapter

import z.app.allever.android.learning.audiovideo.jz.JZMediaIjk
import z.app.allever.android.learning.audiovideo.tiktok.VideoBean
import z.app.allever.android.sample.audiovideo.R
import z.app.allever.android.sample.audiovideo.databinding.ItemRvJzIjkBinding
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class RvJzIjkAdapter : BaseQuickAdapter<VideoBean, BaseViewHolder>(R.layout.item_rv_jz_ijk) {
    override fun convert(holder: BaseViewHolder, item: VideoBean) {
        val binding = ItemRvJzIjkBinding.bind(holder.itemView)

        val jzDataSource = JZDataSource(item.url, item.title)
        jzDataSource.looping = true
        binding.videoView.setUp(jzDataSource, Jzvd.SCREEN_NORMAL, JZMediaIjk::class.java)

    }
}