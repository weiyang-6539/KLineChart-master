package com.github.wyang.klinechartlib.huobi;

import com.github.wyang.klinechartlib.base.ChartAdapter;
import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.huobi.data.DataLineSetProvider;
import com.github.wyang.klinechartlib.huobi.data.KLineEntity;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSet;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSetProvider;
import com.github.wyang.klinechartlib.huobi.interfaces.IKLineChartAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weiyang on 2019-11-04.
 */
public class KLineChartAdapter extends ChartAdapter implements IKLineChartAdapter {
    private List<KLineEntity> mData = new ArrayList<>();
    private IDataLineSetProvider provider;

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public void bindToKLineChartView(KLineChartView kLineChartView) {
        kLineChartView.setAdapter(this);
    }

    @Override
    public void bindToDataLineSetProvider(DataLineSetProvider provider) {
        this.provider = provider;
    }

    @Override
    public void setNewData(List<KLineEntity> list) {
        mData.addAll(list);

        provider.calculate(mData, mData.size() - 1);

        notifyDataSetChanged();
    }

    @Override
    public void addData(List<KLineEntity> list) {
        mData.addAll(0, list);

        int end = list.size() - 1 + provider.getMaxN();
        if (end > mData.size() - 1)
            provider.calculate(mData, mData.size() - 1);
        else
            provider.calculate(mData, end);

        notifyDataSetChanged();
    }

    @Override
    public ICandle getCandle(int position) {
        return mData.get(position).getCandle();
    }

    @Override
    public boolean isIncrease(int position) {
        ICandle candle = getCandle(position);
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
    public IDataLineSet getDataLineSet(String name) {
        return provider.get(name.toLowerCase());
    }

}
