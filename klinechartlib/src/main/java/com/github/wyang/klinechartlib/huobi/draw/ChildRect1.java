package com.github.wyang.klinechartlib.huobi.draw;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;

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
public class ChildRect1 extends ChartRect {

    public ChildRect1(KLineChartView chart, LinePathHelper helper) {
        super(chart, helper);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        KLineChartAdapter mAdapter = mChart.getAdapter();
        IBarLineSet barLineSet = mAdapter.getChild1LineSet();
        mHelper.save(this, barLineSet);

        int startIndex = mChart.getStartIndex();
        int endIndex = mChart.getEndIndex();
        for (int i = startIndex; i <= endIndex; i++) {
            ICandle data = mAdapter.getCandle(i);
            float top = getAxisY(data.getVolume());
            float bottom = getMinAxisY();

            float x = getAxisX(i);
            if (data.getOpen() - data.getClose() >= 0) {//跌
                mChart.drawCandle(canvas, x, top, bottom, false);
            } else {
                mChart.drawCandle(canvas, x, top, bottom, true);
            }

            mHelper.move(i);
        }

        if (barLineSet != null) {
            List<Path> paths = mHelper.getPaths();
            for (int i = 0; i < paths.size(); i++) {
                mChart.drawLinePath(canvas, paths.get(i), barLineSet.getLineColor(i));
            }
            mHelper.restore();

            int selectedIndex = mChart.getSelectedIndex();
            String text;
            PointF p = PointFPool.get(0, getTop());
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
    }

    @Override
    public void updateMaxMinValue(ICandle data, int index) {
        maxValue = Math.max(maxValue, data.getVolume());
        minValue = 0;

        IBarLineSet barLineSet = mChart.getAdapter().getChild1LineSet();
        if (barLineSet != null) {
            maxValue = Math.max(barLineSet.getMax(index), maxValue);
            minValue = Math.min(barLineSet.getMin(index), minValue);
        }
    }
}
