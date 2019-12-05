package com.github.wyang.klinechartlib.base;

/**
 * Created by weiyang on 2019-11-11.
 * 蜡烛图实体类
 */
public interface ICandle {

    float getOpen();//开盘价

    float getHigh();//最高价

    float getLow();//最低价

    float getClose();//收盘价

    float getVolume();//成交量

    float getTotal();//成交额

    long getTime();//时间

    float getChangeValue();//涨跌值

    String getChangePercent();//涨跌幅
}
