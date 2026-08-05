package z.app.allever.android.lib.widget.shake

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import z.app.allever.android.lib.widget.shake.ShakeHelper.createShakeAnimator

/**
 * @author allever
 */
class ShakeImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var mObjectAnimator: ObjectAnimator? = null

    private var mLoop = false

    init {
        init()
    }

    private fun init() {
        mObjectAnimator = createShakeAnimator(this, mLoop)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    fun start(loop: Boolean) {
        if (mObjectAnimator == null) {
            return
        }

        mObjectAnimator?.cancel()
        mLoop = loop
        var repeatCount = 0
        if (mLoop) {
            repeatCount = ValueAnimator.INFINITE
        }
        mObjectAnimator?.repeatCount = repeatCount
        mObjectAnimator?.start()
    }

    fun stop() {
        mObjectAnimator?.cancel()
    }
}
