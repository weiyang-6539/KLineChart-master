package com.github.wyang.klinechartlib.huobi.data;

import android.annotation.SuppressLint;

import com.github.wyang.klinechartlib.huobi.interfaces.KLineConstant;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSet;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSetProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fxb on 2019-12-29.
 */
public class DataLineSetProvider implements IDataLineSetProvider, KLineConstant {
    private Map<String, IDataLineSet> dataLineSetMap = new HashMap<>();

    private DataLineSet candleLineSet = new DataLineSet();
    private DataLineSet maLineSet = new DataLineSet();
    private DataLineSet emaLineSet = new DataLineSet();
    private DataLineSet bollLineSet = new DataLineSet();

    private DataLineSet volumeLineSet = new DataLineSet();
    private DataLineSet macdLineSet = new DataLineSet();
    private DataLineSet kdjLineSet = new DataLineSet();
    private DataLineSet rsiLineSet = new DataLineSet();
    private DataLineSet wrLineSet = new DataLineSet();

    public DataLineSetProvider() {
        resetCandle();
        resetMa();
        resetEma();
        resetBoll();

        resetVol();
        resetMacd();
        resetKdj();
        resetRsi();
        resetWr();
    }

    @Override
    public IDataLineSet get(String name) {
        return dataLineSetMap.get(name);
    }

    @Override
    public void calculate(List<KLineEntity> data, int end) {
        float[] ma = new float[6];
        float[] boll = new float[1];

        float[] volArr = new float[2];
        float[] macdArr = new float[2];

        float[][] rsiArr = new float[3][2];

        //计算各项指标
        for (int i = 0; i <= end; i++) {
            calculateCandle(data, i);
            calculateMa(data, i, ma);
            calculateEma(data, i);
            calculateBoll(data, i, boll);

            calculateVol(data, i, volArr);
            calculateMacd(data, i, macdArr);
            calculateKdj(data, i);

            calculateRri(data, i, rsiArr);
            calculateWr(data, i);
        }
    }

    @Override
    public int getMaxN() {
        int rst = Integer.MIN_VALUE;
        //ma均线
        for (int n : MA_N) {
            rst = Math.max(rst, n);
        }

        rst = Math.max(rst, BOLL_N[0]);

        //成交量均线
        for (int n : VOL_N) {
            rst = Math.max(rst, n);
        }

        for (int n : RSI_N) {
            rst = Math.max(rst, n);
        }
        for (int n : WR_N) {
            rst = Math.max(rst, n);
        }
        return rst;
    }

    /**
     * 界面上绘制仅绘制蜡烛图，无指标线
     */
    private void calculateCandle(List<KLineEntity> data, int position) {
        candleLineSet.addData(data.get(position).getCandle());
    }

    /**
     * 蜡烛图+ma指标
     */
    private void calculateMa(List<KLineEntity> data, int position, float[] ma) {
        KLineEntity entity = data.get(position);

        for (int i = 0; i < MA_N.length; i++) {
            int n = MA_N[i];
            ma[i] += entity.close;
            if (position >= n)
                ma[i] -= data.get(position - n).close;
        }

        maLineSet.addLinePoint(
                position < MA_N[0] - 1 ? null : ma[0] / MA_N[0],
                position < MA_N[1] - 1 ? null : ma[1] / MA_N[1],
                position < MA_N[2] - 1 ? null : ma[2] / MA_N[2],
                position < MA_N[3] - 1 ? null : ma[3] / MA_N[3],
                position < MA_N[4] - 1 ? null : ma[4] / MA_N[4],
                position < MA_N[5] - 1 ? null : ma[5] / MA_N[5]
        );
        maLineSet.addData(entity.getCandle());
    }

    /**
     * 蜡烛图+ema指标
     */
    private void calculateEma(List<KLineEntity> data, int position) {
        float close = data.get(position).close;

        float ema0;
        float ema1;
        if (position == 0) {
            ema0 = close;
            ema1 = close;
        } else {
            int n0 = EMA_N[0];
            int n1 = EMA_N[1];

            Float lastEma0 = emaLineSet.getLinePoint(0, position - 1);
            Float lastEma1 = emaLineSet.getLinePoint(1, position - 1);

            ema0 = lastEma0 * (n0 - 1) / (n0 + 1) + close * 2 / (n0 + 1);
            ema1 = lastEma1 * (n1 - 1) / (n1 + 1) + close * 2 / (n1 + 1);
        }
        emaLineSet.addLinePoint(ema0, ema1);
        emaLineSet.addData(data.get(position).getCandle());
    }

    /**
     * 蜡烛图 + boll指标
     */
    private void calculateBoll(List<KLineEntity> data, int position, float[] bollArr) {
        bollArr[0] += data.get(position).close;
        if (position < BOLL_N[0] - 1) {
            bollLineSet.addLinePoint(null, null, null);
        } else {
            if (position >= BOLL_N[0])
                bollArr[0] -= data.get(position - BOLL_N[0]).close;

            float md = 0;
            for (int i = position - BOLL_N[0] + 1; i <= position; i++) {
                float c = data.get(i).close;
                float value = c - bollArr[0] / BOLL_N[0];
                md += value * value;
            }
            md = md / BOLL_N[0] - 1;
            md = (float) Math.sqrt(md);

            float boll = bollArr[0] / BOLL_N[0];
            float ub = bollArr[0] / BOLL_N[0] + BOLL_N[1] * md;
            float lb = bollArr[0] / BOLL_N[0] - BOLL_N[1] * md;
            bollLineSet.addLinePoint(boll, ub, lb);
        }
        bollLineSet.addData(data.get(position).getCandle());
    }

    /**
     * 成交量 + 成交量ma指标
     */
    private void calculateVol(List<KLineEntity> data, int position, float[] volMa) {
        volMa[0] += data.get(position).vol;
        if (position >= VOL_N[0])
            volMa[0] -= data.get(position - VOL_N[0]).vol;

        volMa[1] += data.get(position).vol;
        if (position >= VOL_N[1])
            volMa[1] -= data.get(position - VOL_N[1]).vol;

        volumeLineSet.addLinePoint(
                position < VOL_N[0] - 1 ? null : volMa[0] / VOL_N[0],
                position < VOL_N[1] - 1 ? null : volMa[1] / VOL_N[1]
        );
        volumeLineSet.addData(data.get(position).getVolume());
    }

    private void calculateMacd(List<KLineEntity> data, int position, float[] macdEma) {
        int n0 = MACD_N[0];// 12
        int n1 = MACD_N[1];// 26
        int n2 = MACD_N[2];// 9

        float close = data.get(position).close;
        if (position == 0) {
            macdEma[0] = close;
            macdEma[1] = close;
        } else {
            macdEma[0] = macdEma[0] * (n0 - 1) / (n0 + 1) + close * 2 / (n0 + 1);
            macdEma[1] = macdEma[1] * (n1 - 1) / (n1 + 1) + close * 2 / (n1 + 1);
        }

        float dif, dea;
        if (position == 0) {
            dif = 0f;
            dea = 0f;
        } else {
            dif = macdEma[0] - macdEma[1];

            Float lastDea = macdLineSet.getLinePoint(1, position - 1);
            dea = lastDea * (n2 - 1) / (n2 + 1) + dif * 2 / (n2 + 1);
        }
        macdLineSet.addLinePoint(dif, dea);

        Macd macd = new Macd((dif - dea) * 2f);
        macdLineSet.addData(macd);
    }

    /**
     * kdj指标
     */
    private void calculateKdj(List<KLineEntity> data, int position) {
        if (position < KDJ_N[0] - 1) {
            kdjLineSet.addLinePoint(null, null, null);
        } else {
            KLineEntity entity = data.get(position);

            int startIndex = position - KDJ_N[0] + 1;
            float maxN = Float.MIN_VALUE;
            float minN = Float.MAX_VALUE;
            for (int index = startIndex; index <= position; index++) {
                maxN = Math.max(maxN, entity.high);
                minN = Math.min(minN, entity.low);
            }
            Float rsv = 100f * (entity.close - minN) / (maxN - minN);
            if (rsv.isNaN())
                rsv = 0f;

            Float lastK = kdjLineSet.getLinePoint(0, position - 1);
            Float lastD = kdjLineSet.getLinePoint(1, position - 1);

            if (lastK == null)
                lastK = 50f;
            if (lastD == null)
                lastD = 50f;

            //TODO 14,1,3 与 9，3，3的差别未解决
            Float k = (rsv + 2 * lastK) / 3;
            Float d = (k + 2 * lastD) / 3;
            Float j = 3 * k - 2 * d;

            if (position <= KDJ_N[0]) {
                d = null;
                j = null;
            }

            kdjLineSet.addLinePoint(k, d, j);
        }

    }

    /**
     * rsi指标
     */
    private void calculateRri(List<KLineEntity> data, int position, float[][] rsiArr) {
        KLineEntity entity = data.get(position);
        if (position > 0) {
            KLineEntity last = data.get(position - 1);
            float rsiMax = Math.max(0, entity.close - last.close);
            float rsiAbs = Math.abs(entity.close - last.close);

            rsiArr[0][0] = (rsiMax + (RSI_N[0] - 1) * rsiArr[0][0]) / RSI_N[0];
            rsiArr[0][1] = (rsiAbs + (RSI_N[0] - 1) * rsiArr[0][1]) / RSI_N[0];

            rsiArr[1][0] = (rsiMax + (RSI_N[1] - 1) * rsiArr[1][0]) / RSI_N[1];
            rsiArr[1][1] = (rsiAbs + (RSI_N[1] - 1) * rsiArr[1][1]) / RSI_N[1];

            rsiArr[2][0] = (rsiMax + (RSI_N[2] - 1) * rsiArr[2][0]) / RSI_N[2];
            rsiArr[2][1] = (rsiAbs + (RSI_N[2] - 1) * rsiArr[2][1]) / RSI_N[2];
        }


        Float rsi1 = null;
        Float rsi2 = null;
        Float rsi3 = null;
        if (position >= RSI_N[0] - 1) {
            rsi1 = 100 * rsiArr[0][0] / rsiArr[0][1];
            if (rsi1.isNaN())
                rsi1 = 0f;
        }
        if (position >= RSI_N[1] - 1) {
            rsi2 = 100 * rsiArr[1][0] / rsiArr[1][1];
            if (rsi2.isNaN())
                rsi2 = 0f;
        }
        if (position >= RSI_N[2] - 1) {
            rsi3 = 100 * rsiArr[2][0] / rsiArr[2][1];
            if (rsi3.isNaN())
                rsi3 = 0f;
        }
        rsiLineSet.addLinePoint(rsi1, rsi2, rsi3);
    }

    private void calculateWr(List<KLineEntity> data, int position) {
        int n0 = WR_N[0];
        int n1 = WR_N[1];
        int n2 = WR_N[2];

        KLineEntity entity = data.get(position);

        //wr1
        int startIndex0 = position - n0 + 1;
        if (startIndex0 < 0) {
            startIndex0 = 0;
        }
        float maxN0 = Float.MIN_VALUE;
        float minN0 = Float.MAX_VALUE;
        for (int i = startIndex0; i <= position; i++) {
            maxN0 = Math.max(maxN0, data.get(i).high);
            minN0 = Math.min(minN0, data.get(i).low);
        }

        //wr2
        int startIndex1 = position - n1 + 1;
        if (startIndex1 < 0) {
            startIndex1 = 0;
        }
        float maxN1 = Float.MIN_VALUE;
        float minN1 = Float.MAX_VALUE;
        for (int i = startIndex1; i <= position; i++) {
            maxN1 = Math.max(maxN1, data.get(i).high);
            minN1 = Math.min(minN1, data.get(i).low);
        }

        //wr3
        int startIndex2 = position - n2 + 1;
        if (startIndex2 < 0) {
            startIndex2 = 0;
        }
        float maxN2 = Float.MIN_VALUE;
        float minN2 = Float.MAX_VALUE;
        for (int i = startIndex2; i <= position; i++) {
            maxN2 = Math.max(maxN2, data.get(i).high);
            minN2 = Math.min(minN2, data.get(i).low);
        }

        Float wr1;
        Float wr2;
        Float wr3;

        if (position < n0 - 1)
            wr1 = null;
        else
            wr1 = -100 * (maxN0 - entity.close) / (maxN0 - minN0);

        if (position < n1 - 1)
            wr2 = null;
        else
            wr2 = -100 * (maxN1 - entity.close) / (maxN1 - minN1);

        if (position < n2 - 1)
            wr3 = null;
        else
            wr3 = -100 * (maxN2 - entity.close) / (maxN2 - minN2);

        if (wr1 != null && wr1.isNaN())
            wr1 = 0f;

        if (wr2 != null && wr2.isNaN())
            wr2 = 0f;

        if (wr3 != null && wr3.isNaN())
            wr3 = 0f;

        if (n0 == Integer.MAX_VALUE)
            wr1 = null;
        if (n1 == Integer.MAX_VALUE)
            wr2 = null;
        if (n2 == Integer.MAX_VALUE)
            wr3 = null;

        wrLineSet.addLinePoint(wr1, wr2, wr3);
    }

    private void resetCandle() {
        candleLineSet.clear();
        dataLineSetMap.put("", candleLineSet);
    }

    @SuppressLint("DefaultLocale")
    private void resetMa() {
        maLineSet.clear();
        maLineSet.setName("MA");
        for (int i = 0; i < MA_N.length; i++) {
            int n = MA_N[i];
            maLineSet.addLine(COLORS[i], String.format("MA%d:", n));
        }
        dataLineSetMap.put("ma", maLineSet);
    }

    @SuppressLint("DefaultLocale")
    private void resetEma() {
        emaLineSet.clear();
        emaLineSet.setName("EMA");
        emaLineSet.addLine(COLORS[0], String.format("EMA(%d):", EMA_N[0]));
        emaLineSet.addLine(COLORS[1], String.format("EMA(%d):", EMA_N[1]));
        dataLineSetMap.put("ema", emaLineSet);
    }

    private void resetBoll() {
        bollLineSet.clear();
        bollLineSet.setName("BOLL");
        bollLineSet.addLine(COLORS[0], "BOLL:");
        bollLineSet.addLine(COLORS[1], "UB:");
        bollLineSet.addLine(COLORS[2], "LB:");
        dataLineSetMap.put("boll", bollLineSet);
    }

    @SuppressLint("DefaultLocale")
    private void resetVol() {
        volumeLineSet.clear();
        volumeLineSet.setName("VOL");
        volumeLineSet.setDataLabel("VOL:");
        volumeLineSet.addLine(COLORS[0], String.format("MA%d:", VOL_N[0]));
        volumeLineSet.addLine(COLORS[1], String.format("MA%d:", VOL_N[1]));
        dataLineSetMap.put("volume", volumeLineSet);
    }

    @SuppressLint("DefaultLocale")
    private void resetMacd() {
        macdLineSet.clear();
        macdLineSet.setName(String.format(
                "MACD(%d,%d,%d)",
                MACD_N[0],
                MACD_N[1],
                MACD_N[2]
        ));
        macdLineSet.setShowName(true);
        macdLineSet.setDataLabel("MACD:");
        macdLineSet.addLine(COLORS[0], "DIF:");
        macdLineSet.addLine(COLORS[1], "DEA:");
        dataLineSetMap.put("macd", macdLineSet);
    }

    @SuppressLint("DefaultLocale")
    private void resetKdj() {
        kdjLineSet.clear();
        kdjLineSet.setName(String.format(
                "KDJ(%d,%d,%d)",
                KDJ_N[0],
                KDJ_N[1],
                KDJ_N[2]
        ));
        kdjLineSet.setShowName(true);
        kdjLineSet.addLine(COLORS[0], "K:");
        kdjLineSet.addLine(COLORS[1], "D:");
        kdjLineSet.addLine(COLORS[2], "J:");
        dataLineSetMap.put("kdj", kdjLineSet);
    }

    @SuppressLint("DefaultLocale")
    private void resetRsi() {
        rsiLineSet.clear();
        rsiLineSet.setName("RSI");
        for (int i = 0; i < RSI_N.length; i++) {
            int n = RSI_N[i];
            rsiLineSet.addLine(COLORS[i], String.format("RSI(%d):", n));
        }
        dataLineSetMap.put("rsi", rsiLineSet);
    }

    @SuppressLint("DefaultLocale")
    private void resetWr() {
        wrLineSet.clear();
        wrLineSet.setName("WR");
        for (int i = 0; i < WR_N.length; i++) {
            int n = WR_N[i];
            wrLineSet.addLine(COLORS[i], String.format("WR(%d):", n));
        }
        dataLineSetMap.put("wr", wrLineSet);
    }
}
