package app.allever.android.sample.app.sticker.icon

import android.view.Gravity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import com.alsg.bakericon.MyApp
import com.alsg.bakericon.SlideMainActivity
import com.alsg.bakericon.ui.SplashActivity
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/appstickericon/main")
class SampleAppStickerIconActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "Sticker Icon"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("启动页") {
            ActivityHelper.startActivity<SplashActivity>()
        },
        TextDetailClickItem("主页") {
            ActivityHelper.startActivity<SlideMainActivity>()
        }
    )

    override fun init() {
        super.init()
        MyApp.init()
    }
}