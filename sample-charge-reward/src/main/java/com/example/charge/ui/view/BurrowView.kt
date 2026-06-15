package com.example.charge.ui.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.graphics.toColorInt
import com.airbnb.lottie.LottieDrawable
import app.allever.android.lib.core.app.App
import com.example.charge.R
import com.example.charge.constant.Burrow
import com.example.charge.constant.BurrowType
import com.example.charge.constant.HitMoleAnim
import com.example.charge.currency.CurrencyType
import com.example.charge.databinding.ViewBurrowBinding
import com.example.charge.event.AnimEvent
import com.example.charge.event.GameSeeAdEvent
import com.example.charge.utils.CountryUtil
import com.example.charge.utils.LogUtil
import com.example.charge.utils.gone
import com.example.charge.utils.setVisible
import com.example.charge.utils.visible
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BurrowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewBurrowBinding.inflate(LayoutInflater.from(context), this, true)
    private var moveDownAnimator: ValueAnimator? = null //向下移动的动画
    private var moveUpAnimator: ValueAnimator? = null //向上移动的动画
    private var animViewGoneY = 320f //动画视图隐藏的Y坐标
    private var animViewShowY = 40f //动画视图显示的Y坐标
    private var mDuration = 500L

    var allowClick = false
    var anim: HitMoleAnim = HitMoleAnim.MOLE
    var hitAnim: HitMoleAnim = HitMoleAnim.MOLE_HIT
    var isPause = false

    var clickX = 0f
    var clickY = 0f

    init {
        //注册eventBus
        EventBus.getDefault().register(this)
        binding.animView.setOnTouchListener { v, ev ->
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    clickX = ev.rawX
                    clickY = ev.rawY
                    false
                }

                else -> false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun showAnimView(
        burrow: Burrow,
        clickCallBack: (burrow: Burrow, clickX: Float, clickY: Float) -> Unit = { aware, clickX, clickY -> },
        animFinishCallBack: () -> Unit = {}
    ) {

        if (burrow.type == BurrowType.MOLE) {
            anim = HitMoleAnim.MOLE
            hitAnim = HitMoleAnim.MOLE_HIT
            binding.awareFl.visible()
            binding.seeAdImg.setVisible(burrow.needSeeAd)
        } else {
            anim = HitMoleAnim.BOMB
            hitAnim = HitMoleAnim.BOMB_HIT
            binding.awareFl.gone()
        }
        if (burrow.awareType == CurrencyType.GOLD) {
            binding.curry.setImageResource(R.drawable.ic_gold)
            if (burrow.num == 1f) {
                binding.awareFl.gone()
            } else {
                binding.numTv.apply {
                    visible()
                    textColorBuilder
                        .setTextColor("#FFFFFF13".toColorInt())
                        .setTextStrokeSize(8)
                        .setTextStrokeColor("#FF6C2700".toColorInt())
                        .intoTextColor()
                    text = " ${burrow.num.toInt()} "
                }
            }
        } else {
            binding.curry.setImageResource(R.drawable.ic_green)
            binding.numTv.apply {
                textColorBuilder
                    .setTextColor("#FF9CFF54".toColorInt())
                    .setTextStrokeSize(8)
                    .setTextStrokeColor("#FF016C03".toColorInt())
                    .intoTextColor()
                text = " ${CountryUtil.getSymbolByCode()}${burrow.num} "
            }
        }

        allowClick = true
        var isClick = false
        isPause = false

        binding.animView.apply {
            playAnim(anim)
            y = animViewGoneY
            binding.awareFl.y = animViewGoneY

            getMoveUpAnimator {
                if (!isClick) {
                    getMoveDownAnimator(1000L) {
                        if (!isClick) animFinishCallBack.invoke()
                    }.start()
                }
            }.start()

            setOnClickListener{
                if (allowClick && !isPause) {
                    if (burrow.type == BurrowType.MOLE && burrow.needSeeAd) {
                        EventBus.getDefault().post(GameSeeAdEvent(burrow.num))
                    }
                    allowClick = false
                    isClick = true
                    moveUpAnimator?.cancel()
                    moveDownAnimator?.cancel()
                    playAnim(hitAnim, 1)
                    if (burrow.type == BurrowType.MOLE) {
                        playHitAnim(HitMoleAnim.MOLE_HIT_Effect)
                    }
                    getMoveDownAnimator {
                        animFinishCallBack.invoke()
                    }.start()

                    clickCallBack.invoke(burrow, clickX, clickY)
                }
            }
        }
    }

    private fun getMoveUpAnimator(
        randomDuration: Long = (0L..500L).random(),
        endCallBack: () -> Unit = {}
    ): ValueAnimator {
        moveUpAnimator = ValueAnimator.ofFloat(animViewGoneY, animViewShowY).apply {
            duration = randomDuration
            interpolator = LinearInterpolator()
            addUpdateListener {
                val animatedValue = it.animatedValue as Float
                binding.animView.y = animatedValue
                binding.awareFl.y = animatedValue
            }
            doOnEnd {
                endCallBack.invoke()
            }
        }
        return moveUpAnimator!!
    }

    private fun getMoveDownAnimator(delay: Long = 0, endCallBack: () -> Unit = {}): ValueAnimator {
        val startY = binding.animView.y
        val time = ((animViewGoneY - startY) / binding.animView.height) * mDuration
        moveDownAnimator = ValueAnimator.ofFloat(startY, animViewGoneY).apply {
            duration = time.toLong()
            startDelay = delay
            interpolator = LinearInterpolator()
            addUpdateListener {
                val animatedValue = it.animatedValue as Float
                binding.animView.y = animatedValue
                binding.awareFl.y = animatedValue
            }
            doOnEnd {
                binding.animView.cancelAnimation()
                endCallBack.invoke()
            }
        }
        return moveDownAnimator!!
    }

    fun playAnim(anim: HitMoleAnim, loop: Int = LottieDrawable.INFINITE) {
        if (App.DEBUG) {
            LogUtil.hitMole("playAnim: ${anim.fileName} ")
        }
        binding.animView.apply {
            cancelAnimation()
            setAnimation(anim.fileName)
            repeatCount = loop
            playAnimation()
        }
    }

    fun playHitAnim(anim: HitMoleAnim) {
        if (App.DEBUG) {
            LogUtil.hitMole("playHitAnim: ${anim.fileName} ")
        }
        binding.hitAnimView.apply {
            cancelAnimation()
            setAnimation(anim.fileName)
            repeatCount = 0
            playAnimation()
        }
    }

    fun pauseAnim() {
        isPause = true
        binding.animView.pauseAnimation()
        moveUpAnimator?.pause()
        moveDownAnimator?.pause()
    }

    fun resumeAnim() {
        isPause = false
        binding.animView.resumeAnimation()
        moveUpAnimator?.resume()
        moveDownAnimator?.resume()
    }


    /*    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
                    binding.animView.apply {
                        post {
                            animViewShowY = y - height
                            animViewGoneY = y
                            LogUtil.hitMole("animViewShowY: $animViewShowY  animViewGoneY: $animViewGoneY")
                        }
                    }
        }*/

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerEventBus()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        moveUpAnimator?.cancel()
        moveDownAnimator?.cancel()
        moveUpAnimator = null
        moveDownAnimator = null
        unregisterEventBus()
    }

    private fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAnimEvent(event: AnimEvent) {
        if (event.state == 0)
            pauseAnim()
        else
            resumeAnim()
    }

}