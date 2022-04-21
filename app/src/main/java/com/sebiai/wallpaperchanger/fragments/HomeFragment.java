package com.sebiai.wallpaperchanger.fragments;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sebiai.wallpaperchanger.MyFileHandler;
import com.sebiai.wallpaperchanger.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private Button buttonSetRandomWallpaper;
    private TextView textViewCurrentWallpaper;
    private FrameLayout frameLayout;

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
    }

    private void updatePreferenceValues() {
        // Get uri
        Uri currentWallpaperUri = MyFileHandler.getCurrentWallpaperUri(requireContext());

        // Check file name cache
        if (getMyApplication(requireContext()).wallpaperFileName == null) {
            // Set from uri
            getMyApplication(requireContext()).wallpaperFileName = MyFileHandler.getNameFromWallpaperUri(requireContext(), currentWallpaperUri);
        }
        setCurrentWallpaperName(getMyApplication(requireContext()).wallpaperFileName);

        // Check last wallpaper drawable cache
        if (getMyApplication(requireContext()).wallpaperDrawableCache == null) {
            // Set from uri
            getMyApplication(requireContext()).wallpaperDrawableCache = MyFileHandler.getDrawableFromWallpaperUri(requireContext(), currentWallpaperUri);
        }
        frameLayout.setBackground(getMyApplication(requireContext()).wallpaperDrawableCache);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if uri is still valid
        buttonSetRandomWallpaper.setEnabled(MyFileHandler.isWallpaperDirValid(requireContext()));

        // Register Listeners
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        // Update Preferences
        updatePreferenceValues();
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
            DocumentFile file = MyFileHandler.setRandomFileAsWallpaper(requireContext(), MyFileHandler.getWallpaperDirUri(requireContext()));
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