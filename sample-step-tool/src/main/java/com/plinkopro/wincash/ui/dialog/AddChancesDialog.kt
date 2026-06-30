package com.plinkopro.wincash.ui.dialog

import android.content.Context
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseApplication
import com.plinkopro.wincash.base.BaseCenterPopupView
import com.plinkopro.wincash.databinding.DialogAddChancesBinding
import com.plinkopro.wincash.event.AdDismissEvent
import com.plinkopro.wincash.init.AdIndex
import com.plinkopro.wincash.utils.ToastUtil
import com.plinkopro.wincash.utils.isNetworkAvailable
import com.plinkopro.wincash.utils.setOnSingleListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AddChancesDialog(
    context: Context,
    val awareAdIndex: Int,
    val addChancesCallback: () -> Unit,
    val notNowCallBack: () -> Unit
) : BaseCenterPopupView(context) {
    private val binding by lazy { DialogAddChancesBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_add_chances
    }

    override fun onCreate() {
        super.onCreate()

        EventBus.getDefault().register(this)
        binding.apply {

            imageView5.setImageResource(
                if (awareAdIndex == AdIndex.LUCKY_WHEEL_ADD_CHANCES_INDEX)
                    R.drawable.ic_get_lucky_wheel_times else R.drawable.ic_get_lotto_times
            )
            getTv.setOnSingleListener {
                BaseApplication.postAdDismissEvent(awareAdIndex)
            }
            notNowTv.setOnSingleListener {
                notNowCallBack.invoke()
                dismiss()
            }
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
        if (event.adIndex == awareAdIndex) {
            addChancesCallback.invoke()
            dismiss()
        }
    }

}