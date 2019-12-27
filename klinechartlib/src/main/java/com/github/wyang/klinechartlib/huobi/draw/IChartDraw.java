package com.github.wyang.klinechartlib.huobi.draw;

/**
 * Created by fxb on 2019-11-12.
 */
public interface IChartDraw {

    /**
     * 设置当前指标名称，名称用于从适配器中取对应DataLineSet
     */
    void setName(String name);

    /**
     * 实际最大值
     */
    float getMaxValue();

    /**
     * 实际最小值
     */
    float getMinValue();

    /**
     * 最大值对应View中的y值
     */
    float getMaxAxisY();

    /**
     * 最小值对应View中的y值
     */
    float getMinAxisY();

    /**
     * 计算当前索引在矩形区域内的X坐标值
     */
    float getAxisX(int position);

    /**
     * 计算某一值在矩形区域内的Y坐标值
     */
    float getAxisY(float value);

    /**
     * 在矩形中，最大值上方预留的间距，用来绘制指标值String
     */
    float getTopSpacing();

    float getTop();

    float getBottom();
}
