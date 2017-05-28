package com.example.huwang.breakpoint;

import android.app.Application;
import android.content.Context;

/**
 * Created by huwang on 2017/5/28.
 */

public class MyApplication extends Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
