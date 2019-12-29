package com.github.wyang.klinechartlib.huobi.data;

import com.github.wyang.klinechartlib.data.IVolume;
import com.github.wyang.klinechartlib.huobi.interfaces.IData;

/**
 * Created by fxb on 2019-12-25.
 */
public class Volume implements IVolume, IData {
    public float volume;

    public Volume(float volume) {
        this.volume = volume;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getMax() {
        return volume;
    }

    @Override
    public float getMin() {
        return 0;
    }
}
