package com.allever.video.editor.function.sticker

import com.android.absbase.App
import com.allever.video.editor.R
import com.allever.video.editor.function.emoji.EmojiUtils
import com.allever.video.editor.function.EffectResource
import com.allever.video.editor.function.InsideResource

import java.io.File
import java.io.IOException
import java.util.LinkedHashMap

/**
 * 内置贴纸
 */

object InsideStickerTool {

    abstract class StickerInsideResource : InsideResource() {
//        var isAssertRes = false
//            protected set
//        var paths: Array<String>? = null
//            protected set
//        var names: Array<String>? = null
//            protected set
//        var ids: IntArray? = null
//            protected set
//        var iconResId: Int = 0
//            protected set
//        var iconAssertPath: String? = null
//            protected set

        protected abstract fun init()
    }


    val STICKER_PKG_NAME_EMOJI = "com.photoeditor.plugins.sticker.inside.emoji"
    val STICKER_PKG_NAME_SOCIAL = "com.photoeditor.plugins.sticker.inside.social"
    val STICKER_INSIDE_INFO: MutableMap<String, StickerInsideResource>

    val sStickerInsideEmojiInfo: StickerInsideEmojiRes
    val sStickerInsideSocialInfo: StickerInsideSocialRes

    init {
        sStickerInsideEmojiInfo = StickerInsideEmojiRes()
        sStickerInsideEmojiInfo.init()

        sStickerInsideSocialInfo = StickerInsideSocialRes()
        sStickerInsideSocialInfo.init()

        STICKER_INSIDE_INFO = LinkedHashMap()
        STICKER_INSIDE_INFO[STICKER_PKG_NAME_EMOJI] = sStickerInsideEmojiInfo
        STICKER_INSIDE_INFO[STICKER_PKG_NAME_SOCIAL] = sStickerInsideSocialInfo
    }

    class StickerInsideEmojiRes : StickerInsideResource() {

        public override fun init() {
            name = "emoji"
            names = App.getContext().resources.getStringArray(R.array.sticker_emojis)
            paths = names?.mapNotNull {
                EmojiUtils.getEmojiAssertPath(it)
            }?.toTypedArray()
            packageName = STICKER_PKG_NAME_EMOJI
            isAssertRes = true
            ids = null
            iconResId = R.drawable.gird_icon_emoji
        }

        companion object {
            private val TAG = EmojiUtils::class.java.name
        }
    }

    class StickerInsideSocialRes : StickerInsideResource() {

        public override fun init() {
            name = "social"
            packageName = STICKER_PKG_NAME_SOCIAL
            isAssertRes = true
            try {
                val files = App.getContext().assets.list(ASSERT_IMG_DIR)
                names = files?.mapNotNull {
                    it
                }?.toTypedArray()
                paths = names?.map {
                    ASSERT_IMG_DIR + File.separator + it
                }?.toTypedArray()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            ids = null
            //            mIconResId = 0;
            iconResId = R.drawable.sticker_logo_social
            iconAssertPath = ASSERT_LOGO
        }

        companion object {
            private val TAG = EmojiUtils::class.java.name
            private val ASSERT_IMG_DIR = "stickers/sociality/512"
            private val ASSERT_LOGO = "stickers/sociality/logo/logo.png"
        }

    }
}
