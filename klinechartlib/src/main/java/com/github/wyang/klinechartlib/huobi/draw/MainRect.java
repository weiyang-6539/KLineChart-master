package com.github.wyang.klinechartlib.huobi.draw;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.github.wyang.klinechartlib.base.IBarLineSet;
import com.github.wyang.klinechartlib.base.ICandle;
import com.github.wyang.klinechartlib.huobi.KLineChartAdapter;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.helper.LinePathHelper;
import com.github.wyang.klinechartlib.utils.PointFPool;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by weiyang on 2019-11-04.
 * 最下面为收盘线or蜡烛图，支持均线，boll线...
 */
public class MainRect extends ChartRect {

    @IntDef({Mode.LINE, Mode.CANDLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
        int LINE = 1;//收盘线
        int CANDLE = 2;//蜡烛图
    }

    @Mode
    private int mode = Mode.LINE;


    private Path linePath = new Path();


    /**
     * 当前可见最高价
     */
    private float maxPrice;
    /**
     * 当前可见最低价
     */
    private float minPrice;
    private int mMaxValueIndex;
    private int mMinValueIndex;

    public MainRect(KLineChartView chart, LinePathHelper helper) {
        super(chart, helper);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        linePath.reset();

        PointF lineP1 = PointFPool.get(0, 0);
        PointF lineP2 = PointFPool.get(0, 0);

        KLineChartAdapter mAdapter = mChart.getAdapter();
        IBarLineSet barLineSet = mAdapter.getMainLineSet();

        mHelper.save(this, barLineSet);

        int startIndex = mChart.getStartIndex();
        int endIndex = mChart.getEndIndex();

        for (int i = startIndex; i <= endIndex; i++) {
            ICandle data = mAdapter.getCandle(i);

            float x = getAxisX(i);
            if (isLine()) {
                if (i == startIndex) {
                    linePath.moveTo(x, getAxisY(data.getClose()));
                    //更新下一个点的控制点1
                    lineP1.x = x + (x - x) * SMOOTHNESS;
                    lineP1.y = getAxisY(data.getClose());
                } else if (i == endIndex) {
                    //更新当前控制点2
                    lineP2.x = x - (x - getAxisX(i - 1)) * SMOOTHNESS;
                    lineP2.y = getAxisY(data.getClose());

                    linePath.cubicTo(lineP1.x, lineP1.y, lineP2.x, lineP2.y, x, getAxisY(data.getClose()));
                } else {
                    ICandle last, next;
                    last = mAdapter.getCandle(i - 1);
                    next = mAdapter.getCandle(i + 1);
                    float k = (getAxisY(next.getClose()) - getAxisY(last.getClose())) / (getAxisX(i + 1) - getAxisX(i - 1));
                    float b = getAxisY(data.getClose()) - k * getAxisX(i);

                    //更新当前控制点2
                    lineP2.x = x - (x - getAxisX(i - 1)) * SMOOTHNESS;
                    lineP2.y = k * lineP2.x + b;

                    linePath.cubicTo(lineP1.x, lineP1.y, lineP2.x, lineP2.y, x, getAxisY(data.getClose()));

                    //更新下一个点的控制点1
                    lineP1.x = x + (getAxisX(i + 1) - x) * SMOOTHNESS;
                    lineP1.y = k * lineP1.x + b;
                }
            } else {
                float high = getAxisY(data.getHigh());
                float low = getAxisY(data.getLow());
                float open = getAxisY(data.getOpen());
                float close = getAxisY(data.getClose());

                if (open > close) {//涨(这里比较的y坐标值)
                    mChart.drawCandle(canvas, x, open, close, true);
                    mChart.drawCandleLine(canvas, x, high, close);
                    mChart.drawCandleLine(canvas, x, open, low);
                } else if (open < close) {
                    mChart.drawCandle(canvas, x, close, open, false);
                    mChart.drawCandleLine(canvas, x, high, open);
                    mChart.drawCandleLine(canvas, x, close, low);
                } else {
                    mChart.drawCandle(canvas, x, close - 1, close, true);
                    mChart.drawCandleLine(canvas, x, high, low);
                }

                mHelper.move(i);
            }
        }

        PointFPool.recycle(lineP1);
        PointFPool.recycle(lineP2);

        //先画线#5776AD
        if (isLine()) {
            mChart.drawLinePath(canvas, linePath, 0xFF5776AD);
            LinearGradient shader = new LinearGradient(0, 0, 0, getMinAxisY(), 0x335776AD, 0x005776AD, Shader.TileMode.CLAMP);

            linePath.lineTo(getAxisX(endIndex), getMinAxisY());
            linePath.lineTo(getAxisX(startIndex), getMinAxisY());
            linePath.close();
            mChart.drawFillPath(canvas, linePath, 0xffff0828, shader);
        } else {
            List<Path> paths = mHelper.getPaths();
            for (int i = 0; i < paths.size(); i++) {
                mChart.drawLinePath(canvas, paths.get(i), barLineSet.getLineColor(i));
            }
            mHelper.restore();

            if (barLineSet != null) {
                int selectedIndex = mChart.getSelectedIndex();
                String text;
                PointF p = PointFPool.get(0, 0);
                for (int i = 0; i < barLineSet.getLineSize(); i++) {
                    Float rst = barLineSet.getLine(i).get(selectedIndex == -1 ? mAdapter.getCount() - 1 : selectedIndex);
                    if (rst == null)
                        continue;

                    text = barLineSet.getLabel(i) + mChart.getPriceFormatter().format(rst);
                    mChart.drawText(canvas, text, p, barLineSet.getLineColor(i));

                    p.x += mChart.getTextPaint().measureText(text) + 5;
                    p.y = 0;
                }
                PointFPool.recycle(p);
            }
        }

        //绘制最大最小值
        if (!isLine()) {
            Paint mHighLightPaint = mChart.getHighLightPaint();
            //画最小值
            ICandle candle = mAdapter.getCandle(mMinValueIndex);
            String text;

            PointF p = PointFPool.get(0, 0);
            p.x = getAxisX(mMinValueIndex);
            p.y = getAxisY(candle.getLow());
            if (p.x < mChart.getWidth() / 2) {
                text = "─ " + mChart.getPriceFormatter().format(candle.getLow());
                //画右边
                mChart.getTextDrawHelper().drawPointRight(canvas, text, p, mHighLightPaint);
            } else {
                //画左边
                text = mChart.getPriceFormatter().format(candle.getLow()) + " ─";
                mChart.getTextDrawHelper().drawPointLeft(canvas, text, p, mHighLightPaint);
            }

            //画最大值
            candle = mAdapter.getCandle(mMaxValueIndex);
            p.x = getAxisX(mMaxValueIndex);
            p.y = getAxisY(candle.getHigh());

            if (p.x < mChart.getWidth() / 2) {
                text = "─ " + mChart.getPriceFormatter().format(candle.getHigh());
                //画右边
                mChart.getTextDrawHelper().drawPointRight(canvas, text, p, mHighLightPaint);
            } else {
                //画左边
                text = mChart.getPriceFormatter().format(candle.getHigh()) + " ─";
                mChart.getTextDrawHelper().drawPointLeft(canvas, text, p, mHighLightPaint);
            }

            PointFPool.recycle(p);
        }
    }

    @Override
    public void updateMaxMinValue(ICandle data, int index) {
        if (isLine()) {
            maxValue = Math.max(data.getClose(), maxValue);
            minValue = Math.min(data.getClose(), minValue);
        } else {
            maxValue = Math.max(data.getHigh(), maxValue);
            minValue = Math.min(data.getLow(), minValue);

            IBarLineSet barLineSet = mChart.getAdapter().getMainLineSet();
            if (barLineSet != null) {
                maxValue = Math.max(barLineSet.getMax(index), maxValue);
                minValue = Math.min(barLineSet.getMin(index), minValue);
            }

            maxPrice = Math.max(data.getHigh(), maxPrice);
            minPrice = Math.min(data.getLow(), minPrice);
            if (maxPrice == data.getHigh())
                mMaxValueIndex = index;
            if (minPrice == data.getLow())
                mMinValueIndex = index;
        }

        float latestPrice = mChart.getAdapter().getLatestPrice();

        maxValue = Math.max(latestPrice, maxValue);
        minValue = Math.min(latestPrice, minValue);
    }

    @Override
    public void resetMaxMinValue() {
        super.resetMaxMinValue();
        maxPrice = Float.MIN_VALUE;
        minPrice = Float.MAX_VALUE;
    }

    public void fixMaxMin() {
        float textHeight = mChart.getTextDrawHelper().getTextHeight(mChart.getHighLightPaint());

        float minimum = (maxValue - minValue) / (getMinAxisY() - getMaxAxisY() - textHeight);
        maxValue += minimum * textHeight / 2;
        minValue -= minimum * textHeight / 2;

        if (maxValue == minValue) {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            maxValue += Math.abs(maxValue * 0.05f);
            minValue -= Math.abs(minValue * 0.05f);
            if (maxValue == 0) {
                maxValue = 1;
            }
        }
    }

    public void setMode(@Mode int mode) {
        this.mode = mode;
    }

    public boolean isLine() {
        return mode == Mode.LINE;
    }
}
