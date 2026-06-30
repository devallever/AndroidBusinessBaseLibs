package com.plinkopro.wincash.ui.dialog

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.CaptchaView
import com.plinkopro.wincash.utils.setOnSingleListener
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager

class CaptchaDialog(
    context: Context,
    val onError: () -> Unit = {},
    val onSuccess: () -> Unit = {}
) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_account_captcha
    }

    override fun onCreate() {
        super.onCreate()

        val cv = findViewById<CaptchaView>(R.id.captchaContent)
        val et = findViewById<EditText>(R.id.et_input)
        findViewById<TextView>(R.id.tv_renew).apply {
            post {
                paint?.flags = Paint.UNDERLINE_TEXT_FLAG
                invalidate()
            }
            setOnSingleListener {
                cv.generateNewCaptcha()
            }
        }
        findViewById<View>(R.id.tv_submit).setOnSingleListener {
            val cvRes = cv.getCaptchaCode()
            val inputRes = et.text

            if (inputRes.isNullOrBlank()){
                Toast.makeText(context, R.string.input_captcha_hint, Toast.LENGTH_SHORT).show()
                cv.generateNewCaptcha()
                return@setOnSingleListener
            }

            if (inputRes.toString() != cvRes){
                Toast.makeText(context, R.string.txt_error_captcha_code, Toast.LENGTH_SHORT).show()
                cv.generateNewCaptcha()
                et.setText("")
                return@setOnSingleListener
            }

            if (cvRes == inputRes.toString()) {
                SdkManager.dot("verify_passed")
                dismiss()
                onSuccess.invoke()
            } else {
                dismiss()
                onError.invoke()
            }
        }
        findViewById<View>(R.id.close_iv).setOnSingleListener {
            dismiss()
        }
        SdkManager.dot("verify_show")
    }

    override fun getMaxWidth(): Int {
        return (XPopupUtils.getAppWidth(this.context))
    }
}
