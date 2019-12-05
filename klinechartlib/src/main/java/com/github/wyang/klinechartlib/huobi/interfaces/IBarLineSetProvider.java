package com.github.wyang.klinechartlib.huobi.interfaces;

import com.github.wyang.klinechartlib.base.IBarLineSet;

/**
 * Created by fxb on 2019-12-03.
 */
public interface IBarLineSetProvider {
    IBarLineSet getMainLineSet();

    IBarLineSet getChild1LineSet();

    IBarLineSet getChild2LineSet();
}
