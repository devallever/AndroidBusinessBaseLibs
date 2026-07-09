package app.android.gp.ai.translator.util

import app.android.gp.ai.translator.R
import app.woejt.wwzdndgl.lib.app.App
import app.woejt.wwzdndgl.lib.util.getString
import app.woejt.wwzdndgl.lib.util.toast

object ClipboardHelper {

    fun copy(content: String?) {
        val ret = ClipboardInterface.setText(content, App.context)
        if (ret) {
            toast(getString(R.string.already_copied_to_clipboard))
        }
    }
}