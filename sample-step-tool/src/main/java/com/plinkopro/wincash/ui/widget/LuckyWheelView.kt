package com.plinkopro.wincash.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.plinkopro.wincash.R
import com.plinkopro.wincash.beans.Cell
import com.plinkopro.wincash.databinding.LuckyWheelViewBinding
import com.plinkopro.wincash.ui.adapter.CellAdapter
import com.plinkopro.wincash.utils.SpKey
import com.plinkopro.wincash.utils.SpUtil
import com.plinkopro.wincash.utils.TimeUtil.isSameDay
import com.plinkopro.wincash.utils.setOnSingleListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

class LuckyWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LuckyWheelViewBinding =
        LuckyWheelViewBinding.inflate(LayoutInflater.from(context), this, true)

    private lateinit var adapter: CellAdapter

    var ringOrder: List<Int> = emptyList()

    var cells = mutableListOf<Cell>()

    private var chances = 0
    private var state = State.IDLE
    private var targetIndex = 0

    @Volatile
    private var forceStop = false

    private val viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val spanCount = 4
    private val spacingPx = dp(3f) // 与网格间距保持一致
    private val gridDecoration = GridSpacingDecoration(spacingPx, spanCount)

    private enum class State { IDLE, SPINNING, DECELERATING }

    init {
        getChances()
        setupView()
    }

    private fun setupView() {
        // 列表 + 适配器
        cells = buildCells4x4() // 中心 4 格隐藏，由 tvCenterBanner 覆盖

        ringOrder = buildRingOrder4x4()
        adapter = CellAdapter().apply {
            setNewData(cells)
        }

        binding.rvGrid.apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            setItemViewCacheSize(20)
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = this@LuckyWheelView.adapter
            setHasFixedSize(true)
            clipToPadding = false
            setPadding(spacingPx, spacingPx, spacingPx, spacingPx)
            if (itemDecorationCount == 0) addItemDecoration(gridDecoration)
        }

        updateChancesUI()

        binding.btnSpin.setOnSingleListener {
            when (state) {
                State.IDLE -> startSpin()
                State.SPINNING -> {
                    forceStop = true
                }

                State.DECELERATING -> Unit
            }
        }

        // 首次布局完成后，计算并放置中心 Banner
        binding.boardFrame.post { layoutCenterBanner() }
    }

    fun getChances() {
        val lastTime = SpUtil.get(SpKey.LAST_TIME_SPIN, 0L)
        chances = if (isSameDay(lastTime)) {
            SpUtil.get(SpKey.LUCKY_WHEEL_CHANCES, 5)
        } else {
            SpUtil.put(SpKey.LAST_TIME_SPIN, System.currentTimeMillis())
            SpUtil.put(SpKey.LUCKY_WHEEL_CHANCES, 5)
            5
        }
    }

    fun setChances(chances: Int) {
        this.chances = chances
        updateChancesUI()
        SpUtil.put(SpKey.LUCKY_WHEEL_CHANCES, chances)
    }

    private fun buildCells4x4(): MutableList<Cell> {
        val list = mutableListOf<Cell>(
            Cell("1500"),
            Cell("20000"),
            Cell("500"),
            Cell("2000"),
            Cell("3000"),
            Cell("", true),
            Cell("", true),
            Cell("2000"),
            Cell("2000"),
            Cell("", true),
            Cell("", true),
            Cell("3000"),
            Cell("2000"),
            Cell("4000"),
            Cell("1000"),
            Cell("1500")
        )
        return list
    }

    /** 外环顺时针位置（12 个） */
    private fun buildRingOrder4x4(): List<Int> = listOf(
        0, 1, 2, 3,
        7, 11,
        15, 14, 13, 12,
        8, 4
    )

    private fun updateChancesUI() {
        binding.tvChances.text = " $chances "
        SpUtil.put(SpKey.LUCKY_WHEEL_CHANCES, chances)
    }

    /** 概率：20000 = 0.1%；其余 11 格平分 99.9% */
    private fun pickTargetIndexWeighted(): Int {
        val p = Random.nextDouble()
        if (p < 0.001) return 1

        // 其余索引列表（排除 20000）
        val others = ringOrder.indices.filter { it != 1 }
        return others.random()
    }

    private fun startSpin() {
        if (chances <= 0) {
            noChances?.invoke()
            return
        }
        chances--

        state = State.SPINNING
        binding.btnSpin.text = context.getString(R.string.stop)
        forceStop = false

        val ringCount = ringOrder.size
        var current = Random.nextInt(ringCount) // 随机起点

        targetIndex = pickTargetIndexWeighted()
        var loopsRemaining = 2 // 更快 ~3s

        viewScope.launch {

            var delayMs = 240L    // 起步延迟
            val minDelay = 100L    // 最高速
            val accelSteps = 10   // 加速步数
            val holdSteps = 10   // 匀速步数

            // 加速
            repeat(accelSteps) {
                current = (current + 1) % ringCount
                updateAdapter(current)
                delay(delayMs)
                delayMs = max(minDelay, (delayMs * 0.80f).toLong())
            }

            // 匀速（可被“停止”打断）
            repeat(holdSteps) {
                if (forceStop) return@repeat
                current = (current + 1) % ringCount
                updateAdapter(current)
                delay(delayMs)
            }

            // 进入减速
            state = State.DECELERATING
            if (forceStop) loopsRemaining = 1 // 用户点击“停止”则更快结束

            fun distance(from: Int, to: Int): Int {
                return if (to >= from) to - from else ringCount - from + to
            }

            var stepsLeft = loopsRemaining * ringCount + distance(current, targetIndex)

            // 逐步减速到停止
            while (stepsLeft > 0) {
                current = (current + 1) % ringCount
                updateAdapter(current)
                stepsLeft--
                delay(delayMs)
                delayMs = (delayMs * 1.10f).toLong().coerceAtMost(320L)
            }

            val result = cells[ringOrder[current]].text
//            Toast.makeText(context, "恭喜获得：$result", Toast.LENGTH_SHORT).show()

            // 复位
            state = State.IDLE
            binding.btnSpin.text = context.getString(R.string.spin_now)
            updateChancesUI()

            // 回调给外部
            onResult?.invoke(result.toInt())

        }
    }

    fun updateAdapter(current: Int) {
        cells.forEach { it.light = false }
        cells[ringOrder[current]].light = true
        adapter.setNewData( cells)
    }

    /** 让中心 Banner 精确覆盖 2x2 中心格，四周各留出 spacingPx 的间距 */
    private fun layoutCenterBanner() {
        val rv = binding.rvGrid
        val frame = binding.boardFrame

        val item = rv.layoutManager?.findViewByPosition(5) ?: return

        // 获取单个 item 的宽高
        val itemWidth = item.width
        val itemHeight = item.height

        // 计算 tvCenterBanner 的宽高：2 倍的 item 尺寸 + 2 倍间距
        val bannerWidth = (itemWidth * 2) + spacingPx
        val bannerHeight = (itemHeight * 2) + spacingPx

        // 设置 tvCenterBanner 的布局
        val lp = LayoutParams(bannerWidth, bannerHeight)
        lp.width = bannerWidth
        lp.height = bannerHeight
        lp.gravity = Gravity.CENTER
        binding.tvCenterBanner.layoutParams = lp
        invalidate()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        binding.boardFrame.post { layoutCenterBanner() }
    }

    // ========== 回调 ==========
    private var onResult: ((Int) -> Unit)? = null

    private var noChances: (() -> Unit)? = null
    fun setOnResultListener(listener: (Int) -> Unit) {
        onResult = listener
    }

    fun setNoChancesListener(listener: () -> Unit) {
        noChances = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope.cancel() // 防泄漏
    }

    private fun dp(v: Float): Int =
        (v * resources.displayMetrics.density).roundToInt()


}
