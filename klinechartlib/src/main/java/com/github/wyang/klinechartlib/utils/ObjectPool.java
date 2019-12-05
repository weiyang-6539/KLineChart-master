package com.github.wyang.klinechartlib.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Created by fxb on 2019-11-27.
 * 对象池，参考-超市购物车,后进先出（理想情况，每一位顾客借车都会归还）
 */
public abstract class ObjectPool<T> {
    /**
     * 初始预期容量,建议为2的指数倍
     */
    private int desiredCapacity;
    /**
     * 扩充量
     */
    private int expansionCapacity;

    private Stack<T> objects;
    private Set<T> recordSet;

    public ObjectPool(int initialCapacity, int expansionCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Object Pool must be instantiated with a capacity greater than 0!");
        }
        this.desiredCapacity = initialCapacity;
        this.objects = new Stack<>();
        this.recordSet = new HashSet<>();
        setExpansionCapacity(expansionCapacity);

        this.refillPool(desiredCapacity);
    }

    public abstract T createObject();

    private void setExpansionCapacity(int expansionCapacity) {
        if (expansionCapacity < 0 || expansionCapacity > desiredCapacity) {
            expansionCapacity = desiredCapacity / 2;
        }
        this.expansionCapacity = expansionCapacity;
    }

    private void refillPool(int count) {
        for (int i = 0; i < count; i++) {
            T t = createObject();
            objects.add(t);

            recordSet.add(t);
        }
    }

    public synchronized T get() {
        if (objects.isEmpty()) {
            if (expansionCapacity == 0) {
                expansionCapacity = desiredCapacity / 2;
            }

            desiredCapacity += expansionCapacity;
            this.refillPool(expansionCapacity);
        }
        return objects.pop();
    }

    public synchronized void recycle(T t) {
        //当前对象池借出而且未归还才执行归还操作
        if (recordSet.contains(t) && !objects.contains(t)) {
            objects.add(t);
        }
    }

    public synchronized void recycle(List<T> list) {
        for (T t : list) {
            recycle(t);
        }
    }

    public void printTestInfo() {
        System.out.println("当前容量=" + desiredCapacity + ",可用数量=" + objects.size() + ",实际数量=" + recordSet.size());
    }
}
