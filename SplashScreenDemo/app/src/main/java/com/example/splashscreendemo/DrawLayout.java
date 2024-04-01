package com.example.splashscreendemo;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class DrawLayout extends LinearLayout {
    public  static boolean sTransparent = false;
    private Paint mPaint;
    /**
     * 面罩 使用它后会出现一个面具在目标的边缘的指定范围，该面具的边缘是否会被包进目标中，
     * 或者是在目标里边，外边，里边都有，这是由BlurMaskFilter.Blur这个枚举所决定的。
     */
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private float mX, mY;
    private Canvas mCanvas;
    private Path mPath;
    private static final float TOUCH_TOLERANCE = 4;
    public DrawLayout(Context context) {
        super(context);
        init();
        setOrientation(LinearLayout.VERTICAL);// 水平排列
        setWillNotDraw(false);
        //设置宽高
        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        View view = LayoutInflater.from(context).inflate(
                R.layout.draw, null);
        this.addView(view);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        if (mPath == null) {
            mPath = new Path();
        }
        if (!sTransparent) {
            mCanvas.drawPath(mPath, mPaint);
        }
    }
    public void setPath(Path path) {
        mPath = path;
    }
    public Path getPath() {
        return mPath;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.d("lsm1","onTouchEvent event = " + event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                break;
        }
        invalidate();
        return true;
    }
    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            /**
             * quadTo方法的实现是当我们不仅仅是画一条线甚至是画弧线时会形成平滑的曲线，
             * 该曲线又称为"贝塞尔曲线"(Beziercurve) 其中，x1，y1为控制点的坐标值，x2，y2为终点的坐标值；
             */
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // 连接绘制
        if(!sTransparent)
            mCanvas.drawPath(mPath, mPaint);
        // 清除path设置的所有属性
      //  mPath.reset();
    }
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);// 抗锯齿
        mPaint.setDither(true); // 防抖动
        mPaint.setColor(0xFFFF0000);// 设置颜色
        mPaint.setStyle(Paint.Style.STROKE);// 画笔类型 STROKE空心 FILL 实心
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 画笔接洽点类型 如影响矩形但角的外轮廓,让画的线圆滑
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 画笔笔刷类型 如影响画笔但始末端
        mPaint.setStrokeWidth(12);// 设置线宽
        // 光源的方向和环境光强度来添加浮雕效果
        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
        // 前面一个控制阴影的宽度，后面一个参数控制阴影效果
        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
    }
}