package com.github.wyang.klinechartdemo;

import android.animation.ValueAnimator;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.wyang.klinechartdemo.utils.AssetUtil;
import com.github.wyang.klinechartdemo.widget.CommonPopupWindow;
import com.github.wyang.klinechartlib.huobi.KLineChartAdapter;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.data.DataLineSetProvider;
import com.github.wyang.klinechartlib.huobi.data.KLineEntity;
import com.github.wyang.klinechartlib.huobi.draw.MainDraw;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fxb on 2019-11-01.
 */
public class KLineChartActivity extends AppCompatActivity {
    private TextView tv_title;
    private TabLayout mTabLayout;
    private KLineChartView mKLineChartView;
    private KLineChartAdapter mAdapter;

    private int pos;
    private TextView tv_buy, tv_sell;
    private List<KLineEntity> data;

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
        mKLineChartView.setOnRefreshListener(new KLineChartView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(() -> mKLineChartView.refreshComplete(), 2000);
            }

            @Override
            public void onLoadMore() {
                new Handler().postDelayed(() -> {
                    List<KLineEntity> kLineData = getKLineData("little2.json");
                    mAdapter.addData(kLineData);

                    mKLineChartView.refreshComplete();
                }, 2000);
            }
        });

        mAdapter = new KLineChartAdapter();
        mAdapter.bindToKLineChartView(mKLineChartView);
        mAdapter.bindToDataLineSetProvider(new DataLineSetProvider());

        data = getKLineData("little.json");
        mAdapter.setNewData(data);
        handler.postDelayed(runnable, 3000);

        mKLineChartView.setMainSelected("ma");
        mKLineChartView.setChild1Selected("volume");
        mKLineChartView.setChild2Selected("wr");
        mKLineChartView.setMode(MainDraw.Mode.LINE);
    }

    private Handler handler = new Handler();

    private Runnable runnable = this::updateLast;

    private int i = 0;

    private void sendNewData() {
        if (i == data.size())
            i = 0;
        KLineEntity entity = data.get(i++);
        mAdapter.addData(entity, false);

        handler.postDelayed(runnable, 3000);
    }

    private void updateLast() {
        KLineEntity entity = data.get(data.size() - 1);
        float newClose = (float) (entity.open + (Math.random() * 50 - 25));

        float oldClose = entity.close;
        float diff = newClose - oldClose;
        ValueAnimator animator = ValueAnimator.ofFloat(1f);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            entity.update(oldClose + value * diff);
            mAdapter.addData(entity, true);
        });
        animator.start();

        Log.e("KLine", "最新收盘价=" + entity.close);
        handler.postDelayed(runnable, 3000);
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

    private List<KLineEntity> getKLineData(String filename) {
        List<KLineEntity> list = new ArrayList<>();

        try {
            String json = AssetUtil.readAsset(this, filename);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray arr = jsonArray.getJSONArray(i);

                KLineEntity entity = new KLineEntity();
                entity.open = (float) arr.getDouble(0);
                entity.close = (float) arr.getDouble(1);
                entity.high = (float) arr.getDouble(2);
                entity.low = (float) arr.getDouble(3);
                entity.time = arr.getLong(4);
                entity.vol = (float) arr.getDouble(5);

                list.add(entity);
            }

        } catch (Exception ignored) {
        }
        return list;
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
