package com.sebiai.wallpaperchanger;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class MyApplication extends Application {
    public Uri wallpaperDir = null;
    public Drawable wallpaperDrawableCache = null;
    public String wallpaperFileName = null;
}
