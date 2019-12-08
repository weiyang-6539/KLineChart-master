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
import com.github.wyang.klinechartlib.base.ICandle;
import com.github.wyang.klinechartlib.formatter.DateFormatter;
import com.github.wyang.klinechartlib.formatter.PriceFormatter;
import com.github.wyang.klinechartlib.formatter.VolumeFormatter;
import com.github.wyang.klinechartlib.huobi.draw.ChildRect1;
import com.github.wyang.klinechartlib.huobi.draw.ChildRect2;
import com.github.wyang.klinechartlib.huobi.draw.MainRect;
import com.github.wyang.klinechartlib.huobi.helper.LinePathHelper;
import com.github.wyang.klinechartlib.huobi.helper.TextDrawHelper;
import com.github.wyang.klinechartlib.utils.PointFPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weiyang on 2019-11-04.
 * 高仿火币K线图，界面绘制结构为：烛状图（主图） + 2个指标图（副图，最少显示一个）
 * 这里实现是在一个ViewGroup里画主图及子图，MPAndroidChart每一个图对应一个ViewGroup（使用较为麻烦）
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
    private Paint mBaseLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

    private float mCandleWidth;//蜡烛的宽
    private float mCandleLineWidth;//蜡烛影线的宽

    @ColorInt
    private int axisTextColor;
    private int axisYTextPadding;

    private int riseFill = 1 << 1;
    private int fallFill = 1;
    private int fillMode = riseFill | fallFill;
    @ColorInt
    private int colorRise;//默认涨的颜色
    @ColorInt
    private int colorFall;//默认跌的颜色

    private TextDrawHelper mTextDrawHelper;

    private LinePathHelper mLinePathHelper;
    private MainRect mMainRect;
    private ChildRect1 mChildRect1;
    private ChildRect2 mChildRect2;

    private boolean isBottomAxisX = true;//x轴绘制于View的底部，反之则在主图底部
    private boolean isShowChild2 = true;//是否显示副图

    private IValueFormatter mPriceFormatter;
    private IDateFormatter mDateFormatter;
    private IValueFormatter mVolumeFormatter;

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

        axisYTextPadding = (int) dp2px(5);

        mTextDrawHelper = new TextDrawHelper();

        mLinePathHelper = new LinePathHelper();

        mMainRect = new MainRect(this, mLinePathHelper);
        mChildRect1 = new ChildRect1(this, mLinePathHelper);
        mChildRect2 = new ChildRect2(this, mLinePathHelper);

        mMainRect.setTopSpacing(getTopSpacing() * 2);
        mChildRect1.setTopSpacing(getTopSpacing());
        mChildRect2.setTopSpacing(getTopSpacing());

        mLinePaint.setStyle(Paint.Style.STROKE);

        mBaseLinePaint.setStyle(Paint.Style.STROKE);
        mBaseLinePaint.setPathEffect(new DashPathEffect(new float[]{20, 10}, 0));

        mSelectedYLinePaint.setStrokeWidth(mPointWidth);
        mSelectedYLinePaint.setColor(0xffaaaaaa);

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
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KLineChartView);

            setPointWidth(typedArray.getDimension(R.styleable.KLineChartView_kc_item_width, getDimension(R.dimen.chart_point_width)));

            setCandleWidth(typedArray.getDimension(R.styleable.KLineChartView_kc_candle_width, getDimension(R.dimen.chart_candle_width)));
            setCandleLineWidth(typedArray.getDimension(R.styleable.KLineChartView_kc_candle_line_width, getDimension(R.dimen.chart_candle_line_width)));

            setGridLineWidth(typedArray.getDimension(R.styleable.KLineChartView_kc_grid_line_width, getDimension(R.dimen.chart_grid_line_width)));
            setGridLineColor(typedArray.getColor(R.styleable.KLineChartView_kc_grid_line_color, getColor(R.color.chart_grid_line)));

            setLineWidth(typedArray.getDimension(R.styleable.KLineChartView_kc_line_width, getDimension(R.dimen.chart_line_width)));

            setAxisTextColor(typedArray.getColor(R.styleable.KLineChartView_kc_text_color, getColor(R.color.chart_text)));
            setTextSize(typedArray.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)));

            setHighlightColor(typedArray.getColor(R.styleable.KLineChartView_kc_highlight_color, getColor(R.color.chart_highlight)));
            setHighlightSize(typedArray.getDimension(R.styleable.KLineChartView_kc_highlight_size, getDimension(R.dimen.chart_highlight_size)));

            setColorRise(getColor(R.color.chart_red));
            setColorFall(getColor(R.color.chart_green));

            typedArray.recycle();
        }
    }

    @Override
    protected boolean isLine() {
        return mMainRect.isLine();
    }

    public void initPartRect() {
        float mGridHeight = mHeight - mMainRect.getTopSpacing() - getAxisXHeight();

        float avg = mGridHeight * 1f / mGridRows;

        float left = 0;
        float top = 0;
        float right = mWidth;
        float bottom = top + (mGridRows - (isShowChild2 ? 2 : 1)) * avg + mMainRect.getTopSpacing();
        mMainRect.setBounds(left, top, right, bottom);

        top = mMainRect.getBottom() + (!isBottomAxisX ? getAxisXHeight() : 0);
        bottom = top + avg;
        mChildRect1.setBounds(left, top, right, bottom);

        if (isShowChild2) {
            top = mChildRect1.getBottom();
            bottom = top + avg;
            mChildRect2.setBounds(left, top, right, bottom);
        }
    }

    @Override
    protected void onScaleChanged(float oldScale) {
        super.onScaleChanged(oldScale);

        mSelectedYLinePaint.setStrokeWidth(mPointWidth * mScaleX);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        log("----------------onSizeChanged()----------------");
        setOverScrollRange(mWidth * 1f / mGridColumns);

        initPartRect();

        //scrollTo(0, 0);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        calculateValue();

        drawGrid(canvas);

        mMainRect.draw(canvas);
        mChildRect1.draw(canvas);

        if (isShowChild2)
            mChildRect2.draw(canvas);

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

        mStartIndex = indexOfTranslateX(x2TranslateX(0));
        mEndIndex = indexOfTranslateX(x2TranslateX(mWidth));

        mMainRect.resetMaxMinValue();
        mChildRect1.resetMaxMinValue();
        mChildRect2.resetMaxMinValue();

        for (int i = mStartIndex; i <= mEndIndex; i++) {
            ICandle point = getAdapter().getCandle(i);
            if (mMainRect != null) {
                mMainRect.updateMaxMinValue(point, i);

                mChildRect1.updateMaxMinValue(point, i);

                mChildRect2.updateMaxMinValue(point, i);
            }
        }

        if (mMainRect != null)
            mMainRect.fixMaxMin();

    }

    private void drawGrid(Canvas canvas) {
        //横向的grid
        float rowSpace = 1.0f * (mMainRect.getMinAxisY() - mMainRect.getMaxAxisY()) / (mGridRows - 2);
        for (int i = 0; i <= mGridRows - (isShowChild2 ? 2 : 1); i++) {
            canvas.drawLine(0, rowSpace * i + mMainRect.getMaxAxisY(), mWidth, rowSpace * i + mMainRect.getMaxAxisY(), mGridPaint);
        }
        //-----------------------下方子图------------------------
        if (!isBottomAxisX)
            canvas.drawLine(0, mChildRect1.getTop(), mWidth, mChildRect1.getTop(), mGridPaint);
        canvas.drawLine(0, mChildRect1.getMinAxisY(), mWidth, mChildRect1.getMinAxisY(), mGridPaint);
        if (isShowChild2)
            canvas.drawLine(0, mChildRect2.getMinAxisY(), mWidth, mChildRect2.getMinAxisY(), mGridPaint);

        //纵向的grid
        float columnSpace = 1.0f * mWidth / mGridColumns;
        for (int i = 1; i < mGridColumns; i++) {
            canvas.drawLine(columnSpace * i, 0, columnSpace * i, mMainRect.getMinAxisY(), mGridPaint);
            canvas.drawLine(columnSpace * i, mChildRect1.getTop(), columnSpace * i, mChildRect1.getBottom(), mGridPaint);
            if (isShowChild2)
                canvas.drawLine(columnSpace * i, mChildRect2.getTop(), columnSpace * i, mChildRect2.getBottom(), mGridPaint);
        }
    }

    /**
     * 绘制主图蜡烛图上下影线
     */
    public void drawCandleLine(Canvas canvas, float x, float startY, float stopY) {
        mCandlePaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(x, startY, x, stopY, mCandlePaint);
    }

    public void drawCandle(Canvas canvas, float x, float top, float bottom, boolean isRise) {
        tempRect.set(x - mCandleWidth * mScaleX * .5f, top, x + mCandleWidth * mScaleX * .5f, bottom);
        if (isRise) {
            mCandlePaint.setStyle(isRiseFill() ? Paint.Style.FILL : Paint.Style.STROKE);
        } else {
            mCandlePaint.setStyle(isFallFill() ? Paint.Style.FILL : Paint.Style.STROKE);
        }
        mCandlePaint.setColor(isRise ? colorRise : colorFall);
        canvas.drawRect(tempRect, mCandlePaint);
    }

    /**
     * macd蜡烛图始终为实心柱子
     */
    public void drawFillCandle(Canvas canvas, float x, float top, float bottom, boolean isRise) {
        tempRect.set(x - mCandleWidth / 2, top, x + mCandleWidth / 2, bottom);
        mCandlePaint.setStyle(Paint.Style.FILL);
        mCandlePaint.setColor(isRise ? colorRise : colorFall);
        canvas.drawRect(tempRect, mCandlePaint);
    }

    public void drawLinePath(Canvas canvas, Path path, @ColorInt int color) {
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(color);
        mLinePaint.setShader(null);

        canvas.drawPath(path, mLinePaint);
    }

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
        PointF p = PointFPool.get(0, 0);


        //画x轴文本
        float columnSpace = 1f * mWidth / mGridColumns;
        for (int i = 0; i <= mGridColumns; i++) {
            float translateX = x2TranslateX(columnSpace * i);
            //最右侧设置可滑出距离并且最后一个数据不在最右侧时，不绘制时间
            if (translateX > mDataLength)
                continue;
            int index = indexOfTranslateX(translateX);
            String text = getDateFormatter().format(mAdapter.getCandle(index).getTime());

            p.x = columnSpace * i;
            p.y = isBottomAxisX ? mChildRect2.getBottom() : mMainRect.getBottom();
            mTextDrawHelper.drawPointBot(canvas, text, p, mTextPaint);
        }

        //画y轴文本
        float rowValue = (mMainRect.getMaxValue() - mMainRect.getMinValue()) / (mGridRows - 2);
        float rowSpace = 1.0f * (mMainRect.getMinAxisY() - mMainRect.getMaxAxisY()) / (mGridRows - 2);

        for (int i = 0; i <= mGridRows - 2; i++) {
            String text = getPriceFormatter().format(rowValue * (mGridRows - 2 - i) + mMainRect.getMinValue());

            p.x = mWidth - axisYTextPadding;
            p.y = mMainRect.getMaxAxisY() + rowSpace * i;
            mTextDrawHelper.drawPointLeftTop(canvas, text, p, mTextPaint);
        }

        PointFPool.recycle(p);
    }

    /**
     * 绘制最新价基准线
     */
    public void drawBaseLine(Canvas canvas) {
        //绘制最新价基准线
        float latestPrice = mAdapter.getLatestPrice();
        float closeY = mMainRect.getAxisY(latestPrice);
        //最新价字符串
        String text = getPriceFormatter().format(latestPrice);

        float w = mTextPaint.measureText(text) + axisYTextPadding * 2;
        if (mWidth - w > getDrawX(mEndIndex) + mPointWidth / 2) {
            mBaseLinePaint.setColor(0xff5187EC);
            canvas.drawLine(getDrawX(mEndIndex), closeY, mWidth, closeY, mBaseLinePaint);

            //分时线时，绘制小圆点
            if (mMainRect.isLine() && isLastVisible()) {
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

            mTextPaint.setColor(mBaseLinePaint.getColor());
            PointF pointF = PointFPool.get(tempRect.centerX(), tempRect.centerY());
            mTextDrawHelper.drawPointCenter(canvas, text, pointF, mTextPaint);
            PointFPool.recycle(pointF);
        } else {
            mBaseLinePaint.setColor(0xffaaaaaa);
            canvas.drawLine(0, closeY, mWidth, closeY, mBaseLinePaint);

            //画最后一个收盘价
            text += " ▶";
            w = mHighLightPaint.measureText(text) + axisYTextPadding * 2;
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
            float y = mMainRect.getAxisY(candle.getClose());//选中数据收盘价对应View的y坐标

            //绘制选择竖线
            mSelectedYLinePaint.setAlpha(0x33);
            LinearGradient shader = new LinearGradient(x, mMainRect.getMaxAxisY(), x, mChildRect2.getMinAxisY(),
                    new int[]{0x11ffffff, 0xffffffff, 0x11ffffff}, new float[]{0, .5f, 1f}, Shader.TileMode.CLAMP);
            mSelectedYLinePaint.setShader(shader);
            canvas.drawLine(x, mMainRect.getMaxAxisY(), x, mHeight, mSelectedYLinePaint);

            //选中收盘价的大圆带透明
            mSelectedYLinePaint.setAlpha(0x44);
            mSelectedYLinePaint.setShader(null);
            canvas.drawCircle(x, y, 1.5f * mPointWidth, mSelectedYLinePaint);

            //选中收盘价的x轴方向基准线及实心小圆点
            canvas.drawLine(0, y, mWidth, y, mStrokePaint);
            canvas.drawCircle(x, y, 5, mCirclePaint);

            //float w1 = dp2px(3);
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
                y = mMainRect.getMinAxisY();
            } else {
                y = mChildRect2.getMinAxisY();
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

            if (!mMainRect.isLine()) {
                float width = 0;
                top = padding + mMainRect.getMaxAxisY();
                float height;

                List<String> labels = new ArrayList<>();
                labels.add(getString(R.string.chart_date));
                labels.add(getString(R.string.chart_open));
                labels.add(getString(R.string.chart_high));
                labels.add(getString(R.string.chart_low));
                labels.add(getString(R.string.chart_close));
                labels.add(getString(R.string.chart_change_value));
                labels.add(getString(R.string.chart_change_percent));
                labels.add(getString(R.string.chart_amount));

                List<String> values = new ArrayList<>();
                values.add(getDateFormatter().format(candle.getTime()));
                values.add(getPriceFormatter().format(candle.getOpen()));
                values.add(getPriceFormatter().format(candle.getHigh()));
                values.add(getPriceFormatter().format(candle.getLow()));
                values.add(getPriceFormatter().format(candle.getClose()));
                values.add(getPriceFormatter().format(candle.getChangeValue()));
                values.add(candle.getChangePercent());
                values.add(getVolumeFormatter().format(candle.getVolume()));

                for (int i = 0; i < labels.size(); i++) {
                    String str = labels.get(i) + values.get(i);
                    width = Math.max(width, mHighLightPaint.measureText(str.replace("-", "+")));
                }

                width += padding * 3;

                if (x > mWidth / 2) {
                    left = padding;
                } else {
                    left = mWidth - width - padding;
                }

                height = padding * (labels.size() + 1) + halfHeight * 2 * labels.size();

                tempRect.set(left, top, left + width, top + height);
                canvas.drawRoundRect(tempRect, 2, 2, mFramePaint);
                canvas.drawRoundRect(tempRect, 2, 2, mStrokePaint);

                y = top + padding * 1;
                PointF p1 = PointFPool.get(0, 0);
                for (int i = 0; i < labels.size(); i++) {
                    String label = labels.get(i);
                    String value = values.get(i);

                    p1.x = left + padding;
                    p1.y = y;
                    mTextDrawHelper.drawPointRightBot(canvas, label, p1, mHighLightPaint);

                    p1.x = left + width - mHighLightPaint.measureText(value) - padding;
                    p1.y = y;
                    mTextDrawHelper.drawPointRightBot(canvas, value, p1, mHighLightPaint);

                    y += halfHeight * 2 + padding;
                }
                PointFPool.recycle(p1);
            }
        }
    }

    public void setColorRise(@ColorInt int color) {
        this.colorRise = color;
    }

    public void setColorFall(@ColorInt int color) {
        this.colorFall = color;
    }

    public void setAxisTextColor(@ColorInt int color) {
        this.axisTextColor = color;
    }

    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
        mBaseLinePaint.setTextSize(textSize);
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
        mBaseLinePaint.setStrokeWidth(lineWidth);
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
            fillMode = riseFill | fallFill;
        else if (fillRst)
            fillMode = riseFill;
        else
            fillMode = fallFill;

        postInvalidate();
    }

    public boolean isRiseFill() {
        return (fillMode & riseFill) == riseFill;
    }

    public boolean isFallFill() {
        return (fillMode & fallFill) == fallFill;
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

    public Paint getBaseLinePaint() {
        return mBaseLinePaint;
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

        initPartRect();
    }

    /**
     * x轴的高度，高亮文本画笔textSize略大于默认文本画笔的textSize
     */
    public float getAxisXHeight() {
        return mTextDrawHelper.getTextHeight(mHighLightPaint);
    }

    public void setMode(@MainRect.Mode int mode) {
        mMainRect.setMode(mode);

        if (isLine())
            mScaleX = 0.7f;
        else
            mScaleX = 1.0f;
        mAdapter.notifyDataSetChanged();
    }

    public IValueFormatter getPriceFormatter() {
        if (mPriceFormatter == null)
            mPriceFormatter = new PriceFormatter();
        return mPriceFormatter;
    }

    public void setPriceFormatter(IValueFormatter mPriceFormatter) {
        this.mPriceFormatter = mPriceFormatter;
    }

    public IDateFormatter getDateFormatter() {
        if (mDateFormatter == null)
            mDateFormatter = new DateFormatter("MM-dd HH:mm");
        return mDateFormatter;
    }

    public void setDateFormatter(IDateFormatter mDateFormatter) {
        this.mDateFormatter = mDateFormatter;
    }

    public IValueFormatter getVolumeFormatter() {
        if (mVolumeFormatter == null)
            mVolumeFormatter = new VolumeFormatter();
        return mVolumeFormatter;
    }

    public void setVolumeFormatter(IValueFormatter mVolumeFormatter) {
        this.mVolumeFormatter = mVolumeFormatter;
    }
}
