package app.android.gp.ai.translator.ui.mvp.presenter

import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.event.UpdateRecordEventB
import app.android.gp.ai.translator.language.LanguageHelper
import app.android.gp.ai.translator.function.SettingHelper
import app.android.gp.ai.translator.translate.TranslationHelper
import app.android.gp.ai.translator.db.DBHelper
import app.android.gp.ai.translator.translate.ITranslateCallback
import app.android.gp.ai.translator.bean.TranslateResult
import app.android.gp.ai.translator.ui.mvp.view.TranslationView
import app.android.gp.ai.translator.util.ClipboardHelper
import app.android.gp.ai.translator.util.JsonHelper
import app.android.gp.ai.translator.app.mvp.BasePresenter
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import org.greenrobot.eventbus.EventBus

class TranslationPresenter : BasePresenter<TranslationView>() {

    fun translate(content: String, sl: String = LanguageHelper.AUTO(), translateLanguage: String) {
        if (content.isEmpty()) {
            toast(R.string.tt_please_input_content)
            return
        }

        val history = DBHelper.getHistory(content, sl, translateLanguage)
        val translationBean =
            JsonHelper.json2Object(history?.result ?: "", TranslateResult::class.java)
        if (translationBean != null) {
            parse(translationBean)
            mViewRef?.get()?.refreshLiked(history?.liked == 1)
            val translateText = translationBean.translateText
            if (translateText.isNotEmpty()) {
                play(translateText, translateLanguage)
                copyToClipBoard(translateText)
            }
            log("获取到数据库翻译内容")
            DBHelper.updateHistoryTime(history)
            EventBus.getDefault().post(UpdateRecordEventB())
            return
        }

        TranslationHelper.translate(content, sl, translateLanguage, object : ITranslateCallback {
            override fun onTranslateResult(result: TranslateResult?) {
                if (result == null) {
                    log("翻译失败")
                    return
                }

                parse(result)

                val translateText = result.translateText

                mViewRef?.get()?.refreshLiked(false)

                play(translateText, translateLanguage)

                copyToClipBoard(translateText)

                DBHelper.addHistory(content, sl, translateLanguage, result)

                EventBus.getDefault().post(UpdateRecordEventB())
            }
        })
    }

    fun playAudio(content: String, translateLanguage: String) {
        TranslationHelper.playTTS(content, translateLanguage)
    }

    fun shareAudio(obj: Any, content: String, tl: String) {
        TranslationHelper.shareAudio(obj, content, tl)
    }

    fun liked(content: String, sl: String = LanguageHelper.AUTO(), tl: String) {
        val result = DBHelper.like(content, sl, tl)
        if (result) {
            val history = DBHelper.getHistory(content, sl, tl)
            val like = history?.liked
            if (like == 0) {
                toast(R.string.tt_added_to_words)
            } else {
                toast(R.string.tt_removed_from_words)
            }
            mViewRef?.get()?.refreshLiked(history?.liked == 1)
            EventBus.getDefault().post(UpdateRecordEventB())
        }
    }

    fun copyText(content: String?) {
        ClipboardHelper.copy(content)
    }

    private fun parse(bean: TranslateResult) {

        val srcSymbol = bean.srcSymbol
        val translateSymbol = bean.translateTextSymbol
        //音标显示逻辑
        if (srcSymbol.isNotEmpty()) {
            mViewRef?.get()?.showOrHideSoundSrcSymbol(true)
        } else {
            mViewRef?.get()?.showOrHideSoundSrcSymbol(false)
        }
        if (translateSymbol.isNotEmpty()) {
            mViewRef?.get()?.showOrHideSoundTranslateSymbol(true)
        } else {
            mViewRef?.get()?.showOrHideSoundTranslateSymbol(false)
        }

        if (bean.more.isNotEmpty()) {
            mViewRef?.get()?.showOrHideDictInfo(true)
        } else {
            mViewRef?.get()?.showOrHideDictInfo(false)
        }

        mViewRef?.get()?.updateResult(
            bean
        )
    }

    private fun play(content: String, translateLanguage: String) {
        //播放语音
        if (SettingHelper.getAutoPlayAudio()) {
            playAudio(content, translateLanguage)
        } else {
            TranslationHelper.requestTTS(content, translateLanguage, null)
        }
    }

    private fun copyToClipBoard(translateText: String) {
        //复制剪到贴板
        if (SettingHelper.getCopyClipBoard()) {
            ClipboardHelper.copy(translateText)
        }
    }
}