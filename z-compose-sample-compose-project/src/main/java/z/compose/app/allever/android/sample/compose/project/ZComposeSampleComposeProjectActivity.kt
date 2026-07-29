package z.compose.app.allever.android.sample.compose.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.allever.android.learning.project.compose.module.tianliao.module.main.TLMainActivity
import app.allever.android.learning.project.compose.module.wechat.ui.WechatComposeActivity
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import com.allever.compose.core.TextClickItem
import com.allever.compose.core.ui.ComposeProjectTheme
import com.allever.compose.core.ui.FunctionList
import com.allever.compose.project.compose.ad.AdMobManager
import com.allever.compose.project.compose.ad.ComposeAdMainActivity
import com.allever.compose.project.compose.basic.ComposeBasicMainActivity
import com.allever.compose.project.google.GoogleComposeMainActivity
import com.allever.compose.project.watch.WatchMainActivity

@Route(path = "/zcomposesampleproject/main")
class ZComposeSampleComposeProjectActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdMobManager.init( this.applicationContext)
        setContent {
            ComposeProjectTheme {
                FunctionList(list = mutableListOf<TextClickItem>().apply {
                    add(TextClickItem("Compose Basic", "Compose 基础") {
                        ActivityHelper.startActivity<ComposeBasicMainActivity>()
                    })
                    add(TextClickItem("GoogleCompose", "Google Compose 教程") {
                        ActivityHelper.startActivity<GoogleComposeMainActivity>()
                    })
                    add(TextClickItem("虚构", "扔物线compose教程，虚构app") {
                        ActivityHelper.startActivity<WatchMainActivity>()
                    })
                    add(TextClickItem("微信", "微信主界面") {
                        ActivityHelper.startActivity<WechatComposeActivity>()
                    })
                    add(TextClickItem("天聊", "天聊Compose") {
                        ActivityHelper.startActivity<TLMainActivity>()
                    })
                    add(TextClickItem("Compose Ad", "Compose 接入 AdMob") {
                        ActivityHelper.startActivity<ComposeAdMainActivity>()
                    })
                })
            }
        }
    }
}