package com.github.wyang.klinechartlib.huobi;

import com.github.wyang.klinechartlib.base.ChartAdapter;
import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSet;
import com.github.wyang.klinechartlib.huobi.interfaces.IKLineChartAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by weiyang on 2019-11-04.
 */
public class KLineChartAdapter extends ChartAdapter implements IKLineChartAdapter {

    private Map<String, IDataLineSet> dataMap = new HashMap<>();

    @Override
    public int getCount() {
        IDataLineSet dataLineSet = dataMap.get("");
        return dataLineSet == null ? 0 : dataLineSet.getCount();
    }

    @Override
    public void bindToKLineChartView(KLineChartView kLineChartView) {
        kLineChartView.setAdapter(this);
    }

    @Override
    public ICandle getCandle(int position) {
        IDataLineSet dataLineSet = dataMap.get("");
        if (dataLineSet == null)
            throw new IllegalArgumentException("未添加Candle数据");
        return (ICandle) dataLineSet.getData(position);
    }

    @Override
    public boolean isIncrease(int position) {
        ICandle candle = getCandle(position);
        if (candle == null)
            throw new IllegalArgumentException("未添加Candle数据");
        return candle.getClose() - candle.getOpen() >= 0;
    }

    @Override
    public float getLatestPrice() {
        return getCount() == 0 ? 0 : getCandle(getCount() - 1).getClose();
    }

    @Override
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public IKLineChartAdapter addDataLineSet(String name, IDataLineSet dataLineSet) {
        dataMap.put(name, dataLineSet);
        return this;
    }

    @Override
    public IDataLineSet getDataLineSet(String name) {
        return dataMap.get(name);
    }

}
