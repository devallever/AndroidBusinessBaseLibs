package com.plinkopro.wincash.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.plinkopro.wincash.base.BaseApplication

object ToastUtil {

    private val context: Context by lazy { BaseApplication.instance }


    fun showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT){
        show(context.getString(text), duration)
    }


    fun showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT){
        show(text, duration)
    }


    private fun show(text: CharSequence, duration: Int){
        Toast.makeText(context, text, duration).show()
    }



//    private val adToast: Toast by lazy { Toast(BaseApplication.instance) }
//    private val adToastView: View by lazy {
//        LayoutInflater.from(BaseApplication.instance).inflate(R.layout.view_ad_toast, null)
//    }
//
//    fun showAdToast(text: String? = null) {
//        adToast.view = adToastView
//        if (!text.isNullOrBlank()) {
//            adToastView.findViewById<TextView>(R.id.tv_content).text = text
//        }
//        adToast.setGravity(Gravity.CENTER or Gravity.FILL_HORIZONTAL, 0, 0)
//        adToast.duration = Toast.LENGTH_LONG
//        adToast.show()
//    }
    
}