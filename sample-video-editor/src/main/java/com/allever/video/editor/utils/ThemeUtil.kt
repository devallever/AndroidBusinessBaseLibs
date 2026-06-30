package com.allever.video.editor.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.os.Build
import androidx.appcompat.widget.TintTypedArray
import android.util.TypedValue

import com.allever.video.editor.R


object ThemeUtil {

    private var value: TypedValue? = null

    fun dpToPx(context: Context, dp: Int): Int {
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics) + 0.5f).toInt()
    }

    fun spToPx(context: Context, sp: Int): Int {
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics) + 0.5f).toInt()
    }

    private fun getColor(context: Context, id: Int, defaultValue: Int): Int {
        if (value == null)
            value = TypedValue()

        try {
            val theme = context.theme
            if (theme != null && theme.resolveAttribute(id,
                    value, true)) {
                if (value!!.type >= TypedValue.TYPE_FIRST_INT && value!!.type <= TypedValue.TYPE_LAST_INT)
                    return value!!.data
                else if (value!!.type == TypedValue.TYPE_STRING)
                    return context.resources.getColor(value!!.resourceId)
            }
        } catch (ex: Exception) {
        }

        return defaultValue
    }

    fun windowBackground(context: Context, defaultValue: Int): Int {
        return getColor(
            context,
            android.R.attr.windowBackground,
            defaultValue
        )
    }

    fun textColorPrimary(context: Context, defaultValue: Int): Int {
        return getColor(
            context,
            android.R.attr.textColorPrimary,
            defaultValue
        )
    }

    fun textColorSecondary(context: Context, defaultValue: Int): Int {
        return getColor(
            context,
            android.R.attr.textColorSecondary,
            defaultValue
        )
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun colorPrimary(context: Context, defaultValue: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getColor(
            context,
            android.R.attr.colorPrimary,
            defaultValue
        ) else getColor(
            context,
            R.attr.colorPrimary,
            defaultValue
        )

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun colorPrimaryDark(context: Context, defaultValue: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getColor(
            context,
            android.R.attr.colorPrimaryDark,
            defaultValue
        ) else getColor(
            context,
            R.attr.colorPrimaryDark,
            defaultValue
        )

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun colorAccent(context: Context, defaultValue: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getColor(
            context,
            android.R.attr.colorAccent,
            defaultValue
        ) else getColor(
            context,
            R.attr.colorAccent,
            defaultValue
        )

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun colorControlNormal(context: Context, defaultValue: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getColor(
            context,
            android.R.attr.colorControlNormal,
            defaultValue
        ) else getColor(
            context,
            R.attr.colorControlNormal,
            defaultValue
        )

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun colorControlActivated(context: Context, defaultValue: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getColor(
            context,
            android.R.attr.colorControlActivated,
            defaultValue
        ) else getColor(
            context,
            R.attr.colorControlActivated,
            defaultValue
        )

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun colorControlHighlight(context: Context, defaultValue: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getColor(
            context,
            android.R.attr.colorControlHighlight,
            defaultValue
        ) else getColor(
            context,
            R.attr.colorControlHighlight,
            defaultValue
        )

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun colorButtonNormal(context: Context, defaultValue: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getColor(
            context,
            android.R.attr.colorButtonNormal,
            defaultValue
        ) else getColor(
            context,
            R.attr.colorButtonNormal,
            defaultValue
        )

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun colorSwitchThumbNormal(context: Context, defaultValue: Int): Int {
        return getColor(
            context,
            R.attr.colorSwitchThumbNormal,
            defaultValue
        )
    }

    fun getType(array: TypedArray, index: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return array.getType(index)
        else {
            val value = array.peekValue(index)
            return value?.type ?: TypedValue.TYPE_NULL
        }
    }

    fun getString(array: TypedArray, index: Int, defaultValue: CharSequence): CharSequence {
        val result = array.getString(index)
        return result ?: defaultValue
    }

    fun getString(array: TintTypedArray, index: Int, defaultValue: CharSequence): CharSequence {
        val result = array.getString(index)
        return result ?: defaultValue
    }
}
