package com.sebiai.wallpaperchanger.fragments;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;
import com.sebiai.wallpaperchanger.MyFileHandler;
import com.sebiai.wallpaperchanger.R;
import com.sebiai.wallpaperchanger.worker.AutoWallpaperChangerWorker;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private Button buttonSetRandomWallpaper;
    private TextView textViewCurrentWallpaper;
    private FrameLayout frameLayout;
    private SwitchCompat switchAutoChange;

    private SharedPreferences sharedPreferences;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();

        // Load Uri
        if (sharedPreferences.contains(getString(R.string.key_wallpaper_dir))) {
            getMyApplication(requireContext()).wallpaperDir = Uri.parse(sharedPreferences.getString(getString(R.string.key_wallpaper_dir), null));
            buttonSetRandomWallpaper.setEnabled(true);
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

    private boolean isWorkScheduled(String tag) {
        WorkManager instance = WorkManager.getInstance(requireContext());
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

    private void setFromCache() {
        String stringUri = sharedPreferences.getString(getString(R.string.key_current_picture), null);

        Uri lastWallpaperUri = null;
        if (stringUri != null)
            lastWallpaperUri = Uri.parse(stringUri);

        // Check file name cache
        if (getMyApplication(requireContext()).wallpaperFileName == null) {
            // Set from uri
            getMyApplication(requireContext()).wallpaperFileName = MyFileHandler.getNameFromWallpaperUri(requireContext(), lastWallpaperUri);
        }
        setCurrentWallpaperName(getMyApplication(requireContext()).wallpaperFileName);

        // Check last wallpaper drawable cache
        if (getMyApplication(requireContext()).wallpaperDrawableCache == null) {
            // Set from uri
            getMyApplication(requireContext()).wallpaperDrawableCache = MyFileHandler.getDrawableFromWallpaperUri(requireContext(), lastWallpaperUri);
        }
        frameLayout.setBackground(getMyApplication(requireContext()).wallpaperDrawableCache);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if uri is still valid
        if (!MyFileHandler.isWallpaperDirValid(requireContext())) {
            buttonSetRandomWallpaper.setEnabled(false);
        }

        // Register Listeners
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        // Load from cache
        setFromCache();
    }

    @Override
    public void onPause() {
        super.onPause();

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    private void setup() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        buttonSetRandomWallpaper = requireView().findViewById(R.id.button_set_random_wallpaper);
        buttonSetRandomWallpaper.setOnClickListener(v -> {
            // Set file as wallpaper
            DocumentFile file = MyFileHandler.setRandomFileAsWallpaper(requireContext(), getMyApplication(requireContext()).wallpaperDir);
            if (file != null) {
                String fileName = file.getName();
                // Display file name
                setCurrentWallpaperName(fileName);
                // Save preferences
                int amountChangesManual = sharedPreferences.getInt(getString(R.string.key_amount_changes_manual), 0);
                sharedPreferences.edit().
                        putString(getString(R.string.key_current_picture), file.getUri().toString()).
                        putInt(getString(R.string.key_amount_changes_manual), ++amountChangesManual).
                        apply();
                // Save cache
                getMyApplication(requireContext()).wallpaperDrawableCache = MyFileHandler.getDrawableFromWallpaperUri(requireContext(), file.getUri());
                getMyApplication(requireContext()).wallpaperFileName = fileName;
                // Set wallpaper as fragment background
                frameLayout.setBackground(getMyApplication(requireContext()).wallpaperDrawableCache);
            }
        });

        textViewCurrentWallpaper = requireView().findViewById(R.id.textview_current_wallpaper);
        setCurrentWallpaperName("-");

        frameLayout = requireView().findViewById(R.id.frame_layout_home_fragment);

        switchAutoChange = requireView().findViewById(R.id.switch_enable_auto_change);
        switchAutoChange.setOnClickListener(v -> {
            boolean isEnabled = ((SwitchCompat) v).isChecked();
            sharedPreferences.edit().
                    putBoolean(getString(R.string.key_auto_change_enabled), isEnabled).
                    apply();
            if (isEnabled) {
                // Start Work
                startWorker();
            } else {
                // Stop Work
                WorkManager.getInstance(requireContext()).cancelUniqueWork(getString(R.string.worker_tag_auto_wallpaper_changer));
            }
        });
    }

    private void startWorker() { // Use AlarmManager (inexact but when idle) for better scheduling
        PeriodicWorkRequest autoWallpaperChangerWorkRequest =
                new PeriodicWorkRequest.Builder(AutoWallpaperChangerWorker.class, 15, TimeUnit.MINUTES).
                        setInitialDelay(5, TimeUnit.SECONDS).
                        addTag(getString(R.string.worker_tag_auto_wallpaper_changer)).
                        build();
        WorkManager.getInstance(requireContext()).
                enqueueUniquePeriodicWork(
                        getString(R.string.worker_tag_auto_wallpaper_changer),
                        ExistingPeriodicWorkPolicy.KEEP,
                        autoWallpaperChangerWorkRequest);
    }

    private void setCurrentWallpaperName(String fileName) {
        textViewCurrentWallpaper.setText(String.format(getString(R.string.textview_current_wallpaper_string), fileName));
    }

    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.key_current_picture))) {
                Uri lastWallpaperUri = Uri.parse(sharedPreferences.getString(getString(R.string.key_current_picture), null));
                // Update name
                getMyApplication(requireContext()).wallpaperFileName = MyFileHandler.getNameFromWallpaperUri(requireContext(), lastWallpaperUri);
                setCurrentWallpaperName(getMyApplication(requireContext()).wallpaperFileName);
                // Update drawable
                getMyApplication(requireContext()).wallpaperDrawableCache = MyFileHandler.getDrawableFromWallpaperUri(requireContext(), lastWallpaperUri);
                frameLayout.setBackground(getMyApplication(requireContext()).wallpaperDrawableCache);
            }
        }
    };
}