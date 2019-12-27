package com.github.wyang.klinechartlib.huobi.helper;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSet;
import com.github.wyang.klinechartlib.huobi.draw.IChartDraw;
import com.github.wyang.klinechartlib.utils.ObjectPool;
import com.github.wyang.klinechartlib.utils.PointFPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fxb on 2019-12-03.
 * 用于绘制指标线的辅助类
 */
public class LinePathHelper {
    private static ObjectPool<Path> pathPool;

    static {
        pathPool = new ObjectPool<Path>(16, 8) {
            @Override
            public Path createObject() {
                return new Path();
            }
        };
    }

    private Path getPath() {
        return pathPool.get();
    }

    private void recycle() {
        pathPool.recycle(path);
        PointFPool.recycle(p1);
        PointFPool.recycle(p2);

        pathPool.recycle(paths);
        PointFPool.recycle(control1Points);
        PointFPool.recycle(control2Points);

        path = null;
        p1 = null;
        p2 = null;
        paths.clear();
        control1Points.clear();
        control2Points.clear();
    }

    private IChartDraw iChartDraw;
    private IDataLineSet iLineSet;

    /**
     * 主图收盘线
     */
    private Path path;
    private PointF p1;
    private PointF p2;

    /**
     * 主图or子图 指标线
     */
    private List<Path> paths = new ArrayList<>();
    private List<PointF> control1Points = new ArrayList<>();
    private List<PointF> control2Points = new ArrayList<>();

    public void save(@NonNull IChartDraw iChartDraw, IDataLineSet iLineSet) {
        this.iChartDraw = iChartDraw;
        this.iLineSet = iLineSet;
        if (iLineSet != null) {
            if (iLineSet.getDataCount() != 0) {
                Object o = iLineSet.getData(0);
                if (o instanceof ICandle) {
                    path = getPath();
                    path.reset();

                    p1 = PointFPool.get(0, 0);
                    p2 = PointFPool.get(0, 0);
                }
            }

            for (int i = 0; i < iLineSet.getLineSize(); i++) {
                Path path = getPath();
                path.reset();
                paths.add(path);

                control1Points.add(PointFPool.get(0, 0));
                control2Points.add(PointFPool.get(0, 0));
            }
        }
    }

    public void restore() {
        recycle();
    }

    public Path getLinePath() {
        return path;
    }

    public List<Path> getPaths() {
        return paths;
    }

    /**
     * 绘制曲线的平滑度
     */
    protected float SMOOTHNESS = 0.2f;

    public void move(int position) {
        if (iLineSet == null)
            return;
        if (iLineSet.getDataCount() != 0) {
            Object o = iLineSet.getData(position);
            if (o instanceof ICandle) {
                ICandle candle = (ICandle) o;

                float x = iChartDraw.getAxisX(position);
                float y = iChartDraw.getAxisY(candle.getClose());

                if (path.isEmpty()) {
                    path.moveTo(x, y);
                    //更新控制点1
                    p1.set(x, y);
                } else if (position == iLineSet.getDataCount() - 1) {
                    float lastX = iChartDraw.getAxisX(position - 1);
                    p2.x = x - (x - lastX) * SMOOTHNESS;
                    p2.y = y;

                    path.cubicTo(p1.x, p1.y, p2.x, p2.y, x, y);
                } else {
                    ICandle last = iLineSet.getData(position - 1);
                    ICandle next = iLineSet.getData(position + 1);
                    float lastX = iChartDraw.getAxisX(position - 1);
                    float lastY = iChartDraw.getAxisY(last.getClose());

                    float nextX = iChartDraw.getAxisX(position + 1);
                    float nextY = iChartDraw.getAxisY(next.getClose());

                    float k = (nextY - lastY) / (nextX - lastX);
                    float b = y - k * x;

                    //更新控制点2
                    p2.x = x - (x - lastX) * SMOOTHNESS;
                    p2.y = k * p2.x + b;

                    path.cubicTo(p1.x, p1.y, p2.x, p2.y, x, y);

                    //更新控制点1
                    p1.x = x + (nextX - x) * SMOOTHNESS;
                    p1.y = k * p1.x + b;
                }
            }
        }
        for (int i = 0; i < iLineSet.getLineSize(); i++) {
            Float rst = iLineSet.getLinePoint(i, position);
            Path path = paths.get(i);

            if (rst == null)
                continue;

            PointF f1 = control1Points.get(i);
            PointF f2 = control2Points.get(i);

            float x = iChartDraw.getAxisX(position);
            float y = iChartDraw.getAxisY(rst);

            if (path.isEmpty()) {
                path.moveTo(x, y);
                //更新控制点1
                f1.set(x, y);
            } else if (position == iLineSet.getCount() - 1) {
                float lastX = iChartDraw.getAxisX(position - 1);
                f2.x = x - (x - lastX) * SMOOTHNESS;
                f2.y = y;

                path.cubicTo(f1.x, f1.y, f2.x, f2.y, x, y);
            } else {
                float lastX = iChartDraw.getAxisX(position - 1);
                float lastY = iChartDraw.getAxisY(iLineSet.getLinePoint(i, position - 1));

                float nextX = iChartDraw.getAxisX(position + 1);
                float nextY = iChartDraw.getAxisY(iLineSet.getLinePoint(i, position + 1));

                float k = (nextY - lastY) / (nextX - lastX);
                float b = y - k * x;

                //更新控制点2
                f2.x = x - (x - lastX) * SMOOTHNESS;
                f2.y = k * f2.x + b;

                path.cubicTo(f1.x, f1.y, f2.x, f2.y, x, y);

                //更新控制点1
                f1.x = x + (nextX - x) * SMOOTHNESS;
                f1.y = k * f1.x + b;
            }
        }
    }
}
