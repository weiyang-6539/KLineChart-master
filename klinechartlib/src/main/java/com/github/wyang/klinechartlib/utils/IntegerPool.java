package com.github.wyang.klinechartlib.utils;

/**
 * Created by fxb on 2019-11-29.
 */
public class IntegerPool extends ObjectPool<Integer> {
    private static int i;
    private static IntegerPool pool;

    static {
        pool = new IntegerPool(32, 16);
    }

    public static IntegerPool getPool() {
        return pool;
    }

    private IntegerPool(int initialCapacity, int expansionCapacity) {
        super(initialCapacity, expansionCapacity);
    }

    @Override
    public Integer createObject() {
        return i++;
    }
}
