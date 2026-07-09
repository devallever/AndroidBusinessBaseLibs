package app.android.gp.ai.translator.translate

import android.content.Context
import app.android.gp.ai.translator.translate.ITranslateCallback

interface ITranslateEngine {
    /**
     * 初始化
     */
    fun init(context: Context)

    fun baseUrl(): String

    /**
     * 翻译
     */
    fun translate(q: String, fromLang: String, toLang: String, callback: ITranslateCallback?)

    /**
     * 语音
     */
    fun tts(content: String, toLang: String)
}