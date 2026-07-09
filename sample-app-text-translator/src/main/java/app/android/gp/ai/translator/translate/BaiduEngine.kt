package app.android.gp.ai.translator.translate

import android.content.Context
import app.android.gp.ai.translator.app.Global
import app.android.gp.ai.translator.language.Lang
import app.android.gp.ai.translator.language.LangCodeBaidu
import app.android.gp.ai.translator.bean.TranslateResult
import app.android.gp.ai.translator.language.LanguageHelper
import app.android.gp.ai.translator.network.RetrofitUtil
import app.android.gp.ai.translator.function.TTSHelper
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.CoroutineHelper
import kotlinx.coroutines.launch


class BaiduEngine : ITranslateEngine {

    override fun init(context: Context) {
        TTSHelper.init(context)
        LanguageHelper.init(LangCodeBaidu())
    }

    override fun baseUrl() = "http://api.fanyi.baidu.com/"

    override fun translate(
        q: String,
        fromLang: String,
        toLang: String,
        callback: ITranslateCallback?
    ) {
        CoroutineHelper.IO.launch {
            val bean = RetrofitUtil.translateBaidu(q, fromLang, toLang)
            val resultBean = TranslateResult()
            resultBean.fromLang = bean.from ?: ""
            resultBean.fromLangText = Global.langCodeKeyMap[bean.from] ?: Lang.CHINESE.KEY
            resultBean.srcText = bean.trans_result?.get(0)?.src ?: ""
            resultBean.toLang = bean.to ?: ""
            resultBean.toLangText = Global.langCodeKeyMap[bean.to] ?: Lang.CHINESE.KEY
            resultBean.translateText = bean.trans_result?.get(0)?.dst ?: ""
            CoroutineHelper.MAIN.launch {
                callback?.onTranslateResult(resultBean)
            }
        }
    }

    override fun tts(content: String, toLang: String) {
        val local = Global.langCodeLocalMap[toLang]
        if (local == null) {
            toast("Un support play sound")
            return
        }
        TTSHelper.speak(content, local)
    }
}