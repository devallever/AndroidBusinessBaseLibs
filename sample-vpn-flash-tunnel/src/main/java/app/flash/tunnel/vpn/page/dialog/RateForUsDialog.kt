package app.flash.tunnel.vpn.page.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.databinding.DialogRateUsBinding
import app.flash.tunnel.vpn.lib.common.util.DisplayManager
import app.flash.tunnel.vpn.lib.common.util.toast

class RateForUsDialog(
    private val mContext: Context
) :
    Dialog(mContext, com.google.android.material.R.style.Theme_Design_BottomSheetDialog) {

    private var mBinding: DialogRateUsBinding = DialogRateUsBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window?.setGravity(Gravity.CENTER)
        window?.decorView?.setPadding(0, 0, 0, 0)
        val layoutParams = window?.attributes
        layoutParams?.width = DisplayManager.getScreenWidth() - DisplayManager.dip2px(32)
        window?.attributes = layoutParams
        window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
        setContentView(mBinding.root)
        setCancelable(false)

        mBinding.apply {
            btnConfirm.setOnClickListener {

                if (ratingBar.rating >= 5) {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
                    ).apply {
                        setPackage("com.android.vending")
                        try {
                            mContext.startActivity(this)
                            dismiss()
                        } catch (e: Exception) {
                            toast("no market")
                            dismiss()
                            e.printStackTrace()
                        }
                    }

                } else {
                    dismiss()
                }
            }
        }
    }

    override fun show() {
        super.show()
        mBinding.ratingBar.rating = 5f
    }

    override fun dismiss() {
        super.dismiss()
    }
}
