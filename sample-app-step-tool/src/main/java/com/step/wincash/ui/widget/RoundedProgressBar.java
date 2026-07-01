package com.step.wincash.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class RoundedProgressBar extends ProgressBar {
    private final Paint backgroundPaint;
    private final Paint progressPaint;
    private float radius;

    public RoundedProgressBar(Context context) {
        this(context, null);
    }

    public RoundedProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        backgroundPaint = new Paint();
        progressPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#FFE6EC"));
        progressPaint.setColor(Color.parseColor("#FE3665"));
        radius = 200;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        // 绘制背景
        RectF backgroundRect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(backgroundRect, radius, radius, backgroundPaint);
        // 绘制进度条
        RectF progressRect = new RectF(0, 0, getWidth() * getProgress() / getMax(), getHeight());
        canvas.drawRoundRect(progressRect, radius, radius, progressPaint);
    }

    public void setProgressColor(String progressPaintColor, String backgroundPaintColor) {
        backgroundPaint.setColor(Color.parseColor(backgroundPaintColor));
        progressPaint.setColor(Color.parseColor(progressPaintColor));
        invalidate();
    }
}