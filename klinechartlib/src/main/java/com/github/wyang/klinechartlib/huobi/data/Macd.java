package com.github.wyang.klinechartlib.huobi.data;

import com.github.wyang.klinechartlib.data.IMacd;
import com.github.wyang.klinechartlib.huobi.interfaces.IData;

/**
 * Created by fxb on 2019-12-25.
 */
public class Macd implements IMacd, IData {
    public float macd;

    public Macd(float macd) {
        this.macd = macd;
    }

    @Override
    public float getMacd() {
        return macd;
    }

    @Override
    public float getMax() {
        return macd;
    }

    @Override
    public float getMin() {
        return macd;
    }
}
