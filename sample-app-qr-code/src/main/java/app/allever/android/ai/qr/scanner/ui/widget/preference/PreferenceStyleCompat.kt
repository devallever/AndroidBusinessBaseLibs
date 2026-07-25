package app.allever.android.ai.qr.scanner.ui.widget.preference

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.allever.app.qr.code.scaner.R

interface StyleCompat {
    var attrs: IntArray
    fun obtainStyledAttributes(context: Context, set: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
    fun bindView(view: View)
}

object StyleFactoryCompat {

    fun <T> get(attrs: IntArray, clazz: Class<T>, context: Context, set: AttributeSet, defStyleAttr: Int, defStyleRes: Int): T? {
        val newInstance = (clazz as Class<out StyleCompat>).newInstance() as StyleCompat
        newInstance?.obtainStyledAttributes(context, set, defStyleAttr, defStyleRes)
        return newInstance as? T
    }
}

class PreferenceStyleCompat : StyleCompat {

    override var attrs = R.styleable.CustomPreference

    var titleTextColor: ColorStateList? = null
        private set
    var titleTextStyle: Int? = null
        private set
    var titleTextSize: Int? = null
        private set
    var titleTextAllCaps: Boolean? = null
        private set
    var titleTextDrawableRight: Drawable? = null
        private set
    var titleTextDrawablePadding: Int? = null
        private set
    var summaryTextColor: ColorStateList? = null
        private set
    var summaryTextStyle: Int? = null
        private set
    var summaryTextSize: Int? = null
        private set
    var summaryTextAllCaps: Boolean? = null
        private set
    var background: Drawable? = null
        private set

    override fun obtainStyledAttributes(context: Context, set: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val a = context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)
        for (i in a.indexCount - 1 downTo 0) {
            val attr = a.getIndex(i)
            when (attr) {
                R.styleable.CustomPreference_cpTitleTextColor -> titleTextColor = a.getColorStateList(attr)
                R.styleable.CustomPreference_cpTitleTextStyle -> titleTextStyle = a.getInt(attr, -1)
                R.styleable.CustomPreference_cpTitleTextSize -> titleTextSize = a.getDimensionPixelSize(attr, 15)
                R.styleable.CustomPreference_cpTitleTextDrawablePadding -> titleTextDrawablePadding = a.getDimensionPixelSize(attr, 15)
                R.styleable.CustomPreference_cpTitleTextAllCaps -> titleTextAllCaps = a.getBoolean(attr, false)
                R.styleable.CustomPreference_cpTitleTextDrawableRight -> titleTextDrawableRight = a.getDrawable(attr)
                R.styleable.CustomPreference_cpSummaryTextColor -> summaryTextColor = a.getColorStateList(attr)
                R.styleable.CustomPreference_cpSummaryTextStyle -> summaryTextStyle = a.getInt(attr, -1)
                R.styleable.CustomPreference_cpSummaryTextSize -> summaryTextSize = a.getDimensionPixelSize(attr, 15)
                R.styleable.CustomPreference_cpSummaryTextAllCaps -> summaryTextAllCaps = a.getBoolean(attr, false)
                R.styleable.CustomPreference_cpBackground -> background = a.getDrawable(attr)
                else -> {
                }
            }
        }
        a.recycle()
    }

    override fun bindView(view: View) {
        val titleView = view.findViewById<View>(AndroidInternalCompat.getResId("title")) as? TextView
        if (titleView != null) {
            if (titleTextColor != null) {
                titleView.setTextColor(titleTextColor)
            }
            if (titleTextStyle != null) {
                titleView.setTypeface(titleView.typeface, titleTextStyle!!)
            }
            if (titleTextSize != null) {
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize!!.toFloat())
            }
            if (titleTextAllCaps != null) {
                titleView.setAllCaps(titleTextAllCaps!!)
            }
            if (titleTextDrawableRight != null) {
                titleTextDrawableRight!!.setBounds(0, 0, titleTextDrawableRight!!.intrinsicWidth, titleTextDrawableRight!!.intrinsicHeight)
                titleView.setCompoundDrawables(null, null, titleTextDrawableRight, null)
            }
            if (titleTextDrawablePadding != null) {
                titleView.compoundDrawablePadding = titleTextDrawablePadding!!.toInt()
            }
        }
        val summaryView = view.findViewById<View>(AndroidInternalCompat.getResId("summary")) as? TextView
        if (summaryView != null) {
            if (summaryTextColor != null) {
                summaryView.setTextColor(summaryTextColor)
            }
            if (summaryTextStyle != null) {
                summaryView.setTypeface(summaryView.typeface, summaryTextStyle!!)
            }
            if (summaryTextSize != null) {
                summaryView.setTextSize(TypedValue.COMPLEX_UNIT_PX, summaryTextSize!!.toFloat())
            }
            if (summaryTextAllCaps != null) {
                summaryView.setAllCaps(summaryTextAllCaps!!)
            }
        }
    }

}