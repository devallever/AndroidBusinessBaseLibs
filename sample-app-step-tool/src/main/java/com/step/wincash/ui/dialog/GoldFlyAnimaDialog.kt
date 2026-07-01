package com.step.wincash.ui.dialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.lxj.xpopup.impl.FullScreenPopupView
import com.step.wincash.R
import com.step.wincash.databinding.DialogGoldFlyAnimaBinding
import kotlin.random.Random

class GoldFlyAnimaDialog(context: Context, private val viewGroup: ViewGroup, private val animCount: Int = 10, private val targetView: View, private val resId: Int, private val dismissCallBack: () -> Unit) :
    FullScreenPopupView(context) {

    private lateinit var binding: DialogGoldFlyAnimaBinding


    override fun getImplLayoutId(): Int {
        return R.layout.dialog_gold_fly_anima
    }

    override fun onCreate() {
        super.onCreate()
        binding = DialogGoldFlyAnimaBinding.bind(this.contentView)
//        SoundUtil.play(SoundRawId.GET_REWARD.id)
        processAnimator()
    }

    override fun onDismiss() {
        super.onDismiss()
        dismissCallBack.invoke()
    }

    private fun processAnimator() {
        try {
            for (index in 0 until animCount) {

                val centerX = viewGroup.width / 2f
                val centerY = viewGroup.height / 2f - 100
                val startView = ImageView(context)

                val drawable = ContextCompat.getDrawable(context, resId) ?: continue
                val canvas = Canvas()
                val icon = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                canvas.setBitmap(icon)
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                drawable.draw(canvas)

                startView.setImageBitmap(icon)
                startView.adjustViewBounds = true
                startView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                startView.layoutParams = LayoutParams(
                    targetView.width, targetView.height
                )

                val startX = centerX - icon.width / 2 + if (index % 2 == 0) {
                    Random.nextInt(-targetView.width, 0).toFloat()
                } else {
                    Random.nextInt(0, targetView.width).toFloat()
                }

                val startY = centerY + if (index % 2 == 0) {
                    Random.nextInt(-targetView.height / 2, 0).toFloat()
                } else {
                    Random.nextInt(0, targetView.height / 2).toFloat()
                }

                startView.translationX = startX
                startView.translationY = startY
                binding.flRoot.addView(startView)

                val endLocation = IntArray(2)
                targetView.getLocationOnScreen(endLocation)

                // (重点关注)将属性动画作用到View中
                // 步骤1:创建初始动画时的对象点  & 结束动画时的对象点
                val startPoint = PointF(startX, startY)
                val endPoint = PointF(endLocation[0].toFloat(), endLocation[1].toFloat())


                // 步骤2:创建动画对象 & 设置初始值 和 结束值
                val animator = ValueAnimator.ofObject({ fraction, startValue, endValue ->
                    // 将动画初始值startValue 和 动画结束值endValue 强制类型转换成Point对象
                    val start = startValue as PointF
                    val end = endValue as PointF
                    // 根据fraction来计算当前动画的x和y的值
                    val x = start.x + fraction * (end.x - start.x)
                    val y = start.y + fraction * (end.y - start.y)
                    // 将计算后的坐标封装到一个新的Point对象中并返回
                    PointF(x, y)
                }, startPoint, endPoint)

                animator.setDuration(1000)
                animator.startDelay = (index * 50).toLong()

                animator.addUpdateListener { valueAnimator ->
                    //将每次变化后的坐标值（估值器PointEvaluator中evaluate()返回的Point对象值）到当前坐标值对象（currentPoint）
                    //从而更新当前坐标值（currentPoint）
                    val currentPoint = valueAnimator.animatedValue as PointF
                    startView.translationX = currentPoint.x - targetView.width / 2f + icon.width / 2
                    startView.translationY = currentPoint.y
                }

                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.flRoot.removeView(startView)
                        if (index == animCount - 1) {
                            dismiss()
                        }
                    }
                })
                animator.start()

            }
//        MusicTool.playSound(MusicTool.TYPE_COIN_FLY)
        }catch (e: Exception){
            e.printStackTrace()
            dismiss()
        }

    }


}