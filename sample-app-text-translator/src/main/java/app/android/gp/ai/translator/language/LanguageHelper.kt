package app.android.gp.ai.translator.language

object LanguageHelper : ILangCode {

    private var mLangCode: ILangCode? = null

    fun init(iLangCode: ILangCode) {
        mLangCode = iLangCode
    }

    override fun CHINESE() = mLangCode?.CHINESE() ?: ""

    override fun CHINESE_TRADITIONAL() = mLangCode?.CHINESE_TRADITIONAL() ?: ""

    override fun ENGLISH() = mLangCode?.ENGLISH() ?: ""

    override fun FRENCH() = mLangCode?.FRENCH() ?: ""

    override fun GERMAN() = mLangCode?.GERMAN() ?: ""

    override fun JAPANESE() = mLangCode?.JAPANESE() ?: ""

    override fun RUSSIAN() = mLangCode?.RUSSIAN() ?: ""

    override fun SPANISH() = mLangCode?.SPANISH() ?: ""

    override fun ITALIAN() = mLangCode?.ITALIAN() ?: ""

    override fun THAI() = mLangCode?.THAI() ?: ""
}