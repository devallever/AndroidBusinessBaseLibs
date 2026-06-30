package com.plinkopro.wincash.ui.dialog

import android.annotation.SuppressLint
import android.content.Context

import com.lxj.xpopup.core.CenterPopupView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.databinding.DialogSeeAdBinding
import com.plinkopro.wincash.utils.setOnSingleListener

@SuppressLint("ViewConstructor")
class SeeAdDialog(context : Context, val moneyNum : Int,  val isSignIn : Boolean = false ,val claimCallBack : ()->  Unit) : CenterPopupView(context) {

    val binding  by lazy { DialogSeeAdBinding.bind(this.contentView) }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_see_ad
    }
    override fun onCreate() {
        super.onCreate()
        binding.apply {

            if (isSignIn) titleTv.text = context.getString(R.string.see_ad_sign_in)

            maxGoldNumTv.text = context.getString(R.string.max_gold_num ,moneyNum)

            closeIv.setOnSingleListener { dismiss() }

            claimTv.setOnSingleListener {
                claimCallBack.invoke()
                dismiss()
            }
        }
    }
}