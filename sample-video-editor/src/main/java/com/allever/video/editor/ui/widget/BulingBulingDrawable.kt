package com.allever.video.editor.ui.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.allever.video.editor.R

class BulingBulingDrawable : BitmapDrawable {
    private val backgroundDrawable: Drawable
    private var valueAnimator: ValueAnimator? = null
    private val porterDuffXfermode: PorterDuffXfermode

    constructor(res: Resources, drawable: Drawable) : super(res, (drawable as? BitmapDrawable)?.bitmap) {
        backgroundDrawable = res.getDrawable(R.drawable.icon_setting_premium_light)
        porterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    }

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
        backgroundDrawable.bounds = bounds
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        backgroundDrawable.setBounds(left, top, right, bottom)
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)

        valueAnimator = null
        startAnimator()
    }

    private fun startAnimator() {
        val valueAnimator = valueAnimator
        if (valueAnimator != null) {
            if (!valueAnimator.isRunning) {
                valueAnimator.startDelay = 1000
                valueAnimator.start()
            }
            return
        }
        val width = backgroundDrawable.intrinsicWidth
        val height = backgroundDrawable.intrinsicHeight
        if (bounds != null) {
            this.valueAnimator = ValueAnimator.ofInt(0, bounds.width() + width / 2)
        }
        this.valueAnimator?.also { animator ->
            //            animator.repeatCount = ValueAnimator.INFINITE
            animator.duration = 1000

            animator.addUpdateListener {
                val value = it.animatedValue as Int
                val left = -width / 2 + value
                val top = -height / 2 + value
                backgroundDrawable.setBounds(left,
                        top,
                        left + width,
                        top + height
                )
                invalidateSelf()
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    startAnimator()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })
        }?.start()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.save()
        paint.xfermode = porterDuffXfermode
        canvas.clipRect(bounds)
        canvas.drawBitmap((backgroundDrawable as BitmapDrawable).bitmap, bounds, backgroundDrawable.bounds, paint)
        paint.xfermode = null
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        super.setAlpha(alpha)
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        super.setColorFilter(colorFilter)
    }

    override fun getOpacity(): Int {
        return super.getOpacity()
    }

}
