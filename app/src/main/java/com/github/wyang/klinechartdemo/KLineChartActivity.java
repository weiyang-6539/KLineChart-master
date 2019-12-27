package com.github.wyang.klinechartdemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.wyang.klinechartdemo.bean.Macd;
import com.github.wyang.klinechartdemo.bean.Volume;
import com.github.wyang.klinechartdemo.utils.AssetUtil;
import com.github.wyang.klinechartdemo.widget.CommonPopupWindow;
import com.github.wyang.klinechartlib.data.IVolume;
import com.github.wyang.klinechartlib.huobi.interfaces.IDataLineSet;
import com.github.wyang.klinechartlib.data.ICandle;
import com.github.wyang.klinechartlib.huobi.KLineChartAdapter;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.data.DataLineSet;
import com.github.wyang.klinechartdemo.bean.Candle;
import com.github.wyang.klinechartlib.huobi.draw.MainDraw;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weiyang on 2019-11-01.
 */
public class KLineChartActivity extends AppCompatActivity {
    private TextView tv_title;
    private TabLayout mTabLayout;
    private KLineChartView mKLineChartView;
    private KLineChartAdapter mAdapter;

    private int pos;
    private TextView tv_buy, tv_sell;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置竖屏Activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_k_line);

        tv_title = findViewById(R.id.tv_title);
        mTabLayout = findViewById(R.id.mTabLayout);
        mKLineChartView = findViewById(R.id.mKLineChartView);
        tv_buy = findViewById(R.id.tv_buy);
        tv_sell = findViewById(R.id.tv_sell);

        tv_title.setText("BTC/USDT");

        //设置买卖按钮颜色
        ViewCompat.setBackgroundTintList(tv_buy,
                getResources().getColorStateList(R.color.chart_red));
        ViewCompat.setBackgroundTintList(tv_sell,
                getResources().getColorStateList(R.color.chart_green));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.k_line_tab01)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.k_line_tab02)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.k_line_tab03)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.k_line_tab04)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.k_line_tab05)));

        //添加选择更多Tab
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.item_tab_k_line_more));
        TabLayout.Tab tab5 = mTabLayout.getTabAt(posMore);
        if (tab5 != null && tab5.getCustomView() != null) {
            tab5.getCustomView().setOnClickListener(v -> onClickMore());
            tv_label = tab5.getCustomView().findViewById(R.id.tv_label);
            iv_label = tab5.getCustomView().findViewById(R.id.iv_label);
        }
        //添加设置指标线Tab
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.item_tab_k_line_setting));
        TabLayout.Tab tab6 = mTabLayout.getTabAt(posSetting);
        if (tab6 != null && tab6.getCustomView() != null) {
            tab6.getCustomView().setOnClickListener(v -> onClickSetting());
            iv_setting = tab6.getCustomView().findViewById(R.id.iv_label);
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() < posMore) {
                    pos = tab.getPosition();
                } else if (tab.getPosition() == posMore) {
                    setTabMoreColor(R.color.theme0xff);
                }
                if (tab.getPosition() == 0) {
                    mKLineChartView.setMode(MainDraw.Mode.LINE);
                } else {
                    mKLineChartView.setMode(MainDraw.Mode.CANDLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == posMore) {
                    tv_label.setText(R.string.k_line_tab06);
                    setTabMoreColor(R.color.light);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        TabLayout.Tab tab = mTabLayout.getTabAt(0);
        if (tab != null)
            tab.select();


        //mKLineChartView.setCandleFill(false);

        mAdapter = new KLineChartAdapter();
        mAdapter.bindToKLineChartView(mKLineChartView);

        initKLineData(this);
    }

    public void onBack(View view) {
        finish();
    }

    private TextView tv_label;
    private ImageView iv_label;
    private int posMore = 5;

    private void setTabMoreColor(@ColorRes int resId) {
        tv_label.setTextColor(getResources().getColor(resId));
        ViewCompat.setBackgroundTintList(iv_label, getResources().getColorStateList(resId));
    }

    public void onClickMore() {
    }

    private ImageView iv_setting;
    private int posSetting = 6;
    // 主图指标下标
    private int mainIndex = 0;
    // 副图指标下标
    private int subIndex = -1;

    private void setTabSettingColor(@ColorRes int resId) {
        ViewCompat.setBackgroundTintList(iv_setting, getResources().getColorStateList(resId));
    }

    private ItemTabAdapter mainItemTabAdapter = new ItemTabAdapter();
    private ItemTabAdapter subItemTabAdapter = new ItemTabAdapter();

    public void onClickSetting() {
        CommonPopupWindow popupWindow = new CommonPopupWindow.Builder(this)
                .setView(R.layout.popup_setting)
                .setOutsideTouchable(true)
                .setWidthAndHeight(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .setViewOnclickListener((view, layoutResId) -> {
                    RecyclerView rv_main = view.findViewById(R.id.rv_main);
                    rv_main.setAdapter(mainItemTabAdapter);
                    RecyclerView rv_sub = view.findViewById(R.id.rv_sub);
                    rv_sub.setAdapter(subItemTabAdapter);
                })
                .create();
        popupWindow.showAsDropDown(mTabLayout);
    }

    private void initKLineData(Context context) {
        int color1 = ContextCompat.getColor(this, R.color.chart_line1);
        int color2 = ContextCompat.getColor(this, R.color.chart_line2);
        int color3 = ContextCompat.getColor(this, R.color.chart_line3);

        DataLineSet none = new DataLineSet();

        DataLineSet maLine = new DataLineSet();
        maLine.setName("MA");
        maLine.addLine(color1, "MA5:");
        maLine.addLine(color2, "MA10:");
        maLine.addLine(color3, "MA30:");

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
        macdLine.addLine(color1, "DIF:");
        macdLine.addLine(color2, "DEA:");

        DataLineSet kdjLine = new DataLineSet();
        kdjLine.setName("KDJ(14,1,3)");
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
            String json = AssetUtil.readAsset(context, "little.json");
            JSONArray jsonArray = new JSONArray(json);

            Log.e("KLineChartActivity", "数据长：" + jsonArray.length());

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
                Float MA5 = i < 4 ? null : ma5 / 5;
                Float MA10 = i < 9 ? null : ma10 / 10;
                Float MA30 = i < 29 ? null : ma30 / 30;
                maLine.addLinePoint(MA5, MA10, MA30);

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

                    bollLine.addLinePoint(ma20 / 20, ma20 / 20 + 2 * md, ma20 / 20 - 2f * md);
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

                    wrLine.addLinePoint(r.isNaN() ? 0 : r);
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

        mKLineChartView.setMainSelected("ma");
        mKLineChartView.setChild1Selected("kdj");
        mKLineChartView.setChild2Selected("macd");
    }

    public void onLandscape(View view) {
        startActivity(new Intent(this, KLineChartLandActivity.class));
    }

    public static class ItemTabAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
        private int selectPosition = 0;

        ItemTabAdapter() {
            super(R.layout.item_tab);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.tv_label, item);
            helper.getView(R.id.tv_label).setSelected(selectPosition == helper.getAdapterPosition());
        }
    }
}
