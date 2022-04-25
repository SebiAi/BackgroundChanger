package com.sebiai.wallpaperchanger.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;

import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sebiai.wallpaperchanger.R;
import com.sebiai.wallpaperchanger.WallpaperBrowserRecyclerViewAdapter;
import com.sebiai.wallpaperchanger.objects.Wallpaper;
import com.sebiai.wallpaperchanger.utils.GlideApp;
import com.sebiai.wallpaperchanger.utils.MyFileHandler;
import com.sebiai.wallpaperchanger.views.TouchImageView;

import java.util.Objects;

public class ImageActivity extends AppCompatActivity {
    private Toolbar customToolbar;
    private SwitchCompat switchAutoChange;

    private TouchImageView imageViewWallpaper;
    private Button buttonSelectWallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        setup();
    }

    private void setup() {
        // get Toolbar
        customToolbar = findViewById(R.id.toolbar_custom);
        setSupportActionBar(customToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // TODO: Finish activity

        // Disable switch in toolbar
        switchAutoChange = findViewById(R.id.switch_enable_auto_change);
        switchAutoChange.setVisibility(View.GONE);

        // Fade animation
        Fade fade = new Fade();
        fade.excludeTarget(R.id.include_custom_toolbar, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        // Image View
        imageViewWallpaper = findViewById(R.id.image_view_wallpaper);

        // Load image
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_wallpaper)
                .error(R.drawable.default_wallpaper);

        Glide.with(this).load(Uri.parse(this.getIntent().getExtras().getString("wallpaper"))).into(imageViewWallpaper);

        // Button
        buttonSelectWallpaper = findViewById(R.id.button_select_wallpaper);
        // TODO: Modify button visibility depending on the arguments passed
        // TODO: Handle onClick for the select wallpaper button
    }
}