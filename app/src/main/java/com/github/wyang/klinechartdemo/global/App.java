package com.github.wyang.klinechartdemo.global;

import android.app.Application;

import com.zhouyou.http.EasyHttp;

/**
 * Created by fxb on 2019-12-13.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        EasyHttp.init(this);
    }
}
