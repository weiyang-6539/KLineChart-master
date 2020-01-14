package com.github.wyang.klinechartlib.base;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;

import com.github.wyang.klinechartlib.huobi.KLineChartView;

/**
 * Created by fxb on 2019-11-04.
 */
public abstract class ChartAdapter {
    protected final DataSetObservable mDataSetObservable = new DataSetObservable();

    /**
     * 获取点的数目
     *
     * @return
     */
    public abstract int getCount();

    /**
     * 注册一个数据观察者
     *
     * @param observer 数据观察者
     */
    void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    /**
     * 移除一个数据观察者
     *
     * @param observer 数据观察者
     */
    void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }
}
