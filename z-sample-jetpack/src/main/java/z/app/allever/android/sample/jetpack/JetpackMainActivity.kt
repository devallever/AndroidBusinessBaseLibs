package z.app.allever.android.sample.jetpack

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.network.core.NetCore
import app.allever.android.lib.network.core.engine.huc.UrlConnectionConfig
import app.allever.android.lib.network.core.engine.huc.UrlConnectionEngine
import com.therouter.router.Route
import z.app.allever.android.sample.jetpack.network.BaseResponse

@Route(path = "/zjetpack/main")
class JetpackMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun init() {
        super.init()
        NetCore.init {
            baseUrl("https://www.wanandroid.com")
            // 选择 HttpURLConnection 引擎
            engine(UrlConnectionEngine.ENGINE_NAME) {
                // HUC 专属配置
                connectTimeout(10_000)
                readTimeout(15_000)
                (this as? UrlConnectionConfig)?.apply {
                    followRedirects(true)
                    keepAlive(true)
                }
            }

            successCode(0)

            // 公共请求头
            header("Accept", "application/json")
            header("App-Version", "1.0.0")

            // 启用日志
            enableLog(true)

            // 设置统一业务响应类型
            responseClass(BaseResponse::class.java)
        }
    }
    override fun getSampleName(): String = "Jetpack"

    override fun getSampleFragment(): Fragment = JetpackMainFragment()
}