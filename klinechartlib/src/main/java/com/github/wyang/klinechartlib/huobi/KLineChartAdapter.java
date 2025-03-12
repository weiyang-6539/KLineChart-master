package com.github.wyang.klinechartlib.huobi;

import com.github.wyang.klinechartlib.base.ChartAdapter;
import com.github.wyang.klinechartlib.huobi.data.DataLineSetProvider;
import com.github.wyang.klinechartlib.huobi.data.KLineEntity;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSet;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSetProvider;
import com.github.wyang.klinechartlib.huobi.interfaces.IKLineChartAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fxb on 2019-11-04.
 */
public class KLineChartAdapter extends ChartAdapter implements IKLineChartAdapter {
    private final List<KLineEntity> mData = new ArrayList<>();
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
        mData.clear();
        mData.addAll(list);

        provider.calculateAll(mData);

        notifyDataSetChanged();
    }

    @Override
    public void addData(List<KLineEntity> list) {
        mData.addAll(0, list);

        provider.calculateAll(mData);

        notifyDataSetChanged();
    }

    @Override
    public void addData(KLineEntity data, boolean replace) {
        if (replace)
            mData.set(mData.size() - 1, data);
        else
            mData.add(data);

        provider.calculateLast(mData, replace);

        notifyDataSetChanged();
    }

    @Override
    public KLineEntity getData(int position) {
        return mData.get(position);
    }

    @Override
    public boolean isIncrease(int position) {
        KLineEntity data = getData(position);
        return data.close - data.open >= 0;
    }

    @Override
    public float getLatestPrice() {
        return getCount() == 0 ? 0 : getData(getCount() - 1).close;
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
