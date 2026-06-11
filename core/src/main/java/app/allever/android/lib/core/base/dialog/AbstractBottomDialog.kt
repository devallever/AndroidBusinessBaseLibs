package app.allever.android.lib.core.base.dialog

import android.content.Context
import android.view.Gravity

abstract class AbstractBottomDialog(context: Context) :
    AbstractDialog(context, 0) {
    override fun getGravity() = Gravity.BOTTOM
}