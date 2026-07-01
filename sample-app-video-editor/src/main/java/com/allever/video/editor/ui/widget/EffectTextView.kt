package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.utils.KeyboardUtils
import java.io.File
import android.graphics.PaintFlagsDrawFilter
import com.allever.video.editor.R


/**
 * @author dell
 */
class EffectTextView : AppCompatEditText {

    private var defaultContent = ""
    private var defaultContentTip = ""
    private var padding = 0f

    private var mBackgroundCanvas: Canvas? = null
    private var mTextCanvas: Canvas? = null
    private var mBackgroundRect: RectF? = null
    private var mTextPaint: Paint? = null
    private var mBackgroundPaint: Paint? = null
    private var mBackgroundBitmap: Bitmap? = null
    private var mTextBitmap: Bitmap? = null
    private var mCornerRadius = 0f
    private var mHollowMode = false
    private var mBackgroundColor = Color.WHITE
//    private var mPaintFlagsDrawFilter: PaintFlagsDrawFilter? = null

    /**
     * 字体的缓存
     */
    private var typeCache = hashMapOf<String,Typeface>()

    /**
     * 上一次确认的内容
     */
    private var prevTextConfirm: String? = null

    var onTextConfirmListener: OnTextConfirmListener? = null
    var prevVisibility = this.visibility

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }
    fun init(context: Context, attrs: AttributeSet?) {
        padding = ResourcesUtils.getDimension(R.dimen.video_edit_frame_text_item_padding)
        defaultContent = ResourcesUtils.getString(R.string.effect_text_bean_default_content)
        defaultContentTip = ResourcesUtils.getString(R.string.effect_text_bean_default_content_tip)
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(content: Editable?) {}
            override fun beforeTextChanged(content: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(content: CharSequence?, start: Int, before: Int, count: Int) {
                if (content?.toString()?.isNotEmpty() == true) {
                    this@EffectTextView.hint = ""
                } else {
                    this@EffectTextView.hint = defaultContentTip
                }
                onTextConfirmListener?.onTextChanged(content?.toString() ?: "")
            }
        })

        //画文字的paint
        mTextPaint = Paint().also {
//            it.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
            it.setAntiAlias(true)
        }
        //这是镂空的关键

        mBackgroundPaint = Paint().also {
            it.setColor(Color.WHITE)
            it.setAntiAlias(true)
        }
//        mPaintFlagsDrawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    fun setDefaultTextTip(){
//        this@EffectTextView.hint = defaultContentTip
    }
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            if(prevVisibility != View.VISIBLE){
                this.visibility = View.VISIBLE
            }
            isCursorVisible = true
            //将光标移至文字末尾
            setSelection(text?.length!!)
            requestFocus()
            KeyboardUtils.showKeyboard(this)
//            this@EffectTextView.hint = defaultContentTip
        } else {
            isCursorVisible = false
            KeyboardUtils.hideKeyboard(this)

            val currentText = text.toString()
            if (!TextUtils.equals(prevTextConfirm, currentText)) {
                onTextConfirmListener?.onTextConfirm(currentText)
                prevTextConfirm = currentText
            }
//            this@EffectTextView.hint = defaultContent
        }
    }

    fun setCursorColor(@ColorInt color: Int) {
        try {
            // Get the cursor resource id
            var field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId = field.getInt(this)

            // Get the editor
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor = field.get(this)

            // Get the drawable and set a color filter
            val drawable = ContextCompat.getDrawable(this.context, drawableResId)
            drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            val drawables = arrayOf(drawable, drawable)

            // Set the drawables
            field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field.set(editor, drawables)
        } catch (ignored: Exception) {
        }

    }
    fun setFontName(path: String, isLocal: Boolean = false) {
        if(path.isEmpty()){
            typeface = Typeface.DEFAULT
            return
        }
        try {
            val tempTypeface = typeCache[path]
            typeface = if(tempTypeface == null){
                val newTypeface: Typeface = if (isLocal) {
                    Typeface.createFromFile(File(path))
                } else {
                    Typeface.createFromAsset(context.assets, path)
                }
                typeCache[path] = newTypeface
                newTypeface
            }else{
                tempTypeface
            }
        } catch (ignored: Exception) {
        }
    }

    fun setHollowMode(hollow: Boolean) {
        mHollowMode = hollow
        if (hollow) {
            //画文字的paint
            mTextPaint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        } else {
            mTextPaint?.xfermode = null
        }
        mTextPaint?.setAntiAlias(true)
    }

    override fun setBackgroundColor(color: Int) {
//        super.setBackgroundColor(color)
        mBackgroundColor = color
        mBackgroundPaint?.color = color
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        reset(w, h)
    }

    fun reset(w: Int = width, h: Int = height) {
        if (w == 0 || h == 0) return
        if (mBackgroundBitmap == null || mTextBitmap == null) {
            return
        }
        mBackgroundBitmap?.recycle()
        mBackgroundBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mBackgroundCanvas = Canvas(mBackgroundBitmap!!)
        mTextBitmap?.recycle()
        mTextBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mTextCanvas = Canvas(mTextBitmap!!)
        mBackgroundRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        if (mBackgroundBitmap == null || mTextBitmap == null) {
            return
        }
        if (!mHollowMode) {
            super.onDraw(canvas)
            drawBackground(canvas)
        } else {
            super.onDraw(mTextCanvas!!)
            mBackgroundCanvas?.let { drawBackground(it) }
            val sc: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), null)
            } else {
                canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), null, Canvas.ALL_SAVE_FLAG)
            }
//            canvas.drawFilter = mPaintFlagsDrawFilter
            canvas.drawBitmap(mBackgroundBitmap!!, 0f, 0f, null)
//            val mBitmapShader = BitmapShader(mTextBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
//            mTextPaint?.setShader(mBitmapShader);
            canvas.drawBitmap(mTextBitmap!!, 0f, 0f, mTextPaint)
            canvas.restoreToCount(sc)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        if (mBackgroundRect == null || mBackgroundPaint == null) {
            return
        }
        if (mCornerRadius > 0) {
            canvas.drawRoundRect(mBackgroundRect!!, mCornerRadius, mCornerRadius, mBackgroundPaint!!)
        } else {
            canvas.drawColor(mBackgroundColor)
        }
    }

    interface OnTextConfirmListener {
        fun onTextConfirm(text: String)
        fun onTextChanged(text: String)
    }

}