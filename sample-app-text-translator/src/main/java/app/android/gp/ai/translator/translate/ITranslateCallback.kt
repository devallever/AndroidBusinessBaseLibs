package app.android.gp.ai.translator.translate

import app.android.gp.ai.translator.bean.TranslateResult

interface ITranslateCallback {
    fun onTranslateResult(result: TranslateResult?)
}