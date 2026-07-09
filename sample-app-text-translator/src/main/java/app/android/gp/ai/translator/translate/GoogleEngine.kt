package app.android.gp.ai.translator.translate

import android.content.Context
import app.android.gp.ai.translator.app.Global
import app.android.gp.ai.translator.language.Lang
import app.android.gp.ai.translator.language.LangCodeGoogle
import app.android.gp.ai.translator.bean.TranslationBean
import app.android.gp.ai.translator.language.LanguageHelper
import app.android.gp.ai.translator.function.MediaHelper
import app.android.gp.ai.translator.db.DBHelper
import app.android.gp.ai.translator.bean.TranslateResult
import app.android.gp.ai.translator.network.RetrofitUtil
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.CoroutineHelper
import kotlinx.coroutines.launch

class GoogleEngine : ITranslateEngine {
    override fun init(context: Context) {
        LanguageHelper.init(LangCodeGoogle())
    }

    override fun baseUrl() = "https://translate.google.com/"


    override fun translate(
        q: String,
        fromLang: String,
        toLang: String,
        callback: ITranslateCallback?
    ) {
        CoroutineHelper.IO.launch {
            val bean = RetrofitUtil.translate(q, fromLang, toLang)
            val resultBean = TranslateResult()
            resultBean.fromLang = getSrcLang(bean)
            resultBean.fromLangText = Global.langCodeKeyMap[fromLang] ?: Lang.CHINESE.KEY
            resultBean.srcText = getSrcText(bean)
            resultBean.srcSymbol = getSrcSymbol(bean)
            resultBean.toLang = toLang
            resultBean.toLangText = Global.langCodeKeyMap[toLang] ?: Lang.CHINESE.KEY
            resultBean.translateText = getTranslateText(bean)
            resultBean.translateTextSymbol = getTranslateSymbol(bean)
            resultBean.more = getDictText(bean)
            CoroutineHelper.MAIN.launch {
                callback?.onTranslateResult(resultBean)
            }
        }

    }

    override fun tts(content: String, toLang: String) {
        val ttsPath = DBHelper.getTTSPath(content, toLang)
        if (ttsPath.isNotEmpty()) {
            MediaHelper.playFile(
                ttsPath
            )
            log("play local cache file")
            return
        }

        TranslationHelper.requestTTS(
            content,
            toLang
        ) {
            val path =
                DBHelper.getTTSPath(
                    content,
                    toLang
                )
            MediaHelper.playFile(
                path
            )
        }
    }

    private fun getSrcLang(bean: TranslationBean): String {
        var srcLang: String = Lang.CHINESE.KEY
        //根据原文本类型去设置
        val srcLangList = bean.ld_result?.extended_srclangs
        if (srcLangList?.isNotEmpty() == true) {
            srcLang = Global.langCodeKeyMap[srcLangList[0]] ?: Lang.CHINESE.KEY
        }
        log("origin Lang = $srcLang")
        return srcLang
    }

    private fun getSrcText(bean: TranslationBean): String {
        //google翻译
        var translation = ""
        if (bean.sentences?.size ?: 0 > 0) {
            translation = bean.sentences?.get(0)?.orig ?: ""
        }
        log("origin Text = $translation")
        return translation
    }

    private fun getTranslateText(bean: TranslationBean): String {
        //google翻译
        var translation = ""
        if ((bean.sentences?.size ?: 0) > 0) {
            translation = bean.sentences?.get(0)?.trans ?: ""
        }
        log("Translate Text = $translation")
        return translation
    }

    private fun getSrcSymbol(bean: TranslationBean): String {
        var srcSymbol = ""
        if ((bean.sentences?.size ?: 0) > 1) {
            val symbols = bean.sentences?.get(1)
            srcSymbol = symbols?.src_translit ?: ""
        }
        return srcSymbol
    }

    private fun getTranslateSymbol(bean: TranslationBean): String {
        var translateSymbol = ""
        if ((bean.sentences?.size ?: 0) > 1) {
            val symbols = bean.sentences?.get(1)
            translateSymbol = symbols?.translit ?: ""
        }

        return translateSymbol
    }

    private fun getDictText(bean: TranslationBean): String {
        //解析词典
        val resultBuilder = StringBuilder()
        val dictionaryBeanList = bean.dict
        dictionaryBeanList?.map {
            resultBuilder.append("${it.pos}\n")
            val entity = it.entry
            entity?.map {
                resultBuilder.append("\t${it.word}\n")
                val list = it.reverse_translation
                list?.mapIndexed { index, s ->
                    val last = if (index != list.size - 1) {
                        ", "
                    } else {
                        "\n\n"
                    }
                    resultBuilder.append("\t$s$last")
                }
            }
        }
        return resultBuilder.toString()
    }
}