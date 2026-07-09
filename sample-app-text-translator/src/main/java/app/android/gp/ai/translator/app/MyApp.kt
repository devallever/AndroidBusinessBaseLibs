package app.android.gp.ai.translator.app

import android.content.ClipboardManager
import android.content.Context
import app.android.gp.ai.translator.function.SettingHelper
import app.android.gp.ai.translator.translate.TranslationHelper
import app.android.gp.ai.translator.ui.DialogTranslatePage
import app.woejt.wwzdndgl.lib.app.App
import app.woejt.wwzdndgl.lib.util.ActivityCollector
import app.woejt.wwzdndgl.lib.util.log
import app.woejt.wwzdndgl.lib.util.logRandomString
import com.allever.android.lib.admob.AdConfig
import com.allever.android.lib.admob.AdDevManager
import com.allever.android.lib.admob.AdManager
import org.litepal.LitePal

class MyApp : App() {
    override fun onCreate() {
        super.onCreate()
        AdManager.init(AdConfig(), this)
        AdDevManager.init(this)
        logRandomString()
        com.android.absbase.App.setContext(this@MyApp)
        logRandomString()
        TranslationHelper.init(this)
        logRandomString()

        Global.initLanguage()
        logRandomString()
        LitePal.initialize(this@MyApp)
        logRandomString()
        val clipBoardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipBoardManager.addPrimaryClipChangedListener {
            if (ActivityCollector.size() == 0 && SettingHelper.getAutoTranslate()) {
                val srcText = clipBoardManager.primaryClip?.getItemAt(0)?.text.toString()
                DialogTranslatePage.start(this, srcText)
            }
        }
    }
}