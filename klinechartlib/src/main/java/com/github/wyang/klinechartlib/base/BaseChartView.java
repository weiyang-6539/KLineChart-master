package com.github.wyang.klinechartlib.base;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.widget.OverScroller;

/**
 * Created by weiyang on 2019-11-01.
 * K线图View基类，处理测量逻辑，滑动缩放逻辑，及数据适配器和长按选中哪一项，具体绘制交由子类
 */
public abstract class BaseChartView<T extends ChartAdapter> extends ViewGroup implements
        GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
    protected final String TAG = getClass().getSimpleName();
    protected GestureDetectorCompat mGestureDetector;
    protected ScaleGestureDetector mScaleDetector;
    private OverScroller mScroller;

    /**
     * mScroller当前x轴的滑动量
     */
    protected int mScrollX;

    protected float mOverScrollRange;//图标右方后可滑出距离

    protected float mOffsetX = Float.MIN_VALUE;//第一个数据相对屏幕最左侧的偏移量，与mScrollX相关

    protected float mScaleX = 1;//缩放大小

    protected int mWidth;
    protected int mHeight;
    private float aspectRatio = 0.95f;//宽高比

    private boolean isTouch = false;//触摸动作的标记
    private boolean isLongPress = false;//长按的标记
    private boolean mMultipleTouch = false;//多点触摸的标记

    private boolean mScrollEnable = true;//是否能够滚动
    private boolean mScaleEnable = true;//是否能够缩放

    private T mAdapter;
    protected float mPointWidth;//当mScaleX=1时，数据占据屏幕的宽
    protected float mDataLength;//数据宽度总和

    protected int mSelectedIndex;//长按选中索引
    protected int mStartIndex;//可见数据起始索引
    protected int mEndIndex;//可见数据结束索引

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyScrollChanged();
        }

        @Override
        public void onInvalidated() {
            notifyInvalidated();
        }
    };

    public BaseChartView(Context context) {
        this(context, null);
    }

    public BaseChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);//硬件加速，否则虚线绘制成实线

        mGestureDetector = new GestureDetectorCompat(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);
        mScroller = new OverScroller(getContext());
    }

    public float getDimension(@DimenRes int resId) {
        return getResources().getDimension(resId);
    }

    public int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(getContext(), resId);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = (int) (mWidth * aspectRatio);
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                mHeight = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    private float downX;

    /**
     * 处理触摸逻辑
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //多点触摸
        mMultipleTouch = event.getPointerCount() > 1;
        if (mMultipleTouch)
            isLongPress = false;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isTouch = true;
                downX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isLongPress)
                    onLongPress(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (downX == event.getX() && isLongPress) {
                    isLongPress = false;
                }
                isTouch = false;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                isLongPress = false;
                isTouch = false;
                invalidate();
                break;
            default:
                break;
        }
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 手势监听回调的6个方法
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!isLongPress && !mMultipleTouch) {
            scrollBy(Math.round(distanceX), 0);
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        isLongPress = true;

        //长按选中点index计算，并回调
        int lastIndex = mSelectedIndex;

        mSelectedIndex = indexOfX(drawX2X(e.getX()));
        if (mSelectedIndex < mStartIndex) {
            mSelectedIndex = mStartIndex;
        }
        if (mSelectedIndex > mEndIndex) {
            mSelectedIndex = mEndIndex;
        }

        if (lastIndex != mSelectedIndex) {
            if (onSelectedChangedListener != null) {
                onSelectedChangedListener.onSelectedChanged(this, mSelectedIndex);
            }
        }

        invalidate();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!isTouch && !isLongPress() && isScrollEnable()) {
            mScroller.fling(mScrollX, 0, Math.round(-velocityX), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        }
        return true;
    }

    /**
     * 缩放手势回调的3个方法
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!isScaleEnable())
            return false;

        float oldScaleX = mScaleX;
        mScaleX *= detector.getScaleFactor();
        if (mScaleX < getMinScaleX()) {
            mScaleX = getMinScaleX();
        } else if (mScaleX > getMaxScaleX()) {
            mScaleX = getMaxScaleX();
        } else {
            onScaleChanged(oldScaleX);
        }
        notifyScaleChange(oldScaleX);

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    /**
     * 处理滑动逻辑
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (!isTouch) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

                //这一行代码不加，惯性滑动时看不到滑动效果
                postInvalidate();
            } else {
                mScroller.forceFinished(true);
            }
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (!isScrollEnable()) {
            mScroller.forceFinished(true);
            return;
        }
        int oldX = mScrollX;
        mScrollX = x;

        notifyScrollChanged();
        /*log("mScrollX=" + mScrollX);

        mOffsetX += mScrollX - oldX;

        checkOffsetX();*/
    }

    @Override
    public void scrollBy(int x, int y) {
        scrollTo(mScrollX + Math.round(x), 0);
    }

    /**
     * 缩放比例变化
     */
    protected void onScaleChanged(float oldScale) {
    }

    /**
     * 更新mScrollX
     */
    private void checkOffsetX() {
        if (mOffsetX > getMaxOffsetX()) {
            mOffsetX = getMaxOffsetX();
            onRightSide();
        }
        if (mOffsetX < getMinOffsetX()) {
            mOffsetX = getMinOffsetX();
            onLeftSide();
        }

        if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX();
        }
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX();
        }

        invalidate();
    }

    protected float getMaxScaleX() {
        return 2f;
    }

    protected float getMinScaleX() {
        return .5f;
    }

    protected float getMinOffsetX() {
        return 0;
    }

    protected float getMaxOffsetX() {
        return mDataLength - mWidth + mOverScrollRange;
    }

    private int getMinScrollX() {
        return (int) (getMinOffsetX() - getMaxOffsetX());
    }

    private int getMaxScrollX() {
        return 0;
    }

    /**
     * 设置超出右方后可滑动的范围
     */
    public void setOverScrollRange(float overScrollRange) {
        if (overScrollRange < 0) {
            overScrollRange = 0;
        }
        mOverScrollRange = overScrollRange;
    }

    public void onLeftSide() {
        log("滑动至最左侧");
    }

    public void onRightSide() {
        log("滑动至最右侧");
    }

    public boolean isScrollEnable() {
        return mScrollEnable;
    }

    public void setScrollEnable(boolean mScrollEnable) {
        this.mScrollEnable = mScrollEnable;
    }

    public boolean isScaleEnable() {
        return mScaleEnable;
    }

    public void setScaleEnable(boolean mScaleEnable) {
        this.mScaleEnable = mScaleEnable;
    }

    public boolean isLongPress() {
        return isLongPress;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public int getStartIndex() {
        return mStartIndex;
    }

    public int getEndIndex() {
        return mEndIndex;
    }

    public boolean isLastVisible() {
        return mEndIndex == getAdapter().getCount() - 1;
    }

    public void setAdapter(T mAdapter) {
        if (this.mAdapter != null && mDataSetObserver != null) {
            this.mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        this.mAdapter = mAdapter;
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    public T getAdapter() {
        if (mAdapter == null) {
            throw new NullPointerException("You must set an adapter for BaseChartView!");
        }
        return mAdapter;
    }

    /**
     * 什么时候调用？
     * 1.第一次设置数据
     * 2.分页添加历史数据（List中向最前添加）
     * 3.新增柱子（List中向末尾添加）
     */
    private void notifyScrollChanged() {
        //设置数据并测量完成
        if (getAdapter().getCount() != 0 && mWidth != 0) {
            mDataLength = mAdapter.getCount() * mPointWidth * mScaleX;

            mOffsetX = getMinOffsetX() - getMinScrollX() + mScrollX;

            checkOffsetX();
        }
    }

    /**
     * 手势缩放时调用
     */
    private void notifyScaleChange(float oldScaleX) {
        if (getAdapter().getCount() != 0 && mWidth != 0) {
            mDataLength = getAdapter().getCount() * mPointWidth * mScaleX;

            mOffsetX = (mOffsetX + mWidth * 0.5f) * mScaleX / oldScaleX - mWidth * 0.5f;
            mScrollX = (int) (mOffsetX - getMinOffsetX() + getMinScrollX());

            checkOffsetX();
        }
    }

    /**
     * 最后一根柱子更新时调用
     */
    private void notifyInvalidated() {

        invalidate();
    }

    public void cancelSelect() {
        isLongPress = false;
        invalidate();
    }

    public float drawX2X(float x) {
        return mOffsetX + x;
    }

    public int indexOfX(float x) {
        return indexOfX(x, 0, getAdapter().getCount() - 1);
    }

    /**
     * 二分查找当前值的index
     */
    public int indexOfX(float x, int start, int end) {
        if (end == start) {
            return start;
        }
        if (end - start == 1) {
            float startX = getX(start);
            float endX = getX(end);
            return Math.abs(x - startX) < Math.abs(x - endX) ? start : end;
        }
        int mid = start + (end - start) / 2;
        float midX = getX(mid);
        if (x < midX) {
            return indexOfX(x, start, mid);
        } else if (x > midX) {
            return indexOfX(x, mid, end);
        } else {
            return mid;
        }
    }

    /**
     * mOffsetX==0时，每个数据相对坐标原点的x值
     */
    private float getX(int position) {
        return (position + 0.5f) * mPointWidth * mScaleX;
    }

    public float getDrawX(int position) {
        return getX(position) - mOffsetX;
    }

    public void setPointWidth(float mPointWidth) {
        this.mPointWidth = mPointWidth;
    }

    private OnSelectedChangedListener onSelectedChangedListener;

    public void setOnSelectedChangedListener(OnSelectedChangedListener onSelectedChangedListener) {
        this.onSelectedChangedListener = onSelectedChangedListener;
    }

    /**
     * 选中点变化时的监听
     */
    public interface OnSelectedChangedListener {
        /**
         * 当选点中变化时
         *
         * @param view  当前view
         * @param index 选中点的索引
         */
        void onSelectedChanged(BaseChartView view, int index);
    }

    public void log(String text) {
        Log.e(TAG, text);
    }

    protected float dp2px(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    protected String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }
}
