package com.allever.video.editor.ui.widget

import android.view.View
import android.view.ViewConfiguration

object OnClickListenerHelper {
    fun setOnClickListener(view: View, onClickListener: View.OnClickListener,
                           disableDouble: Boolean = true,
                           doubleTimeout: Int = ViewConfiguration.getDoubleTapTimeout()) {
        view.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime: Long = 0

            override fun onClick(v: View?) {
                if (disableDouble) {
                    val time = System.currentTimeMillis()
                    if (time - lastClickTime < doubleTimeout)
                        return
                    lastClickTime = time
                }

                onClickListener.onClick(v)
            }

        })
    }
}
