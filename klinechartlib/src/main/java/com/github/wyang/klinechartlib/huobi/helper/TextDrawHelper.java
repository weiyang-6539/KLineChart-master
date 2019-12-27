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
     * 默认绘制文本，点在文本矩形的正左方
     */
    private void draw(Canvas c, String text, float x, float y, Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        y -= (fontMetrics.descent + fontMetrics.ascent) / 2;

        c.drawText(text, x, y, paint);
    }

    /**
     * 绘制文本于点的正中心
     */
    public void drawPointCenter(Canvas c, String text, PointF p, Paint paint) {
        float x = p.x - paint.measureText(text) / 2;
        float y = p.y;

        draw(c, text, x, y, paint);
    }

    /**
     * 绘制文本于点的正左方
     */
    public void drawPointLeft(Canvas c, String text, PointF p, Paint paint) {
        float x = p.x - paint.measureText(text);
        float y = p.y;

        draw(c, text, x, y, paint);
    }

    /**
     * 绘制文本于点的正上方
     */
    public void drawPointTop(Canvas c, String text, PointF p, Paint paint) {
        float x = p.x - paint.measureText(text) / 2;
        float y = p.y - getTextHeight(paint) / 2;

        draw(c, text, x, y, paint);
    }

    /**
     * 绘制文本于点的正右方
     */
    public void drawPointRight(Canvas c, String text, PointF p, Paint paint) {
        float x = p.x;
        float y = p.y;

        draw(c, text, x, y, paint);
    }

    /**
     * 绘制文本于点的正下方
     */
    public void drawPointBot(Canvas c, String text, PointF p, Paint paint) {
        float x = p.x - paint.measureText(text) / 2;
        float y = p.y + getTextHeight(paint) / 2;

        draw(c, text, x, y, paint);
    }

    /**
     * 绘制文本于点的左上方
     */
    public void drawPointLeftTop(Canvas c, String text, PointF p, Paint paint) {
        float x = p.x - paint.measureText(text);
        float y = p.y - getTextHeight(paint) / 2;

        draw(c, text, x, y, paint);
    }

    /**
     * 绘制文本于点的左下方
     */
    public void drawPointLeftBot(Canvas c, String text, PointF p, Paint paint) {
        float x = p.x - paint.measureText(text);
        float y = p.y + getTextHeight(paint) / 2;

        draw(c, text, x, y, paint);
    }

    /**
     * 绘制文本于点的右下方
     */
    public void drawPointRightBot(Canvas c, String text, PointF p, Paint paint) {
        float x = p.x;
        float y = p.y + getTextHeight(paint) / 2;

        draw(c, text, x, y, paint);
    }

    public float getTextHeight(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.bottom - fontMetrics.top;
    }

    public float getTextHeight2(Paint paint) {
        return paint.ascent() + paint.descent();
    }
}
