package com.github.wyang.klinechartlib.huobi.data;

import android.util.SparseArray;
import android.util.SparseIntArray;

import com.github.wyang.klinechartlib.base.IBarLineSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fxb on 2019-11-18.
 */
public class BarLineSet implements IBarLineSet {
    /**
     * 指标名称（ma,rsi,sar,macd,boll...）
     */
    private String name;
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

    public BarLineSet(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
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
    public List<Float> getLine(int index) {
        return lines.get(index);
    }

    @Override
    public int getLineSize() {
        return colors.size();
    }

    @Override
    public float getMax(int index) {
        float rst = Float.MIN_VALUE;
        for (int i = 0; i < getLineSize(); i++) {
            Float f = getLine(i).get(index);
            if (f != null)
                rst = Math.max(rst, f);
        }
        return rst;
    }

    @Override
    public float getMin(int index) {
        float rst = Float.MAX_VALUE;
        for (int i = 0; i < getLineSize(); i++) {
            Float f = getLine(i).get(index);
            if (f != null)
                rst = Math.min(rst, f);
        }
        return rst;
    }
}
