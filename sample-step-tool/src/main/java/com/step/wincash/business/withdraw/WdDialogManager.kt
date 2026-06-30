package com.step.wincash.business.withdraw

import android.app.Activity
import com.step.wincash.business.withdraw.account.AccountBean
import com.step.wincash.business.withdraw.account.AccountManager
import com.step.wincash.init.InitManager
import com.step.wincash.ui.dialog.BDAccountDialog
import com.step.wincash.ui.dialog.BRAccountDialog
import com.step.wincash.ui.dialog.BaseAccountInputDialog
import com.step.wincash.ui.dialog.CaptchaDialog
import com.step.wincash.ui.dialog.InputConfirmDialog
import com.step.wincash.ui.dialog.KOAccountDialog
import com.step.wincash.ui.dialog.MostAccountDialog
import com.step.wincash.ui.dialog.PhoneFeeAccountDialog
import com.step.wincash.ui.dialog.SelectPaymentDialog
import com.step.wincash.ui.dialog.WdErrorDialog
import com.step.wincash.utils.PopupHelper
import com.step.wincash.utils.SpKey
import com.step.wincash.utils.SpUtil
import com.step.wincash.utils.TimeUtil

class WdDialogManager(
    val apply: (account: AccountBean, paymentParams: PaymentParams) -> Unit = { account, paymentParams -> }
) {
    private var hasShownCaptchaDialog: Boolean = false
        get() {
            if (!field) {
                field = SpUtil.Companion.get(SpKey.HAS_SHOWN_CAPTCHA_DIALOG, false)
            }
            return field
        }
        set(value) {
            if (field != value) {
                field = value
                SpUtil.Companion.put(SpKey.HAS_SHOWN_CAPTCHA_DIALOG, value)
            }
        }

    // 展示弹窗
    fun show(activity: Activity,amount : Float, paymentParams: PaymentParams) {
        val paymentName = WdUtil.getLastSelectPaymentName()
        selectPaymentDialog(activity,amount, paymentParams)
        return
        if (paymentName.isNotEmpty()) {
            val paymentParams =
                WalletManager.findPaymentParams(paymentName, InitManager.getCountryCode())
            val accountsBean = AccountManager.findAccountsBean(paymentName)
            if (accountsBean == null) {
                //展示选择弹窗
                selectPaymentDialog(activity, amount, paymentParams)
            } else if (!hasShownCaptchaDialog) {
                showCaptchaDialog(
                    activity = activity,
                    onSuccess = {
                        showConfirmDialog(activity, paymentParams, accountsBean,amount)
                        hasShownCaptchaDialog = true
                    })
            } else {
                paymentName?.apply {
                    //展示确认弹窗
                    showConfirmDialog(activity, paymentParams, accountsBean,amount)
                }
            }
        } else {
            selectPaymentDialog(activity,amount, paymentParams)
        }
    }

    private fun selectPaymentDialog(activity: Activity,amount: Float, paymentParams: PaymentParams) {
//        PopupHelper.createDialog(activity, SelectPaymentDialog(activity) {
//            showAccountDialog(activity, it, amount)
//        }).show()
        showAccountDialog(activity, paymentParams, amount)
    }

    private fun showAccountDialog(activity: Activity, paymentParams: PaymentParams?,amount: Float) {
        createAccountDialog(
            activity = activity,
            paymentParams = paymentParams,
            pre = {
                paymentParams?.apply {
                    selectPaymentDialog(activity,amount, paymentParams)
                }
            },
            next = {
                paymentParams?.apply {
                    WdUtil.saveLastSelectPaymentName(paymentName)
                }
                if (!hasShownCaptchaDialog) {
                    showCaptchaDialog(
                        activity = activity,
                        onSuccess = {
                            showConfirmDialog(activity, paymentParams, it,amount)
                            hasShownCaptchaDialog = true
                        },
                    )

                } else {
                    showConfirmDialog(activity, paymentParams, it,amount)
                }
            }
        )?.let {
            PopupHelper.createDialog(activity, it).show()
        }
    }

    private fun createAccountDialog(
        activity: Activity,
        paymentParams: PaymentParams?,
        pre: () -> Unit,
        next: (account: AccountBean) -> Unit
    ): BaseAccountInputDialog? {
        paymentParams?.apply {
            return when (paymentName) {
                PaymentName.Companion.PIX, PaymentName.Companion.PAGBANK -> {
                    BRAccountDialog(activity, paymentParams, pre, next)
                }
                PaymentName.Companion.BankCard, PaymentName.Companion.Clipspay -> {
                    KOAccountDialog(activity, paymentParams, pre, next)
                }
                PaymentName.Companion.BKASH, PaymentName.Companion.EASYPAISA -> {
                    BDAccountDialog(activity, paymentParams, pre, next)
                }
                PaymentName.Companion.PhoneFee -> {
                    PhoneFeeAccountDialog(activity, paymentParams, pre, next)
                }
                else -> {
                    MostAccountDialog(activity, paymentParams, pre, next)
                }
            }
        }
        return null
    }

    private fun showCaptchaDialog(
        activity: Activity,
        onSuccess: () -> Unit = {},
    ) {
        PopupHelper.createDialog(
            context = activity,
            popupView = CaptchaDialog(
                context = activity,
                onError = {
                    showCaptchaDialog(activity, onSuccess)
                },
                onSuccess = onSuccess
            )
        ).show()
    }

    private fun showConfirmDialog(
        activity: Activity,
        paymentParams: PaymentParams?,
        account: AccountBean,
        amount: Float
    ) {
        paymentParams?.let {
            PopupHelper.createDialog(
                activity,
                InputConfirmDialog(activity, paymentParams, account, amount, {
                    showAccountDialog(activity, it, amount)
                }) {
                    apply.invoke(account, paymentParams)
                },
            ).show()
        }
    }

    fun showWdErrorDialog(
        activity: Activity,
        paymentParams: PaymentParams?,
        account: AccountBean?,
        errorCode: String,
        amount: Float
    ) {
        //要显示错误弹窗，需要绑定过账户发起过提现了
        if (paymentParams == null || account == null) return

        PopupHelper.createDialog(
            activity,
            WdErrorDialog(
                activity,
                paymentParams,
                account,
                amount,
                WdUtil.matchWdMsgByFailCode(activity, errorCode),
                TimeUtil.getTimeStrByTimeMillis(System.currentTimeMillis())
            ) {
                showAccountDialog(activity, it, amount)
            },
        ).show()
    }

}