package com.example.charge.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import com.example.charge.R
import com.example.charge.ad.AdIndex
import com.example.charge.databinding.DialogGameAwareBinding
import com.example.charge.event.DismissAdEvent
import com.example.charge.init.InitManager
import com.example.charge.utils.CountryUtil
import com.example.charge.utils.formThousand
import com.example.charge.utils.gone
import com.example.charge.utils.invisible
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.setVisible
import com.example.charge.utils.showXPopup
import com.example.charge.utils.visible
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdShowFailedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class GameAwareDialog(
    val mActivity: Activity,
    val goldNum : Int,
    val greenNum : Float,
    val clickNum  : Int,
    val adIndex: Int,
    val closeCallBack : (goldNum: Int, greenNum: Float) -> Unit,

) : BaseCenterPopupView(mActivity) {

    private val binding by lazy { DialogGameAwareBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_game_aware
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)

        binding.apply {
            goldNumTv.text = goldNum.toString()
            greenNumTv.text = " $ ${greenNum.formThousand()} "
            clickNumTv.text = clickNum.toString()

            clickNumLL.setVisible(clickNum >= 0)//通过点击数来判断是接金币的奖励还是打地鼠的奖励
            if (clickNum< 0){
                getDoubleTv.visible()
                getDoubleTv.text = context.getString(R.string.gain)
            }

            doubleAwareLL.setOnSingleListener {
//                AdManager.showRewardAd(activity, adIndex)
                closeCallBack.invoke(goldNum, greenNum)
                dismiss()
            }
            closeImg.setOnSingleListener {
                closeCallBack.invoke(goldNum, greenNum)
                dismiss()
            }
            naviteView.initView(mActivity)
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onDismissAdEvent(event: DismissAdEvent) {
        if (event.adIndex == adIndex){
            binding.apply {
                clickNumLL.gone()
                getDoubleTv.visible()
                doubleAwareLL.invisible()
                getAwareTv.visible()
                closeImg.gone()
                val doubleNum = ( 2..5).random()
                val doubleGold = goldNum*doubleNum
                val doubleGreen = greenNum*doubleNum
                getDoubleTv.text = context.getString(R.string.get_double_num,doubleNum)
                goldNumTv.text = "$doubleGold"
                greenNumTv.text =  " $ ${doubleGreen.formThousand()}"
                getAwareTv.setOnSingleListener {
                    closeCallBack.invoke(doubleGold, doubleGreen)
                    dismiss()
                }
            }
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