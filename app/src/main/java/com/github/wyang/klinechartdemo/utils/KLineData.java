package com.github.wyang.klinechartdemo.utils;

import android.annotation.SuppressLint;

import com.github.wyang.klinechartdemo.bean.Candle;
import com.github.wyang.klinechartlib.huobi.data.DataLineSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fxb on 2019-12-27.
 * 设置一次参数，重新计算所有指标线
 */
public class KLineData {
    private DataLineSet candleLineSet = new DataLineSet();
    private DataLineSet maLineSet = new DataLineSet();
    private DataLineSet emaLineSet = new DataLineSet();
    private DataLineSet bollLineSet = new DataLineSet();

    private DataLineSet volumeLineSet = new DataLineSet();
    private DataLineSet macdLineSet = new DataLineSet();
    private DataLineSet kdjLineSet = new DataLineSet();
    private DataLineSet rsiLineSet = new DataLineSet();
    private DataLineSet wrLineSet = new DataLineSet();

    private List<Candle> data = new ArrayList<>();

    public KLineData() {
        resetMa();
        resetEma();
        resetBoll();

        resetVol();
        resetMacd();
        resetKdj();
        resetRsi();
        resetWr();
    }

    public void addCandles(List<Candle> list) {

    }

    public void addCandles(List<Candle> list, int index) {

    }

    public void updateLatest(Candle data) {

    }

    @SuppressLint("DefaultLocale")
    private void resetMa() {
        maLineSet.clear();
        maLineSet.setName("MA");
        for (int i = 0; i < KLineConstant.MA_N.length; i++) {
            Integer n = KLineConstant.MA_N[i];
            if (n != null)
                maLineSet.addLine(KLineConstant.COLORS[i], String.format("MA%d:", n));
        }
    }

    @SuppressLint("DefaultLocale")
    private void resetEma() {
        emaLineSet.clear();
        emaLineSet.setName("EMA");
        emaLineSet.addLine(KLineConstant.COLORS[0], String.format("EMA(%d):", KLineConstant.EMA_N[0]));
        emaLineSet.addLine(KLineConstant.COLORS[1], String.format("EMA(%d):", KLineConstant.EMA_N[1]));
    }

    private void resetBoll() {
        bollLineSet.clear();
        bollLineSet.setName("BOLL");
        bollLineSet.addLine(KLineConstant.COLORS[0], "BOLL:");
        bollLineSet.addLine(KLineConstant.COLORS[1], "UB:");
        bollLineSet.addLine(KLineConstant.COLORS[2], "LB:");
    }

    @SuppressLint("DefaultLocale")
    private void resetVol() {
        volumeLineSet.clear();
        volumeLineSet.setName("VOL");
        volumeLineSet.setDataLabel("VOL:");
        volumeLineSet.addLine(KLineConstant.COLORS[0], String.format("MA%d:", KLineConstant.VOL_N[0]));
        volumeLineSet.addLine(KLineConstant.COLORS[1], String.format("MA%d:", KLineConstant.VOL_N[1]));
    }

    @SuppressLint("DefaultLocale")
    private void resetMacd() {
        macdLineSet.clear();
        macdLineSet.setName(String.format(
                "MACD(%d,%d,%d)",
                KLineConstant.MACD_N[0],
                KLineConstant.MACD_N[1],
                KLineConstant.MACD_N[2]
        ));
        macdLineSet.addLine(KLineConstant.COLORS[0], "DIF");
        macdLineSet.addLine(KLineConstant.COLORS[1], "DEA");
    }

    @SuppressLint("DefaultLocale")
    private void resetKdj() {
        kdjLineSet.clear();
        kdjLineSet.setName(String.format(
                "KDJ(%d,%d,%d)",
                KLineConstant.KDJ_N[0],
                KLineConstant.KDJ_N[1],
                KLineConstant.KDJ_N[2]
        ));
        kdjLineSet.addLine(KLineConstant.COLORS[0], "K:");
        kdjLineSet.addLine(KLineConstant.COLORS[1], "D:");
        kdjLineSet.addLine(KLineConstant.COLORS[2], "J:");
    }

    private void resetRsi() {
    }

    private void resetWr() {
    }

}
