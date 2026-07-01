package app.allever.android.sample.app.spy.camera

import android.view.Gravity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.allever.stealthcamera.ui.MainActivity
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/appspycamera/main")
class SampleAppSypCameraActivity :
    ListActivity<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "隐私相机"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER
    )

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("主页") {
            ActivityHelper.startActivity<MainActivity>()
        }
    )
}