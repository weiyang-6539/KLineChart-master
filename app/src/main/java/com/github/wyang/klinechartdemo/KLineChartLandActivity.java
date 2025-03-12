package com.github.wyang.klinechartdemo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.wyang.klinechartdemo.utils.AssetUtil;
import com.github.wyang.klinechartlib.huobi.KLineChartAdapter;
import com.github.wyang.klinechartlib.huobi.KLineChartView;
import com.github.wyang.klinechartlib.huobi.data.KLineEntity;
import com.github.wyang.klinechartlib.huobi.data.DataLineSetProvider;
import com.github.wyang.klinechartlib.huobi.draw.MainDraw;

import org.json.JSONArray;

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
        mAdapter.bindToDataLineSetProvider(new DataLineSetProvider());

        initKLineData(this);

        rg_main = findViewById(R.id.rg_main);
        rg_sub = findViewById(R.id.rg_sub);

        btn_main_hide = findViewById(R.id.btn_main_hide);
        btn_sub_hide = findViewById(R.id.btn_sub_hide);

        rg_main.setOnCheckedChangeListener((group, checkedId) -> {
            btn_main_hide.setSelected(checkedId != View.NO_ID);
            if (checkedId == R.id.rb_ma) {
                mKLineChartView.setMainSelected("ma");
            } else if (checkedId == R.id.rb_ema) {
                mKLineChartView.setMainSelected("ema");
            } else if (checkedId == R.id.rb_boll) {
                mKLineChartView.setMainSelected("boll");
            } else {
                mKLineChartView.setMainSelected("");
            }
        });
        rg_main.check(R.id.rb_ma);

        rg_sub.setOnCheckedChangeListener((group, checkedId) -> {
            btn_sub_hide.setSelected(checkedId != View.NO_ID);
            mKLineChartView.setShowChild2(checkedId != View.NO_ID);

            if (checkedId == R.id.rb_macd) {
                mKLineChartView.setChild2Selected("macd");
            } else if (checkedId == R.id.rb_kdj) {
                mKLineChartView.setChild2Selected("kdj");
            } else if (checkedId == R.id.rb_rsi) {
                mKLineChartView.setChild2Selected("rsi");
            } else if (checkedId == R.id.rb_wr) {
                mKLineChartView.setChild2Selected("wr");
            } else if (checkedId == R.id.rb_boll_child) {
                mKLineChartView.setChild2Selected("boll");
            }
        });
        rg_sub.check(R.id.rb_macd);
    }

    public void onClose(View v) {
        finish();
    }

    public void onHideMain(View view) {
        rg_main.check(View.NO_ID);
    }

    public void onHideSub(View view) {
        rg_sub.check(View.NO_ID);
    }

    public void onSetting(View view) {
    }


    private void initKLineData(Context context) {
        try {
            String json = AssetUtil.readAsset(context, "test.json");
            JSONArray jsonArray = new JSONArray(json);

            List<KLineEntity> list = new ArrayList<>();
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

            mAdapter.setNewData(list);

            mKLineChartView.setMainSelected("ma");
            mKLineChartView.setChild1Selected("volume");
            mKLineChartView.setChild2Selected("macd");
        } catch (Exception ignored) {
        }

    }

}
