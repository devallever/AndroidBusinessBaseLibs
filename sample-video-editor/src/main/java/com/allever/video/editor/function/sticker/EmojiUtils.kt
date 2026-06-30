package com.allever.video.editor.function.sticker

import android.graphics.Bitmap

import com.android.absbase.App
import com.allever.video.editor.utils.AssetsUtil

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.HashMap

object EmojiUtils {
    private val EMOJI_ASSERT_DIR = "emoji"
    private val sEmojiMaps = HashMap<String, String>()

    init {
        try {
            val files = App.getContext().assets.list(EMOJI_ASSERT_DIR)
            for (file in files!!) {
                val cols = file.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                sEmojiMaps[cols[0].toLowerCase()] = EMOJI_ASSERT_DIR + File.separator + file
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun getEmojiAssertPath(uni: String): String? {
        val path = sEmojiMaps[uni.toLowerCase()]
        return path
    }

    fun getEmojiInputStream(uni: String): InputStream? {
        val emojiAssertPath = getEmojiAssertPath(uni)
        return AssetsUtil.toInputStream(emojiAssertPath)
    }

    fun getEmojiBitmap(uni: String): Bitmap? {
        val emojiAssertPath = getEmojiAssertPath(uni)
        return AssetsUtil.toBitmap(emojiAssertPath)
    }
}
