package com.github.wyang.klinechartlib.huobi.data;

import android.util.SparseArray;
import android.util.SparseIntArray;

import com.github.wyang.klinechartlib.huobi.interfaces.IData;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fxb on 2019-11-18.
 */
public class DataLineSet implements IDataLineSet {
    /**
     * 指标名称（ma,rsi,sar,macd,boll...）
     */
    private String name;
    /**
     * 是否显示名称
     */
    private boolean isShowName;
    /**
     * data数据对应标签
     */
    private String dataLabel;
    /**
     * 用于绘制矩形的值
     */
    private List<IData> data = new ArrayList<>();
    /**
     * 线的颜色
     */
    private SparseIntArray colors = new SparseIntArray();
    /**
     * 指标标签
     */
    private SparseArray<String> labels = new SparseArray<>();
    /**
     * 绘制线的点
     */
    private SparseArray<List<Float>> lines = new SparseArray<>();

    public DataLineSet() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShowName(boolean showName) {
        isShowName = showName;
    }

    public void setDataLabel(String dataLabel) {
        this.dataLabel = dataLabel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isShowName() {
        return isShowName;
    }

    @Override
    public String getDataLabel() {
        return dataLabel;
    }

    @Override
    public int getCount() {
        if (!data.isEmpty())
            return getDataCount();
        if (lines.size() != 0)
            return lines.get(0).size();
        return 0;
    }

    @Override
    public void addData(boolean replace, int position, IData o) {
        if (replace && position < data.size())
            data.set(position, o);
        else
            data.add(position, o);
    }

    @Override
    public <T extends IData> T getData(int position) {
        return (T) data.get(position);
    }

    @Override
    public int getDataCount() {
        return data.size();
    }

    @Override
    public void addLine(int color, String label) {
        int size = getLineSize();
        colors.put(size, color);
        labels.put(size, label);
        lines.put(size, new ArrayList<Float>());
    }

    @Override
    public int getLineColor(int index) {
        return colors.get(index);
    }

    @Override
    public String getLabel(int index) {
        return labels.get(index);
    }

    @Override
    public void addLinePoint(boolean replace, int position, Float... floats) {
        if (floats.length != lines.size()) {
            throw new IllegalArgumentException("更新线的元素点时，点与线的数量必须相等");
        }

        for (int i = 0; i < floats.length; i++) {
            List<Float> list = lines.get(i);
            if (replace && position < list.size())
                list.set(position, floats[i]);
            else
                list.add(position, floats[i]);
        }
    }

    @Override
    public Float getLinePoint(int index, int position) {
        return lines.get(index).get(position);
    }

    @Override
    public int getLineSize() {
        return colors.size();
    }

    @Override
    public float getMax(int index) {
        float rst = Float.MIN_VALUE;
        for (int i = 0; i < getLineSize(); i++) {
            Float f = getLinePoint(i, index);
            if (f != null)
                rst = Math.max(rst, f);
        }
        return data.isEmpty() ? rst : Math.max(rst, data.get(index).getMax());
    }

    @Override
    public float getMin(int index) {
        float rst = Float.MAX_VALUE;
        for (int i = 0; i < getLineSize(); i++) {
            Float f = getLinePoint(i, index);
            if (f != null)
                rst = Math.min(rst, f);
        }
        return data.isEmpty() ? rst : Math.min(rst, data.get(index).getMin());
    }

    @Override
    public void clear() {
        data.clear();
        colors.clear();
        labels.clear();
        lines.clear();
    }
}
