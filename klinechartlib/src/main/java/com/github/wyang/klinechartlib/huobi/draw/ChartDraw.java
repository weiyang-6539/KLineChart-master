package com.github.wyang.klinechartlib.huobi.draw;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;

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
public abstract class ChartDraw implements IChartDraw {
    /**
     * 表格一部分在View所占矩形区域
     */
    private RectF mBounds = new RectF();

    private float mTopSpacing;

    /**
     * 矩形最上方y轴对应值（最大）
     */
    float maxValue;
    /**
     * 矩形最下方y轴对应值（最小）
     */
    float minValue;
    /**
     * 指标名称
     */
    protected String name = "";

    KLineChartView mChart;
    LinePathHelper mHelper;

    ChartDraw(KLineChartView chart, LinePathHelper helper) {
        this.mChart = chart;
        this.mHelper = helper;
    }

    public abstract void draw(@NonNull Canvas canvas);

    public abstract void calcMinMax(int position, boolean isReset);

    public abstract void fixMaxMin(float diff);

    @Override
    public void setName(String name) {
        this.name = name;
    }

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
    public float getAxisX(int position) {
        return mChart.getDrawX(position);
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
}
