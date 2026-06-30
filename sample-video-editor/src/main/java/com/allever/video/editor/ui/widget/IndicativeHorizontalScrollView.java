package com.allever.video.editor.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;


/**
 * 带有左右滑动指示器的水平滚动控件
 */
public class IndicativeHorizontalScrollView extends HorizontalScrollView {
    private Bitmap mLeftIndicator;
    private Bitmap mRightIndicator;

    /**
     * 不透明画笔
     */
    private Paint mPaint1;

    /**
     * 半透明画笔
     */
    private Paint mPaint2;

    private long mLastTime;

    private int mLastScrollX = 0;

    /**
     * 动画间隔
     */
    private final int DURATION = 500;

    private boolean mScrollable = true;

    public IndicativeHorizontalScrollView(Context context) {
        super(context);
        initilize();
    }

    public IndicativeHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initilize();
    }

    public IndicativeHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initilize();
    }


    private void initilize() {
        mLastTime = System.currentTimeMillis();

        mPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setAlpha(127);
    }


    /**
     * 通过资源文件ID来获取图片内容
     *
     * @param id      资源文件ID
     * @param context 上下文对象
     * @return 位图对象，如果读取失败将返回null
     */
    public static Bitmap readImageFileFromResource(int id, Context context) {
        if (id == 0) {
            return null;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
            if (bitmap == null) {
            }
            return bitmap;
        } catch (OutOfMemoryError er) {
        } catch (Exception e) {
        }

        return null;
    }


    public void setScrollable(boolean scrollable) {
        mScrollable = scrollable;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mScrollable) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.dispatchDraw(canvas);

        View childView = getChildAt(0);
        if (childView != null && childView.getWidth() > getWidth()) {
            int scrollX = getScrollX();
            if (scrollX != 0 && mLeftIndicator != null) {
                int left = scrollX + 2;
                int top = (getHeight() - mLeftIndicator.getHeight()) / 2;
                long time = System.currentTimeMillis();
                if (time - mLastTime > DURATION) {
                    if (time - mLastTime > 2 * DURATION) {
                        mLastTime = time;
                    }
                    canvas.drawBitmap(mLeftIndicator, left, top, mPaint2);
                } else {
                    canvas.drawBitmap(mLeftIndicator, left, top, mPaint1);
                }
            }

            int visualRegionX = getWidth() - getPaddingLeft() - getPaddingRight();
            int maxScrollX = childView.getWidth() - visualRegionX;
            if (scrollX != maxScrollX & mRightIndicator != null) {
                int left = getWidth() - mRightIndicator.getWidth() + scrollX - 2;
                int top = (getHeight() - mRightIndicator.getHeight()) / 2;
                long time = System.currentTimeMillis();
                if (time - mLastTime > DURATION) {
                    if (time - mLastTime > 2 * DURATION) {
                        mLastTime = time;
                    }
                    canvas.drawBitmap(mRightIndicator, left, top, mPaint2);
                } else {
                    canvas.drawBitmap(mRightIndicator, left, top, mPaint1);
                }
            }
            if (scrollX != mLastScrollX) {
                if (mOnScrollXListener != null) {
                    mOnScrollXListener.onScrollX(scrollX, scrollX - mLastScrollX >= 0);
                }
                mLastScrollX = scrollX;
            }
            if (mLeftIndicator != null && mRightIndicator != null) {
                postInvalidate();
            }
        }
    }

    private OnScrollXListener mOnScrollXListener;

    public void setOnScrollXListener(OnScrollXListener onScrollXListener) {
        mOnScrollXListener = onScrollXListener;
    }

    public interface OnScrollXListener {
        /**
         * @param scrollX 左边缘到原来的距离
         * @param toLeft
         */
        void onScrollX(int scrollX, boolean toLeft);
    }

}
