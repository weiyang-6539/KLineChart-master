package com.github.wyang.klinechartdemo.utils;

/**
 * Created by fxb on 2019-12-27.
 */
public interface KLineConstant {
    //最多有6条均线，对应也就6个色号
    Integer[] COLORS = new Integer[]{0xfff2dc9c, 0xff7ecfc0, 0xffc197f7, 0xffec4d46, 0xff89ce40, 0xff6437f5};

    /**
     * 数组中存在null说明线的显示数量可变
     */
    Integer[] MA_N = new Integer[]{5, 10, 30, null, null, null};
    Integer[] EMA_N = new Integer[]{12, 26};
    Integer[] BOLL_N = new Integer[]{20, 2};

    Integer[] VOL_N = new Integer[]{5, 10};
    Integer[] MACD_N = new Integer[]{12, 26, 9};
    Integer[] KDJ_N = new Integer[]{14, 1, 3};
    Integer[] RSI_N = new Integer[]{14, null, null};
    Integer[] WR_N = new Integer[]{14, null, null};
}
