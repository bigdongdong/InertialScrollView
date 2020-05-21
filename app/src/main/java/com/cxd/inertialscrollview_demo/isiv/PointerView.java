package com.cxd.inertialscrollview_demo.isiv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class PointerView extends View {
    private int mWidth , mHeight;
    private Paint mPaint ;
    private RectF mRectf ;
    public PointerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        mRectf = new RectF(0 ,mHeight-mWidth,mWidth,mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*绘制一个指针*/
        Path p = new Path();
        p.moveTo(mWidth/2,0);
        p.lineTo(0,mHeight-mWidth/2);
        p.arcTo(mRectf,180,-180);
        p.close();
        canvas.drawPath(p,mPaint);
    }
}
