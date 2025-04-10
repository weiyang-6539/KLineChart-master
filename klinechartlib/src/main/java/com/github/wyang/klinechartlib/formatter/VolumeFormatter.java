package com.github.wyang.klinechartlib.formatter;

import android.support.annotation.NonNull;

import com.github.wyang.klinechartlib.base.IValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by fxb on 2019-11-25.
 */
public class VolumeFormatter implements IValueFormatter {
    private DecimalFormat format;

    public VolumeFormatter() {
        format = new DecimalFormat("0");
    }

    public VolumeFormatter(@NonNull DecimalFormat format) {
        this.format = format;
    }

    @Override
    public String format(float value) {
        return format.format(value);
    }
}
