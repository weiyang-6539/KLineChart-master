package com.github.wyang.klinechartlib.data;

/**
 * Created by fxb on 2019-12-25.
 * SAR指标对应图：空心圆（算法复杂，后续拓展）
 */
public interface ISar {
    float getSar();

    boolean isIncrease();
}
