package app.android.gp.ai.translator.ui.mvp.view

import app.android.gp.ai.translator.bean.TranslateResult

interface TranslationView {
    fun updateResult(
        bean: TranslateResult
    )

    fun refreshLiked(liked: Boolean)

    fun showOrHideSoundSrcSymbol(show: Boolean)
    fun showOrHideSoundTranslateSymbol(show: Boolean)
    fun showOrHideDictInfo(show: Boolean)
}