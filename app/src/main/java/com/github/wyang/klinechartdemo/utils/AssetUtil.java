package com.github.wyang.klinechartdemo.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by weiyang on 2019-11-04.
 */
public class AssetUtil {

    public static String readAsset(Context context, String filename) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = context.getAssets().open(filename);

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            is.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
