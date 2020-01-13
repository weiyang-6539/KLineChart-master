package com.github.wyang.klinechartlib.huobi.data;

/**
 * Created by fxb on 2019-12-28.
 */
public class KLineEntity {
    public float open;//开盘价
    public float high;//最高价
    public float low;//最低价
    public float close;//收盘价
    public float vol;//成交量
    public float total;//成交额
    public long time;//时间戳 单位毫秒

    private Candle candle;
    private Volume volume;

    public Candle getCandle() {
        if (candle == null)
            candle = new Candle();

        candle.open = open;
        candle.high = high;
        candle.close = close;
        candle.low = low;
        candle.time = time;
        return candle;
    }

    public Volume getVolume() {
        if (volume == null)
            volume = new Volume();

        volume.volume = vol;
        return volume;
    }

    public void update(float newClose) {
        this.close = newClose;

        high = Math.max(high, close);
        low = Math.min(low, close);
    }
}
