package com.github.wyang.klinechartlib.base;

/**
 * Created by weiyang on 2019-11-08.
 * 数值格式化接口，价格精确 涨跌幅计算 成交量转换
 */
public interface IValueFormatter {
    String format(float value);
}
