package com.sebiai.wallpaperchanger;

import android.content.Context;

public class MyApplicationHelper {
    public static MyApplication getMyApplication(Context context) {
        return (MyApplication) context.getApplicationContext();
    }
}
