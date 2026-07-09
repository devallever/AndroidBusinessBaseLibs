package app.android.gp.ai.translator.util

import app.android.gp.ai.translator.R
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.toast

object ClipboardHelper {

    fun copy(content: String?) {
        val ret = ClipboardInterface.setText(content, App.context)
        if (ret) {
            toast(App.context.getString(R.string.tt_already_copied_to_clipboard))
        }
    }
}