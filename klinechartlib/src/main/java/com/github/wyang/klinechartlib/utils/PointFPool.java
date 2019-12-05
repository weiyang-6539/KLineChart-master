package com.github.wyang.klinechartlib.utils;

import android.graphics.PointF;

import java.util.List;

/**
 * Created by fxb on 2019-11-29.
 */
public class PointFPool {
    private static ObjectPool<PointF> pool;

    static {
        pool = new ObjectPool<PointF>(4, 2) {
            @Override
            public PointF createObject() {
                return new PointF();
            }
        };
    }

    public static PointF get(float x, float y) {
        PointF pointF = pool.get();
        pointF.set(x, y);
        return pointF;
    }

    public static void recycle(PointF pointF) {
        pool.recycle(pointF);
    }

    public static void recycle(List<PointF> list) {
        pool.recycle(list);
    }
}
