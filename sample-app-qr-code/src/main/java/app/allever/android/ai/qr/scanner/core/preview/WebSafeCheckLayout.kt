package app.allever.android.ai.qr.scanner.core.preview

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.allever.app.qr.code.scaner.R
import android.view.animation.LinearInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.android.absbase.helper.log.DLog
import com.android.absbase.utils.TaskRunnable


class WebSafeCheckLayout : LinearLayout {
    companion object {
        private var TAG = WebSafeCheckLayout::class.java.name
        const val TYPE_CHECKING = 0
        const val TYPE_SECURE = 1
        const val TYPE_MALICIOUS = 2
        const val TYPE_UNKNOWN = 3

        private const val WHAT_RESULT = 1;
    }

    private var mCheckIcon: ImageView? = null
    private var mCheckText: TextView? = null
    private var mRotateAnimation: RotateAnimation? = null
    var onCheckListener: OnCheckLinsenter? = null
    var type: Int = TYPE_CHECKING
        set(value) {
            mRotateAnimation?.cancel()
            when (value) {
                TYPE_CHECKING -> {
                    mCheckIcon?.setImageResource(R.drawable.qr_icon_check_web_loading)
                    mCheckText?.setText(R.string.result_website_detecting)
                    mCheckText?.setTextColor(resources.getColor(R.color.qr_result_website_check_detecting))

                    val rotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    rotateAnimation.fillAfter = true
                    rotateAnimation.duration = 1500
                    rotateAnimation.repeatCount = -1
                    rotateAnimation.interpolator = LinearInterpolator()
                    mRotateAnimation = rotateAnimation
                    mCheckIcon?.startAnimation(mRotateAnimation)
                }
                TYPE_SECURE -> {
                    mCheckIcon?.setImageResource(R.drawable.qr_icon_check_web_safe)
                    mCheckText?.setText(R.string.result_website_secure)
                    mCheckText?.setTextColor(resources.getColor(R.color.qr_result_website_check_secure))
                }
                TYPE_MALICIOUS -> {
                    mCheckIcon?.setImageResource(R.drawable.qr_icon_check_web_dangerous)
                    mCheckText?.setText(R.string.result_website_malicious)
                    mCheckText?.setTextColor(resources.getColor(R.color.qr_result_website_check_malicious))

                }
                else -> {
                    mCheckIcon?.setImageResource(R.drawable.qr_icon_check_web_unknown)
                    mCheckText?.setText(R.string.result_website_unknown)
                    mCheckText?.setTextColor(resources.getColor(R.color.qr_result_website_check_unknown))

                }
            }
            field = value
            onCheckListener?.onChecked(value)
        }

    private var mHandler: Handler = Handler(Handler.Callback { msg ->
        when (msg?.what) {
            WHAT_RESULT -> {
                type = msg.arg1
                if (msg.arg1 == TYPE_UNKNOWN) {
                    DLog.d(TAG, "check cause: " + (msg.obj as? String ?: "unknown"))
                }
            }
        }
        true
    })

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
        mCheckIcon = findViewById(R.id.check_icon) as? ImageView?
        mCheckIcon?.setImageResource(R.drawable.qr_icon_check_web_loading)
        mCheckIcon?.drawable?.mutate()

        mCheckText = findViewById(R.id.check_text) as? TextView?
    }

    fun start(uri: String) {
        type = TYPE_CHECKING

        if (!uri.isNullOrEmpty()) {
            TaskRunnable.run({
                val sb = StringBuffer()
                val type = WebSafeCheck.getSafeTypeIfNeedrequestUri(uri, sb)
                sendMsg(WHAT_RESULT, type, sb.toString())
            }, 0, TaskRunnable.TYPE_ASYNC)
        } else {
            sendMsg(WHAT_RESULT, TYPE_UNKNOWN, "uri is null")
        }
    }

    fun stop() {
        if (type == TYPE_CHECKING) {
            type = TYPE_UNKNOWN
        }
    }

    private fun sendMsg(what: Int, arg1: Int?, obj: Any?) {
        val msg = Message.obtain(mHandler, what)
        if (arg1 != null) {
            msg.arg1 = arg1
        }
        if (obj != null) {
            msg.obj = obj
        }
        msg.sendToTarget()
    }

    interface OnCheckLinsenter {
        fun onChecked(type: Int)
    }
}
