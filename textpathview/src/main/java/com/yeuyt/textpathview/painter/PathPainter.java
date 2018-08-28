package com.yeuyt.textpathview.painter;

import android.graphics.Path;

public interface PathPainter {
    /**
     * 进行一些初始化操作
     */
    void onDrawStart();

    /**
     * 绘画画笔特效时执行
     * @param x 当前绘画点x坐标
     * @param y 当前绘画点y坐标
     * @param paintPath 画笔Path对象，在这里画出想要的画笔特效
     */
    void onDrawPaintPath(float x, float y, Path paintPath);
}
