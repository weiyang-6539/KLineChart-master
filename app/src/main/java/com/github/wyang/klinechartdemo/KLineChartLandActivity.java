package com.github.wyang.klinechartdemo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.wyang.klinechartdemo.utils.AssetUtil;
import com.github.wyang.klinechartlib.base.IBarLineSet;
import com.github.wyang.klinechartlib.base.ICandle;
import com.github.wyang.klinechartlib.huobi.KLineChartAdapter;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.data.BarLineSet;
import com.github.wyang.klinechartlib.huobi.data.Candle;
import com.github.wyang.klinechartlib.huobi.draw.MainRect;

import org.json.JSONArray;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fxb on 2019-12-05.
 */
public class KLineChartLandActivity extends AppCompatActivity {
    private TextView tv_code;

    private TabLayout mTabLayout;
    private KLineChartView mKLineChartView;
    private KLineChartAdapter mAdapter;

    private RadioGroup rg_main;
    private RadioGroup rg_sub;

    private ImageButton btn_main_hide;
    private ImageButton btn_sub_hide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置竖屏Activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_k_line_land);

        mTabLayout = findViewById(R.id.mTabLayout);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab1));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab2));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab3));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab4));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab5));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab6));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab7));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab8));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab9));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.k_tab10));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0)
                    mKLineChartView.setMode(MainRect.Mode.LINE);
                else
                    mKLineChartView.setMode(MainRect.Mode.CANDLE);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mKLineChartView = findViewById(R.id.mKLineChartView);
        mKLineChartView.setBottomAxisX(false);

        mAdapter = new KLineChartAdapter();
        mAdapter.bindToChartView(mKLineChartView);

        initKLineData(this);

        rg_main = findViewById(R.id.rg_main);
        rg_sub = findViewById(R.id.rg_sub);

        btn_main_hide = findViewById(R.id.btn_main_hide);
        btn_sub_hide = findViewById(R.id.btn_sub_hide);

        rg_main.setOnCheckedChangeListener((group, checkedId) -> {
            btn_main_hide.setSelected(checkedId != View.NO_ID);
            switch (checkedId) {
                case R.id.rb_ma:
                    mAdapter.changeMain(0);
                    break;
                case R.id.rb_boll:
                    mAdapter.changeMain(1);
                    break;
                default:
                    mAdapter.changeMain(-1);
                    break;
            }
        });
        rg_main.check(R.id.rb_ma);

        rg_sub.setOnCheckedChangeListener((group, checkedId) -> {
            btn_sub_hide.setSelected(checkedId != View.NO_ID);
            mKLineChartView.setShowChild2(checkedId != View.NO_ID);

            switch (checkedId) {
                case R.id.rb_macd:
                    mAdapter.changeChild2(0);
                    break;
                case R.id.rb_kdj:
                    mAdapter.changeChild2(1);
                    break;
                case R.id.rb_rsi:
                    mAdapter.changeChild2(2);
                    break;
                case R.id.rb_wr:
                    mAdapter.changeChild2(3);
                    break;
                default:
                    mAdapter.changeChild2(-1);
                    break;
            }
        });
        rg_sub.check(R.id.rb_macd);
    }

    public void onClose(View v) {
        finish();
    }

    private void initKLineData(Context context) {
        int color1 = ContextCompat.getColor(this, R.color.chart_line1);
        int color2 = ContextCompat.getColor(this, R.color.chart_line2);
        int color3 = ContextCompat.getColor(this, R.color.chart_line3);

        List<ICandle> candles = new ArrayList<>();
        List<IBarLineSet> mainBarLineSets = new ArrayList<>();

        BarLineSet maLine = new BarLineSet("MA");
        maLine.addLine(color1, "MA5:");
        maLine.addLine(color2, "MA10:");
        maLine.addLine(color3, "MA30:");

        BarLineSet bollLine = new BarLineSet("BOLL");
        bollLine.addLine(color1, "BOLL:");//中轨线
        bollLine.addLine(color2, "UB:");//上轨线
        bollLine.addLine(color3, "LB:");//下轨线

        mainBarLineSets.add(maLine);
        mainBarLineSets.add(bollLine);

        List<IBarLineSet> child1BarLineSets = new ArrayList<>();

        BarLineSet volumeLine = new BarLineSet("成交量");
        volumeLine.addLine(color1, "MA5:");
        volumeLine.addLine(color2, "MA10:");
        child1BarLineSets.add(volumeLine);

        List<IBarLineSet> child2BarLineSets = new ArrayList<>();

        BarLineSet macdLine = new BarLineSet("MACD");
        macdLine.addLine(color1, "DIF:");
        macdLine.addLine(color2, "DEA:");
        child2BarLineSets.add(macdLine);

        BarLineSet kdjLine = new BarLineSet("KDJ(14,1,3)");
        kdjLine.addLine(color1, "K:");
        kdjLine.addLine(color2, "D:");
        kdjLine.addLine(color3, "J:");
        child2BarLineSets.add(kdjLine);

        BarLineSet rsiLine = new BarLineSet("RSI");
        rsiLine.addLine(color1, "RSI(14):");
        child2BarLineSets.add(rsiLine);

        BarLineSet wrLine = new BarLineSet("WR");
        wrLine.addLine(color1, "WR(14):");
        child2BarLineSets.add(wrLine);

        try {
            String json = AssetUtil.readAsset(context, "test.json");
            JSONArray jsonArray = new JSONArray(json);

            Log.e("KLineChartActivity", "数据长：" + jsonArray.length());
            DecimalFormat format = new DecimalFormat("0.00%");

            float ma5 = 0;
            float ma10 = 0;
            float ma30 = 0;

            float ma20 = 0;

            float volMa5 = 0;
            float volMa10 = 0;

            float ema12 = 0;
            float ema26 = 0;
            float dif = 0;
            float dea = 0;
            float macd = 0;

            float K = 0;
            float D = 0;
            float J = 0;

            Float rsi = null;
            float rsiABSEma = 0;
            float rsiMaxEma = 0;

            Float r;
            Candle entity;
            for (int i = 0; i < jsonArray.length(); i++) {
                entity = new Candle();
                JSONArray arr = jsonArray.getJSONArray(i);
                entity.open = (float) arr.getDouble(0);
                entity.close = (float) arr.getDouble(1);
                entity.high = (float) arr.getDouble(2);
                entity.low = (float) arr.getDouble(3);
                entity.time = arr.getLong(4);
                entity.volume = (float) arr.getDouble(5);
                entity.total = entity.volume * entity.close;
                entity.changeValue = entity.open - entity.close;
                //计算涨跌幅
                String per = format.format((entity.close - entity.open) / entity.open);
                entity.changePercent = per.startsWith("-") ? per : "+" + per;
                candles.add(entity);

                float close = entity.getClose();

                ma5 += close;
                if (i == 4) {
                    maLine.getLine(0).add(ma5 / 5);
                } else if (i >= 5) {
                    ma5 -= candles.get(i - 5).getClose();
                    maLine.getLine(0).add(ma5 / 5);
                } else {
                    maLine.getLine(0).add(null);
                }
                ma10 += close;
                if (i == 9) {
                    maLine.getLine(1).add(ma10 / 10);
                } else if (i >= 10) {
                    ma10 -= candles.get(i - 10).getClose();
                    maLine.getLine(1).add(ma10 / 10);
                } else {
                    maLine.getLine(1).add(null);
                }
                ma30 += close;
                if (i == 29) {
                    maLine.getLine(2).add(ma30 / 30);
                } else if (i >= 30) {
                    ma30 -= candles.get(i - 30).getClose();
                    maLine.getLine(2).add(ma30 / 30);
                } else {
                    maLine.getLine(2).add(null);
                }

                ma20 += close;
                if (i < 19) {
                    bollLine.getLine(0).add(null);
                    bollLine.getLine(1).add(null);
                    bollLine.getLine(2).add(null);
                } else {
                    if (i >= 20)
                        ma20 -= candles.get(i - 20).getClose();

                    float md = 0;
                    for (int j = i - 19; j <= i; j++) {
                        float c = candles.get(j).getClose();
                        float value = c - ma20 / 20;
                        md += value * value;
                    }
                    md = md / 19;
                    md = (float) Math.sqrt(md);

                    bollLine.getLine(0).add(ma20 / 20);
                    bollLine.getLine(1).add(ma20 / 20 + 2f * md);
                    bollLine.getLine(2).add(ma20 / 20 - 2f * md);
                }

                volMa5 += entity.getVolume();
                if (i < 4) {
                    volumeLine.getLine(0).add(null);
                } else {
                    if (i >= 5)
                        volMa5 -= candles.get(i - 5).getVolume();
                    volumeLine.getLine(0).add(volMa5 / 5);
                }

                volMa10 += entity.getVolume();
                if (i < 9) {
                    volumeLine.getLine(1).add(null);
                } else {
                    if (i >= 10)
                        volMa10 -= candles.get(i - 10).getVolume();
                    volumeLine.getLine(1).add(volMa10 / 10);
                }

                //计算macd
                if (i == 0) {
                    ema12 = close;
                    ema26 = close;
                } else {
                    // EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
                    ema12 = ema12 * 11f / 13f + close * 2f / 13f;
                    // EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                    ema26 = ema26 * 25f / 27f + close * 2f / 27f;
                }
                // DIF = EMA（12） - EMA（26） 。
                // 今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
                // 用（DIF-DEA）*2即为MACD柱状图。
                dif = ema12 - ema26;
                dea = dea * 8f / 10f + dif * 2f / 10f;
                macd = (dif - dea) * 2f;
                macdLine.getLine(0).add(dif);
                macdLine.getLine(1).add(dea);

                //计算kdj
                int startIndex = i - 13;
                if (startIndex < 0) {
                    startIndex = 0;
                }
                float max14 = Float.MIN_VALUE;
                float min14 = Float.MAX_VALUE;
                for (int index = startIndex; index <= i; index++) {
                    max14 = Math.max(max14, entity.getHigh());
                    min14 = Math.min(min14, entity.getLow());
                }
                Float rsv = 100f * (close - min14) / (max14 - min14);
                if (rsv.isNaN()) {
                    rsv = 0f;
                }
                if (i == 0) {
                    K = 50;
                    D = 50;
                } else {
                    K = (rsv + 2f * K) / 3f;
                    D = (K + 2f * D) / 3f;
                }
                if (i < 13) {
                    K = 0;
                    D = 0;
                    J = 0;
                } else if (i == 13 || i == 14) {
                    D = 0;
                    J = 0;
                } else {
                    J = 3f * K - 2 * D;
                }
                kdjLine.getLine(0).add(K);
                kdjLine.getLine(1).add(D);
                kdjLine.getLine(2).add(J);

                //计算rsi
                if (i > 0) {
                    float Rmax = Math.max(0, close - candles.get(i - 1).getClose());
                    float RAbs = Math.abs(close - candles.get(i - 1).getClose());

                    rsiMaxEma = (Rmax + (14f - 1) * rsiMaxEma) / 14f;
                    rsiABSEma = (RAbs + (14f - 1) * rsiABSEma) / 14f;
                    rsi = (rsiMaxEma / rsiABSEma) * 100;
                }
                if (i < 13) {
                    rsi = 0f;
                }
                if (rsi.isNaN())
                    rsi = 0f;

                rsiLine.getLine(0).add(rsi);


                //计算wr
                startIndex = i - 14;
                if (startIndex < 0) {
                    startIndex = 0;
                }
                max14 = Float.MIN_VALUE;
                min14 = Float.MAX_VALUE;
                for (int index = startIndex; index <= i; index++) {
                    max14 = Math.max(max14, entity.getHigh());
                    min14 = Math.min(min14, entity.getLow());
                }
                if (i < 13) {
                    wrLine.getLine(0).add(-10f);
                } else {
                    r = -100 * (max14 - close) / (max14 - min14);

                    wrLine.getLine(0).add(r.isNaN() ? 0f : r);
                }
            }


        } catch (Exception ignored) {
        }

        mAdapter.setData(candles);
        mAdapter.setMainLineSets(mainBarLineSets);
        mAdapter.setChild1LineSets(child1BarLineSets);
        mAdapter.setChild2LineSets(child2BarLineSets);
    }


    public void onHideMain(View view) {
        rg_main.check(View.NO_ID);
    }

    public void onHideSub(View view) {
        rg_sub.check(View.NO_ID);
    }
}
