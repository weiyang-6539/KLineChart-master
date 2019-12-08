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
    private List<IBarLineSet> mChild1LineSets;
    private List<IBarLineSet> mChild2LineSets;

    private int indexMain;
    private int indexChild1;
    private int indexChild2;

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

    public void setChild1LineSets(List<IBarLineSet> barLineSets) {
        this.mChild1LineSets = barLineSets;
    }

    public void setChild2LineSets(List<IBarLineSet> barLineSets) {
        this.mChild2LineSets = barLineSets;
    }

    public void changeMain(int index) {
        this.indexMain = index;

        notifyDataSetInvalidated();
    }

    public void changeChild1(int index) {
        this.indexChild1 = index;

        notifyDataSetInvalidated();
    }

    public void changeChild2(int index) {
        this.indexChild2 = index;

        notifyDataSetInvalidated();
    }

    @Override
    public IBarLineSet getMainLineSet() {
        return indexMain != -1 ? mMainLineSets.get(indexMain) : null;
    }

    @Override
    public IBarLineSet getChild1LineSet() {
        return indexChild1 != -1 ? mChild1LineSets.get(indexChild1) : null;
    }

    @Override
    public IBarLineSet getChild2LineSet() {
        return indexChild2 != -1 ? mChild2LineSets.get(indexChild2) : null;
    }
}
