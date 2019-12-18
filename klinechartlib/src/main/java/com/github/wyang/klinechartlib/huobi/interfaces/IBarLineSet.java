package com.github.wyang.klinechartlib.huobi.interfaces;

import java.util.List;

/**
 * Created by fxb on 2019-11-18.
 */
public interface IBarLineSet {

    String getName();

    boolean showName();

    String getBarLabel();

    void addData(Float data);

    List<Float> getData();

    void addLine(int color, String label);

    int getLineColor(int index);

    String getLabel(int index);

    List<Float> getLine(int index);

    int getLineSize();

    float getMax(int index);

    float getMin(int index);
}
