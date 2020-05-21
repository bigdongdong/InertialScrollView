package com.cxd.inertialscrollview_demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;
import android.widget.Toast;


@SuppressLint("DrawAllocation")
public class InertialScrollView extends View {
    private final String TAG = "InertialScrollView";
    private int mWidth , mHeight ;
    private double mAngle ; //手指旋转的角度
    private PointF mPivotPf = new PointF();//圆心点
    private PointF mStartPf = new PointF(); //起始落点
    private PointF mLastMovePf = new PointF(); //上一次移动点
    private PointF mCurMovePf = new PointF(); //当前移动点
    private PointF mRaiseupPf = new PointF(); //抬起点

    private VelocityTracker mVelocityTracker ;
    private Scroller mScroller ;
    private Context mContext ;

    public InertialScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mVelocityTracker = VelocityTracker.obtain();
        mScroller = new Scroller(mContext);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mWidth = mHeight = getMeasuredWidth();

        mPivotPf.x = mPivotPf.y = mWidth / 2 ;
    }




    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        mVelocityTracker.addMovement(ev);

        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mStartPf.x = ev.getX();
                mStartPf.y = ev.getY();
                mLastMovePf.x = mStartPf.x ;
                mLastMovePf.y = mStartPf.y ;
                mAngle = 0 ;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mCurMovePf.x = ev.getX();
                mCurMovePf.y = ev.getY();

                /*累加求旋转的角度*/
                mAngle += get2PointsAngle(mLastMovePf,mCurMovePf);

                mLastMovePf.x = mCurMovePf.x ;
                mLastMovePf.y = mCurMovePf.y ;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mRaiseupPf.x = ev.getX();
                mRaiseupPf.y = ev.getY();
                Toast.makeText(mContext, "角度："+mAngle,Toast.LENGTH_SHORT).show();

                mVelocityTracker.computeCurrentVelocity(1000);
                mScroller.fling(0, 0, (int) mVelocityTracker.getXVelocity(), (int) mVelocityTracker.getYVelocity(),
                        Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
                invalidate();

                break;
        }
        return true;

    }


    /**
     * 获取两个点（依据圆心）的旋转角度
     * @param p1 出发点
     * @param p2 停止点
     * @return
     */
    private double get2PointsAngle(PointF p1 , PointF p2){
        double cosC , angle ;
        double lineA2 =  (Math.pow(p1.x - mPivotPf.x,2) + Math.pow(p1.y - mPivotPf.y,2));
        double lineA =  Math.sqrt(lineA2);
        double lineB2 =   (Math.pow(p2.x - mPivotPf.x,2) + Math.pow(p2.y - mPivotPf.y,2));
        double lineB =   Math.sqrt(lineB2);
        double lineCC =   (Math.pow(p2.x - p1.x,2) + Math.pow(p2.y - p1.y,2));

        /*cosC = (a² + b² - c²) / 2ab*/
        cosC = (lineA2 + lineB2 - lineCC)/(2 * lineA * lineB);

        /*避免NaN，Math.acos参数必须在[-1,1]内*/
        cosC = Math.max(-1,cosC) ;
        cosC = Math.min(1,cosC) ;
        angle = Math.acos(cosC) ; //旋转的角度

        /*根据两个方向上的移动分量，
        将移动拆分成水平移动或者垂直移动*/
        boolean isHorizontal;
        float dx , dy ;
        int direction ;

        dx = Math.abs(p2.x - p1.x); //水平分量
        dy = Math.abs(p2.y - p1.y) ;//垂直分量

        isHorizontal = dx > dy ;

        if(isHorizontal){
            if(p1.y < (float)mHeight / 2){
                direction = (p2.x > p1.x) ? 1:-1 ;
            }else{
                direction = (p2.x > p1.x) ? -1:1 ;
            }
        }else {
            if(p1.x > (float)mWidth / 2){
                direction = (p2.y < p1.y) ? -1:1 ;
            }else{
                direction = (p2.y < p1.y) ? 1:-1 ;
            }
        }

        return Math.toDegrees(angle) * direction;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();
        p.setStrokeWidth(10);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.BLUE);

        canvas.drawCircle(mWidth/2,mHeight/2,mWidth/2 - 10 ,p);

        Paint centerPaint = new Paint();
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(Color.RED);
        canvas.drawCircle(mWidth/2,mHeight/2,15,centerPaint);

        Paint redLinePaint = new Paint();
        redLinePaint.setStrokeWidth(10);
        redLinePaint.setAntiAlias(true);
        redLinePaint.setStyle(Paint.Style.STROKE);
        redLinePaint.setColor(Color.RED);

        /*黄点*/
        Paint yellowPaint = new Paint();
        yellowPaint.setAntiAlias(true);
        yellowPaint.setStyle(Paint.Style.FILL);
        yellowPaint.setColor(Color.YELLOW);
        if(mStartPf.x != 0 || mStartPf.y != 0){
            Path temp = new Path();
            temp.moveTo(mStartPf.x,mStartPf.y);
            temp.lineTo(mWidth/2,mHeight/2);
            canvas.drawPath(temp,redLinePaint); //圆心到起点（红线）

            /*两条线*/
            if(mCurMovePf.x != 0 || mCurMovePf.y != 0){
                temp = new Path();
                temp.moveTo(mCurMovePf.x,mCurMovePf.y);
                temp.lineTo(mWidth/2,mHeight/2);
                canvas.drawPath(temp,redLinePaint); //圆心到当前点（红线）

                Paint greenLinePaint = new Paint();
                greenLinePaint.setAntiAlias(true);
                greenLinePaint.setStrokeWidth(10);
                greenLinePaint.setStyle(Paint.Style.STROKE);
                greenLinePaint.setColor(Color.GREEN);
                temp = new Path();
                temp.moveTo(mStartPf.x,mStartPf.y);
                temp.lineTo(mCurMovePf.x,mCurMovePf.y);
                canvas.drawPath(temp,greenLinePaint);  //起点当前点(绿线)
            }

            /*绘制起点和当前点*/
            canvas.drawCircle(mStartPf.x,mStartPf.y,15,yellowPaint);
            if(mCurMovePf.x != 0 || mCurMovePf.y != 0){
                canvas.drawCircle(mCurMovePf.x,mCurMovePf.y,15,yellowPaint); //当前点
            }

        }


    }
}
