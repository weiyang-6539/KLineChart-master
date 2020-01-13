package com.github.wyang.klinechartlib.huobi.interfaces;

/**
 * Created by fxb on 2019-12-27.
 */
public interface KLineConstant {
    //最多有6条均线，对应也就6个色号
    Integer[] COLORS = new Integer[]{0xfff2dc9c, 0xff7ecfc0, 0xffc197f7, 0xffec4d46, 0xff89ce40, 0xff6437f5};

    /**
     * 数组中存在 Integer.MAX_VALUE 时，表示当前指标线不可用
     */
    int[] MA_N = new int[]{5, 20, 30, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
    int[] EMA_N = new int[]{12, 26};
    int[] BOLL_N = new int[]{20, 2};

    int[] VOL_N = new int[]{5, 10};
    int[] MACD_N = new int[]{12, 26, 9};
    int[] KDJ_N = new int[]{14, 1, 3};
    int[] RSI_N = new int[]{14, 12, 6};
    int[] WR_N = new int[]{14, Integer.MAX_VALUE, Integer.MAX_VALUE};
}
