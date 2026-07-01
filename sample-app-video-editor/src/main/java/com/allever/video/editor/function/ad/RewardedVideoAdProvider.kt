//package com.qrcode.scanner.ad
//
//import android.content.Context
//import android.content.DialogInterface
//import android.graphics.Color
//import android.graphics.drawable.ColorDrawable
//import android.view.Gravity
//import android.view.View
//import android.view.WindowManager
//import android.widget.ImageView
//import android.widget.TextView
//import com.android.absbase.ui.dialog.BaseDialog
//import com.android.absbase.utils.NetworkUtils
//import com.android.absbase.utils.TimeUtils
//
//import com.rice.balls.ad.AdListener
//import com.rice.balls.ad.thirdparty.Ad
//import com.statistics.StatisticsUtils
//import com.allever.video.editor.R
//import com.allever.video.editor.function.billing.Billing
//import com.allever.video.editor.ui.dialog.RetainDialog2
//
//
//class RewardVideoOrVipDialog(context: Context) : BaseDialog(context), View.OnClickListener {
//
//    var clickListener: OnClickListener? = null
//    var sceneName: String = ""
//
//    override fun initDefaultView(context: Context) {
//        super.initDefaultView(context)
//        setContentView(R.layout.dialog_rewarded_video_or_vip)
//
//        val ivClose = findViewById(R.id.close) as? ImageView
//        val tvGetVip = findViewById(R.id.get_vip) as? TextView
//        val tvWatch = findViewById(R.id.watch) as? TextView
//
//        findViewById<View>(R.id.close)?.setOnClickListener(this)
//        findViewById<View>(R.id.get_vip)?.setOnClickListener(this)
//        findViewById<View>(R.id.watch)?.setOnClickListener(this)
//
//
//        if (Billing.switch) {
//            tvGetVip?.visibility = View.VISIBLE
//        } else {
//            tvGetVip?.visibility = View.GONE
//        }
//
//        setOnDismissListener(object : DialogInterface.OnDismissListener {
//            override fun onDismiss(dialog: DialogInterface?) {
//            }
//        })
//
//        setOnCancelListener(object : DialogInterface.OnCancelListener {
//            override fun onCancel(dialog: DialogInterface?) {
//                clickListener?.onClose()
//            }
//
//        })
//    }
//
//    override fun onClick(v: View?) {
//        when (v?.id) {
//            R.id.close -> {
//                //StatisticsUtils.statisics("rv_dialog", "action_$sceneName", "close")
//                clickListener?.onClose()
//                dismiss()
//            }
//            R.id.get_vip -> {
//                //StatisticsUtils.statisics("rv_dialog", "action_$sceneName", "close")
//                clickListener?.onGetVip()
//                dismiss()
//            }
//            R.id.watch -> {
//                //StatisticsUtils.statisics("rv_dialog", "action_$sceneName", "close")
//                clickListener?.onWatch()
//                dismiss()
//            }
//        }
//    }
//
//    override fun show() {
//        super.show()
//
//        val window = window
//        if (window != null) {
//            // 必须设置，否则无法全屏
//            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            //设置dialog在屏幕底部
//            window.setGravity(Gravity.CENTER)
//            //设置dialog弹出时的动画效果，从屏幕底部向上弹出
////                window.setWindowAnimations(R.style.DialogStyle)
//            //获得window窗口的属性
//            val lp = window.attributes
//            //设置窗口宽度为充满全屏
//            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
//            //设置窗口高度为包裹内容
//            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
//            //将设置好的属性set回去
//            window.attributes = lp
//        }
//    }
//
//    interface OnClickListener {
//        fun onGetVip()
//        fun onWatch()
//        fun onClose()
//    }
//
//    companion object {
//        fun showDialog(context: Context, sceneName: String, listener: OnClickListener) {
//            val rewardVideoOrVipDialog = RewardVideoOrVipDialog(context)
//            rewardVideoOrVipDialog.sceneName = sceneName
//            rewardVideoOrVipDialog.clickListener = listener
//            rewardVideoOrVipDialog.show()
//        }
//    }
//}
//
//class RewardedVideoAdProvider(val context: Context) {
////    private val rewardedVideoProxy = RewardedVideoProxy.get(context)
////
////    fun loadAndShowIfNeed(sceneName: String, unitId: String, adListener: AdListener?) {
////        rewardedVideoProxy.setLoadingDialog(null, TimeUtils.TimeConstant.ONE_SEC * 10)
////        val request = rewardedVideoProxy.getRequest(unitId, sceneName)
////        request.setSingleTimeoutMillis(TimeUtils.TimeConstant.ONE_SEC * 5)
////        rewardedVideoProxy.loadAdAndShowIfNeed(request, object : AdListener {
////            override fun onError(err: String) {
////                super.onError(err)
////
////
////                // 弹挽留页面
////                val retainDialog = RetainDialog2(context)
////                retainDialog.updateTitle(context.resources.getString(R.string.gp_retain_title_watch_noad), View.VISIBLE)
////                retainDialog.updateDescription(null, View.GONE)
////                retainDialog.updateFreeButton(null, View.GONE)
//////                retainDialog.updateQuitButton(context.resources.getString(R.string.gp_retain_i_know), View.VISIBLE)
////                retainDialog.show()
////                if (NetworkUtils.isNetworkAvailable()) {
////                    // 如果有网络又没拿到广告则解锁档次
////                    adListener?.onRewarded("", 0)
////                } else {
////                    adListener?.onError(err)
////                }
////            }
////
////            override fun onLoggingImpression(obj: Ad) {
////                super.onLoggingImpression(obj)
////                adListener?.onLoggingImpression(obj)
////            }
////
////            override fun onRewarded(type: String, amount: Int) {
////                super.onRewarded(type, amount)
////                adListener?.onRewarded(type, amount)
////            }
////        })
////    }
////
////    fun destroy() {
////        rewardedVideoProxy.destroyAd()
////    }
//
//}