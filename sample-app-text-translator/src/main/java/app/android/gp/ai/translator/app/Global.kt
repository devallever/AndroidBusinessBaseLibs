package app.android.gp.ai.translator.app

import app.android.gp.ai.translator.language.Lang
import app.android.gp.ai.translator.language.LanguageHelper
import app.android.gp.ai.translator.translate.EngineType
import app.woejt.wwzdndgl.lib.util.log
import app.woejt.wwzdndgl.lib.util.logRandomString
import app.woejt.wwzdndgl.lib.util.logRandomTime
import java.util.*

object Global {
    var langKeyCodeMap = mutableMapOf<String, String>()
    var langCodeKeyMap = mutableMapOf<String, String>()
    var langCodeLocalMap = mutableMapOf<String, Locale>()
    var langList = mutableListOf<Lang>()
    var engineList = mutableListOf<Int>()
    var searchCount = 0

    fun initLanguage() {
        logRandomTime()
        langKeyCodeMap[Lang.AUTO.KEY] = LanguageHelper.AUTO()
        logRandomTime()
        langKeyCodeMap[Lang.CHINESE.KEY] = LanguageHelper.CHINESE()
        logRandomTime()
//        langKeyCodeMap[Lang.CHINESE_CANTONESE.KEY] = Lang.CHINESE_CANTONESE.CODE
        langKeyCodeMap[Lang.CHINESE_TRADITIONAL.KEY] = LanguageHelper.CHINESE_TRADITIONAL()
        langKeyCodeMap[Lang.ENGLISH.KEY] = LanguageHelper.ENGLISH()
        langKeyCodeMap[Lang.FRENCH.KEY] = LanguageHelper.FRENCH()
        logRandomTime()
        langKeyCodeMap[Lang.GERMAN.KEY] = LanguageHelper.GERMAN()
        langKeyCodeMap[Lang.JAPANESE.KEY] = LanguageHelper.JAPANESE()
        logRandomTime()
        langKeyCodeMap[Lang.RUSSIAN.KEY] = LanguageHelper.RUSSIAN()
        langKeyCodeMap[Lang.SPANISH.KEY] = LanguageHelper.SPANISH()
        langKeyCodeMap[Lang.ITALIAN.KEY] = LanguageHelper.ITALIAN()
        logRandomTime()
        langKeyCodeMap[Lang.THAI.KEY] = LanguageHelper.THAI()

        langCodeKeyMap[LanguageHelper.AUTO()] = Lang.AUTO.KEY
        logRandomTime()
        langCodeKeyMap[LanguageHelper.CHINESE()] = Lang.CHINESE.KEY
//        langCodeKeyMap[Lang.CHINESE_CANTONESE.CODE] = Lang.CHINESE_CANTONESE.KEY
        logRandomTime()
        langCodeKeyMap[LanguageHelper.CHINESE_TRADITIONAL()] = Lang.CHINESE_TRADITIONAL.KEY
        langCodeKeyMap[LanguageHelper.ENGLISH()] = Lang.ENGLISH.KEY
        langCodeKeyMap[LanguageHelper.FRENCH()] = Lang.FRENCH.KEY
        langCodeKeyMap[LanguageHelper.GERMAN()] = Lang.GERMAN.KEY
        logRandomTime()
        langCodeKeyMap[LanguageHelper.JAPANESE()] = Lang.JAPANESE.KEY
        langCodeKeyMap[LanguageHelper.RUSSIAN()] = Lang.RUSSIAN.KEY
        langCodeKeyMap[LanguageHelper.SPANISH()] = Lang.SPANISH.KEY
        logRandomTime()
        langCodeKeyMap[LanguageHelper.ITALIAN()] = Lang.ITALIAN.KEY
        langCodeKeyMap[LanguageHelper.THAI()] = Lang.THAI.KEY

        //        langCodeLocalMap[LanguageHelper.AUTO()] = null
        langCodeLocalMap[LanguageHelper.CHINESE()] = Locale.SIMPLIFIED_CHINESE
        logRandomTime()
//        langCodeKeyMap[Lang.CHINESE_CANTONESE.CODE] = Lang.CHINESE_CANTONESE.KEY
        langCodeLocalMap[LanguageHelper.CHINESE_TRADITIONAL()] = Locale.TRADITIONAL_CHINESE
        langCodeLocalMap[LanguageHelper.ENGLISH()] = Locale.ENGLISH
        logRandomTime()
        langCodeLocalMap[LanguageHelper.FRENCH()] = Locale.FRENCH
        langCodeLocalMap[LanguageHelper.GERMAN()] = Locale.GERMAN
        logRandomTime()
        logRandomTime()
        langCodeLocalMap[LanguageHelper.JAPANESE()] = Locale.JAPANESE
//        langCodeLocalMap[LanguageHelper.RUSSIAN()] = Locale.
        logRandomTime()
//        langCodeLocalMap[LanguageHelper.SPANISH()] = Locale.S
        langCodeLocalMap[LanguageHelper.ITALIAN()] = Locale.ITALIAN
//        langCodeLocalMap[LanguageHelper.THAI()] =

        langList.add(
            Lang.AUTO
        )
        logRandomTime()
        langList.add(
            Lang.CHINESE
        )
        logRandomTime()
//        langList.add(Lang.CHINESE_CANTONESE)
        langList.add(
            Lang.CHINESE_TRADITIONAL
        )
        logRandomString()
        langList.add(
            Lang.ENGLISH
        )
        logRandomString()
        langList.add(
            Lang.FRENCH
        )
        logRandomString()
        langList.add(
            Lang.GERMAN
        )
        langList.add(
            Lang.JAPANESE
        )
        logRandomString()
        langList.add(
            Lang.RUSSIAN
        )
        langList.add(
            Lang.SPANISH
        )
        logRandomString()
        langList.add(
            Lang.ITALIAN
        )
        logRandomString()
        langList.add(
            Lang.THAI
        )

        engineList.add(EngineType.BAIDU)
        logRandomString()
        engineList.add(EngineType.GOOGLE)

    }
}