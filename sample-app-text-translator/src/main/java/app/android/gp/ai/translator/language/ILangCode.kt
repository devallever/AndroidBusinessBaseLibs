package app.android.gp.ai.translator.language

interface ILangCode {
    fun ALL() = ""
    fun AUTO() = "auto"
    fun CHINESE(): String
    fun CHINESE_TRADITIONAL(): String
    fun ENGLISH(): String
    fun FRENCH(): String
    fun GERMAN(): String
    fun JAPANESE(): String
    fun RUSSIAN(): String
    fun SPANISH(): String
    fun ITALIAN(): String
    fun THAI(): String
}