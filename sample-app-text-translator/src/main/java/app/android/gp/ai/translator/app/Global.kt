package app.android.gp.ai.translator.app

import app.android.gp.ai.translator.language.Lang
import app.android.gp.ai.translator.language.LanguageHelper
import app.android.gp.ai.translator.translate.EngineType
import java.util.*

object Global {
    var langKeyCodeMap = mutableMapOf<String, String>()
    var langCodeKeyMap = mutableMapOf<String, String>()
    var langCodeLocalMap = mutableMapOf<String, Locale>()
    var langList = mutableListOf<Lang>()
    var engineList = mutableListOf<Int>()
    var searchCount = 0

    fun initLanguage() {
        
        langKeyCodeMap[Lang.AUTO.KEY] = LanguageHelper.AUTO()
        
        langKeyCodeMap[Lang.CHINESE.KEY] = LanguageHelper.CHINESE()
        
//        langKeyCodeMap[Lang.CHINESE_CANTONESE.KEY] = Lang.CHINESE_CANTONESE.CODE
        langKeyCodeMap[Lang.CHINESE_TRADITIONAL.KEY] = LanguageHelper.CHINESE_TRADITIONAL()
        langKeyCodeMap[Lang.ENGLISH.KEY] = LanguageHelper.ENGLISH()
        langKeyCodeMap[Lang.FRENCH.KEY] = LanguageHelper.FRENCH()
        
        langKeyCodeMap[Lang.GERMAN.KEY] = LanguageHelper.GERMAN()
        langKeyCodeMap[Lang.JAPANESE.KEY] = LanguageHelper.JAPANESE()
        
        langKeyCodeMap[Lang.RUSSIAN.KEY] = LanguageHelper.RUSSIAN()
        langKeyCodeMap[Lang.SPANISH.KEY] = LanguageHelper.SPANISH()
        langKeyCodeMap[Lang.ITALIAN.KEY] = LanguageHelper.ITALIAN()
        
        langKeyCodeMap[Lang.THAI.KEY] = LanguageHelper.THAI()

        langCodeKeyMap[LanguageHelper.AUTO()] = Lang.AUTO.KEY
        
        langCodeKeyMap[LanguageHelper.CHINESE()] = Lang.CHINESE.KEY
//        langCodeKeyMap[Lang.CHINESE_CANTONESE.CODE] = Lang.CHINESE_CANTONESE.KEY
        
        langCodeKeyMap[LanguageHelper.CHINESE_TRADITIONAL()] = Lang.CHINESE_TRADITIONAL.KEY
        langCodeKeyMap[LanguageHelper.ENGLISH()] = Lang.ENGLISH.KEY
        langCodeKeyMap[LanguageHelper.FRENCH()] = Lang.FRENCH.KEY
        langCodeKeyMap[LanguageHelper.GERMAN()] = Lang.GERMAN.KEY
        
        langCodeKeyMap[LanguageHelper.JAPANESE()] = Lang.JAPANESE.KEY
        langCodeKeyMap[LanguageHelper.RUSSIAN()] = Lang.RUSSIAN.KEY
        langCodeKeyMap[LanguageHelper.SPANISH()] = Lang.SPANISH.KEY
        
        langCodeKeyMap[LanguageHelper.ITALIAN()] = Lang.ITALIAN.KEY
        langCodeKeyMap[LanguageHelper.THAI()] = Lang.THAI.KEY

        //        langCodeLocalMap[LanguageHelper.AUTO()] = null
        langCodeLocalMap[LanguageHelper.CHINESE()] = Locale.SIMPLIFIED_CHINESE
        
//        langCodeKeyMap[Lang.CHINESE_CANTONESE.CODE] = Lang.CHINESE_CANTONESE.KEY
        langCodeLocalMap[LanguageHelper.CHINESE_TRADITIONAL()] = Locale.TRADITIONAL_CHINESE
        langCodeLocalMap[LanguageHelper.ENGLISH()] = Locale.ENGLISH
        
        langCodeLocalMap[LanguageHelper.FRENCH()] = Locale.FRENCH
        langCodeLocalMap[LanguageHelper.GERMAN()] = Locale.GERMAN
        
        
        langCodeLocalMap[LanguageHelper.JAPANESE()] = Locale.JAPANESE
//        langCodeLocalMap[LanguageHelper.RUSSIAN()] = Locale.
        
//        langCodeLocalMap[LanguageHelper.SPANISH()] = Locale.S
        langCodeLocalMap[LanguageHelper.ITALIAN()] = Locale.ITALIAN
//        langCodeLocalMap[LanguageHelper.THAI()] =

        langList.add(
            Lang.AUTO
        )
        
        langList.add(
            Lang.CHINESE
        )
        
//        langList.add(Lang.CHINESE_CANTONESE)
        langList.add(
            Lang.CHINESE_TRADITIONAL
        )
        
        langList.add(
            Lang.ENGLISH
        )
        
        langList.add(
            Lang.FRENCH
        )
        
        langList.add(
            Lang.GERMAN
        )
        langList.add(
            Lang.JAPANESE
        )
        
        langList.add(
            Lang.RUSSIAN
        )
        langList.add(
            Lang.SPANISH
        )
        
        langList.add(
            Lang.ITALIAN
        )
        
        langList.add(
            Lang.THAI
        )

        engineList.add(EngineType.BAIDU)
        
        engineList.add(EngineType.GOOGLE)

    }
}