package com.allever.video.editor.ui.widget

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout


class EffectFrameView : LinearLayout {
    private lateinit var contentLayout: FrameLayout
    private lateinit var btnLeft: ImageView
    private lateinit var btnRight: ImageView


    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

//        contentLayout = findViewById(R.id.container)
//        btnLeft = findViewById(R.id.btn_left)
//        btnRight = findViewById(R.id.btn_right)
//
//        val transition = LayoutTransition()
//        contentLayout.layoutTransition = transition
//        transition.enableTransitionType(LayoutTransition.CHANGING)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    fun setEditState(show: Boolean) {
//        if (show) {
//            this.setBackgroundColor(Color.WHITE)
//            btnLeft.visibility = View.VISIBLE
//            btnRight.visibility = View.VISIBLE
//        } else {
//            this.setBackgroundColor(Color.TRANSPARENT)
//            btnLeft.visibility = View.INVISIBLE
//            btnRight.visibility = View.INVISIBLE
//        }
    }

}
