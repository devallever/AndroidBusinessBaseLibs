package app.allever.android.ai.qr.scanner.core

import android.content.Context
import android.net.ConnectivityManager

import com.android.absbase.App

class RateGuide {
    class Builder {
        internal var packageName = App.getPackageName()
        internal var forceShow = false
        internal var hasSpecificAction = false

        fun setDialogClickListener(): Builder {
            return this
        }

        fun setForceShow(forceShow: Boolean): Builder {
            this.forceShow = forceShow
            return this
        }

        fun setSpecificAction(hasSpecificAction: Boolean): Builder {
            this.hasSpecificAction = hasSpecificAction
            return this
        }

        fun show(context: Context): Boolean {
            val type = checkType(context, this)
            return showRate(context, type, this)
        }

        fun needShowRate(context: Context): Boolean {
            val type = checkType(context, this)
            return type == TYPE_MAIN_RATE
        }
    }

    companion object {


        private val TYPE_NO_RATE = 0
        private val TYPE_MAIN_RATE = 1

        private fun isNetWorkAvailable(context: Context?): Boolean {
            var result = false
            if (context != null) {
                val connectivityManager = context
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (connectivityManager != null) {
                    val networkInfo = connectivityManager
                            .activeNetworkInfo
                    if (networkInfo != null && networkInfo.isConnected) {
                        result = true
                    }
                }
            }
            return result
        }

        private fun isNeedShowRateDialog(context: Context, forceShow: Boolean = false, hasSpecificAction: Boolean = false): Boolean {
            return false
        }

        fun showRate(context: Context, forceShow: Boolean = false): Boolean {
            return Builder().setForceShow(forceShow).setDialogClickListener().show(context)
        }

        private fun checkType(context: Context, builder: Builder): Int {
            return TYPE_NO_RATE
        }

        private fun showRate(context: Context, type: Int, builder: Builder): Boolean {
            when (type) {
                TYPE_MAIN_RATE -> {
                    return true
                }
            }
            return false
        }

        fun showRate(context: Context): Boolean {
            return showRate(context, false)
        }
    }


}
