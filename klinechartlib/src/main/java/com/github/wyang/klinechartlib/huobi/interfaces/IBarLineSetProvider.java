package com.github.wyang.klinechartlib.huobi.interfaces;

/**
 * Created by fxb on 2019-12-03.
 */
public interface IBarLineSetProvider {
    IBarLineSet getMainLineSet();

    IBarLineSet getChild1LineSet();

    IBarLineSet getChild2LineSet();
}
