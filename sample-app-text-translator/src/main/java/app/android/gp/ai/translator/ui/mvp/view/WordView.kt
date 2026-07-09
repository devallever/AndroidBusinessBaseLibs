package app.android.gp.ai.translator.ui.mvp.view

import app.android.gp.ai.translator.ui.adapter.item.WordItem

interface WordView {
    fun updateWordList(data: MutableList<WordItem>)
}