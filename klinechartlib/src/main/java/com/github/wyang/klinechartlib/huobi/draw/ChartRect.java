package com.github.wyang.klinechartlib.huobi.draw;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.github.wyang.klinechartlib.base.ICandle;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.helper.LinePathHelper;

/**
 * Created by weiyang on 2019-11-04.
 * 图表拆分部分，绘制流程如下：
 * 1，绘制蜡烛图（K线蜡烛，成交量，成交额）
 * 2，绘制指标线（同区域存在多条指标线，不分先后顺序）
 * 3，绘制坐标值（x轴日期，y轴价格值，指标值，成交量值..）
 * 4，绘制高亮值（最大最小值在前，长按高亮线，该点对应各值等）
 */
public abstract class ChartRect implements ICharRect {
    /**
     * 表格一部分在View所占矩形区域
     */
    private RectF mBounds = new RectF();
    /**
     * 绘制曲线的平滑度
     */
    protected float SMOOTHNESS = 0.2f;

    protected float mTopSpacing;

    /**
     * 矩形最上方y轴对应值（最大）
     */
    protected float maxValue;
    /**
     * 矩形最下方y轴对应值（最小）
     */
    protected float minValue;

    protected KLineChartView mChart;
    protected LinePathHelper mHelper;

    public ChartRect(KLineChartView chart, LinePathHelper helper) {
        this.mChart = chart;
        this.mHelper = helper;
    }

    public abstract void draw(@NonNull Canvas canvas);

    public abstract void updateMaxMinValue(ICandle t, int index);

    public void setBounds(float left, float top, float right, float bottom) {
        mBounds.set(left, top, right, bottom);
    }

    public void setTopSpacing(float mTopSpacing) {
        this.mTopSpacing = mTopSpacing;
    }

    @Override
    public float getMaxValue() {
        return maxValue;
    }

    @Override
    public float getMinValue() {
        return minValue;
    }

    @Override
    public float getMaxAxisY() {
        return mBounds.top + mTopSpacing;
    }

    @Override
    public float getMinAxisY() {
        return mBounds.bottom;
    }

    @Override
    public float getAxisX(int index) {
        return mChart.getDrawX(index);
    }

    @Override
    public float getAxisY(float value) {
        float scaleY = (getMinAxisY() - getMaxAxisY()) / (maxValue - minValue);

        return (maxValue - value) * scaleY + getMaxAxisY();
    }

    @Override
    public float getTopSpacing() {
        return mTopSpacing;
    }

    @Override
    public float getTop() {
        return mBounds.top;
    }

    @Override
    public float getBottom() {
        return mBounds.bottom;
    }

    public void resetMaxMinValue() {
        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;
    }
}
