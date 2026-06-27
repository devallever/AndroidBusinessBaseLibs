package com.example.charge.ui.activity

import android.annotation.SuppressLint
import android.app.ActionBar
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import app.allever.android.lib.core.app.App
import com.example.charge.ChargeApp
import com.example.charge.R
import com.example.charge.ad.AdIndex
import com.example.charge.ad.InterAdUtil
import com.example.charge.base.BaseActivity
import com.example.charge.base.BaseBindingAdapter
import com.example.charge.constant.Burrow
import com.example.charge.constant.BurrowType
import com.example.charge.constant.getDefaultBurrowList
import com.example.charge.currency.CurrencyFlyAnimatorUtil
import com.example.charge.currency.CurrencyType
import com.example.charge.databinding.ActivityHitMoleBinding
import com.example.charge.databinding.ItemBurrowBinding
import com.example.charge.event.AnimEvent
import com.example.charge.event.GameSeeAdEvent
import com.example.charge.event.InterAdCDTimeEvent
import com.example.charge.task.TaskType
import com.example.charge.ui.dialog.GameAwareDialog
import com.example.charge.ui.dialog.GameSeeAdDialog
import com.example.charge.ui.dialog.InfiniteGameDialog
import com.example.charge.utils.ExtraKey
import com.example.charge.utils.LogUtil
import com.example.charge.utils.MusicUtil
import com.example.charge.utils.SoundUtil
import com.example.charge.utils.gone
import com.example.charge.utils.invisible
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.setVisible
import com.example.charge.utils.visible
import com.example.charge.vm.VMHelper
import com.plinkopro.wincash.utils.PopupHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HitMoleActivity : BaseActivity<ActivityHitMoleBinding>() {
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityHitMoleBinding {
        return ActivityHitMoleBinding.inflate(layoutInflater)
    }

    val viewModel by lazy { VMHelper.hitMoleViewModel }

    val taskModel by lazy { VMHelper.taskViewModel }

    val onceGameTime = 30000L

    var burrowList = getDefaultBurrowList() //地洞对象
    var countDown = onceGameTime //倒计时
    var count = 0
    var bombNum = 0
    var clickNum = 0
    var awareGold = 0
    var awareGreen = 0f
    var inPlayGame = false
    var needSeeAdNum = 0
    var bombBociIndex = mutableListOf<Int>()
    var needSeeAdBociIndex = mutableListOf<Int>()
    var countDownTimer: CountDownTimer = getCountDownTimer(countDown)

    var effectEndX = 0f
    var effectEndY = 0f

    var isShowSeeAdDialog = false  //看广告弹窗是否在展示
    var isShowAwareDialog = false //展示奖励弹窗是否在展示

    var showInterAd = false //是否展示插屏

    override fun initView() {
        fixStatusBar(binding.currencyView)
        registerEventbus()
        initObserver()
        binding.apply {
            startGameFl.setOnSingleListener {
                gameStart()
            }
            gameRv.apply {
                adapter = hitMoleAdapter.apply {
                    setNewData(burrowList)
                }
            }
            backImg.setOnSingleListener {
                finish()
            }
            taskImg.setOnSingleListener {
                goTo<TaskActivity>(this@HitMoleActivity) {
                    putExtra(ExtraKey.TASK_TYPE, TaskType.HIT_MOLE)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeGame()
        MusicUtil.play(MusicUtil.hitMolePlayer)
    }

    override fun onPause() {
        super.onPause()
        pauseGame()
        MusicUtil.pause(MusicUtil.hitMolePlayer)
    }

    private fun initObserver() {
        taskModel.showTaskDot.observe(this@HitMoleActivity) {
            binding.tipsImg.setVisible(it)
        }
    }

    val hitMoleAdapter = object : BaseBindingAdapter<Burrow, ItemBurrowBinding>() {
        override fun createViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemBurrowBinding {
            return ItemBurrowBinding.inflate(inflater, parent, false)
        }

        override fun convert(
            helper: BaseBindViewHolder<ItemBurrowBinding>,
            item: Burrow
        ) {
            helper.binding.root.post {
                helper.binding.burrowView.apply {
                    if (item.type != BurrowType.BURROW) {
                        showAnimView(
                            item,
                            { burrow, clickX, clickY ->
                                if (burrow.num <= 0f) {
                                    //播放扣除次数的动画
                                    binding.numView.startAnimation {
                                        updateClickNum(if (clickNum <= 2) 0 else clickNum - 2)
                                    }
                                    SoundUtil.play(R.raw.bomb_blasts) //播放炸弹爆炸音效
                                    if (App.DEBUG) LogUtil.hitMole("炸弹被打爆炸了~")
                                } else if (burrow.needSeeAd){
                                    addAndAnimateImage(binding.root, clickX, clickY) {
                                        updateClickNum(clickNum + 1)
                                    }
                                } else{
                                    if (burrow.num >= 1) {
                                        awareGold += burrow.num.toInt()
                                    } else {
                                        awareGreen += burrow.num
                                    }
                                    addAndAnimateImage(binding.root, clickX, clickY) {
                                        updateClickNum(clickNum + 1)
                                    }
                                    taskModel.hitMoleCount.value =
                                        taskModel.hitMoleCount.value?.plus(1)
                                    SoundUtil.play(R.raw.mole_hit) //播放地鼠被打音效
                                    if (App.DEBUG) LogUtil.hitMole("地鼠被打咯~")
                                }
                            }, {
                                item.type = BurrowType.BURROW
                            }
                        )
                    }
                }
            }
        }
    }

    fun updateClickNum(num: Int) {
        clickNum = num
        binding.clickNumTv.text = "$num"
    }

    fun gameStart() {
        bombNum = (2..5).random()  //本局的炸弹数
        repeat(bombNum) {  //随机生成炸弹位置
            randomShowBombBoci()
        }
        needSeeAdNum = (1..2).random()
        repeat(needSeeAdNum) {
            randomShowSeeAdBoci()
        }
        binding.apply {
            countDownFl.visible()
            clickNumTv.visible()
            clickNumFl.visible()
            startGameFl.gone()
        }
        inPlayGame = true
        getCountDownTimer(countDown).start()
    }

    fun gameOver() {
        count = 0
        countDown = onceGameTime
        bombNum = 0
        needSeeAdNum = 0
        awareGreen = 0f
        awareGold = 0
        inPlayGame = false
        updateClickNum(0)
        bombBociIndex = mutableListOf<Int>()
        needSeeAdBociIndex = mutableListOf<Int>()
        burrowList = getDefaultBurrowList()

        binding.apply {
            countDownFl.gone()
            clickNumTv.gone()
            clickNumFl.invisible()
            startGameFl.visible()
        }
        hitMoleAdapter.setNewData(burrowList)
    }

    fun showAwareDialog() {
        if (!isShowAwareDialog) {
            isShowAwareDialog = true
            if (InterAdUtil.isMatchLogic(2) && showInterAd){
                if (App.DEBUG) {
                    LogUtil.showInterAd("游戏结束，是第二套,游戏中cd到了,弹广告")
                }
                ChargeApp.postAdDismissEvent(AdIndex.GAME_AWARE_INTER)
                showInterAd = false
            }else if (InterAdUtil.isMatchLogic(2) && !showInterAd && InterAdUtil.isProbabilityHit()) {
                if (App.DEBUG) {
                    LogUtil.showInterAd("游戏结束，是第二套,并且在游戏过程中播放cd没到，但命中概率，弹广告")
                }
                ChargeApp.postAdDismissEvent(AdIndex.GAME_AWARE_INTER)
            }else if (InterAdUtil.isMatchLogic(1) && showInterAd){
                if (App.DEBUG) {
                    LogUtil.showInterAd("游戏结束，是第一套,游戏中cd到了,弹广告")
                }
                ChargeApp.postAdDismissEvent(AdIndex.GAME_AWARE_INTER)
                showInterAd = false
            }

            PopupHelper.createDialog(
                this,
                GameAwareDialog(
                    this,
                    awareGold,
                    awareGreen,
                    clickNum,
                    AdIndex.MOLE_GAME_AWARE_INDEX,
                ) { goldNum, greenNum ->
                    if (viewModel.isInfiniteTime.value == false) {
                        viewModel.gameCount.value = viewModel.gameCount.value?.minus(1)
                    }
                    taskModel.hitMoleGameCount.value = taskModel.hitMoleGameCount.value?.plus(1)
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
                        gameOver()
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
                AdIndex.MOLE_GAME_INFINITE_INDEX,
                {
                    finish()
                }, {
                    viewModel.residueInfiniteTime.value = viewModel.onceInfiniteTime
                }
            )
        ).show()
    }

    fun randomShowBombBoci() {
        val boci = (1..58).random()
        if (boci !in bombBociIndex)
            bombBociIndex.add(boci)
        else
            randomShowBombBoci()
    }

    fun randomShowSeeAdBoci() {
        val boci = (1..58).random()
        if (boci !in needSeeAdBociIndex)
            needSeeAdBociIndex.add(boci)
        else
            randomShowSeeAdBoci()
    }

    fun getCountDownTimer(millisInFuture: Long = countDown): CountDownTimer {
        countDownTimer = object : CountDownTimer(millisInFuture, 500) { // 30秒，每0.5秒回调一次
            override fun onTick(millisUntilFinished: Long) {
                count += 1
                countDown -= 500

                if (App.DEBUG) LogUtil.hitMole("第${count}波:")

                binding.countDownTv.text = if (countDown <= 0L) "0" else "${countDown / 1000L}"
                //获取展示地洞的索引列表
                val burrowIndices = burrowList.withIndex()
                    .filter { (index, value) -> value.type == BurrowType.BURROW }
                    .map { it.index }.toMutableList()
                if (App.DEBUG) LogUtil.hitMole("本波剩余地洞数量为：${burrowIndices.size}，索引列表为：${burrowIndices}")

                //生成炸弹
                if (count in bombBociIndex) {
                    if (burrowIndices.isNotEmpty()) {
                        burrowIndices.random().let {
                            burrowList[it].apply {
                                type = BurrowType.BOMB
                                num = 0f
                                needSeeAd = false
                            }
                            burrowIndices.remove(it)
                            hitMoleAdapter.notifyItemChanged(it)  //刷新显示炸弹动画
                            if (App.DEBUG) LogUtil.hitMole("添加炸弹到地洞，地洞索引为：$it")
                        }
                    }
                }

                val moleNum = (1..3).random() //随机地鼠数量
                if (App.DEBUG) LogUtil.hitMole("本波老鼠数量为：$moleNum")
                repeat(moleNum) { i ->
                    if (burrowIndices.isNotEmpty()) {
                        if (count in needSeeAdBociIndex && i == 0) {
                            val aware = (10..20).random() / 10f
                            if (App.DEBUG) LogUtil.hitMole("添加广告鼠：$moleNum ,奖励随机区间(1.0 .. 2.0)1位小数结果为：$aware")
                            burrowIndices.random().let {
                                burrowList[it].apply {
                                    type = BurrowType.MOLE
                                    num = aware
                                    awareType = CurrencyType.GREEN
                                    needSeeAd = true
                                }
                                burrowIndices.remove(it)
                                hitMoleAdapter.notifyItemChanged(it)  //刷新显示地鼠动画
                            }
                        } else {
                            burrowIndices.random().let {
                                burrowList[it].apply {
                                    type = BurrowType.MOLE
                                    num = getRewardByNumber((0..10000).random())
                                    awareType =
                                        if (num < 1f) CurrencyType.GREEN else CurrencyType.GOLD
                                    needSeeAd = false
                                }
                                burrowIndices.remove(it)
                                hitMoleAdapter.notifyItemChanged(it)  //刷新显示地鼠动画
                            }
                        }
                    }
                }
            }

            override fun onFinish() {
                binding.root.postDelayed({
                    inPlayGame = false
                    binding.countDownTv.text = "0"
                    showAwareDialog()
                }, 2000)
            }
        }
        return countDownTimer
    }

    /**
     * 根据数字获取对应的奖励值
     * @param moleTypeNum 输入数字
     * @return 对应的奖励值
     */
    fun getRewardByNumber(moleTypeNum: Int): Float {
        val rewards = listOf(
            0..2703 to 1f,
            2704..4595 to 10f,
            4596..5946 to 20f,
            5947..7027 to 50f,
            7028..7838 to 100f,
            7839..8379 to 200f,
            8380..9190 to 0.1f,
            9191..9731 to 0.2f,
            9732..10000 to 0.3f
        )

        val matchedPair = rewards.find { (range, _) -> moleTypeNum in range }
        val reward = matchedPair?.second ?: 0f
        val matchedRange = matchedPair?.first
        LogUtil.hitMole("添加老鼠，随机类型数字: $moleTypeNum, 区间: ${matchedRange ?: "无匹配"}, 价值: $reward")
        return reward
    }


    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

    fun pauseGame() {
        if ((inPlayGame && isShowSeeAdDialog) || (inPlayGame && isShowAwareDialog)) {
            if (App.DEBUG) LogUtil.hitMole("游戏暂停")
            countDownTimer.cancel()
            EventBus.getDefault().post(AnimEvent(0))
        }
    }

    fun resumeGame() {
        if (inPlayGame && !isShowSeeAdDialog && !isShowAwareDialog) {
            if (App.DEBUG) LogUtil.hitMole("游戏继续")
            getCountDownTimer().start()
            EventBus.getDefault().post(AnimEvent(1))
        }
    }

    /**
     * 向 ConstraintLayout 添加一个 ImageView，并将其从 (startX, startY) 动画移动到 (endX, endY)。
     * @param parent 目标 ConstraintLayout
     * @param resId  ImageView 的图片资源
     * @param startX 起点X（px，相对 parent 左上角）
     * @param startY 起点Y（px，相对 parent 左上角）
     * @param endX   终点X（px，相对 parent 左上角）
     * @param endY   终点Y（px，相对 parent 左上角）
     * @param sizeDp 视图宽高（正方形），默认48dp
     * @param durationMs 动画时长，默认600ms
     * @param removeOnEnd 结束后是否从父布局移除，默认 true
     * @param onEnd 动画结束回调
     * @return 创建的 ImageView
     */
    fun addAndAnimateImage(
        parent: ConstraintLayout,
        startX: Float,
        startY: Float,
        sizeDp: Int = 44,
        durationMs: Long = 500,
        removeOnEnd: Boolean = true,
        onEnd: (() -> Unit)? = null
    ): ImageView {
        val context = parent.context
        val sizePx = (sizeDp * context.resources.displayMetrics.density + 0.5f).toInt()

        binding.clickNumFl.apply {
            effectEndX = x + width / 2f
            effectEndY = y + height / 2f
        }

        // 1) 创建 ImageView 并添加到父布局
        val iv = ImageView(context).apply {
            id = View.generateViewId()
            setImageResource(R.drawable.ic_star)
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = ActionBar.LayoutParams(sizePx, sizePx).apply {
                // 作为自由位置元素，不强加约束；位置用 translation 控制，不改变原始布局
            }
            // 先放到起点（使用 translation 不会触发布局重排，更平滑也更安全）
            translationX = startX
            translationY = startY
        }
        parent.addView(iv)

        // 2) 开启动画（确保父视图已布局后再执行，避免坐标系未就绪）
        parent.post {
            iv.animate()
                .translationX(effectEndX)
                .translationY(effectEndY)
                .setDuration(durationMs)
                .setInterpolator(FastOutSlowInInterpolator())
                .withEndAction {
                    onEnd?.invoke()
                    if (removeOnEnd) {
                        (iv.parent as? ViewGroup)?.removeView(iv)
                    }
                }
                .start()
        }

        return iv
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGameSeeAdEvent(event: GameSeeAdEvent) {
        isShowSeeAdDialog = true
        pauseGame()
        PopupHelper.createDialog(
            this,
            GameSeeAdDialog(
                this, event.aware,
                AdIndex.MOLE_GAME_SEE_AD_INDEX,
                {
                    isShowSeeAdDialog = false
                    resumeGame()
                    it.dismiss()
                }, {
                    isShowSeeAdDialog = false
                    binding.apply {
                        greenNumTv.text =
                            " $ ${event.aware}"
                        toastLL.visible()
                        toastLL.postDelayed(
                            {
                                toastLL.gone()
                            }, 1500
                        )
                        awareGreen += event.aware
                    }
                    resumeGame()
                }
            )
        ).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveInterAdCDTimeEvent(event: InterAdCDTimeEvent) {
        if (!isShow) return

        if (App.DEBUG) {
            LogUtil.showInterAd("打地鼠界面收到插屏广告倒计时结束事件")
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
            }
        }
    }
}