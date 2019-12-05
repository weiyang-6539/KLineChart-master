package com.github.wyang.klinechartlib.huobi.helper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Created by fxb on 2019-12-04.
 * 画布上绘制文本辅助类
 */
public class TextDrawHelper {
    /**
     * 默认绘制文本，点在绘制文本矩形的整正左方
     */
    private void draw(Canvas c, String text, PointF p, Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        p.y -= (fontMetrics.descent + fontMetrics.ascent) / 2;

        c.drawText(text, p.x, p.y, paint);
    }

    /**
     * 绘制文本于点的正中心
     */
    public void drawPointCenter(Canvas c, String text, PointF p, Paint paint) {
        p.x -= paint.measureText(text) / 2;

        draw(c, text, p, paint);
    }


    /**
     * 绘制文本于点的正左方
     */
    public void drawPointLeft(Canvas c, String text, PointF p, Paint paint) {
        p.x -= paint.measureText(text);

        draw(c, text, p, paint);
    }

    /**
     * 绘制文本于点的正上方
     */
    public void drawPointTop(Canvas c, String text, PointF p, Paint paint) {
        p.x -= paint.measureText(text) / 2;
        p.y -= getTextHeight(paint) / 2;

        draw(c, text, p, paint);
    }

    /**
     * 绘制文本于点的正右方
     */
    public void drawPointRight(Canvas c, String text, PointF p, Paint paint) {
        draw(c, text, p, paint);
    }

    /**
     * 绘制文本于点的正下方
     */
    public void drawPointBot(Canvas c, String text, PointF p, Paint paint) {
        p.x -= paint.measureText(text) / 2;
        p.y += getTextHeight(paint) / 2;

        draw(c, text, p, paint);
    }

    /**
     * 绘制文本于点的左上方
     */
    public void drawPointLeftTop(Canvas c, String text, PointF p, Paint paint) {
        p.x -= paint.measureText(text);
        p.y -= getTextHeight(paint) / 2;

        draw(c, text, p, paint);
    }

    /**
     * 绘制文本于点的左上方
     */
    public void drawPointRightBot(Canvas c, String text, PointF p, Paint paint) {
        p.y += getTextHeight(paint) / 2;

        draw(c, text, p, paint);
    }

    public float getTextHeight(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.bottom - fontMetrics.top;
    }
}
