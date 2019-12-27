package com.github.wyang.klinechartlib.huobi;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.github.wyang.klinechartlib.R;
import com.github.wyang.klinechartlib.base.BaseChartView;
import com.github.wyang.klinechartlib.base.IDateFormatter;
import com.github.wyang.klinechartlib.base.IValueFormatter;
import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.formatter.DateFormatter;
import com.github.wyang.klinechartlib.formatter.PercentValueFormatter;
import com.github.wyang.klinechartlib.formatter.PriceFormatter;
import com.github.wyang.klinechartlib.formatter.VolumeFormatter;
import com.github.wyang.klinechartlib.huobi.draw.ChildDraw;
import com.github.wyang.klinechartlib.huobi.draw.MainDraw;
import com.github.wyang.klinechartlib.huobi.helper.LinePathHelper;
import com.github.wyang.klinechartlib.huobi.helper.TextDrawHelper;
import com.github.wyang.klinechartlib.utils.PointFPool;

/**
 * Created by weiyang on 2019-11-04.
 * 高仿火币K线图，界面绘制结构为：烛状图（主图） + 2个指标图（副图，最少显示一个）
 * 这里实现是在一个ViewGroup里画主图及子图，MPAndroidChart每一个图对应一个ViewGroup（使用较为麻烦）
 * 这里仅实现绘制蜡烛图图及添加辅助线，子图的Candle使用美国线绘制
 */
public class KLineChartView extends BaseChartView<KLineChartAdapter> {
    private int mGridRows = 5;//行
    private int mGridColumns = 5;//列
    /**
     * 网格线的画笔
     */
    private Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 绘制蜡烛的画笔，绘制时改变FILL或STROKE、颜色（红涨绿跌或绿涨红跌）
     */
    private Paint mCandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 绘制指标线的画笔（所有指标线使用同一画笔，线的粗细一致）
     */
    private Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 绘制文本的画笔，x轴的时间，y轴的价格，左上角指标值 不包括主图的最高最低及选中时的文本
     */
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 高亮文本画笔（最大最小值，选中值）,size略大于mTextPaint
     */
    private Paint mHighLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 绘制选中的值
     */
    private Paint mSelectedYLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 最新价基准线（最后一个数据的收盘价）
     */
    private Paint mBaselinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 画圆的画笔（分时图的最新价，选中默一点时）
     */
    private Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 绘制在View中的文字的背景边框画笔
     */
    private Paint mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 描边的画笔
     */
    private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF tempRect = new RectF();

    private float mCandleWidth;//蜡烛的宽=mPointWidth - 1dp
    private float mCandleLineWidth;//蜡烛影线的宽

    private boolean isDrawGridStartEnd;

    @ColorInt
    private int axisTextColor;
    private int axisTextPadding;

    private int increaseFill = 1 << 1;
    private int decreaseFill = 1;
    private int fillMode = increaseFill | decreaseFill;
    @ColorInt
    private int colorIncrease;//涨的颜色
    @ColorInt
    private int colorDecrease;//跌的颜色
    @ColorInt
    private int colorCandle;//子图Candle 颜色 参考腾讯自选股

    @ColorInt
    private int baselineColorDefault;//收盘线默认色，最后一个数据不可见时
    @ColorInt
    private int baselineColorHighlight;//收盘线高亮色，最后一个数据完全滑出时

    private TextDrawHelper mTextDrawHelper;

    private LinePathHelper mLinePathHelper;
    private MainDraw mMainDraw;
    private ChildDraw mChildDraw1;
    private ChildDraw mChildDraw2;

    private boolean isBottomAxisX = true;//x轴绘制于View的底部，反之则在主图底部
    private boolean isShowChild2 = true;//是否显示副图

    private IValueFormatter mPriceFormatter;
    private IValueFormatter mVolumeFormatter;
    private IValueFormatter mPercentFormatter;
    private IDateFormatter mDateFormatter;

    private ValueAnimator mBeatAnimator;

    private RadialGradient circleGradient;

    public KLineChartView(Context context) {
        this(context, null);
    }

    public KLineChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(context, attrs);

        axisTextPadding = (int) dp2px(5);

        mTextDrawHelper = new TextDrawHelper();

        mLinePathHelper = new LinePathHelper();

        mMainDraw = new MainDraw(this, mLinePathHelper);
        mChildDraw1 = new ChildDraw(this, mLinePathHelper);
        mChildDraw2 = new ChildDraw(this, mLinePathHelper);

        mMainDraw.setTopSpacing(getTopSpacing() * 2 + getAxisTextPadding());
        mChildDraw1.setTopSpacing(getTopSpacing());
        mChildDraw2.setTopSpacing(getTopSpacing());

        mLinePaint.setStyle(Paint.Style.STROKE);

        mBaselinePaint.setStyle(Paint.Style.STROKE);
        mBaselinePaint.setPathEffect(new DashPathEffect(new float[]{20, 10}, 0));

        mSelectedYLinePaint.setStrokeWidth(mPointWidth);
        mSelectedYLinePaint.setColor(0xffaaaaaa);

        mFramePaint.setColor(0xff081928);

        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(dp2px(.5f));
        mStrokePaint.setColor(0xffffffff);

        mCirclePaint.setColor(0xffffffff);

        mBeatAnimator = ValueAnimator.ofFloat(0, 1);
        mBeatAnimator.setDuration(500);
        mBeatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mBeatAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mBeatAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        mBeatAnimator.start();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KLineChartView);

            setGridLineWidth(a.getDimension(R.styleable.KLineChartView_kc_grid_line_width, getDimension(R.dimen.chart_grid_line_width)));
            setGridLineColor(a.getColor(R.styleable.KLineChartView_kc_grid_line_color, getColor(R.color.chart_grid_line)));

            setPointWidth(a.getDimension(R.styleable.KLineChartView_kc_point_width, getDimension(R.dimen.chart_point_width)));

            setCandleWidth(a.getDimension(R.styleable.KLineChartView_kc_candle_width, getDimension(R.dimen.chart_candle_width)));
            setCandleLineWidth(a.getDimension(R.styleable.KLineChartView_kc_candle_line_width, getDimension(R.dimen.chart_candle_line_width)));

            setColorIncrease(a.getColor(R.styleable.KLineChartView_kc_increase_color, getColor(R.color.chart_red)));
            setColorDecrease(a.getColor(R.styleable.KLineChartView_kc_decrease_color, getColor(R.color.chart_green)));

            setColorCandle(a.getColor(R.styleable.KLineChartView_kc_candle_color, getColor(R.color.chart_close_line)));

            setLineWidth(a.getDimension(R.styleable.KLineChartView_kc_line_width, getDimension(R.dimen.chart_line_width)));
            setBaselineWidth(a.getDimension(R.styleable.KLineChartView_kc_baseline_width, getDimension(R.dimen.chart_baseline_width)));
            setBaselineColorDefault(a.getColor(R.styleable.KLineChartView_kc_baseline_color_default, getColor(R.color.chart_baseline_color_default)));
            setBaselineColorHighlight(a.getColor(R.styleable.KLineChartView_kc_baseline_color_default, getColor(R.color.chart_baseline_color_highlight)));

            setAxisTextColor(a.getColor(R.styleable.KLineChartView_kc_text_color, getColor(R.color.chart_text)));
            setTextSize(a.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)));

            setHighlightColor(a.getColor(R.styleable.KLineChartView_kc_highlight_color, getColor(R.color.chart_highlight)));
            setHighlightSize(a.getDimension(R.styleable.KLineChartView_kc_highlight_size, getDimension(R.dimen.chart_highlight_size)));

            a.recycle();
        }
    }

    @Override
    protected boolean isLine() {
        return mMainDraw.isLine();
    }

    public void initChartPart() {
        float mGridHeight = mHeight - mMainDraw.getTopSpacing() - getAxisXHeight();

        float avg = mGridHeight * 1f / mGridRows;

        float left = 0;
        float top = 0;
        float right = mWidth;
        float bottom = top + (mGridRows - (isShowChild2 ? 2 : 1)) * avg + mMainDraw.getTopSpacing();
        mMainDraw.setBounds(left, top, right, bottom);

        top = mMainDraw.getBottom() + (!isBottomAxisX ? getAxisXHeight() : 0);
        bottom = top + avg;
        mChildDraw1.setBounds(left, top, right, bottom);

        if (isShowChild2) {
            top = mChildDraw1.getBottom();
            bottom = top + avg;
            mChildDraw2.setBounds(left, top, right, bottom);
        }
    }

    @Override
    protected float getMinScaleX() {
        return mCandleLineWidth * 2 / mPointWidth;
    }

    @Override
    protected float getMaxScaleX() {
        return super.getMaxScaleX();
    }

    @Override
    protected void onScaleChanged(float oldScale) {
        mCandleWidth = mPointWidth * mScaleX - mCandleLineWidth;
        mSelectedYLinePaint.setStrokeWidth(mPointWidth * mScaleX);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        log("----------------onSizeChanged()----------------");
        setOverScrollRange(mWidth * 1f / mGridColumns);

        initChartPart();

        //scrollTo(0, 0);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawGrid(canvas);

        if (mAdapter.getCount() == 0)
            return;

        calculateValue();

        mMainDraw.draw(canvas);
        mChildDraw1.draw(canvas);

        if (isShowChild2)
            mChildDraw2.draw(canvas);

        //绘制坐标轴文本
        drawAxisText(canvas);

        //绘制最新价基准线
        drawBaseLine(canvas);

        //长按选中点
        drawLongPressStatus(canvas);
    }

    /**
     * 计算当前的显示区域
     */
    private void calculateValue() {
        if (!isLongPress()) {
            mSelectedIndex = -1;
        }

        mStartIndex = indexOfX(drawX2X(0));
        mEndIndex = indexOfX(drawX2X(mWidth));

        for (int i = mStartIndex; i <= mEndIndex; i++) {
            mMainDraw.calcMinMax(i, i == mStartIndex);

            mChildDraw1.calcMinMax(i, i == mStartIndex);

            mChildDraw2.calcMinMax(i, i == mStartIndex);
        }

        mMainDraw.fixMaxMin(mTextDrawHelper.getTextHeight(mHighLightPaint));
        //mChildDraw1.fixMaxMin(mLinePaint.getStrokeWidth());
        mChildDraw2.fixMaxMin(mLinePaint.getStrokeWidth());
    }

    private void drawGrid(Canvas canvas) {
        //横向的grid
        canvas.drawLine(0, mMainDraw.getTop(), mWidth, mMainDraw.getTop(), mGridPaint);
        float rowSpace = 1.0f * (mMainDraw.getMinAxisY() - mMainDraw.getMaxAxisY()) / (mGridRows - (isShowChild2 ? 2 : 1));
        for (int i = 0; i <= mGridRows - (isShowChild2 ? 2 : 1); i++) {
            canvas.drawLine(0, rowSpace * i + mMainDraw.getMaxAxisY(), mWidth, rowSpace * i + mMainDraw.getMaxAxisY(), mGridPaint);
        }
        //-----------------------下方子图------------------------
        if (!isBottomAxisX)
            canvas.drawLine(0, mChildDraw1.getTop(), mWidth, mChildDraw1.getTop(), mGridPaint);
        canvas.drawLine(0, mChildDraw1.getMinAxisY(), mWidth, mChildDraw1.getMinAxisY(), mGridPaint);
        if (isShowChild2)
            canvas.drawLine(0, mChildDraw2.getMinAxisY(), mWidth, mChildDraw2.getMinAxisY(), mGridPaint);

        //纵向的grid
        float columnSpace = 1.0f * mWidth / mGridColumns;
        for (int i = isDrawGridStartEnd ? 0 : 1; i <= (isDrawGridStartEnd ? mGridColumns : mGridColumns - 1); i++) {
            canvas.drawLine(columnSpace * i, 0, columnSpace * i, mMainDraw.getMinAxisY(), mGridPaint);
            canvas.drawLine(columnSpace * i, mChildDraw1.getTop(), columnSpace * i, mChildDraw1.getBottom(), mGridPaint);
            if (isShowChild2)
                canvas.drawLine(columnSpace * i, mChildDraw2.getTop(), columnSpace * i, mChildDraw2.getBottom(), mGridPaint);
        }
    }

    /**
     * 绘制蜡烛矩形
     */
    public void drawCandle(Canvas canvas, float x, float top, float bottom, boolean isIncrease) {
        tempRect.set(x - mCandleWidth * .5f, top, x + mCandleWidth * .5f, bottom);
        if (isIncrease) {
            mCandlePaint.setStyle(getIncreaseFill() ? Paint.Style.FILL : Paint.Style.STROKE);
        } else {
            mCandlePaint.setStyle(getDecreaseFill() ? Paint.Style.FILL : Paint.Style.STROKE);
        }
        mCandlePaint.setColor(isIncrease ? colorIncrease : colorDecrease);
        canvas.drawRect(tempRect, mCandlePaint);
    }

    /**
     * 绘制蜡烛图上下影线
     */
    public void drawCandleLine(Canvas canvas, float x, float startY, float stopY) {
        mCandlePaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(x, startY, x, stopY, mCandlePaint);
    }

    /**
     * 子图美国线的绘制
     */
    public void drawAmericanLine(Canvas canvas, float x, float[] arrY) {
        mLinePaint.setColor(colorCandle);
        canvas.drawLine(x, arrY[2], x, arrY[3], mLinePaint);
        canvas.drawLine(x - mCandleWidth * .5f, arrY[0], x, arrY[0], mLinePaint);
        canvas.drawLine(x + mCandleWidth * .5f, arrY[1], x, arrY[1], mLinePaint);
    }

    /**
     * macd蜡烛图始终为实心柱子
     */
    public void drawFillCandle(Canvas canvas, float x, float top, float bottom, boolean isRise) {
        tempRect.set(x - mCandleWidth * .5f, top, x + mCandleWidth * .5f, bottom);
        mCandlePaint.setStyle(Paint.Style.FILL);
        mCandlePaint.setColor(isRise ? colorIncrease : colorDecrease);
        canvas.drawRect(tempRect, mCandlePaint);
    }

    /**
     * sar指标绘制圆圈
     */
    public void drawCircle(Canvas canvas, float x, float y, boolean isIncrease) {
        mCandlePaint.setStyle(Paint.Style.STROKE);
        mCandlePaint.setColor(isIncrease ? colorIncrease : colorDecrease);
        canvas.drawCircle(x, y, 5, mCandlePaint);
    }

    /**
     * 绘制指标线
     */
    public void drawLinePath(Canvas canvas, Path path, @ColorInt int color) {
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(color);
        mLinePaint.setShader(null);

        canvas.drawPath(path, mLinePaint);
    }

    /**
     * 绘制收盘线（分时线）
     */
    public void drawFillPath(Canvas canvas, Path path, @ColorInt int color, Shader shader) {
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setColor(color);
        mLinePaint.setShader(shader);

        canvas.drawPath(path, mLinePaint);
    }

    public void drawText(Canvas canvas, String text, PointF p, @ColorInt int color) {
        mTextPaint.setColor(color);
        mTextDrawHelper.drawPointRightBot(canvas, text, p, mTextPaint);
    }

    /**
     * 画坐标轴文本
     */
    private void drawAxisText(Canvas canvas) {
        mTextPaint.setColor(axisTextColor);
        //点对象池取一个PointF对象，用于确定文本位置
        PointF p = PointFPool.get(0, 0);
        //画x轴文本
        float columnSpace = 1f * mWidth / mGridColumns;
        for (int i = 0; i <= mGridColumns; i++) {
            float translateX = drawX2X(columnSpace * i);
            //最右侧设置可滑出距离并且最后一个数据不在最右侧时，不绘制时间
            if (translateX > mDataLength)
                continue;
            int index = indexOfX(translateX);
            String text = getDateFormatter().format(mAdapter.getCandle(index).getTime());

            p.x = columnSpace * i;
            p.y = isBottomAxisX ? mChildDraw2.getBottom() : mMainDraw.getBottom();
            mTextDrawHelper.drawPointBot(canvas, text, p, mTextPaint);
        }

        //画y轴 主图文本
        float rowValue = (mMainDraw.getMaxValue() - mMainDraw.getMinValue()) / (mGridRows - 2);
        float rowSpace = 1.0f * (mMainDraw.getMinAxisY() - mMainDraw.getMaxAxisY()) / (mGridRows - 2);
        for (int i = 0; i <= mGridRows - 2; i++) {
            String text = getPriceFormatter().format(rowValue * (mGridRows - 2 - i) + mMainDraw.getMinValue());

            p.x = mWidth - axisTextPadding;
            p.y = mMainDraw.getMaxAxisY() + rowSpace * i;
            mTextDrawHelper.drawPointLeftTop(canvas, text, p, mTextPaint);
        }

        //画y轴 成交量文本
        String text = getVolumeFormatter().format(mChildDraw1.getMaxValue());
        p.x = mWidth - axisTextPadding;
        p.y = mChildDraw1.getMaxAxisY();
        mTextDrawHelper.drawPointLeftTop(canvas, text, p, mTextPaint);

        PointFPool.recycle(p);
    }

    /**
     * 绘制最新价基准线
     */
    public void drawBaseLine(Canvas canvas) {
        //绘制最新价基准线
        float latestPrice = mAdapter.getLatestPrice();
        float closeY = mMainDraw.getAxisY(latestPrice);
        //最新价字符串
        String text = getPriceFormatter().format(latestPrice);

        float w = mTextPaint.measureText(text) + axisTextPadding * 2;
        if (mWidth - w > getDrawX(mEndIndex) + mPointWidth / 2) {
            mBaselinePaint.setColor(baselineColorHighlight);
            canvas.drawLine(getDrawX(mEndIndex), closeY, mWidth, closeY, mBaselinePaint);

            //分时线时，绘制小圆点
            if (mMainDraw.isLine() && isLastVisible()) {
                float x = getDrawX(mEndIndex);

                circleGradient = new RadialGradient(x, closeY, 1.5f * mPointWidth, 0x66ffffff, 0x00ffffff, Shader.TileMode.CLAMP);
                float per = (float) mBeatAnimator.getAnimatedValue();
                mCirclePaint.setShader(circleGradient);
                mCirclePaint.setAlpha((int) (0xff * per));
                canvas.drawCircle(x, closeY, 1.5f * mPointWidth, mCirclePaint);

                mCirclePaint.setShader(null);
                mCirclePaint.setAlpha(0xff);
                canvas.drawCircle(x, closeY, 5, mCirclePaint);
            }

            float left = mWidth - w;
            float top = closeY - mTextDrawHelper.getTextHeight(mTextPaint) / 2;
            float right = mWidth;
            float bottom = closeY + mTextDrawHelper.getTextHeight(mTextPaint) / 2;
            tempRect.set(left, top, right, bottom);
            canvas.drawRect(tempRect, mFramePaint);

            mTextPaint.setColor(mBaselinePaint.getColor());
            PointF pointF = PointFPool.get(tempRect.centerX(), tempRect.centerY());
            mTextDrawHelper.drawPointCenter(canvas, text, pointF, mTextPaint);
            PointFPool.recycle(pointF);
        } else {
            mBaselinePaint.setColor(baselineColorDefault);
            canvas.drawLine(0, closeY, mWidth, closeY, mBaselinePaint);

            //画最后一个收盘价
            text += " ▶";
            w = mHighLightPaint.measureText(text) + axisTextPadding * 2;
            float radius = mTextDrawHelper.getTextHeight(mHighLightPaint) / 2;
            float left = (mWidth - w) * 4.0f / 5;
            float top = closeY - radius;
            float right = left + w;
            float bottom = closeY + radius;

            tempRect.set(left, top, right, bottom);

            canvas.drawRoundRect(tempRect, radius, radius, mFramePaint);
            canvas.drawRoundRect(tempRect, radius, radius, mStrokePaint);

            PointF pointF = PointFPool.get(tempRect.centerX(), tempRect.centerY());
            mTextDrawHelper.drawPointCenter(canvas, text, pointF, mHighLightPaint);
            PointFPool.recycle(pointF);
        }
    }

    /**
     * 长按选择时的绘制逻辑
     */
    private void drawLongPressStatus(Canvas canvas) {
        if (isLongPress()) {
            ICandle candle = mAdapter.getCandle(mSelectedIndex);

            float x = getDrawX(mSelectedIndex);//选中数据对应View的x坐标
            float y = mMainDraw.getAxisY(candle.getClose());//选中数据收盘价对应View的y坐标

            //绘制选择竖线
            mSelectedYLinePaint.setAlpha(0x33);
            LinearGradient shader = new LinearGradient(x, mMainDraw.getMaxAxisY(), x, mChildDraw2.getMinAxisY(),
                    new int[]{0x11ffffff, 0xffffffff, 0x11ffffff}, new float[]{0, .5f, 1f}, Shader.TileMode.CLAMP);
            mSelectedYLinePaint.setShader(shader);
            canvas.drawLine(x, mMainDraw.getMaxAxisY(), x, mHeight, mSelectedYLinePaint);

            //选中收盘价的大圆带透明
            mSelectedYLinePaint.setAlpha(0x44);
            mSelectedYLinePaint.setShader(null);
            canvas.drawCircle(x, y, 1.5f * mPointWidth, mSelectedYLinePaint);

            //选中收盘价的x轴方向基准线及实心小圆点
            canvas.drawLine(0, y, mWidth, y, mStrokePaint);
            canvas.drawCircle(x, y, 5, mCirclePaint);

            float padding = dp2px(5);

            //当前选中数据的收盘价
            String text = getPriceFormatter().format(candle.getClose());

            float halfHeight = mTextDrawHelper.getTextHeight(mHighLightPaint) / 2;
            float textWidth = mHighLightPaint.measureText(text);
            float halfStroke = mStrokePaint.getStrokeWidth() / 2;
            if (getDrawX(mSelectedIndex) < mWidth / 2) {
                Path path = new Path();
                path.moveTo(halfStroke, y - halfHeight);
                path.lineTo(halfStroke, y + halfHeight);
                path.lineTo(textWidth + 2 * padding, y + halfHeight);
                path.lineTo(textWidth + 2 * padding + halfHeight, y);
                path.lineTo(textWidth + 2 * padding, y - halfHeight);
                path.close();
                canvas.drawPath(path, mFramePaint);
                canvas.drawPath(path, mStrokePaint);

                PointF pointF = PointFPool.get(textWidth / 2 + padding, y);
                mTextDrawHelper.drawPointCenter(canvas, text, pointF, mHighLightPaint);
                PointFPool.recycle(pointF);
            } else {
                float temp = mWidth - textWidth - halfStroke - 2 * padding - halfHeight;
                Path path = new Path();
                path.moveTo(temp, y);
                path.lineTo(temp + halfHeight, y + halfHeight);
                path.lineTo(mWidth - halfStroke, y + halfHeight);
                path.lineTo(mWidth - halfStroke, y - halfHeight);
                path.lineTo(temp + halfHeight, y - halfHeight);
                path.close();
                canvas.drawPath(path, mFramePaint);
                canvas.drawPath(path, mStrokePaint);

                PointF pointF = PointFPool.get(mWidth - textWidth / 2 - padding, y);
                mTextDrawHelper.drawPointCenter(canvas, text, pointF, mHighLightPaint);
                PointFPool.recycle(pointF);
            }

            //当前选中数据时间
            String date = getDateFormatter().format(candle.getTime());
            if (!isBottomAxisX) {
                y = mMainDraw.getMinAxisY();
            } else {
                y = mChildDraw2.getMinAxisY();
            }
            float dateWidth = mHighLightPaint.measureText(date);
            if (x < dateWidth + 2 * padding) {
                x = halfStroke + dateWidth / 2 + padding;
            } else if (mWidth - x < dateWidth + 2 * padding) {
                x = mWidth - halfStroke - dateWidth / 2 - padding;
            }
            float left = x - dateWidth / 2 - padding;
            float top = y + halfStroke;
            float right = x + dateWidth / 2 + padding;
            float bottom = y + mTextDrawHelper.getTextHeight(mHighLightPaint) - halfStroke;
            tempRect.set(left, top, right, bottom);
            canvas.drawRect(tempRect, mFramePaint);
            canvas.drawRect(tempRect, mStrokePaint);

            PointF p = PointFPool.get(tempRect.centerX(), tempRect.centerY());
            mTextDrawHelper.drawPointCenter(canvas, date, p, mHighLightPaint);
            PointFPool.recycle(p);

             /*if (!mMainDraw.isLine()) {
                float width = 0;
                float height;
                top = padding + mMainDraw.getMaxAxisY();

                List<String> labels = new ArrayList<>();
                labels.add(getString(R.string.chart_date));
                labels.add(getString(R.string.chart_open));
                labels.add(getString(R.string.chart_high));
                labels.add(getString(R.string.chart_low));
                labels.add(getString(R.string.chart_close));
                labels.add(getString(R.string.chart_change_value));
                labels.add(getString(R.string.chart_change_percent));
                labels.add(getString(R.string.chart_amount));

                float change = candle.getClose() - candle.getOpen();
                float percent = change / candle.getClose();

                List<String> values = new ArrayList<>();
                values.add(getDateFormatter().format(candle.getTime()));
                values.add(getPriceFormatter().format(candle.getOpen()));
                values.add(getPriceFormatter().format(candle.getHigh()));
                values.add(getPriceFormatter().format(candle.getLow()));
                values.add(getPriceFormatter().format(candle.getClose()));
                values.add(getPriceFormatter().format(change));
                values.add(getPercentFormatter().format(percent));
                values.add(getVolumeFormatter().format(candle.getVolume()));

                for (int i = 0; i < labels.size(); i++) {
                    String str = labels.get(i) + values.get(i);
                    width = Math.max(width, mTextPaint.measureText(str.replace("-", "+")));
                }

                width += padding * 3;

                if (x > mWidth / 2) {
                    left = padding;
                } else {
                    left = mWidth - width - padding;
                }

                float h = mTextDrawHelper.getTextHeight(mTextPaint);
                height = padding * 2 + h * labels.size();

                tempRect.set(left, top, left + width, top + height);

                mFramePaint.setAlpha(0xaa);
                canvas.drawRoundRect(tempRect, 2, 2, mFramePaint);
                canvas.drawRoundRect(tempRect, 2, 2, mStrokePaint);
                mFramePaint.setAlpha(0xff);


                y = top + padding * 1;

                mTextPaint.setColor(axisTextColor);
                PointF p1 = PointFPool.get(0, 0);
                for (int i = 0; i < labels.size(); i++) {
                    String label = labels.get(i);
                    String value = values.get(i);

                    p1.x = left + padding;
                    p1.y = y;
                    mTextDrawHelper.drawPointRightBot(canvas, label, p1, mTextPaint);

                    p1.x = left + width - mTextPaint.measureText(value) - padding;
                    p1.y = y;
                    mTextDrawHelper.drawPointRightBot(canvas, value, p1, mTextPaint);

                    y += h;
                }
                PointFPool.recycle(p1);
            }*/
        }
    }

    public void setDrawGridStartEnd(boolean drawGridStartEnd) {
        isDrawGridStartEnd = drawGridStartEnd;
    }

    public void setColorIncrease(@ColorInt int color) {
        this.colorIncrease = color;
    }

    public void setColorDecrease(@ColorInt int color) {
        this.colorDecrease = color;
    }

    public void setColorCandle(@ColorInt int color) {
        this.colorCandle = color;
    }

    public void setAxisTextColor(@ColorInt int color) {
        this.axisTextColor = color;
    }

    public void setAxisTextPadding(int padding) {
        this.axisTextPadding = padding;
    }

    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
        mBaselinePaint.setTextSize(textSize);
    }

    public void setHighlightColor(@ColorInt int color) {
        mHighLightPaint.setColor(color);
    }

    public void setHighlightSize(float textSize) {
        mHighLightPaint.setTextSize(textSize);
    }

    public void setGridLineWidth(float lineSize) {
        mGridPaint.setStrokeWidth(lineSize);
    }

    public void setGridLineColor(@ColorInt int color) {
        mGridPaint.setColor(color);
    }

    public void setLineWidth(float lineWidth) {
        mLinePaint.setStrokeWidth(lineWidth);
    }

    public void setBaselineWidth(float baselineWidth) {
        mBaselinePaint.setStrokeWidth(baselineWidth);
    }

    public void setBaselineColorDefault(@ColorInt int color) {
        this.baselineColorDefault = color;
    }

    public void setBaselineColorHighlight(@ColorInt int color) {
        this.baselineColorHighlight = color;
    }

    /**
     * 设置蜡烛宽度
     */
    public void setCandleWidth(float candleWidth) {
        this.mCandleWidth = candleWidth;
    }

    /**
     * 设置蜡烛线(影线)宽度
     */
    public void setCandleLineWidth(float candleLineWidth) {
        this.mCandleLineWidth = candleLineWidth;

        mCandlePaint.setStrokeWidth(mCandleLineWidth);
    }

    /**
     * 设置蜡烛空心模式
     *
     * @param fillRst null-涨跌均为实心，true-涨实心跌空心，false-涨空心跌实心
     */
    public void setCandleFill(Boolean fillRst) {
        if (fillRst == null)
            fillMode = increaseFill | decreaseFill;
        else if (fillRst)
            fillMode = increaseFill;
        else
            fillMode = decreaseFill;

        postInvalidate();
    }

    public boolean getIncreaseFill() {
        return (fillMode & increaseFill) == increaseFill;
    }

    public boolean getDecreaseFill() {
        return (fillMode & decreaseFill) == decreaseFill;
    }

    public Paint getGridPaint() {
        return mGridPaint;
    }

    public Paint getTextPaint() {
        return mTextPaint;
    }

    public Paint getHighLightPaint() {
        return mHighLightPaint;
    }

    public Paint getSelectedYLinePaint() {
        return mSelectedYLinePaint;
    }

    public Paint getBaselinePaint() {
        return mBaselinePaint;
    }

    public TextDrawHelper getTextDrawHelper() {
        return mTextDrawHelper;
    }

    public float getTopSpacing() {
        return mTextDrawHelper.getTextHeight(mTextPaint);
    }

    public void setBottomAxisX(boolean bottomAxisX) {
        isBottomAxisX = bottomAxisX;
    }

    public void setShowChild2(boolean isShowChild2) {
        this.isShowChild2 = isShowChild2;

        initChartPart();
    }

    /**
     * x轴的高度，高亮文本画笔textSize略大于默认文本画笔的textSize
     */
    public float getAxisXHeight() {
        return mTextDrawHelper.getTextHeight(mHighLightPaint);
    }

    public float getDefaultTextHeight() {
        return mTextDrawHelper.getTextHeight(mTextPaint);
    }

    public int getAxisTextPadding() {
        return axisTextPadding;
    }

    public void setMode(@MainDraw.Mode int mode) {
        mMainDraw.setMode(mode);

        float oldScaleX = mScaleX;
        if (isLine())
            mScaleX = 0.7f;
        else
            mScaleX = 1.0f;
        onScaleChanged(oldScaleX);

        mAdapter.notifyDataSetChanged();
    }

    public void setMainSelected(String name) {
        mMainDraw.setName(name);

        mAdapter.notifyDataSetInvalidated();
    }

    public void setChild1Selected(String name) {
        mChildDraw1.setName(name);

        mAdapter.notifyDataSetInvalidated();
    }

    public void setChild2Selected(String name) {
        mChildDraw2.setName(name);

        mAdapter.notifyDataSetInvalidated();
    }

    public IValueFormatter getPriceFormatter() {
        if (mPriceFormatter == null)
            mPriceFormatter = new PriceFormatter();
        return mPriceFormatter;
    }

    public void setPriceFormatter(IValueFormatter mPriceFormatter) {
        this.mPriceFormatter = mPriceFormatter;
    }

    public IValueFormatter getVolumeFormatter() {
        if (mVolumeFormatter == null)
            mVolumeFormatter = new VolumeFormatter();
        return mVolumeFormatter;

    }

    public void setVolumeFormatter(IValueFormatter mVolumeFormatter) {
        this.mVolumeFormatter = mVolumeFormatter;
    }

    public IValueFormatter getPercentFormatter() {
        if (mPercentFormatter == null)
            mPercentFormatter = new PercentValueFormatter();
        return mPercentFormatter;
    }

    public void setPercentFormatter(IValueFormatter mPercentFormatter) {
        this.mPercentFormatter = mPercentFormatter;
    }

    public IDateFormatter getDateFormatter() {
        if (mDateFormatter == null)
            mDateFormatter = new DateFormatter("MM-dd HH:mm");
        return mDateFormatter;
    }

    public void setDateFormatter(IDateFormatter mDateFormatter) {
        this.mDateFormatter = mDateFormatter;
    }
}
