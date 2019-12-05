package com.github.wyang.klinechartlib.huobi.helper;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.github.wyang.klinechartlib.base.IBarLineSet;
import com.github.wyang.klinechartlib.huobi.draw.ICharRect;
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
        pathPool.recycle(paths);

        PointFPool.recycle(control1Points);
        PointFPool.recycle(control2Points);

        paths.clear();
        control1Points.clear();
        control2Points.clear();
    }


    private ICharRect iCharRect;
    private IBarLineSet iBarLineSet;
    private List<Path> paths = new ArrayList<>();
    private List<PointF> control1Points = new ArrayList<>();
    private List<PointF> control2Points = new ArrayList<>();

    public void save(@NonNull ICharRect iCharRect, IBarLineSet iBarLineSet) {
        this.iCharRect = iCharRect;
        this.iBarLineSet = iBarLineSet;
        if (iBarLineSet != null) {
            for (int i = 0; i < iBarLineSet.getLineSize(); i++) {
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

    public List<Path> getPaths() {
        return paths;
    }

    /**
     * 绘制曲线的平滑度
     */
    protected float SMOOTHNESS = 0.2f;

    public void move(int index) {
        if (iBarLineSet == null)
            return;
        for (int i = 0; i < iBarLineSet.getLineSize(); i++) {
            List<Float> points = iBarLineSet.getLine(i);

            Float rst = points.get(index);
            Path path = paths.get(i);

            if (rst == null)
                continue;

            PointF f1 = control1Points.get(i);
            PointF f2 = control2Points.get(i);

            float x = iCharRect.getAxisX(index);
            float y = iCharRect.getAxisY(rst);

            if (path.isEmpty()) {
                path.moveTo(x, y);
                //更新控制点1
                f1.set(x, y);
            } else if (index == iBarLineSet.getLine(0).size() - 1) {
                float lastX = iCharRect.getAxisX(index - 1);
                f2.x = x - (x - lastX) * SMOOTHNESS;
                f2.y = y;

                path.cubicTo(f1.x, f1.y, f2.x, f2.y, x, y);
            } else {
                float lastX = iCharRect.getAxisX(index - 1);
                float lastY = iCharRect.getAxisY(iBarLineSet.getLine(i).get(index - 1));

                float nextX = iCharRect.getAxisX(index + 1);
                float nextY = iCharRect.getAxisY(iBarLineSet.getLine(i).get(index + 1));

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
