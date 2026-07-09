package app.android.gp.ai.translator.function

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log

import app.allever.android.lib.core.ext.logE
import java.util.*

object TTSHelper {

    private var mSpeech: TextToSpeech? = null

    private var mInit = false

    fun init(context: Context) {
        
        mSpeech = TextToSpeech(
            context
        ) { status ->
            
            if (status == TextToSpeech.SUCCESS) {
                mInit = true
                log("init tts success")
            } else {
                logE("init tts fail")
            }
        }
    }

    fun speak(content: String, local: Locale) {
        if (!mInit) {
            init(App.context)
        }
        
        mSpeech?.language = local
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSpeech?.speak(
                content,
                TextToSpeech.QUEUE_FLUSH,
                null,
                System.currentTimeMillis().toString()
            )
            
        } else {
            mSpeech?.speak(content, TextToSpeech.QUEUE_FLUSH, null)
        }
    }
}