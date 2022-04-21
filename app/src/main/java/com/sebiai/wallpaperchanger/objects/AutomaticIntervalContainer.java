package com.sebiai.wallpaperchanger.objects;

import java.io.Serializable;

public class AutomaticIntervalContainer implements Serializable {
    public int intervalTimeHours;
    public int intervalTimeMinutes;

    public int intervalStartHours;
    public int intervalStartMinutes;

    public AutomaticIntervalContainer() {
        intervalTimeHours = 0;
        intervalTimeMinutes = 15;

        intervalStartHours = 0;
        intervalStartMinutes = 0;
    }
}
