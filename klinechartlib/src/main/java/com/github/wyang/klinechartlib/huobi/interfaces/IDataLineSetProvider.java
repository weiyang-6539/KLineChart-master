package com.github.wyang.klinechartlib.huobi.interfaces;

import com.github.wyang.klinechartlib.huobi.data.KLineEntity;

import java.util.List;

/**
 * Created by fxb on 2019-12-29.
 */
public interface IDataLineSetProvider {

    IDataLineSet get(String name);

    void calculateAll(List<KLineEntity> data);

    void calculateLast(List<KLineEntity> data, boolean replace);
}
