package com.yeuyt.textpathview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;


import com.yeuyt.pathview.R;
import com.yeuyt.textpathview.painter.PathPainter;
import com.yeuyt.textpathview.painter.PenPainter;
import com.yeuyt.textpathview.utils.PathAnimatorListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class TextPathView extends View{

    public static final int RESTART = 1;
    public static final int REVERSE = 2;

    /**
     * 参照ValueAnimator中的RESTART, REVERSE常量定义的
     */
    @IntDef({RESTART, REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Repeat{}

    /**
     * 动画播放模式，默认为重复播放模式
     * 另，当repeatCount的播放次数>=1，才有效
     */
    @Repeat
    protected int mRepeatMode = RESTART;

    /**
     * 要绘画文字的路径
     */
    protected Path mTextPath;
    /**
     * 画笔的路径
     */
    protected Path mPenPath;

    /**
     * 绘画路径的画笔
     */
    protected Paint mTextPaint;
    /**
     * 绘画画笔的画笔
     */
    protected Paint mPenPaint;

    /**
     * 是否在绘画过程中展示画笔, 默认为true
     */
    protected boolean mShowPen = true;

    /**
     * 动画进度
     */
    protected float mProgressAnimator;
    /**
     * 绘画的持续时间, 默认为6秒
     */
    protected int mDuration = 6000;

    /**
     * 用来存放text的完整路径
     */
    protected Path mCompletePath;

    /**
     * 测量mCompletePath
     */
    protected PathMeasure mPathMeasure;

    /**
     * mCompletePath的总长度
     */
    protected float mLengthSum;

    /**
     * 画布的中心坐标
     */
    int centerX, centerY;
    /**
     * 当前绘画位置,用来确定画笔位置
     */
    protected float[] mCurPos = new float[2];

    /**
     * 要绘画的文字
     */
    protected String mText;
    /**
     * 文字大小
     */
    protected float mTextSize;

    /**
     * 文字宽高
     */
    protected float mTextWidth, mTextHeight;


    /**
     * 绘画结束后，文字是否填充颜色,默认填充
     */
    protected boolean mShouldFill = true;

    //文字路径的粗细，画笔粗细
    protected int mTextStrokeWidth = 5, mPenStrokeWidth = 3;
    //文字路径的颜色，画笔路径颜色
    protected int mTextStrokeColor = Color.BLACK, mPenStrokeColor = Color.BLACK;

    protected ValueAnimator mAnimator;
    protected PathAnimatorListener mListener;

    /**
     * 画笔的特效，默认为penPainter
     */
    protected PathPainter mPathPainter;

    public TextPathView(Context context) {
        super(context);
        initAttr(context, null);
    }

    public TextPathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
    }

    public TextPathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
    }

    public TextPathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context, attrs);
    }

    protected void initAttr(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TextPathView);
        if (array != null) {
            mDuration = array.getInt(R.styleable.TextPathView_duration, mDuration);
            mShowPen = array.getBoolean(R.styleable.TextPathView_showPen, mShowPen);
            mRepeatMode = array.getInt(R.styleable.TextPathView_repeatMode, mRepeatMode);

            mTextStrokeWidth = array.getDimensionPixelOffset(R.styleable.TextPathView_pathStrokeWidth, mTextStrokeWidth);
            mTextStrokeColor = array.getColor(R.styleable.TextPathView_pathStrokeColor, mTextStrokeColor);
            mPenStrokeWidth = array.getDimensionPixelOffset(R.styleable.TextPathView_paintStrokeWidth, mPenStrokeWidth);
            mPenStrokeColor = array.getColor(R.styleable.TextPathView_paintStrokeColor, mPenStrokeColor);
            mText = array.getString(R.styleable.TextPathView_text);
            if(TextUtils.isEmpty(mText)) {
                mText = "O(∩_∩)O~";
            }
            float defTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 60,
                    context.getResources().getDisplayMetrics());
            //默认为60sp
            mTextSize = array.getDimension(R.styleable.TextPathView_textSize, defTextSize);
            array.recycle();
        }
        initCommon();

    }

    /**
     * 初始化一些公共操作
     */
    private void initCommon() {
        initPaint();
        initPath();
        initMeasure();

        mPathPainter = new PenPainter();
    }
    private void initPaint() {
        mTextPaint = new Paint();
        mTextPaint.setColor(mTextStrokeColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setStrokeWidth(mTextStrokeWidth);

        mPenPaint = new Paint();
        mPenPaint.setAntiAlias(true);
        mPenPaint.setStyle(Paint.Style.STROKE);
        mPenPaint.setStrokeWidth(mPenStrokeWidth);
        mPenPaint.setColor(mPenStrokeColor);

    }
    protected void initPath() {
        mTextPath = new Path();
        mPenPath = new Path();
        mCompletePath = new Path();
    }
    private void initMeasure() {
        mPathMeasure = new PathMeasure();

        mTextPath.reset();
        mCompletePath.reset();

        mTextPaint.setTextSize(mTextSize);

        //获取宽高
        mTextWidth = mTextPaint.measureText(mText);
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        mTextHeight = metrics.bottom - metrics.top;

        //TextAlign可以决定text的绘画起点，center表示起点在文字bottom的中点
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        //参数中的x, y表示mCompletePath在画布中的坐标,
        // 和TextAlign配合使用决定在画布中的绘画起点
        mTextPaint.getTextPath(mText, 0, mText.length(), 0, -metrics.ascent, mCompletePath);
        mPathMeasure.setPath(mCompletePath, false);

        do {
            mLengthSum +=mPathMeasure.getLength();
        } while (mPathMeasure.nextContour());

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        if (mTextWidth >  measureWidth) {
            handleNewLines( measureWidth);
            mTextWidth =  measureWidth;
        }
        //考虑到边缘溢出画布，应该加个1
        int width = (int)mTextWidth + getPaddingLeft()+getPaddingRight();
        int height = (int)mTextHeight + getPaddingBottom()+getPaddingTop();
        setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? measureWidth : width,
                (modeHeight == MeasureSpec.EXACTLY) ? measureHeight : height);

    }


    /**
     * 处理换行：拆分字符串，分别获取它们的path，再拼接
     * @param measureWidth  TextPathView的宽
     */
    protected void handleNewLines(float measureWidth) {
        float[] widths = new float[mText.length()];
        mTextPaint.getTextWidths(mText, widths);

        float widthSum = 0;
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        float ascent = -mTextPaint.getFontMetrics().ascent;
        float height = metrics.descent + ascent;
        int start = 0, count = 0;
        mCompletePath.reset();
        for (int i = 0; i < widths.length; i++) {
            float width = widths[i];
            widthSum += width;
            if (widthSum > measureWidth) {
                String text = mText.substring(start, i);
                widthSum = width;
                start = i;
                Path path = new Path();
                mTextPaint.getTextPath(text, 0, text.length(), 0, ascent, path);
                mCompletePath.addPath(path, 0, height * count);
                count++;
            }
        }
        if (start < widths.length) {
            String text = mText.substring(start, widths.length);
            Path path = new Path();
            mTextPaint.getTextPath(text, 0, text.length(), 0, ascent, path);
            mCompletePath.addPath(path, 0, height * count);
        }
        mTextHeight = height * ++count;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w/2;
        centerY = h/2;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //需要考虑文字的高度,进行一下微调
        canvas.translate(centerX, centerY-mTextHeight/2);

        if(mProgressAnimator<1) {
            if (mShowPen) {
                canvas.drawPath(mPenPath, mPenPaint);

            }
            canvas.drawPath(mTextPath, mTextPaint);
        } else {
            if (mShouldFill)
                mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawPath(mCompletePath, mTextPaint);
            //恢复mTextPaint
            mTextPaint.setStyle(Paint.Style.STROKE);
        }
    }
    /**
     * 路径的具体绘制方法
     * @param progressAnimator 动画进度，根据动画进度来绘画path
     */
    private void drawPath(float progressAnimator) {
        if (!isProgressValid(progressAnimator)){
            return;
        }
        mProgressAnimator = progressAnimator;
        float mCurrent = progressAnimator*mLengthSum;

        //重置路径
        mTextPath.reset();
        mPenPath.reset();
        mPathMeasure.setPath(mCompletePath, false);

        //根据进度获取路径
        while (mCurrent > mPathMeasure.getLength()) {
            mCurrent = mCurrent - mPathMeasure.getLength();
            mPathMeasure.getSegment(0, mPathMeasure.getLength(), mTextPath, true);
            if (!mPathMeasure.nextContour()) {
                break;
            }
        }
        mPathMeasure.getSegment(0, mCurrent, mTextPath, true);

        if (mShowPen && mCurrent != mLengthSum && mCurrent != 0) {
            mPathMeasure.getPosTan(mCurrent, mCurPos, null);
            drawPen(mCurPos[0], mCurPos[1], mPenPath);
        }
        postInvalidate();
    }
    /**
     * 画笔的具体绘制方法
     */
    protected void drawPen(float x, float y, Path penPath) {
        mPathPainter.onDrawPaintPath(x, y, penPath);
    }

    private void initAnimator(float start, float end, @Repeat int repeatMode, int repeatCount) {
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float)animation.getAnimatedValue();
                drawPath(progress);
            }
        });

        if (mListener != null){
            mAnimator.removeAllListeners();
            mAnimator.addListener(mListener);
        }

        mAnimator.setDuration(mDuration);
        //设置插补器
        mAnimator.setInterpolator(new LinearInterpolator());
        switch (repeatMode) {
            case RESTART:
                mAnimator.setRepeatMode(ValueAnimator.RESTART);
                break;
            case REVERSE:
                mAnimator.setRepeatMode(ValueAnimator.REVERSE);
                break;
        }
        mAnimator.setRepeatCount(repeatCount);
    }

    /**
     * 开始绘制文字路径动画
     * @param start 路径比例，范围0-1
     * @param end   路径比例，范围0-1
     */
    public void startAnimation(float start,float end) {
        startAnimation(start, end, mRepeatMode, ValueAnimator.INFINITE);
    }

    public void startAnimation(float start, float end, @Repeat int repeatMode, int repeatCount) {
        clearAnimation();
        initAnimator(start, end, repeatMode, repeatCount);
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator.start();
        mPathPainter.onDrawStart();
    }

    protected boolean isProgressValid(float progress) {
        if (progress < 0 || progress > 1) {
            try {
                throw new Exception("Progress is invalid!");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 清除画面
     */
    public void clearAnimation() {
        mProgressAnimator = 0;
        mTextPath.reset();
        mPenPath.reset();
        postInvalidate();
    }
//------------------------------以下都是setter和getter方法----------------------------------


    public void setPathPainter(PathPainter mPathPainter) {
        this.mPathPainter = mPathPainter;
    }

    public void cancelAnimation() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        clearAnimation();
    }

    public void setText(String text) {
        mText = text;
        try {
            initMeasure();
        } catch (Exception e) {
            e.printStackTrace();
        }
        clearAnimation();
        requestLayout();
    }

    public void pauseAnimation() {
        if (mAnimator != null) {
            mAnimator.pause();
        }
    }

    public void resumeAnimation() {
        if (mAnimator != null) {
            mAnimator.resume();
        }
    }

    public Paint getTextPaint() {
        return mTextPaint;
    }

    public Paint getPenPaint() {
        return mPenPaint;
    }
    //设置动画时能否显示画笔效果
    public void setShowPen(boolean showPen) {
        this.mShowPen = showPen;
    }
    /**
     * 必须在startAnimation前设置才有效
     */
    public void setAnimatorListener(PathAnimatorListener animatorListener) {
        this.mListener = animatorListener;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public void setRepeatMode(@Repeat int repeatMode) {
        this.mRepeatMode = repeatMode;
    }

    public void setmShouldFill(boolean mShouldFill) {
        this.mShouldFill = mShouldFill;
    }
}
