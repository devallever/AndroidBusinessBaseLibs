package com.allever.video.editor.ui.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.TextView
import com.allever.video.editor.R

object DialogUtils {

    fun show(
        context: Context, titleResId: Int, detailResId: Int, cancelResId: Int, okResId: Int,
        okListener: (() -> Unit)? = null,
        cancelListener: (() -> Unit)? = null
    ) {
        val dialog = AlertDialog.Builder(context).create()
        dialog.show()
        val window = dialog.window
        window!!.setContentView(R.layout.layout_alert_dialog)
        val titleView = window.findViewById(R.id.title) as TextView
        val detailView = window.findViewById(R.id.detail) as TextView
        val cancelView = window.findViewById(R.id.btn_cancel) as TextView
        val okView = window.findViewById(R.id.btn_ok) as TextView
        if (titleResId == -1) {
            titleView.visibility = View.GONE
        } else {
            titleView.setText(titleResId)
        }
        if (detailResId == -1) {
            detailView.visibility = View.GONE
        } else {
            detailView.setText(detailResId)
        }

        cancelView.setText(cancelResId)
        cancelView.setOnClickListener {
            cancelListener?.invoke()
            dialog.dismiss()
        }
        okView.setText(okResId)
        okView.setOnClickListener {
            okListener?.invoke()
            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
    }
}