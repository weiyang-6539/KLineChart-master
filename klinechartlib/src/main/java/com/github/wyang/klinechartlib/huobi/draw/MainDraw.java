package com.github.wyang.klinechartlib.huobi.draw;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.huobi.KLineChartAdapter;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.data.KLineEntity;
import com.github.wyang.klinechartlib.huobi.helper.LinePathHelper;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSet;
import com.github.wyang.klinechartlib.utils.PointFPool;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by weiyang on 2019-11-04.
 * 最下面为收盘线or蜡烛图，支持均线，boll线...
 */
public class MainDraw extends ChartDraw {

    @IntDef({Mode.LINE, Mode.CANDLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
        int LINE = 1;//收盘线
        int CANDLE = 2;//蜡烛图
    }

    @Mode
    private int mode = Mode.LINE;

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

    public MainDraw(KLineChartView chart, LinePathHelper helper) {
        super(chart, helper);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        KLineChartAdapter mAdapter = mChart.getAdapter();
        IDataLineSet dataLineSet = mAdapter.getDataLineSet(name);

        mHelper.save(this, dataLineSet);

        int startIndex = mChart.getStartIndex();
        int endIndex = mChart.getEndIndex();

        for (int i = startIndex; i <= endIndex; i++) {
            ICandle data = mAdapter.getData(i).getCandle();

            if (isCandle()) {
                float x = getAxisX(i);

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
            }
            mHelper.move(i);
        }

        //先画线#5776AD
        if (isLine()) {
            Path linePath = mHelper.getLinePath();
            mChart.drawLinePath(canvas, linePath, 0xFF5776AD);
            LinearGradient shader = new LinearGradient(0, 0, 0, getMinAxisY(), 0x335776AD, 0x005776AD, Shader.TileMode.CLAMP);

            linePath.lineTo(getAxisX(endIndex), getMinAxisY());
            linePath.lineTo(getAxisX(startIndex), getMinAxisY());
            linePath.close();
            mChart.drawFillPath(canvas, linePath, 0xffff0828, shader);
        } else {
            List<Path> paths = mHelper.getPaths();
            for (int i = 0; i < paths.size(); i++) {
                mChart.drawLinePath(canvas, paths.get(i), dataLineSet.getLineColor(i));
            }

            if (dataLineSet != null) {
                int selectedIndex = mChart.getSelectedIndex();
                String text;
                PointF p = PointFPool.get(mChart.getAxisTextPadding(), mChart.getAxisTextPadding());
                for (int i = 0; i < dataLineSet.getLineSize(); i++) {
                    Float rst = dataLineSet.getLinePoint(i, selectedIndex == -1 ? mAdapter.getCount() - 1 : selectedIndex);
                    if (rst == null)
                        continue;

                    text = dataLineSet.getLabel(i) + mChart.getPriceFormatter().format(rst);
                    float x = p.x + mChart.getTextPaint().measureText(text + "    ");
                    if (x > mChart.getWidth() - mChart.getAxisTextPadding()) {
                        p.x = mChart.getAxisTextPadding();
                        p.y += mChart.getDefaultTextHeight();
                    }
                    mChart.drawText(canvas, text, p, dataLineSet.getLineColor(i));
                    if (p.x < mChart.getWidth() - mChart.getAxisTextPadding())
                        p.x += mChart.getTextPaint().measureText(text + "    ");

                }
                PointFPool.recycle(p);
            }
        }
        mHelper.restore();

        //绘制最大最小值
        if (!isLine()) {
            Paint mHighLightPaint = mChart.getHighLightPaint();
            //画最小值
            KLineEntity entity = mAdapter.getData(mMinValueIndex);
            String text;

            PointF p = PointFPool.get(0, 0);
            p.x = getAxisX(mMinValueIndex);
            p.y = getAxisY(entity.low);
            if (p.x < mChart.getWidth() / 2) {
                text = "─ " + mChart.getPriceFormatter().format(entity.low);
                //画右边
                mChart.getTextDrawHelper().drawPointRight(canvas, text, p, mHighLightPaint);
            } else {
                //画左边
                text = mChart.getPriceFormatter().format(entity.low) + " ─";
                mChart.getTextDrawHelper().drawPointLeft(canvas, text, p, mHighLightPaint);
            }

            //画最大值
            entity = mAdapter.getData(mMaxValueIndex);
            p.x = getAxisX(mMaxValueIndex);
            p.y = getAxisY(entity.high);

            if (p.x < mChart.getWidth() / 2) {
                text = "─ " + mChart.getPriceFormatter().format(entity.high);
                //画右边
                mChart.getTextDrawHelper().drawPointRight(canvas, text, p, mHighLightPaint);
            } else {
                //画左边
                text = mChart.getPriceFormatter().format(entity.high) + " ─";
                mChart.getTextDrawHelper().drawPointLeft(canvas, text, p, mHighLightPaint);
            }

            PointFPool.recycle(p);
        }
    }

    @Override
    public void calcMinMax(int position, boolean isReset) {
        if (isReset) {
            maxValue = maxPrice = Float.MIN_VALUE;
            minValue = minPrice = Float.MAX_VALUE;
        }
        ICandle data = mChart.getAdapter().getData(position).getCandle();
        if (isLine()) {
            maxValue = Math.max(data.getClose(), maxValue);
            minValue = Math.min(data.getClose(), minValue);
        } else {
            IDataLineSet dataLineSet = mChart.getAdapter().getDataLineSet(name);
            if (dataLineSet != null) {
                maxValue = Math.max(dataLineSet.getMax(position), maxValue);
                minValue = Math.min(dataLineSet.getMin(position), minValue);
            }

            maxPrice = Math.max(data.getHigh(), maxPrice);
            minPrice = Math.min(data.getLow(), minPrice);
            if (maxPrice == data.getHigh())
                mMaxValueIndex = position;
            if (minPrice == data.getLow())
                mMinValueIndex = position;
        }

        float latestPrice = mChart.getAdapter().getLatestPrice();

        maxValue = Math.max(latestPrice, maxValue);
        minValue = Math.min(latestPrice, minValue);
    }

    @Override
    public void fixMaxMin(float diff) {
        float minimum = (maxValue - minValue) / (getMinAxisY() - getMaxAxisY() - diff);
        maxValue += minimum * diff / 2;
        minValue -= minimum * diff / 2;
    }

    public void setMode(@Mode int mode) {
        this.mode = mode;
    }

    public boolean isLine() {
        return mode == Mode.LINE;
    }

    public boolean isCandle() {
        return mode == Mode.CANDLE;
    }
}
