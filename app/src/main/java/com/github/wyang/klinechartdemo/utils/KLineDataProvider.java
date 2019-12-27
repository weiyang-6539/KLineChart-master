package com.github.wyang.klinechartdemo.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fxb on 2019-12-25.
 */
public class KLineDataProvider {
    private List<KLineData> dataList = new ArrayList<>();

    public KLineDataProvider() {
        dataList.add(new KLineData());//分时线
        dataList.add(new KLineData());//1分钟
        dataList.add(new KLineData());//5分钟
        dataList.add(new KLineData());//15分钟
        dataList.add(new KLineData());//30分钟
        dataList.add(new KLineData());//1小时
        dataList.add(new KLineData());//4小时
        dataList.add(new KLineData());//1天
        dataList.add(new KLineData());//1周
        dataList.add(new KLineData());//1月
    }


}
