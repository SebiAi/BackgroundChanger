package com.sebiai.backgroundchanger;

import static com.sebiai.backgroundchanger.MyApplicationHelper.getMyApplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Button buttonChooseDir;
    private Button buttonSetRandomBackground;
    private TextView textViewCurrentWallpaper;
    private ActivityResultLauncher<Uri> uriActivityResultLauncher;

    SharedPreferences sharedPreferences;
    private final String PREFERENCEKEY_WALLPAPER_DIR = "PREFERENCEKEY_WALLPAPER_DIR";
    private final String PREFERENCEKEY_LAST_WALLPAPER_NAME = "PREFERENCEKEY_LAST_WALLPAPER_NAME";

    public HomeFragment() {
        // Required empty public constructor
        uriActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), result -> {
            if (result == null)
                return;

            // Make persistent
            requireActivity().getContentResolver().takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Save globally and in preferences
            getMyApplication(requireContext()).wallpaperDir = result;
            sharedPreferences.edit().putString(PREFERENCEKEY_WALLPAPER_DIR, result.toString()).apply();

            // Enable button
            buttonSetRandomBackground.setEnabled(true);

//                final ArrayList<Uri> uris = MyFileHandler.getFiles(requireContext(), result);
//
//                DocumentFile file = MyFileHandler.getRandomFile(requireContext(), uris);
//                if (file != null) {
//                    // Log
//                    Log.d("Result", file.getName() + "\t" + file.getUri().getPath());
//                    // Set as Wallpaper
//                    try {
//                        InputStream stream = requireContext().getContentResolver().openInputStream(file.getUri());
//                        Drawable drawable = Drawable.createFromStream(stream, file.getUri().getPath());
//                        stream.close();
//                        frameLayout.setBackground(drawable);
//
//                        MyFileHandler.setFileAsWallpaper(requireContext(), file);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        // Load Uri
        if (sharedPreferences.contains(PREFERENCEKEY_WALLPAPER_DIR)) {
            getMyApplication(requireContext()).wallpaperDir = Uri.parse(sharedPreferences.getString(PREFERENCEKEY_WALLPAPER_DIR, null));
            buttonSetRandomBackground.setEnabled(true);
        }
        // Load last set wallpaper name
        if (sharedPreferences.contains(PREFERENCEKEY_LAST_WALLPAPER_NAME)) {
            textViewCurrentWallpaper.setText(String.format(getString(R.string.textview_current_wallpaper_string), sharedPreferences.getString(PREFERENCEKEY_LAST_WALLPAPER_NAME, "-")));
        }
    }

    private void setup() {
        buttonChooseDir = requireView().findViewById(R.id.button_choose_dir);
        buttonChooseDir.setOnClickListener(v -> uriActivityResultLauncher.launch(Uri.parse("image/*")));

        buttonSetRandomBackground = requireView().findViewById(R.id.button_set_random_background);
        buttonSetRandomBackground.setOnClickListener(v -> {
            DocumentFile file = MyFileHandler.setRandomFileAsWallpaper(requireContext());
            if (file != null) {
                String fileName = file.getName();
                textViewCurrentWallpaper.setText(String.format(getString(R.string.textview_current_wallpaper_string), fileName));
                sharedPreferences.edit().putString(PREFERENCEKEY_LAST_WALLPAPER_NAME, fileName).apply();
            }
        });

        textViewCurrentWallpaper = requireView().findViewById(R.id.textview_current_wallpaper);
        textViewCurrentWallpaper.setText(String.format(getString(R.string.textview_current_wallpaper_string), "-"));
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if uri is still valid
        if (!MyFileHandler.isWallpaperDirValid(requireContext())) {
            buttonSetRandomBackground.setEnabled(false);
        }
    }
}