package app.flash.tunnel.vpn.page.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import app.flash.tunnel.vpn.databinding.DialogAppendTimeBinding
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.common.util.DisplayManager

class RewardTipsDialog(
    context: Context, private val callback: (dialog: Dialog) -> Unit
) :
    Dialog(context, com.google.android.material.R.style.Theme_Design_BottomSheetDialog) {

    private var mBinding: DialogAppendTimeBinding = DialogAppendTimeBinding.inflate(layoutInflater)

    override fun show() {
        AdHelper.loadRewardDialogNative(mBinding.adContainer)
        super.show()
    }

    override fun dismiss() {
        AdHelper.destroyNative(mBinding.adContainer)
        super.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.apply {
            setGravity(Gravity.CENTER)
            decorView.setPadding(0, 0, 0, 0)
            val layoutParams = window?.attributes
            layoutParams?.width = DisplayManager.dip2px(300)
            attributes = layoutParams
            decorView.setBackgroundColor(Color.TRANSPARENT)
        }
        setContentView(mBinding.root)

        mBinding.btnConfirm.setOnClickListener {
            callback.invoke(this)
        }
    }

}