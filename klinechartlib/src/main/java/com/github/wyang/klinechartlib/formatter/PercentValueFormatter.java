package com.github.wyang.klinechartlib.formatter;


import com.github.wyang.klinechartlib.base.IValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by *** on 2019-08-24.
 */
public class PercentValueFormatter implements IValueFormatter {
    private DecimalFormat format = new DecimalFormat("0.00%");

    @Override
    public String format(float value) {
        String format = this.format.format(value);
        return format.startsWith("-") ? format : "+" + format;
    }
}
