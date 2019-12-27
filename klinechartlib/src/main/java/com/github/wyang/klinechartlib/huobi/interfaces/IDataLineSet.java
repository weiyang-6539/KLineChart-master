package com.github.wyang.klinechartlib.huobi.interfaces;

/**
 * Created by fxb on 2019-11-18.
 */
public interface IDataLineSet {

    String getName();

    boolean isShowName();

    String getDataLabel();

    int getCount();

    void addData(IData o);

    <T extends IData> T getData(int position);

    int getDataCount();

    void addLine(int color, String label);

    int getLineColor(int index);

    String getLabel(int index);

    void addLinePoint(Float... floats);

    Float getLinePoint(int index, int position);

    int getLineSize();

    float getMax(int index);

    float getMin(int index);

    void clear();
}
