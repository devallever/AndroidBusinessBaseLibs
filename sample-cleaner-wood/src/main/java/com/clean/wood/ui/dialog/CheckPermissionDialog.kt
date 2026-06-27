package com.clean.wood.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import com.clean.wood.databinding.DialogCheckPermissionBinding
import com.clean.wood.utils.DisplayUtils

class CheckPermissionDialog(context: Context) :

    Dialog(context, com.google.android.material.R.style.Theme_Design_BottomSheetDialog) {

    interface Callback {
        fun onClickClose()
        fun onClickGo()
    }

    var callback: Callback? = null

    private var mBinding: DialogCheckPermissionBinding =
        DialogCheckPermissionBinding.inflate(layoutInflater)

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
        setCancelable(false)

        mBinding.apply {
            btnGo.setOnClickListener {
                callback?.onClickGo()
            }

            ivClose.setOnClickListener {
                dismiss()
                callback?.onClickClose()
            }
        }
    }
}