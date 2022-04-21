package com.sebiai.wallpaperchanger;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.sebiai.wallpaperchanger.worker.AutoWallpaperChangerWorker;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Toolbar customToolbar;
    private SwitchCompat switchAutoChange;
    private NavController navController;
    private BottomNavigationView bottomNav;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Test for optimization // TODO: Implement this in setup process (https://stackoverflow.com/questions/39256501/check-if-battery-optimization-is-enabled-or-not-for-an-app)
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        boolean isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
        Toast.makeText(getApplicationContext(), "Battery optimizations enabled: " + !isIgnoringBatteryOptimizations, Toast.LENGTH_LONG).show();

        // Check if uri is still valid
        if (!MyFileHandler.isWallpaperDirValid(this)) {
            // Disable
            switchAutoChange.setChecked(false);
            onClickSwitchAutoChange(switchAutoChange);
            switchAutoChange.setEnabled(false);
            // Clear related preferences
            sharedPreferences.edit().
                    remove(getString(R.string.key_wallpaper_dir)).
                    remove(getString(R.string.key_current_picture)).
                    remove(getString(R.string.key_auto_change_enabled)).
                    apply();
            // Clear cache
            getMyApplication(this).wallpaperDrawableCache = null;
            getMyApplication(this).wallpaperFileName = null;
        }

        // Load auto wallpaper
        boolean isChecked = sharedPreferences.getBoolean(getString(R.string.key_auto_change_enabled), false);
        switchAutoChange.setChecked(isChecked);
        // Check if not running
        if (!isWorkScheduled(getString(R.string.worker_tag_auto_wallpaper_changer)) && isChecked) {
            // Start again
            startWorker();
        }
    }

    private void setup() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        customToolbar = findViewById(R.id.toolbar_custom);
        setSupportActionBar(customToolbar);

        switchAutoChange = findViewById(R.id.switch_enable_auto_change);
        switchAutoChange.setOnClickListener(this::onClickSwitchAutoChange);

        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))).getNavController();

        bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    private void startWorker() { // Use AlarmManager (inexact but when idle) for better scheduling
        PeriodicWorkRequest autoWallpaperChangerWorkRequest =
                new PeriodicWorkRequest.Builder(AutoWallpaperChangerWorker.class, 15, TimeUnit.MINUTES).
                        setInitialDelay(5, TimeUnit.SECONDS).
                        addTag(getString(R.string.worker_tag_auto_wallpaper_changer)).
                        build();
        WorkManager.getInstance(this).
                enqueueUniquePeriodicWork(
                        getString(R.string.worker_tag_auto_wallpaper_changer),
                        ExistingPeriodicWorkPolicy.KEEP,
                        autoWallpaperChangerWorkRequest);
    }

    private boolean isWorkScheduled(String tag) {
        WorkManager instance = WorkManager.getInstance(this);
        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(tag);
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
            }
            return running;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void onClickSwitchAutoChange(View v) {
        boolean isEnabled = ((SwitchCompat) v).isChecked();
        sharedPreferences.edit().
                putBoolean(getString(R.string.key_auto_change_enabled), isEnabled).
                apply();
        if (isEnabled) {
            // Start Work
            startWorker();
        } else {
            // Stop Work
            WorkManager.getInstance(this).cancelUniqueWork(getString(R.string.worker_tag_auto_wallpaper_changer));
        }
    }
}