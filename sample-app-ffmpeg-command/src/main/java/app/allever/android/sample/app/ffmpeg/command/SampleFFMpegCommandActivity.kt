package app.allever.android.sample.app.ffmpeg.command

import android.view.Gravity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.coder.ffmpegtest.BaseApplication
import com.coder.ffmpegtest.ui.MainActivity

@Route(path = "/appffmpegcommand/main")
class SampleFFMpegCommandActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "FFMpegCommand"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *>  = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("主页") {
            ActivityHelper.startActivity<MainActivity>()
        }
    )

    override fun init() {
        super.init()
        BaseApplication.onCreate()
    }
}