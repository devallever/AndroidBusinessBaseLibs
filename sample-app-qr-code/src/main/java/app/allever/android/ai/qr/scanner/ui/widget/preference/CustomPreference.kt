package app.allever.android.ai.qr.scanner.ui.widget.preference

import android.content.Context
import android.os.Build
import android.preference.Preference
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View

import com.allever.app.qr.code.scaner.R

class CustomPreference : Preference {

    internal var preferenceStyleCompat: PreferenceStyleCompat? = null

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val defStyleAttr = AndroidInternalCompat.getAttrId("preferenceStyle")
        init(context, attrs, defStyleAttr, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        preferenceStyleCompat = StyleFactoryCompat.get(
            R.styleable.CustomPreference,
            PreferenceStyleCompat::class.java,
            context,
            attrs!!,
            defStyleAttr,
            defStyleRes
        )
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        if (preferenceStyleCompat != null) {
            preferenceStyleCompat!!.bindView(view)
        }
    }
}
