package com.github.wyang.klinechartlib.huobi.interfaces;

import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.huobi.KLineChartView;

/**
 * Created by fxb on 2019-12-23.
 */
public interface IKLineChartAdapter {

    void bindToKLineChartView(KLineChartView kLineChartView);

    IKLineChartAdapter addDataLineSet(String name, IDataLineSet dataLineSet);

    IDataLineSet getDataLineSet(String name);

    ICandle getCandle(int position);

    boolean isIncrease(int position);

    float getLatestPrice();

    void notifyDataSetChanged();

    void notifyDataSetInvalidated();
}
