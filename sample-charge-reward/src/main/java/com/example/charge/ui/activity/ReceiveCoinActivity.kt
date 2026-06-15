package com.example.charge.ui.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import app.allever.android.lib.core.app.App
import com.example.charge.R
import com.example.charge.ad.AdIndex
import com.example.charge.ad.InterAdUtil
import com.example.charge.base.BaseActivity
import com.example.charge.constant.Coin
import com.example.charge.constant.LogTag
import com.example.charge.currency.CurrencyFlyAnimatorUtil
import com.example.charge.currency.CurrencyType
import com.example.charge.databinding.ActivityReceiveCoinBinding
import com.example.charge.event.ClosePageEvent
import com.example.charge.event.InterAdCDTimeEvent
import com.example.charge.init.InitManager
import com.example.charge.task.TaskType
import com.example.charge.ui.dialog.GameAwareDialog
import com.example.charge.ui.dialog.GameSeeAdDialog
import com.example.charge.ui.dialog.InfiniteGameDialog
import com.example.charge.ui.view.ReceiveCoinView
import com.example.charge.utils.CountryUtil
import com.example.charge.utils.ExtraKey
import com.example.charge.utils.LogUtil
import com.example.charge.utils.MusicUtil
import com.example.charge.utils.SoundUtil
import com.example.charge.utils.formThousand
import com.example.charge.utils.gone
import com.example.charge.utils.log
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.visible
import com.example.charge.vm.VMHelper
import com.plinkopro.wincash.utils.PopupHelper
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ReceiveCoinActivity : BaseActivity<ActivityReceiveCoinBinding>(), ReceiveCoinView.Listener {
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityReceiveCoinBinding {
        return ActivityReceiveCoinBinding.inflate(inflater, container, false)
    }

    var goldNum = 0f
    var greenNum = 0f

    val viewModel by lazy {
        VMHelper.receiveCoinViewModel
    }

    var isShowAwareDialog = false

    var showInterAd = false //是否展示插屏
    var inPlayGame = false

    override fun initView() {
        fixStatusBar(binding.currencyView)
        initObserver()
        binding.apply {
            startGameFl.setOnSingleListener {
                receiveCoinView.startGame(this@ReceiveCoinActivity)
            }
            backImg.setOnSingleListener {
                finish()
            }
            taskImg.setOnSingleListener {
                goTo<TaskActivity>(this@ReceiveCoinActivity) {
                    putExtra(ExtraKey.TASK_TYPE, TaskType.RECEIVE_COIN)
                }
            }
        }
    }

    private fun initObserver() {
        VMHelper.taskViewModel.showTaskDot.observe(this) {
            binding.tipsImg.isVisible = it
        }
    }

    fun updateAwareNum() {
        binding.apply {
            goldNumTv.text = " ${goldNum.toInt()} "
            greenNumTv.text = " ${greenNum.formThousand()} "
        }
    }

    override fun onResume() {
        super.onResume()
        MusicUtil.play(MusicUtil.receiveCoinPlayer)
        binding.receiveCoinView.resumeGame()
    }

    override fun onPause() {
        super.onPause()
        binding.receiveCoinView.pauseGame()
        MusicUtil.pause(MusicUtil.receiveCoinPlayer)
    }


    override fun onCoinCaught(coinView: View, coin: Coin) {
        VMHelper.taskViewModel.receiveCoinCount.value = VMHelper.taskViewModel.receiveCoinCount.value?.plus(1)
        if (coin.num >= 10) {
            goldNum += coin.num
        } else {
            greenNum += coin.num
        }
        updateAwareNum()
        SoundUtil.play(R.raw.receive_coin)
    }

    override fun onGameStarted(totalSeconds: Int) {
        binding.apply {
            startGameFl.gone()
            awareLL.visible()
            timeFl.visible()
            inPlayGame = true
        }
    }

    override fun onGamePaused() {

    }

    override fun onGameResumed() {

    }

    override fun onGameStopped() {
        showAwareDialog {
            binding.apply {
                goldNum = 0f
                greenNum = 0f
                startGameFl.visible()
                awareLL.gone()
                timeFl.gone()
                inPlayGame = false
            }
        }

        if (InterAdUtil.isMatchLogic(2) && InterAdUtil.isProbabilityHit()) {
            AdManager.showInterAd(this, AdIndex.GAME_AWARE_INTER)
        }
    }

    override fun onTick(remainingSeconds: Int) {
        binding.timeTv.text = " ${remainingSeconds}s "
    }

    override fun onSeeAd(coinView: View, coin: Coin) {
        binding.receiveCoinView.pauseGame()
        PopupHelper.createDialog(
            this, GameSeeAdDialog(
                this, coin.num,
                AdIndex.COIN_GAME_SEE_AD_INDEX,
                {
                    binding.receiveCoinView.resumeGame()
                    it.dismiss()
                }, {
                    binding.apply {
                        greenNumTv.text =
                            " $ ${coin.num}"
                        toastLL.visible()
                        toastLL.postDelayed({ toastLL.gone() }, 1500)
                    }
                    binding.receiveCoinView.resumeGame()
                }
            )
        ).show()
    }

    fun showAwareDialog(closeCallBack: () -> Unit) {
        if (!isShowAwareDialog) {
            isShowAwareDialog = true

            if (InterAdUtil.isMatchLogic(2) && showInterAd){
                if (App.DEBUG) {
                    LogUtil.showInterAd("游戏结束，是第二套,游戏中cd到了,弹广告")
                }
                AdManager.showInterAd(this@ReceiveCoinActivity, AdIndex.GAME_AWARE_INTER)
                showInterAd = false
            }else if (InterAdUtil.isMatchLogic(2) && !showInterAd && InterAdUtil.isProbabilityHit()) {
                if (App.DEBUG) {
                    LogUtil.showInterAd("游戏结束，是第二套,并且在游戏过程中播放cd没到，但命中概率，弹广告")
                }
                AdManager.showInterAd(this@ReceiveCoinActivity, AdIndex.GAME_AWARE_INTER)
            }else if (InterAdUtil.isMatchLogic(1) && showInterAd){
                if (App.DEBUG) {
                    LogUtil.showInterAd("游戏结束，是第一套,游戏中cd到了,弹广告")
                }
                AdManager.showInterAd(this@ReceiveCoinActivity, AdIndex.GAME_AWARE_INTER)
                showInterAd = false
            }

            PopupHelper.createDialog(
                this,
                GameAwareDialog(
                    this,
                    goldNum.toInt(),
                    greenNum,
                    -1,
                    AdIndex.COIN_GAME_AWARE_INDEX,
                ) { goldNum, greenNum ->
                    isShowAwareDialog = true
                    if (viewModel.isInfiniteTime.value == false) {
                        viewModel.gameCount.value = viewModel.gameCount.value?.minus(1)
                    }
                    VMHelper.taskViewModel.receiveCoinGameCount.value =
                        VMHelper.taskViewModel.receiveCoinGameCount.value?.plus(1)
                    CurrencyFlyAnimatorUtil.start(
                        this,
                        binding.currencyView,
                        binding.root,
                        CurrencyType.GOLD,
                        goldNum.toFloat()
                    )
                    CurrencyFlyAnimatorUtil.start(
                        this,
                        binding.currencyView,
                        binding.root,
                        CurrencyType.GREEN,
                        greenNum
                    ) {
                        isShowAwareDialog = false
                        closeCallBack.invoke()
                        if (viewModel.gameCount.value == 0 && viewModel.isInfiniteTime.value == false) {
                            showInfiniteTimeDialog()
                        }
                    }

                }
            ).show()
        }
    }

    fun showInfiniteTimeDialog() {
        PopupHelper.createDialog(
            this,
            InfiniteGameDialog(
                this,
                AdIndex.COIN_GAME_INFINITE_INDEX,
                {
                    finish()
                }, {
                    viewModel.residueInfiniteTime.value = viewModel.onceInfiniteTime
                }
            )
        ).show()
    }

    override fun enableEventBus(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveInterAdCDTimeEvent(event: InterAdCDTimeEvent) {
        if (!isShow) return

        if (App.DEBUG) {
            LogUtil.showInterAd("接金币界面收到插屏广告倒计时结束事件")
        }
        if (InterAdUtil.isMatchLogic(1)) {
            if (inPlayGame) {
                if (App.DEBUG) {
                    LogUtil.showInterAd("是第一套,但在游戏中，先不弹广告，等游戏结束弹")
                }
                showInterAd = true
                return
            }else{
                if (App.DEBUG) {
                    LogUtil.showInterAd("是第一套,不在游戏中，弹广告")
                }
                if (!AdManager.showInterAd(this, AdIndex.GAME_AWARE_INTER)) {
                    if (App.DEBUG) {
                        LogUtil.showInterAd("插屏广告加载失败")
                    }
                }
            }
        }else{
            if (inPlayGame){
                if (App.DEBUG) {
                    LogUtil.showInterAd("是第二套,但在游戏中，先不弹广告，等游戏结束弹")
                }
                showInterAd = true
            }else{
                if (App.DEBUG) {
                    LogUtil.showInterAd("是第二套,不在游戏中 弹广告")
                }
                if (!AdManager.showInterAd(this, AdIndex.GAME_AWARE_INTER)) {
                    if (App.DEBUG) {
                        LogUtil.showInterAd("插屏广告加载失败")
                    }
                }
            }
        }
    }
}