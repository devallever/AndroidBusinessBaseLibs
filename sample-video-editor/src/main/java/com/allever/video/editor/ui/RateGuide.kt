//package com.videoeditor.ui
//
//import android.content.Context
//import android.net.ConnectivityManager
//
//import com.android.absbase.App
//import com.android.absbase.utils.TimeUtils
//
//import com.allever.video.editor.ConfigManager
//
//import java.util.ArrayList
//
//
//class RateGuide : com.rice.balls.utils.RateGuide() {
//
//
//    class Builder {
//        internal var packageName = App.getPackageName()
//        internal var forceShow = false
//        internal var listener: DialogClickListener? = null
//        internal var actions = ArrayList<Int>()
//
//        fun setPackageName(packageName: String): Builder {
//            this.packageName = packageName
//            return this
//        }
//
//        fun setDialogClickListener(listener: DialogClickListener?): Builder {
//            this.listener = listener
//            return this
//        }
//
//        fun addAction(action: Int): Builder {
//            actions.add(action)
//            return this
//        }
//
//        fun setForceShow(forceShow: Boolean): Builder {
//            this.forceShow = forceShow
//            return this
//        }
//
//        fun checkShow(context: Context): Boolean {
//            val type = checkType(context, this)
//            return type != TYPE_NO_RATE
//        }
//
//        fun show(context: Context): Boolean {
//            val type = checkType(context, this)
//            return showRate(context, type, this)
//        }
//    }
//
//    companion object {
//
//        private val TYPE_NO_RATE = 0
//        private val TYPE_MAIN_RATE = 1
//
//        private fun isNetWorkAvailable(context: Context?): Boolean {
//            var result = false
//            if (context != null) {
//                val connectivityManager = context
//                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                if (connectivityManager != null) {
//                    val networkInfo = connectivityManager
//                            .activeNetworkInfo
//                    if (networkInfo != null && networkInfo.isConnected) {
//                        result = true
//                    }
//                }
//            }
//            return result
//        }
//
//        fun isNeedShowRateDialog(context: Context, builder: Builder): Boolean {
//            if (builder.forceShow) {
//                return true
//            }
//            /**
//             * 2.判断用户是否进行过评价（即是否点击过去评价的按钮）
//             * 已进行过评价，则无需展示评分引导
//             * 未进行过评价，则当该用户满足下述条件时，弹出评分引导（下述为与的关系）
//             * 容器内，当用户点击save键后
//             * 每天打开应用次数>=2次
//             * 用户第2天再次进入应用，不论是否满足操作次数，均弹出评分引导
//             */
//            val isNeed: Boolean
//            val lastShowTime = lastShowTime
//            val isClickYes = isClickYes(context.packageName)
//            // 给过差评
////            val giveABadReview = giveABadReview()
//            val openAppTimes = ConfigManager.openAppCountInDay
//            val firstOpenAppTime = ConfigManager.firstOpenAppTime
//            val currentTimeMillis = System.currentTimeMillis()
//            isNeed = (isNetWorkAvailable(App.getContext())
//                    && !isClickYes
//                    && currentTimeMillis - lastShowTime > TimeUtils.TimeConstant.ONE_DAY
//                    && (openAppTimes >= 2 && builder.actions.size > 0
//                    || !TimeUtils.isSameDayOfMillis(firstOpenAppTime, currentTimeMillis))
//                    )
//
//            return isNeed
//        }
//
//        private fun checkType(context: Context, builder: Builder): Int {
//            val config = rateStrategyConfig
//            if (config == null || config.isAllowReal) {
//                if (isNeedShowRateDialog(context, builder)) {
//                    return TYPE_MAIN_RATE
//                }
//            }
//            return TYPE_NO_RATE
//        }
//
//        private fun showRate(context: Context, type: Int, builder: Builder): Boolean {
//            when (type) {
//                TYPE_MAIN_RATE -> {
//                    showRateDialog(context, App.getPackageName(), builder.listener)
//                    saveExitRateDialogTime()
//                    return true
//                }
//            }
//            return false
//        }
//
//        @Deprecated("")
//        @JvmOverloads
//        fun showRate(context: Context, forceShow: Boolean = false, listener: DialogClickListener? = null): Boolean {
//            return Builder().setForceShow(forceShow).setDialogClickListener(listener).show(context)
//        }
//
//        @Deprecated("")
//        fun showRate(context: Context, listener: DialogClickListener): Boolean {
//            return showRate(context, false, listener)
//        }
//    }
//}
