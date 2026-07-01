package com.allever.video.editor.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.android.absbase.utils.DeviceUtils;
import com.allever.video.editor.R;
import com.allever.video.editor.utils.ViewUtil;


public class CircleFillProgressView extends View {

    private static final String TAG = CircleFillProgressView.class.getSimpleName();

    private int mMaxProgress;

    private int mProgress;

    private int mCircleLineStrokeWidth;

    //View的范围
    private RectF mRectF;

    private RectF mViewTrueRectF;

    // 画圆所在的距形区域
    private RectF mOvalRectF;

    private Paint mPaint;

    private int mStrokeColor;

    private int mProgressColor;

    public static final int MODE_DEFAULT = 0;
    public static final int MODE_REVERSE = 1;
    private int mProgressMode = MODE_DEFAULT;

    public CircleFillProgressView(Context context) {
        this(context, null);
    }

    public CircleFillProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWithAttr(attrs);
    }

    private void initWithAttr(AttributeSet attrs){
        Resources res = getResources();
        if(attrs != null){
            TypedArray typedArray = res.obtainAttributes(attrs, R.styleable.CircleProgressView);
            mProgress = typedArray.getInt(R.styleable.CircleProgressView_curProgress, 0);
            mMaxProgress = typedArray.getInt(R.styleable.CircleProgressView_maxProgress, 100);
            int strokeWidthResId = typedArray.getResourceId(R.styleable.CircleProgressView_progressStrokeWidth, View.NO_ID);
            if(strokeWidthResId != View.NO_ID){
                mCircleLineStrokeWidth = res.getDimensionPixelSize(strokeWidthResId);
            } else{
                mCircleLineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CircleProgressView_progressStrokeWidth,
                        DeviceUtils.dip2px(getContext(), 0));
            }

            int strokeColorResId = typedArray.getResourceId(R.styleable.CircleProgressView_progressStrokeColor, View.NO_ID);
            if(strokeColorResId != View.NO_ID){
                mStrokeColor = res.getColor(strokeColorResId);
            } else{
                mStrokeColor = typedArray.getColor(R.styleable.CircleProgressView_progressStrokeColor, Color.WHITE);
            }

            int progressColorResId = typedArray.getResourceId(R.styleable.CircleProgressView_progressColor, View.NO_ID);
            if(progressColorResId != View.NO_ID){
                mProgressColor = res.getColor(progressColorResId);
            } else{
                mProgressColor = typedArray.getColor(R.styleable.CircleProgressView_progressColor, Color.GRAY);
            }
            mProgressMode = typedArray.getInt(R.styleable.CircleProgressView_progressMode, MODE_DEFAULT);
            typedArray.recycle();
        } else{
            mCircleLineStrokeWidth = DeviceUtils.dip2px(getContext(), 0);
            mMaxProgress = 100;
            mProgress = 0;
            mStrokeColor = Color.WHITE;
            mProgressColor = Color.GRAY;
        }
        mRectF = new RectF();
        mViewTrueRectF = new RectF();
        mOvalRectF = new RectF();
        mPaint = new Paint();
        // 设置画笔相关属性
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mCircleLineStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(changed){
            initOnlayout();
        }
    }

    private void initOnlayout(){
        RectF viewRect = ViewUtil.INSTANCE.getViewRectF(this);
        mRectF.set(viewRect);
        mViewTrueRectF.set(mRectF);
        mViewTrueRectF.offset(-mRectF.left, -mRectF.top);

        float width = mRectF.width() - getPaddingLeft() - getPaddingRight();
        float height = mRectF.height() - getPaddingTop() - getPaddingBottom();
        float newWidth;
        float newHeight;
        if(width > height){
            newWidth = height;
            newHeight = height;
        } else{
            newWidth = width;
            newHeight = width;
        }

        // 位置
        mOvalRectF.left = mCircleLineStrokeWidth / 2; // 左上角x
        mOvalRectF.top = mCircleLineStrokeWidth / 2; // 左上角y
        mOvalRectF.right = newWidth - mCircleLineStrokeWidth / 2; // 左下角x
        mOvalRectF.bottom = newHeight - mCircleLineStrokeWidth / 2; // 右下角y
        mOvalRectF.offset((width - newWidth) / 2, (height - newHeight) / 2);
        setProgress(mProgress);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = canvas.save();
        canvas.clipRect(getPaddingLeft(), getPaddingTop(), mViewTrueRectF.right - getPaddingRight(), mViewTrueRectF.bottom - getPaddingBottom());
        canvas.translate(getPaddingLeft(), getPaddingTop());
        // 绘制圆圈，进度条背景
        mPaint.setColor(mStrokeColor);
        canvas.drawOval(mOvalRectF, mPaint);
        mPaint.setColor(mProgressColor);
        if(mProgressMode == MODE_DEFAULT) {
            canvas.drawArc(mOvalRectF, -90, ((float) mProgress / mMaxProgress) * 360, false, mPaint);
        }else if(mProgressMode == MODE_REVERSE){
            canvas.drawArc(mOvalRectF, -90+((float)mProgress / mMaxProgress) * 360, ((float) (mMaxProgress-mProgress) / mMaxProgress) * 360, false, mPaint);
        }
        canvas.restoreToCount(count);
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        this.invalidate();
    }
}
