package app.allever.android.sample.app.secret.album

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.absbase.App
import com.chad.library.adapter.base.BaseQuickAdapter
import org.xm.secret.photo.album.ui.MainActivity
import org.xm.secret.photo.album.ui.SplashActivity

@Route(path = "/secretalbum/main")
class SampleAppSecretAlbumActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "私密相册"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("启动页"){
            ActivityHelper.startActivity<SplashActivity>()
        },
        TextClickItem("主页"){
            ActivityHelper.startActivity<MainActivity>()
        },
    )

    override fun init() {
        super.init()
        App.setContext(applicationContext)
    }
}