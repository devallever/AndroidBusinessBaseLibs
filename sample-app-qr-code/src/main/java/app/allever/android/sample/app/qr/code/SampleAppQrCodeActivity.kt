package app.allever.android.sample.app.qr.code

import app.allever.android.ai.qr.scanner.QRCodeApp
import app.allever.android.ai.qr.scanner.ui.MainActivity
import app.allever.android.ai.qr.scanner.ui.SplashActivity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/appqrcode/main")
class SampleAppQrCodeActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String =  "二维码"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("启动页",) {
            ActivityHelper.startActivity(SplashActivity::class.java)
        },
        TextClickItem("主页",) {
            ActivityHelper.startActivity<MainActivity>()
        }
    )

    override fun init() {
        super.init()
        QRCodeApp.onCreate()
    }
}