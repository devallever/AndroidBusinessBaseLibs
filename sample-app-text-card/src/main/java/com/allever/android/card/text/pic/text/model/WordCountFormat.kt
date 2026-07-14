package com.allever.android.card.text.pic.text.model

import com.allever.android.card.text.pic.text.R

class WordCountFormat {
    companion object {
        val FORMAT_WORD_COUNT = R.string.tc_word_format_word_count
        val FORMAT_COUNT_WORD = R.string.tc_word_format_count_word
        val FORMAT_COUNT = R.string.tc_word_format_count

        val FORMAT_LIST = mutableListOf(
            FORMAT_WORD_COUNT,
            FORMAT_COUNT_WORD,
            FORMAT_COUNT
        )
    }
}