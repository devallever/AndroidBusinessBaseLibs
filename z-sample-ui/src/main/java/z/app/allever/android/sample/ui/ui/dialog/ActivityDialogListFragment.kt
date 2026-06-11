package z.app.allever.android.sample.ui.ui.dialog

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextAdapter
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.toast
import z.app.allever.android.sample.ui.ui.dialog.activitydialog.BottomActivityDialog
import com.chad.library.adapter.base.BaseQuickAdapter

class ActivityDialogListFragment : ListFragment<FragmentListBinding, ListViewModel, String>() {
    override fun getAdapter(): BaseQuickAdapter<String, *> = TextAdapter()

    override fun getList() = mutableListOf(
        "顶部弹窗",
        "中部弹窗",
        "底部弹窗"
    )

    override fun onItemClick(position: Int, item: String) {
        toast(item)
        when (position) {
            0 -> {
            }
            1 -> {
            }
            2 -> {
                BottomActivityDialog.show(requireContext())
            }
        }
    }
}