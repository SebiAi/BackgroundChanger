package com.sebiai.wallpaperchanger.fragments;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sebiai.wallpaperchanger.utils.MyFileHandler;
import com.sebiai.wallpaperchanger.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private Button buttonSetRandomWallpaper;
    private ImageView imageViewWallpaper;

    private SharedPreferences sharedPreferences;

    private Lifecycle.State lastState;

    RequestOptions options = new RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.default_wallpaper)
            .error(R.drawable.default_wallpaper);

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
        lastState = this.getLifecycle().getCurrentState();
    }

    private void updatePreferenceValues() {
        // Get uri
        Uri currentWallpaperUri = MyFileHandler.getCurrentWallpaperUri(requireContext());

        // Check last wallpaper drawable cache
        if (getMyApplication(requireContext()).wallpaperDrawableCache == null) {
            // Set from uri
            getMyApplication(requireContext()).wallpaperDrawableCache = MyFileHandler.getDrawableFromWallpaperUri(requireContext(), currentWallpaperUri);
        }
        setWallpaper();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register Listeners
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        // If not previously updated by setup()
        if (lastState == Lifecycle.State.STARTED) {
            // Check if uri is still valid
            buttonSetRandomWallpaper.setEnabled(MyFileHandler.isWallpaperDirValid(requireContext()));
            // Update Preferences
            updatePreferenceValues();
        }
        lastState = this.getLifecycle().getCurrentState();
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
                setWallpaper();
            }
        });

        imageViewWallpaper = requireView().findViewById(R.id.image_view_wallpaper);

        updatePreferenceValues();

        // Check if uri is still valid
        buttonSetRandomWallpaper.setEnabled(MyFileHandler.isWallpaperDirValid(requireContext()));
    }

    private void setWallpaper() {
        if (getMyApplication(requireContext()).wallpaperDrawableCache != null)
            Glide.with(requireContext()).load(getMyApplication(requireContext()).wallpaperDrawableCache).apply(options).into(imageViewWallpaper);
    }

    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (sharedPreferences, key) -> {
        if (key.equals(getString(R.string.key_current_picture))) {
            Uri lastWallpaperUri = Uri.parse(sharedPreferences.getString(getString(R.string.key_current_picture), null));
            // Update drawable
            getMyApplication(requireContext()).wallpaperDrawableCache = MyFileHandler.getDrawableFromWallpaperUri(requireContext(), lastWallpaperUri);
            setWallpaper();
        }
    };
}