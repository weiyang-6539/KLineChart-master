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

import com.github.wyang.klinechartdemo.bean.Candle;
import com.github.wyang.klinechartdemo.bean.Macd;
import com.github.wyang.klinechartdemo.bean.Volume;
import com.github.wyang.klinechartdemo.utils.AssetUtil;
import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.data.IVolume;
import com.github.wyang.klinechartlib.huobi.KLineChartAdapter;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.data.DataLineSet;
import com.github.wyang.klinechartlib.huobi.draw.MainDraw;

import org.json.JSONArray;

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
                    mKLineChartView.setMode(MainDraw.Mode.LINE);
                else
                    mKLineChartView.setMode(MainDraw.Mode.CANDLE);

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
        mKLineChartView.setDrawGridStartEnd(true);

        mAdapter = new KLineChartAdapter();
        mAdapter.bindToKLineChartView(mKLineChartView);

        initKLineData(this);

        rg_main = findViewById(R.id.rg_main);
        rg_sub = findViewById(R.id.rg_sub);

        btn_main_hide = findViewById(R.id.btn_main_hide);
        btn_sub_hide = findViewById(R.id.btn_sub_hide);

        rg_main.setOnCheckedChangeListener((group, checkedId) -> {
            btn_main_hide.setSelected(checkedId != View.NO_ID);
            switch (checkedId) {
                case R.id.rb_ma:
                    mKLineChartView.setMainSelected("ma");
                    break;
                case R.id.rb_ema:
                    mKLineChartView.setMainSelected("ema");
                    break;
                case R.id.rb_boll:
                    mKLineChartView.setMainSelected("boll");
                    break;
                default:
                    mKLineChartView.setMainSelected("");
                    break;
            }
        });
        rg_main.check(R.id.rb_ma);

        rg_sub.setOnCheckedChangeListener((group, checkedId) -> {
            btn_sub_hide.setSelected(checkedId != View.NO_ID);
            mKLineChartView.setShowChild2(checkedId != View.NO_ID);

            switch (checkedId) {
                case R.id.rb_macd:
                    mKLineChartView.setChild2Selected("macd");
                    break;
                case R.id.rb_kdj:
                    mKLineChartView.setChild2Selected("kdj");
                    break;
                case R.id.rb_rsi:
                    mKLineChartView.setChild2Selected("rsi");
                    break;
                case R.id.rb_wr:
                    mKLineChartView.setChild2Selected("wr");
                    break;
                case R.id.rb_boll_child:
                    mKLineChartView.setChild2Selected("boll");
                    break;
                default:
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
        int color4 = ContextCompat.getColor(this, R.color.chart_line4);
        int color5 = ContextCompat.getColor(this, R.color.chart_line5);
        int color6 = ContextCompat.getColor(this, R.color.chart_line6);

        DataLineSet none = new DataLineSet();

        DataLineSet maLine = new DataLineSet();
        maLine.setName("MA");
        maLine.addLine(color1, "MA5:");
        maLine.addLine(color2, "MA10:");
        maLine.addLine(color3, "MA30:");
        maLine.addLine(color4, "MA45:");
        maLine.addLine(color5, "MA60:");
        maLine.addLine(color6, "MA90:");

        DataLineSet emaLine = new DataLineSet();
        emaLine.setName("EMA");
        emaLine.addLine(color1, "EMA(12):");
        emaLine.addLine(color2, "EMA(26):");

        DataLineSet bollLine = new DataLineSet();
        bollLine.setName("BOLL");
        bollLine.addLine(color1, "BOLL:");//中轨线
        bollLine.addLine(color2, "UB:");//上轨线
        bollLine.addLine(color3, "LB:");//下轨线

        DataLineSet volumeLine = new DataLineSet();
        volumeLine.setName("VOL");
        volumeLine.setDataLabel("VOL:");
        volumeLine.addLine(color1, "MA5:");
        volumeLine.addLine(color2, "MA10:");

        DataLineSet macdLine = new DataLineSet();
        macdLine.setName("MACD(12,26,9)");
        macdLine.setShowName(true);
        macdLine.setDataLabel("MACD");
        macdLine.addLine(color1, "DIF:");
        macdLine.addLine(color2, "DEA:");

        DataLineSet kdjLine = new DataLineSet();
        kdjLine.setName("KDJ(14,1,3)");
        kdjLine.setShowName(true);
        kdjLine.addLine(color1, "K:");
        kdjLine.addLine(color2, "D:");
        kdjLine.addLine(color3, "J:");

        DataLineSet rsiLine = new DataLineSet();
        rsiLine.setName("RSI");
        rsiLine.addLine(color1, "RSI(14):");

        DataLineSet wrLine = new DataLineSet();
        wrLine.setName("WR");
        wrLine.addLine(color1, "WR(14):");

        try {
            String json = AssetUtil.readAsset(context, "test.json");
            JSONArray jsonArray = new JSONArray(json);

            Log.e("KLineChartActivity", "数据长：" + jsonArray.length());

            float ma5 = 0;
            float ma10 = 0;
            float ma30 = 0;
            float ma45 = 0;
            float ma60 = 0;
            float ma90 = 0;

            float ma20 = 0;

            float volMa5 = 0;
            float volMa10 = 0;

            float ema12 = 0;
            float ema26 = 0;
            float dif = 0;
            float dea = 0;

            float K = 0;
            float D = 0;
            float J = 0;

            Float rsi = null;
            float rsiABSEma = 0;
            float rsiMaxEma = 0;

            Float r;
            Candle candle;
            Volume volume;
            Macd macd;
            for (int i = 0; i < jsonArray.length(); i++) {
                candle = new Candle();
                volume = new Volume();
                macd = new Macd();

                JSONArray arr = jsonArray.getJSONArray(i);
                candle.open = (float) arr.getDouble(0);
                candle.close = (float) arr.getDouble(1);
                candle.high = (float) arr.getDouble(2);
                candle.low = (float) arr.getDouble(3);
                candle.time = arr.getLong(4);

                volume.volume = (float) arr.getDouble(5);

                none.addData(candle);
                maLine.addData(candle);
                emaLine.addData(candle);
                bollLine.addData(candle);

                float close = candle.getClose();

                //计算ma
                ma5 += close;
                if (i >= 5) {
                    ICandle data = none.getData(i - 5);
                    ma5 -= data.getClose();
                }

                ma10 += close;
                if (i >= 10) {
                    ICandle data = none.getData(i - 10);
                    ma10 -= data.getClose();
                }

                ma30 += close;
                if (i >= 30) {
                    ICandle data = none.getData(i - 30);
                    ma30 -= data.getClose();
                }

                ma45 += close;
                if (i >= 45) {
                    ICandle data = none.getData(i - 45);
                    ma45 -= data.getClose();
                }

                ma60 += close;
                if (i >= 60) {
                    ICandle data = none.getData(i - 60);
                    ma60 -= data.getClose();
                }

                ma90 += close;
                if (i >= 90) {
                    ICandle data = none.getData(i - 90);
                    ma90 -= data.getClose();
                }

                Float MA5 = i < 4 ? null : ma5 / 5;
                Float MA10 = i < 9 ? null : ma10 / 10;
                Float MA30 = i < 29 ? null : ma30 / 30;
                Float MA45 = i < 44 ? null : ma45 / 45;
                Float MA60 = i < 59 ? null : ma60 / 60;
                Float MA90 = i < 89 ? null : ma90 / 90;
                maLine.addLinePoint(MA5, MA10, MA30, MA45, MA60, MA90);

                //计算boll
                ma20 += close;
                if (i < 19) {
                    bollLine.addLinePoint(null, null, null);
                } else {
                    if (i >= 20) {
                        ICandle data = none.getData(i - 20);
                        ma20 -= data.getClose();
                    }

                    float md = 0;
                    for (int j = i - 19; j <= i; j++) {
                        ICandle data = none.getData(j);

                        float c = data.getClose();
                        float value = c - ma20 / 20;
                        md += value * value;
                    }
                    md = md / 19;
                    md = (float) Math.sqrt(md);

                    bollLine.addLinePoint(ma20 / 20, ma20 / 20 + 2f * md, ma20 / 20 - 2f * md);
                }

                //计算成交量
                volumeLine.addData(volume);
                volMa5 += volume.getVolume();
                if (i >= 5) {
                    IVolume data = volumeLine.getData(i - 5);
                    volMa5 -= data.getVolume();
                }

                volMa10 += volume.getVolume();
                if (i >= 10) {
                    IVolume data = volumeLine.getData(i - 10);
                    volMa10 -= data.getVolume();
                }

                Float volMA5 = i < 4 ? null : volMa5 / 5;
                Float volMA10 = i < 9 ? null : volMa10 / 10;

                volumeLine.addLinePoint(volMA5, volMA10);

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
                macd.macd = (dif - dea) * 2f;
                macdLine.addData(macd);
                macdLine.addLinePoint(dif, dea);

                emaLine.addLinePoint(ema12, ema26);

                //计算kdj
                int startIndex = i - 13;
                if (startIndex < 0) {
                    startIndex = 0;
                }
                float max14 = Float.MIN_VALUE;
                float min14 = Float.MAX_VALUE;
                for (int index = startIndex; index <= i; index++) {
                    max14 = Math.max(max14, candle.getHigh());
                    min14 = Math.min(min14, candle.getLow());
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
                kdjLine.addLinePoint(K, D, J);

                //计算rsi
                if (i > 0) {
                    ICandle data = none.getData(i - 1);
                    float Rmax = Math.max(0, close - data.getClose());
                    float RAbs = Math.abs(close - data.getClose());

                    rsiMaxEma = (Rmax + (14f - 1) * rsiMaxEma) / 14f;
                    rsiABSEma = (RAbs + (14f - 1) * rsiABSEma) / 14f;
                    rsi = (rsiMaxEma / rsiABSEma) * 100;
                }
                if (i < 13) {
                    rsi = 0f;
                }
                if (rsi.isNaN())
                    rsi = 0f;

                rsiLine.addLinePoint(rsi);

                //计算wr
                startIndex = i - 14;
                if (startIndex < 0) {
                    startIndex = 0;
                }
                max14 = Float.MIN_VALUE;
                min14 = Float.MAX_VALUE;
                for (int index = startIndex; index <= i; index++) {
                    max14 = Math.max(max14, candle.getHigh());
                    min14 = Math.min(min14, candle.getLow());
                }
                if (i < 13) {
                    wrLine.addLinePoint(-10f);
                } else {
                    r = -100 * (max14 - close) / (max14 - min14);

                    wrLine.addLinePoint(r.isNaN() ? 0f : r);
                }
            }


        } catch (Exception ignored) {
        }

        mAdapter.addDataLineSet("", none)
                .addDataLineSet("ma", maLine)
                .addDataLineSet("ema", emaLine)
                .addDataLineSet("boll", bollLine)
                //添加子图
                .addDataLineSet("volume", volumeLine)
                .addDataLineSet("macd", macdLine)
                .addDataLineSet("kdj", kdjLine)
                .addDataLineSet("rsi", rsiLine)
                .addDataLineSet("wr", wrLine)
                .notifyDataSetChanged();

        mKLineChartView.setChild1Selected("volume");
    }

    public void onHideMain(View view) {
        rg_main.check(View.NO_ID);
    }

    public void onHideSub(View view) {
        rg_sub.check(View.NO_ID);
    }

    public void onSetting(View view) {
    }
}
