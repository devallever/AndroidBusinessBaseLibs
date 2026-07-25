package app.allever.android.ai.qr.scanner.ui.widget.preference

import android.view.View

object AndroidInternalCompat {
    //com.android.internal.R$
    private val prefixChars = intArrayOf(
            99, 111, 109, 46, 97, 110, 100, 114, 111, 105, 100, 46, 105,
            110, 116, 101, 114, 110, 97, 108, 46, 82, 36)
    private val prefix = prefixChars.joinToString(separator = "") { it.toChar().toString() }

    private val maps = linkedMapOf<String, Int>()

    @JvmStatic
    fun getResId(name: String): Int {
        return getIdByType(
            "id",
            name
        )
    }

    @JvmStatic
    fun getAttrId(name: String): Int {
        return getIdByType(
            "attr",
            name
        )
    }

    @JvmStatic
    fun getIdByType(type: String, name: String): Int {
        val clazzName = "$prefix$type"
        return maps[clazzName] ?: try {
            val clazz = Class.forName(clazzName)
            val field = clazz.getField(name)
            field.isAccessible = true
            field.getInt(null)
        } catch (e: Exception) {
            View.NO_ID
        }
    }
}