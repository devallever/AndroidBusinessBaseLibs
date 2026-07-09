package app.android.gp.ai.translator.app

import android.content.ClipboardManager
import android.content.Context
import app.allever.android.lib.core.app.App
import app.android.gp.ai.translator.function.SettingHelper
import app.android.gp.ai.translator.translate.TranslationHelper
import app.android.gp.ai.translator.ui.DialogTranslatePage
import app.allever.android.lib.core.helper.ActivityHelper
import org.litepal.LitePal

object MyApp {
    fun onCreate() {
        TranslationHelper.init(App.context)
        

        Global.initLanguage()
        
        LitePal.initialize(App.context)
        
        val clipBoardManager = App.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipBoardManager.addPrimaryClipChangedListener {
            if (ActivityHelper.size() == 0 && SettingHelper.getAutoTranslate()) {
                val srcText = clipBoardManager.primaryClip?.getItemAt(0)?.text.toString()
                DialogTranslatePage.start(App.context, srcText)
            }
        }
    }
}