package app.android.gp.ai.translator.language

enum class Lang(
    val KEY: String,
    val CODE: String
) {
    /** 自动检测 */
    ALL("All", ""),

    /** 自动检测 */
    AUTO("Automatic", "auto"),

    /** 中文 */
//    CHINESE("中文", "zh-CN"),
    CHINESE("中文", "zh"),

    /** 中文(繁体) */
//    CHINESE_TRADITIONAL("中文(繁体)", "zh-TW"),
    CHINESE_TRADITIONAL("中文(繁体)", "cht"),
    /** 粤语 */
//    CHINESE_CANTONESE("粤语", "zh-CANTONESE"),
    /** 英语 */
    ENGLISH("English", "en"),

    /** 法语 */
//    FRENCH("French", "fr"),
    FRENCH("French", "fra"),

    /** 德语 */
    GERMAN("German", "de"),

    /** 日语 */
//    JAPANESE("Japanese", "ja"),
    JAPANESE("Japanese", "jp"),

    /** 俄语 */
    RUSSIAN("Russian", "ru"),

    /** 西班牙语 */
//    SPANISH("Spanish", "es"),
    SPANISH("Spanish", "spa"),

    /** 意大利语 */
    ITALIAN("Italian", "it"),

    /** 泰语 */
    THAI("Thai", "th"),
    ;

    fun getKeyByCode(code: String) {
    }
}