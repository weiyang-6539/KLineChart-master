package com.github.wyang.klinechartlib.formatter;

import android.support.annotation.NonNull;

import com.github.wyang.klinechartlib.base.IDateFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by weiyang on 2019-11-06.
 */
public class DateFormatter implements IDateFormatter {
    private SimpleDateFormat format;
    private Date date = new Date();

    public DateFormatter(String pattern) {
        format = new SimpleDateFormat(pattern, Locale.SIMPLIFIED_CHINESE);
        format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    }

    public DateFormatter(@NonNull SimpleDateFormat format) {
        this.format = format;
    }

    @Override
    public String format(long time) {
        date.setTime(time);
        return format.format(date);
    }
}
