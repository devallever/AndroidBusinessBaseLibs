package app.allever.android.sample.demo.hen.coder.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import app.allever.android.lib.core.ext.LoggerKt;
import app.allever.android.lib.core.helper.DisplayHelper;
import app.allever.android.sample.demo.hen.coder.R;

public class MyCustomView extends View {
    private float mMidWidth;
    private float mEndWidth;
    private Paint mPaint;

    private static final int sMargin = 20;
    private Rect mRect;
    private RectF mRectF;
    private RectF mOvalRect;
    private RectF mRoundRectF;

    private Path mPath;


    public MyCustomView(Context context) {
        super(context);
        init();
    }

    public MyCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint = new Paint();
        //抗锯齿参数方式创建
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //抗锯齿
        mPaint.setAntiAlias(true);
        //FILL->实心, STROKE->空心, FILL_AND_STROKE->????
        mPaint.setStyle(Paint.Style.FILL);
        //边框, 在内容外部区域绘制
        mPaint.setStrokeWidth(10);
        mPaint.setColor(getContext().getResources().getColor(R.color.hc_colorPrimary));

        mRect = new Rect(
                100 + sMargin, sMargin, 100 + sMargin + 200, sMargin + 100
        );
        mRectF =  new RectF(
                100 + sMargin, sMargin, 100 + sMargin + 200, sMargin + 100
        );
        mOvalRect = new RectF(450 + sMargin * 3 + sMargin, sMargin, 450 + sMargin * 3 + sMargin + 150, sMargin + 100);

        mRoundRectF = new RectF(
                sMargin, 250 + sMargin * 3, sMargin + 200, 250 + 100 + sMargin * 3
        );

        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mEndWidth =MeasureSpec.getSize(widthMeasureSpec);
        mMidWidth = mEndWidth / 2;

        log("中点横坐标 = " + mMidWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        log("onDraw");

        drawCircle(canvas);

        drawColor(canvas);

        drawRect(canvas);

        drawPoint(canvas);

        drawOval(canvas);

        drawLine(canvas);

        drawRoundRect(canvas);

//        drawArc(canvas);

        drawPath(canvas);

        drawBitmap(canvas);

        drawText(canvas);

    }

    private void drawPath(Canvas canvas) {
        mPath.reset();
        int top = 250 + sMargin * 3;
        mPath.moveTo(500 + sMargin * 5 + 50, top);
        mPath.lineTo(500 + sMargin * 5, top + 100);
        mPath.lineTo(500 + sMargin * 5 + 100, top + 100);
        mPath.close();
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mPath, mPaint);
    }

    private void drawCircle(Canvas canvas) {
        float margin = 20;

        RadialGradient radialGradient = new RadialGradient(50f, 50f + margin, 50f, Color.parseColor("#ff0000"), Color.parseColor("#000000"), Shader.TileMode.CLAMP);
//        SweepGradient sweepGradient = new SweepGradient(50f, 50f + margin, Color.parseColor("#ff0000"), Color.parseColor("#000000"));
        //绘制圆形
        mPaint.setShader(radialGradient);

//        canvas.drawCircle(50f, 50f + margin, 50f, mPaint);

        // Direction 是画圆的路径的方向。
        //路径方向有两种：顺时针 (CW clockwise) 和逆时针 (CCW counter-clockwise) 。
        // 对于普通情况，这个参数填 CW 还是填 CCW 没有影响。
        // 它只是在需要填充图形 (Paint.Style 为 FILL 或 FILL_AND_STROKE) ，并且图形出现自相交时，用于判断填充范围的。
        mPath.reset();
        mPath.addCircle(50f, 50f + margin, 50f, Path.Direction.CW);
        canvas.drawPath(mPath, mPaint);
        mPaint.setShader(null);
    }

    private void drawColor(Canvas canvas) {
        //绘制颜色,这类颜色填充方法一般用于在绘制之前设置底色，或者在绘制之后为界面设置半透明蒙版。
//        canvas.drawColor(getContext().getResources().getColor(R.color.colorAccent));//将整个canvas变色
//        canvas.drawColor(Color.parseColor("#88000000"));
//        canvas.drawRGB();
//        canvas.drawARGB();
    }

    private void drawRect(Canvas canvas) {
        //绘制矩形
        LinearGradient linearGradient = new LinearGradient(
                100 + sMargin, sMargin + 0,
                100 + sMargin + 200, sMargin + 100,
                Color.parseColor("#ff0000"), Color.parseColor("#000000"), Shader.TileMode.CLAMP
        );
        mPaint.setShader(linearGradient);
        canvas.drawRect(100 + sMargin, sMargin, 100 + sMargin + 200, sMargin + 100, mPaint);
//        mRect = new Rect(100 + sMargin, sMargin, 100 + sMargin + 200, sMargin + 100);
//        canvas.drawRect(mRect,mPaint);
//        mRectF = new Rect(100 + sMargin, sMargin, 100 + sMargin + 200, sMargin + 100);
//        canvas.drawRect(mRectF, mPaint);
        mPaint.setShader(null);
    }

    private void drawPoint(Canvas canvas) {
        //绘制点
        mPaint.setStrokeWidth(20);
        //不是专门用来设置点的形状的，而是一个设置线条端点形状的方法
        mPaint.setStrokeCap(Paint.Cap.BUTT); //设置点形状, SQUARE->方头，ROUND->圆头， BUTT->平头
//        canvas.drawPoint(370 + sMargin, 50 + sMargin, mPaint);
        //绘制多个点
//        float[] points = {370 + sMargin, 50 + sMargin, 370 + sMargin + 50, 50 + sMargin, 370 + sMargin + 100, 50 + sMargin};
        float[] points = {0, 0, 370 + sMargin, 50 + sMargin, 370 + sMargin + 50, 50 + sMargin, 370 + sMargin + 100, 50 + sMargin};
        canvas.drawPoints(points, 2, 6, mPaint);//跳过前面两个数(0,0), 用后面六个数画点

    }

    private void drawOval(Canvas canvas) {
        SweepGradient sweepGradient = new SweepGradient(450 + sMargin * 3 + sMargin + 75, sMargin + 50, Color.parseColor("#ff0000"), Color.parseColor("#000000"));
        mPaint.setShader(sweepGradient);
        //画椭圆
        mPaint.setStrokeWidth(0);
        //高版本api 21
//        canvas.drawOval(450 + sMargin * 3 + sMargin, sMargin, 450 + sMargin * 3 + sMargin + 150, sMargin + 100, mPaint);
        canvas.drawOval(mOvalRect, mPaint);
        mPaint.setShader(null);
    }

    private void drawLine(Canvas canvas) {
        //画线
        mPaint.setStrokeWidth(10);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
//        canvas.drawLine(sMargin, 100 + sMargin * 2, mEndWidth - sMargin, 100 + sMargin * 2, mPaint);
        //绘制多条线, 和绘制多个点方法类似
//        float[] lines = {
//                sMargin, 150 + sMargin * 2, mEndWidth - sMargin, 150 + sMargin * 2,
//                sMargin, 200 + sMargin * 3, mEndWidth - sMargin, 200 + sMargin * 3
//        };
//        canvas.drawLines(lines, mPaint);
        float[] lines = {
                0,0,0,0,
                sMargin, 150 + sMargin * 2, mEndWidth - sMargin, 150 + sMargin * 2,
                sMargin, 200 + sMargin * 3, mEndWidth - sMargin, 200 + sMargin * 3
        };
        canvas.drawLines(lines, 4, 8,  mPaint);
    }

    private void drawRoundRect(Canvas canvas) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hc_logo);
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint.setShader(bitmapShader);
        //绘制圆角矩形
        mPaint.setStrokeWidth(0);
        //高版本api 21
//        canvas.drawRoundRect(
//                sMargin, 250 + sMargin * 3, sMargin + 200, 250 + 100 + sMargin * 3, 10, 10, mPaint
//        );
        //rx->圆角横向半径， ry->圆角纵向半径, 随便一个填0， 四个角都看不到圆角效果, 可能理解错了
        int radius = 10;
        canvas.drawRoundRect(mRoundRectF, radius, radius, mPaint);
        mPaint.setShader(null);
    }

    //绘制扇形
    //绘制扇形弧线相关
    //drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) 绘制弧形或扇形
    //drawArc() 是使用一个椭圆来描述弧形的。left, top, right, bottom 描述的是这个弧形所在的椭圆；
    // startAngle 是弧形的起始角度（x 轴的正向，即正右的方向，是 0 度的位置；顺时针为正角度，逆时针为负角度），
    //以椭圆中心建立坐标轴，中心横坐标为旋转角0度开始，顺时针为正，逆时针为负
    // sweepAngle 是弧形划过的角度；
    // useCenter 表示是否连接到圆心，如果不连接到圆心，就是弧形，如果连接到圆心，就是扇形。
    private void drawArc(Canvas canvas) {
        int top = 250 + sMargin * 3;
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0);
        canvas.drawArc(200 + sMargin * 2, top, 200 + 100 + sMargin * 2, top + 100, -90, 90, true, mPaint);
        //绘制空心扇形
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        canvas.drawArc(300 + sMargin * 3, top, 300 + 100 + sMargin * 3, top + 100, -90, 90, true, mPaint);
        //绘制弧线
//        canvas.drawArc(400 + sMargin * 4, top, 400 + 100 + sMargin * 4, top + 100, -90, 90, false, mPaint);
        //使用path绘制弧形
        mPath.reset();
        // forceMoveTo 参数的意思是，绘制是要「抬一下笔移动过去」，还是「直接拖着笔过去」，区别在于是否留下移动的痕迹。
        //true表示无拖动痕迹，即抬起笔移动过去
        mPath.arcTo(400 + sMargin * 4, top, 400 + 100 + sMargin * 4, top + 100, -90, 90, false);
        canvas.drawPath(mPath, mPaint);

    }

    private void drawBitmap(Canvas canvas) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hc_logo);
        RectF rectF = new RectF(
                0, 400 + sMargin * 3, 540, 400 + sMargin * 3 + 258
        );
//        canvas.drawBitmap(bitmap, 0, 400 + sMargin * 3, mPaint);
        canvas.drawBitmap(bitmap, null , rectF, mPaint);
    }

    private void drawText(Canvas canvas) {
        mPaint.setTextSize(DisplayHelper.INSTANCE.dip2px(20));
        mPaint.setColor(Color.parseColor("#ff0000"));
        canvas.drawText("HelloWorld, 你好，Android", sMargin, 800, mPaint);
//        canvas.drawText("HelloWorld, 你好，Android", 0, 0, mPaint);
    }

    private void log(String msg) {
        LoggerKt.log(msg);
    }
}
