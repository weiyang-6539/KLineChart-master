package com.github.wyang.klinechartlib.huobi.data;

import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.huobi.interfaces.IData;

/**
 * Created by weiyang on 2019-11-11.
 */
public class Candle implements ICandle, IData {
    public float open;
    public float high;
    public float low;
    public float close;
    public long time;

    public Candle(float open, float high, float low, float close, long time) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.time = time;
    }

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
    public long getTime() {
        return time;
    }

    @Override
    public float getMax() {
        return high;
    }

    @Override
    public float getMin() {
        return low;
    }
}
