package com.sebiai.wallpaperchanger.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.sebiai.wallpaperchanger.utils.MyFileHandler;
import com.sebiai.wallpaperchanger.R;
import com.sebiai.wallpaperchanger.dialogs.ConfigureAutomaticModeDialogFragment;
import com.sebiai.wallpaperchanger.objects.AutomaticIntervalContainer;
import com.sebiai.wallpaperchanger.utils.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat implements ConfigureAutomaticModeDialogFragment.OnConfigureAutomaticModeDialogDismissListener {
    private ActivityResultLauncher<Uri> uriActivityResultLauncher;
    private SharedPreferences sharedPreferences;

    private SwitchCompat switchAutoChange;

    private Preference preferenceWallpaperDir;
    private Preference preferenceIntervalTime;

    private Lifecycle.State lastState;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setup();

        uriActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), result -> {
            if (result == null)
                return;

            // Make old not persistent any more
            ContentResolver cs = requireActivity().getContentResolver();
            List<UriPermission> persistedUriPermissions = cs.getPersistedUriPermissions();
            for (UriPermission persistedUriPermission : persistedUriPermissions) {
                cs.releasePersistableUriPermission(persistedUriPermission.getUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // Make persistent
            requireActivity().getContentResolver().takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Save preference
            sharedPreferences.edit().putString(getString(R.string.key_wallpaper_dir), result.toString()).apply();

            // Refresh preference
            setPreferenceSummary(preferenceWallpaperDir, String.format(getString(R.string.preference_wallpaper_dir_summary_string),
                    FileUtil.getFullPathFromTreeUri(result, requireContext())));

            // Enable toggle switch
            switchAutoChange.setEnabled(true);
        });

        lastState = this.getLifecycle().getCurrentState();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (lastState == Lifecycle.State.STARTED)
            updatePreferenceValues();
        lastState = this.getLifecycle().getCurrentState();
    }

    private void setup() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        preferenceWallpaperDir = findPreference(getString(R.string.key_wallpaper_dir));
        if (preferenceWallpaperDir != null) {
            preferenceWallpaperDir.setOnPreferenceClickListener(this::onPreferenceClick);
        }

        preferenceIntervalTime = findPreference(getString(R.string.key_automatic_interval));
        if (preferenceIntervalTime != null) {
            preferenceIntervalTime.setOnPreferenceClickListener(this::onPreferenceClick);
        }

        switchAutoChange = requireActivity().findViewById(R.id.switch_enable_auto_change);

        updatePreferenceValues();
    }

    private void setPreferenceSummary(Preference preference, String summary) {
        if (preference != null)
            preference.setSummary(summary);
    }

    private void updatePreferenceValues() {
        // Current Wallpaper Dir
        String wallpaperDir = "-";

        Uri wallpaperDirUri = MyFileHandler.getWallpaperDirUri(requireContext());
        if (wallpaperDirUri != null)
            wallpaperDir = FileUtil.getFullPathFromTreeUri(wallpaperDirUri, requireContext());

        setPreferenceSummary(preferenceWallpaperDir, String.format(getString(R.string.preference_wallpaper_dir_summary_string),
                wallpaperDir));

        // Automatic Interval
        AutomaticIntervalContainer automaticIntervalContainer = new AutomaticIntervalContainer();
        String automaticIntervalContainerString = sharedPreferences.getString(getString(R.string.key_automatic_interval), null);

        if (automaticIntervalContainerString != null) {
            // Deserialize
            try {
                automaticIntervalContainer = (AutomaticIntervalContainer) fromString(automaticIntervalContainerString);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        String preferenceSummary = String.format(getString(R.string.preference_interval_start_time_summary_string),
                automaticIntervalContainer.intervalTimeHours,
                automaticIntervalContainer.intervalTimeMinutes,
                automaticIntervalContainer.intervalStartHours,
                automaticIntervalContainer.intervalStartMinutes);
        setPreferenceSummary(preferenceIntervalTime, preferenceSummary);
    }

    private boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "wallpaperDir":
                uriActivityResultLauncher.launch(Uri.parse("image/*"));
                return true;
            case "automaticInterval":
                // Start dialog
                ConfigureAutomaticModeDialogFragment dialog = new ConfigureAutomaticModeDialogFragment();
                dialog.show(getParentFragmentManager(), null);
                return true;
        }
        return false;
    }

    @Override
    public void onConfigureAutomaticModeDialogDismissListener(AutomaticIntervalContainer automaticIntervalContainer) {
        // Initialize variables
        String automaticIntervalContainerString;

        // Serialize to string
        try {
            automaticIntervalContainerString = toString(automaticIntervalContainer);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Save in sharedPreferences
        sharedPreferences.edit().
                putString(getString(R.string.key_automatic_interval), automaticIntervalContainerString).
                apply();

        // Change summary of preference
        String preferenceSummary = String.format(getString(R.string.preference_interval_start_time_summary_string),
                automaticIntervalContainer.intervalTimeHours,
                automaticIntervalContainer.intervalTimeMinutes,
                automaticIntervalContainer.intervalStartHours,
                automaticIntervalContainer.intervalStartMinutes);

        setPreferenceSummary(preferenceIntervalTime, preferenceSummary);
    }

    /** Write the object to a Base64 string. */
    private static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /** Read the object from Base64 string. */
    private static Object fromString( String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }
}

