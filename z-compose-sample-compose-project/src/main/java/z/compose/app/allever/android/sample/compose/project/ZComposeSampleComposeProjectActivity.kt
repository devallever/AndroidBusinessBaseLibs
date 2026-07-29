package z.compose.app.allever.android.sample.compose.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import com.allever.compose.core.TextClickItem
import com.allever.compose.core.ui.ComposeProjectTheme
import com.allever.compose.core.ui.FunctionList
import com.allever.compose.project.compose.basic.ComposeBasicMainActivity

@Route(path = "/zcomposesampleproject/main")
class ZComposeSampleComposeProjectActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeProjectTheme {
                FunctionList(list = mutableListOf<TextClickItem>().apply {
                    add(TextClickItem("Compose Basic", "Compose 基础") {
                        ActivityHelper.startActivity<ComposeBasicMainActivity>()
                    })
                    add(TextClickItem("微信", "微信主界面") {
//                        WechatComposeActivity.start(this@MainActivity)
                    })
                    add(TextClickItem("天聊", "天聊Compose") {
//                        ActivityHelper.startActivity(TLMainActivity::class.java, this@MainActivity)
                    })
                    add(TextClickItem("虚构", "扔物线compose教程，虚构app") {
//                        ActivityHelper.startActivity<WatchMainActivity>(this@MainActivity) {  }
                    })
                    add(TextClickItem("GoogleCompose", "Google Compose 教程") {
//                        ActivityHelper.startActivity<GoogleComposeMainActivity>(this@MainActivity) {  }
                    })
                    add(TextClickItem("Compose Ad", "Compose 接入 AdMob") {
//                        ActivityHelper.startActivity<ComposeAdMainActivity> (this@MainActivity){  }
                    })
                })
            }
        }
    }
}