package com.github.wyang.klinechartlib.huobi.interfaces;

import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.data.DataLineSetProvider;
import com.github.wyang.klinechartlib.huobi.data.KLineEntity;

import java.util.List;

/**
 * Created by fxb on 2019-12-23.
 */
public interface IKLineChartAdapter {

    void bindToKLineChartView(KLineChartView kLineChartView);

    void bindToDataLineSetProvider(DataLineSetProvider provider);

    /**
     * 第一次添加数据
     */
    void setNewData(List<KLineEntity> list);

    /**
     * 分页加载时添加数据
     */
    void addData(List<KLineEntity> list);

    /**
     * 接收报价时添加柱子
     */
    void addData(KLineEntity data, boolean replace);

    KLineEntity getData(int position);

    IDataLineSet getDataLineSet(String name);

    boolean isIncrease(int position);

    float getLatestPrice();

    void notifyDataSetChanged();

    void notifyDataSetInvalidated();
}
