package app.flash.tunnel.vpn.page.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import androidx.core.view.isVisible
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.databinding.DialogCommonBinding
import app.flash.tunnel.vpn.lib.common.util.DisplayManager

class AppDialog(
    context: Context,
    var message: String = "",
    var icon: Int = R.drawable.icon_dialog_connect_fail,
    var confirmText: String = TunnelApp.context.getString(R.string.ok),
    var showClose: Boolean = true
) :
    Dialog(context, com.google.android.material.R.style.Theme_Design_BottomSheetDialog) {


    private var mBinding: DialogCommonBinding = DialogCommonBinding.inflate(layoutInflater)

    var callback: (dialog: Dialog) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window?.setGravity(Gravity.CENTER)
        window?.decorView?.setPadding(0, 0, 0, 0)
        val layoutParams = window?.attributes
        layoutParams?.width = DisplayManager.getScreenWidth() - DisplayManager.dip2px(32)
        window?.attributes = layoutParams
//        window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
        setContentView(mBinding.root)

        mBinding.apply {
            ivClose.isVisible = showClose
            ivClose.setOnClickListener {
                dismiss()
            }

            ivIcon.setImageResource(icon)

            tvMessage.text = message

            btnConfirm.text = confirmText
            btnConfirm.setOnClickListener {
                callback.invoke(this@AppDialog)
            }
        }
    }
}