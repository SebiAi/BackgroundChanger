package com.sebiai.wallpaperchanger;

import android.app.Application;
import android.graphics.drawable.Drawable;

public class MyApplication extends Application {
    public Drawable wallpaperDrawableCache = null; // TODO: Replace with better UI State preserving technique (https://developer.android.com/topic/libraries/architecture/saving-states)
    public String wallpaperFileName = null;
}
