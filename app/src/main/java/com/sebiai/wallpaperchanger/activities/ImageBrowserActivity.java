package com.sebiai.wallpaperchanger.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sebiai.wallpaperchanger.R;
import com.sebiai.wallpaperchanger.WallpaperBrowserRecyclerViewAdapter;
import com.sebiai.wallpaperchanger.objects.Wallpaper;
import com.sebiai.wallpaperchanger.utils.MyFileHandler;

import java.util.Objects;

public class ImageBrowserActivity extends AppCompatActivity implements WallpaperBrowserRecyclerViewAdapter.ItemClickListener {
    private Toolbar customToolbar;
    private SwitchCompat switchAutoChange;

    private RecyclerView recyclerView;
    private WallpaperBrowserRecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_browser);

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

        // Prepare data for the recycler view
        Wallpaper[] wallpapers = Wallpaper.parse(this, MyFileHandler.getFiles(this));

        // Recycler view
        recyclerView = findViewById(R.id.recycler_view_image_browser);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewAdapter = new WallpaperBrowserRecyclerViewAdapter(this, wallpapers);
        recyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(recyclerViewAdapter);

        // Fade animation
        Fade fade = new Fade();
        fade.excludeTarget(R.id.include_custom_toolbar, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }

    @Override
    public void onItemClick(View view, int position) {
        // Get wallpaper
        Uri wallpaper = recyclerViewAdapter.getItem(position).getWallpaperUri();

        // Get image view
        ImageView imageView = view.findViewById(R.id.image_view_wallpaper);

        // Transition
        Intent intent = new Intent(ImageBrowserActivity.this, ImageActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                ImageBrowserActivity.this, imageView, Objects.requireNonNull(ViewCompat.getTransitionName(imageView)));
        Bundle bundle = new Bundle();
        bundle.putString("wallpaper", wallpaper.toString());
        intent.putExtras(bundle);
        startActivity(intent, options.toBundle());
        //startActivity(intent);
    }
}