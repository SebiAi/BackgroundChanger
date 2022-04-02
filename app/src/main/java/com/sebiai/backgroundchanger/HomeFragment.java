package com.sebiai.backgroundchanger;

import static com.sebiai.backgroundchanger.MyApplicationHelper.getMyApplication;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Button buttonChooseDir;
    private Button buttonSetRandomBackground;
    private TextView textViewCurrentWallpaper;
    private ActivityResultLauncher<Uri> uriActivityResultLauncher;

    public HomeFragment() {
        // Required empty public constructor
        uriActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), result -> {
            if (result == null)
                return;

            // Save globally
            getMyApplication(requireContext()).wallpaperDir = result;

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
    }

    private void setup() {
        buttonChooseDir = requireView().findViewById(R.id.button_choose_dir);
        buttonChooseDir.setOnClickListener(v -> uriActivityResultLauncher.launch(Uri.parse("image/*")));

        buttonSetRandomBackground = requireView().findViewById(R.id.button_set_random_background);
        buttonSetRandomBackground.setOnClickListener(v -> {
            DocumentFile file = MyFileHandler.setRandomFileAsWallpaper(requireContext());
            if (file != null)
                textViewCurrentWallpaper.setText(String.format(getString(R.string.textview_current_wallpaper_string), file.getName()));
        });

        textViewCurrentWallpaper = requireView().findViewById(R.id.textview_current_wallpaper);
        textViewCurrentWallpaper.setText(String.format(getString(R.string.textview_current_wallpaper_string), "-"));
    }
}