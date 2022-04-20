package com.sebiai.wallpaperchanger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNav;

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
    }

    private void setup() {
        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))).getNavController();

        bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }
}