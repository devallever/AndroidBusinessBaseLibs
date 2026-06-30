package com.allever.video.editor.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.absbase.utils.DeviceUtils;
import com.allever.video.editor.R;
import com.allever.video.editor.utils.FontUtil;
import com.allever.video.editor.utils.TouchUtil;
import com.allever.video.editor.utils.ViewUtil;

/**
 * 自定义的SeekBar 中间有数字的seekbar
 * 注意 这个View不支持设置Padding
 */
public class CustomNumSeekBar extends View {

    private final int DEFAULT_PROGRESS_BG_COLOR = 0x1a000000;

    private int mProgressHeight;

    /**
     * 显示数字的背景
     */
    private Drawable mNumBgTumb;

    /**
     * 按下后的效果
     */
    private Drawable mTouchTumb;

    /**
     * 进度背景
     */
    private Drawable mProgressBgTumb;

    /**
     * 进度
     */
    private Drawable mProgressTumb;

    /**
     * 是否展示文字
     */
    private boolean mShowText = true;

    /**
     * 是否展示顶部文字
     */
    private boolean mShowTopText = false;

    /**
     * 用于绘制数字
     */
    private StaticLayout mTextLayout;

    /**
     * 数字
     */
    private String mText;

    /**
     * 画笔
     */
    private TextPaint mTextPaint;

    /**
     * 回调
     */
    private OnSeekBarChangeListener mListener;

    /**
     * 是否被点击
     */
    private boolean mIsTouch = false;

    /**
     * 是否初始化完成
     */
    private boolean mIsInit = false;

    /**
     * 当前文字的宽
     */
    private float mTextWidth;

    /**
     * 点击后按钮的宽
     */
    private int mTouchWidth;

    /**
     * 显示文字按钮的宽  也是文字的最大长度
     */
    private int mTextMaxWidth;

    /**
     * 进度的最大值
     */
    private int mMax;

    /**
     * 当前的进度
     */
    private int mProgress;

    /**
     * 文字的颜色
     */
    private int mTextColor;

    /**
     * 文字的size
     */
    private int mTextSize;

    /**
     * 顶上数字距离
     */
    private int mNumTopHeight;

    /**
     * 绘制的一些参数
     */
    private float mTextLeft;
    private float mTextTop;
    private RectF mViewRect;
    private Rect mNumBgRect;
    private Rect mTouchRect;
    private Rect mProgressBgRect;
    private RectF mProgressBgRectF;
    private Rect mProgressRect;

    /**
     * 下面用于处理Touch
     */
    private float mDownX;
    private final int TYPE_MOVE = 2;
    private final int TYPE_DOWN = 1;
    private final int TYPE_NONE = 0;
    private int mTouchType = TYPE_NONE;

    /**
     * 默认的颜色style
     */
    private int mDefaultColorStyle;
    private int mWidth;
    private int mHeight;
    private int[] mBackgroundGradient;
    private Paint mBgPaint;
    private Paint mNumTextPaint;

    public CustomNumSeekBar(Context context) {
        this(context, null);
    }

    public CustomNumSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mProgressHeight = context.getResources().getDimensionPixelSize(R.dimen.image_edit_seekbar_height);
        init(context, attrs);
    }

    /**
     * 开放接口给子类修改滚动条大小
     *
     * @return
     */
    protected int getCustomMaxTextWidth() {
        return 0;
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        Resources res = context.getResources();
        mDefaultColorStyle = getContext().getResources().getColor(R.color.accent_color);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomNumSeekBar);
            int numBgTumbId = typedArray.getResourceId(R.styleable.CustomNumSeekBar_numBgThumb, View.NO_ID);
            int touchThumbId = typedArray.getResourceId(R.styleable.CustomNumSeekBar_touchThumb, View.NO_ID);
            int progressBgTumbId = typedArray.getResourceId(R.styleable.CustomNumSeekBar_progressBgDrawable, View.NO_ID);
            int progressTumbId = typedArray.getResourceId(R.styleable.CustomNumSeekBar_progressDrawable, View.NO_ID);
            mShowText = typedArray.getBoolean(R.styleable.CustomNumSeekBar_showNum, true);
            mShowTopText = typedArray.getBoolean(R.styleable.CustomNumSeekBar_showTopNum, false);

            mTextColor = typedArray.getColor(R.styleable.CustomNumSeekBar_numColor, Color.WHITE);
            mTextSize = typedArray.getDimensionPixelSize(R.styleable.CustomNumSeekBar_numSize, res.getDimensionPixelSize(R.dimen.image_edit_seekbar_num_size));
            mNumTopHeight = typedArray.getDimensionPixelSize(R.styleable.CustomNumSeekBar_numTopHeight, 0);

            mMax = typedArray.getInt(R.styleable.CustomNumSeekBar_max, 100);
            mProgress = typedArray.getInt(R.styleable.CustomNumSeekBar_progress, 0);

            if (numBgTumbId != View.NO_ID) {
                mNumBgTumb = res.getDrawable(numBgTumbId);
            } else {
                mNumBgTumb = new ColorDrawable(typedArray.getColor(R.styleable.CustomNumSeekBar_numBgThumb, Color.CYAN));
            }

            if (touchThumbId != View.NO_ID) {
                mTouchTumb = res.getDrawable(touchThumbId);
            } else {
                mTouchTumb = new ColorDrawable(typedArray.getColor(R.styleable.CustomNumSeekBar_touchThumb, Color.YELLOW));
            }

            if (progressBgTumbId != View.NO_ID) {
                mProgressBgTumb = res.getDrawable(progressBgTumbId);
            } else {
                mProgressBgTumb = new ColorDrawable(typedArray.getColor(R.styleable.CustomNumSeekBar_progressBgDrawable, DEFAULT_PROGRESS_BG_COLOR));
            }

            if (progressTumbId != View.NO_ID) {
                mProgressTumb = res.getDrawable(progressTumbId);
            } else {
                mProgressTumb = new ColorDrawable(typedArray.getColor(R.styleable.CustomNumSeekBar_progressDrawable, Color.BLUE));
            }
            typedArray.recycle();

            mNumBgTumb.setColorFilter(mDefaultColorStyle, PorterDuff.Mode.SRC_IN);
            mTouchTumb.setColorFilter(mDefaultColorStyle, PorterDuff.Mode.SRC_IN);
            mProgressTumb.setColorFilter(mDefaultColorStyle, PorterDuff.Mode.SRC_IN);
        } else {//deal
            mTextColor = Color.WHITE;
            mTextSize = res.getDimensionPixelSize(R.dimen.image_edit_seekbar_num_size);

            mMax = 100;
            mProgress = 0;

            mNumBgTumb = new ColorDrawable(Color.CYAN);

            mTouchTumb = new ColorDrawable(Color.YELLOW);

            mProgressBgTumb = new ColorDrawable(DEFAULT_PROGRESS_BG_COLOR);

            mProgressTumb = new ColorDrawable(Color.BLUE);

            mShowText = true;
        }
        mText = mProgress + "";
    }

    /**
     * 做初始化的动作
     */
    private void doInit() {
        if (!mIsInit) {
            mIsInit = true;
            mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
//            mTextPaint.setShadowLayer(getResources().getDimensionPixelSize(R.dimen.image_edit_seekbar_shadow_size), 0, 0,
//                    getResources().getColor(R.color.image_edit_seekbar_shadow_color));
            mTextPaint.setColor(mTextColor);
            mTextPaint.setTextSize(mTextSize);
            mTextPaint.setTypeface(FontUtil.CUSTOM_FONT);

            if (getCustomMaxTextWidth() != 0) {
                mTextMaxWidth = getCustomMaxTextWidth();
            } else if (mNumBgTumb instanceof BitmapDrawable) {
                mTextMaxWidth = ((BitmapDrawable) mNumBgTumb).getBitmap().getWidth();
            } else {
                mTextMaxWidth = getResources().getDimensionPixelSize(R.dimen.image_edit_seekbar_num_height);
            }
            mTextWidth = StaticLayout.getDesiredWidth(mText, mTextPaint);
            mTextLayout = new StaticLayout(mText, 0, mText.length(), mTextPaint, (int) (mTextWidth + 1), Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
                    false, TruncateAt.END, mTextMaxWidth);

            mViewRect = ViewUtil.INSTANCE.getViewRectF(this);
            mNumBgRect = new Rect();
            mTouchRect = new Rect();
            mProgressBgRect = new Rect();
            mProgressBgRectF = new RectF();
            mProgressRect = new Rect();

            if (mTouchTumb instanceof BitmapDrawable) {
                mTouchWidth = ((BitmapDrawable) mTouchTumb).getBitmap().getWidth();
            } else {
                mTouchWidth = getResources().getDimensionPixelSize(R.dimen.image_edit_seekbar_touch_height);
            }

            mProgressBgRect.set(mTextMaxWidth / 2, (int) ((mViewRect.height() - mProgressHeight) / 2),
                    (int) (mViewRect.width() - mTextMaxWidth / 2), (int) ((mViewRect.height() + mProgressHeight) / 2));
            mProgressBgRectF.set(mProgressBgRect);
            setInformation();
        }
    }

    /**
     * 重写onMeasure确保能完全显示 默认30dp
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        if (mNumBgTumb != null) {
            if (mNumBgTumb instanceof BitmapDrawable) {
                height = ((BitmapDrawable) mNumBgTumb).getBitmap().getWidth();
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                        MeasureSpec.EXACTLY);
            } else {
//                height = getResources().getDimensionPixelSize(R.dimen.image_edit_seekbar_num_height);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            doInit();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        if (this.mBackgroundGradient != null) {
            LinearGradient linearGradient = new LinearGradient(0, 0, mWidth, mHeight, this.mBackgroundGradient,
                    null, Shader.TileMode.CLAMP);
            if (mBgPaint == null) {
                mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mBgPaint.setStyle(Paint.Style.FILL);
            }
            mBgPaint.setShader(linearGradient);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsInit) {
            if (this.mBackgroundGradient != null) {
                canvas.drawRoundRect(mProgressBgRectF, 0, 0, mBgPaint);
            } else {
                mProgressBgTumb.setBounds(mProgressBgRect);
                mProgressBgTumb.draw(canvas);
                mProgressTumb.setBounds(mProgressRect);
                mProgressTumb.draw(canvas);
            }
            if (mIsTouch && mTouchTumb != null) {//按下
                mTouchTumb.setBounds(mTouchRect);
                mTouchTumb.draw(canvas);
            } else {
                if (mTextLayout != null) {
                    mNumBgTumb.setBounds(mNumBgRect);
                    mNumBgTumb.draw(canvas);
                    if (mShowText) {
                        int count = canvas.save();
                        canvas.translate(mTextLeft, mTextTop);
                        mTextLayout.draw(canvas);
                        canvas.restoreToCount(count);
                    }
                    if (mShowTopText) {
                        if (mNumTextPaint == null) {
                            mNumTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                            mNumTextPaint.setColor(mTextColor);
                            mNumTextPaint.setStyle(Paint.Style.FILL);
                            mNumTextPaint.setTextSize(mTextSize);
                        }
                        canvas.drawText(mText, mNumBgTumb.getBounds().left, mNumBgTumb.getBounds().centerY() - mNumTopHeight, mNumTextPaint);
                    }
                }
            }
        }
    }

    public void setProgressHeight(int height) {
        mProgressHeight = height;
    }

    public void setBackgroundGradientColor(int[] colors) {
        this.mBackgroundGradient = colors;
    }

    /**
     * 设置是否展示Text
     *
     * @param flag
     */
    public void setShowText(boolean flag) {
        mShowText = flag;
        invalidate();
    }

    /**
     * 设置文字
     *
     * @param str
     */
    public void setText(String str) {
        if (TextUtils.isEmpty(str)) {
            mText = "";
            mTextLayout = null;
        } else {
            mText = str;
            if (mIsInit) {
                mTextWidth = StaticLayout.getDesiredWidth(mText, mTextPaint);
                mTextLayout = new StaticLayout(mText, 0, mText.length(), mTextPaint, (int) (mTextWidth + 1), Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
                        false, TruncateAt.END, mTextMaxWidth);
            }
        }
        setInformation();
    }

    /**
     * 设置文字的大小
     *
     * @param px
     */
    public void setTextSize(int px) {
        mTextSize = px;
        if (mIsInit) {
            mTextPaint.setTextSize(px);
            mTextWidth = StaticLayout.getDesiredWidth(mText, mTextPaint);
            mTextLayout = new StaticLayout(mText, 0, mText.length(), mTextPaint, (int) (mTextWidth + 1), Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
                    false, TruncateAt.END, mTextMaxWidth);
            setInformation();
        }
    }

    /**
     * 设置文字的颜色
     *
     * @param color
     */
    public void setTextColor(int color) {
        mTextColor = color;
        if (mIsInit) {
            mTextPaint.setColor(color);
            mTextLayout = new StaticLayout(mText, 0, mText.length(), mTextPaint, (int) (mTextWidth + 1), Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
                    false, TruncateAt.END, mTextMaxWidth);
            invalidate();
        }
    }

    /**
     * 设置最大的进度值
     *
     * @param max
     */
    public void setMax(int max) {
        mMax = max;
        setInformation();
    }

    /**
     * 外部设置进度  回调时会返回isFromUser 为false
     *
     * @param progress
     */
    public void setProgress(int progress) {
        mProgress = Math.min(progress, mMax);
        mProgress = Math.max(0, mProgress);
        setText(mProgress + "");
        if (mListener != null) {
            mListener.onProgressChanged(this, mProgress, false);
        }
    }

    /**
     * touch的时候设置进度  回调时会返回isFromUser 为true
     *
     * @param progress
     */
    private void setProgressFromTouch(int progress) {
        mProgress = Math.min(progress, mMax);
        mProgress = Math.max(0, mProgress);
        setText(mProgress + "");
        if (mListener != null) {
            mListener.onProgressChanged(this, mProgress, true);
        }
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mListener = listener;
    }

    /**
     * 用于改变整体风格的颜色
     *
     * @param color
     */
    public void setColorStyle(int color) {
        mNumBgTumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        mTouchTumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        mProgressTumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        invalidate();
    }

    /**
     * 将整体风格恢复默认
     */
    public void setDefaultColorStyle() {
        mNumBgTumb.setColorFilter(mDefaultColorStyle, PorterDuff.Mode.SRC_IN);
        mTouchTumb.setColorFilter(mDefaultColorStyle, PorterDuff.Mode.SRC_IN);
        mProgressTumb.setColorFilter(mDefaultColorStyle, PorterDuff.Mode.SRC_IN);
        invalidate();
    }

    /**
     * 将整体风格恢复默认
     *
     * @param defaultColorStyle
     */
    public void setDefaultColorStyle(int defaultColorStyle) {
        mDefaultColorStyle = defaultColorStyle;
        mNumBgTumb.setColorFilter(mDefaultColorStyle, PorterDuff.Mode.SRC_IN);
        mTouchTumb.setColorFilter(mDefaultColorStyle, PorterDuff.Mode.SRC_IN);
        mProgressTumb.setColorFilter(mDefaultColorStyle, PorterDuff.Mode.SRC_IN);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mDownX = event.getX();
            if (mNumBgRect.contains((int) mDownX, (int) (event.getY()))) {//点在区域内
                mTouchType = TYPE_DOWN;
            } else {
                if (mListener != null) {
                    mListener.onStartTrackingTouch(this);
                }
                int progress = (int) (mDownX * 1.0f / mViewRect.width() * mMax + 0.5f);
                setProgressFromTouch(progress);
                mTouchType = TYPE_MOVE;
            }
            mIsTouch = true;
            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float fdownX = event.getX();
            if (mTouchType == TYPE_DOWN) {
                if (Math.abs(fdownX - mDownX) >= TouchUtil.OFFSET) {
                    if (mListener != null) {
                        mListener.onStartTrackingTouch(this);
                    }
                    int progress = (int) (fdownX * 1.0f / mViewRect.width() * mMax + 0.5f);
                    setProgressFromTouch(progress);
                    mTouchType = TYPE_MOVE;
                }
            } else if (mTouchType == TYPE_MOVE) {
                int progress = (int) (fdownX * 1.0f / mViewRect.width() * mMax + 0.5f);
                setProgressFromTouch(progress);
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            mIsTouch = false;
            mTouchType = TYPE_NONE;
            if (mListener != null) {
                mListener.onStopTrackingTouch(this);
            }
            invalidate();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 更新绘制的参数并刷新视图
     */
    private void setInformation() {
        if (!mIsInit) return;
        /**
         * mTextMaxWidth 实际上就是图片的宽----直径
         */
        float len = mViewRect.width() - mTextMaxWidth;

        float qlen = len * (mProgress * 1.0f / mMax);

        mTextLeft = mTextMaxWidth / 2 + qlen - (int) (mTextWidth + 1) / 2 + DeviceUtils.dip2px(getContext(), 0.5f);
        mTextTop = (mViewRect.height() - mTextLayout.getHeight()) / 2 + DeviceUtils.dip2px(getContext(), 0.5f);

        int left = Math.round(qlen);
        int bottom = Math.round(mViewRect.height());
        int yOffset = 0;
        if (mTextMaxWidth < bottom) {
            yOffset = (bottom - mTextMaxWidth) / 2;
        }
        mNumBgRect.set(left, yOffset, left + mTextMaxWidth, bottom - yOffset);
        mTouchRect.set(mTextMaxWidth / 2 + left - mTouchWidth / 2,
                (int) ((mViewRect.height() - mTouchWidth) / 2),
                (int) (mTextMaxWidth / 2 + left + mTouchWidth / 2),
                (int) ((mViewRect.height() + mTouchWidth) / 2));

        mProgressRect.set(mTextMaxWidth / 2, (int) ((mViewRect.height() - mProgressHeight) / 2),
                mTextMaxWidth / 2 + left, (int) ((mViewRect.height() + mProgressHeight) / 2));
        invalidate();
    }

    public int getProgress() {
        return mProgress;
    }

    public Drawable getNumBgTumb() {
        return mNumBgTumb;
    }

    /**
     * 设置显示数字的背景
     *
     * @param numBgTumb
     */
    public void setNumBgTumb(Drawable numBgTumb) {
        mNumBgTumb = numBgTumb;
        if (mNumBgTumb == null) {
            mNumBgTumb = new ColorDrawable(Color.CYAN);
        }
        if (mIsInit) {
            if (getCustomMaxTextWidth() != 0) {
                mTextMaxWidth = getCustomMaxTextWidth();
            } else if (mNumBgTumb instanceof BitmapDrawable) {
                mTextMaxWidth = ((BitmapDrawable) mNumBgTumb).getBitmap().getWidth();
            } else {
                mTextMaxWidth = getResources().getDimensionPixelSize(R.dimen.image_edit_seekbar_num_height);
            }
            mTextWidth = StaticLayout.getDesiredWidth(mText, mTextPaint);
            mTextLayout = new StaticLayout(mText, 0, mText.length(), mTextPaint, (int) (mTextWidth + 1), Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
                    false, TruncateAt.END, mTextMaxWidth);

            setInformation();
        }
    }

    public Drawable getTouchTumb() {
        return mTouchTumb;
    }

    /**
     * 设置按下去的按钮的drawable
     *
     * @param touchTumb
     */
    public void setTouchTumb(Drawable touchTumb) {
        mTouchTumb = touchTumb;
//        if (mTouchTumb == null) {
//            mTouchTumb = new ColorDrawable(Color.YELLOW);
//        }
        if (mIsInit) {
            if (mTouchTumb instanceof BitmapDrawable) {
                mTouchWidth = ((BitmapDrawable) mTouchTumb).getBitmap().getWidth();
            } else {
                mTouchWidth = getResources().getDimensionPixelSize(R.dimen.image_edit_seekbar_touch_height);
            }
            setInformation();
        }
    }

    public Drawable getProgressBgTumb() {
        return mProgressBgTumb;
    }

    /**
     * 设置进度的背景
     *
     * @param progressBgTumb
     */
    public void setProgressBgTumb(Drawable progressBgTumb) {
        mProgressBgTumb = progressBgTumb;
        if (mProgressBgTumb == null) {
            mProgressBgTumb = new ColorDrawable(Color.GRAY);
        }
        if (mIsInit) {
            mProgressBgRect.set(mTextMaxWidth / 2, (int) ((mViewRect.height() - mProgressHeight) / 2),
                    (int) (mViewRect.width() - mTextMaxWidth / 2), (int) ((mViewRect.height() + mProgressHeight) / 2));
            mProgressBgRectF.set(mProgressBgRect);
            setInformation();
        }
    }

    public Drawable getProgressTumb() {
        return mProgressTumb;
    }

    /**
     * 设置进度
     *
     * @param progressTumb
     */
    public void setProgressTumb(Drawable progressTumb) {
        mProgressTumb = progressTumb;
        if (mProgressTumb == null) {
            mProgressTumb = new ColorDrawable(Color.BLUE);
        }
        if (mIsInit) {
            setInformation();
        }
    }

    /**
     * 获取当前的最大进度值
     *
     * @return
     */
    public int getMax() {
        return mMax;
    }

}
