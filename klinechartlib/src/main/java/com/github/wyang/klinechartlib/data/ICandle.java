package com.github.wyang.klinechartlib.data;

/**
 * Created by fxb on 2019-11-11.
 * 蜡烛图对应图形：上影线 + 矩形 + 下影线
 */
public interface ICandle {

    float getOpen();//开盘价

    float getHigh();//最高价

    float getLow();//最低价

    float getClose();//收盘价

    long getTime();//时间
}
