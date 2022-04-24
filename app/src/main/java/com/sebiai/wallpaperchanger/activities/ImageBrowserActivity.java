package com.sebiai.wallpaperchanger.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sebiai.wallpaperchanger.R;
import com.sebiai.wallpaperchanger.WallpaperBrowserRecyclerViewAdapter;
import com.sebiai.wallpaperchanger.objects.Wallpaper;
import com.sebiai.wallpaperchanger.utils.MyFileHandler;

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
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getApplicationContext(),
                "Clicked '" + recyclerViewAdapter.getItem(position).getWallpaperName() + "' at " + position, Toast.LENGTH_SHORT).show();
    }
}