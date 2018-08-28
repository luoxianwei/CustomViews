package com.yeuyt.spiderwebchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 正N边形的蛛网布局
 */
public class SpiderWebChart extends View{
    /**
     * 蜘蛛网图的中心坐标
     */
    int centerX, centerY;
    /**
     * 正N边形的边数，默认为6,且borders>=3
     */
    int borders = 6;
    /**
     * 多边形的一个内夹角弧度
     */
    double rad;
    /**
     * 有多少层网格，默认为5
     */
    int webLevel = 5;

    /**
     * 网格最大半径
     */
    float radius;
    /**
     * 网格画笔，数据画笔，文字描述画笔，数据顶点的小原点画笔
     */
    Paint webPaint, dataPaint, textPaint, circlePaint;

    /**
     * 传入的数据比列
     */
    private float[] data;

    /**
     * 文字描述数组，应和data一起传入
     */
    private String[] texts;
    /**
     * 所有的线条共用一个path，使用的时候，记得先reset()
     */
    private Path path;


    public SpiderWebChart(Context context) {
        super(context);
        initPaint();

    }

    public SpiderWebChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public SpiderWebChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public SpiderWebChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }


    private void initPaint() {
        webPaint = new Paint();
        webPaint.setAntiAlias(true);
        webPaint.setColor(Color.BLACK);
        webPaint.setStyle(Paint.Style.STROKE);
        webPaint.setStrokeWidth(3);

        dataPaint = new Paint();
        dataPaint.setAntiAlias(true);
        dataPaint.setColor(Color.BLUE);

        dataPaint.setAlpha(100);
        dataPaint.setStyle(Paint.Style.FILL);

        circlePaint = new Paint(dataPaint);
        circlePaint.setAlpha(255);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.CENTER);

        path = new Path();

        //模拟一个数据,实际使用时，应将其删除，由外部提供数据
        data = new float[]{0.5f, 0.4f, 0.2f, 0.7f, 2};
        borders = data.length;
        rad = (float) (Math.PI*2/ borders);//内部顶角之和总为360
    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawPolygonal(canvas);
        drawAllLines(canvas);
        if (data != null)
            drawData(canvas);

    }


    /**
     * 绘制嵌套的多边形
     */
    private void drawPolygonal(Canvas canvas) {
        float dr = radius/(webLevel);//多边形之间的距离
        for(int i=1; i<=webLevel; i++) {
            float currentR = dr*i;//当前网格的长
            path.reset();
            for(int j = 0; j< borders; j++) {
                if(j == 0)
                    //移动到绘画的初始点
                    path.moveTo(centerX+currentR, centerY);
                else {
                    double realRad = rad*j;//记得乘以j
                    float nextX = (float)(centerX+currentR*Math.cos(realRad));
                    float nextY = (float)(centerY+currentR*Math.sin(realRad));
                    path.lineTo(nextX, nextY);
                }
            }

            path.close();
            canvas.drawPath(path, webPaint);
        }
    }

    /**
     *
     绘制从中心到末端的直线,以及相应的描述文字
     */
    private void drawAllLines(Canvas canvas) {

        for(int i = 0; i< borders; i++) {
            path.reset();
            path.moveTo(centerX, centerY);
            double realRad = rad*i;
            float nextX = (float)(centerX+radius*Math.cos(realRad));
            float nextY = (float)(centerY+radius*Math.sin(realRad));
            path.lineTo(nextX, nextY);
            canvas.drawPath(path, webPaint);

            String ss = "顶点"+i;
            /**
             * 顶点文字不应该与蛛网重合,
             * 所以再加点距离,
             */
            //这种算法的实际显示效果不是很好，暂时没想到更好的方法
            float offset = textPaint.measureText(ss)*0.8f;

            nextX = (float)(centerX+ (offset+radius)*Math.cos(realRad));
            nextY = (float)(centerY+ (offset+radius)*Math.sin(realRad));
            canvas.drawText(ss, nextX, nextY, textPaint);

        }
    }

    /**
     *根据传入的data,绘制数据,及顶点小圆点
     */
    private void drawData(Canvas canvas) {

        path.reset();
        //先画顶点的小原点

        for(int i = 0; i< borders; i++) {
            float dataRadius = data[i]*radius;
            if (dataRadius>radius)
                dataRadius = radius;

            double realRad = rad * i;
            float nextX = (float) (centerX + dataRadius * Math.cos(realRad));
            float nextY = (float) (centerY + dataRadius * Math.sin(realRad));
            if(i == 0)
                path.moveTo(centerX+dataRadius, centerY);//绘画的初始点
            else {
                path.lineTo(nextX, nextY);//记录下点的位置
            }
            canvas.drawCircle(nextX, nextY, 10, circlePaint);
        }

        canvas.drawPath(path, dataPaint);

    }
    /**
     *设置数据百分比，根据data长度决定borders和rad
     */
    public void setDataPercentage(float[] data) {
        if (data == null || data.length<3) {
            try {
                throw new Exception("数据不符合规则");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        borders = data.length;
        rad = (float) ((borders -2)*Math.PI/ borders);
        this.data = data;
        invalidate();
    }

    /**
     * 清除数据并重绘
     */
    public void clear() {
        data = null;
        borders = 6;
        invalidate();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius = Math.min(w, h)/2*0.8f;

        centerX = w/2;
        centerY = h/2;
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
