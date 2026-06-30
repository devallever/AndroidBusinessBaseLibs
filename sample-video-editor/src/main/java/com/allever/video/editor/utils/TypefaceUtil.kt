package com.allever.video.editor.utils

import android.content.Context
import android.graphics.Typeface

import java.util.HashMap

/**
 *
 */
object TypefaceUtil {

    private const val PREFIX_ASSET = "asset:"
    private val sCachedFonts = HashMap<String, Typeface>()

    /**
     * @param familyName if start with 'asset:' prefix, then load font from asset folder.
     * @return
     */
    fun load(context: Context, familyName: String?, style: Int): Typeface {
        if (familyName != null && familyName.startsWith(PREFIX_ASSET))
            synchronized(sCachedFonts) {
                val typeface = sCachedFonts[familyName]
                return typeface ?: try {
                    val typeface = Typeface.createFromAsset(context.assets, familyName.substring(PREFIX_ASSET.length))
                    sCachedFonts[familyName] = typeface
                    typeface
                } catch (e: Exception) {
                    Typeface.DEFAULT
                }
            }
        return Typeface.create(familyName, style)
    }
}
