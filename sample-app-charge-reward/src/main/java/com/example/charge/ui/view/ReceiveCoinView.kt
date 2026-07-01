package com.example.charge.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import app.allever.android.lib.core.app.App
import com.example.charge.constant.Coin
import com.example.charge.constant.HitMoleAnim
import com.example.charge.databinding.ViewRecieveCoinBinding
import com.example.charge.utils.CoinGenerator
import com.example.charge.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ReceiveCoinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewRecieveCoinBinding.inflate(LayoutInflater.from(context), this, true)

    interface Listener {
        fun onCoinCaught(coinView: View, coin: Coin) // 当金币被狐狸成功接住时调用
        fun onCoinSpawned(coinView: View, coin: Coin){} //当金币被生成并开始下落时调用。
        fun onCoinMissed(coinView: View, coin: Coin){} // 当金币未被接住而掉出屏幕底部时调用。
        fun onGameStarted(totalSeconds: Int) // 当游戏开始时调用。
        fun onGamePaused() // 当游戏暂停时调用。
        fun onGameResumed()  // 当游戏恢复时调用。
        fun onGameStopped() // 当游戏结束（无论成功或失败）时调用。
        fun onTick(remainingSeconds: Int) // 每秒回调
        fun onSeeAd(coinView: View, coin: Coin)   // 当需要展示广告时调用
    }

    var listener: Listener? = null

    // 配置
    var fallDurationMsRange: LongRange = 1000L..3000L    // 金币下落时间
    var catchVerticalTolerancePx: Int = dp(0f)  // 接到金币的允许误差
    var spawnIntervalMs: Long = 500L  //每波间隔
    var spawnBatch: IntRange = 1..2 //每波金币数
    var totalGameSeconds: Int = 30  // 游戏时长

    // 运行状态
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var spawnerJob: Job? = null    // 生成器
    private var timerJob: Job? = null  // 计时器
    private val activeAnimators = mutableSetOf<ValueAnimator>() // 正在运行的动画
    private val activeCoins = mutableSetOf<View>()  // 正在下落的金币View
    private var isRunning = false  // 游戏是否运行中
    private var isPaused = false  // 游戏是否暂停

    private var adScheduleSeconds = mutableSetOf<Int>()   // 本局安排在这些秒内必产出一个 needSeeAd=true
    private var pendingAdInSecond = mutableSetOf<Int>()   // 该秒内是否还没产出广告币
    private var secondsElapsed = 0                        // 已经过去的秒数（用于定位当前 second）

    // 拖拽狐狸（水平）
    private var foxDownX = 0f
    private var foxStartX = 0f

    init {
        setOnTouchListener { v, ev ->
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (isRunning && !isPaused) {
                        foxDownX = ev.rawX
                        foxStartX = binding.foxImg.x
                        return@setOnTouchListener true
                    }
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isRunning && !isPaused) {
                        val dx = ev.rawX - foxDownX
                        val newX = clamp(foxStartX + dx, -100f, width - binding.foxImg.width.toFloat())
                        binding.foxImg.x = newX
                        binding.foxEffectImg.x = newX
                        return@setOnTouchListener true
                    }
                    false
                }

                else -> false
            }
        }
    }

    fun startGame(listener: Listener) {
        this.listener = listener
        doOnLayout {
            stopGame(invokeListener = false) // 清理旧状态

            // ===== 初始化广告金币调度（本局 1~2 个）=====
            val adCount = Random.nextInt(1, 3) // 1 或 2
            adScheduleSeconds.clear()
            // 避免选到 0 和最后 1 秒（更稳妥）
            val candidates = (1 until totalGameSeconds - 1).toMutableList()
            candidates.shuffle()
            adScheduleSeconds.addAll(candidates.take(adCount))
            pendingAdInSecond = adScheduleSeconds.toMutableSet()

            isRunning = true
            isPaused = false
            secondsElapsed = 0
            listener.onGameStarted(totalGameSeconds)

            // 生成器
            spawnerJob = scope.launch {
                while (isActive && isRunning) {
                    if (!isPaused) {
                        val spanNumber = spawnBatch.random()
                        repeat(spanNumber) { spawnOneCoin() }
                    }
                    delay(spawnIntervalMs)
                }
            }

            // ===== 计时器（每秒回调）=====
            timerJob = scope.launch {
                var remaining = totalGameSeconds
                while (isActive && isRunning && remaining >= 0) {
                    if (!isPaused) {
                        listener.onTick(remaining)
                        if (remaining == 0) {
                            // 时间到 -> 结束游戏
                            stopGame()
                            break
                        }
                        delay(1000)
                        secondsElapsed += 1
                        remaining -= 1
                    } else {
                        delay(50)
                    }
                }
            }
        }
    }

    fun pauseGame() {
        if (!isRunning || isPaused) return
        isPaused = true
        activeAnimators.forEach { if (it.isRunning) it.pause() }
        listener?.onGamePaused()
    }

    fun resumeGame() {
        if (!isRunning || !isPaused) return
        isPaused = false
        activeAnimators.forEach { if (it.isPaused) it.resume() }
        listener?.onGameResumed()
    }

    fun stopGame(invokeListener: Boolean = true) {
        isRunning = false
        isPaused = false
        spawnerJob?.cancel(); spawnerJob = null
        timerJob?.cancel(); timerJob = null

        val anims = activeAnimators.toList()
        activeAnimators.clear()
        anims.forEach { it.cancel() }

        val coins = activeCoins.toList()
        activeCoins.clear()
        coins.forEach {
            removeCoinView(it)
        }
        // 水平居中狐狸
        binding.foxImg.doOnLayout { fox ->
            fox.x = ((width - fox.width) / 2f).coerceAtLeast(0f)
        }

        if (invokeListener) listener?.onGameStopped()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopGame()
        scope.cancel()
        binding.foxImg.cancelAnimation()
    }

    // ---------------- 内部细节 ----------------

    private fun spawnOneCoin() {
        if (width <= 0 || height <= 0) return

        val coinData = CoinGenerator.randomCoin()

        val nowSecond = secondsElapsed.coerceIn(0, totalGameSeconds)
        if (pendingAdInSecond.contains(nowSecond)) {
            coinData.needSeeAd = true
            coinData.num = (10..20).random()/10f
            pendingAdInSecond.remove(nowSecond)
        }

        val coin = CoinView(context).apply {
            layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
            init(coinData)
        }
        binding.mainFl.addView(coin)

        coin.doOnLayout {
            val coinW = coin.width
            val coinH = coin.height

            val startX = Random.nextInt(0, max(1, width - coinW + 1)).toFloat()
            coin.x = startX
            coin.y = -coinH.toFloat()

            listener?.onCoinSpawned(coin, coinData)

            val endY = height.toFloat() + coinH
            val duration = Random.nextLong(fallDurationMsRange.first, fallDurationMsRange.last + 1)

            val animator = ValueAnimator.ofFloat(coin.y, endY).apply {
                this.duration = duration
                interpolator = LinearInterpolator()
                addUpdateListener { va ->
                    val curY = va.animatedValue as Float
                    coin.y = curY

                    if (checkCatch(coin)) {
                        this.cancel()
                        if (activeCoins.remove(coin)) {
                            removeCoinView(coin)
                            val data = (coin.tag as? Coin) ?: coinData
                            // 常规命中回调
                            listener?.onCoinCaught(coin, data)
                            playReceiveCoinAnim() // 收金币动画
                            // ===== 命中广告币 -> 触发 seeAd 回调 =====
                            if (data.needSeeAd) {
                                listener?.onSeeAd(coin, data)
                            }
                        }
                    } else if (curY >= height) {
                        this.cancel()
                        if (activeCoins.remove(coin)) {
                            removeCoinView(coin)
                            listener?.onCoinMissed(coin, coinData)
                        }
                    }
                }
                doOnEndOrCancel { activeAnimators.remove(this) }
            }

            activeCoins.add(coin)
            activeAnimators.add(animator)
            if (!isPaused) animator.start() else animator.pause()
        }
    }

    fun removeCoinView(coin: View) {
        binding.mainFl.removeView(coin)
    }
    /** 命中判定 */
    private fun checkCatch(coin: View): Boolean {
        val fox = binding.foxImg

        // 1) 根据给定的“上、下、左、右”内边距，计算篮子口在父容器坐标系下的矩形
        val mouthLeft   = fox.x + dp(62f)   // 左边距
        val mouthTop    = fox.y + dp(175f)  // 上边距 169dp
        val mouthRight  = fox.x + fox.width - dp(36f)  // 右边距
        val mouthBottom = fox.y + fox.height - dp(74f) // 下边距
        val basketMouth = RectF(mouthLeft, mouthTop, mouthRight, mouthBottom)

        // 2) 金币当前矩形
        val coinRect = RectF(coin.x, coin.y, coin.x + coin.width, coin.y + coin.height)

        // 3) 命中规则：金币的“底边”落在篮子口竖直范围内（带容差），且金币与篮子口水平重叠
        val coinBottomY = coinRect.bottom
        val verticalOk = coinBottomY >= (basketMouth.top - catchVerticalTolerancePx) &&
                coinBottomY <= (basketMouth.bottom + catchVerticalTolerancePx)

        val horizontalOk = coinRect.right >= basketMouth.left &&
                coinRect.left  <= basketMouth.right

        return verticalOk && horizontalOk
    }

    fun playReceiveCoinAnim() {
        binding.foxEffectImg.apply {
            cancelAnimation()
            setAnimation(HitMoleAnim.ReceiveCoin.fileName)
            repeatCount = 0
            playAnimation()
        }
    }

    // 工具
    private fun ValueAnimator.doOnEndOrCancel(block: () -> Unit) {
        addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) = block()
            override fun onAnimationCancel(animation: android.animation.Animator) = block()
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }

    private fun dp(v: Float): Int = (v * resources.displayMetrics.density + 0.5f).toInt()
    private fun clamp(v: Float, minV: Float, maxV: Float): Float = max(minV, min(maxV, v))
}


