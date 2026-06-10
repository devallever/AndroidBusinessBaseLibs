package z.app.allever.android.sample.safe

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

class SafeMainFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("Gzip") {
            FragmentActivity.start<GzipAesBase64Fragment>(it.title)
        },
        TextClickItem("AES") {
        },
        TextClickItem("Base64") {
        },
    )
}