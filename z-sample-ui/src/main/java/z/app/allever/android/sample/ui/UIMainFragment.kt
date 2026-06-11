package z.app.allever.android.sample.ui

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import z.app.allever.android.sample.ui.ui.*
import z.app.allever.android.sample.ui.ui.autoscroll.AutoScrollMainFragment
import z.app.allever.android.sample.ui.ui.dialog.DialogMainFragment
import z.app.allever.android.sample.ui.ui.dragclose.DragCloseMainFragment
import z.app.allever.android.sample.ui.ui.dragrecycler.ManageFriendGroupFragment
import z.app.allever.android.sample.ui.ui.expandrecycler.ContactExpandListFragment
import z.app.allever.android.sample.ui.ui.notification.NotificationMainFragment
import z.app.allever.android.sample.ui.ui.sticktop.StickyTopMainFragment
import com.chad.library.adapter.base.BaseQuickAdapter

class UIMainFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("UI效果") {
            FragmentActivity.start<UIFragment>(it.title)
        },
        TextClickItem("吸顶效果") {
            FragmentActivity.start<StickyTopMainFragment>(it.title)
        },
        TextClickItem("个人主页折叠效果") {
            ActivityHelper.startActivity<UserCenterActivity>()
        },
        TextClickItem("回弹效果") {
            FragmentActivity.start<FlingScrollTabFragment>(it.title)
        },
        TextClickItem("自动滚动效果") {
            FragmentActivity.start<AutoScrollMainFragment>(it.title)
        },
        TextClickItem("探探-交互效果") {
            FragmentActivity.start<TanTanFragment>(it.title)
        },
        TextClickItem("Soul-交互效果") {
            FragmentActivity.start<SoulFragment>(it.title)
        },
        TextClickItem("分组联系人列表") {
            FragmentActivity.start<ContactListFragment>(it.title)
        },
        TextClickItem("分组联系人列表-折叠") {
            FragmentActivity.start<ContactExpandListFragment>(it.title)
        },
        TextClickItem("分组管理") {
            FragmentActivity.start<ManageFriendGroupFragment>(it.title)
        },
        TextClickItem("弹窗") {
            FragmentActivity.start<DialogMainFragment>(it.title)
        },
        TextClickItem("通知") {
            FragmentActivity.start<NotificationMainFragment>(it.title)
        },
        TextClickItem("拖拽关闭页面效果") {
            FragmentActivity.start<DragCloseMainFragment>(it.title)
        },
        TextClickItem("多区间速度进度条") {
            FragmentActivity.start<MultiSpeedProgressFragment>(it.title)
        },
        TextClickItem("居中Item的RecyclerView") {
            FragmentActivity.start<CenterRvFragment>(it.title)
        },
        TextClickItem("居中Item的RecyclerView2") {
            FragmentActivity.start<CenterRvStyle2Fragment>(it.title)
        },
        TextClickItem("Ripple") {
            FragmentActivity.start<RippleFragment>(it.title)
        },

        )
}