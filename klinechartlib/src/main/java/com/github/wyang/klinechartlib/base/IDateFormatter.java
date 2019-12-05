package com.github.wyang.klinechartlib.base;

/**
 * Created by weiyang on 2019-11-06.
 * 时间转换器接口，参数毫秒值
 * 数据时间不推荐使用字符串（类似 yyyy-MM-dd HH:mm:ss.SSS）
 * 分时线 K线（分钟，日K，周K，月K）格式化后的String不相同
 */
public interface IDateFormatter {
    String format(long time);
}
