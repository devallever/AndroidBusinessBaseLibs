package com.allever.video.editor.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.allever.video.editor.R;


public class SettingCheck extends View {

    private Drawable mCheckBgDrawable;
    private Drawable mUnCheckBgDrawable;
//    private Drawable mCheckDrawable;
//    private Drawable mUnCheckDrawable;
    private Context mContext;

    private boolean mIsCheck = true;

    private int mWidth;
    private int mHeight;

    public SettingCheck(Context context) {
        super(context);
        init(context, null);
    }

    public SettingCheck(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SettingCheck(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SettingCheck(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mCheckBgDrawable = mContext.getResources().getDrawable(R.drawable.icon_switch_on);
        mUnCheckBgDrawable = mContext.getResources().getDrawable(R.drawable.icon_switch_off);
//        mCheckDrawable = mContext.getResources().getDrawable(R.drawable.setting_icon_switch_on);
//        mUnCheckDrawable = mContext.getResources().getDrawable(R.drawable.setting_icon_switch_off);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int maxWidth = Math.max(mCheckBgDrawable.getIntrinsicWidth(), mCheckDrawable.getIntrinsicWidth());
//        int maxHeight = Math.max(mCheckBgDrawable.getIntrinsicHeight(), mCheckDrawable.getIntrinsicHeight());
        int maxWidth = mCheckBgDrawable.getIntrinsicWidth();
        int maxHeight = mCheckBgDrawable.getIntrinsicHeight();
        int desiredWidth = getPaddingLeft() + getPaddingRight() + maxWidth;
        int desiredHeight = getPaddingBottom() + getPaddingTop() + maxHeight;
        int widthSpec = resolveSizeAndState(desiredWidth, widthMeasureSpec);
        int heightSpec = resolveSizeAndState(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(widthSpec, heightSpec);
        mWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        mHeight = getMeasuredHeight() - getPaddingBottom() - getPaddingTop();
        initBgDrawable(mCheckBgDrawable);
        initBgDrawable(mUnCheckBgDrawable);
//        initButtonDrawable(mCheckDrawable, true);
//        initButtonDrawable(mUnCheckDrawable, false);
    }

    private void initButtonDrawable(Drawable checkBgDrawable, boolean isRight) {
        if (isRight) {
            final int drawableHeight = checkBgDrawable.getIntrinsicHeight();
            final int drawableWidth = checkBgDrawable.getIntrinsicWidth();
            final int right = mWidth - getPaddingRight();
            final int left = right - drawableWidth;
            final int top = getPaddingTop();
            final int bottom = top + drawableHeight;
            checkBgDrawable.setBounds(left, top, right, bottom);
        } else {
            final int drawableHeight = checkBgDrawable.getIntrinsicHeight();
            final int drawableWidth = checkBgDrawable.getIntrinsicWidth();
            final int left = 0;
            final int right = left + drawableWidth;
            final int top = getPaddingTop();
            final int bottom = top + drawableHeight;
            checkBgDrawable.setBounds(left, top, right, bottom);
        }

    }

    private void initBgDrawable(Drawable checkBgDrawable) {
        //居中
        final int drawableHeight = checkBgDrawable.getIntrinsicHeight();
        final int drawableWidth = checkBgDrawable.getIntrinsicWidth();
        final int left = (mWidth - drawableWidth) / 2;
        final int right = left + drawableWidth;
        final int top = (mHeight - drawableHeight) / 2;
        final int bottom = top + drawableHeight;
        checkBgDrawable.setBounds(left, top, right, bottom);
    }

    public static int resolveSizeAndState(int desireSize, int measureSpec) {
        int result = desireSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = desireSize;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < desireSize) {
                    result = specSize;
                } else {
                    result = desireSize;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        if (mIsCheck) {
            mCheckBgDrawable.draw(canvas);
//            mCheckDrawable.draw(canvas);
        } else {
            mUnCheckBgDrawable.draw(canvas);
//            mUnCheckDrawable.draw(canvas);
        }
        canvas.restore();

    }

    public void setCheck(boolean check) {
        if (mIsCheck != check) {
            mIsCheck = check;
            invalidate();
        }
    }

    public boolean isCheck() {
        return mIsCheck;
    }

}
