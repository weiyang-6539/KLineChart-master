package com.github.wyang.klinechartlib.base;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;

/**
 * Created by weiyang on 2019-11-04.
 */
public abstract class ChartAdapter {
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    /**
     * 获取点的数目
     *
     * @return
     */
    public abstract int getCount();

    /**
     * 获取最新价，最后一个点的收盘价
     *
     * @return
     */
    public abstract float getLatestPrice();

    /**
     * 通过序号获取item
     *
     * @param position 对应的序号
     * @return 数据实体
     */
    public abstract ICandle getCandle(int position);

    public void bindToChartView(@NonNull BaseChartView chartView) {
        chartView.setAdapter(this);
    }

    /**
     * 注册一个数据观察者
     *
     * @param observer 数据观察者
     */
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    /**
     * 移除一个数据观察者
     *
     * @param observer 数据观察者
     */
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }
}
