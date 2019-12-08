package com.github.wyang.klinechartdemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
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
import com.github.wyang.klinechartdemo.utils.AssetUtil;
import com.github.wyang.klinechartdemo.widget.CommonPopupWindow;
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
                    mKLineChartView.setMode(MainRect.Mode.LINE);
                } else {
                    mKLineChartView.setMode(MainRect.Mode.CANDLE);
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
        mAdapter.bindToChartView(mKLineChartView);

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
        List<ICandle> candles = new ArrayList<>();
        List<IBarLineSet> mainBarLineSets = new ArrayList<>();

        BarLineSet maLine = new BarLineSet("MA");
        maLine.addLine(0xFFDA8AE5, "MA5:");
        maLine.addLine(0xFF39B0E8, "MA10:");
        maLine.addLine(0xFFFFC76D, "MA30:");

        BarLineSet bollLine = new BarLineSet("BOLL");
        bollLine.addLine(0xFFDA8AE5, "BOLL:");//中轨线
        bollLine.addLine(0xFF39B0E8, "UB:");//上轨线
        bollLine.addLine(0xFFFFC76D, "LB:");//下轨线

        mainBarLineSets.add(maLine);
        mainBarLineSets.add(bollLine);

        for (IBarLineSet barLineSet : mainBarLineSets) {
            mainItemTabAdapter.addData(barLineSet.getName());
        }

        List<IBarLineSet> child1BarLineSets = new ArrayList<>();

        BarLineSet volumeLine = new BarLineSet("成交量");
        volumeLine.addLine(0xFFDA8AE5, "MA5:");
        volumeLine.addLine(0xFF39B0E8, "MA10:");
        child1BarLineSets.add(volumeLine);


        List<IBarLineSet> child2BarLineSets = new ArrayList<>();

        BarLineSet macdLine = new BarLineSet("MACD");
        child2BarLineSets.add(macdLine);


        BarLineSet kdjLine = new BarLineSet("KDJ");
        child2BarLineSets.add(kdjLine);


        BarLineSet rsiLine = new BarLineSet("RSI");
        child2BarLineSets.add(rsiLine);


        BarLineSet wrLine = new BarLineSet("WR");
        child2BarLineSets.add(wrLine);

        for (IBarLineSet barLineSet : child2BarLineSets) {
            subItemTabAdapter.addData(barLineSet.getName());
        }


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

                ma5 += entity.getClose();
                if (i == 4) {
                    maLine.getLine(0).add(ma5 / 5);
                } else if (i >= 5) {
                    ma5 -= candles.get(i - 5).getClose();
                    maLine.getLine(0).add(ma5 / 5);
                } else {
                    maLine.getLine(0).add(null);
                }
                ma10 += entity.getClose();
                if (i == 9) {
                    maLine.getLine(1).add(ma10 / 10);
                } else if (i >= 10) {
                    ma10 -= candles.get(i - 10).getClose();
                    maLine.getLine(1).add(ma10 / 10);
                } else {
                    maLine.getLine(1).add(null);
                }
                ma30 += entity.getClose();
                if (i == 29) {
                    maLine.getLine(2).add(ma30 / 30);
                } else if (i >= 30) {
                    ma30 -= candles.get(i - 30).getClose();
                    maLine.getLine(2).add(ma30 / 30);
                } else {
                    maLine.getLine(2).add(null);
                }

                ma20 += entity.getClose();
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
            }
        } catch (Exception ignored) {
        }

        mAdapter.setData(candles);
        mAdapter.setMainLineSets(mainBarLineSets);
        mAdapter.setChild1LineSets(child1BarLineSets);
        mAdapter.setChild1LineSets(child2BarLineSets);

        ICandle candle = mAdapter.getCandle(mAdapter.getCount() - 1);
        original = candle.getClose();

        //mHandler.postDelayed(runnable, 1000);
    }

    private float original;
    private Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            float v = (float) (Math.random() * 10 - 5);
            Candle candle = (Candle) mAdapter.getCandle(mAdapter.getCount() - 1);
            candle.close = original + v;
            candle.volume += 1;

            mAdapter.notifyDataSetInvalidated();

            mHandler.postDelayed(this, 1000);
        }
    };

    public void onLandscape(View view) {
        startActivity(new Intent(this, KLineChartLandActivity.class));
    }

    public static class ItemTabAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
        private int selectPosition = 0;

        public ItemTabAdapter() {
            super(R.layout.item_tab);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.tv_label, item);
            helper.getView(R.id.tv_label).setSelected(selectPosition == helper.getAdapterPosition());
        }
    }
}
