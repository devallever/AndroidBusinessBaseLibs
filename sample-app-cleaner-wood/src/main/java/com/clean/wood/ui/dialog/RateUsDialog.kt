package com.clean.wood.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import app.allever.android.lib.core.app.App
import com.clean.wood.databinding.DialogRateBinding
import com.clean.wood.utils.DisplayUtils

class RateUsDialog(context: Context) :

    Dialog(context, com.google.android.material.R.style.Theme_Design_BottomSheetDialog) {

    private var mBinding: DialogRateBinding =
        DialogRateBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window?.setGravity(Gravity.CENTER)
        window?.decorView?.setPadding(0, 0, 0, 0)
        val layoutParams = window?.attributes
        layoutParams?.width = DisplayUtils.dip2px(292)
        window?.attributes = layoutParams
        window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
        setContentView(mBinding.root)
        setCancelable(true)

        mBinding.apply {
            btnGo.setOnClickListener {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=app.allever.android.lib.cleaner.wood")
                ).apply {
                    setPackage("com.android.vending")
                    try {
                        context.startActivity(this)
                        dismiss()
                    } catch (e: Exception) {
                        dismiss()
                        e.printStackTrace()
                    }
                }
            }

            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }
}