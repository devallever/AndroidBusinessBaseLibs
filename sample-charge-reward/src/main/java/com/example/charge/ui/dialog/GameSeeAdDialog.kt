package com.example.charge.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.example.charge.R
import com.example.charge.ad.AdIndex
import com.example.charge.currency.CurrencyType
import com.example.charge.currency.CurrencyUtils
import com.example.charge.databinding.DialogGameAwareBinding
import com.example.charge.databinding.DialogGameInfiniteBinding
import com.example.charge.databinding.DialogGameSeeAdBinding
import com.example.charge.event.AdShowFailedEvent
import com.example.charge.event.DismissAdEvent
import com.example.charge.init.InitManager
import com.example.charge.utils.CountryUtil
import com.example.charge.utils.formThousand
import com.example.charge.utils.gone
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.showXPopup
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class GameSeeAdDialog(
    val mActivity: Activity,
    val aware : Float,
    val adIndex: Int,
    val closeCallBack: (dialog : GameSeeAdDialog) -> Unit,
    val seeAdCallBack: () -> Unit
    ) : BaseCenterPopupView(mActivity) {

    private val binding by lazy { DialogGameSeeAdBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_game_see_ad
    }

    val isSeeAdCallBack = true //防止多次回调

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)

        binding.apply {
            awareTv.text = "$${aware}"
            seeAdLL.setOnSingleListener {
                closeCallBack.invoke(this@GameSeeAdDialog)
//                AdManager.showRewardAd(activity, adIndex)
            }
            closeImg.setOnSingleListener {
                closeCallBack.invoke(this@GameSeeAdDialog)
            }
            naviteView.initView(mActivity)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDismissAdEvent(event: DismissAdEvent) {
        if (event.adIndex == adIndex && isSeeAdCallBack) {
            seeAdCallBack.invoke()
            dismiss()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdShowFailedEvent(event: AdShowFailedEvent) {
        when (event.adIndex) {
            adIndex -> {
                context.showXPopup(AdFailDialog(context){

                })
            }
        }
    }
}