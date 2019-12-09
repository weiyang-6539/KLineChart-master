package com.github.wyang.klinechartlib.huobi.draw;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.github.wyang.klinechartlib.R;
import com.github.wyang.klinechartlib.base.IBarLineSet;
import com.github.wyang.klinechartlib.base.ICandle;
import com.github.wyang.klinechartlib.huobi.KLineChartAdapter;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.helper.LinePathHelper;
import com.github.wyang.klinechartlib.utils.PointFPool;

import java.util.List;

/**
 * Created by fxb on 2019-11-04.
 */
public class ChildRect2 extends ChartRect {

    public ChildRect2(KLineChartView chart, LinePathHelper helper) {
        super(chart, helper);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        KLineChartAdapter mAdapter = mChart.getAdapter();
        IBarLineSet barLineSet = mAdapter.getChild2LineSet();
        if (barLineSet == null)
            return;
        mHelper.save(this, barLineSet);

        int startIndex = mChart.getStartIndex();
        int endIndex = mChart.getEndIndex();

        List<Float> barData = barLineSet.getBarData();
        for (int i = startIndex; i <= endIndex; i++) {
            mHelper.move(i);

            if (barData.isEmpty())
                continue;
            ICandle candle = mAdapter.getCandle(i);
            Float rst = barData.get(i);
            if (rst != null) {
                float x = getAxisX(i);
                mChart.drawFillCandle(canvas, x, getAxisY(rst), getAxisY(0), candle.getChangeValue() >= 0);
            }
        }

        List<Path> paths = mHelper.getPaths();
        for (int i = 0; i < paths.size(); i++) {
            mChart.drawLinePath(canvas, paths.get(i), barLineSet.getLineColor(i));
        }
        mHelper.restore();

        int selectedIndex = mChart.getSelectedIndex();
        String text;
        PointF p = PointFPool.get(0, getTop());
        if (barLineSet.showName()) {
            text = barLineSet.getName();

            mChart.drawText(canvas, text, p, mChart.getColor(R.color.chart_text));

            p.x += mChart.getTextPaint().measureText(text) + 5;
            p.y = getTop();
        }
        if (!barData.isEmpty()) {
            text = barLineSet.getBarLabel() + mChart.getPriceFormatter().format(
                    barData.get(selectedIndex == -1 ? mAdapter.getCount() - 1 : selectedIndex));

            mChart.drawText(canvas, text, p, mChart.getColor(R.color.chart_text));

            p.x += mChart.getTextPaint().measureText(text) + 5;
            p.y = getTop();
        }

        for (int i = 0; i < barLineSet.getLineSize(); i++) {
            Float rst = barLineSet.getLine(i).get(selectedIndex == -1 ? mAdapter.getCount() - 1 : selectedIndex);
            if (rst == null)
                continue;

            text = barLineSet.getLabel(i) + mChart.getPriceFormatter().format(rst);
            mChart.drawText(canvas, text, p, barLineSet.getLineColor(i));

            p.x += mChart.getTextPaint().measureText(text) + 5;
            p.y = getTop();
        }
        PointFPool.recycle(p);
    }

    @Override
    public void updateMaxMinValue(ICandle data, int index) {
        IBarLineSet barLineSet = mChart.getAdapter().getChild2LineSet();
        if (barLineSet != null) {
            maxValue = Math.max(barLineSet.getMax(index), maxValue);
            minValue = Math.min(barLineSet.getMin(index), minValue);
        } else {
            maxValue = Math.max(maxValue, data.getVolume());
            minValue = 0;
        }
    }
}
