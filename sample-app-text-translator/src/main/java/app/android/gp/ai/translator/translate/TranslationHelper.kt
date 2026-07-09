package app.android.gp.ai.translator.translate

import android.content.Context
import app.android.gp.ai.translator.event.LikeUpdateEventB
import app.android.gp.ai.translator.function.MediaHelper
import app.android.gp.ai.translator.network.NetworkHelper
import app.android.gp.ai.translator.function.SettingHelper
import app.android.gp.ai.translator.network.TTSRequestCallback
import app.android.gp.ai.translator.db.DBHelper
import app.android.gp.ai.translator.db.History
import app.android.gp.ai.translator.network.RetrofitUtil
import app.woejt.wwzdndgl.lib.util.ShareHelper
import app.woejt.wwzdndgl.lib.util.log
import org.greenrobot.eventbus.EventBus
import java.io.File

object TranslationHelper : ITranslateEngine {

    private var mEngine: ITranslateEngine? = null

    fun setEngine(context: Context, engine: ITranslateEngine) {
        mEngine = engine
    }

    override fun init(context: Context) {
        mEngine = if (SettingHelper.getDefaultTranslateEngine() == EngineType.GOOGLE) {
            GoogleEngine()
        } else {
            BaiduEngine()
        }
        mEngine?.init(context)
        RetrofitUtil.init(baseUrl())
    }

    override fun baseUrl() = mEngine?.baseUrl() ?: ""

    override fun translate(
        q: String,
        fromLang: String,
        toLang: String,
        callback: ITranslateCallback?
    ) {
        mEngine?.translate(q, fromLang, toLang, callback)
    }

    override fun tts(content: String, toLang: String) {
        mEngine?.tts(content, toLang)
    }

    fun playTTS(content: String, tl: String) {
        tts(content, tl)
    }

    fun isPlaying(): Boolean {
        return MediaHelper.isPlaying()
    }

    fun liked(history: History): History? {
        val result = DBHelper.like(history)
        var newHistory: History? = history
        if (result) {
            newHistory = DBHelper.getHistory(history.srcText, history.sl, history.tl)
            if (newHistory != null) {
                EventBus.getDefault().post(
                    LikeUpdateEventB(
                        newHistory
                    )
                )
            }
        } else {
        }

        return newHistory
    }

    fun shareAudio(obj: Any, content: String, tl: String) {
        val path = DBHelper.getTTSPath(content, tl)
        val file = File(path)
        if (file.exists()) {
            ShareHelper.shareAudio(obj, path)
            return
        }

        requestTTS(
            content,
            tl,
            Runnable {
                ShareHelper.shareAudio(
                    obj,
                    DBHelper.getTTSPath(
                        content,
                        tl
                    )
                )
            })
    }

    fun requestTTS(content: String, tl: String, runnable: Runnable?) {
        NetworkHelper.requestTTS(
            content,
            tl,
            object :
                TTSRequestCallback {
                override fun onSuccess(ttsPath: String) {
                    DBHelper.saveTTS(
                        content,
                        tl,
                        ttsPath
                    )
                    runnable?.run()
                }

                override fun onFail(msg: String) {
                    log(msg)
                }
            })
    }
}