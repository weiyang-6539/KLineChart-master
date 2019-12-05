package com.github.wyang.klinechartlib.huobi;

import com.github.wyang.klinechartlib.base.ChartAdapter;
import com.github.wyang.klinechartlib.base.ICandle;
import com.github.wyang.klinechartlib.base.IBarLineSet;
import com.github.wyang.klinechartlib.huobi.interfaces.IBarLineSetProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weiyang on 2019-11-04.
 */
public class KLineChartAdapter extends ChartAdapter implements IBarLineSetProvider {
    private List<ICandle> mCandleData = new ArrayList<>();
    private List<IBarLineSet> mMainLineSets;
    private List<IBarLineSet> mChildLineSets;

    private int indexMain = 0;
    private int indexChild1 = 0;
    private int indexChild2 = -1;

    public KLineChartAdapter() {
    }

    @Override
    public int getCount() {
        return mCandleData.size();
    }

    @Override
    public float getLatestPrice() {
        return getCandle(getCount() - 1).getClose();
    }

    @Override
    public ICandle getCandle(int position) {
        return mCandleData.get(position);
    }

    public void setData(List<ICandle> candles) {
        mCandleData.addAll(candles);
    }

    public void setMainLineSets(List<IBarLineSet> barLineSets) {
        this.mMainLineSets = barLineSets;
    }

    public void setChildLineSets(List<IBarLineSet> barLineSets) {
        this.mChildLineSets = barLineSets;
    }

    @Override
    public IBarLineSet getMainLineSet() {
        return indexMain != -1 ? mMainLineSets.get(indexMain) : null;
    }

    @Override
    public IBarLineSet getChild1LineSet() {
        return indexChild1 != -1 ? mChildLineSets.get(indexChild1) : null;
    }

    @Override
    public IBarLineSet getChild2LineSet() {
        return indexChild2 != -1 ? mChildLineSets.get(indexChild2) : null;
    }
}
