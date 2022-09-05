package com.sebiai.wallpaperchanger.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sebiai.wallpaperchanger.R;
import com.sebiai.wallpaperchanger.WallpaperBrowserRecyclerViewAdapter;
import com.sebiai.wallpaperchanger.objects.Wallpaper;
import com.sebiai.wallpaperchanger.utils.FileUtil;
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
        // TODO: [NOW] Convert into util
        // TODO: [NOW] Get Image in ImageBrowserActivity and pass it to this when clicked
        imageViewWallpaper.setImageBitmap(decodeSampledBitmapFromStream());

        // Button
        buttonSelectWallpaper = findViewById(R.id.button_select_wallpaper);
        // TODO: Modify button visibility depending on the arguments passed
        // TODO: Handle onClick for the select wallpaper button
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public Bitmap decodeSampledBitmapFromStream() {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Wallpaper w = new Wallpaper(this.getApplicationContext(), Uri.parse(this.getIntent().getExtras().getString("wallpaper")));
        BitmapFactory.decodeStream(w.getInputStream(), null, options);

        // Calculate inSampleSize
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(w.getInputStream(), null, options);
    }
}