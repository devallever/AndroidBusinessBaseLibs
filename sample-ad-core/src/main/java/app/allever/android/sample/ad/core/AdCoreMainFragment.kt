package app.allever.android.sample.ad.core

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.chad.library.adapter.base.BaseQuickAdapter

class AdCoreMainFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("Single Ad Provider") {
            ActivityHelper.startActivity<SingleProviderTabActivity> {}
        },
        TextClickItem("Waterfall Ad") {
            FragmentActivity.start<WaterfallFragment>("Waterfall")
        },
        TextClickItem("Bidding Ad") {
            FragmentActivity.start<BiddingFragment>("Bidding")
        },
    )
}