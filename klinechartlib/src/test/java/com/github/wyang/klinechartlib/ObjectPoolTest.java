package com.github.wyang.klinechartlib;

import com.github.wyang.klinechartlib.utils.IntegerPool;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fxb on 2019-11-29.
 */
public class ObjectPoolTest {

    @Test
    public void test() {
        IntegerPool pool = IntegerPool.getPool();

        pool.printTestInfo();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 48; i++) {
            list.add(pool.get());
        }

        pool.printTestInfo();
        System.out.println(list.toString());

        pool.recycle(list);
        pool.printTestInfo();

    }

    @Test
    public void testSqrt() {
        System.out.println(sqrt(2));

        System.out.println(~0b101);
    }

    private static float sqrt(int number) {
        if (number == 0) {
            return 0;
        }

        float i = 0;
        float x1, x2 = 0;
        while ((i * i) <= number) {
            i += 0.1;
        }
        x1 = i;
        for (int j = 0; j < 10; j++) {
            x2 = number;
            x2 /= x1;
            x2 += x1;
            x2 /= 2;
            x1 = x2;
        }
        return x2;
    }
}
