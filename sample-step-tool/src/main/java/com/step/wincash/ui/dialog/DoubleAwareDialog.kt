package com.step.wincash.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import com.step.wincash.R
import com.step.wincash.base.BaseApplication
import com.step.wincash.base.BaseCenterPopupView
import com.step.wincash.databinding.DialogDoubleAwardBinding
import com.step.wincash.event.AdDismissEvent
import com.step.wincash.utils.InterAdUtil
import com.step.wincash.utils.ToastUtil
import com.step.wincash.utils.formThousand
import com.step.wincash.utils.isNetworkAvailable
import com.step.wincash.utils.setOnSingleListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("ViewConstructor")
class DoubleAwareDialog(
    val mActivity: Activity,
    val award: Int,
    val awardAdIndex: Int,
    val interAdIndex: Int,
    val activityType: String, //活动类型 luckywheel/scratchcard
    val multiple: Int = 5,
    val onAwardCallback: ((award: Int) -> Unit)? = null
) : BaseCenterPopupView(mActivity) {

    private val binding by lazy { DialogDoubleAwardBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_double_award
    }

    override fun onCreate() {
        super.onCreate()

        EventBus.getDefault().register(this)
        binding.apply {

            awareTv.text = "+${award.formThousand()}"
            doubleAwareTv.text = "+${(award * multiple).formThousand()}"

            seeAdTv.setOnSingleListener {
                BaseApplication.postAdDismissEvent(awardAdIndex)
            }

            getTv.setOnSingleListener {
                if (InterAdUtil.showAd()) {
                    BaseApplication.postAdDismissEvent(interAdIndex)
                } else {
                    dismissDialog(award)
                }
            }

            naviteView.initView(mActivity)
        }

        if (!isNetworkAvailable()) {
            ToastUtil.showToast(R.string.no_network_tips)
        }
    }

    override fun onDismiss() {
        super.onDismiss()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdDismissEvent(event: AdDismissEvent) {
        if (event.adIndex == awardAdIndex) {
            val money = award * multiple
            dismissDialog(money)
        } else if (event.adIndex == interAdIndex) {
            dismissDialog(award)
        }
    }

    fun dismissDialog(money: Int) {
        dismissWith {
            onAwardCallback?.invoke(money)
        }
    }

}