package com.zhanglin.recordview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by zhanglin on 2018/6/27.
 */
public class RoundView extends View {

    private static final int EXIT_CIRCLE_DEFAULT_RADIUS = 50;//dp
    private static final int INNER_CIRCLE_DEFAULT_RADIUS = 40;//dp
    private static final int INNER_CIRCLE_DEFAULT_COLOR = Color.parseColor("#ffffff");
    private static final int EXIT_CIRCLE_DEFAULT_COLOR = Color.parseColor("#dddddd");
    private static final int PROGRESS_DEFAULT_COLOR = Color.parseColor("#00dd00");
    private static final int PROGRESS_DEFAULT_WIDTH = 5;//dp
    private static final int LONG_PRESS_TIME = 500;//ms
    private static final int CIRCLE_SCALE_TIME = 150;//ms
    private static final int PROGRESS_DEFAULT_DURATION = 15000;//ms
    private Paint mProgressPaint, mExitPaint, mInnerPaint;
    private ValueAnimator mProgressAnim, mExitAnim, mInnerAnim;
    private float currentProgress;
    private boolean isStart = false;
    private int mHeight, mWidth;
    private long startTime, endTime;
    private float mExitCircleRadius, mExitInitCircleRadius, mInnerCircleRadius, mInnerInitCircleRadius, mProgressWidth;
    private int mExitCircleColor, mInnerCircleColor, mProgressColor;

    public RoundView(Context context) {
        this(context, null);
    }

    public RoundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundView);
        mExitCircleRadius = mExitInitCircleRadius = a.getDimensionPixelSize(R.styleable.RoundView_exitCircleRadius, dpToPx(EXIT_CIRCLE_DEFAULT_RADIUS));
        mInnerCircleRadius = mInnerInitCircleRadius = a.getDimensionPixelSize(R.styleable.RoundView_innerCircleRadius, dpToPx(INNER_CIRCLE_DEFAULT_RADIUS));
        mExitCircleColor = a.getColor(R.styleable.RoundView_exitCircleColor, EXIT_CIRCLE_DEFAULT_COLOR);
        mInnerCircleColor = a.getColor(R.styleable.RoundView_innerCircleColor, INNER_CIRCLE_DEFAULT_COLOR);
        mProgressColor = a.getColor(R.styleable.RoundView_progressColor, PROGRESS_DEFAULT_COLOR);
        mProgressWidth = a.getDimensionPixelSize(R.styleable.RoundView_progressWidth, dpToPx(PROGRESS_DEFAULT_WIDTH));
        a.recycle();
        init();
    }

    private int dpToPx(int dp) {
        return ScreenUtils.dip2px(getContext(), dp);
    }

    private void init() {
        mExitPaint = new Paint();
        mExitPaint.setColor(mExitCircleColor);
        mExitPaint.setAntiAlias(true);
        mExitPaint.setStyle(Paint.Style.FILL);

        mInnerPaint = new Paint();
        mInnerPaint.setColor(mInnerCircleColor);
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setStyle(Paint.Style.FILL);

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStrokeWidth(mProgressWidth);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        initExitAnim();
        initInnerAnim();
        initProgressAnim();
    }

    private void initInnerAnim() {
        mInnerAnim = new ValueAnimator();
        mInnerAnim.setDuration(CIRCLE_SCALE_TIME);
        mInnerAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerCircleRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public void setMaxDuration(int maxDuration) {
        mProgressAnim.setDuration(maxDuration);
    }

    private void startInnerAnim(boolean isScaleBig) {
        if (!isScaleBig) {
            mInnerAnim.setFloatValues(mInnerCircleRadius, mInnerCircleRadius / 2);
        } else {
            mInnerAnim.setFloatValues(mInnerCircleRadius, mInnerInitCircleRadius);
        }
        mInnerAnim.start();
    }

    private void startExitAnim(boolean isScaleBig) {
        if (isScaleBig) {
            mExitAnim.setFloatValues(mExitCircleRadius, mExitCircleRadius * 1.5f);
        } else {
            mExitAnim.setFloatValues(mExitCircleRadius, mExitInitCircleRadius);
        }
        mExitAnim.start();
    }

    private void initExitAnim() {
        mExitAnim = new ValueAnimator();
        mExitAnim.setDuration(CIRCLE_SCALE_TIME);
        mExitAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mExitCircleRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    private void initProgressAnim() {
        mProgressAnim = ValueAnimator.ofFloat(0, 360f);
        mProgressAnim.setDuration(PROGRESS_DEFAULT_DURATION);
        mProgressAnim.setInterpolator(new LinearInterpolator());
        mProgressAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mProgressAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isStart = false;
                currentProgress = 0;
                startExitAnim(false);
                startInnerAnim(true);
                invalidate();
                if (iRoundViewAction != null) {
                    iRoundViewAction.onRecordFinish();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mExitCircleRadius, mExitPaint);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mInnerCircleRadius, mInnerPaint);
        if (isStart) {
            drawProgress(canvas);
        }
    }

    private void drawProgress(Canvas canvas) {
        float offset = mExitCircleRadius - mProgressPaint.getStrokeWidth() / 2;
        RectF rect = new RectF(mWidth / 2 - offset, mHeight / 2 - offset, mWidth / 2 + offset, mHeight / 2 + offset);
        canvas.drawArc(rect, -90, currentProgress, false, mProgressPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isStart = true;
                startTime = System.currentTimeMillis();
                handler.sendEmptyMessageDelayed(LONG_PRESS_TIME, LONG_PRESS_TIME);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isStart = false;
                endTime = System.currentTimeMillis();
                if (endTime - startTime < LONG_PRESS_TIME) {
                    if (handler.hasMessages(LONG_PRESS_TIME)) {
                        handler.removeMessages(LONG_PRESS_TIME);
                    }
                    if (iRoundViewAction != null) {
                        iRoundViewAction.onSingleClick();
                    }
                } else {
                    mProgressAnim.cancel();
                }

                break;
        }

        return true;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LONG_PRESS_TIME:
                    startExitAnim(true);
                    startInnerAnim(false);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressAnim.start();
                        }
                    }, CIRCLE_SCALE_TIME);
                    if (iRoundViewAction != null) {
                        iRoundViewAction.onRecordStart();
                    }
                    break;
            }
        }
    };
    private IRoundViewAction iRoundViewAction;

    public void setiRoundViewAction(IRoundViewAction iRoundViewAction) {
        this.iRoundViewAction = iRoundViewAction;
    }

    public interface IRoundViewAction {
        void onSingleClick();

        void onRecordStart();

        void onRecordFinish();
    }
}
