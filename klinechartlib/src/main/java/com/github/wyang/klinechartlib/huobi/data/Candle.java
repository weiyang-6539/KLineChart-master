package com.github.wyang.klinechartlib.huobi.data;

import com.github.wyang.klinechartlib.base.ICandle;

/**
 * Created by weiyang on 2019-11-11.
 */
public class Candle implements ICandle {
    public float open;
    public float high;
    public float low;
    public float close;
    public float volume;
    public float total;
    public long time;

    public float changeValue;
    public String changePercent;

    @Override
    public float getOpen() {
        return open;
    }

    @Override
    public float getHigh() {
        return high;
    }

    @Override
    public float getLow() {
        return low;
    }

    @Override
    public float getClose() {
        return close;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getTotal() {
        return total;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public float getChangeValue() {
        return changeValue;
    }

    @Override
    public String getChangePercent() {
        return changePercent;
    }
}
