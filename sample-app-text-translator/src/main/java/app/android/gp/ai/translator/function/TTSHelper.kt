package app.android.gp.ai.translator.function

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import app.woejt.wwzdndgl.lib.app.App
import app.woejt.wwzdndgl.lib.util.log
import app.woejt.wwzdndgl.lib.util.logRandomString
import app.woejt.wwzdndgl.lib.util.loge
import java.util.*

object TTSHelper {

    private var mSpeech: TextToSpeech? = null

    private var mInit = false

    fun init(context: Context) {
        logRandomString()
        mSpeech = TextToSpeech(
            context
        ) { status ->
            logRandomString()
            if (status == TextToSpeech.SUCCESS) {
                mInit = true
                log("init tts success")
            } else {
                loge("init tts fail")
            }
        }
    }

    fun speak(content: String, local: Locale) {
        if (!mInit) {
            init(App.context)
        }
        logRandomString()
        mSpeech?.language = local
        logRandomString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSpeech?.speak(
                content,
                TextToSpeech.QUEUE_FLUSH,
                null,
                System.currentTimeMillis().toString()
            )
            logRandomString()
        } else {
            mSpeech?.speak(content, TextToSpeech.QUEUE_FLUSH, null)
        }
    }
}