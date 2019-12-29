package com.github.wyang.klinechartlib.huobi.interfaces;

import com.github.wyang.klinechartlib.huobi.data.KLineEntity;

import java.util.List;

/**
 * Created by fxb on 2019-12-29.
 */
public interface IDataLineSetProvider {

    IDataLineSet get(String name);

    /**
     * 计算指标线
     *
     * @param data 适配器中数据
     * @param end  计算结尾position
     */
    void calculate(List<KLineEntity> data, int end);

    /**
     * 指标线最大周期数
     */
    int getMaxN();
}
