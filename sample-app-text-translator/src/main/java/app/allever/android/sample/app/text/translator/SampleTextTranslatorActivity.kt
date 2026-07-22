package app.allever.android.sample.app.text.translator

import android.view.Gravity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.android.gp.ai.translator.app.MyApp
import app.android.gp.ai.translator.ui.SplashPage
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/apptexttranslator/main")
class SampleTextTranslatorActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "文本翻译器"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("启动页", ) {
            ActivityHelper.startActivity<SplashPage>()
        },
    )

    override fun init() {
        super.init()
        MyApp.onCreate()
    }
}