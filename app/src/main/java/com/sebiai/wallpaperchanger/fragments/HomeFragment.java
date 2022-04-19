package com.sebiai.wallpaperchanger.fragments;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sebiai.wallpaperchanger.MyFileHandler;
import com.sebiai.wallpaperchanger.R;
import com.sebiai.wallpaperchanger.worker.AutoWallpaperChangerWorker;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Button buttonChooseDir;
    private Button buttonSetRandomWallpaper;
    private TextView textViewCurrentWallpaper;
    private final ActivityResultLauncher<Uri> uriActivityResultLauncher;
    private FrameLayout frameLayout;
    private SwitchCompat switchAutoChange;

    private SharedPreferences sharedPreferences;

    public HomeFragment() {
        // Required empty public constructor
        uriActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), result -> {
            if (result == null)
                return;

            // Make persistent
            requireActivity().getContentResolver().takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Save globally and in preferences
            getMyApplication(requireContext()).wallpaperDir = result;
            sharedPreferences.edit().putString(getString(R.string.key_wallpaper_dir), result.toString()).apply();

            // Enable button
            buttonSetRandomWallpaper.setEnabled(true);
        });
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
        switchAutoChange.setChecked(sharedPreferences.getBoolean(getString(R.string.key_auto_change_enabled), false));
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

        buttonChooseDir = requireView().findViewById(R.id.button_choose_dir);
        buttonChooseDir.setOnClickListener(v -> uriActivityResultLauncher.launch(Uri.parse("image/*")));

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
                // TODO: Use AlarmManager instead, more reliable or test what happens if battery optimisation for this app is disabled
                // Enable Work
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
            } else {
                // Disable Work
                WorkManager.getInstance(requireContext()).cancelUniqueWork(getString(R.string.worker_tag_auto_wallpaper_changer));
            }
        });
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