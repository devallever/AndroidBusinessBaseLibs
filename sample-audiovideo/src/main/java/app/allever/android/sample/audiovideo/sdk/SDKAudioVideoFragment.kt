package app.allever.android.sample.audiovideo.sdk

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

class SDKAudioVideoFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("ExoPlayer") {
            FragmentActivity.start<ExoVideoPlayerSampleFragment>(it.title)
        },
        TextClickItem("ijkPlayer") {
            FragmentActivity.start<IjkVideoPlayerSampleFragment>(it.title)
        },
    )
}