package app.android.gp.ai.translator.function

import app.android.gp.ai.translator.language.Lang
import app.android.gp.ai.translator.translate.EngineType
import app.android.gp.ai.translator.util.SharedPrefUtils



object SettingHelper {

    private const val KEY_AUTO_PLAY_AUDIO = "KEY_AUTO_PLAY_AUDIO"
    private const val KEY_COPY_TO_CLIPBOARD = "KEY_COPY_TO_CLIPBOARD"
    private const val KEY_AUTO_TRANSLATE = "KEY_AUTO_TRANSLATE"
    private const val KEY_FOREGROUND_SERVICE = "KEY_FOREGROUND_SERVICE"
    private const val KEY_DEFAULT_TRANSLATE_LANG = "KEY_DEFAULT_TRANSLATE_LANG"
    private const val KEY_DEFAULT_TRANSLATE_ENGINE = "KEY_DEFAULT_TRANSLATE_ENGINE"

    fun setAutoPlayAudioSwitch(switch: Boolean) {
        
        SharedPrefUtils.putBoolean(KEY_AUTO_PLAY_AUDIO, switch)
    }

    fun getAutoPlayAudio(): Boolean {
        return SharedPrefUtils.getBoolean(KEY_AUTO_PLAY_AUDIO, true)
    }

    fun setCopyClipBoardSwitch(switch: Boolean) {
        
        SharedPrefUtils.putBoolean(KEY_COPY_TO_CLIPBOARD, switch)
    }

    fun getCopyClipBoard(): Boolean {
        
        return SharedPrefUtils.getBoolean(KEY_COPY_TO_CLIPBOARD, true)
    }

    fun setAutoTranslateSwitch(switch: Boolean) {
        
        SharedPrefUtils.putBoolean(KEY_AUTO_TRANSLATE, switch)
    }

    fun getAutoTranslate(): Boolean {
        
        return SharedPrefUtils.getBoolean(KEY_AUTO_TRANSLATE, true)
    }

    fun setForegroundServiceSwitch(switch: Boolean) {
        
        SharedPrefUtils.putBoolean(KEY_FOREGROUND_SERVICE, switch)
    }

    fun getForegroundServiceSwitch(): Boolean {
        
        return SharedPrefUtils.getBoolean(KEY_FOREGROUND_SERVICE, true)
    }

    fun setDefaultTranslateLang(translateLang: String) {
        
        SharedPrefUtils.putString(KEY_DEFAULT_TRANSLATE_LANG, translateLang)
    }

    fun getDefaultTranslateLangKey(): String {
        return SharedPrefUtils.getString(KEY_DEFAULT_TRANSLATE_LANG, Lang.CHINESE.KEY)
    }

    fun setDefaultTranslateEngine(translateEngine: Int) {
        
        SharedPrefUtils.putInt(KEY_DEFAULT_TRANSLATE_ENGINE, translateEngine)
    }

    fun getDefaultTranslateEngine(): Int {
        
        return SharedPrefUtils.getInt(KEY_DEFAULT_TRANSLATE_ENGINE, EngineType.GOOGLE)
    }
}