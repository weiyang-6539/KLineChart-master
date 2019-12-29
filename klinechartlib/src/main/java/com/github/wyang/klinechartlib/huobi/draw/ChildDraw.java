package com.github.wyang.klinechartlib.huobi.draw;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.github.wyang.klinechartlib.R;
import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.data.IMacd;
import com.github.wyang.klinechartlib.data.IVolume;
import com.github.wyang.klinechartlib.huobi.KLineChartAdapter;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.helper.LinePathHelper;
import com.github.wyang.klinechartlib.huobi.interfaces.IData;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSet;
import com.github.wyang.klinechartlib.utils.PointFPool;

import java.util.List;

/**
 * Created by fxb on 2019-11-04.
 */
public class ChildDraw extends ChartDraw {

    public ChildDraw(KLineChartView chart, LinePathHelper helper) {
        super(chart, helper);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        KLineChartAdapter mAdapter = mChart.getAdapter();
        IDataLineSet dataLineSet = mAdapter.getDataLineSet(name);
        if (dataLineSet == null)
            return;
        mHelper.save(this, dataLineSet);

        int startIndex = mChart.getStartIndex();
        int endIndex = mChart.getEndIndex();

        //生成绘制线的Path并绘制特殊图形（蜡烛图、Sar圆、成交量、macd……）
        for (int i = startIndex; i <= endIndex; i++) {
            mHelper.move(i);

            if (dataLineSet.getDataCount() == 0)
                continue;

            IData data = dataLineSet.getData(i);

            float x = getAxisX(i);
            if (data instanceof ICandle) {
                ICandle candle = (ICandle) data;
                float[] arrY = new float[4];
                arrY[0] = getAxisY(candle.getOpen());
                arrY[1] = getAxisY(candle.getClose());
                arrY[2] = getAxisY(candle.getHigh());
                arrY[3] = getAxisY(candle.getLow());
                mChart.drawAmericanLine(canvas, x, arrY);
            }
            if (data instanceof IVolume) {
                IVolume volume = (IVolume) data;
                float y = getAxisY(volume.getVolume());
                mChart.drawCandle(canvas, x, y, getAxisY(0), mAdapter.isIncrease(i));
            }
            if (data instanceof IMacd) {
                IMacd macd = (IMacd) data;
                float y = getAxisY(macd.getMacd());
                mChart.drawCandle(canvas, x, y, getAxisY(0), mAdapter.isIncrease(i));
            }
        }

        //绘制指标线
        List<Path> paths = mHelper.getPaths();
        for (int i = 0; i < paths.size(); i++) {
            mChart.drawLinePath(canvas, paths.get(i), dataLineSet.getLineColor(i));
        }
        mHelper.restore();

        int selectedIndex = mChart.getSelectedIndex();
        String text;
        PointF p = PointFPool.get(mChart.getAxisTextPadding(), getTop());
        //绘制指标名称
        if (dataLineSet.isShowName()) {
            text = dataLineSet.getName();

            mChart.drawText(canvas, text, p, mChart.getColor(R.color.chart_text));

            p.x += mChart.getTextPaint().measureText(text + "    ");
            p.y = getTop();
        }

        //绘制各项指标值
        if (dataLineSet.getDataCount() != 0 && !TextUtils.isEmpty(dataLineSet.getDataLabel())) {
            IData data = dataLineSet.getData(selectedIndex == -1 ? dataLineSet.getDataCount() - 1 : selectedIndex);

            text = dataLineSet.getDataLabel() + mChart.getPriceFormatter().format(data.getMax());

            mChart.drawText(canvas, text, p, mChart.getColor(R.color.chart_text));

            p.x += mChart.getTextPaint().measureText(text + "    ");
            p.y = getTop();
        }
        for (int i = 0; i < dataLineSet.getLineSize(); i++) {
            Float rst = dataLineSet.getLinePoint(i, selectedIndex == -1 ? mAdapter.getCount() - 1 : selectedIndex);
            if (rst == null)
                continue;

            text = dataLineSet.getLabel(i) + mChart.getPriceFormatter().format(rst);
            mChart.drawText(canvas, text, p, dataLineSet.getLineColor(i));

            p.x += mChart.getTextPaint().measureText(text + "    ");
            p.y = getTop();
        }
        PointFPool.recycle(p);
    }

    @Override
    public void calcMinMax(int position, boolean isReset) {
        if (isReset) {
            maxValue = Float.MIN_VALUE;
            minValue = Float.MAX_VALUE;
        }
        IDataLineSet dataLineSet = mChart.getAdapter().getDataLineSet(name);
        if (dataLineSet != null) {
            maxValue = Math.max(dataLineSet.getMax(position), maxValue);
            minValue = Math.min(dataLineSet.getMin(position), minValue);
        } else {
            maxValue = Float.MIN_VALUE;
            minValue = Float.MAX_VALUE;
        }
    }

    @Override
    public void fixMaxMin(float diff) {
        float minimum = (maxValue - minValue) / (getMinAxisY() - getMaxAxisY() - diff);
        minValue -= minimum * diff;
    }
}
