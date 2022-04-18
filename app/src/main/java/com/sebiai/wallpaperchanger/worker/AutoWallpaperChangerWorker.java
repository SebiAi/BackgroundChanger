package com.sebiai.wallpaperchanger.worker;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sebiai.wallpaperchanger.MyFileHandler;
import com.sebiai.wallpaperchanger.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AutoWallpaperChangerWorker extends Worker {
    private Context context;
    private WorkerParameters workerParams;

    private SharedPreferences sharedPreferences;

    public AutoWallpaperChangerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.workerParams = workerParams;

        // Get shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.GERMAN);
        Log.d("AutoWallpaperChangerWorker", "Worker run at " + sdf.format(Calendar.getInstance(Locale.GERMAN).getTime()));
        // Get Uri
        Uri wallpaperUri = Uri.parse(sharedPreferences.getString(context.getString(R.string.key_wallpaper_dir), null));

        // If no directory is set then return
        if (wallpaperUri == null)
            return Result.success();

        // Set file as wallpaper
        DocumentFile file = MyFileHandler.setRandomFileAsWallpaper(context, wallpaperUri);
        if (file != null) {
            String fileName = file.getName();
            // Save preferences
            int amountChangesAutomatic = sharedPreferences.getInt(context.getString(R.string.key_amount_changes_automatic), 0);
            sharedPreferences.edit().
                    putString(context.getString(R.string.key_current_picture), file.getUri().toString()).
                    putInt(context.getString(R.string.key_amount_changes_automatic), ++amountChangesAutomatic).
                    apply();
            // Set cache if possible
            if (getMyApplication(context) != null) {
                getMyApplication(context).wallpaperFileName = fileName;
                getMyApplication(context).wallpaperDrawableCache = MyFileHandler.getDrawableFromWallpaperUri(context, file.getUri());
            }
            return Result.success();
        }

        return Result.failure();
    }
}
