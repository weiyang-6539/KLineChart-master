package com.github.wyang.klinechartlib.base;

import java.util.List;

/**
 * Created by fxb on 2019-11-18.
 */
public interface IBarLineSet {

    String getName();

    boolean showName();

    String getBarLabel();

    void addBarData(Float data);

    List<Float> getBarData();

    void addLine(int color, String label);

    int getLineColor(int index);

    String getLabel(int index);

    List<Float> getLine(int index);

    int getLineSize();

    float getMax(int index);

    float getMin(int index);
}
