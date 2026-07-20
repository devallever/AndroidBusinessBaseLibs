package app.allever.android.sample.app.gif.search

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.therouter.router.Route
import com.allever.app.gif.memes.ui.SplashActivity
import com.allever.app.gif.memes.ui.main.GifMainActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.funny.gif.memes.app.GifSearch

@Route(path = "/appgifsearch/main")
class SampleAppGifSearchActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "Gif搜索"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()
    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("启动页") {
            ActivityHelper.startActivity<SplashActivity>()
        },
        TextClickItem("主页") {
            ActivityHelper.startActivity<GifMainActivity>()
        }
    )

    override fun init() {
        super.init()
        GifSearch.initThreadPackage()
    }


}