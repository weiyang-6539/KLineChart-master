package com.github.wyang.klinechartlib.huobi.interfaces;

import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.data.KLineEntity;
import com.github.wyang.klinechartlib.huobi.data.DataLineSetProvider;

import java.util.List;

/**
 * Created by fxb on 2019-12-23.
 */
public interface IKLineChartAdapter {

    void bindToKLineChartView(KLineChartView kLineChartView);

    void bindToDataLineSetProvider(DataLineSetProvider provider);

    void setNewData(List<KLineEntity> list);

    void addData(List<KLineEntity> list);

    IDataLineSet getDataLineSet(String name);

    ICandle getCandle(int position);

    boolean isIncrease(int position);

    float getLatestPrice();

    void notifyDataSetChanged();

    void notifyDataSetInvalidated();
}
