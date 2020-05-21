package com.cxd.inertialscrollview_demo.isiv;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.cxd.inertialscrollview_demo.InertialScrollView;

import java.util.List;


@SuppressLint("DrawAllocation")
public class InertialScrollImageView extends android.support.v7.widget.AppCompatImageView {
    private final String TAG = "InertialScrollView";
    private int mWidth , mHeight ;
    private double mAngle ; //手指旋转的角度
    private PointF mPivotPf = new PointF();//圆心点
    private PointF mStartPf = new PointF(); //起始落点
    private PointF mLastMovePf = new PointF(); //上一次移动点
    private PointF mCurMovePf = new PointF(); //当前移动点
    private PointF mRaiseupPf = new PointF(); //抬起点

    private VelocityTracker mVelocityTracker ;
    private Context mContext ;
    private Matrix mMatrix ;
    private List<ItemBean> mItems ;
    private Bitmap mBitmap ;
    private Canvas mCanvas ;
    private Paint mLinePaint ;
    private Paint mTextPaint ;
    private ObjectAnimator mAnimator ;
    private boolean isFirstDraw = true ;


    public InertialScrollImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.setScaleType(ScaleType.MATRIX);
        this.setBackgroundColor(Color.TRANSPARENT);

        mContext = context;
        mVelocityTracker = VelocityTracker.obtain();
        mMatrix = new Matrix() ;
        mMatrix.set(this.getMatrix());

        /*文字画笔*/
        mTextPaint = new Paint();
        mTextPaint.setTextSize(sp2px(15));
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setAntiAlias(true);

        /*边框画笔*/
        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(Color.GRAY);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = mHeight = getMeasuredWidth();
        mPivotPf.x = mPivotPf.y = mWidth / 2 ;

        if(mBitmap == null){
            mBitmap = Bitmap.createBitmap(mWidth,mHeight, Bitmap.Config.ARGB_4444);
            mCanvas = new Canvas(mBitmap);
            Path p = new Path();
            p.addCircle(mWidth/2,mHeight/2,mWidth/2, Path.Direction.CW);
            mCanvas.clipPath(p);
        }

        /*绘制圆盘*/
        Shader bgShader = null;
        Paint bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);

        if(mItems != null && isFirstDraw){
            /*再封装mItems*/
            final int r = mWidth / 2 ; //半径
            for (int i = 0; i < mItems.size(); i++) {
                final ItemBean ib = mItems.get(i);
                /*背景贝塞尔参数*/
                PointF startPf = new PointF(mWidth/2,0);
                PointF endPf = calcNewPoint(startPf,ib.getAngle());
                PointF bCenterPf = calcNewPoint(startPf,ib.getAngle()/2); //弧中间点
                float bl = (float) (r / Math.cos((ib.getAngle()/2 * Math.PI) / 180)); //贝塞尔b点到圆心长度
                float bx = (bCenterPf.x - r) * bl /r + r;
                float by = (bCenterPf.y - r) * bl /r + r;
                PointF vertexPf = new PointF(bx,by);
                ib.setBgBessel(new BesselBean(vertexPf,startPf,endPf));
                /*文字贝塞尔参数*/
                startPf = new PointF(mWidth/2 ,mWidth /6);
                endPf = calcNewPoint(startPf,ib.getAngle());
                bCenterPf = calcNewPoint(startPf,ib.getAngle()/2); //弧中间点
                bl = (float) (r / Math.cos((ib.getAngle()/2 * Math.PI) / 180)); //贝塞尔b点到圆心长度
                bx = (bCenterPf.x - r) * bl /r + r;
                by = (bCenterPf.y - r) * bl /r + r;
                vertexPf = new PointF(bx,by);
                ib.setTextBessel(new BesselBean(vertexPf,startPf,endPf));
            }

            /*对封装后的mItems进行绘制*/
            for (int i = 0; i < mItems.size(); i++) {
                final ItemBean ib = mItems.get(i);
                BesselBean b ;
                Path p ;

                b = ib.getBgBessel();
                p = new Path();
                bgShader = new RadialGradient(mWidth / 2 , mHeight / 2 , mWidth / 2,ib.getColors(), null,Shader.TileMode.REPEAT);
                bgPaint.setShader(bgShader);

                /*扇形path*/
                p.moveTo(mWidth/2,mHeight/2);
                p.lineTo(b.getStartPf().x,b.getStartPf().y);
                p.quadTo(b.getVertexPf().x,b.getVertexPf().y,b.getEndPf().x,b.getEndPf().y);
                p.close();
                /*绘制渐变背景*/
                mCanvas.save();
                mCanvas.clipPath(p);
                mCanvas.drawPath(p,bgPaint);
                mCanvas.restore();
//                /*绘制扇形边框*/
                mCanvas.drawPath(p,mLinePaint);
//                mCanvas.drawPoints(new float[]{b.getStartPf().x,b.getStartPf().y,b.getVertexPf().x,b.getVertexPf().y,b.getEndPf().x,b.getEndPf().y},mLinePaint);
                /*绘制文字*/
                if(ib.getText() != null){
                    b = ib.getTextBessel();
                    p = new Path();
                    p.moveTo(b.getStartPf().x,b.getStartPf().y);
                    p.quadTo(b.getVertexPf().x,b.getVertexPf().y,b.getEndPf().x,b.getEndPf().y);
                    mCanvas.drawTextOnPath(ib.getText(),p,0,sp2px(15)/2,mTextPaint);
                }
//                mCanvas.drawPath(p,mLinePaint);
//                mCanvas.drawPoints(new float[]{b.getStartPf().x,b.getStartPf().y,b.getVertexPf().x,b.getVertexPf().y,b.getEndPf().x,b.getEndPf().y},mLinePaint);
                mCanvas.rotate(ib.getAngle(),mWidth/2,mHeight/2);
            }
            this.setImageBitmap(mBitmap);
            isFirstDraw = false ;
        }
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
                if(mAnimator != null && mAnimator.isRunning()){
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mCurMovePf.x = ev.getX();
                mCurMovePf.y = ev.getY();
                /*累加求旋转的角度*/
                mAngle += get2PointsAngle(mLastMovePf,mCurMovePf);
                /*滑动对应角度*/
                double curAngle = get2PointsAngle(mLastMovePf,mCurMovePf) ;
                mMatrix.postRotate((float) curAngle,mWidth/2,mHeight/2);
                setImageMatrix(mMatrix);

                mLastMovePf.x = mCurMovePf.x ;
                mLastMovePf.y = mCurMovePf.y ;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mRaiseupPf.x = ev.getX();
                mRaiseupPf.y = ev.getY();

                mVelocityTracker.computeCurrentVelocity(1000);
                /*先求出切线方向速度*/
                double angle = Math.abs(get2PointsAngle(new PointF(mWidth/2,0),mRaiseupPf));
                double vTangent = Math.abs(mVelocityTracker.getXVelocity() * Math.cos(angle)
                        + mVelocityTracker.getYVelocity()*Math.sin(angle));
                inertialScroll(vTangent);
                break;
        }
        return true;

    }

    /**
     * 惯性滑动
     * @param v
     */
    private void inertialScroll(final double v){
        /*将速度v转换成需要转动的角度*/
        float angle = (float) (v / 10);
        /*判断顺时针还是逆时针*/
        int direction = mAngle > 0 ? 1 : -1 ;
        mAnimator = ObjectAnimator.ofFloat(this, "rotation",0,angle * direction);
        mAnimator.setDuration(2000);
        mAnimator.setInterpolator(new DecelerateInterpolator(2));
        mAnimator.start();
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Toast.makeText(mContext,"done",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Toast.makeText(mContext,"done",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
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

    /**
     * 某个点旋转一定角度后，得到一个新的点
     * @param p 初始点
     * @param angle 旋转角度
     * @return
     */
    private PointF calcNewPoint(PointF p , float angle) {
        PointF pCenter = new PointF(mWidth/2,mHeight/2) ;
        // calc arc
        float l = (float) ((angle * Math.PI) / 180);

        //sin/cos value
        float cosv = (float) Math.cos(l);
        float sinv = (float) Math.sin(l);

        // calc new point
        float newX = (p.x - pCenter.x) * cosv - (p.y - pCenter.y) * sinv + pCenter.x;
        float newY = (p.x - pCenter.x) * sinv + (p.y - pCenter.y) * cosv + pCenter.y;
        return new PointF(newX, newY);
    }

    /**
     * 设置数据
     * @param items
     */
    public void setData(final List<ItemBean> items){
        if(items == null){
            return;
        }
        isFirstDraw = true ;
        mItems = items ;
        requestLayout();
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * @return
     */
    public int sp2px(float spValue) {
        final float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
